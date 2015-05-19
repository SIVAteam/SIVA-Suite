package org.iviPro.newExport.profile;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import org.iviPro.transcoding.format.AudioBitRate;
import org.iviPro.transcoding.format.AudioCodec;
import org.iviPro.transcoding.format.AudioContainer;
import org.iviPro.transcoding.format.Channels;
import org.iviPro.transcoding.format.SampleRate;
import org.iviPro.transcoding.format.SampleSize;

public class AudioProfile implements Comparable<AudioProfile> {

	private AudioContainer audioContainer;
	private AudioCodec audioCodec;
	private AudioBitRate bitRate;
	private SampleRate sampleRate;
	private SampleSize sampleSize;
	private Channels channels;
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);

	public AudioProfile() {

	}

	public AudioProfile(AudioContainer audioContainer, AudioCodec audioCodec,
			AudioBitRate bitRate, SampleRate sampleRate, SampleSize sampleSize,
			Channels channels) {
		this.audioContainer = audioContainer;
		this.audioCodec = audioCodec;
		this.bitRate = bitRate;
		this.sampleRate = sampleRate;
		this.sampleSize = sampleSize;
		this.channels = channels;
	}

	public AudioProfile(AudioProfile audioProfile) {
		this.audioContainer = audioProfile.audioContainer;
		this.audioCodec = audioProfile.audioCodec;
		this.bitRate = audioProfile.bitRate;
		this.sampleRate = audioProfile.sampleRate;
		this.sampleSize = audioProfile.sampleSize;
		this.channels = audioProfile.channels;
	}

	public static AudioProfile getDefault() {
		return new AudioProfile(AudioContainer.MP3, AudioCodec.MP3,
				AudioBitRate.KBIT_128, SampleRate.HZ_48000, SampleSize.BIT_16,
				Channels.STEREO);
	}

	public static AudioProfile getDefault(
			List<AudioContainer> availableContainers) {
		AudioProfile audioProfile = AudioProfile.getDefault();
		audioProfile.setAudioContainer(availableContainers.get(0));
		audioProfile.setAudioCodec(availableContainers.get(0)
				.getSupportedAudioCodecs()[0]);
		return audioProfile;
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public AudioContainer getAudioContainer() {
		return audioContainer;
	}

	public void setAudioContainer(AudioContainer audioContainer) {
		propertyChangeSupport.firePropertyChange("audioContainer", //$NON-NLS-1$
				this.audioContainer, this.audioContainer = audioContainer);
	}

	public AudioCodec getAudioCodec() {
		return audioCodec;
	}

	public void setAudioCodec(AudioCodec audioCodec) {
		propertyChangeSupport.firePropertyChange("audioCodec", this.audioCodec, //$NON-NLS-1$
				this.audioCodec = audioCodec);
	}

	public AudioBitRate getBitRate() {
		return bitRate;
	}

	public void setBitRate(AudioBitRate bitRate) {
		propertyChangeSupport.firePropertyChange("bitRate", this.bitRate, //$NON-NLS-1$
				this.bitRate = bitRate);
	}

	public SampleRate getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(SampleRate sampleRate) {
		propertyChangeSupport.firePropertyChange("sampleRate", this.sampleRate, //$NON-NLS-1$
				this.sampleRate = sampleRate);
	}

	public SampleSize getSampleSize() {
		return sampleSize;
	}

	public void setSampleSize(SampleSize sampleSize) {
		propertyChangeSupport.firePropertyChange("sampleSize", this.sampleSize, //$NON-NLS-1$
				this.sampleSize = sampleSize);
	}

	public Channels getChannels() {
		return channels;
	}

	public void setChannels(Channels channels) {
		propertyChangeSupport.firePropertyChange("channels", this.channels, //$NON-NLS-1$
				this.channels = channels);
	}

	@Override
	public String toString() {
		return "AudioProfile [audioContainer=" + audioContainer //$NON-NLS-1$
				+ ", audioCodec=" + audioCodec + ", bitRate=" + bitRate //$NON-NLS-1$ //$NON-NLS-2$
				+ ", sampleRate=" + sampleRate + ", sampleSize=" + sampleSize //$NON-NLS-1$ //$NON-NLS-2$
				+ ", channels=" + channels + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public int compareTo(AudioProfile audioProfile) {
		return audioContainer.toString().compareTo(
				audioProfile.getAudioContainer().toString());
	}
}
