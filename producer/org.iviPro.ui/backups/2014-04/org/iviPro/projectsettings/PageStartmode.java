package org.iviPro.projectsettings;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.application.Application;
import org.iviPro.model.ProjectSettings;

public class PageStartmode extends PreferencePage {
	
	private Startmode startmode;
	private ProjectSettings settings;
	private SettingsStorer storer;
	
	public PageStartmode(SettingsStorer storer) {
		super(Messages.PageStartmode_StartmodeTitle);
		setDescription(Messages.PageStartmode_StartmodeDescription);
		this.storer = storer;
	}

	@Override
	protected Control createContents(Composite parent) {

		startmode = new Startmode(parent, SWT.None);
		settings = Application.getCurrentProject().getSettings();
		if (settings != null) {
			startmode.fullscreen = settings.isFullscreen();
			startmode.setField();
		}
		startmode.addListener(SWT.Modify, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				setValid(true);
				
			}
		});
		return startmode;
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
		return startmode != null;
	}

	public boolean getStartmode() {
		return startmode.fullscreen;
	}
	@Override
	protected void performDefaults() {
		if (settings != null && startmode != null) {
			startmode.init();
		} 
		super.performDefaults();
	}
	
	

}
