package org.iviPro.newExport;

import java.io.File;

/**
 * Defines the constants used during the export process.
 * 
 * @author Codebold
 * 
 */
public interface ExportDefinition {

	/** The name of the player archive. */
	public static final String ZIP_FILENAME = "player.zip"; //$NON-NLS-1$

	/** The name of folder containing the exported files. */
	public static final String EXPORT_DIRECTORY = "export"; //$NON-NLS-1$

	/** The default export profile title. */
	public static final String PROFILE_DEFAULT_TITLE = "Untitled";

	/** The default export profile description. */
	public static final String PROFILE_DEFAULT_DESCRIPTION = "A short description about the profile...";

	/** The name of the XML folder inside the export directory. */
	public static final String XML_DIRECTORY = "XML"; //$NON-NLS-1$

	/** The name of the image folder inside the export directory. */
	public static final String PICTURES_DIRECTORY = "pix"; //$NON-NLS-1$

	/** The name of the richtext folder inside the export directory. */
	public static final String RICHTEXTS_DIRECTORY = "richpages"; //$NON-NLS-1$

	/** The name of the video folder inside the export directory. */
	public static final String VIDEOS_DIRECTORY = "videos"; //$NON-NLS-1$

	/** The name of the audio folder inside the export directory. */
	public static final String AUDIOS_DIRECTORY = "audios"; //$NON-NLS-1$
	
	/** The name of the pdf folder inside the export directory. */
	public static final String PDF_DIRECTORY = "pdfs"; //$NON-NLS-1$

	/** The default title for audio or video variants. */
	public static final String VARIANT_DEFAULT_TITLE = "default";

	/** The default description for the audio variant. */
	public static final String AUDIO_VARIANT_DEFAULT_DESCRIPTION = "The default audio variant.";

	/** The default description for the video variant. */
	public static final String VIDEO_VARIANT_DEFAULT_DESCRIPTION = "The default video variant.";

	/** The prefix of the HTML directories. */
	public static final String HTML_PREFIX = "Files" + File.separator; //$NON-NLS-1$

	/** The name of the temporary directory used during the export process. */
	public static final String ZIP_TMP_DIRECTORY = "tmp"; //$NON-NLS-1$
}
