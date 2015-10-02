package org.iviPro.operations.settings;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.model.ProjectSettings;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation for project setting changes.
 * 
 */
public class ProjectSettingsSaveOperation extends IAbstractOperation {
	
	//Identifikator fuer PropertyChangeEvent
	public static final String PROP_ANNO_ADDED = "settingsAdded";
	
	// Die Variablen zum Speichern der Operations-Daten
	private ProjectSettings usedSettings;
	private ProjectSettings newSettings;
	private ProjectSettings oldSettings;
		
	/**
	 * @param newSettings
	 * @param oldSettings
	 */
	public ProjectSettingsSaveOperation(ProjectSettings newSettings, ProjectSettings oldSettings) {
		
		super(Messages.ProjectSettingsSaveOperation_UndoLabel);
		if (newSettings == null || oldSettings == null) {
			throw new IllegalArgumentException(
					"None of the parameters may be null."); //$NON-NLS-1$
		}			
		usedSettings = Application.getCurrentProject().getSettings();
		this.newSettings = newSettings;
		this.oldSettings = oldSettings;
	}

	@Override
	public boolean canExecute() {
		return newSettings != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ProjectSettingsSaveOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		// Resolution
		usedSettings.setResolutionWidth(newSettings.getResolutionWidth());
		usedSettings.setResolutionHeight(newSettings.getResolutionHeight());
		// Sidebars
		usedSettings.setAnnobarVisibility(newSettings.getAnnobarVisibility());
		usedSettings.setAnnobarOverlay(newSettings.isAnnobarOverlayEnabled());
		usedSettings.setNavigationBarWidth(newSettings.getNavigationBarWidth());
		usedSettings.setAnnotationBarWidth(newSettings.getAnnotationBarWidth());
		// Colors
		usedSettings.setPrimaryColor(newSettings.getPrimaryColor());
		usedSettings.setSecondaryColor(newSettings.getSecondaryColor());
		// Video settings
		usedSettings.setVideoTitle(newSettings.getVideoTitle());
		usedSettings.setAutoStart(newSettings.isAutostartEnabled());
		// Player functions
		usedSettings.setUserDiary(newSettings.isUserDiaryEnabled());
		usedSettings.setCollaboration(newSettings.isCollaborationEnabled());
		usedSettings.setLogging(newSettings.isLoggingEnabled());
		usedSettings.setLoggingServerUrl(newSettings.getLoggingServerUrl());
		
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		// Resolution
		usedSettings.setResolutionWidth(oldSettings.getResolutionWidth());
		usedSettings.setResolutionHeight(oldSettings.getResolutionHeight());
		// Sidebars
		usedSettings.setAnnobarVisibility(oldSettings.getAnnobarVisibility());
		usedSettings.setAnnobarOverlay(oldSettings.isAnnobarOverlayEnabled());
		usedSettings.setNavigationBarWidth(oldSettings.getNavigationBarWidth());
		usedSettings.setAnnotationBarWidth(oldSettings.getAnnotationBarWidth());
		// Colors
		usedSettings.setPrimaryColor(oldSettings.getPrimaryColor());
		usedSettings.setSecondaryColor(oldSettings.getSecondaryColor());
		// Video settings
		usedSettings.setVideoTitle(oldSettings.getVideoTitle());
		usedSettings.setAutoStart(oldSettings.isAutostartEnabled());
		// Player functions
		usedSettings.setUserDiary(oldSettings.isUserDiaryEnabled());
		usedSettings.setCollaboration(oldSettings.isCollaborationEnabled());
		usedSettings.setLogging(oldSettings.isLoggingEnabled());
		usedSettings.setLoggingServerUrl(oldSettings.getLoggingServerUrl());
		
		return Status.OK_STATUS;
	}

}