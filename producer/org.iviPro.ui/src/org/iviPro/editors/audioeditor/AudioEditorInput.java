package org.iviPro.editors.audioeditor;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.iviPro.application.Application;
import org.iviPro.model.resources.Audio;

/**
 * Klasse die die Input-Parameter fuer den AudioPart-Editor kapselt.
 */
public class AudioEditorInput implements IEditorInput {
	private static Logger logger = Logger.getLogger(AudioEditorInput.class);

	/**
	 * Das Media Object das im Audio-Part-Editor geladen werden soll
	 */
	private Audio audio;

	/**
	 * Erstellt ein Input-Objekt fuer den Audio-Editor
	 * 
	 */
	public AudioEditorInput(Audio audio) {
		this.audio = audio;
		logger.debug("Created new DefineScenesEditorInput for " + audio); //$NON-NLS-1$
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return audio.getTitle(Application.getCurrentLanguage());
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return audio.getFile().getAbsolutePath();
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean equals(Object o) {

		// Einschränken auf Inputs der Klasse MediaObject Input
		if (o instanceof AudioEditorInput) {

			Audio otherAudio = ((AudioEditorInput) o).getAudio();

			if (otherAudio.equals(this.audio)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * Gibt den Wert von mediaObject zurueck
	 * 
	 * @return Wert von mediaObject
	 */
	public Audio getAudio() {
		return audio;
	}
}
