package org.iviPro.transcoder;

import java.util.Arrays;

import org.iviPro.transcoding.exception.TranscodingException;
import org.iviPro.transcoding.exception.TranscodingReason;
import org.iviPro.transcoding.exception.TranscodingReasonDescriptor;
import org.iviPro.transcoding.format.AudioCodec;
import org.iviPro.transcoding.format.AudioContainer;

public class AudioFormat {

	private final AudioContainer audioContainer;
	private final AudioCodec audioCodec;

	public AudioFormat(AudioContainer audioContainer, AudioCodec audioCodec)
			throws TranscodingException {
		this.audioContainer = audioContainer;
		this.audioCodec = audioCodec;
		if (audioContainer == null || audioCodec == null) {
			throw new NullPointerException(
					"Audio container and audio condex must not be null!");
		}
		if (!Arrays.asList(audioContainer.getSupportedAudioCodecs()).contains(
				audioCodec)) {
			throw new TranscodingException(
					new TranscodingReason(
							TranscodingReasonDescriptor.CONTAINER_DOES_NOT_SUPPORT_CODEC,
							new String[] { audioContainer.toString(),
									audioCodec.toString() }));
		}
	}

	public AudioContainer getAudioContainer() {
		return audioContainer;
	}

	public AudioCodec getAudioCodec() {
		return audioCodec;
	}

	@Override
	public String toString() {
		return "AudioFormat [audioContainer=" + audioContainer
				+ ", audioCodec=" + audioCodec + "]";
	}
}
