package org.iviPro.newExport.profile;

import org.iviPro.newExport.util.FileUtils;

public enum ExportPreferences {

	OUTPUT("exportOutputFolder", FileUtils.USER_HOME), SELECTED_PROFILES( //$NON-NLS-1$
			"selectedProfiles", ""); //$NON-NLS-1$ //$NON-NLS-2$

	public static final String ID = "org.iviPro.export.ExportPreferences"; //$NON-NLS-1$

	private ExportPreferences(final String id, final String defaultValue) {
		this.id = id;
		this.defaultValue = defaultValue;
	}

	private final String id;
	private final String defaultValue;

	public String getId() {
		return id;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

}
