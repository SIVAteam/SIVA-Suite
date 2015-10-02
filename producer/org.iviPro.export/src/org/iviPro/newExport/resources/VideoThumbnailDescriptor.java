package org.iviPro.newExport.resources;

import java.util.Locale;

import org.iviPro.model.resources.Video;

/**
 * Descriptor of thumbnails associated with a video or scene.
 * In contrary to a {@link ResourceDescriptor}, ThumbnailDescriptors do not 
 * reference a source file since the thumbnail files are not stored within the
 * project. The thumbnail files are created on the fly during export.
 * @author John
 *
 */
public class VideoThumbnailDescriptor {
	/**
	 * Desired filename for exporting the thumbnail.
	 */
	private final String target;
	private final Video video;

	private final long time;
	
	/**
	 * Constructs a ThumbnailDescriptor with the given parameters.
	 * @param target desired name of the thumbnail file
	 * @param video video associated to the thumbnail
	 * @param time time of the frame which should be used as thumbnail
	 */
	public VideoThumbnailDescriptor(String target, Video video, long time) {
		this.target = target;
		this.video = video;
		this.time = time;
	}
	
	public String getTarget() {
		return target;
	}
	
	public Video getVideo() {
		return video;
	}
	
	public long getTime() {
		return time;
	}
	
	@Override
	public String toString() {
		return "ThumbnailDescriptor [target=" + target + ", video=" + video //$NON-NLS-1$ //$NON-NLS-2$
				+ ", time=" + time + "]"; //$NON-NLS-1$
				
	}
}
