package org.iviPro.newExport.profile;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import org.iviPro.transcoding.format.AudioBitRate;
import org.iviPro.transcoding.format.AudioCodec;
import org.iviPro.transcoding.format.Channels;
import org.iviPro.transcoding.format.FrameRate;
import org.iviPro.transcoding.format.SampleRate;
import org.iviPro.transcoding.format.SampleSize;
import org.iviPro.transcoding.format.VideoCodec;
import org.iviPro.transcoding.format.VideoCodecQuality;
import org.iviPro.transcoding.format.VideoContainer;

public class VideoProfile implements Comparable<VideoProfile> {

	private VideoContainer videoContainer;
	private VideoCodec videoCodec;
	private VideoCodecQuality videoCodecQuality;
	private VideoBitRateType videoBitRateType;
	private int videoBitRate;
	private FrameRate frameRate;
	private AudioCodec audioCodec;
	private AudioBitRate audioBitRate;
	private SampleRate sampleRate;
	private SampleSize sampleSize;
	private Channels channels;
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);

	public VideoProfile(VideoContainer videoContainer, 
			VideoCodec videoCodec, VideoCodecQuality videoCodecQuality, 
			VideoBitRateType videoBitRateType, int videoBitRate,
			FrameRate frameRate, AudioCodec audioCodec,
			AudioBitRate audioBitRate, SampleRate sampleRate,
			SampleSize sampleSize, Channels channels) {
		this.videoContainer = videoContainer;
		this.videoCodec = videoCodec;
		this.videoCodecQuality = videoCodecQuality;
		this.audioCodec = audioCodec;
		this.videoBitRateType = videoBitRateType;
		this.videoBitRate = videoBitRate;
		this.frameRate = frameRate;
		this.audioBitRate = audioBitRate;
		this.sampleRate = sampleRate;
		this.sampleSize = sampleSize;
		this.channels = channels;
	}

	public VideoProfile(VideoProfile videoProfile) {
		this.videoContainer = videoProfile.videoContainer;
		this.videoCodec = videoProfile.videoCodec;
		this.videoCodecQuality = videoProfile.videoCodecQuality;
		this.audioCodec = videoProfile.audioCodec;
		this.videoBitRateType = videoProfile.videoBitRateType;
		this.videoBitRate = videoProfile.videoBitRate;
		this.frameRate = videoProfile.frameRate;
		this.audioBitRate = videoProfile.audioBitRate;
		this.sampleRate = videoProfile.sampleRate;
		this.sampleSize = videoProfile.sampleSize;
		this.channels = videoProfile.channels;
	}

	public static VideoProfile getDefault() {
		return new VideoProfile(VideoContainer.MP4, VideoCodec.H264,
				VideoCodecQuality.HIGH,	VideoBitRateType.ORIGINAL,
				512, FrameRate.ORIGINAL, AudioCodec.AAC,
				AudioBitRate.ORIGINAL, SampleRate.ORIGINAL,
				SampleSize.ORIGINAL, Channels.ORIGINAL);
	}

	public static VideoProfile getDefault(
			List<VideoContainer> availableContainers) {
		VideoProfile videoProfile = VideoProfile.getDefault();
		videoProfile.setVideoContainer(availableContainers.get(0));
		VideoCodec codec = availableContainers.get(0)
				.getSupportedVideoCodecs()[0];
		videoProfile.setVideoCodec(codec);
		if (codec.getSupportedQualityProfiles() != null) {
			videoProfile.setVideoCodecQuality(availableContainers.get(0)
					.getSupportedVideoCodecs()[0].getSupportedQualityProfiles()[0]);
		}
		videoProfile.setAudioCodec(availableContainers.get(0)
				.getSupportedAudioCodecs()[0]);
		return videoProfile;
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public VideoContainer getVideoContainer() {
		return videoContainer;
	}

	public void setVideoContainer(VideoContainer videoContainer) {
		propertyChangeSupport.firePropertyChange("videoContainer", //$NON-NLS-1$
				this.videoContainer, this.videoContainer = videoContainer);
	}

	public VideoCodec getVideoCodec() {
		return videoCodec;
	}

	public void setVideoCodec(VideoCodec videoCodec) {
		propertyChangeSupport.firePropertyChange("videoCodec", this.videoCodec, //$NON-NLS-1$
				this.videoCodec = videoCodec);
	}
	
	public VideoCodecQuality getVideoCodecQuality() {
		return videoCodecQuality;
	}

	public void setVideoCodecQuality(VideoCodecQuality newQuality) {
		propertyChangeSupport.firePropertyChange("videoCodecQuality", this.videoCodecQuality, //$NON-NLS-1$
				this.videoCodecQuality = newQuality);
	}
	public VideoBitRateType getVideoBitRateType() {
		return videoBitRateType;
	}

	public void setVideoBitRateType(VideoBitRateType videoBitRateType) {
		propertyChangeSupport
				.firePropertyChange(
						"videoBitRateType", //$NON-NLS-1$
						this.videoBitRateType,
						this.videoBitRateType = videoBitRateType);
	}

	public int getVideoBitRate() {
		return videoBitRate;
	}

	public void setVideoBitRate(int videoBitRate) {
		propertyChangeSupport.firePropertyChange("videoBitRate", //$NON-NLS-1$
				this.videoBitRate, this.videoBitRate = videoBitRate);
	}

	public FrameRate getFrameRate() {
		return frameRate;
	}

	public void setFrameRate(FrameRate frameRate) {
		propertyChangeSupport.firePropertyChange("frameRate", this.frameRate, //$NON-NLS-1$
				this.frameRate = frameRate);
	}

	public AudioCodec getAudioCodec() {
		return audioCodec;
	}

	public void setAudioCodec(AudioCodec audioCodec) {
		propertyChangeSupport.firePropertyChange("audioCodec", this.audioCodec, //$NON-NLS-1$
				this.audioCodec = audioCodec);
	}

	public AudioBitRate getAudioBitRate() {
		return audioBitRate;
	}

	public void setAudioBitRate(AudioBitRate audioBitRate) {
		propertyChangeSupport.firePropertyChange("audioBitRate", //$NON-NLS-1$
				this.audioBitRate, this.audioBitRate = audioBitRate);
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
		return "VideoProfile [videoContainer=" + videoContainer //$NON-NLS-1$
				+ ", videoCodec=" + videoCodec //$NON-NLS-1$
				+ ", videoCodecQuality=" + videoCodecQuality //$NON-NLS-1$
				+ ", videoBitRate=" //$NON-NLS-1$
				+ videoBitRate + ", frameRate=" + frameRate //$NON-NLS-1$ //$NON-NLS-2$
				+ ", audioCodec=" + audioCodec //$NON-NLS-1$
				+ ", audioBitRate=" + audioBitRate + ", sampleRate=" //$NON-NLS-1$ //$NON-NLS-2$
				+ sampleRate + ", sampleSize=" + sampleSize + ", channels=" //$NON-NLS-1$ //$NON-NLS-2$
				+ channels + "]"; //$NON-NLS-1$
	}

	@Override
	public int compareTo(VideoProfile videoProfile) {
		return videoContainer.toString().compareTo(
				videoProfile.getVideoContainer().toString());
	}
}
