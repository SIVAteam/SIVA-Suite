package org.iviPro.views;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.views.messages"; //$NON-NLS-1$
	public static String IAbstractRepositoryView_LabelClear;
	public static String IAbstractRepositoryView_LabelSearch;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
