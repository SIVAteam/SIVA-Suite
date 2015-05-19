package org.iviPro.transcoder;

import java.awt.Dimension;

import org.iviPro.transcoding.exception.TranscodingException;
import org.iviPro.transcoding.format.VideoQuality;

public class VideoDescriptor extends TranscodingDescriptor {

	private final Dimension originalResolution;
	private final VideoFormat videoFormat;
	private final VideoQuality videoQuality;
	private final AudioQuality audioQuality;

	public VideoDescriptor(Dimension originalResolution, VideoFormat videoFormat, VideoQuality videoQuality,
			AudioQuality audioQuality, TranscodingPath transcodingPath,
			boolean overwrite, MediaSection mediaSection)
			throws TranscodingException {
		super(transcodingPath, overwrite, mediaSection, videoFormat
				.getVideoContainer().getFileExtension());
		this.originalResolution = originalResolution;
		this.videoFormat = videoFormat;
		this.videoQuality = videoQuality;
		this.audioQuality = audioQuality;
	}

	/**
	 * Returns the original resolution of the video which should be transcoded
	 * using this descriptor.
	 * @return original resolution
	 */
	public Dimension getOriginalResolution() {
		return originalResolution;
	}
	
	public VideoFormat getVideoFormat() {
		return videoFormat;
	}

	public VideoQuality getVideoQuality() {
		return videoQuality;
	}

	public AudioQuality getAudioQuality() {
		return audioQuality;
	}

	@Override
	public String toString() {
		return super.toString() + " >> VideoDescriptor [originalResolution=" 
				+ originalResolution + ", videoFormat="
				+ videoFormat + ", videoQuality=" + videoQuality
				+ ", audioQuality=" + audioQuality + "]";
	}

}
