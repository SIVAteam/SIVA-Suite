package org.iviPro.newExport.view;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.ExportProfile;
import org.iviPro.newExport.profile.ExportProfileProvider;

public class ProfileSelectionPage extends WizardPage implements
		DirectoryChangedListener {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ProfileSelectionPage.class);

	private final ExportProfileProvider provider;
	private final List<ExportProfile> exportProfiles;
	private File outputFolder;
	private boolean openFolderPref;
	
	private ExportProfilesWidget exportProfilesWidget;
	private Label separator;
	private DirectorySelectorWidget directoryWidget;
	private Button openFolderButton;

	public ProfileSelectionPage(ExportProfileProvider provider,
			List<ExportProfile> exportProfiles, File outputFolder,
			boolean openFolderPref) {
		super(ExportWizard.WIZARD_TITLE);
		setTitle(Messages.ProfileSelectionPage_Title);
		setDescription(Messages.ProfileSelectionPage_Description);

		this.provider = provider;
		this.exportProfiles = exportProfiles;
		this.outputFolder = outputFolder;
		this.openFolderPref = openFolderPref;
	}

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));

		exportProfilesWidget = new ExportProfilesWidget(container, SWT.NONE,
				provider, exportProfiles);
		exportProfilesWidget.setLayoutData(new GridData(GridData.FILL,
				GridData.FILL, true, true));

		separator = new Label(container, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
				true, true));

		directoryWidget = new DirectorySelectorWidget(container, SWT.NONE,
				outputFolder, Messages.OutputFolder,
				Messages.OutputFolder_ChooseFolder, this);
		directoryWidget.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, true));
		
		
		Composite openFolderComp = new Composite(container, SWT.NONE);
		openFolderComp.setLayout(new GridLayout(2, false));
		openFolderButton = new Button(openFolderComp, SWT.CHECK);
		Label openFolderLabel = new Label(openFolderComp, SWT.NONE);
		openFolderLabel.setText(Messages.ProfileSelectionPage_OpenFolder_Label);
		openFolderButton.setSelection(openFolderPref);
		
		setControl(container);
		setPageComplete(true);
	}

	@Override
	public void onDirectoryChanged(File directory) {
		((ExportWizard) getWizard()).setOutputFolder(directory);
	}
	
	/**
	 * Returns whether or not the user selected to show the export folder in
	 * the file manager after export is done.
	 * @return true if export folder should be shown - else otherwise
	 */
	public boolean isOpenFolderSet() {
		return openFolderButton.getSelection();
	}

}
