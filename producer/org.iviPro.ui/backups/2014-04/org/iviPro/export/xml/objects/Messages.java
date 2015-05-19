package org.iviPro.export.xml.objects;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.export.xml.objects.messages"; //$NON-NLS-1$
	public static String XMLExporterNodeStart_ErrorMsg_MoreThanOneStartScene;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
