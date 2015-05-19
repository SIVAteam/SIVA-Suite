package org.iviPro.transcoding.format;

public enum FrameRate {

	ORIGINAL("Original", -1), FPS_12("12 fps", 12), FPS_15("15 fps", 15), FPS_20(
			"20 fps", 20), FPS_23_976("23,976 fps", 23.976), FPS_24("24 fps",
			24), FPS_25("25 fps", 25), FPS_29_97("29,97 fps", 29.97), FPS_30(
			"30 fps", 30);

	private final String label;
	private final double frameRate;

	private FrameRate(String label, double frameRate) {
		this.label = label;
		this.frameRate = frameRate;
	}

	public String getLabel() {
		return label;
	}

	public double getFrameRate() {
		return frameRate;
	}

	@Override
	public String toString() {
		return label;
	}

	public static FrameRate fromDouble(double sampleSize) {
		if (sampleSize < 0) {
			return ORIGINAL;
		} else if (sampleSize <= 12) {
			return FPS_12;
		} else if (sampleSize <= 15) {
			return FPS_15;
		} else if (sampleSize <= 20) {
			return FPS_20;
		} else if (sampleSize == 23.976) {
			return FPS_23_976;
		} else if (sampleSize <= 24) {
			return FPS_24;
		} else if (sampleSize <= 25) {
			return FPS_25;
		} else if (sampleSize == 29.97) {
			return FPS_29_97;
		} else {
			return FPS_30;
		}
	}

}
