package org.iviPro.application;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.iviPro.application.messages"; //$NON-NLS-1$

	public static String Application_Key;
	public static String Application_MsgBox_FFMPEG_OkButton;
	public static String Application_MsgBox_FFMPEG_Text;
	public static String Application_MsgBox_FFMPEG_Title;
	public static String Application_MsgBox_FOBS_OkButton;
	public static String Application_MsgBox_FOBS_Text;
	public static String Application_MsgBox_FOBS_Title;
	public static String Application_Value;

	public static String Application_WindowTitle;
	public static String ApplicationActionBarAdvisor_Edit;
	public static String ApplicationActionBarAdvisor_File;
	public static String ApplicationActionBarAdvisor_Help;
	public static String ApplicationActionBarAdvisor_Menu_File_Exit;

	public static String ApplicationActionBarAdvisor_Menu_Media;

	public static String ApplicationActionBarAdvisor_Menu_Project;
	public static String ApplicationWorkbenchWindowAdvisor_Title;

	// Labels die im ErrorDialog benutzt werden
	// Siehe ApplicationWorkbenchAdvisor.eventLoopException()
	public static String ErrorDialog_Title;
	public static String ErrorDialog_Subtitle;
	public static String ErrorDialog_Subtitle_NoMsg;
	public static String ErrorDialog_Details;

	public static String StartupExtension_MsgBox_SaveChangesBeforeQuit_Text;

	public static String StartupExtension_MsgBox_SaveChangesBeforeQuit_Title;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
