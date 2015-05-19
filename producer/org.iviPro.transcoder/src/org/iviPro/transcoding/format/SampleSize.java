package org.iviPro.transcoding.format;

public enum SampleSize {

	ORIGINAL("Original", -1), BIT_8(" 8 bit", 8), BIT_16("16 bit", 16), BIT_24(
			"24 bit", 24), BIT_32("32 bit", 32);

	private final String label;
	private final int sampleSize;

	private SampleSize(String label, int sampleSize) {
		this.label = label;
		this.sampleSize = sampleSize;
	}

	public String getLabel() {
		return label;
	}

	public int getSampleSize() {
		return sampleSize;
	}

	@Override
	public String toString() {
		return label;
	}

	public static SampleSize fromInteger(int sampleSize) {
		if (sampleSize < 0) {
			return ORIGINAL;
		} else if (sampleSize <= 8) {
			return BIT_8;
		} else if (sampleSize <= 16) {
			return BIT_16;
		} else if (sampleSize <= 24) {
			return BIT_24;
		} else {
			return BIT_32;
		}
	}
}
