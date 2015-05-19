package org.iviPro.mediaaccess.videograb;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.iviPro.mis.messages"; //$NON-NLS-1$

	public static String Cache_CacheFileError1;
	public static String Cache_CacheFileError2;
	public static String MediaInfoThread_ProcCreationError;
	public static String MediaInfoThread_ProzConfigError;
	public static String MediaInfoThread_ProzRealisationError;
	public static String MediaInfoThread_TrackControlsError;
	public static String MediaInfoThread_UrlError;
	public static String MediaInfoThread_VideoTrackError;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
