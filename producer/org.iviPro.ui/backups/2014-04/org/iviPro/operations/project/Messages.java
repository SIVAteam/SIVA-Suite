package org.iviPro.operations.project;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.operations.project.messages"; //$NON-NLS-1$
	public static String ChangeExportSettingsOperation_ExportSettings_label;
	public static String ChangeSettingsOperation_ChangeSettingsOperationTitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
