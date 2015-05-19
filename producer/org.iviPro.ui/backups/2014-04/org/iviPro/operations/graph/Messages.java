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
	public static String ModifyNodeSelectionControlOperation_ErrorMsg;
	public static String ModifyNodeSelectionControlOperation_Title;
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
