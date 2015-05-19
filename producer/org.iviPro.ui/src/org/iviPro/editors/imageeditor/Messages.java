package org.iviPro.editors.imageeditor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.editors.imageeditor.messages"; //$NON-NLS-1$
	public static String ImageEditWidget_Circle;
	public static String ImageEditWidget_Color_select;
	public static String ImageEditWidget_Delete;
	public static String ImageEditWidget_Move_Below;
	public static String ImageEditWidget_Rectangle;
	public static String ImageEditWidget_Save;
	public static String ImageEditWidget_Selection;
	public static String ImageEditWidget_Text;
	public static String ImageEditWidget_WarningTransparentRegion;
	public static String ImageEditWidget_LineWidth;
	public static String OverlayTextEditor_Cancel_Button;
	public static String OverlayTextEditor_Editor_Title;
	public static String OverlayTextEditor_OK_Button;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
