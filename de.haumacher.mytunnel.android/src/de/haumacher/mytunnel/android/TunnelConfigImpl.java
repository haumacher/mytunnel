package de.haumacher.mytunnel.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class TunnelConfigImpl implements Comparable<TunnelConfigImpl> {
	
	public static final int SSH_DEFAULT_PORT = 22;
	
	private final String _id;
	
	private String _user;
	private String _host;
	private int _port;

	public TunnelConfigImpl(String tunnelId) {
		_id = tunnelId;
		setPort(22);
	}
	
	public String getId() {
		return _id;
	}
	
	public String getUser() {
		return _user;
	}
	
	public String getHost() {
		return _host;
	}
	
	public int getPort() {
		return _port;
	}

	public String getPortString() {
		return Integer.toString(getPort());
	}

	public TunnelConfigImpl update(String user, String host, int port) {
		setUser(user);
		setHost(host);
		setPort(port);
		return this;
	}

	public void setUser(String user) {
		_user = user;
	}

	public void setHost(String host) {
		_host = host;
	}

	public void setPort(int port) {
		_port = port;
	}

	public TunnelConfigImpl load(SharedPreferences preferences) {
		String prefix = prefix();
		setHost(getString(preferences, hostKey(prefix), getHost()));
		setUser(getString(preferences, userKey(prefix), getUser()));
		String portString = getString(preferences, portKey(prefix), null);
		if (portString != null) {
			setPort(Integer.parseInt(portString));
		}
		return this;
	}

	private String getString(SharedPreferences preferences, String key, String defaultValue) {
		try {
			return preferences.getString(key, defaultValue);
		} catch (ClassCastException ex) {
			return null;
		}
	}

	public void store(Editor editor) {
		String prefix = prefix();
		editor.putString(hostKey(prefix), getHost());
	    editor.putString(userKey(prefix), getUser());
	    editor.putString(portKey(prefix), getPortString());
	}
	
	public void delete(Editor editor) {
		String prefix = prefix();
		editor.remove(userKey(prefix));
		editor.remove(hostKey(prefix));
		editor.remove(portKey(prefix));
	}

	public String prefix() {
		return "tunnel." + _id;
	}

	public String portKey(String prefix) {
		return prefix + ".port";
	}

	public String userKey(String prefix) {
		return prefix + ".user";
	}

	public String hostKey(String prefix) {
		return prefix + ".host";
	}

	@Override
	public int compareTo(TunnelConfigImpl other) {
		int userResult = getUser().compareTo(other.getUser());
		if (userResult != 0) {
			return userResult;
		}
		int hostResult = getHost().compareTo(other.getHost());
		if (hostResult != 0) {
			return hostResult;
		}
		return compare(getPort(), other.getPort());
	}

	private int compare(int x1, int x2) {
		return x1 < x2 ? -1 : x1 > x2 ? 1 : 0;
	}

	@Override
	public String toString() {
		return getUser() + "@" + getHost() + (getPort() != SSH_DEFAULT_PORT ? ":" + getPort() : "");
	}

	public static List<TunnelConfigImpl> loadTunnels(SharedPreferences preferences) {
		Map<String, TunnelConfigImpl> tunnelsById1 = new HashMap<String, TunnelConfigImpl>();
		for (String key : preferences.getAll().keySet()) {
			if (key.startsWith("tunnel.")) {
				int idStart = "tunnel.".length();
				int idEnd = key.indexOf('.', idStart);
				if (idEnd < 0) {
					continue;
				}
				String id = key.substring(idStart, idEnd);
				if (!tunnelsById1.containsKey(id)) {
					tunnelsById1.put(id, load(preferences, id));
				}
			}
		}
		Map<String, TunnelConfigImpl> tunnelsById = tunnelsById1;
		
		List<TunnelConfigImpl> tunnels = new ArrayList<TunnelConfigImpl>(tunnelsById.values());
		Collections.sort(tunnels);
		return tunnels;
	}

	public static TunnelConfigImpl load(SharedPreferences preferences, String tunnelId) {
		return new TunnelConfigImpl(tunnelId).load(preferences);
	}

}