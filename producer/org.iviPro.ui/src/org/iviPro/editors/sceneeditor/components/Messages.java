package org.iviPro.editors.sceneeditor.components;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.iviPro.editors.sceneeditor.components.messages"; //$NON-NLS-1$
	
	public static String CustomPopup_Label_EndTime;
	public static String CustomPopup_Label_Scene;
	public static String CustomPopup_Label_StartTime;

	public static String FrameCutWidget_FrameLabelText;
	
	public static String SceneDefineWidget_Define;
	public static String SceneDefineWidget_End;
	public static String SceneDefineWidget_SaveButton;
	public static String SceneDefineWidget_SaveCreateButton;
	public static String SceneDefineWidget_Start;
	public static String SceneDefineWidget_StartPosition;
	public static String SceneDefineWidget_StopPosition;
	public static String SceneDefineWidget_Label_SceneName;
	public static String SceneDefineWidget_Label_Keywords;
	public static String SceneDefineWidget_Label_KeywordsTooltip;

	public static String SceneDefineWidget_Label_Preview;
	public static String SceneDefineWidget_Text_SaveButton;
	public static String SceneDefineWidget_Text_SaveCreateButton;
	public static String SceneDefineWidget_PositionInput_inTime;
	public static String SceneDefineWidget_PositionInput_InFrames;

	public static String SceneDefineWidget_Tooltip_SaveButton;

	public static String SceneDefineWidget_Tooltip_SaveCreateButton;
	
	public static String ScenesOverview_Label_NumScenes;
	public static String ScenesOverview_Tooltip_ChangeView;
	
	public static String SceneDefineWidgetScaleTooltip;

	public static String SingleSceneBar_Label_EndTime;
	public static String SingleSceneBar_Label_Scene;
	public static String SingleSceneBar_Label_StartTime;

	public static String VideoTimelineWidget_LockUnlock;
	public static String VideoTimelineWidget_Timeline_ThumbnailFrame;
	public static String VideoTimelineWidget_Timeline_Time;
	
	public static String SceneDefineWidget_MessageBox_CloseAnnoEditors;

	public static String SceneDefineWidget_MsgBoxNameAlreadyUsed_Text1;
	public static String SceneDefineWidget_MsgBoxNameAlreadyUsed_Text2;
	public static String SceneDefineWidget_MsgBoxNameAlreadyUsed_Text3;
	public static String SceneDefineWidget_MsgBoxNameAlreadyUsed_Title;
	public static String SceneDefineWidget_MsgBoxNoName_Text1;
	public static String SceneDefineWidget_MsgBoxNoName_Text2;
	
	public static String SceneDefineWidget_FrameCut_StartFrame;
	public static String SceneDefineWidget_FrameCut_EndFrame;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
