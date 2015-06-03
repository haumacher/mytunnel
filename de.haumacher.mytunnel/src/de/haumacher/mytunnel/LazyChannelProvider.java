/*
 * MyTunnel on-demand connection to any SSH server on the net.
 * Copyright (C) 2015 Bernhard Haumacher and others
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.haumacher.mytunnel;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * {@link ChannelProvider} that opens the corresponding {@link Session} at the
 * time the first {@link Channel} is requested.
 * 
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 * @version $Revision: $ $Author: $ $Date: $
 */
public class LazyChannelProvider implements ChannelProvider, Runnable {

	private static final int MILLIS = 1;

	private static final int SEC = 1000 * MILLIS;

	private static final int MIN = 60 * SEC;

	private final UserInfo _ui;
	
	private final JSch _jsch;

	private final String _user;

	private final String _host;

	private final int _port;

	private SessionWrapper _session;

	private boolean _active;

	private long _idleTimeout = 1 * MIN;

	private long _pollingIntervall = 20 * SEC;

	private Thread _timer;

	public LazyChannelProvider(UserInfo ui, JSch jsch, String user, String host, int port) {
		this._ui = ui;
		_jsch = jsch;
		_user = user;
		_host = host;
		_port = port;
	}

	@Override
	public synchronized Channel openChannel(String type) throws JSchException {
		if (!_active) {
			throw new JSchException("Not active.");
		}

		if (_session == null || !_session.isConnected()) {
			createSession();
			if (_timer == null) {
				_timer = new Thread(this, "SessionTimer");
				_timer.start();
			}
		}

		return _session.openChannel(type);
	}

	private void createSession() throws JSchException {
		Session session = _jsch.getSession(_user, _host, _port);
		session.setUserInfo(_ui);
		SessionWrapper newSession = new SessionWrapperImpl(session);
		newSession.connect();
		_session = newSession;
	}

	@Override
	public synchronized void run() {
		try {
			while (_active) {
				if (_session != null) {
					long idleSince = _session.getIdleSince();
					long now = System.currentTimeMillis();
					if (now - idleSince > _idleTimeout) {
						_session.disconnect();
						_session = null;
					}
				} else {
					return;
				}

				wait(_pollingIntervall);
			}
		} catch (InterruptedException ex) {
			// Exit.
		} finally {
			_timer = null;
		}
	}

	@Override
	public synchronized void start() {
		if (!_active) {
			_active = true;
		}
	}
	
	@Override
	public synchronized void stop() {
		if (_active) {
			_active = false;

			if (_session != null) {
				_session.disconnect();
				_session = null;
			}

			notifyAll();
		}
	}

}
