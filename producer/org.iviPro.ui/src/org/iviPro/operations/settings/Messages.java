package org.iviPro.operations.settings;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.operations.settings.messages"; //$NON-NLS-1$

	public static String ProjectSettingsSaveOperation_UndoLabel;
	public static String ProjectSettingsSaveOperation_ErrorMsg;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
