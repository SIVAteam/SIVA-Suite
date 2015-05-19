package org.iviPro.transcoding.format;

import org.iviPro.transcoder.FrameSettings;

public class VideoQuality {

	public final static int BITRATE_ORIGINAL = -1;

	private final int bitRate;
	private final FrameSettings frameSettings;

	public VideoQuality() {
		this.bitRate = BITRATE_ORIGINAL;
		this.frameSettings = new FrameSettings();
	}

	public VideoQuality(int bitRate, FrameSettings frameSettings) {
		this.bitRate = bitRate;
		this.frameSettings = frameSettings;
	}

	public int getBitRate() {
		return bitRate;
	}

	public FrameSettings getFrameSettings() {
		return frameSettings;
	}

	@Override
	public String toString() {
		return "VideoQuality [bitrate=" + bitRate + ", frameSettings="
				+ frameSettings + "]";
	}

}
