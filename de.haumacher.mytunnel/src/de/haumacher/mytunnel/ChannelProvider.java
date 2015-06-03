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
 * Factory for {@link Channel}s.
 * 
 * <p>
 * Abstraction of a JSch {@link Session}. A {@link ChannelProvider} can be
 * created without having already established a real SSH {@link Session}.
 * </p>
 * 
 * @see LazyChannelProvider
 * 
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 * @version $Revision: $ $Author: $ $Date: $
 */
public interface ChannelProvider {

	void start();
	
	void stop();
	
	Channel openChannel(String type) throws JSchException;

}
