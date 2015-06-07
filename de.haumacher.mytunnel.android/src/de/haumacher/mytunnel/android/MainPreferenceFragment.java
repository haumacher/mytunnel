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

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;

public class MainPreferenceFragment extends PreferenceFragment {
	
	private static final OnPreferenceChangeListener UPDATE_SUMMARY = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			preference.setSummary(newValue.toString());
			return true;
		}
	};
	
	private Context _context;

	private final DialogInterface.OnDismissListener _updateTunnels = new DialogInterface.OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface dialog) {
			updateTunnelPreferences();
		}
	};

	public MainPreferenceFragment(Context context) {
		_context = context;
	}
	
	public Context getContext() {
		return _context;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		updateTunnelPreferences();
	}
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		AddTunnelDialog addDialog = (AddTunnelDialog) findPreference("addTunnel");
		updateOnTunnelAdded(addDialog);
		
		updateTunnelPreferences();
	}
	
	void updateTunnelPreferences() {
		PreferenceManager manager = getPreferenceManager();
		final SharedPreferences preferences = manager.getSharedPreferences();
		
		List<TunnelConfigImpl> tunnels = TunnelConfigImpl.loadTunnels(preferences);
		
		Editor editor = null;
		PreferenceGroup tunnelGroup = (PreferenceGroup) findPreference("tunnels");
		tunnelGroup.removeAll();
		
		for (final TunnelConfigImpl tunnel : tunnels) {
			final PreferenceScreen screen;
			try {
				String prefix = tunnel.prefix();
				screen = manager.createPreferenceScreen(getContext());
				screen.setTitle(tunnel.toString());
				screen.setIcon(R.drawable.tunnel_settings);
				updateOnClose(screen);

				EditTextPreference userName = new EditTextPreference(getContext());
				userName.setTitle(R.string.userNameTitle);
				userName.getEditText().setSingleLine();
				userName.setSummary(tunnel.getUser());
				userName.setOnPreferenceChangeListener(UPDATE_SUMMARY);
				userName.setKey(tunnel.userKey(prefix));
				screen.addPreference(userName);
				
				EditTextPreference hostName = new EditTextPreference(getContext());
				hostName.getEditText().setSingleLine();
				hostName.setTitle(R.string.hostNameTitle);
				hostName.setSummary(tunnel.getHost());
				hostName.setOnPreferenceChangeListener(UPDATE_SUMMARY);
				hostName.setKey(tunnel.hostKey(prefix));
				screen.addPreference(hostName);
				
				EditTextPreference port = new EditTextPreference(getContext());
				port.getEditText().setSingleLine();
				port.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
				port.setTitle(R.string.portTitle);
				port.setSummary(tunnel.getPortString());
				port.setOnPreferenceChangeListener(UPDATE_SUMMARY);
				port.setKey(tunnel.portKey(prefix));
				screen.addPreference(port);
				
				PreferenceCategory forwards = new PreferenceCategory(getContext());
				forwards.setTitle(R.string.forwardsTitle);
				screen.addPreference(forwards);
				
				Preference delete = new Preference(getContext());
				delete.setIcon(R.drawable.delete);
				delete.setTitle(R.string.deleteTunnelTitle);
				delete.setSummary(R.string.deleteTunnelSummary);
				delete.setOnPreferenceClickListener(onDeleteListener(screen, tunnel));
				screen.addPreference(delete);
			} catch (Throwable ex) {
				if (editor == null) {
					editor = preferences.edit();
				}
				tunnel.delete(editor);
				continue;
			}
			
			tunnelGroup.addPreference(screen);
		}
		
		if (editor != null) {
			editor.commit();
		}
	}

	private OnPreferenceClickListener onDeleteListener(final PreferenceScreen screen, final TunnelConfigImpl tunnel) {
		return new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Editor editor = getPreferenceManager().getSharedPreferences().edit();
				tunnel.delete(editor);
				editor.commit();
				
				updateTunnelPreferences();
				screen.getDialog().dismiss();
				
				return true;
			}
		};
	}

	private void updateOnClose(final PreferenceScreen screen) {
		final OnPreferenceClickListener chain = screen.getOnPreferenceClickListener();
		screen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (chain != null) {
					chain.onPreferenceClick(preference);
				}
				screen.getDialog().setOnDismissListener(updateListener());
				return false;
			}
		});
	}

	private void updateOnTunnelAdded(final AddTunnelDialog dialog) {
		dialog.setOnTunnelAdded(updateListener());
	}

	OnDismissListener updateListener() {
		return _updateTunnels;
	}
}