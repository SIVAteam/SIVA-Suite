package org.iviPro.operations.toc;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.operations.toc.messages"; //$NON-NLS-1$
	public static String ChangeSceneOperation_Error;
	public static String ChangeSceneOperation_Title;
	public static String ChangeTocItemOperation_Error;
	public static String ChangeTocItemOperation_Title;
	public static String ChangeTocPositionOperation_ErrorMessagePrefix;
	public static String ChangeTocPositionOperation_TocPositionTitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
