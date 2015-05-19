package org.iviPro.dialogs.projectcreate;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.projectsettings.LayoutEditor;

public class ProjectSetLayoutPage extends WizardPage {

	private ProjectCreateData data;
	
	protected ProjectSetLayoutPage() {
		super(""); //$NON-NLS-1$
		setTitle(Messages.ProjectSetLayoutPage_LayoutTitle);
		setDescription(Messages.ProjectSetLayoutPage_LayoutDescription);
		setPageComplete(false);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createControl(Composite parent) {
		data = ((ProjectCreateWizard) getWizard()).getData();
		final LayoutEditor layout = new LayoutEditor(parent, SWT.None);
		
		layout.addListener(SWT.Modify, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				data.sizeWidth = layout.getSizeWidth();
				data.sizeHeight = layout.getSizeHeight();
				data.areaLeftWidth = layout.getAreaLeftWidth();
				data.areaTopHeight = layout.getAreaTopHeight();
				data.areaBottomHeight = layout.getAreaBottomHeight();
				data.areaRightWidth = layout.getAreaRightWidth();
				setPageComplete(true);
			}
		});
		layout.init();
		setControl(layout);

	}

}
