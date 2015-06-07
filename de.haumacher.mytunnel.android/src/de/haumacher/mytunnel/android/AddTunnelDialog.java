package de.haumacher.mytunnel.android;

import java.util.UUID;

import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class AddTunnelDialog extends DialogPreference {

    private EditText _host;
	private EditText _user;
	private EditText _port;
	private TunnelConfigImpl _tunnel;
	private OnDismissListener _onDismissListener;

	public AddTunnelDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
    	
    	_host = (EditText) view.findViewById(R.id.createTunnelHost);
    	_user = (EditText) view.findViewById(R.id.createTunnelUser);
    	_port = (EditText) view.findViewById(R.id.createTunnelPort);
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        
        if (positiveResult) {
        	TunnelConfigImpl tunnel = getTunnel();
        	
        	SharedPreferences preferences = getSharedPreferences();
        	SharedPreferences.Editor editor = preferences.edit();
        	tunnel.store(editor);
        	editor.commit(); 
        	
        	if (_onDismissListener != null) {
        		_onDismissListener.onDismiss(this.getDialog());
        	}
        }
    }

	private TunnelConfigImpl getTunnel() {
		return mkTunnel().update(getUser(), getHost(), getPort());
	}

	public void setTunnel(TunnelConfigImpl tunnel) {
		_tunnel = tunnel;
		
		_user.setText(tunnel.getUser());
		_host.setText(tunnel.getHost());
		_port.setText(Integer.toString(tunnel.getPort()));
		
		setIcon(null);
		setTitle(tunnel.toString());
	}

	private TunnelConfigImpl mkTunnel() {
		if (_tunnel == null) {
			_tunnel = new TunnelConfigImpl(newId());
		}
		return _tunnel;
	}

	private String newId() {
		return UUID.randomUUID().toString();
	}

	private int getPort() {
		return Integer.parseInt(toString(_port.getText()));
	}

	private String getHost() {
		return toString(_host.getText());
	}

	private String getUser() {
		return toString(_user.getText());
	}

	private String toString(Editable text) {
		if (text == null) {
			return "";
		}
		return text.toString();
	}

	public void setOnTunnelAdded(OnDismissListener onDismissListener) {
		_onDismissListener = onDismissListener;
	}

}