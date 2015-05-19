package org.iviPro.transcoder;

import java.util.Arrays;

import org.iviPro.transcoding.exception.TranscodingException;
import org.iviPro.transcoding.exception.TranscodingReason;
import org.iviPro.transcoding.exception.TranscodingReasonDescriptor;
import org.iviPro.transcoding.format.AudioCodec;
import org.iviPro.transcoding.format.VideoCodec;
import org.iviPro.transcoding.format.VideoCodecQuality;
import org.iviPro.transcoding.format.VideoContainer;

public class VideoFormat {

	private final VideoContainer videoContainer;
	private final VideoCodec videoCodec;
	private final VideoCodecQuality videoCodecQuality;
	private final AudioCodec audioCodec;

	public VideoFormat(VideoContainer videoContainer, VideoCodec videoCodec,
			VideoCodecQuality videoCodecQuality,
			AudioCodec audioCodec) throws TranscodingException {
		if (videoContainer == null || videoCodec == null || audioCodec == null) {
			throw new NullPointerException("Audio container, audio codec "
					+ "and video codec must not be null!");
		}
		this.videoContainer = videoContainer;
		this.videoCodec = videoCodec;
		this.videoCodecQuality = videoCodecQuality;
		this.audioCodec = audioCodec;
		if (!Arrays.asList(videoContainer.getSupportedVideoCodecs()).contains(
				videoCodec)) {
			throw new TranscodingException(
					new TranscodingReason(
							TranscodingReasonDescriptor.CONTAINER_DOES_NOT_SUPPORT_CODEC,
							new String[] { videoContainer.toString(),
									videoCodec.toString() }));
		}
		if (!Arrays.asList(videoContainer.getSupportedAudioCodecs()).contains(
				audioCodec)) {
			throw new TranscodingException(
					new TranscodingReason(
							TranscodingReasonDescriptor.CONTAINER_DOES_NOT_SUPPORT_CODEC,
							new String[] { videoContainer.toString(),
									audioCodec.toString() }));
		}
	}

	public VideoContainer getVideoContainer() {
		return videoContainer;
	}

	public VideoCodec getVideoCodec() {
		return videoCodec;
	}
	
	public VideoCodecQuality getVideoCodecQuality() {
		return videoCodecQuality;
	}

	public AudioCodec getAudioCodec() {
		return audioCodec;
	}

	@Override
	public String toString() {
		return "VideoFormat [videoContainer=" + videoContainer
				+ ", videoCodec=" + videoCodec 
				+ ", videoCodecQuality=" + videoCodecQuality 
				+ ", audioCodec=" + audioCodec
				+ "]";
	}

}
