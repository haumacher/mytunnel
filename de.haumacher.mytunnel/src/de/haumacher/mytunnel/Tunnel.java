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
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.UserInfo;

public class Tunnel {

	private final LazyChannelProvider _sessionProvider;
	
	private final List<Forward> _forwards = new ArrayList<Forward>();

	public Tunnel(UserInfo ui, JSch ssh, String user, String host, int port) {
		_sessionProvider = new LazyChannelProvider(ui, ssh, user, host, port);
	}
	
	public void addPortForwarding(InetAddress bindAddress, int localPort, String remoteHost, int remotePort) {
		_forwards.add(new Forward(_sessionProvider, bindAddress, localPort, remoteHost, remotePort));
	}

	public void start() {
		_sessionProvider.start();
		for (Forward forward : _forwards) {
			forward.start();
		}
	}
	
	public void stop() {
		for (Forward forward : _forwards) {
			forward.close();
		}
		_sessionProvider.stop();
	}

}
