package org.iviPro.projectsettings;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Shell;

public class ProjectSettingsDialog extends PreferenceDialog {
	
	PreferenceManager manager;

	public ProjectSettingsDialog(Shell parentShell, PreferenceManager manager) {
		super(parentShell, manager);
		this.manager = manager;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.ProjectSettingsDialog_ProjectSettingsTitle);
	}
}
