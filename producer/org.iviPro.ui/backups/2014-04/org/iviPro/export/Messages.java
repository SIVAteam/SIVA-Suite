package org.iviPro.export;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.export.messages"; //$NON-NLS-1$
	public static String ExportDialog_Button_OnlyXML;
	public static String ExportDialog_Button_OnlyXML_Tooltip;
	public static String ExportDialog_Button_ZIP;
	public static String ExportDialog_ButtonCancel;
	public static String ExportDialog_ButtonOK;
	public static String ExportDialog_DirectoryDialogText;
	public static String ExportDialog_ErrorDirectoryDoesNotExist_Text;
	public static String ExportDialog_ErrorDirectoryDoesNotExist_Title;
	public static String ExportDialog_ErrorSceneGraphEmpty_Text;
	public static String ExportDialog_ErrorSceneGraphEmpty_Title;
	public static String ExportDialog_LabelExportType;
	public static String ExportDialog_LabelPath;
	public static String ExportDialog_WindowTitle;
	public static String ExportProject_ExportInProgress;
	public static String ExportProject_Step1Preprocessing;
	public static String ExportProject_Step2Transcoding;
	public static String ExportProject_Step3XML;

	public static String Exporter_Error_CantCreateDir_1;
	public static String Exporter_Error_CantCreateDir_2;
	public static String Exporter_Error_CantCreateDir_3;
	public static String Exporter_Monitor_Zip_File;
	public static String Exporter_MsgBox_ExportFailed_OkButton;
	public static String Exporter_MsgBox_ExportFailed_Title;
	public static String Exporter_MsgBox_TranscodeError_Text1;
	public static String Exporter_MsgBox_TranscodeError_Text2;
	public static String Exporter_MsgBox_TranscodeError_Text3;
	public static String Exporter_Step_TranscodeVideoAnnotations;
	public static String Exporter_Success_Message;
	public static String Exporter_Success_Title;
	public static String Exporter_TaskTranscodingAudio;
	public static String ExportType_Name_Custom_Settings;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
