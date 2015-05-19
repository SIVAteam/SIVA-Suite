package org.iviPro.dialogs.projectcreate;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.projectsettings.Startmode;

public class ProjectSetStartmodePage extends WizardPage {

	private ProjectCreateData data;
	
	protected ProjectSetStartmodePage() {
		super(""); //$NON-NLS-1$
		setTitle(Messages.ProjectSetStartmodePage_StartmodeTitle);
		setDescription(Messages.ProjectSetStartmodePage_StartmodeDescription);
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {
		data = ((ProjectCreateWizard) getWizard()).getData();

		final Startmode startmode = new Startmode(parent, SWT.None);
		
		startmode.addListener(SWT.Modify, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				setPageComplete(true);
				data.fullscreen = startmode.fullscreen;
			}
		});
		
		setControl(startmode);
		startmode.init();
	}

}
