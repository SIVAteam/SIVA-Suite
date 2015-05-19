package org.iviPro.transcoding.format;

public enum SampleRate {
	ORIGINAL("Original", -1), HZ_8000(" 8000 Hz", 8000), HZ_11025("11025 Hz",
			11025), HZ_16000("16000 Hz", 16000), HZ_22050("22050 Hz", 22050), HZ_24000(
			"24000 Hz", 24000), HZ_32000("32000 Hz", 32000), HZ_44100(
			"44100 Hz", 44100), HZ_48000("48000 Hz", 48000), HZ_88200(
			"88200 Hz", 88200), HZ_96000("96000 Hz", 96000);

	private final String label;
	private final int sampleRate;

	private SampleRate(String label, int sampleRate) {
		this.label = label;
		this.sampleRate = sampleRate;
	}

	public String getLabel() {
		return label;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	@Override
	public String toString() {
		return label;
	}

	public static SampleRate fromInteger(int sampleRate) {
		if (sampleRate < 0) {
			return ORIGINAL;
		} else if (sampleRate <= 8000) {
			return HZ_8000;
		} else if (sampleRate <= 11025) {
			return HZ_11025;
		} else if (sampleRate <= 16000) {
			return HZ_16000;
		} else if (sampleRate <= 22050) {
			return HZ_22050;
		} else if (sampleRate <= 24000) {
			return HZ_24000;
		} else if (sampleRate <= 32000) {
			return HZ_32000;
		} else if (sampleRate <= 44100) {
			return HZ_44100;
		} else if (sampleRate <= 48000) {
			return HZ_48000;
		} else if (sampleRate <= 88200) {
			return HZ_88200;
		} else {
			return HZ_96000;
		}
	}

}