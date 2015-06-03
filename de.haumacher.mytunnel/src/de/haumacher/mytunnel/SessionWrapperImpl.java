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

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Default {@link SessionWrapper} implementation.
 * 
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 * @version $Revision: $ $Author: $ $Date: $
 */
public final class SessionWrapperImpl implements SessionWrapper {
	
	private final Session _session;

	private Session _impl;

	private ConcurrentLinkedQueue<Channel> _channels = new ConcurrentLinkedQueue<Channel>();

	private long _idleSince = Long.MIN_VALUE;

	public SessionWrapperImpl(Session session) {
		_session = session;
		_impl = _session;
	}

	@Override
	public void connect() throws JSchException {
		_impl.connect();
	}

	@Override
	public Channel openChannel(String type) throws JSchException {
		Channel result = _impl.openChannel(type);
		addChannel(result);
		
		return result;
	}

	private void addChannel(Channel result) {
		cleanup();
		_channels.add(result);
		_idleSince = Long.MAX_VALUE;
	}

	private void cleanup() {
		for (Iterator<Channel> it = _channels.iterator(); it.hasNext(); ) {
			Channel channel = it.next();
			
			if (channel.isClosed()) {
				it.remove();
			}
		}
	}

	@Override
	public long getIdleSince() {
		cleanup();
		if (_channels.isEmpty()) {
			_idleSince = Math.min(_idleSince, System.currentTimeMillis());
		}
		return _idleSince;
	}

	@Override
	public boolean isConnected() {
		return _impl.isConnected();
	}

	@Override
	public void disconnect() {
		_impl.disconnect();
	}
}