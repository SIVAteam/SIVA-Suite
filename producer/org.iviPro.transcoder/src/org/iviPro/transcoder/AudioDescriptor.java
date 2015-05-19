package org.iviPro.transcoder;

import org.iviPro.transcoding.exception.TranscodingException;

public class AudioDescriptor extends TranscodingDescriptor {

	private final AudioFormat audioFormat;
	private final AudioQuality audioQuality;

	public AudioDescriptor(AudioFormat audioFormat, AudioQuality audioQuality,
			TranscodingPath transcodingPath, boolean overwrite,
			MediaSection mediaSection) throws TranscodingException {
		super(transcodingPath, overwrite, mediaSection, audioFormat
				.getAudioContainer().getFileExtension());
		this.audioFormat = audioFormat;
		this.audioQuality = audioQuality;
	}

	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	public AudioQuality getAudioQuality() {
		return audioQuality;
	}

	@Override
	public String toString() {
		return super.toString() + " >> AudioDescriptor [audioFormat="
				+ audioFormat + ", audioQuality=" + audioQuality + "]";
	}

}
