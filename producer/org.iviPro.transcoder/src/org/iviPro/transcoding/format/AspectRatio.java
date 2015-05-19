package org.iviPro.transcoding.format;

public enum AspectRatio {
	XGA("4:3 (Standard television/monitor)", "4:3"), WIDE_TV(
			"16:9 (Widescreen television)", "16:9"), WXGA(
			"16:10 (Widescreen monitor)", "16:10");

	private final String label;
	private final String ratio;

	private AspectRatio(String label, String ratio) {
		this.label = label;
		this.ratio = ratio;
	}

	public String getLabel() {
		return label;
	}

	public String getRatio() {
		return ratio;
	}

	@Override
	public String toString() {
		return label;
	}
}
