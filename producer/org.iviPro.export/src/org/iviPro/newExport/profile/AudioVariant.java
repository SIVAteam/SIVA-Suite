package org.iviPro.newExport.profile;

import java.util.ArrayList;
import java.util.List;

public class AudioVariant extends MediaVariant {

	private List<AudioProfile> audioProfiles;

	public AudioVariant() {
		super();
	}

	public AudioVariant(String title, String description,
			List<AudioProfile> audioProfiles) {
		super(title, description);
		this.audioProfiles = audioProfiles;
	}

	public AudioVariant(AudioVariant audioVariant) {
		this.title = audioVariant.title;
		this.description = audioVariant.description;
		this.audioProfiles = new ArrayList<AudioProfile>();
		for (AudioProfile audioProfile : audioVariant.audioProfiles) {
			this.audioProfiles.add(new AudioProfile(audioProfile));
		}
	}

	public static AudioVariant getDefault() {
		AudioProfile audioProfile = AudioProfile.getDefault();
		List<AudioProfile> audioProfiles = new ArrayList<AudioProfile>();
		audioProfiles.add(audioProfile);
		return new AudioVariant(VARIANT_DEFAULT_TITLE,
				AUDIO_VARIANT_DEFAULT_DESCRIPTION, audioProfiles);
	}

	public List<AudioProfile> getAudioProfiles() {
		return audioProfiles;
	}

	public void setAudioProfiles(List<AudioProfile> audioProfiles) {
		propertyChangeSupport.firePropertyChange("audioProfiles", //$NON-NLS-1$
				this.audioProfiles, this.audioProfiles = audioProfiles);
	}

	@Override
	public String toString() {
		return "AudioVariant [title=" + title + ", description=" + description
				+ ", audioProfiles=" + audioProfiles + "]";
	}
}
