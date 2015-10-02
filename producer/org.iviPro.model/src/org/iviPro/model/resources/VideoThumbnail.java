package org.iviPro.model.resources;

import java.util.List;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedElement;
import org.iviPro.model.Project;

public class VideoThumbnail extends IAbstractBean implements IResource {
	
	/**
	 * Scene associated with this thumbnail.
	 */
	private Scene scene;

	/**
	 * Video associated with this thumbnail.
	 */
	private Video video;
	
	/**
	 * Time in nanoseconds of the video frame referenced by this thumbnail.
	 */
	private long time;	
	
	public VideoThumbnail(Scene scene, long time, Project project) {
		super("", project);
		this.scene = scene;
		this.video = scene.getVideo();
		this.time = time;
	}
	
	public VideoThumbnail(Video video, long time, Project project) {
		super("", project);
		this.video = video;
		this.time = time;
	}
	
	/**
	 * Returns the scene this thumbnail is associated with.
	 * @return scene associated with thumnail
	 */
	public Scene getScene() {
		return scene;
	}
	
	/**
	 * Returns the video this thumbnail is associated with.
	 * @return video associated with thumnail
	 */
	public Video getVideo() {
		return video;
	}
		
	/**
	 * Returns the absolute time in nanoseconds of the video frame which is
	 * referenced by this thumbnail.
	 * @return absolute time of the thumbnail frame in nanoseconds
	 */
	public long getTime() {
		return time;
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
	@Override
	public List<LocalizedElement> getLocalizedContents() {
		return video.getLocalizedContents();
	}

	@Override
	public String getBeanTag() {
		return "Video thumbnail";
	}
}
