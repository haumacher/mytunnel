/*
 * TimeCollect records time you spent on your development work.
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
package de.haumacher.mytunnel.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;

public class ForwardConfigImpl extends GenericConfig {

	private final String _parentPrefix;

	public ForwardConfigImpl(String parentPrefix, String id) {
		super(id, createProperties());
		
		_parentPrefix = parentPrefix;
	}

	private static Property[] createProperties() {
		Property[] result = {
			new StringProperty("remoteHost"),
			new IntProperty("remotePort"),
			new IntProperty("localPort"),
		};
		return result;
	}

	@Override
	protected String parentPrefix() {
		return _parentPrefix;
	}

	public static List<GenericConfig> loadForwards(SharedPreferences preferences, String parentPrefix) {
		Map<String, GenericConfig> forwardsById = new HashMap<String, GenericConfig>();
		for (String key : preferences.getAll().keySet()) {
			if (key.startsWith(parentPrefix)) {
				int idStart = parentPrefix.length();
				int idEnd = key.indexOf('.', idStart);
				if (idEnd < 0) {
					continue;
				}
				String id = key.substring(idStart, idEnd);
				if (!forwardsById.containsKey(id)) {
					forwardsById.put(id, load(preferences, parentPrefix, id));
				}
			}
		}
		List<GenericConfig> tunnels = new ArrayList<GenericConfig>(forwardsById.values());
		Collections.sort(tunnels);
		return tunnels;
	}

	public static GenericConfig load(SharedPreferences preferences, String parentPrefix, String id) {
		return new ForwardConfigImpl(parentPrefix, id).load(preferences);
	}


}
