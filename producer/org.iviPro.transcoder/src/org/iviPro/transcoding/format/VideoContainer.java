package org.iviPro.transcoding.format;

import org.iviPro.transcoding.exception.TranscodingException;
import org.iviPro.transcoding.exception.TranscodingReason;
import org.iviPro.transcoding.exception.TranscodingReasonDescriptor;

public enum VideoContainer implements Container {

	THIRDGP("3GP (Third Generation Partnership)", "3gp", "3gp",
			new AudioCodec[] { AudioCodec.AAC }, 
			new VideoCodec[] { VideoCodec.H263, VideoCodec.H264 }), 
	ASF("ASF (Advanced Straeaming Format)", "asf", "wmv",
			new AudioCodec[] { AudioCodec.WMA },
			new VideoCodec[] { VideoCodec.WMV }), 
	AVI("AVI (Audio Video Interleave)", "avi", "avi", 
			new AudioCodec[] { AudioCodec.AAC, AudioCodec.MP3, AudioCodec.WMA },
			new VideoCodec[] { VideoCodec.H262, VideoCodec.H263,
					VideoCodec.H264, VideoCodec.MJPEG, VideoCodec.MPEG1,
					VideoCodec.WMV }), 
	FLV("FLV (Flash Video)", "flv", "flv",
			new AudioCodec[] { AudioCodec.MP3 },
			new VideoCodec[] { VideoCodec.FLV }), 
	MKV("MKV (Matroska)", "matroska", "mkv", 
			AudioCodec.values(), 
			VideoCodec.values()), 
	MP4("MP4 (MPEG-4 Part 14)", "mp4", "mp4", 
			new AudioCodec[] { AudioCodec.AAC, AudioCodec.MP3 }, 
			new VideoCodec[] { VideoCodec.H262, VideoCodec.H263, VideoCodec.H264 }), 
	MPG("MPEG (Moving Picture Expert Group)", "mpeg", "mpg",
			new AudioCodec[] { AudioCodec.MP3 }, 
			new VideoCodec[] { VideoCodec.MJPEG, VideoCodec.MPEG1, VideoCodec.H262 }), 
	OGG("OGG", "ogg", "ogg", 
			new AudioCodec[] { AudioCodec.VORBIS },
			new VideoCodec[] { VideoCodec.THEORA }), 
	WEBM("WebM", "webm","webm", 
			new AudioCodec[] { AudioCodec.VORBIS },
			new VideoCodec[] { VideoCodec.VP8 });

	private final String realName;
	private final String transcoderParameter;
	private final String fileExtension;
	private final AudioCodec[] supportedAudioCodecs;
	private final VideoCodec[] supportedVideoCodecs;

	private VideoContainer(String realName, String transcoderParameter,
			String fileExtension, AudioCodec[] supportedAudioCodecs,
			VideoCodec[] supportedVideoCodecs) {
		this.realName = realName;
		this.transcoderParameter = transcoderParameter;
		this.fileExtension = fileExtension;
		this.supportedAudioCodecs = supportedAudioCodecs;
		this.supportedVideoCodecs = supportedVideoCodecs;
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

	public VideoCodec[] getSupportedVideoCodecs() {
		return supportedVideoCodecs;
	}

	@Override
	public String toString() {
		return realName;
	}

	public static VideoContainer fromTranscoderParameter(
			String transcoderParameter) throws TranscodingException {
		if (transcoderParameter.equals(THIRDGP.transcoderParameter)) {
			return THIRDGP;
		} else if (transcoderParameter.equals(ASF.transcoderParameter)) {
			return ASF;
		} else if (transcoderParameter.equals(AVI.transcoderParameter)) {
			return AVI;
		} else if (transcoderParameter.equals(FLV.transcoderParameter)) {
			return FLV;
		} else if (transcoderParameter.equals(MKV.transcoderParameter)) {
			return MKV;
		} else if (transcoderParameter.equals(MP4.transcoderParameter)) {
			return MP4;
		} else if (transcoderParameter.equals(MPG.transcoderParameter)) {
			return MPG;
		} else if (transcoderParameter.equals(OGG.transcoderParameter)) {
			return OGG;
		} else if (transcoderParameter.equals(WEBM.transcoderParameter)) {
			return WEBM;
		} else {
			throw new TranscodingException(new TranscodingReason(
					TranscodingReasonDescriptor.VIDEO_CONTAINER_NOT_FOUND,
					transcoderParameter));
		}
	}

}
