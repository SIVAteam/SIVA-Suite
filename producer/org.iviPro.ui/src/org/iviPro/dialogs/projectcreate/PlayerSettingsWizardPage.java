package org.iviPro.dialogs.projectcreate;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.model.ProjectSettings;
import org.iviPro.projectsettings.PlayerSettingsComposite;

public class PlayerSettingsWizardPage extends WizardPage {
	
	private ProjectCreateData data;
	
	protected PlayerSettingsWizardPage(ProjectCreateData data) {
		super(""); //$NON-NLS-1$
		data.settings = new ProjectSettings(data.project);
		this.data = data;
	}

	@Override
	public void createControl(Composite parent) {
		final PlayerSettingsComposite settingsComp = 
				new PlayerSettingsComposite(parent, SWT.NONE, data.settings);
		settingsComp.addListener(SWT.Modify, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				if (settingsComp.checkSettings()) {
					setPageComplete(true);
					data.settings = settingsComp.getSettings();
				} else {
					setPageComplete(false);
				}
			}
		});
		if (settingsComp.checkSettings()) {
			setPageComplete(true);
		}
		setControl(settingsComp);
	}
}
