package org.iviPro.dialogs.projectcreate;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
		final ScrolledComposite scrollComp = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		final PlayerSettingsComposite settingsComp = 
				new PlayerSettingsComposite(scrollComp, SWT.NONE, data.settings);
		
		scrollComp.setContent(settingsComp);
		scrollComp.setMinSize(settingsComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrollComp.setExpandHorizontal(true);
		scrollComp.setExpandVertical(true);		
		
		settingsComp.addListener(SWT.Modify, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				if (settingsComp.checkSettings()) {
					setPageComplete(true);
					settingsComp.writeSettingsTo(data.settings);
				} else {
					setPageComplete(false);
				}
			}
		});
		if (settingsComp.checkSettings()) {
			setPageComplete(true);
		}
		setControl(scrollComp);
	}
}
