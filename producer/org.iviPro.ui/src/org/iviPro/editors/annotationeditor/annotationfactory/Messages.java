package org.iviPro.editors.annotationeditor.annotationfactory;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.iviPro.editors.annotationeditor.annotationfactory.messages"; //$NON-NLS-1$

	public static String TypeName_Video;
	public static String TypeName_Audio;
	public static String TypeName_Subtitle;
	public static String TypeName_Text;
	public static String TypeName_Richtext;
	public static String TypeName_Pdf;

	public static String TypeName_Pdf_Mark;

	public static String TypeName_Picture;
	public static String TypeName_Video_Mark;
	public static String TypeName_Audio_Mark;
	public static String TypeName_Subtitle_Mark;
	public static String TypeName_Text_Mark;
	public static String TypeName_Richtext_Mark;
	public static String TypeName_Picture_Mark;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}