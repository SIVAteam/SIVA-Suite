/**
 * 
 */
package org.iviPro.model.resources;

import java.util.ArrayList;
import java.util.List;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedElement;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;

/**
 * @author   dellwo
 * @uml.dependency   supplier="org.iviPro.model.Video"
 */
public class Scene extends IAbstractBean implements IResource, IVideoResource {

	public static final String PROP_START = "start"; //$NON-NLS-1$
	public static final String PROP_END = "end"; //$NON-NLS-1$
	public static final String PROP_THUMB = "thumb"; //$NON-NLS-1$

	/**
	 * The underlying video of the scene.
	 * @uml.property   name="video"
	 */
	private Video video;

	/**
	 * The end time of the scene in nanoseconds.
	 * @uml.property   name="end"
	 */
	private Long end;

	/**
	 * The start time of the scene in nanoseconds.
	 * @uml.property   name="start"
	 */
	private Long start;
	
	/**
	 * Thumbnail used to describe this scene.
	 */
	private VideoThumbnail thumbnail;

	/**
	 * Konstruktor fuer eine neue Szene die sich per Default ueber ein gesamtes
	 * Video erstreckt.
	 * 
	 * @param title
	 *            Titel der Szene.
	 * @param video
	 *            Video aus dem die Szene stammt.
	 */
	public Scene(LocalizedString title, Video video, Project project) {
		super(title, project);
		this.video = video;
		start = new Long(0);
		end = video.getDuration();
		thumbnail = new VideoThumbnail(this, start, project);
	}

	/**
	 * Konstruktor fuer eine neue Szene die sich per Default ueber ein gesamtes
	 * Video erstreckt.
	 * 
	 * @param title
	 *            Titel der Szene.
	 * @param video
	 *            Video aus dem die Szene stammt.
	 */
	public Scene(String title, Video video, Project project) {
		super(title, project);
		this.video = video;
		start = new Long(0);
		end = video.getDuration();
		thumbnail = new VideoThumbnail(this, start, project);
	}

	/**
	 * Getter of the property <tt>video</tt>
	 * 
	 * @return Returns the video.
	 * @uml.property name="video"
	 */
	public Video getVideo() {
		return video;
	}

	/**
	 *  Returns the absolute start time of the scene in nanoseconds.
	 * 
	 * @return Returns the start.
	 * @uml.property name="start"
	 */
	public Long getStart() {
		return start;
	}

	/**
	 * Returns the absolute end time of the scene in nanoseconds.
	 * 
	 * @return Returns the end.
	 * @uml.property name="end"
	 */
	public Long getEnd() {
		return end;
	}
	
	/**
	 * Returns the thumbnail used to describe the scene.
	 * @return thumbnail of the scene
	 */
	@Override
	public VideoThumbnail getThumbnail() {
		return thumbnail;
	}
	

	/**
	 * Setter of the property <tt>start</tt>
	 * 
	 * @param start
	 *            The start to set.
	 * @uml.property name="start"
	 */
	public void setStart(Long start) {
		Long oldValue = this.start;
		this.start = start;
		firePropertyChange(PROP_START, oldValue, start);
	}

	/**
	 * Setter of the property <tt>end</tt>
	 * 
	 * @param end
	 *            The end to set.
	 * @uml.property name="end"
	 */
	public void setEnd(Long end) {
		Long oldValue = this.end;
		this.end = end;
		firePropertyChange(PROP_END, oldValue, end);
	}	
	
	/**
	 * Set the absolute time in nanoseconds of the video frame which should be
	 * referenced by the thumbnail of this scene.
	 * @param time absolute time of the thumbnail frame
	 */
	@Override
	public void changeThumbnailTime(long time) {
		long oldValue = this.getThumbnail().getTime();
		this.getThumbnail().setTime(time);
		firePropertyChange(PROP_THUMB, oldValue, time);
	}

	@Override
	public List<LocalizedElement> getLocalizedContents() {
		return new ArrayList<LocalizedElement>(video.getFiles());
	}
}
