package org.iviPro.newExport.profile;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.iviPro.newExport.Activator;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.xml.ProfileExporter;
import org.iviPro.newExport.profile.xml.ProfileParser;
import org.iviPro.newExport.profile.xml.TransformingException;
import org.iviPro.newExport.util.FileUtils;
import org.iviPro.newExport.util.PathHelper;
import org.iviPro.newExport.view.ProfileTitleDialog;
import org.iviPro.transcoder.VideoDimension;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.xml.sax.InputSource;

public class ExportProfileProvider {

	private static final String LOG_DELETE_PROFILE = "Deleting the profile '%s'."; //$NON-NLS-1$
	private static final String LOG_STORE_PROFILE = "Storing the profile '%s'."; //$NON-NLS-1$
	private static final String LOG_UPDATE_PROFILE = "Updating the profile '%s'."; //$NON-NLS-1$
	private static final String LOG_LOAD_PREFERENCES = "Load profile preferences for profile '%s'."; //$NON-NLS-1$
	private static final String LOG_REMOVE_UNUSED_PROFILES = "Removing unused profiles from preferences '%s'."; //$NON-NLS-1$
	private static final String LOG_GET_PROFILE_KEYS = "Getting all profile keys from preferences '%s'."; //$NON-NLS-1$
	private static final String LOG_GET_CUSTOM_PROFILES = "Getting all custom export profiles."; //$NON-NLS-1$
	private static final String LOG_PARSE_DEFAULT_PROFILE = "Parsing default profile '%s' ('%s')."; //$NON-NLS-1$
	private static final String LOG_GET_DEFAULT_PROFILES = "Getting all default export profiles."; //$NON-NLS-1$
	private static final String LOG_CHECK_PROFILE_AGAINST_OTHER = "Checking custom profile '%s' against default profile '%s'."; //$NON-NLS-1$
	private static final String LOG_SEARCH_CONFLICTING_TITLES = "Searching for conflicting profile titles."; //$NON-NLS-1$
	private static final String LOG_STORE_PROFILE_PREFERENCES = "Storing the preferences for export profile '%s'."; //$NON-NLS-1$
	private static final String LOG_STORE_PROFILES_PREFERENCES = "Storing the preferences of the export profiles."; //$NON-NLS-1$
	private static final String LOG_CREATE_CUSTOM_PROFILES_DIRECTORY = "Did not find the custom profiles directory! Creating it at '%s'."; //$NON-NLS-1$
	private static final String LOG_GET_AVAILABLE_PROFILES = "Getting all available export profiles."; //$NON-NLS-1$
	private static final String CHANGING_TITLE_FAILED = "Changing the title for profile '%s' to '%s' failed!"; //$NON-NLS-1$
	private static final String DEFAULT_PROFILES_CACHE = "defaultProfileCache"; //$NON-NLS-1$
	private static final String DEFAULT_PROFILES_DIRECTORY = "profiles"; //$NON-NLS-1$
	private static final String CUSTOM_PROFILES_CACHE = "customProfilesCache"; //$NON-NLS-1$
	private static final String KEY_CHECKED = "checked"; //$NON-NLS-1$
	private static final String KEY_RESOURCES = "resources"; //$NON-NLS-1$
	private static final String KEY_COMPRESS = "compress"; //$NON-NLS-1$
	private static final String KEY_CONVERT = "convert"; //$NON-NLS-1$
	private static final String WARNING_TITLE = "Warning"; //$NON-NLS-1$

	private static final Logger logger = Logger
			.getLogger(ExportProfileProvider.class);

	private final VideoDimension projectDimension;
	private final Preferences preferences;

	/**
	 * Constructs a <code>ExportProfileProvider</code> and connects it to its
	 * preferences service.
	 * <p>
	 * The platform's <code>Preferences</code> are used to store the profile's
	 * flags state (<code>exportResources</code>, <code>compress</code>, ...).
	 */
	public ExportProfileProvider(int projectVideoWidth, int projectVideoHeight) {
		this.projectDimension = new VideoDimension(projectVideoWidth,
				projectVideoHeight);
		this.preferences = Platform.getPreferencesService().getRootNode()
				.node(ConfigurationScope.SCOPE).node(Activator.PLUGIN_ID)
				.node(ExportPreferences.ID);
	}

	public VideoDimension getProjectDimension() {
		return projectDimension;
	}

	/**
	 * Gets the default and user defined export profiles.
	 * 
	 * @return All available export profiles.
	 */
	public List<ExportProfile> getAvailableProfiles(Shell shell)
			throws ExportException {
		logger.debug(LOG_GET_AVAILABLE_PROFILES);
		List<ExportProfile> defaultProfiles = getDefaultProfiles(
				getDefaultProfileUrls(),
				preferences.node(DEFAULT_PROFILES_CACHE));
		if (PathHelper.CUSTOM_EXPORT_PROFILES_DIRECTORY.exists()) {
			List<ExportProfile> customProfiles = getCustomProfiles(
					PathHelper.CUSTOM_EXPORT_PROFILES_DIRECTORY,
					preferences.node(CUSTOM_PROFILES_CACHE));
			if (checkProfileTitles(shell, defaultProfiles, customProfiles)) {
				defaultProfiles.addAll(customProfiles);
			} else {
				return null;
			}
		} else {
			logger.debug(String.format(LOG_CREATE_CUSTOM_PROFILES_DIRECTORY,
					PathHelper.CUSTOM_EXPORT_PROFILES_DIRECTORY
							.getAbsolutePath()));
			PathHelper.CUSTOM_EXPORT_PROFILES_DIRECTORY.mkdirs();
		}
		return defaultProfiles;
	}

	/**
	 * Gets the default and user defined export profiles.
	 * 
	 * @return All available export profiles.
	 */
	public void storeProfilesPreferences(List<ExportProfile> exportProfiles)
			throws ExportException {
		logger.debug(LOG_STORE_PROFILE_PREFERENCES);
		Set<String> profileKeys = getStoredProfiles(preferences);

		for (ExportProfile exportProfile : exportProfiles) {
			if (exportProfile.isProtected()) {
				storeProfilePreferences(exportProfile,
						preferences.node(DEFAULT_PROFILES_CACHE));
			} else {
				storeProfilePreferences(exportProfile,
						preferences.node(CUSTOM_PROFILES_CACHE));
				removeProcessedProfileKey(exportProfile, profileKeys);
			}
		}

		clearPreferences(preferences.node(CUSTOM_PROFILES_CACHE), profileKeys);
	}

	private void storeProfilePreferences(ExportProfile exportProfile,
			Preferences preferences) {
		logger.debug(String.format(LOG_STORE_PROFILES_PREFERENCES,
				exportProfile.getProfileTitle()));
		preferences.node(exportProfile.getProfileTitle().toLowerCase())
				.putBoolean(KEY_CHECKED, exportProfile.isChecked());
		preferences.node(exportProfile.getProfileTitle().toLowerCase())
				.putBoolean(KEY_RESOURCES, exportProfile.isExportResources());
		preferences.node(exportProfile.getProfileTitle().toLowerCase())
				.putBoolean(KEY_COMPRESS, exportProfile.isCompress());
		preferences.node(exportProfile.getProfileTitle().toLowerCase())
		.putBoolean(KEY_CONVERT, exportProfile.isConvert());
	}

	private boolean checkProfileTitles(Shell shell,
			List<ExportProfile> defaultProfiles,
			List<ExportProfile> customProfiles) {
		logger.debug(LOG_SEARCH_CONFLICTING_TITLES);
		ExportProfile conflictingProfile = findConflictingExportProfile(
				defaultProfiles, customProfiles);

		if (conflictingProfile != null) {
			if (!requestNewProfileTitle(shell, conflictingProfile,
					defaultProfiles, customProfiles)) {
				return false;
			}
			return checkProfileTitles(shell, defaultProfiles, customProfiles);
		} else {
			return true;
		}
	}

	private ExportProfile findConflictingExportProfile(
			List<ExportProfile> defaultProfiles,
			List<ExportProfile> customProfiles) {
		for (ExportProfile customProfile : customProfiles) {
			for (ExportProfile defaultProfile : defaultProfiles) {
				logger.debug(String.format(LOG_CHECK_PROFILE_AGAINST_OTHER,
						customProfile.toString(), defaultProfile.toString()));
				if (customProfile.getProfileTitle().equalsIgnoreCase(
						defaultProfile.getProfileTitle())) {
					return customProfile;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private boolean requestNewProfileTitle(Shell shell,
			ExportProfile exportProfile, List<ExportProfile> defaultProfiles,
			List<ExportProfile> customProfiles) {
		String oldTitle = exportProfile.getProfileTitle();
		ProfileTitleDialog dialog = new ProfileTitleDialog(shell,
				exportProfile, ExportProfile.getAllTitles(defaultProfiles,
						customProfiles));
		if (dialog.open() == Dialog.OK) {
			try {
				updateProfile(exportProfile, oldTitle);
			} catch (ExportException e) {
				logger.error(e.getMessage());
				MessageDialog.openWarning(shell, WARNING_TITLE, String.format(
						CHANGING_TITLE_FAILED, oldTitle,
						exportProfile.getProfileTitle()));
			}
			return true;
		} else {
			return false;
		}
	}

	private List<ExportProfile> getDefaultProfiles(
			List<URL> defaultProfileUrls, Preferences preferences)
			throws ExportException {
		logger.debug(LOG_GET_DEFAULT_PROFILES);

		List<ExportProfile> profiles = new ArrayList<ExportProfile>();

		for (URL url : defaultProfileUrls) {
			String fileName = url.getFile();
			ExportProfile profile = null;
			try {
				logger.debug(String.format(LOG_PARSE_DEFAULT_PROFILE, fileName,
						url.toString()));
				profile = new ExportProfile(
						ProfileParser.parseProfile(new InputSource(
								new InputStreamReader(url.openStream()))),
						projectDimension, false, true, false, true, true);
				profiles.add(profile);
			} catch (IOException cause) {
				logger.error(cause.getMessage());
				throw new ExportException(
						String.format(Messages.Exception_ParsingProfileFailed,
								url.toString()), cause);
			} catch (TransformingException cause) {
				logger.error(cause.getMessage());
				throw new ExportException(
						String.format(Messages.Exception_ParsingProfileFailed,
								url.toString()), cause);
			}

			loadProfilePreferences(profile, preferences);
		}

		return profiles;
	}

	private List<ExportProfile> getCustomProfiles(File directory,
			Preferences preferences) throws ExportException {
		logger.debug(LOG_GET_CUSTOM_PROFILES);

		List<ExportProfile> profiles = new ArrayList<ExportProfile>();
		Set<String> profileKeys = getStoredProfiles(preferences);

		for (File file : directory.listFiles(new ProfileFilter())) {
			String fileName = file.getName();
			ExportProfile exportProfile = null;
			try {
				logger.debug(String.format(LOG_PARSE_DEFAULT_PROFILE, fileName,
						file.getAbsolutePath()));
				exportProfile = new ExportProfile(
						ProfileParser.parseProfile(file), projectDimension,
						false, true, false, true, false);
				profiles.add(exportProfile);
			} catch (TransformingException cause) {
				logger.error(cause.getMessage());
				throw new ExportException(String.format(
						Messages.Exception_ParsingProfileFailed,
						file.getAbsolutePath()), cause);
			}

			loadProfilePreferences(exportProfile, preferences);
			removeProcessedProfileKey(exportProfile, profileKeys);
		}

		clearPreferences(preferences, profileKeys);

		return profiles;
	}

	/**
	 * Returns the keys of the profiles that are currently stored in the
	 * submitted preferences.
	 * 
	 * @return The keys of the profiles that are currently stored in the
	 *         submitted preferences.
	 */
	private Set<String> getStoredProfiles(Preferences preferences)
			throws ExportException {
		logger.debug(String.format(LOG_GET_PROFILE_KEYS,
				preferences.absolutePath()));
		String[] profilesPreferences = new String[0];
		try {
			profilesPreferences = preferences.childrenNames();
		} catch (BackingStoreException cause) {
			logger.error(cause.getMessage());
			throw new ExportException(String.format(
					Messages.Exception_GettingProfilesFromPreferencesFailed,
					preferences.absolutePath()));
		}
		return new HashSet<String>(Arrays.asList(profilesPreferences));
	}

	private void removeProcessedProfileKey(ExportProfile exportProfile,
			Set<String> profileKeys) {
		if (profileKeys.contains(exportProfile.getProfileTitle().toLowerCase())) {
			profileKeys.remove(exportProfile.getProfileTitle().toLowerCase());
		}
	}

	/**
	 * Removes the preferences nodes specified by the submitted keys from the
	 * submitted preferences.
	 * 
	 * @param preferences
	 *            The references from which the nodes have to be removed.
	 * @param keys
	 *            The keys identifying the nodes to remove.
	 */
	private void clearPreferences(Preferences preferences, Set<String> keys)
			throws ExportException {
		logger.debug(String.format(LOG_REMOVE_UNUSED_PROFILES,
				preferences.absolutePath()));
		for (String s : keys) {
			try {
				preferences.node(s).removeNode();
			} catch (BackingStoreException cause) {
				logger.error(cause.getMessage());
				throw new ExportException(String.format(
						Messages.Exception_RemovingPreferencesNodeFailed,
						preferences.absolutePath()));
			}
		}
	}

	/**
	 * Sets the flags stored under the submitted profiles at the submitted
	 * profile.
	 * 
	 * @param profile
	 *            The profile to process.
	 * @param preferences
	 *            The preferences where to find the stored flags.
	 */
	private void loadProfilePreferences(ExportProfile profile,
			Preferences preferences) {
		logger.debug(String.format(LOG_LOAD_PREFERENCES, profile
				.getProfileTitle().toLowerCase()));
		profile.setChecked(preferences.node(
				profile.getProfileTitle().toLowerCase()).getBoolean(
				KEY_CHECKED, false));
		profile.setExportResources(preferences.node(
				profile.getProfileTitle().toLowerCase()).getBoolean(
				KEY_RESOURCES, true));
		profile.setCompress(preferences.node(
				profile.getProfileTitle().toLowerCase()).getBoolean(
				KEY_COMPRESS, false));
		profile.setConvert(preferences.node(
				profile.getProfileTitle().toLowerCase()).getBoolean(
				KEY_CONVERT, true));
	}

	/**
	 * Assembles the file name for the submitted profile.
	 * <p>
	 * This is done by placing export profile file extension in back of the
	 * actual profile title.
	 * 
	 * @param profile
	 *            The profile to get a path for.
	 * @return The assembled file name for the profile.
	 */
	public String getFileNameForProfile(Profile profile) {
		return profile.getProfileTitle() + ExportProfile.PROFILE_EXTENSION;
	}

	/**
	 * Assembles the file name for the submitted profile title.
	 * <p>
	 * This is done by placing export profile file extension in back of the
	 * actual profile title.
	 * 
	 * @param profileTitle
	 *            The profile title to get the file name for.
	 * @return The assembled file name for the profile.
	 */
	public String getFileNameForProfile(String profileTitle) {
		return profileTitle + ExportProfile.PROFILE_EXTENSION;
	}

	public void updateProfile(ExportProfile exportProfile, String oldTitle)
			throws ExportException {
		updateProfile(exportProfile.getProfile(), oldTitle);
	}

	/**
	 * Updates the submitted export profile.
	 * 
	 * @param profile
	 *            The export profile to update.
	 * @param oldTitle
	 *            The profile's original title.
	 * @throws ExportException
	 *             If updating the profile fails.
	 */
	private void updateProfile(Profile profile, String oldTitle)
			throws ExportException {
		logger.debug(String.format(LOG_UPDATE_PROFILE, oldTitle));
		deleteExportProfile(oldTitle);
		storeProfile(profile);
	}

	public void storeProfile(Profile profile) throws ExportException {
		logger.debug(String.format(LOG_STORE_PROFILE, profile.getProfileTitle()));
		try {
			ProfileExporter.export(profile, new File(
					PathHelper.CUSTOM_EXPORT_PROFILES_DIRECTORY,
					getFileNameForProfile(profile)));
		} catch (TransformingException e) {
			logger.error(e.getMessage());
			throw new ExportException(String.format(
					Messages.Exception_StoringProfileFailed,
					profile.getProfileTitle()));
		}
	}

	public void deleteExportProfile(ExportProfile exportProfile) {
		logger.debug(String.format(LOG_DELETE_PROFILE,
				exportProfile.getProfileTitle()));
		deleteExportProfile(exportProfile.getProfileTitle());
	}

	private void deleteExportProfile(String profileTitle) {
		File profileToDelete = new File(
				PathHelper.CUSTOM_EXPORT_PROFILES_DIRECTORY,
				getFileNameForProfile(profileTitle));
		logger.debug(String.format(Messages.Exception_DeletingProfileFailed,
				profileToDelete.getAbsolutePath()));
		FileUtils.delete(profileToDelete);
	}

	/**
	 * Loads the urls to the default profiles from the actual bundle.
	 * 
	 * @return The urls pointing to the default profiles.
	 */
	@SuppressWarnings("unchecked")
	private List<URL> getDefaultProfileUrls() {
		return Collections.list((Enumeration<URL>) Platform.getBundle(
				Activator.PLUGIN_ID).findEntries(DEFAULT_PROFILES_DIRECTORY,
				"*" + ExportProfile.PROFILE_EXTENSION, true)); //$NON-NLS-1$
	}

	/**
	 * Filters export profile files from the submitted files.
	 * <p>
	 * A file is accepted if it ends with '.xml'.
	 * 
	 * @author Codebold
	 * 
	 */
	private class ProfileFilter implements FileFilter {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean accept(File file) {
			return file.isFile()
					&& file.getName().toLowerCase()
							.endsWith(ExportProfile.PROFILE_EXTENSION);
		}
	}
}
