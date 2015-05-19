package org.iviPro.preview;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.preview.messages"; //$NON-NLS-1$
	public static String AbstractPlayer_SceneEndText;
	public static String PreviewInput_PreviewTitle;
	public static String PreviewInput_PreviewToolTip;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
