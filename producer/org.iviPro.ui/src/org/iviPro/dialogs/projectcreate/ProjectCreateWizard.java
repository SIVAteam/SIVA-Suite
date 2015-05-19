package org.iviPro.dialogs.projectcreate;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchWindow;
import org.iviPro.actions.nondestructive.ProjectSaveAction;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.ProjectSettings;
import org.iviPro.theme.Icons;

/**
 * Wizard zum Erstellen eines neuen Projekts
 * 
 * @author dellwo
 * 
 */
public class ProjectCreateWizard extends Wizard {

	private static Logger logger = Logger.getLogger(ProjectCreateWizard.class);

	private ProjectCreateData data;
	private Project createdProject;

	/**
	 * Erstellt den Wizard.
	 * 
	 * @param window
	 */
	public ProjectCreateWizard(IWorkbenchWindow window) {
		setWindowTitle(Messages.ProjectCreateWizard_WindowTitle);
		setDefaultPageImageDescriptor(Icons.DIALOG_PROJECTCREATEWIZARD_TITLE
				.getImageDescriptor());
		DialogSettings dialogSettings = new DialogSettings(getClass().getName());
		setDialogSettings(dialogSettings);
		data = new ProjectCreateData();
		data.locale = getDefaultLocale();
	}

	/**
	 * Gibt das Default-Locale zurueck. Wenn das Land nicht festgestellt werden
	 * kann wird "US" angenommen.
	 * 
	 * @return
	 */
	private Locale getDefaultLocale() {
		Locale locale = Locale.getDefault();
		if (locale.getCountry().isEmpty()) {
			String language = locale.getLanguage();
			String country = language.toUpperCase();
			List<String> countries = Arrays.asList(Locale.getISOCountries());
			if (!countries.contains(country)) {
				country = Locale.US.getCountry();
			}
			locale = new Locale(language, country);
		}
		return locale;

	}

	/**
	 * Gibt das erstellte Projekt zurueck, sobald der Wizard abgeschlossen
	 * wurde. Falls der Benutzer den Wizard abbrach oder der Wizard noch gar
	 * nicht gestartet wurde wird hier null zurueck gegeben.
	 * 
	 * @return
	 */
	public Project getCreatedProject() {
		return createdProject;
	}
	
	/**
	 * <b>Note:</b> The wizard may finish when last page is reached.
	 * {@inheritDoc}
	 * <p>
	 * <b>Note:</b> This wizard may finish when last page is reached.
	 */
	@Override
	public boolean canFinish() {
		// Default implementation is to check if all pages are complete.
        for (int i = 0; i < getPageCount(); i++) {
            if (!((IWizardPage) getPages()[i]).isPageComplete()) {
				return false;
			}
        }        
		return getContainer().getCurrentPage().equals(getPages()[getPageCount()-1]);
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#performCancel()
	 */
	public boolean performCancel() {
		return true;
		// Nachfrage unnoetig?
		// boolean cancel = MessageDialog.openConfirm(getShell(),
		// "Confirmation", "Are you sure to cancel the task?");
		// if (cancel) { return true; } else { return false; }
	}

	@Override
	public boolean performFinish() {
		logger.debug("Finishing wizard..."); //$NON-NLS-1$
		logger.debug("data.projectName = " + data.project.getTitle()); //$NON-NLS-1$
		logger.debug("data.projectDir = " + data.project.getFile().getValue().getParent()); //$NON-NLS-1$
		logger.debug("data.locale = " + data.locale); //$NON-NLS-1$
		
		this.createdProject = data.project;
		// Associate project with settings
		createdProject.setSettings(data.settings);

		// Projekt-Verzeichnisstruktur anlegen
		try {
			createdProject.createProjectDirectoryStructure();
		} catch (IOException e) {
			logger.error("Could not create project subdirectory: " //$NON-NLS-1$
					+ e.getMessage(), e);
			displayErrorMsg(Messages.ProjectCreateWizard_Error_FailedToCreateProjectDirectory1
					+ e.getMessage()
					+ Messages.ProjectCreateWizard_Error_FailedToCreateProjectDirectory2);
			return false;
		}

		// Leeres Projekt gleich abspeichern
		try {
			ProjectSaveAction.doSave(createdProject, 
					createdProject.getFile().getValue());
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			displayErrorMsg(Messages.ProjectCreateWizard_Error_FailedCreateProjectFile1
					+ createdProject.getFile().getAbsolutePath()
					+ Messages.ProjectCreateWizard_Error_FailedCreateProjectFile2);
			return false;
		}

		// Falls alles geklappt hat setzen wir das Projekt, damit der Aufrufer
		// des Wizards das Projekt holen kann.
		this.createdProject = createdProject;

		return true;
	}

	/**
	 * Zeigt Fehlermeldung in Form einer Dialog-Box an und loggt den Fehler
	 * 
	 * @param message
	 *            Fehlermeldungs-Text
	 */
	private void displayErrorMsg(String message) {
		logger.error(message);
		MessageBox messageBox = new MessageBox(Display.getCurrent()
				.getActiveShell(), SWT.ICON_ERROR);
		messageBox.setText(Messages.ProjectCreateWizard_Error_MsgBoxTitle);
		messageBox.setMessage(message);
		messageBox.open();

	}

	@Override
	public void addPages() {
		addPage(new ProjectCreateWizardPage(data));
		addPage(new PlayerSettingsWizardPage(data));
		
	}
}
