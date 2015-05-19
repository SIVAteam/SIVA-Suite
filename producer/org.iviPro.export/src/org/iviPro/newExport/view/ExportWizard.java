package org.iviPro.newExport.view;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.iviPro.newExport.Activator;
import org.iviPro.newExport.ExportDefinition;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.ExportPreferences;
import org.iviPro.newExport.profile.ExportProfile;
import org.iviPro.newExport.profile.ExportProfileProvider;
import org.iviPro.newExport.util.FileUtils;
import org.iviPro.newExport.util.PathHelper;
import org.osgi.service.prefs.Preferences;

public class ExportWizard extends Wizard {

	private static final String PREF_OPENFOLDER = "openFolderAfterExport"; //$NON-NLS-1$
	private static final String LOG_COULD_NOT_STORE_PREFERENCES = "Could not store the profiles preferences!"; //$NON-NLS-1$
	protected final static String WIZARD_TITLE = Messages.ExportWizard_Title;

	private static final Logger logger = Logger.getLogger(ExportWizard.class);

	private final ExportProfileProvider provider;
	private final List<ExportProfile> exportProfiles;
	private final File projectFile;
	private final Preferences preferences;
	private final File initialOutputFolder;

	private File outputFolder;
	private boolean createdInitialExportFolder;

	private final ProfileSelectionPage profileSelectionPage;

	public ExportWizard(ExportProfileProvider provider,
			List<ExportProfile> exportProfiles, File projectFile) {
		setWindowTitle(Messages.ExportWizard_Title);

		this.provider = provider;
		this.exportProfiles = exportProfiles;
		this.projectFile = projectFile;
		this.preferences = Platform.getPreferencesService().getRootNode()
				.node(ConfigurationScope.SCOPE).node(Activator.PLUGIN_ID)
				.node(ExportPreferences.ID);
		this.initialOutputFolder = getExportFolder(projectFile);
		this.outputFolder = initialOutputFolder;
		
		this.profileSelectionPage = new ProfileSelectionPage(provider,
				exportProfiles, outputFolder, isOpenFolderSet());
	}

	private File getExportFolder(File projectFile) {
		String cachedExportFolder = preferences.get(
				projectFile.getAbsolutePath(), null);
		if (cachedExportFolder != null) {
			return new File(cachedExportFolder);
		} else {
			return getInitialExportFolder(projectFile);
		}
	}
	
	public boolean isOpenFolderSet() {
		String openFolderPref = preferences.get(
				PREF_OPENFOLDER, null);
		if (openFolderPref != null) {
			return new Boolean(openFolderPref);
		} else {
			return false;
		}
	}

	private File getInitialExportFolder(File projectFile) {
		File exportFolder = null;
		String projectDirectoyPath = projectFile.getParent();

		if (projectDirectoyPath != null) {
			exportFolder = new File(projectDirectoyPath,
					ExportDefinition.EXPORT_DIRECTORY);
		} else {
			exportFolder = new File(PathHelper.SIVA_USER_DIRECTORY,
					ExportDefinition.EXPORT_DIRECTORY);
		}

		if (exportFolder != null && !exportFolder.exists()) {
			exportFolder.mkdirs();
			createdInitialExportFolder = true;
		}

		return exportFolder;
	}

	public List<ExportProfile> getExportProfiles() {
		return exportProfiles;
	}

	public File getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(File outputFolder) {
		this.outputFolder = outputFolder;
	}

	@Override
	public void addPages() {
		addPage(profileSelectionPage);
	}

	@Override
	public boolean performFinish() {
		if (createdInitialExportFolder
				&& !initialOutputFolder.getAbsolutePath().equalsIgnoreCase(
						outputFolder.getAbsolutePath())
				&& initialOutputFolder.listFiles().length == 0) {
			FileUtils.delete(initialOutputFolder);
		}

		preferences.put(projectFile.getAbsolutePath(), outputFolder.toString());
		preferences.put(PREF_OPENFOLDER, Boolean.toString(
				profileSelectionPage.isOpenFolderSet()));
		
		try {
			provider.storeProfilesPreferences(exportProfiles);
		} catch (ExportException warn) {
			logger.warn(warn.getMessage());
			logger.warn(LOG_COULD_NOT_STORE_PREFERENCES);
			MessageDialog.openWarning(getShell(), Messages.Warning,
					Messages.WarningCouldNotStorePreferences);
		}
		return true;
	}
}
