package org.iviPro.operations;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.operations.messages"; //$NON-NLS-1$
	public static String CompoundOperation_ErrorRolledBackBecauseChildCanceled;
	public static String CompoundOperation_ErrorRolledBackBecauseChildFailed;
	public static String IAbstractDeleteOperation_QuestionDeleteDependentObj_Dependencies;
	public static String IAbstractDeleteOperation_QuestionDeleteDependentObj_Part1;
	public static String IAbstractDeleteOperation_QuestionDeleteDependentObj_Part2;
	public static String IAbstractDeleteOperation_QuestionDeleteDependentObj_Title;
	public static String IAbstractDeleteOperation_QuestionDeleteDependentObj_Uses;
	public static String OperationHistory_ErrorMsgBox_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
