/**
 * 
 */
package org.iviPro.model.graph;

import java.util.ArrayList;
import java.util.List;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.model.resources.IResource;

/**
 * Annotationsknoten fuer Audio-Annotationen.
 * 
 * @author dellwo
 */
public class NodeAnnotationAudio extends INodeAnnotationLeaf {
	
	/**
	 * Das Audio-Objekt dem die Annotation zu Grunde liegt.
	 * 
	 * @uml.property name="audio"
	 */
	private Audio audio;
		
	/**
	 * Der Audio-Part dem die Annotation zu Grunde liegt
	 * 
	 * @uml.property name="audiopart"
	 */
	private AudioPart audioPart;
	
	/**
	 * der Content-Typ, Audio oder Audio-Part
	 */
	private int contentType = CONTENT_AUDIO;	
	public static final int CONTENT_AUDIO = 0;
	public static final int CONTENT_AUDIOPART = 1;

	/**
	 * Erstellt eine neue Audio-Annotation
	 * 
	 * @param title
	 *            Der Titel der Annotation.
	 * @param project
	 *            Das zugehoerige Projekt.
	 */
	public NodeAnnotationAudio(LocalizedString title, Project project) {
		super(title, project);
	}

	/**
	 * Erstellt eine neue Audio-Annotation
	 * 
	 * @param title
	 *            Der Titel der Annotation.
	 * @param project
	 *            Das zugehoerige Projekt.
	 */
	public NodeAnnotationAudio(String title, Project project) {
		super(title, project);
	}
	
	/**
	 * Gibt das mit der Annotation verknuepfte Audio-Objekt zurueck.
	 * 
	 * @return Verknuepftes Audio-Objekt.
	 * @uml.property name="audio"
	 */
	public Audio getAudio() {
		return audio;
	}
	
	/**
	 * Gibt das mit der Annotation verknuepfte Audio-Objekt zurueck.
	 * 
	 * @return Verknuepftes AudioPart-Objekt.
	 * @uml.property name="audioPart"
	 */
	public AudioPart getAudioPart() {
		return audioPart;
	}

	/**
	 * Setzt das mit der Annotation verknuepfte Audio-Objekt.
	 * 
	 * @param audio
	 *            Verknuepftes Audio-Objekt.
	 * @uml.property name="audio"
	 */
	public void setAudio(Audio audio) {
		this.audio = audio;
		this.contentType = CONTENT_AUDIO;
		this.audioPart = null;
		firePropertyChange(PROP_SETCONTENT, null, audio);
	}
	
	/**
	 * Setzt das mit der Annotation verknuepfte AudioPart-Objekt.
	 * 
	 * @param audio Verknuepftes AudioPart-Objekt.
	 * @uml.property name="audio"
	 */
	public void setAudioPart(AudioPart audioPart) {
		this.audioPart = audioPart;
		this.contentType = CONTENT_AUDIOPART;
		this.audio = null;
		firePropertyChange(PROP_SETCONTENT, null, audioPart);
	}

	public int getContentType() {
		return this.contentType;
	}

	@Override
	public List<IResource> getResources() {
		ArrayList<IResource> resources = new ArrayList<IResource>(1);
		if (contentType == CONTENT_AUDIO && audio != null) {
			resources.add(audio);
		}
		if (contentType == CONTENT_AUDIOPART && audioPart != null) {
			resources.add(audioPart);
		}
		return resources;
	}
	
	@Override
	public boolean isDependentOn(IAbstractBean object) {
		// Audio-Annotation ist abhaengig von ihrem Audio-Objekt.
		return object != null && (object == audio || object == audioPart);
	}

	@Override
	public String getBeanTag() {
		return "Audio annotation";
	}
}
