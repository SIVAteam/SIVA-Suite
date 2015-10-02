package org.iviPro.model.resources;

import java.util.ArrayList;
import java.util.List;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedElement;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;


/**
 * ein Audiopart definiert einen Abschnitt in einem Audiofile entsprechend einer Szene im Video
 * @author juhoffma
 */
public class AudioPart extends IAbstractBean implements IResource {	
	
	public static final String PROP_START = "start"; //$NON-NLS-1$
	public static final String PROP_END = "end"; //$NON-NLS-1$

	/**
	 * @uml.property   name="video"
	 */
	private Audio audio;

	/**
	 * @uml.property   name="end"
	 */
	private Long end;

	/**
	 * @uml.property   name="start"
	 */
	private Long start;

	/**
	 * Konstruktor fuer einen neuen Audiopart
	 * @param title
	 *            Titel der Szene.
	 * @param video
	 *            Video aus dem die Szene stammt.
	 */
	public AudioPart(LocalizedString title, Audio audio, Project project) {
		super(title, project);
		this.audio = audio;
		start = new Long(0);
		end = audio.getDuration();
	}

	/**
	 * Konstruktor fuer einen neuen Audiopart
	 * @param title
	 *            Titel der Szene.
	 * @param video
	 *            Video aus dem die Szene stammt.
	 */
	public AudioPart(String title, Audio audio, Project project) {
		super(title, project);
		this.audio = audio;
		start = new Long(0);
		end = audio.getDuration();
	}

	/**
	 * Getter of the property <tt>video</tt>
	 * 
	 * @return Returns the audio.
	 * @uml.property name="audio"
	 */
	public Audio getAudio() {
		return audio;
	}

	/**
	 * Getter of the property <tt>start</tt>
	 * 
	 * @return Returns the start.
	 * @uml.property name="start"
	 */
	public Long getStart() {
		return start;
	}

	/**
	 * Getter of the property <tt>end</tt>
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
		return new ArrayList<LocalizedElement>(audio.getFiles());
	}
	
	@Override
	public String getBeanTag() {
		return "Audio part";
	}
}
