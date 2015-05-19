package org.iviPro.editors.sceneeditor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.editors.sceneeditor.messages"; //$NON-NLS-1$
	public static String DefineScenesEditor_Button_NewScene;
	public static String DefineScenesEditor_EditorName_Prefix;
	public static String DefineScenesEditor_MsgBox_SaveScene_Text;
	public static String DefineScenesEditor_MsgBox_SaveScene_Title;
	public static String DefineScenesEditor_SortScenes_Tooltip;
	public static String DefineScenesEditor_TabTitle_UnnamedScene;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
