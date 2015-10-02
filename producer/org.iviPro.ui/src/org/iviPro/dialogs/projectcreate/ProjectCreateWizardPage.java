package org.iviPro.dialogs.projectcreate;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.utils.PreferencesHelper;

class ProjectCreateWizardPage extends WizardPage {

	private static final String PREF_DEFAULT_PROJECT_DIR = "PREF_DEFAULT_PROJECT_DIR"; //$NON-NLS-1$
	private static final int MIN_PROJECTNAME_LENGTH = 3;
	private Text dirTextfield;
	private Text nameTextfield;
	private ProjectCreateData data;
	boolean pageComplete;

	protected ProjectCreateWizardPage(ProjectCreateData data) {
		super(""); //$NON-NLS-1$
		this.data = data;
		setTitle(Messages.ProjectCreateWizardPage_WizardTitle);
		setDescription(Messages.ProjectCreateWizardPage_WizardDescripton);
		setPageComplete(false);
		
		// Create dummy project object
		File projectFile = new File("");
		LocalizedString projectTitle = new LocalizedString("", data.locale);
		data.project = new Project(projectTitle, projectFile);
		// TODO: Projekt-Sprache in Projekt-Erstellen-Wizard aufnehmen

	}

	@Override
	public void createControl(Composite parent) {
		final ScrolledComposite scrollComp = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		Composite composite = new Composite(scrollComp, SWT.None);

		// Basis-Panel
		final Shell shell = parent.getShell();
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.marginLeft = 10;
		gridLayout.marginTop = 10;
		gridLayout.marginRight = 30;
		composite.setLayout(gridLayout);

		// Controls zur Auswahl des Projekt-Verzeichnis
		// Label + Textfield + Button
		new Label(composite, SWT.NULL)
				.setText(Messages.ProjectCreateWizardPage_LabelDirectory);
		dirTextfield = new Text(composite, SWT.DEFAULT);
		String defaultDir = PreferencesHelper.getPreference(
				PREF_DEFAULT_PROJECT_DIR, ""); //$NON-NLS-1$
		dirTextfield.setText(defaultDir);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		dirTextfield.setLayoutData(data);
		Button dirButton = new Button(composite, SWT.PUSH);
		dirButton.setText(Messages.ProjectCreateWizardPage_ButtonDirectory);
		dirButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				showDirectoryDialog(shell);
			}
		});

		// Controls zur Auswahl des Projekt-Namens
		int topMargin = 5;
		GridData labelData = new GridData();
		labelData.verticalIndent = topMargin;
		Label nameLabel = new Label(composite, SWT.NULL);
		nameLabel.setText(Messages.ProjectCreateWizardPage_LabelName);
		nameLabel.setLayoutData(labelData);
		nameTextfield = new Text(composite, SWT.BORDER);
		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		textData.verticalIndent = topMargin;
		nameTextfield.setLayoutData(textData);

		// Listener hinzufuegen die bei korrekten Daten den Finish-Button
		// der Wizard-Seite freischalten
		Listener listener = createInputChangeListener();
		dirTextfield.addListener(SWT.Modify, listener);
		nameTextfield.addListener(SWT.Modify, listener);
		
		scrollComp.setContent(composite);
		scrollComp.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrollComp.setExpandHorizontal(true);
		scrollComp.setExpandVertical(true);	

		// Finish
		setControl(scrollComp);

	}

	private Listener createInputChangeListener() {
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				String curParentDirPath = dirTextfield.getText();
				String curProjectName = nameTextfield.getText();
				String curProjectPath = curParentDirPath + File.separator
						+ curProjectName;
				File curParentDir = new File(curParentDirPath);
				File curProjectDir = new File(curProjectPath);
				String errorMsg = null;
				pageComplete = true;
				
				// Teste ob Eingabe-Daten OK sind
				if (curParentDirPath.length() == 0
						|| curProjectName.length() == 0) {
					// Mindestens ein Feld ist noch nicht ausgefuellt
					// Dialog-Abschluss nicht moegilch, aber keine Fehlermeldung
					pageComplete = false;

				} else if (!curParentDir.exists()
						|| !curParentDir.isDirectory()
						|| !curParentDir.canWrite()) {
					// Parent-Verzeichnis ist ungueltig
					pageComplete = false;
					errorMsg = Messages.ProjectCreateWizardPage_Error_ParentDirectoryInvalid;

				} else if (curProjectName.length() < MIN_PROJECTNAME_LENGTH) {
					// Projekt-Name zu kurz
					pageComplete = false;
					errorMsg = Messages.ProjectCreateWizardPage_Error_ProjectNameTooShort1
							+ MIN_PROJECTNAME_LENGTH
							+ Messages.ProjectCreateWizardPage_Error_ProjectNameTooShort2;

				} else if (curProjectDir.exists()) {
					// Projekt-Verzeichnis ungueltig
					pageComplete = false;
					errorMsg = Messages.ProjectCreateWizardPage_Error_HintProjectDirExistsAlready;
				}

				// Falls alles OK ist, uebernehmen wir die Daten fuer den Wizard
				if (pageComplete) {
					data.project.setFile(new File(curProjectDir, 
							curProjectName + "." + Project.PROJECT_FILE_EXTENSION));
					data.project.setTitle(curProjectName);					
				}
				setErrorMessage(errorMsg);
				//getWizard().getContainer().updateButtons();
				setPageComplete(pageComplete);
			}
		};
		return listener;
	}
	
//	/**
//	 * Controls display of next button.
//	 */
//	@Override
//	public boolean canFlipToNextPage() {
//		return pageComplete;
//	};

	/**
	 * Zeigt den Verzeichnis-Dialog an und speichert den gewaehlten
	 * Verzeichnispfad im zugehoerigen Textfeld
	 * 
	 * @param shell
	 */
	private void showDirectoryDialog(Shell shell) {
		DirectoryDialog dlg = new DirectoryDialog(shell);
		dlg.setFilterPath(dirTextfield.getText());
		dlg.setText(Messages.ProjectCreateWizardPage_ChooseDirDialog_Title);
		dlg
				.setMessage(Messages.ProjectCreateWizardPage_ChooseDirDialog_Description);
		String dir = dlg.open();
		if (dir != null) {
			dirTextfield.setText(dir);
			PreferencesHelper.storePreference(PREF_DEFAULT_PROJECT_DIR, dir);
		}
	}
}
