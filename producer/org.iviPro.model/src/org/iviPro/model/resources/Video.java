package org.iviPro.model.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.iviPro.model.BeanList;
import org.iviPro.model.IPixelBasedObject;
import org.iviPro.model.ITimeBasedObject;
import org.iviPro.model.LocalizedElement;
import org.iviPro.model.Project;

/**
 * @author dellwo
 */
public class Video extends IPixelBasedObject implements ITimeBasedObject, IResource {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Der Name des "frameRate-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_FRAMERATE = "framerate";

	/**
	 * @uml.property name="scenes" readOnly="true"
	 */
	private BeanList<Scene> scenes;

	/**
	 * Dauer des Videos in Nanosekunden.
	 */
	private Long duration = null;
	
	/**
	 * Framerate des Videos
	 */
	private double frameRate = 0;

	public Video(File file, Project project) {
		super(file, null, project);
		scenes = new BeanList<Scene>(project);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.iviPro.model.ITimeBasedMediaObject#getDuration()
	 */
	@Override
	public Long getDuration() {
		return duration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.iviPro.model.ITimeBasedMediaObject#setDuration(java.lang.Long)
	 */
	@Override
	public void setDuration(Long duration) {
		Long oldValue = this.duration;
		this.duration = duration;
		firePropertyChange(PROP_DURATION, oldValue, duration);
	}

	/**
	 * Getter of the property <tt>scenes</tt>
	 * 
	 * @return Returns the scenes.
	 * @uml.property name="scenes"
	 */
	public BeanList<Scene> getScenes() {
		return scenes;
	}

	/**
	 * Gibt die Szene des Videos mit einem bestimmten Titel zurueck. Gibt null
	 * zurueck, wenn keine Szene mit so einem Titel existiert.
	 * 
	 * @param title
	 * @param language
	 * @return
	 */
	public Scene getScene(String title, Locale language) {
		Iterator<Scene> it = scenes.iterator();
		while (it.hasNext()) {
			Scene curScene = it.next();
			if (title.equals(curScene.getTitle(language))) {
				return curScene;
			}
		}
		return null;
	}
	
	public void setFrameRate(double frameRate) {
		double oldValue = this.frameRate;
		this.frameRate = frameRate;
		firePropertyChange(PROP_FRAMERATE, oldValue, frameRate);
	}
	
	public double getFrameRate() {
		return this.frameRate;
	}

	@Override
	public List<LocalizedElement> getLocalizedContents() {
		return new ArrayList<LocalizedElement>(getFiles());
	}
}
