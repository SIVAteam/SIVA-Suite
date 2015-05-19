package org.iviPro.dialogs.projectcreate;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.projectsettings.PlayerSettings;

public class ProjectChoosePlayerDesignPage extends WizardPage {

	private ProjectCreateData data;

	protected ProjectChoosePlayerDesignPage() {
		super(""); //$NON-NLS-1$
		setTitle(Messages.ProjectChoosePlayerDesignPage_PlayerDesignTitle);
		setDescription(Messages.ProjectChoosePlayerDesignPage_PlayerDesignDescription);
		setPageComplete(true);

	}

	@Override
	public void createControl(Composite parent) {
		data = ((ProjectCreateWizard) getWizard()).getData();
		final PlayerSettings playerSettings = new PlayerSettings(parent, SWT.None);

		playerSettings.addListener(SWT.Modify, new Listener() {

			@Override
			public void handleEvent(Event event) {

					data.skin = 0;
					data.designName = playerSettings.designName;
					data.designSchema = playerSettings.designSchema;
					data.colorSchema = playerSettings.colorSchema;
					data.backgroundColor = playerSettings.backgroundColor;
					data.borderColor = playerSettings.borderColor;
					data.textColor = playerSettings.fontColor;
					data.font = playerSettings.font;
					data.fontSize = playerSettings.fontSize;
					data.autoPlay = playerSettings.autoPlay;
					data.primaryColorValue = playerSettings.primaryColorValue;
					data.primaryColor = playerSettings.isPrimaryColor;
					setPageComplete(true);
			}
		});

		playerSettings.init();
		setControl(playerSettings);

	}
}
