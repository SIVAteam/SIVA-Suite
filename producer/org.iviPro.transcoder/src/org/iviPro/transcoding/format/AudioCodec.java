package org.iviPro.transcoding.format;

import org.iviPro.transcoding.exception.TranscodingException;
import org.iviPro.transcoding.exception.TranscodingReason;
import org.iviPro.transcoding.exception.TranscodingReasonDescriptor;

public enum AudioCodec {
	AAC("AAC (Advanced Audio Coding)", "aac", "libfdk_aac"), MP3(
			"MP3 (MPEG Audio Layer III)", "mp3", "libmp3lame"), VORBIS(
			"Vorbis", "vorbis", "libvorbis"), WMA("WMA (Windows Media Audio)",
			"wma", "wmav2");

	private final String realName;
	private final String label;
	private final String transcoderParameter;

	private AudioCodec(String realName, String label, String transcoderParameter) {
		this.realName = realName;
		this.label = label;
		this.transcoderParameter = transcoderParameter;
	}

	public String getRealName() {
		return realName;
	}

	public String getLabel() {
		return label;
	}

	public String getTranscoderParameter() {
		return transcoderParameter;
	}

	@Override
	public String toString() {
		return realName;
	}

	public static AudioCodec fromTranscoderParameter(String transcoderParameter)
			throws TranscodingException {
		if (transcoderParameter.equals(AAC.transcoderParameter)) {
			return AAC;
		} else if (transcoderParameter.equals(MP3.transcoderParameter)) {
			return MP3;
		} else if (transcoderParameter.equals(VORBIS.transcoderParameter)) {
			return VORBIS;
		} else if (transcoderParameter.equals(WMA.transcoderParameter)) {
			return WMA;
		} else {
			throw new TranscodingException(new TranscodingReason(
					TranscodingReasonDescriptor.AUDIO_CODEC_NOT_FOUND,
					transcoderParameter));
		}
	}
}
