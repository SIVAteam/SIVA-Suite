package org.iviPro.views.mediarepository;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.views.mediarepository.messages"; //$NON-NLS-1$
	public static String MediaTypes_Audio;
	public static String MediaTypes_Text;
	public static String MediaTypes_Pdf;
	public static String MediaTypes_Picture;
	public static String MediaTypes_Video;
	public static String MediaTypes_Subtitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
