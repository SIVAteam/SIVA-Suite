package org.iviPro.newExport.profile.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.AudioConfiguration;
import org.iviPro.newExport.profile.AudioProfile;
import org.iviPro.newExport.profile.AudioVariant;
import org.iviPro.newExport.profile.GeneralConfiguration;
import org.iviPro.newExport.profile.Profile;
import org.iviPro.newExport.profile.VideoBitRateType;
import org.iviPro.newExport.profile.VideoConfiguration;
import org.iviPro.newExport.profile.VideoProfile;
import org.iviPro.newExport.profile.VideoVariant;
import org.iviPro.newExport.view.ProfileSelectionPage;
import org.iviPro.transcoding.exception.TranscodingException;
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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handles the export profile file.
 * 
 * @author Codebold
 * 
 */
public class ProfileHandler extends DefaultHandler implements ProfileDefinition {

	private static Logger logger = Logger.getLogger(ProfileHandler.class);

	/** Specifies if the handler already parsed the export profile file. */
	private boolean parsed;

	/** The resulting profile. */
	private Profile result;

	/** The temporary audio variant */
	private AudioVariant tmpAudioVariant;

	/** The temporary video variant */
	private VideoVariant tmpVideoVariant;

	/**
	 * Constructs a <code>ProfileHandler</code>.
	 */
	public ProfileHandler() {
		this.parsed = false;
	}

	/**
	 * Gets the export profile.
	 * 
	 * @return The export profile.
	 * @throws IllegalStateException
	 *             If the export profile file has not been completely parsed
	 *             already.
	 */
	public Profile getProfile() {
		if (!parsed) {
			throw new IllegalStateException(
					Messages.Exception_XmlNotCompletelyParsed);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (qName.equals(ELEMENT_PROFILE)) {
			handleProfileElement(attributes);
		} else if (qName.equals(ELEMENT_AUDIO_VARIANTS)) {
			// Do nothing
		} else if (qName.equals(ELEMENT_AUDIO_VARIANT)) {
			handleAudioVariantElement(attributes);
		} else if (qName.equals(ELEMENT_AUDIO_PROFILE)) {
			handleAudioProfileElement(attributes);
		} else if (qName.equals(ELEMENT_VIDEO_VARIANTS)) {
			// Do nothing
		} else if (qName.equals(ELEMENT_VIDEO_VARIANT)) {
			handleVideoVariantElement(attributes);
		} else if (qName.equals(ELEMENT_VIDEO_PROFILE)) {
			handleVideoProfileElement(attributes);
		} else {
			logger.warn("Skipping unknown xml element: " + qName);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equals(ELEMENT_PROFILE)) {
			parsed = true;
		}
	}

	/**
	 * Handles the {@link Profile} element.
	 * 
	 * @param attributes
	 *            The attributes of the element.
	 */
	private void handleProfileElement(Attributes attributes) {
		result = new Profile(
				new GeneralConfiguration(attributes.getValue(ATTRIBUTE_TITLE),
						attributes.getValue(ATTRIBUTE_DESCRIPTION),
						Boolean.parseBoolean(attributes
								.getValue(ATTRIBUTE_EXPORT_XML)),
						Boolean.parseBoolean(attributes
								.getValue(ATTRIBUTE_EXPORT_SMIL)),
						Boolean.parseBoolean(attributes
								.getValue(ATTRIBUTE_EXPORT_FLASH_PLAYER)),
						Boolean.parseBoolean(attributes
								.getValue(ATTRIBUTE_EXPORT_HTML_PLAYER)),
						Boolean.parseBoolean(attributes
								.getValue(ATTRIBUTE_EXPORT_AUDIO_EXTENSIONS)),
						Boolean.parseBoolean(attributes
								.getValue(ATTRIBUTE_EXPORT_VIDEO_EXTENSIONS)),
						harmonizeFileSeparators(attributes
								.getValue(ATTRIBUTE_DESCRIPTOR_DIRECTORY)),
						harmonizeFileSeparators(attributes
								.getValue(ATTRIBUTE_IMAGE_DIRECTORY)),
						harmonizeFileSeparators(attributes
								.getValue(ATTRIBUTE_RICHPAGE_DIRECTORY)),
						harmonizeFileSeparators(attributes
								.getValue(ATTRIBUTE_AUDIO_DIRECTORY)),
						harmonizeFileSeparators(attributes
								.getValue(ATTRIBUTE_VIDEO_DIRECTORY)),
						harmonizeFileSeparators(attributes
								.getValue(ATTRIBUTE_PDF_DIRECTORY))),
				new AudioConfiguration(new ArrayList<AudioVariant>()),
				new VideoConfiguration(new ArrayList<VideoVariant>()));
	}

	/**
	 * Handles the {@link AudioVariant} element.
	 * 
	 * @param attributes
	 *            The attributes of the element.
	 * @throws SAXException
	 *             If an error occurs while parsing the element.
	 */
	private void handleAudioVariantElement(Attributes attributes)
			throws SAXException {
		tmpAudioVariant = new AudioVariant(
				attributes.getValue(ATTRIBUTE_TITLE),
				attributes.getValue(ATTRIBUTE_DESCRIPTION),
				new ArrayList<AudioProfile>());
		result.getAudio().getAudioVariants().add(tmpAudioVariant);
	}

	/**
	 * Handles the {@link AudioProfile} element.
	 * 
	 * @param attributes
	 *            The attributes of the element.
	 * @throws SAXException
	 *             If an error occurs while parsing the element.
	 */
	private void handleAudioProfileElement(Attributes attributes)
			throws SAXException {
		if (tmpAudioVariant == null) {
			throw new SAXException(Messages.Exception_XmlChildWithoutParent);
		}
		try {
			tmpAudioVariant
					.getAudioProfiles()
					.add(new AudioProfile(
							AudioContainer.fromTranscoderParameter(attributes
									.getValue(ATTRIBUTE_AUDIO_CONTAINER)),
							AudioCodec.fromTranscoderParameter(attributes
									.getValue(ATTRIBUTE_AUDIO_CODEC)),
							AudioBitRate.fromInteger(Integer.parseInt(attributes
									.getValue(ATTRIBUTE_AUDIO_BIT_RATE))),
							SampleRate.fromInteger(Integer.parseInt(attributes
									.getValue(ATTRIBUTE_SAMPLE_RATE))),
							SampleSize.fromInteger(Integer.parseInt(attributes
									.getValue(ATTRIBUTE_SAMPLE_SIZE))),
							Channels.fromInteger(Integer.parseInt(attributes
									.getValue(ATTRIBUTE_CHANNELS)))));
		} catch (TranscodingException e) {
			throw new SAXException(e.getMessage(), e);
		}
	}

	/**
	 * Handles the {@link VideoVariant} element.
	 * 
	 * @param attributes
	 *            The attributes of the element.
	 * @throws SAXException
	 *             If an error occurs while parsing the element.
	 */
	private void handleVideoVariantElement(Attributes attributes)
			throws SAXException {
		tmpVideoVariant = new VideoVariant(
				attributes.getValue(ATTRIBUTE_TITLE),
				attributes.getValue(ATTRIBUTE_DESCRIPTION),
				new ArrayList<VideoProfile>());
		result.getVideo().getVideoVariants().add(tmpVideoVariant);
	}

	/**
	 * Handles the {@link VideoProfile} element.
	 * 
	 * @param attributes
	 *            The attributes of the element.
	 * @throws SAXException
	 *             If an error occurs while parsing the element.
	 */
	private void handleVideoProfileElement(Attributes attributes)
			throws SAXException {
		if (tmpVideoVariant == null) {
			throw new SAXException(Messages.Exception_XmlChildWithoutParent);
		}
		
		try {
			VideoCodec codec = VideoCodec.fromTranscoderParameter(attributes
					.getValue(ATTRIBUTE_VIDEO_CODEC));
			VideoCodecQuality quality = null;
			// Need to check whether or not a quality profile may be specified
			if (codec.getSupportedQualityProfiles() != null) {
				quality = VideoCodecQuality.fromTranscoderParameter(attributes
						.getValue(ATTRIBUTE_VIDEO_CODEC_QUALITY));
			}
				
			tmpVideoVariant
					.getVideoProfiles()
					.add(new VideoProfile(
							VideoContainer.fromTranscoderParameter(attributes
									.getValue(ATTRIBUTE_VIDEO_CONTAINER)),
							codec,
							quality,
							VideoBitRateType.fromParameter(attributes
									.getValue(ATTRIBUTE_VIDEO_BIT_RATE_TYPE)),
							Integer.parseInt(attributes
									.getValue(ATTRIBUTE_VIDEO_BIT_RATE)),
							FrameRate.fromDouble(Double.parseDouble(attributes
									.getValue(ATTRIBUTE_FRAME_RATE))),
							AudioCodec.fromTranscoderParameter(attributes
									.getValue(ATTRIBUTE_AUDIO_CODEC)),
							AudioBitRate.fromInteger(Integer.parseInt(attributes
									.getValue(ATTRIBUTE_AUDIO_BIT_RATE))),
							SampleRate.fromInteger(Integer.parseInt(attributes
									.getValue(ATTRIBUTE_SAMPLE_RATE))),
							SampleSize.fromInteger(Integer.parseInt(attributes
									.getValue(ATTRIBUTE_SAMPLE_SIZE))),
							Channels.fromInteger(Integer.parseInt(attributes
									.getValue(ATTRIBUTE_CHANNELS)))));
		} catch (TranscodingException e) {
			throw new SAXException(e.getMessage(), e);
		} catch (ExportException e) {
			throw new SAXException(e.getMessage(), e);
		}
	}

	private String harmonizeFileSeparators(String s) {
		return s.replaceAll("\\\\|/", Matcher.quoteReplacement(File.separator)); //$NON-NLS-1$
	}

}
