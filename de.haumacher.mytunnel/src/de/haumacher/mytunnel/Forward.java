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

import java.net.InetAddress;


public class Forward {

	private final LazyChannelProvider _sessionProvider;
	
	private final InetAddress _bindAddress;
	private final int _localPort;
	private final String _remoteHost;
	private final int _remotePort;

	private OnDemandPortForwarder _forwarder;

	public Forward(LazyChannelProvider sessionProvider, InetAddress bindAddress, int localPort, String remoteHost, int remotePort) {
		_sessionProvider = sessionProvider;
		
		_localPort = localPort;
		_remoteHost = remoteHost;
		_remotePort = remotePort;
		_bindAddress = bindAddress;
	}

	public void start() {
		_forwarder = OnDemandPortForwarder.createForwarder(_sessionProvider, _bindAddress, _localPort, _remoteHost, _remotePort, null);
	}
	
	public void close() {
		if (_forwarder != null) {
			_forwarder.close();
			_forwarder = null;
		}
	}

}
