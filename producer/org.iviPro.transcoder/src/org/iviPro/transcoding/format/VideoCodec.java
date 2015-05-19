package org.iviPro.transcoding.format;

import org.iviPro.transcoding.exception.TranscodingException;
import org.iviPro.transcoding.exception.TranscodingReason;
import org.iviPro.transcoding.exception.TranscodingReasonDescriptor;

public enum VideoCodec {

	FLV("FLV (Flash Video)", "flv", "flv", null), 
	H262("H.262/MPEG-2", "mpg", "mpeg2video", null), 
	H263("H.263", "3gp", "libavcodec", null), 
	H264("H.264/MPEG-4 AVC", "avc", "libx264", 
			new VideoCodecQuality[] { VideoCodecQuality.HIGH,
										VideoCodecQuality.BASELINE}),
	MJPEG("MJPEG (Motion JPEG)","mjpeg", "mjpeg", null), 
	MPEG1("MPEG-1", "mpg", "mpeg1video", null), 
	THEORA("Theora", "theora", "libtheora", null), 
	VP8("VP8", "vp8", "libvpx", null),
	WMV("WMV (Windows Media Video)", "wmv", "wmv2", null);

	private final String realName;
	private final String label;
	private final String transcoderParameter;
	private final VideoCodecQuality[] supportedQualityProfiles;

	private VideoCodec(String realName, String label, String transcoderParameter,
			VideoCodecQuality[] supportedQualityProfiles) {
		this.realName = realName;
		this.label = label;
		this.transcoderParameter = transcoderParameter;
		this.supportedQualityProfiles = supportedQualityProfiles;
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
	
	public VideoCodecQuality[] getSupportedQualityProfiles() {
		return supportedQualityProfiles;
	}

	public static VideoCodec fromTranscoderParameter(String transcoderParameter)
			throws TranscodingException {
		if (transcoderParameter.equals(FLV.transcoderParameter)) {
			return FLV;
		} else if (transcoderParameter.equals(H262.transcoderParameter)) {
			return H262;
		} else if (transcoderParameter.equals(H263.transcoderParameter)) {
			return H263;
		} else if (transcoderParameter.equals(H264.transcoderParameter)) {
			return H264;
		} else if (transcoderParameter.equals(MJPEG.transcoderParameter)) {
			return MJPEG;
		} else if (transcoderParameter.equals(MPEG1.transcoderParameter)) {
			return MPEG1;
		} else if (transcoderParameter.equals(THEORA.transcoderParameter)) {
			return THEORA;
		} else if (transcoderParameter.equals(VP8.transcoderParameter)) {
			return VP8;
		} else if (transcoderParameter.equals(WMV.transcoderParameter)) {
			return WMV;
		} else {
			throw new TranscodingException(new TranscodingReason(
					TranscodingReasonDescriptor.VIDEO_CODEC_NOT_FOUND,
					transcoderParameter));
		}
	}
}
