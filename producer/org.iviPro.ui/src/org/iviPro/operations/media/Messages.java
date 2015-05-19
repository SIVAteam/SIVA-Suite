package org.iviPro.operations.media;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.operations.media.messages"; //$NON-NLS-1$
	public static String MediaAddOperation_ErrorMsg;
	public static String MediaAddOperation_LabelAudio;
	public static String MediaAddOperation_LabelGeneralMediaObject;
	public static String MediaAddOperation_LabelPdf;
	public static String MediaAddOperation_LabelPicture;
	public static String MediaAddOperation_LabelRichtext;
	public static String MediaAddOperation_LabelVideo;
	public static String MediaDeleteOperation_ErrorMsg;
	public static String MediaDeleteOperation_LabelAudio;
	public static String MediaDeleteOperation_LabelGeneralObject;
	public static String MediaDeleteOperation_LabelImage;
	public static String MediaDeleteOperation_LabelRichtext;
	public static String MediaDeleteOperation_LabelVideo;
	public static String MediaDeleteOperation_AudioPartDeleteText;
	public static String MediaDeleteOperation_AudioPartDeleteMessage;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
