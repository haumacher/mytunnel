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
package de.haumacher.mytunnel.swt;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import com.jcraft.jsch.JSchException;

import de.haumacher.mytunnel.DynamicProxy;

public class Application implements Runnable {

	private static final Logger LOG = Logger.getLogger(Application.class.getName());

	private Display _display;
	private Shell _shell;
	private TrayItem _trayItem;

	private Image _activeImage;

	private Image _inactiveImage;

	private Image _disabledImage;

	private boolean _enabled;

	private boolean _active;

	private DynamicProxy _proxy;

	private Menu _menu;

	public void start() {
		new Thread(this, "MyTunnel UI").start();
	}

	private void setup() {
		try {
			_proxy = new DynamicProxy(new SwtUI(_shell));
			Properties config = DynamicProxy.loadConfig();
			_proxy.init(config);
		} catch (JSchException ex) {
			throw new RuntimeException(ex);
		} catch (UnknownHostException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private void updateIcon() {
		_trayItem.setImage(_enabled ? statusImage() : _disabledImage);		
	}

	private Image statusImage() {
		return _active ? _activeImage : _inactiveImage;
	}

	void openMenu() {
		if (_menu == null) {
			_menu = new Menu (_shell, SWT.POP_UP);
			
			MenuItem exitItem = new MenuItem(_menu, SWT.PUSH);
			exitItem.setText("Exit");
			exitItem.addListener (SWT.Selection, new Listener () {
				@Override
				public void handleEvent(Event event) {
					stop();
				}
			});
			
		}
		_menu.setVisible(true);
	}

	void toggleMode() {
		_enabled = !_enabled;
		if (_enabled) {
			startTunnels();
		} else {
			stopTunnels();
		}
		updateIcon();
	}

	private void startTunnels() {
		_proxy.start();
	}

	void stopTunnels() {
		_proxy.stop();
	}
	
	public void stop() {
		_display.syncExec(new Runnable () {
			@Override
			public void run() {
				stopTunnels();
				disposeUI();
			}
		});
	}

	@Override
	public void run() {
		createUI();
		_shell.setVisible(false);
		
		setup();
		
		while (!_shell.isDisposed ()) {
			try {
				if (!_display.readAndDispatch ()) _display.sleep ();
			} catch (Exception ex) {
				LOG.log(Level.SEVERE, "Internal error.", ex);
			}
		}
		disposeUI();
	}

	private void createUI() {
		_display = new Display ();
		
		_shell = new Shell (_display);
		_shell.setText("MyTunnel");

		final Tray systemTray = _display.getSystemTray();
		if (systemTray == null) {
			LOG.severe("The system tray is not available.");
			disposeUI();
			return;
		}

		_trayItem = new TrayItem (systemTray, SWT.NONE);
		_trayItem.setToolTipText("MyTunnel");
		_trayItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toggleMode();
			}

		});
		_trayItem.addListener (SWT.MenuDetect, new Listener () {
			@Override
			public void handleEvent (Event event) {
				openMenu();
			}
		});

		_activeImage = new Image(_display, Application.class.getResourceAsStream("smart-tunnel-active.png"));		
		_inactiveImage = new Image(_display, Application.class.getResourceAsStream("smart-tunnel-inactive.png"));		
		_disabledImage = new Image(_display, Application.class.getResourceAsStream("smart-tunnel-disabled.png"));		
		
		updateIcon();
	}

	void disposeUI() {
		_activeImage.dispose ();
		_inactiveImage.dispose ();
		_disabledImage.dispose ();
		_display.dispose ();
	}

}
