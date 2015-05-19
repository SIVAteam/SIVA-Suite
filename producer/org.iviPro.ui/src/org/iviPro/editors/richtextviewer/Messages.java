package org.iviPro.editors.richtextviewer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.editors.richtextviewer.messages"; //$NON-NLS-1$
	public static String RichtextViewer_Error_MsgBoxText_NoBrowserAvailable;
	public static String RichtextViewer_Error_MsgBoxTitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
