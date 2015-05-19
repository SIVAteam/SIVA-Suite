package org.iviPro.newExport.profile;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class VideoConfiguration {

	private List<VideoVariant> videoVariants;
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);

	public VideoConfiguration() {

	}

	public VideoConfiguration(List<VideoVariant> videoVariants) {
		this.videoVariants = videoVariants;
	}

	public VideoConfiguration(VideoConfiguration videoConfiguration) {
		this.videoVariants = new ArrayList<VideoVariant>();
		for (VideoVariant videoVariant : videoConfiguration.videoVariants) {
			this.videoVariants.add(new VideoVariant(videoVariant));
		}
	}

	public static VideoConfiguration getDefault() {
		List<VideoVariant> result = new ArrayList<VideoVariant>();
		result.add(VideoVariant.getDefault());
		return new VideoConfiguration(result);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public List<VideoVariant> getVideoVariants() {
		return videoVariants;
	}

	public void setVideoVariants(List<VideoVariant> videoVariants) {
		propertyChangeSupport.firePropertyChange("videoVariants", //$NON-NLS-1$
				this.videoVariants, this.videoVariants = videoVariants);
	}

	@Override
	public String toString() {
		return "VideoConfiguration [videoVariants=" + videoVariants.toString() //$NON-NLS-1$
				+ "]"; //$NON-NLS-1$
	}

}
