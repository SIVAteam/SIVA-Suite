package org.iviPro.editors.imageviewer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.editors.imageviewer.messages"; //$NON-NLS-1$
	public static String PREFIX_IMAGEEDITOR;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
