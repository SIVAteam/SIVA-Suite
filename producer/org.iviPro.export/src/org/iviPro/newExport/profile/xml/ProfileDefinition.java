package org.iviPro.newExport.profile.xml;

import org.iviPro.newExport.profile.AudioProfile;
import org.iviPro.newExport.profile.AudioVariant;
import org.iviPro.newExport.profile.VideoBitRateType;
import org.iviPro.newExport.profile.VideoProfile;
import org.iviPro.newExport.profile.VideoVariant;
import org.iviPro.transcoding.format.AudioBitRate;
import org.iviPro.transcoding.format.AudioCodec;
import org.iviPro.transcoding.format.AudioContainer;
import org.iviPro.transcoding.format.Channels;
import org.iviPro.transcoding.format.FrameRate;
import org.iviPro.transcoding.format.SampleRate;
import org.iviPro.transcoding.format.SampleSize;
import org.iviPro.transcoding.format.VideoCodec;
import org.iviPro.transcoding.format.VideoCodecQuality;
import org.iviPro.transcoding.format.VideoContainer;

/**
 * Specifies the XML structure of the export profile files.
 * 
 * @author Codebold
 * 
 */
public interface ProfileDefinition {

	/** The CDATA type label. */
	public static final String TYPE_CDATA = "CDATA"; //$NON-NLS-1$

	/** Contains the profile definition. */
	public static final String ELEMENT_PROFILE = "profile"; //$NON-NLS-1$

	/** Specifies the title of the element. */
	public static final String ATTRIBUTE_TITLE = "title"; //$NON-NLS-1$

	/** Specifies the description of the element. */
	public static final String ATTRIBUTE_DESCRIPTION = "description"; //$NON-NLS-1$

	/** Specifies if the XML descriptor file should be exported. */
	public static final String ATTRIBUTE_EXPORT_XML = "exportXml"; //$NON-NLS-1$

	/** Specifies if the SMIL descriptor file should be exported. */
	public static final String ATTRIBUTE_EXPORT_SMIL = "exportSmil"; //$NON-NLS-1$

	/** Specifies if the FLASH player should be exported. */
	public static final String ATTRIBUTE_EXPORT_FLASH_PLAYER = "exportFlashPlayer"; //$NON-NLS-1$

	/** Specifies if the HTML player should be exported. */
	public static final String ATTRIBUTE_EXPORT_HTML_PLAYER = "exportHtmlPlayer"; //$NON-NLS-1$

	/**
	 * Specifies if the extensions of the audio files should be included in the
	 * descriptor file.
	 */
	public static final String ATTRIBUTE_EXPORT_AUDIO_EXTENSIONS = "exportAudioExtensions"; //$NON-NLS-1$

	/**
	 * Specifies if the extensions of the video files should be included in the
	 * descriptor file.
	 */
	public static final String ATTRIBUTE_EXPORT_VIDEO_EXTENSIONS = "exportVideoExtensions"; //$NON-NLS-1$

	/** Specifies the name of the descriptor folder. */
	public static final String ATTRIBUTE_DESCRIPTOR_DIRECTORY = "descriptorDirectory"; //$NON-NLS-1$

	/** Specifies the name of the image folder. */
	public static final String ATTRIBUTE_IMAGE_DIRECTORY = "imageDirectory"; //$NON-NLS-1$

	/** Specifies the name of the rich page folder. */
	public static final String ATTRIBUTE_RICHPAGE_DIRECTORY = "richPageDirectory"; //$NON-NLS-1$

	/** Specifies the name of the audio folder. */
	public static final String ATTRIBUTE_AUDIO_DIRECTORY = "audioDirectory"; //$NON-NLS-1$

	/** Specifies the name of the video folder. */
	public static final String ATTRIBUTE_VIDEO_DIRECTORY = "videoDirectory"; //$NON-NLS-1$
	
	/** Specifies the name of the pdf folder. */
	public static final String ATTRIBUTE_PDF_DIRECTORY = "pdfDirectory"; //$NON-NLS-1$

	/** Contains the {@link AudioVariant}s that should be exported. */
	public static final String ELEMENT_AUDIO_VARIANTS = "audioVariants"; //$NON-NLS-1$

	/** Specifies an {@link AudioVariant}. */
	public static final String ELEMENT_AUDIO_VARIANT = "audioVariant"; //$NON-NLS-1$

	/** Specifies an {@link AudioProfile}. */
	public static final String ELEMENT_AUDIO_PROFILE = "audioProfile"; //$NON-NLS-1$

	/** Specifies the {@link AudioContainer} of the actual audio profile. */
	public static final String ATTRIBUTE_AUDIO_CONTAINER = "audioContainer"; //$NON-NLS-1$

	/** Specifies the {@link AudioCodec} of the actual audio/video profile. */
	public static final String ATTRIBUTE_AUDIO_CODEC = "audioCodec"; //$NON-NLS-1$

	/** Specifies the {@link AudioBitRate} of the actual audio/video profile. */
	public static final String ATTRIBUTE_AUDIO_BIT_RATE = "audioBitRate"; //$NON-NLS-1$

	/** Specifies the {@link SampleRate} of the actual audio/video profile. */
	public static final String ATTRIBUTE_SAMPLE_RATE = "sampleRate"; //$NON-NLS-1$

	/** Specifies the {@link SampleSize} of the actual audio/video profile. */
	public static final String ATTRIBUTE_SAMPLE_SIZE = "sampleSize"; //$NON-NLS-1$

	/** Specifies the {@link Channels} of the actual audio/video profile. */
	public static final String ATTRIBUTE_CHANNELS = "channels"; //$NON-NLS-1$

	/** Contains the {@link VideoVariant}s that should be exported. */
	public static final String ELEMENT_VIDEO_VARIANTS = "videoVariants"; //$NON-NLS-1$

	/** Specifies a {@link VideoVariant}. */
	public static final String ELEMENT_VIDEO_VARIANT = "videoVariant"; //$NON-NLS-1$

	/** Specifies a {@link VideoProfile}. */
	public static final String ELEMENT_VIDEO_PROFILE = "videoProfile"; //$NON-NLS-1$

	/** Specifies the {@link VideoContainer} of the actual video profile. */
	public static final String ATTRIBUTE_VIDEO_CONTAINER = "videoContainer"; //$NON-NLS-1$

	/** Specifies the {@link VideoCodec} of the actual video profile. */
	public static final String ATTRIBUTE_VIDEO_CODEC = "videoCodec"; //$NON-NLS-1$
	
	/** Specifies the {@link VideoCodecQuality} of the actual video profile. */
	public static final String ATTRIBUTE_VIDEO_CODEC_QUALITY = "videoCodecQuality"; //$NON-NLS-1$

	/** Specifies the {@link VideoBitRateType} of the actual video profile. */
	public static final String ATTRIBUTE_VIDEO_BIT_RATE_TYPE = "videoBitRateType"; //$NON-NLS-1$

	/** Specifies the bit rate of the actual video profile. */
	public static final String ATTRIBUTE_VIDEO_BIT_RATE = "videoBitRate"; //$NON-NLS-1$

	/** Specifies the {@link FrameRate} of the actual video profile. */
	public static final String ATTRIBUTE_FRAME_RATE = "frameRate"; //$NON-NLS-1$
}
