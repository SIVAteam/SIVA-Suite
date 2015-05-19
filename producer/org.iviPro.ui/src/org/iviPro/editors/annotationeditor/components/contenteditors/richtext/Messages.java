package org.iviPro.editors.annotationeditor.components.contenteditors.richtext;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.editors.annotationeditor.components.contenteditors.richtext.messages"; //$NON-NLS-1$
	public static String RichHTMLEditor_Cut_Name;
	public static String RichHTMLEditor_Copy_Name;
	public static String RichHTMLEditor_Paste_Name;
	public static String RichHTMLEditor_Underline_Name;
	public static String RichHTMLEditor_Italic_Name;
	public static String RichHTMLEditor_ForegroundColorPicker_Name;
	public static String RichHTMLEditor_Bold_Name;
	public static String RichHTMLEditor_AlignCenter_Name;
	public static String RichHTMLEditor_AlignRight_Name;
	public static String RichHTMLEditor_AlignJustify_Name;
	public static String RichHTMLEditor_AlignLeft_Name;
	public static String RichHTMLEditor_Error_MsgBoxTitle;
	public static String RichHTMLEditor_Error_MsgBoxText_LookAndFeel;
	public static String RichHTMLEditor_FontSize_Name;
	public static String RichHTMLEditor_Font_Name;
	public static String RichHTMLEditor_InsertImage_Name;
	public static String RichHTMLEditor_Image_Resize;
	public static String RichHTMLEditor_Image_Delete;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
