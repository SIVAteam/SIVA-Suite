package org.iviPro.editors.subtitleviewer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.editors.subtitleviewer.messages"; //$NON-NLS-1$
	public static String SubtitleViewer_Error_MsgBoxText_NoBrowserAvailable;
	public static String SubtitleViewer_Error_MsgBoxTitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
