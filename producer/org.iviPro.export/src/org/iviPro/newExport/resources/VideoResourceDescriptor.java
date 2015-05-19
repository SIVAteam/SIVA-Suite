package org.iviPro.newExport.resources;

import java.io.File;

import org.iviPro.model.resources.Video;

public class VideoResourceDescriptor extends TimedResourceDescriptor {
	
	private Video video;

	public VideoResourceDescriptor(File source, String target, long startTime,
			long endTime, Video video) {
		super(source, target, startTime, endTime);
		this.video = video;
	}
	
	/**
	 * Returns the video resource associated with this descriptor.
	 * @return video resource
	 */
	public Video getVideo() {
		return video;
	}	
}
