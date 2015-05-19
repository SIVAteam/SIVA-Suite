package org.iviPro.transcoding.format;

public enum AudioBitRate {
	ORIGINAL("Original", -1), KBIT_16(" 16 kbps", 16), KBIT_32(" 32 kbps", 32), KBIT_48(
			" 48 kbps", 48), KBIT_64(" 64 kbps", 64), KBIT_80(" 80 kbps", 80), KBIT_96(
			" 96 kbps", 96), KBIT_112("112 kbps", 112), KBIT_128("128 kbps",
			128), KBIT_160("160 kbps", 160), KBIT_192("192 kbps", 192), KBIT_224(
			"224 kbps", 224), KBIT_256("256 kbps", 256), KBIT_288("288 kbps",
			288), KBIT_320("320 kbps", 320);

	private final String label;
	private final int bitRate;

	private AudioBitRate(String label, int bitRate) {
		this.label = label;
		this.bitRate = bitRate;
	}

	public String getLabel() {
		return label;
	}

	public int getBitRate() {
		return bitRate;
	}

	@Override
	public String toString() {
		return label;
	}

	public static AudioBitRate fromInteger(int bitRate) {
		if (bitRate < 0) {
			return ORIGINAL;
		} else if (bitRate <= 16) {
			return KBIT_16;
		} else if (bitRate <= 32) {
			return KBIT_32;
		} else if (bitRate <= 48) {
			return KBIT_48;
		} else if (bitRate <= 64) {
			return KBIT_64;
		} else if (bitRate <= 80) {
			return KBIT_80;
		} else if (bitRate <= 96) {
			return KBIT_96;
		} else if (bitRate <= 112) {
			return KBIT_112;
		} else if (bitRate <= 128) {
			return KBIT_128;
		} else if (bitRate <= 160) {
			return KBIT_160;
		} else if (bitRate <= 192) {
			return KBIT_192;
		} else if (bitRate <= 224) {
			return KBIT_224;
		} else if (bitRate <= 256) {
			return KBIT_256;
		} else if (bitRate <= 288) {
			return KBIT_288;
		} else {
			return KBIT_320;
		}
	}
}
