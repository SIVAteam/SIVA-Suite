package org.iviPro.operations.video;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.operations.video.messages"; //$NON-NLS-1$
	public static String SceneCreateOperation_ErrorMsg;
	public static String SceneCreateOperation_UndoLabel;
	public static String SceneDeleteOperation_ErrorMsg;
	public static String SceneDeleteOperation_UndoLabel;
	public static String SceneMergeAction_MsgBox_Text1;
	public static String SceneMergeAction_MsgBox_Text2;
	public static String SceneMergeAction_MsgBox_Text3;
	public static String SceneMergeAction_MsgBox_Title;
	public static String SceneMergeOperation_NewScenePrefix;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
