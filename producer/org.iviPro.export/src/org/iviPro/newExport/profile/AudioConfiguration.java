package org.iviPro.newExport.profile;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class AudioConfiguration {

	private List<AudioVariant> audioVariants;
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);

	public AudioConfiguration() {

	}

	public AudioConfiguration(List<AudioVariant> audioVariants) {
		this.audioVariants = audioVariants;
	}

	public AudioConfiguration(AudioConfiguration audioConfiguration) {
		this.audioVariants = new ArrayList<AudioVariant>();
		for (AudioVariant audioVariant : audioConfiguration.audioVariants) {
			this.audioVariants.add(new AudioVariant(audioVariant));
		}
	}

	public static AudioConfiguration getDefault() {
		List<AudioVariant> result = new ArrayList<AudioVariant>();
		result.add(AudioVariant.getDefault());
		return new AudioConfiguration(result);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public List<AudioVariant> getAudioVariants() {
		return audioVariants;
	}

	public void setAudioVariants(List<AudioVariant> audioVariants) {
		propertyChangeSupport.firePropertyChange("audioVariants", //$NON-NLS-1$
				this.audioVariants, this.audioVariants = audioVariants);
	}

	@Override
	public String toString() {
		return "AudioConfiguration [audioVariants=" + audioVariants.toString() //$NON-NLS-1$
				+ "]"; //$NON-NLS-1$
	}

}
