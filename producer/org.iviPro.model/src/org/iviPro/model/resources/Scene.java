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
public class Scene extends IAbstractBean implements IResource {

	public static final String PROP_START = "start"; //$NON-NLS-1$
	public static final String PROP_END = "end"; //$NON-NLS-1$

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

	@Override
	public List<LocalizedElement> getLocalizedContents() {
		return new ArrayList<LocalizedElement>(video.getFiles());
	}

}
