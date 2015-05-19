package org.iviPro.newExport.profile;

public class Profile {
	private final GeneralConfiguration general;
	private final AudioConfiguration audio;
	private final VideoConfiguration video;

	public Profile(GeneralConfiguration general, AudioConfiguration audio,
			VideoConfiguration video) {
		this.general = general;
		this.audio = audio;
		this.video = video;
	}

	public Profile(Profile profile) {
		this.general = new GeneralConfiguration(profile.general);
		this.audio = new AudioConfiguration(profile.audio);
		this.video = new VideoConfiguration(profile.video);
	}

	public static Profile getDefault() {
		return new Profile(GeneralConfiguration.getDefault(),
				AudioConfiguration.getDefault(),
				VideoConfiguration.getDefault());
	}

	/**
	 * Convenience method for accessing the profile's title directly.
	 * 
	 * @return The title of the profile.
	 */
	public String getProfileTitle() {
		return getGeneral().getTitle();
	}

	public GeneralConfiguration getGeneral() {
		return general;
	}

	public AudioConfiguration getAudio() {
		return audio;
	}

	public VideoConfiguration getVideo() {
		return video;
	}

	@Override
	public String toString() {
		return "Profile [general=" + general + ", audio=" + audio + ", video=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ video + "]"; //$NON-NLS-1$
	}
}
