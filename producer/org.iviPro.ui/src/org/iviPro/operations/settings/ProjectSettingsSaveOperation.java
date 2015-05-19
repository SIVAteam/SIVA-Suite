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
		Application.getCurrentProject().setSettings(this.newSettings);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		Application.getCurrentProject().setSettings(this.oldSettings);
		return Status.OK_STATUS;
	}

}