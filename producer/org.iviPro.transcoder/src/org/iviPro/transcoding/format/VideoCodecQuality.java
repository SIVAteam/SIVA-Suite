package org.iviPro.transcoding.format;

import org.iviPro.transcoding.exception.TranscodingException;
import org.iviPro.transcoding.exception.TranscodingReason;
import org.iviPro.transcoding.exception.TranscodingReasonDescriptor;

public enum VideoCodecQuality {
	HIGH("high"),
	BASELINE("baseline");
	
	private final String transcoderParameter;
	
	private VideoCodecQuality(String transcoderParameter) {
		this.transcoderParameter = transcoderParameter;
	}
	
	public static VideoCodecQuality fromTranscoderParameter(String transcoderParameter)
			throws TranscodingException {
		
		if (transcoderParameter.equals(BASELINE.transcoderParameter)) {
			return BASELINE;
		} else if (transcoderParameter.equals(HIGH.transcoderParameter)) {
			return HIGH;
		} else {
			throw new TranscodingException(new TranscodingReason(
				TranscodingReasonDescriptor.VIDEO_CODEC_NOT_FOUND,
				transcoderParameter));
		}
	}
	
	public String getTranscoderParameter() {
		return transcoderParameter;
	}
	
	@Override
	public String toString() {
		return transcoderParameter;
	}
}
