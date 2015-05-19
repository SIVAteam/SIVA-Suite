package org.iviPro.editors.annotationeditor.components.contenteditors.richtext.dialog;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.editors.annotationeditor.components.contenteditors.richtext.dialog.messages"; //$NON-NLS-1$
	public static String ImageDialog_Thumbnail_Label_Title;
	public static String ImageDialog_Thumbnail_Label_Path;
	public static String ImageDialog_Thumbnail_Label_Resolution;
	public static String ImageDialog_Thumbnail_Error_Dialog_Title;
	public static String ImageDialog_Thumbnail_Error_LoadingImage;
	public static String ImageDialog_Button_Insert;
	public static String ImageDialog_Button_Cancel;
	public static String ImageDialog_Group_Controls;
	public static String ImageDialog_Group_Options;
	public static String ImageDialog_Label_Width;
	public static String ImageDialog_Label_Height;
	public static String ImageDialog_Label_Ratio;
	public static String ImageDialog_Title;

	public static String LinkDialog_Title;
	public static String LinkDialog_InputLabel;
	public static String LinkDialog_OK;
	public static String LinkDialog_Cancel;
	public static String ImageResize_Dialog_Title;
	public static String ImageResize_Dialog_Options_Title;
	public static String ImageResize_Dialog_Button_Cancel;
	public static String ImageResize_Dialog_Button_OK;
	public static String ImageResize_Dialog_Dimensions_Title;
	public static String ImageResize_Dialog_Width;
	public static String ImageResize_Dialog_Height;
	public static String ImageResize_Dialog_KeepRatio;
	

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
