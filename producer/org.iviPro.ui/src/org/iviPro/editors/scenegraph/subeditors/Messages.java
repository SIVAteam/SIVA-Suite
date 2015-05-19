package org.iviPro.editors.scenegraph.subeditors;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.editors.scenegraph.subeditors.messages"; //$NON-NLS-1$
	public static String AbstractNodeEditor_Label_NodeID;
	public static String Common_Button_Cancel;
	public static String Common_Button_Create;
	public static String Common_Button_Delete;
	public static String Common_Button_Edit;
	public static String Common_ErrorDialog_Title;
	public static String Commonr_Button_OK;
	public static String NodeQuizEditor_Group_Quiz;
	public static String NodeQuizEditor_Shell_Title;
	public static String NodeQuizControlEditor_IntersectionWarnMessage;
	public static String NodeSelectionControlEditor_Compound_Modification_Operation;
	public static String NodeSelectionControlEditor_Condition_Group;
	public static String NodeSelectionControlEditor_Condition_Message;
	public static String NodeSelectionControlEditor_Condition_Visibility;
	public static String NodeSelectionControlEditor_Label_Image;
	public static String NodeSelectionControlEditor_WarnMessage;
	public static String NodeSelectionEditor_Button_Down;
	public static String NodeSelectionEditor_Button_Up;
	public static String NodeSelectionEditor_Group_DefaultPath;
	public static String NodeSelectionEditor_Group_Order;
	public static String NodeSelectionEditor_Group_Subgroup_DefaultPath;
	public static String NodeSelectionEditor_Label_DefaultPath;
	public static String NodeSelectionEditor_Label_Timeout;
	public static String NodeSelectionEditor_Shell_Title;
	public static String NodeQuizControlEditor_Label_MaxAmount;
	public static String NodeQuizControlEditor_Label_MinAmount;
	public static String NodeQuizControlEditor_Shell_Title;
	public static String NodeQuizControlEditor_ValuesWarnMessage;
	public static String NodeRandomSelectionEditor_Button_Equal_Probability;
	public static String NodeRandomSelectionEditor_Group_Path_Probability;
	public static String NodeRandomSelectionEditor_Label_Path_Title;
	public static String NodeRandomSelectionEditor_Label_Total;
	public static String NodeRandomSelectionEditor_Label_Warning_Total;
	public static String NodeRandomSelectionEditor_Shell_Title;
	public static String NodeResumeEditor_Button_Timeout;
	public static String NodeResumeEditor_Group_Timeout;
	public static String NodeResumeEditor_Shell_Title;
	public static String NodeSelectionControlEditor_Group_Prerequisite_Scenes;
	public static String NodeSelectionControlEditor_Shell_Title;
	public static String TitledNodeEditor_Shell_Title;
	public static String TitledNodeEditor_WarnMessage;
	public static String TitleSelector_Label_Title;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
