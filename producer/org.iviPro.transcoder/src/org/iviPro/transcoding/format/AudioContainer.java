package org.iviPro.transcoding.format;

import org.iviPro.transcoding.exception.TranscodingException;
import org.iviPro.transcoding.exception.TranscodingReason;
import org.iviPro.transcoding.exception.TranscodingReasonDescriptor;

public enum AudioContainer implements Container {
	ASF("ASF (Advanced Systems Format)", "asf", "wma",
			new AudioCodec[] { AudioCodec.WMA }), MKA("MKA (Matroska)",
			"matroska", "mka", AudioCodec.values()), MP3(
			"MP3 (MPEG Audio Layer III)", "mp3", "mp3", new AudioCodec[] {
					AudioCodec.MP3, AudioCodec.AAC }), OGG("OGG", "ogg", "ogg",
			new AudioCodec[] { AudioCodec.VORBIS });

	private final String realName;
	private final String transcoderParameter;
	private final String fileExtension;
	private final AudioCodec[] supportedAudioCodecs;

	private AudioContainer(String realName, String transcoderParameter,
			String fileExtension, AudioCodec[] supportedAudioCodecs) {
		this.realName = realName;
		this.transcoderParameter = transcoderParameter;
		this.fileExtension = fileExtension;
		this.supportedAudioCodecs = supportedAudioCodecs;
	}

	public String getRealName() {
		return realName;
	}

	public String getTranscoderParameter() {
		return transcoderParameter;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public AudioCodec[] getSupportedAudioCodecs() {
		return supportedAudioCodecs;
	}

	@Override
	public String toString() {
		return realName;
	}

	public static AudioContainer fromTranscoderParameter(
			String transcoderParameter) throws TranscodingException {
		if (transcoderParameter.equals(ASF.transcoderParameter)) {
			return ASF;
		} else if (transcoderParameter.equals(MKA.transcoderParameter)) {
			return MKA;
		} else if (transcoderParameter.equals(MP3.transcoderParameter)) {
			return MP3;
		} else if (transcoderParameter.equals(OGG.transcoderParameter)) {
			return OGG;
		} else {
			throw new TranscodingException(new TranscodingReason(
					TranscodingReasonDescriptor.VIDEO_CONTAINER_NOT_FOUND,
					transcoderParameter));
		}
	}
}
