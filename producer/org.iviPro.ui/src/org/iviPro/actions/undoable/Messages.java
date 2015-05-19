package org.iviPro.actions.undoable;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.iviPro.actions.undoable.messages"; //$NON-NLS-1$

	// AbstractUndoableAction
	public static String AbstractUndoableAction_ErrorMsgBox_Title;

	// AnnotationDeleteAction
	public static String AnnotationDeleteAction_Label;
	public static String AnnotationDeleteAction_Tooltip;

	public static String AnnotationDeleteAction_UndoLabel;

	// MediaDeleteAction
	public static String MediaDeleteAction_0;
	public static String MediaDeleteAction_Label;
	public static String MediaDeleteAction_Tooltip;

	// MediaLoadAction
	public static String MediaLoadAction_AllAudioTypes;
	public static String MediaLoadAction_AllMediaTypes;
	public static String MediaLoadAction_AllPdfTypes;

	public static String MediaLoadAction_AllPictureTypes;
	public static String MediaLoadAction_AllTextTypes;
	public static String MediaLoadAction_AllVideoTypes;
	public static String MediaLoadAction_LoadMedia;
	public static String MediaLoadAction_LoadMediaToolTip;
	public static String MediaLoadAction_ErrorTitle;
	public static String MediaLoadAction_NameMsg;
	public static String MediaLoadAction_TypeMsg;
	public static String MediaLoadAction_ErrorMsg_DuplicateFile;
	public static String MediaLoadAction_ErrorMsg_TheFollowingErrorOccured;
	public static String MediaLoadAction_1;

	// Change TitleAction
	public static String ChangeTitleAction_Label;
	public static String ChangeTitleAction_Tooltip;

	// MediaRenameAction
	public static String MediaRenameAction_MsgBox_Text;
	public static String MediaRenameAction_MsgBox_Title;
	
	//New Folder & Folder rename
	public static String NewFolderAction_Title;
	public static String FolderRenameAction_Title;

	// SceneDetectionAction
	public static String OpenSceneDetectionEditorAction_MsgFinishPlural;
	public static String OpenSceneDetectionEditorAction_MsgFinishPlural2;
	public static String OpenSceneDetectionEditorAction_MsgFinishSingular;
	public static String OpenSceneDetectionEditorAction_MsgFinishSingular2;
	public static String OpenSceneDetectionEditorAction_MsgFinishTitle;
	public static String OpenSceneDetectionEditorAction_SceneTitlePrefix;
	public static String OpenSceneDetectionEditorAction_Text;
	public static String OpenSceneDetectionEditorAction_Tooltip;

	public static String SceneDetectionAction_InfoNoScenesFound_Msg;

	public static String SceneDetectionAction_InfoNoScenesFound_Title;

	public static String SceneDetectionAction_UndoLabel;

	public static String SceneFromVideoAction_Label;

	public static String SceneFromVideoAction_ToolTip;
	
	public static String SceneDeleteAction_0;

	public static String SceneDeleteAction_Text;

	public static String SceneDeleteAction_ToolTip;

	public static String GlobalAnnotationDeleteOperation_UndoLabel;	
	
	

	/**
	 * Initialize resource bundle
	 */
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
