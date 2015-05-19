package org.iviPro.projectsettings;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.application.Application;
import org.iviPro.model.ProjectSettings;

public class PagePlayerSettings extends PreferencePage {

	private PlayerSettings playerSettings;
	private ProjectSettings settings;
	private SettingsStorer storer;

	public PagePlayerSettings(SettingsStorer storer) {
		super(Messages.PagePlayerSettings_PlayerSettings_Title);
		setDescription(Messages.PagePlayerSettings_PlayerSettings_Description);
		this.storer = storer;
	}

	@Override
	protected Control createContents(Composite parent) {
		playerSettings = new PlayerSettings(parent, SWT.NONE);
		settings = Application.getCurrentProject().getSettings();

		if (settings != null) {
			playerSettings.designName = settings.getDesignName();
			playerSettings.designSchema = settings.getDesignSchema();
			playerSettings.colorSchema = settings.getColorSchema();
			playerSettings.backgroundColor = settings.getBackgroundColor();
			playerSettings.borderColor = settings.getBorderColor();
			playerSettings.fontColor = settings.getTextColor();
			playerSettings.font = settings.getFont();
			playerSettings.fontSize = settings.getFontSize();
			playerSettings.primaryColorValue = settings.getPrimaryColor();
			playerSettings.isPrimaryColor = settings.isPrimaryColor();
			playerSettings.autoPlay = settings.autoPlay();

			playerSettings.setFields();
		}
		
		playerSettings.addListener(SWT.Modify, new Listener() {

			@Override
			public void handleEvent(Event event) {
				setValid(true);
			}

		});
		return playerSettings;
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
		return playerSettings != null;
	}
	
	public boolean autoPlay() {
		return playerSettings.autoPlay;
	}

	public boolean isOwnDesign() {
		return !playerSettings.isPrimaryColor;
	}

	protected String getDesignName() {
		return playerSettings.designName;
	}

	protected String getDesignSchema() {
		return playerSettings.designSchema;
	}

	protected String getColorSchema() {
		return playerSettings.colorSchema;
	}

	protected String getBackgroundColor() {
		return playerSettings.backgroundColor;
	}

	protected String getBorderColor() {
		return playerSettings.borderColor;
	}

	protected String getFontColor() {
		return playerSettings.fontColor;
	}

	protected String getTextFont() {
		return playerSettings.font;
	}

	protected String getFontSize() {
		return playerSettings.fontSize;
	}

	protected String getPrimaryColor() {
		return playerSettings.primaryColorValue;
	}

	@Override
	protected void performDefaults() {
		if (settings != null && playerSettings != null) {
			playerSettings.init();
		}
		super.performDefaults();
	}
}
