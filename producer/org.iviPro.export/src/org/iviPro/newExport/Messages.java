package org.iviPro.newExport;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.newExport.messages"; //$NON-NLS-1$

	public static String Exception_CannotDeleteDefaultVariant;
	public static String Exception_CannotEditDefaultVariant;

	public static String Exception_CannotReadResource;
	public static String Exception_CompressingOutputFailed;
	public static String Exception_CopyingOutputFailed;
	public static String Exception_CopyingPlayerFilesFailed;

	public static String Exception_CopyPdfFailed;
	public static String Exception_CreatingActionIdFailed;
	public static String Exception_CreatingDescriptorDirectoryFailed;
	public static String Exception_CreatingDirectoryFailed;
	public static String Exception_CreatingResourceIdFailed;
	public static String Exception_ExportingProfileFailed;
	public static String Exception_ExtractingExtensionFailed;
	public static String Exception_GettingDocumentBuilderFailed;
	public static String Exception_GettingXmlTransformerFailed;
	public static String Exception_PreparingExportDirectoriesFailed;
	public static String Exception_ProfileFileNotFound;
	public static String Exception_ProjectFileNotFound;
	public static String Exception_SmileNotSupported;
	public static String Exception_TaskCanceledByUser;
	public static String Exception_TempDirectoryDoesNotExist;
	public static String Exception_TempDirectoryNotAvailable;
	public static String Exception_TempDirectoryNotWriteable;
	public static String Exception_UnknownExportEntity;
	public static String Exception_WritingDescriptorFileFailed;

	public static String Exception_WritingJSONFileFailed;

	public static String Exception_WrongHtmlFileFormat;
	public static String Exception_CopyRichPageFailed;
	public static String Exception_CreatingStaticAnnotationDirectoryFailed;
	public static String Exception_InvalidAudioParameters;
	public static String Exception_InvaliedVideoParameters;
	public static String Exception_ReadingImageFailed;
	public static String Exception_DeletingProfileFailed;
	public static String Exception_DirectoryNotFound;
	public static String Exception_GettingProfilesFromPreferencesFailed;
	public static String Exception_NoSaxTransformerFactory;
	public static String Exception_ParsingProfileFailed;
	public static String Exception_ParsingProfileFileFailed;
	public static String Exception_RemovingPreferencesNodeFailed;
	public static String Exception_StoringProfileFailed;
	public static String Exception_ReadingProjectFileFailed;
	public static String Exception_TranscodingCanceledByUser;
	public static String Exception_TranscodingFailed;
	public static String Exception_XmlChildWithoutParent;
	public static String Exception_XmlNotCompletelyParsed;
	public static String Exception_ZipOutputFileAlreadyExists;

	public static String ExportInteractiveVideo;
	public static String ExportInteractiveVideoToolTip;

	/* --- EXPORT WIZARD --------------------------------- EXPORT WIZARD --- */
	public static String ExportWizard_Title;
	public static String ProfileWizard_Title;

	/* --- EXPORT PROFILES ----------------------------- EXPORT PROFILES --- */

	public static String Descriptor_XmlDescriptor;
	public static String Descriptor_ExportPlayer;
	public static String Descriptor_ExportAudioExtensions;
	public static String Descriptor_ExportVideoExtensions;
	public static String Descriptor_SmilDescriptor;

	public static String OutputFolder;
	public static String OutputFolder_SelectorTitle;
	public static String OutputFolder_ChooseFolder;
	public static String OutputFolder_NoSuchFolderTitle;
	public static String OutputFolder_NoSuchFolderMessage;

	public static String Task_Annotations;
	public static String Task_CleaningUp;
	public static String Task_Descriptor;
	public static String Task_Export;
	public static String Task_Finishing;
	public static String Task_Flash;
	public static String Task_HTML;
	public static String Task_LoadProject;
	public static String Task_Media;
	public static String Task_Player;
	public static String Task_Preparing;
	public static String Task_SMIL;
	public static String Task_Static;
	public static String Task_XML;

	public static String Title;
	public static String TitleToolTip;
	public static String Description;
	public static String DescriptionToolTip;
	public static String Descriptors;
	public static String DescriptorsToolTip;
	public static String Players;
	public static String PlayersToolTip;
	public static String Extensions;
	public static String ExtensionsToolTip;
	public static String DescriptorDirectory;
	public static String DescriptorDirectoryToolTip;
	public static String ImageDirectory;
	public static String ImageDirectoryToolTip;
	public static String RichPageDirectory;
	public static String RichPageDirectoryToolTip;

	public static String AudioDirectory;
	public static String AudioDirectoryToolTip;
	public static String VideoBitRateToolTip;
	public static String VideoCodecToolTip;
	public static String VideoContainerToolTip;
	public static String VideoDirectory;
	public static String VideoDirectoryToolTip;
	public static String VideoProfile;

	public static String VideoProfileDialog_Bitrate_Warn_Num;

	public static String VideoProfileDialog_Bitrate_Warn_Range;

	public static String VideoProfileDialog_CodecQuality;

	public static String VideoProfileDialog_CodecQuality_Tooltip;

	public static String VideoProfileDialog_Dimension;

	public static String VideoProfileDialog_Dimensions_Tooltip;

	public static String VideoProfileDialog_Height_Warn_Num;

	public static String VideoProfileDialog_Height_Warn_Range;

	public static String VideoProfileDialog_Width_Warn_Num;

	public static String VideoProfileDialog_Width_Warn_Range;

	public static String Add;
	public static String Edit;
	public static String Delete;
	public static String Aborting;
	public static String Canceled;
	public static String Browse;
	public static String Yes;
	public static String No;

	public static String AudioPage_Title;
	public static String AudioPage_Description;
	public static String AudioProfile;
	public static String VideoPage_Description;
	public static String VideoPage_Title;

	public static String AudioFormats;
	public static String AudioFormatsToolTip;
	public static String AudioVariants;
	public static String AudioVariantsToolTip;

	public static String BitRate;
	public static String AudioBitRateToolTip;
	public static String Container;
	public static String AudioContainerToolTip;
	public static String Codec;
	public static String AudioCodecToolTip;
	public static String Channels;
	public static String ChannelsToolTip;

	public static String VideoVariants;
	public static String VideoVariantsToolTip;

	public static String FrameRate;
	public static String FrameRateToolTip;
	public static String Width;
	public static String WidthToolTip;
	public static String Height;
	public static String HeightToolTip;
	public static String SampleRate;
	public static String SampleRateToolTip;
	public static String SampleSize;
	public static String SampleSizeToolTip;
	public static String VideoFormats;
	public static String VideoFormatsToolTip;

	public static String GeneralPage_Description;
	public static String GeneralPage_Title;

	public static String Error;
	public static String Error_NoWritePermissionMessage;
	public static String Error_NotADirectoryMessage;

	public static String ExportProfilesTable_Export;
	public static String ExportProfilesTable_ExportToolTip;
	public static String ExportProfilesTable_Profile;
	public static String ExportProfilesTable_ProfileToolTip;
	public static String ExportProfilesTable_Description;
	public static String ExportProfilesTable_DescriptionToolTip;
	public static String ExportProfilesTable_Resources;
	public static String ExportProfilesTable_ResourcesToolTip;
	public static String ExportProfilesTable_Compress;
	public static String ExportProfilesTable_CompressToolTip;
	public static String ExportProfilesTable_Convert;
	public static String ExportProfilesTable_ConvertToolTip;

	public static String ProfileSelectionPage_Title;
	public static String ProfileSelectionPage_Description;
	public static String ProfileSelectionPage_AddToolTip;
	public static String ProfileSelectionPage_Compress;
	public static String ProfileSelectionPage_DeleteToolTip;
	public static String ProfileSelectionPage_EditToolTip;

	public static String ProfileSelectionPage_OpenFolder_Label;
	public static String ProfileTitleDialog_Description;
	public static String ProfileTitleDialog_Title;
	public static String ProfileTitleDialog_Warning;

	public static String Warning;
	public static String Warning_AddingProfileFailed;
	public static String Warning_AudioDirectoryTooLong;
	public static String Warning_DeletingDefaultProfileProhibited;
	public static String Warning_DescriptionTooLong;
	public static String Warning_DescriptorDirectoryTooLong;
	public static String Warning_DirectoryTooLong;
	public static String Warning_EditingDefaultProfileProhibited;
	public static String Warning_EditingProfileFailed;
	public static String Warning_TitleEmpty;
	public static String Warning_ImageDirectoryTooLong;
	public static String Warning_RichPageDirectoryTooLong;
	public static String Warning_TitleAlreadyInUse;
	public static String Warning_TitleTooLong;
	public static String Warning_VideoDirectoryTooLong;
	public static String Warning_LabelAlreadyExists;
	public static String Warning_LabelCannotBeEmpty;
	public static String Warning_LabelSize;
	public static String WarningCouldNotStorePreferences;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
