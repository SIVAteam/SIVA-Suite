package org.iviPro.editors.audioeditor.components;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.iviPro.editors.audioeditor.components.messages"; //$NON-NLS-1$

	public static String FrameCutWidget_FrameLabelText;
	
	public static String AudioPartDefineWidget_Define;
	public static String AudioPartDefineWidget_End;
	public static String AudioPartDefineWidget_SaveButton;
	public static String AudioPartDefineWidget_SaveCreateButton;
	public static String AudioPartDefineWidget_Start;
	public static String AudioPartDefineWidget_StartPosition;
	public static String AudioPartDefineWidget_StopPosition;
	public static String AudioPartDefineWidget_Label_AudioPartName;
	public static String AudioPartDefineWidget_Label_Keywords;
	public static String AudioPartDefineWidget_Text_SaveButton;
	public static String AudioPartDefineWidget_Text_SaveCreateButton;

	public static String AudioPartDefineWidget_Tooltip_SaveButton;

	public static String AudioPartDefineWidget_Tooltip_SaveCreateButton;
	
	public static String AudioPartOverview_Label_NumAudioParts;
	public static String AudioPartOverview_Tooltip_ChangeView;
	
	public static String AudioPartDefineWidgetScaleTooltip;

	public static String SingleAudioBar_Label_EndTime;
	public static String SingleAudioBar_Label_AudioPart;
	public static String SingleAudioBar_Label_StartTime;
	
	public static String AudioPartDefineWidget_MsgBoxNameAlreadyUsed_Text1;
	public static String AudioPartDefineWidget_MsgBoxNameAlreadyUsed_Text2;
	public static String AudioPartDefineWidget_MsgBoxNameAlreadyUsed_Text3;
	public static String AudioPartDefineWidget_MsgBoxNameAlreadyUsed_Title;
	public static String AudioPartDefineWidget_MsgBoxNoName_Text1;
	public static String AudioPartDefineWidget_MsgBoxNoName_Text2;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
