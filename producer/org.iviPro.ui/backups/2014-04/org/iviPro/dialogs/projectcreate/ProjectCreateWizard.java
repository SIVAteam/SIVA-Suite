package org.iviPro.dialogs.projectcreate;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.DialogSettings;
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
		data.projectName = Messages.ProjectCreateNewAction_DefaultProjectName;
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
	 * Gibt die Wizard-Daten zurueck
	 */
	ProjectCreateData getData() {
		return data;
	}

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
		logger.debug("data.projectName = " + data.projectName); //$NON-NLS-1$
		logger.debug("data.projectDir = " + data.projectDir); //$NON-NLS-1$
		logger.debug("data.locale = " + data.locale); //$NON-NLS-1$
		this.createdProject = null;

		// Projekt-Objekt erstellen
		File projectFile = new File(data.projectDir, data.projectName + "." //$NON-NLS-1$
				+ Project.PROJECT_FILE_EXTENSION);
		LocalizedString projectTitle = new LocalizedString(data.projectName,
				data.locale);
		Project createdProject = new Project(projectTitle, projectFile);

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

		// Projekteinstellungen im Projekt speichern
		// erstelle die Settings nur neu wenn es ein neues Projekt ist
		ProjectSettings settings = createdProject.getSettings();
		if (settings == null) {
			settings = new ProjectSettings("", createdProject); //$NON-NLS-1$
		}

		settings.setFullscreen(data.fullscreen);
		settings.setSkin(data.skin);
		
		settings.setDesignName(data.designName);
		settings.setDesignSchema(data.designSchema);
		settings.setColorSchema(data.colorSchema);
		settings.setBackgroundColor(data.backgroundColor);
		settings.setBorderColor(data.borderColor);
		settings.setTextColor(data.textColor);
		settings.setFont(data.font);
		settings.setFontSize(data.fontSize);	
		
		settings.setAutoPlay(data.autoPlay);
		settings.setPrimaryColor(data.primaryColorValue);
		settings.setPrimaryColorBool(data.primaryColor);
		
		settings.setDimensions(data.sizeWidth, data.sizeHeight,
				data.areaLeftWidth, data.areaTopHeight, data.areaBottomHeight,
				data.areaRightWidth);
		createdProject.setSettings(settings);

		// Leeres Projekt gleich abspeichern
		try {
			ProjectSaveAction.doSave(createdProject, projectFile);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			displayErrorMsg(Messages.ProjectCreateWizard_Error_FailedCreateProjectFile1
					+ projectFile.getAbsolutePath()
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
		addPage(new ProjectCreateWizardPage());
		addPage(new ProjectSetStartmodePage());
		addPage(new ProjectChoosePlayerDesignPage());
		addPage(new ProjectSetLayoutPage());
	}

}
