package org.iviPro.projectsettings;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.application.Application;
import org.iviPro.model.ProjectSettings;

public class PageExtended extends PreferencePage {
	
	private Extended extended;
	private ProjectSettings settings;
	private SettingsStorer storer;
	
	public PageExtended(SettingsStorer storer) {
		super(Messages.PageExtended_Extended_Title);
		setDescription(Messages.PageExtended_Extended_Description);
		this.storer = storer;
	}

	@Override
	protected Control createContents(Composite parent) {

		extended = new Extended(parent, SWT.None);
		settings = Application.getCurrentProject().getSettings();
		if (settings != null) {
			extended.reload = settings.isAutoreload();
			extended.reloadTime = settings.getAutoreloadTime();
			extended.projectName = settings.getProjectName();
			extended.projectCollaborationID = settings.getProjectCollaborationID();
			extended.setField();
		}
		extended.addListener(SWT.Modify, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				setValid(true);
				
			}
		});
		return extended;
	}

	@Override
	public boolean performOk() {
		storer.storeSettings();
		return true;
	}
	
	
	@Override
	protected void performApply() {
		storer.storeSettings();
	}

	public boolean isInit() {
		return extended != null;
	}
	
	protected boolean getAutoReload() {
		return extended.reload;
	}
	
	protected int getAutoReloadTime() {
		return extended.reloadTime;
	}
	
	protected String getProjectName() {
		return extended.projectName;
	}
	
	protected int getProjectCollaborationID() {
		return extended.projectCollaborationID;
	}

	@Override
	protected void performDefaults() {
		if (settings != null && extended != null) {
			extended.init();
		} 
		super.performDefaults();
	}
	
	

}
