package org.iviPro.views.scenerepository;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.views.scenerepository.messages"; //$NON-NLS-1$
	public static String SceneAdapterFactory_ScenesNotUsed;
	public static String SceneAdapterFactory_ScenesUsed;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
