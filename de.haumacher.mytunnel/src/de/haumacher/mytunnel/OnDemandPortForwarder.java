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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import com.jcraft.jsch.ChannelDirectTCPIP;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ServerSocketFactory;

/**
 * {@link ServerSocket} watcher that uses a {@link ChannelProvider} to open a
 * SSH session on demand (when the first client connects to the monitored server
 * socket).
 * 
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 * @version $Revision: $ $Author: $ $Date: $
 */
public class OnDemandPortForwarder implements Runnable {

	private ChannelProvider _sessionProvider;

	private int _lport;

	private int _rport;

	private String _host;

	private InetAddress _bindAddress;

	private int _connectTimeout = 0;

	private final ServerSocketFactory _socketfactory;

	private volatile boolean _shouldStop;

	private ServerSocket _serverSocket;

	public static InetAddress fromString(String address) throws UnknownHostException {
		return InetAddress.getByName(normalize(address));
	}
	
	private static String normalize(String address) {
		if (address == null || address.length() == 0 || address.equals("*")) {
			return "0.0.0.0";
		} else if (address.equals("localhost")) {
			return "127.0.0.1";
		} else {
			return address;
		}
	}

	public static OnDemandPortForwarder createForwarder(
			ChannelProvider sessionProvider, InetAddress bindAddress, int lport,
			String host, int rport, ServerSocketFactory ssf) {
		OnDemandPortForwarder forwarder = new OnDemandPortForwarder(sessionProvider, bindAddress, lport, host, rport, ssf);

		new Thread(forwarder, "PortWatcher-" + lport).start();

		return forwarder;
	}

	OnDemandPortForwarder(ChannelProvider sessionProvider,
			InetAddress bindAddress, int lport, String host, int rport,
			ServerSocketFactory socketFactory) {
		_sessionProvider = sessionProvider;
		_lport = lport;
		_host = host;
		_rport = rport;
		_socketfactory = socketFactory;
		_bindAddress = bindAddress;
	}

	public void setConnectTimeout(int connectTimeout) {
		_connectTimeout = connectTimeout;
	}

	@Override
	public void run() {
		try {
			_serverSocket = openServerSocket();

			if (_lport == 0) {
				int assignedPort = _serverSocket.getLocalPort();
				if (assignedPort != -1)
					_lport = assignedPort;
			}

			while (!_shouldStop) {
				Socket socket = _serverSocket.accept();
				
				InputStream in;
				OutputStream out;
				try {
					socket.setTcpNoDelay(true);

					// Only accept Init clients.
					SocketAddress remoteAddress = socket.getRemoteSocketAddress();
					if (!(remoteAddress instanceof InetSocketAddress)) {
						socket.close();
						continue;
					}
					
					// Only accept connections from localhost.
					InetSocketAddress remoteInetAddress = (InetSocketAddress) remoteAddress;
					if (!remoteInetAddress.getAddress().isLoopbackAddress()) {
						socket.close();
						continue;
					}

					in = socket.getInputStream();
					out = socket.getOutputStream();
				} catch (Exception ex) {
					ex.printStackTrace();
					try {
						socket.close();
					} catch (Exception ex1) {
						// Ignore.
						ex.printStackTrace();
					}
					continue;
				}

				ChannelDirectTCPIP channel;
				try {
					channel = (ChannelDirectTCPIP) _sessionProvider.openChannel("direct-tcpip");
				} catch (JSchException ex) {
					ex.printStackTrace();
					continue;
				}

				try {
					channel.setInputStream(in);
					channel.setOutputStream(out);

					channel.setHost(_host);
					channel.setPort(_rport);
					channel.setOrgIPAddress(socket.getInetAddress()
							.getHostAddress());
					channel.setOrgPort(socket.getPort());

					channel.connect(_connectTimeout);
				} catch (JSchException ex) {
					ex.printStackTrace();
					try {
						channel.disconnect();
					} catch (Exception ex1) {
						// Ignore.
						ex1.printStackTrace();
					}
					continue;
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (RuntimeException ex) {
			ex.printStackTrace();
		}

		closeServerSocket();
		_serverSocket = null;
	}

	private ServerSocket openServerSocket() throws IOException {
		ServerSocket serverSocket;
		if (_socketfactory == null) {
			serverSocket = new ServerSocket(_lport, 0, _bindAddress);
		} else {
			serverSocket = _socketfactory.createServerSocket(_lport, 0,
					_bindAddress);
		}
		return serverSocket;
	}

	private void closeServerSocket() {
		try {
			if (_serverSocket != null) {
				_serverSocket.close();
			}
		} catch (Exception e) {
			// Ignore.
		}
	}

	public void close() {
		_shouldStop = true;

		// Make sure that the thread leaves the accept() method.
		closeServerSocket();
	}
}
