package de.haumacher.mytunnel.android;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MainActivity extends PreferenceActivity {

	private final MainPreferenceFragment _fragment = new MainPreferenceFragment(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FragmentManager m = getFragmentManager();
		FragmentTransaction tx = m.beginTransaction();
		tx.replace(android.R.id.content, _fragment);
		tx.commit();
	}

}
