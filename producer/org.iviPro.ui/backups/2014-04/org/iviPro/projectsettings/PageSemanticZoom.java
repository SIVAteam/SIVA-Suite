package org.iviPro.projectsettings;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.application.Application;
import org.iviPro.model.ProjectSettings;

public class PageSemanticZoom extends PreferencePage {
	
	private SemanticZoom semanticZoom;
	private ProjectSettings settings;
	private SettingsStorer storer;
	
	public PageSemanticZoom(SettingsStorer storer) {
		super("Semantic Zoom");
		setDescription("Erweiterte Einstellungen für den semantischen Zoom");
		this.storer = storer;
	}

	@Override
	protected Control createContents(Composite parent) {

		semanticZoom = new SemanticZoom(parent, SWT.None);
		settings = Application.getCurrentProject().getSettings();
		if (settings != null) {
			semanticZoom.setFullZoomLevels(settings.isFullSemanticZoomLevels());
		}
		semanticZoom.addListener(SWT.Modify, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				setValid(true);
				
			}
		});
		return semanticZoom;
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
		return semanticZoom != null;
	}
	
	protected boolean getFullZoomLevels() {
		return semanticZoom.fullSemanticZoomLevels;
	}

	@Override
	protected void performDefaults() {
		if (settings != null && semanticZoom != null) {
			semanticZoom.init();
		} 
		super.performDefaults();
	}
	
	

}
