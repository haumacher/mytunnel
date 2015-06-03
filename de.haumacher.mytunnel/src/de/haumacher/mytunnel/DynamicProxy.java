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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UserInfo;

public class DynamicProxy {
	
	private final UserInfo _ui;
	
	private final JSch _ssh;
	
	private final List<Tunnel> _tunnels;

	public DynamicProxy(UserInfo ui) {
		_ui = ui;
		_ssh = new JSch();
		_tunnels = new ArrayList<Tunnel>();
	}

	public void init(Properties properties) throws JSchException, UnknownHostException {
		for (int n = 1; true; n++) {
			String fileName = properties.getProperty("id" + n + ".file");
			if (fileName == null) {
				break;
			}
			String password = properties.getProperty("id" + n + ".password");
			
			System.out.println("Adding identity: " + fileName);
			addIdentity(fileName, password);
		}
		
		String knownHostsFile = System.getProperty("user.home") + File.separatorChar + ".ssh" + File.separatorChar  + "known_hosts";
		System.out.println("Using known hosts file: " + knownHostsFile);
		setKnownHosts(knownHostsFile);
		
		for (int t = 1; true; t++) {
			String tunnelPrefix = "tunnel" + t;
			
			String hostName = properties.getProperty(tunnelPrefix + ".host");
			if (hostName == null) {
				break;
			}
			
			String userName = properties.getProperty(tunnelPrefix + ".user");
			int port = Integer.parseInt(properties.getProperty(tunnelPrefix + ".port"));
			System.out.println("Setting up tunnel to: " + userName + "@" + hostName + ":" + port);
			
			Tunnel tunnel = createTunnel(userName, hostName, port);
			
			for (int n = 1; true; n++) {
				String forwardPrefix = tunnelPrefix + ".forward" + n;
				
				String forwardHost = properties.getProperty(forwardPrefix + ".host");
				if (forwardHost == null) {
					break;
				}
				int localPort = Integer.parseInt(properties.getProperty(forwardPrefix + ".localPort"));
				int remotePort = Integer.parseInt(properties.getProperty(forwardPrefix + ".remotePort"));
				InetAddress bindAddress = InetAddress.getByName(properties.getProperty(forwardPrefix + ".bindAddress"));
				
				System.out.println("Adding local forward: " + localPort + "->" + forwardHost + ":" + remotePort);
				tunnel.addPortForwarding(bindAddress, localPort, forwardHost, remotePort);
			}
		}
	}

	public void setKnownHosts(String knownHostsFile) throws JSchException {
		_ssh.setKnownHosts(knownHostsFile);
	}
	
	public void addIdentity(String fileName, String password) throws JSchException {
		_ssh.addIdentity(fileName, password);
	}
	
	public Tunnel createTunnel(String userName, String hostName, int port) {
		Tunnel tunnel = new Tunnel(_ui, _ssh, userName, hostName, port);
		_tunnels.add(tunnel);
		return tunnel;
	}

	public void start() {
		for (Tunnel tunnel : _tunnels) {
			tunnel.start();
		}
	}

	public void stop() {
		for (Tunnel tunnel : _tunnels) {
			tunnel.stop();
		}
	}

	public static File configFile() {
		String configName = System.getProperty("user.home") + File.separatorChar + ".mytunnel" + File.separatorChar + "config.properties";
		File configFile = new File(configName);
		return configFile;
	}

	public static Properties loadConfig() throws IOException {
		Properties properties = new Properties();
		File configFile = DynamicProxy.configFile();
		if (configFile.exists()) {
			FileInputStream in = new FileInputStream(configFile);
			try {
				properties.load(in);
			} finally {
				in.close();
			}
		}
		return properties;
	}
}