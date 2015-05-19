package org.iviPro.editors.audioeditor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.editors.audioeditor.messages"; //$NON-NLS-1$
	public static String AudioEditor_Button_NewAudioPart;
	public static String AudioEditor_MsgBox_SaveAudioPart_Text;
	public static String AudioEditor_MsgBox_SaveAudioPart_Title;
	public static String AudioEditor_SortAudioParts_Tooltip;
	public static String AudioEditor_TabTitle_UnnamedAudioPart;
	public static String AudioEditor_EditorName_Prefix;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
