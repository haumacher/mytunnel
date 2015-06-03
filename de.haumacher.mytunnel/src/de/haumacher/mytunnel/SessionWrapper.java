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
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Abstraction for a JSch {@link Session} that monitors its usage.
 * 
 * @see #getIdleSince()
 * 
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 * @version $Revision: $ $Author: $ $Date: $
 */
public interface SessionWrapper {

	/**
	 * The time (compatible to {@link System#currentTimeMillis()} when this
	 * {@link SessionWrapper} became idle.
	 * 
	 * @return The time since when this session is idle, or
	 *         {@link Long#MAX_VALUE}, if this is not idle currently.
	 */
	long getIdleSince();

	/**
	 * @see Session#connect()
	 */
	void connect() throws JSchException;

	/**
	 * @see Session#disconnect()
	 */
	void disconnect();

	/**
	 * @see Session#isConnected()
	 */
	boolean isConnected();

	/**
	 * @see Session#openChannel(String)
	 */
	Channel openChannel(String type) throws JSchException;

}
