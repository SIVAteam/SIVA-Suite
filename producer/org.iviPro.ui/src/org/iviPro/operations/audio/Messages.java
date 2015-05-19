package org.iviPro.operations.audio;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.operations.audio.messages"; //$NON-NLS-1$
	public static String AudioPartCreateOperation_ErrorMsg;
	public static String AudioPartCreateOperation_UndoLabel;
	public static String AudioPartMergeAction_MsgBox_Text1;
	public static String AudioPartMergeAction_MsgBox_Text2;
	public static String AudioPartMergeAction_MsgBox_Text3;
	public static String AudioPartMergeAction_MsgBox_Title;
	public static String AudioPartMergeOperation_NewAudioPartPrefix;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
