package org.iviPro.operations.graph;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.operations.graph.messages"; //$NON-NLS-1$
	public static String ConnectionCreateOperation_ErrorMsg;
	public static String ConnectionCreateOperation_UndoRedoLabel;
	public static String ConnectionDeleteOperation_ErrorMsg;
	public static String ConnectionDeleteOperation_UndoRedoLabel;
	public static String ConnectionReconnectOperation_ErrorMsg;
	public static String ConnectionReconnectOperation_UndoRedoLabel_Default;
	public static String ConnectionReconnectOperation_UndoRedoLabel_MoveEndpoint;
	public static String ConnectionReconnectOperation_UndoRedoLabel_MoveStartpoint;
	public static String CreateMediaAnnotationOperation_ErrorMsg;
	public static String CreateMediaAnnotationOperation_UndoRedoLabel;
	public static String ModifyAbstractNodeSelectionControlOperation_ErrorMsg;
	public static String ModifyAbstractNodeSelectionControlOperation_Title;
	public static String ModifyNodeCondSelectionControlOperation_ErrorMsg;
	public static String ModifyNodeCondSelectionControlOperation_Title;
	public static String ModifyNodeRandomSelectionOperation_Error_Message;
	public static String ModifyNodeRandomSelectionOperation_Modification_Title;
	public static String ModifyNodeResumeOperation_Modify_Error;
	public static String ModifyNodeResumeOperation_Modify_Message;
	public static String ModifyNodeSelectionOperation_ErrorMsg;
	public static String ModifyNodeSelectionOperation_Title;
	public static String NodeCreateOperation_ErrorMsg;
	public static String NodeCreateOperation_Label;
	public static String NodeDeleteOperation_ErrorMsg;
	public static String NodeDeleteOperation_Label;
	public static String NodeDeleteOperation_UndoRedoLabel;
	public static String NodeMoveOperation_ErrorMsg;
	public static String NodeMoveOperation_UndoRedoLabel;
	public static String AnnotationPrefix;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
