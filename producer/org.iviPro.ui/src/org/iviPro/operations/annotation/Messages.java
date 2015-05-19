package org.iviPro.operations.annotation;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.operations.annotation.messages"; //$NON-NLS-1$

	public static String AnnotationSaveOperation_UndoLabel;
	public static String AnnotationSaveOperation_ErrorMsg;
	public static String ChangeScreenPositionOperation_Label;
	public static String ChangeMarkOperation_Label;
	public static String ChangeMarkOperation_ErrorMsg;
	public static String ChangeScreenPositionOperation_ErrorMsg;
	public static String ChangeContentOperation_SuffixRichtext;
	public static String ChangeDisableableOperation_ErrorMsg;

	public static String ChangeDisableableOperation_Label;

	public static String ChangePauseOperation_Label;
	public static String ChangePauseOperation_ErrorMsg;
	public static String ChangeMuteOperation_Label;
	public static String ChangeMuteOperation_ErrorMsg;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
