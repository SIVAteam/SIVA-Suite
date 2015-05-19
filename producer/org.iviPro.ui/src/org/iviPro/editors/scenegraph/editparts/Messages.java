package org.iviPro.editors.scenegraph.editparts;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.editors.scenegraph.editparts.messages"; //$NON-NLS-1$
	public static String IEditPartNode_ErrorMsg_CouldNotRenameNode;
	public static String IEditPartNode_RenameDialog_TextfieldTitle_ErrorMsgToShort1;
	public static String IEditPartNode_RenameDialog_TextfieldTitle_ErrorMsgToShort2;
	public static String IEditPartNode_RenameDialog_TextfieldTitle_Label;
	public static String IEditPartNode_RenameDialogTitle;
	public static String NodeRenameAction_ErrorMsg;
	public static String NodeRenameAction_Label;
	public static String NodeRenameAction_Tooltip;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
