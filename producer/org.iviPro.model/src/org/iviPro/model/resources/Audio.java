/**
 * 
 */
package org.iviPro.model.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.iviPro.model.BeanList;
import org.iviPro.model.IMediaObject;
import org.iviPro.model.ITimeBasedObject;
import org.iviPro.model.LocalizedElement;
import org.iviPro.model.Project;

/** 
 * @author juhoffma
 */
public class Audio extends IMediaObject implements ITimeBasedObject, IResource {
	
	private long duration;

	public Audio(File file, Project project) {
		super(file, project);
		parts = new BeanList<AudioPart>(project);		
	}
	
	/**
	 * @uml.property name="scenes" readOnly="true"
	 */
	private BeanList<AudioPart> parts;
	
	/**
	 * Getter of the property <tt>audioParts</tt>
	 * 
	 * @return Returns a list of the AudioParts depending on this Audio object.
	 * @uml.property name="audioParts"
	 */
	public BeanList<AudioPart> getAudioParts() {
		return parts;
	}

	/* (non-Javadoc)
	 * @see org.iviPro.model.ITimeBasedMediaObject#getDuration()
	 */
	@Override
	public Long getDuration() {
		return this.duration;
	}
	
	/**
	 * Gibt den Audio-Part des Audio-Files mit einem bestimmten Titel zurueck. Gibt null
	 * zurueck, wenn kein Audio-Part mit so einem Titel existiert.
	 * 
	 * @param title
	 * @param language
	 * @return
	 */
	public AudioPart getAudioPart(String title, Locale language) {
		Iterator<AudioPart> it = parts.iterator();
		while (it.hasNext()) {
			AudioPart curPart = it.next();
			if (title.equals(curPart.getTitle(language))) {
				return curPart;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.iviPro.model.ITimeBasedMediaObject#setDuration(java.lang.Long)
	 */
	@Override
	public void setDuration(Long duration) {	
		this.duration = duration;
	}

	@Override
	public List<LocalizedElement> getLocalizedContents() {
		return new ArrayList<LocalizedElement>(getFiles());
	}
}
