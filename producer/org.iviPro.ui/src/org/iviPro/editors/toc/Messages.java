package org.iviPro.editors.toc;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.editors.toc.messages"; //$NON-NLS-1$
	public static String ControlPanel_PositionLabel;
	public static String ControlPanel_Tooltip_AddNewItem;
	public static String ControlPanel_Tooltip_MoveDown;
	public static String ControlPanel_Tooltip_MoveUp;
	public static String TocEditor_EditorTitle;
	public static String TocEditorInput_Title;
	public static String TocEditorInput_ToolTipText;
	public static String TreePanel_ButtonAdd;
	public static String TreePanel_ButtonDown;
	public static String TreePanel_ButtonUp;
	public static String TreePanel_DefaultNewNodeTitle;
	public static String TreePanel_Delete_Confirmation_Text;
	public static String TreePanel_Delete_Confirmation_Title;
	public static String TreePanel_EntryTitle_1;
	public static String TreePanel_EntryTitle_2;
	public static String TreePanel_EntryTitle_3;
	public static String TreePanel_LabelTitleOfTableOfCotent;
	public static String TreePanel_MenuDelete;
	public static String TreePanel_MenuRemoveScene;
	public static String TreePanel_MenuRename;
	public static String TreePanel_LEFT_HEADER;
	public static String TreePanel_RIGHT_HEADER;
	public static String TreePanel_SceneId_1;
	public static String TreePanel_SceneId_2;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
