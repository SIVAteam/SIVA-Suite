package org.iviPro.transcoder;

public class VideoDimension {
	public static final int WIDTH_ORIGINAL = -1;
	public static final int HEIGHT_ORIGINAL = -1;

	private final int width;
	private final int height;

	public VideoDimension() {
		this.width = WIDTH_ORIGINAL;
		this.height = HEIGHT_ORIGINAL;
	}

	public VideoDimension(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public static VideoDimension getDefault() {
		return new VideoDimension(WIDTH_ORIGINAL, HEIGHT_ORIGINAL);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public String toString() {
		return "VideoDimension [width=" + width + ", height=" + height + "]";
	}
}
