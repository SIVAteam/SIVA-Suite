package org.iviPro.views.informationview;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.views.informationview.messages"; //$NON-NLS-1$

	public static String InformationLabelProvider_Description;
	public static String InformationLabelProvider_Value;
	public static String InformationView_Datatype;
	public static String InformationView_Duration;
	public static String InformationView_End;
	public static String InformationView_Hours;
	public static String InformationView_Id;
	public static String InformationView_Media;
	public static String InformationView_Medianame;
	public static String InformationView_MediaDuration;
	public static String InformationView_MediaCodec;
	public static String InformationView_MediaDimension;
	public static String InformationView_MediaScenes;	
	public static String InformationView_MediaFrameRate;
	public static String InformationView_Path;
	public static String InformationView_Screenname;
	public static String InformationView_Start;
	public static String InformationView_AnnoScreenname;
	public static String InformationView_AnnoNodeScene;
	public static String InformationView_AnnotationType;
	public static String InformationView_Keywords;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
