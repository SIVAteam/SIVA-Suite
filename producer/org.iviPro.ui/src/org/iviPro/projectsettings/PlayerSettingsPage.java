package org.iviPro.projectsettings;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.iviPro.application.Application;
import org.iviPro.model.ProjectSettings;

/**
 * Preference page used for editing the settings the player should use when
 * displaying the interactive video created by the actual project.
 * @author John
 */
public class PlayerSettingsPage extends PreferencePage {
	
	private static Logger logger = Logger.getLogger(PlayerSettingsPage.class);
	
	private final ProjectSettings settings;
	private PlayerSettingsComposite settingsComp;
	
	public PlayerSettingsPage() {
		super(Messages.PlayerSettingsPage_PlayerSettingsTitle);
		settings = Application.getCurrentProject().getSettings();
		//setDescription(Messages.PagePlayerSettings_PlayerSettings_Description);
	}

	@Override
	protected Control createContents(Composite parent) {
		settingsComp = new PlayerSettingsComposite(parent, SWT.NONE, settings);
		return settingsComp;
	}
	
	@Override
	public boolean performOk() {
		if (!settingsComp.checkSettings()) {
			return false;
		}
		settingsComp.updateProjectSettings();
		return true;
	}
	
	@Override
	protected void performDefaults() {
		settingsComp.setToDefault();
		super.performDefaults();
	}
	
	@Override
	protected void performApply() {
		if (settingsComp.checkSettings()) {
			//Preview not finished yet
			//settingsComp.repaintPreview();
		}		
		super.performApply();
	}
}