package org.iviPro.transcoder;

import org.iviPro.transcoding.format.FrameRate;

public class FrameSettings {

	private final VideoDimension videoDimension;
	private final FrameRate frameRate;

	public FrameSettings() {
		this.videoDimension = new VideoDimension();
		this.frameRate = FrameRate.ORIGINAL;
	}

	public FrameSettings(VideoDimension videoDimension, FrameRate frameRate) {
		this.videoDimension = videoDimension;
		this.frameRate = frameRate;
	}

	public VideoDimension getVideoDimension() {
		return videoDimension;
	}

	public FrameRate getFrameRate() {
		return frameRate;
	}

	@Override
	public String toString() {
		return "FrameSettings [videoDimension=" + videoDimension
				+ ", frameRate=" + frameRate + "]";
	}
}
