package org.iviPro.transcoder;

import org.iviPro.transcoding.format.AudioBitRate;
import org.iviPro.transcoding.format.Channels;
import org.iviPro.transcoding.format.SampleRate;
import org.iviPro.transcoding.format.SampleSize;

public class AudioQuality {

	private final AudioBitRate bitRate;
	private final SampleRate sampleRate;
	private final SampleSize sampleSize;
	private final Channels channels;

	public AudioQuality() {
		this.bitRate = AudioBitRate.KBIT_128;
		this.sampleRate = SampleRate.HZ_44100;
		this.sampleSize = SampleSize.BIT_16;
		this.channels = Channels.STEREO;
	}

	public AudioQuality(AudioBitRate audioBitRate, SampleRate sampleRate,
			SampleSize sampleSize, Channels channels) {
		this.bitRate = audioBitRate;
		this.sampleRate = sampleRate;
		this.sampleSize = sampleSize;
		this.channels = channels;
	}

	public AudioBitRate getBitRate() {
		return bitRate;
	}

	public SampleRate getSampleRate() {
		return sampleRate;
	}

	public SampleSize getSampleSize() {
		return sampleSize;
	}

	public Channels getChannels() {
		return channels;
	}

	@Override
	public String toString() {
		return "AudioQuality [bitRate=" + bitRate + ", sampleRate="
				+ sampleRate + ", sampleSize=" + sampleSize + ", channels="
				+ channels + "]";
	}

}
