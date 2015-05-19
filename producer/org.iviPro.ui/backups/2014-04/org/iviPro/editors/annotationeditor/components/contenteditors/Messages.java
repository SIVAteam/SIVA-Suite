package org.iviPro.editors.annotationeditor.components.contenteditors;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.editors.annotationeditor.components.contenteditors.messages"; //$NON-NLS-1$
	public static String AudioEditor_0;
	public static String AudioEditor_1;
	public static String RichHTMLEditor_ComboFontTitle;
	public static String RichHTMLEditor_ComboFormatTitle;
	public static String RichHTMLEditor_ComboSizeTitle;
	public static String RichHTMLEditor_TabEditorTitle;
	public static String RichHTMLEditor_TabHTMLTitle;
	public static String VideoEditor_0;
	public static String VideoEditor_1;
	public static String ImageEditor_0;
	public static String PictureEditor_SelectElement_None;
	public static String PictureEditor_Delete_Picture;
	public static String PictureEditor_Delete_AllPictures;
	public static String PictureEditor_AddPicGal_Add;
	public static String PictureEditor_AddPicGal_Replace;
	public static String PictureEditor_AddPicGal_Append;
	public static String PictureEditor_AddPicGal_Cancel;
	public static String PictureEditor_AddPicGal_Text;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
