package org.iviPro.newExport.profile.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.AudioProfile;
import org.iviPro.newExport.profile.AudioVariant;
import org.iviPro.newExport.profile.Profile;
import org.iviPro.newExport.profile.VideoProfile;
import org.iviPro.newExport.profile.VideoVariant;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Exports the export profile to XML.
 * 
 * @author Codebold
 * 
 */
public class ProfileExporter implements ProfileDefinition,
		XmlTransformingSettings {
	
	private static final Logger logger = Logger
			.getLogger(ProfileExporter.class);

	private static final String EMPTY = ""; //$NON-NLS-1$

	public static void export(Profile profile, File file)
			throws TransformingException {
		try {
			/* Creating the StreamResult directly from the File seems to leave an
			 * open stream when using a TransformHandler. Using a FileOutputStream
			 * allows for manually closing the relevant stream.
			 */
			FileOutputStream fileStream = new FileOutputStream(file);
			StreamResult sr = new StreamResult(fileStream);
			export(profile, sr);
			sr.getOutputStream().close();
		} catch (FileNotFoundException fnf) {
			logger.error("Could not write profile to disc.");
		} catch (IOException io) {
			logger.error("Error closing stream when writing profile to disc.");
		}
	}

	/**
	 * Exports the submitted {@link Profile} to the desired output.
	 * 
	 * @param profile
	 *            The export profile to export.
	 * @param output
	 *            The target output.
	 * @throws TransformingException
	 *             If an error occurs during exporting the export profile.
	 */
	private static void export(Profile profile, StreamResult output)
			throws TransformingException {
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		if (!transformerFactory.getFeature(SAXTransformerFactory.FEATURE)) {
			throw new RuntimeException(
					Messages.Exception_NoSaxTransformerFactory);
		}
		SAXTransformerFactory saxTransformerFactory = (SAXTransformerFactory) transformerFactory;

		TransformerHandler handler = null;
		try {
			handler = saxTransformerFactory.newTransformerHandler();
		} catch (TransformerConfigurationException cause) {
			throw new TransformingException(
					Messages.Exception_ExportingProfileFailed, cause);
		}
		Transformer transformer = handler.getTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, XML_OUTPUT);
		transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING);
		transformer.setOutputProperty(OutputKeys.VERSION, XML_VERSION);
		// TODO Create a definition file!
		// transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
		// "profile.dtd");
		transformer.setOutputProperty(OutputKeys.STANDALONE, STANDALONE);
		transformer.setOutputProperty(OutputKeys.INDENT, INDENT);

		handler.setResult(output);

		try {
			handler.startDocument();
			exportProfile(handler, profile);
			handler.endDocument();
		} catch (SAXException cause) {
			throw new TransformingException(
					Messages.Exception_ExportingProfileFailed, cause);
		}
	}

	/**
	 * Exports the {@link Profile}.
	 * 
	 * @param handler
	 *            The transformer handler.
	 * @param profile
	 *            The export profile to export.
	 * @throws SAXException
	 *             If an error occurs while creating the XML output.
	 */
	private static void exportProfile(TransformerHandler handler,
			Profile profile) throws SAXException {
		AttributesImpl profileAttributes = new AttributesImpl();
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_TITLE, ATTRIBUTE_TITLE,
				TYPE_CDATA, profile.getProfileTitle());
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_DESCRIPTION,
				ATTRIBUTE_DESCRIPTION, TYPE_CDATA, profile.getGeneral()
						.getDescription());
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_EXPORT_XML,
				ATTRIBUTE_EXPORT_XML, TYPE_CDATA,
				String.valueOf(profile.getGeneral().isExportXml()));
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_EXPORT_SMIL,
				ATTRIBUTE_EXPORT_SMIL, TYPE_CDATA,
				String.valueOf(profile.getGeneral().isExportSmil()));
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_EXPORT_FLASH_PLAYER,
				ATTRIBUTE_EXPORT_FLASH_PLAYER, TYPE_CDATA,
				String.valueOf(profile.getGeneral().isExportFlashPlayer()));
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_EXPORT_HTML_PLAYER,
				ATTRIBUTE_EXPORT_HTML_PLAYER, TYPE_CDATA,
				String.valueOf(profile.getGeneral().isExportHtmlPlayer()));
		profileAttributes.addAttribute(EMPTY,
				ATTRIBUTE_EXPORT_AUDIO_EXTENSIONS,
				ATTRIBUTE_EXPORT_AUDIO_EXTENSIONS, TYPE_CDATA,
				String.valueOf(profile.getGeneral().isExportAudioExtensions()));
		profileAttributes.addAttribute(EMPTY,
				ATTRIBUTE_EXPORT_VIDEO_EXTENSIONS,
				ATTRIBUTE_EXPORT_VIDEO_EXTENSIONS, TYPE_CDATA,
				String.valueOf(profile.getGeneral().isExportVideoExtensions()));
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_DESCRIPTOR_DIRECTORY,
				ATTRIBUTE_DESCRIPTOR_DIRECTORY, TYPE_CDATA, profile
						.getGeneral().getDescriptorDirectory());
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_IMAGE_DIRECTORY,
				ATTRIBUTE_IMAGE_DIRECTORY, TYPE_CDATA, profile.getGeneral()
						.getImageDirectory());
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_RICHPAGE_DIRECTORY,
				ATTRIBUTE_RICHPAGE_DIRECTORY, TYPE_CDATA, profile.getGeneral()
						.getRichPageDirectory());
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_AUDIO_DIRECTORY,
				ATTRIBUTE_AUDIO_DIRECTORY, TYPE_CDATA, profile.getGeneral()
						.getAudioDirectory());
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_VIDEO_DIRECTORY,
				ATTRIBUTE_VIDEO_DIRECTORY, TYPE_CDATA, profile.getGeneral()
						.getVideoDirectory());
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_PDF_DIRECTORY,
				ATTRIBUTE_PDF_DIRECTORY, TYPE_CDATA, profile.getGeneral()
						.getPdfDirectory());
		handler.startElement(EMPTY, ELEMENT_PROFILE, ELEMENT_PROFILE,
				profileAttributes);
		exportAudioVariants(handler, profile.getAudio().getAudioVariants());
		exportVideoVariants(handler, profile.getVideo().getVideoVariants());
		handler.endElement(EMPTY, ELEMENT_PROFILE, ELEMENT_PROFILE);
	}

	/**
	 * Exports the {@link AudioVariant}s.
	 * 
	 * @param handler
	 *            The transformer handler.
	 * @param audioVariants
	 *            The audio variants to export.
	 * @throws SAXException
	 *             If an error occurs while creating the XML output.
	 */
	private static void exportAudioVariants(TransformerHandler handler,
			List<AudioVariant> audioVariants) throws SAXException {
		handler.startElement(EMPTY, ELEMENT_AUDIO_VARIANTS,
				ELEMENT_AUDIO_VARIANTS, null);
		for (AudioVariant audioVariant : audioVariants) {
			exportAudioVariant(handler, audioVariant);
		}
		handler.endElement(EMPTY, ELEMENT_AUDIO_VARIANTS,
				ELEMENT_AUDIO_VARIANTS);
	}

	/**
	 * Exports the actual {@link AudioVariant}.
	 * 
	 * @param handler
	 *            The transformer handler.
	 * @param audioVariant
	 *            The actual audio variant to export.
	 * @throws SAXException
	 *             If an error occurs while creating the XML output.
	 */
	private static void exportAudioVariant(TransformerHandler handler,
			AudioVariant audioVariant) throws SAXException {
		AttributesImpl variantAttributes = new AttributesImpl();
		variantAttributes.addAttribute(EMPTY, ATTRIBUTE_TITLE, ATTRIBUTE_TITLE,
				TYPE_CDATA, audioVariant.getTitle());
		variantAttributes.addAttribute(EMPTY, ATTRIBUTE_DESCRIPTION,
				ATTRIBUTE_DESCRIPTION, TYPE_CDATA,
				audioVariant.getDescription());
		handler.startElement(EMPTY, ELEMENT_AUDIO_VARIANT,
				ELEMENT_AUDIO_VARIANT, variantAttributes);
		for (AudioProfile audioProfile : audioVariant.getAudioProfiles()) {
			exportAudioProfile(handler, audioProfile);
		}
		handler.endElement(EMPTY, ELEMENT_AUDIO_VARIANT, ELEMENT_AUDIO_VARIANT);
	}

	/**
	 * Exports the actual {@link AudioProfile}.
	 * 
	 * @param handler
	 *            The transformer handler.
	 * @param audioProfile
	 *            The actual audio profile to export.
	 * @throws SAXException
	 *             If an error occurs while creating the XML output.
	 */
	private static void exportAudioProfile(TransformerHandler handler,
			AudioProfile audioProfile) throws SAXException {
		AttributesImpl profileAttributes = new AttributesImpl();
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_AUDIO_CONTAINER,
				ATTRIBUTE_AUDIO_CONTAINER, TYPE_CDATA, audioProfile
						.getAudioContainer().getTranscoderParameter());
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_AUDIO_CODEC,
				ATTRIBUTE_AUDIO_CODEC, TYPE_CDATA, audioProfile.getAudioCodec()
						.getTranscoderParameter());
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_AUDIO_BIT_RATE,
				ATTRIBUTE_AUDIO_BIT_RATE, TYPE_CDATA,
				String.valueOf(audioProfile.getBitRate().getBitRate()));
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_SAMPLE_RATE,
				ATTRIBUTE_SAMPLE_RATE, TYPE_CDATA,
				String.valueOf(audioProfile.getSampleRate().getSampleRate()));
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_SAMPLE_SIZE,
				ATTRIBUTE_SAMPLE_SIZE, TYPE_CDATA,
				String.valueOf(audioProfile.getSampleSize().getSampleSize()));
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_CHANNELS,
				ATTRIBUTE_CHANNELS, TYPE_CDATA,
				String.valueOf(audioProfile.getChannels().getChannels()));
		handler.startElement(EMPTY, ELEMENT_AUDIO_PROFILE,
				ELEMENT_AUDIO_PROFILE, profileAttributes);
		handler.endElement(EMPTY, ELEMENT_AUDIO_PROFILE, ELEMENT_AUDIO_PROFILE);
	}

	/**
	 * Exports the {@link VideoVariant}s.
	 * 
	 * @param handler
	 *            The transformer handler.
	 * @param videoVariants
	 *            The video variants to export.
	 * @throws SAXException
	 *             If an error occurs while creating the XML output.
	 */
	private static void exportVideoVariants(TransformerHandler handler,
			List<VideoVariant> videoVariants) throws SAXException {
		handler.startElement(EMPTY, ELEMENT_VIDEO_VARIANTS,
				ELEMENT_VIDEO_VARIANTS, null);
		for (VideoVariant videoVariant : videoVariants) {
			exportVideoVariant(handler, videoVariant);
		}
		handler.endElement(EMPTY, ELEMENT_VIDEO_VARIANTS,
				ELEMENT_VIDEO_VARIANTS);
	}

	/**
	 * Exports the actual {@link VideoVariant}.
	 * 
	 * @param handler
	 *            The transformer handler.
	 * @param videoVariant
	 *            The actual video variant to export.
	 * @throws SAXException
	 *             If an error occurs while creating the XML output.
	 */
	private static void exportVideoVariant(TransformerHandler handler,
			VideoVariant videoVariant) throws SAXException {
		AttributesImpl variantAttributes = new AttributesImpl();
		variantAttributes.addAttribute(EMPTY, ATTRIBUTE_TITLE, ATTRIBUTE_TITLE,
				TYPE_CDATA, videoVariant.getTitle());
		variantAttributes.addAttribute(EMPTY, ATTRIBUTE_DESCRIPTION,
				ATTRIBUTE_DESCRIPTION, TYPE_CDATA,
				videoVariant.getDescription());
		handler.startElement(EMPTY, ELEMENT_VIDEO_VARIANT,
				ELEMENT_VIDEO_VARIANT, variantAttributes);
		for (VideoProfile videoProfile : videoVariant.getVideoProfiles()) {
			exportVideoProfile(handler, videoProfile);
		}
		handler.endElement(EMPTY, ELEMENT_VIDEO_VARIANT, ELEMENT_VIDEO_VARIANT);

	}

	/**
	 * Exports the actual {@link VideoProfile}.
	 * 
	 * @param handler
	 *            The transformer handler.
	 * @param videoProfile
	 *            The actual video profile to export.
	 * @throws SAXException
	 *             If an error occurs while creating the XML output.
	 */
	private static void exportVideoProfile(TransformerHandler handler,
			VideoProfile videoProfile) throws SAXException {
		AttributesImpl profileAttributes = new AttributesImpl();
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_VIDEO_CONTAINER,
				ATTRIBUTE_VIDEO_CONTAINER, TYPE_CDATA, videoProfile
						.getVideoContainer().getTranscoderParameter());
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_VIDEO_CODEC,
				ATTRIBUTE_VIDEO_CODEC, TYPE_CDATA, videoProfile.getVideoCodec()
						.getTranscoderParameter());
		// Check if a quality has been set otherwise ignore that attribute 
		if (videoProfile.getVideoCodecQuality() != null) {
			profileAttributes.addAttribute(EMPTY, ATTRIBUTE_VIDEO_CODEC_QUALITY,
					ATTRIBUTE_VIDEO_CODEC_QUALITY, TYPE_CDATA, videoProfile.getVideoCodecQuality()
					.getTranscoderParameter());
		}
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_VIDEO_BIT_RATE_TYPE,
				ATTRIBUTE_VIDEO_BIT_RATE_TYPE, TYPE_CDATA, videoProfile
						.getVideoBitRateType().getParameter());
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_VIDEO_BIT_RATE,
				ATTRIBUTE_VIDEO_BIT_RATE, TYPE_CDATA,
				String.valueOf(videoProfile.getVideoBitRate()));
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_FRAME_RATE,
				ATTRIBUTE_FRAME_RATE, TYPE_CDATA,
				String.valueOf(videoProfile.getFrameRate().getFrameRate()));
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_AUDIO_CODEC,
				ATTRIBUTE_AUDIO_CODEC, TYPE_CDATA, videoProfile.getAudioCodec()
						.getTranscoderParameter());
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_AUDIO_BIT_RATE,
				ATTRIBUTE_AUDIO_BIT_RATE, TYPE_CDATA,
				String.valueOf(videoProfile.getAudioBitRate().getBitRate()));
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_SAMPLE_RATE,
				ATTRIBUTE_SAMPLE_RATE, TYPE_CDATA,
				String.valueOf(videoProfile.getSampleRate().getSampleRate()));
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_SAMPLE_SIZE,
				ATTRIBUTE_SAMPLE_SIZE, TYPE_CDATA,
				String.valueOf(videoProfile.getSampleSize().getSampleSize()));
		profileAttributes.addAttribute(EMPTY, ATTRIBUTE_CHANNELS,
				ATTRIBUTE_CHANNELS, TYPE_CDATA,
				String.valueOf(videoProfile.getChannels().getChannels()));
		handler.startElement(EMPTY, ELEMENT_VIDEO_PROFILE,
				ELEMENT_VIDEO_PROFILE, profileAttributes);
		handler.endElement(EMPTY, ELEMENT_VIDEO_PROFILE, ELEMENT_VIDEO_PROFILE);
	}
}
