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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.jcraft.jsch.UserInfo;

public class SwtUI implements UserInfo {

	private String _passphrase;
	private final Display _display;
	final Shell _shell;

	public SwtUI(Shell shell) {
		_shell = shell;
		_display = shell.getDisplay();
	}

	@Override
	public String getPassphrase() {
		return _passphrase;
	}

	@Override
	public String getPassword() {
		return getPassphrase();
	}

	@Override
	public boolean promptPassword(String message) {
		return promptPassphrase(message);
	}

	@Override
	public boolean promptPassphrase(String message) {
		return false;
	}

	@Override
	public boolean promptYesNo(final String message) {
		final boolean result[] = {false};
		_display.syncExec(new Runnable() {
			@Override
			public void run() {
				MessageBox messageBox = new MessageBox(_shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setMessage(message);
				result[0] = messageBox.open() == SWT.YES;
			}
		});
		return result[0];
	}

	@Override
	public void showMessage(final String message) {
		_display.syncExec(new Runnable() {
			@Override
			public void run() {
				MessageBox messageBox = new MessageBox(_shell, SWT.ICON_INFORMATION | SWT.OK);
				messageBox.setMessage(message);
			}
		});
	}

}
