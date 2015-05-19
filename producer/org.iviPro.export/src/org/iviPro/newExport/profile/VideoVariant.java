package org.iviPro.newExport.profile;

import java.util.ArrayList;
import java.util.List;

public class VideoVariant extends MediaVariant {

	private List<VideoProfile> videoProfiles;

	public VideoVariant() {
		super();
	}

	public VideoVariant(String title, String description,
			List<VideoProfile> videoProfiles) {
		super(title, description);
		this.videoProfiles = videoProfiles;
	}

	public VideoVariant(VideoVariant videoVariant) {
		this.title = videoVariant.title;
		this.description = videoVariant.description;
		this.videoProfiles = new ArrayList<VideoProfile>();
		for (VideoProfile videoProfile : videoVariant.videoProfiles) {
			this.videoProfiles.add(new VideoProfile(videoProfile));
		}
	}

	public static VideoVariant getDefault() {
		VideoProfile videoProfile = VideoProfile.getDefault();
		List<VideoProfile> videoProfiles = new ArrayList<VideoProfile>();
		videoProfiles.add(videoProfile);
		return new VideoVariant(VARIANT_DEFAULT_TITLE,
				VIDEO_VARIANT_DEFAULT_DESCRIPTION, videoProfiles);
	}

	public List<VideoProfile> getVideoProfiles() {
		return videoProfiles;
	}

	public void setVideoProfiles(List<VideoProfile> videoProfiles) {
		propertyChangeSupport.firePropertyChange("videoProfiles", //$NON-NLS-1$
				this.videoProfiles, this.videoProfiles = videoProfiles);
	}

	@Override
	public String toString() {
		return "VideoVariant [title=" + title + ", description=" + description
				+ ", videoProfiles=" + videoProfiles + "]";
	}
}
