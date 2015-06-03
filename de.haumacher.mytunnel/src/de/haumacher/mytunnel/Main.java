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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

public class Main {
	
	public static void main(String[] arg) {
		final BufferedReader sysIn = new BufferedReader(new InputStreamReader(System.in));
		try {
			Properties properties = DynamicProxy.loadConfig();
			
			DynamicProxy proxy = new DynamicProxy(null);
			proxy.init(properties);
			proxy.start();
			
			// Wait until signal to stop.
			sysIn.readLine();
			
			proxy.stop();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}