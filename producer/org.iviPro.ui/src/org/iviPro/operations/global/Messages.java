package org.iviPro.operations.global;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.operations.global.messages"; //$NON-NLS-1$
	public static String ChangeDescriptionOperation_ErrorMsg;
	public static String ChangeDescriptionOperation_Label;
	public static String ChangeTitleOperation_ErrorMsg;
	public static String ChangeTitleOperation_Label;
	public static String ChangeKeywordsOperation_ErrorMsg;
	public static String ChangeTimeOperation_Label;
	public static String ChangeTimeOperation_ErrorMsg;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
