package org.iviPro.transcoding.format;

import org.iviPro.transcoding.exception.TranscodingException;
import org.iviPro.transcoding.exception.TranscodingReason;
import org.iviPro.transcoding.exception.TranscodingReasonDescriptor;

public enum Channels {

	ORIGINAL("Original", -1), MONO("Mono", 1), STEREO("Stereo", 2);

	private final String label;
	private final int channels;

	private Channels(String label, int channels) {
		this.label = label;
		this.channels = channels;
	}

	public String getLabel() {
		return label;
	}

	public int getChannels() {
		return channels;
	}

	@Override
	public String toString() {
		return label;
	}

	public static Channels fromInteger(int channels)
			throws TranscodingException {
		if (channels < 0) {
			return ORIGINAL;
		} else if (channels == 1) {
			return MONO;
		} else if (channels == 2) {
			return STEREO;
		} else {
			throw new TranscodingException(new TranscodingReason(
					TranscodingReasonDescriptor.CHANNELS_NOT_SUPPORTED,
					String.valueOf(channels)));
		}
	}
}
