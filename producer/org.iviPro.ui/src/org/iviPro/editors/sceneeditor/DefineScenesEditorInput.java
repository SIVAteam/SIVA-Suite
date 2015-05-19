package org.iviPro.editors.sceneeditor;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.iviPro.application.Application;
import org.iviPro.model.resources.Video;

/**
 * Klasse die die Input-Parameter fuer den Szenen-Editor kapselt.
 */
public class DefineScenesEditorInput implements IEditorInput {
	private static Logger logger = Logger
			.getLogger(DefineScenesEditorInput.class);

	/**
	 * Das Media Object das im Szenen-Editor geladen werden soll
	 */
	private Video video;

	/**
	 * Erstellt ein Input-Objekt fuer den Szenen-Editor
	 * 
	 */
	public DefineScenesEditorInput(Video video) {
		this.video = video;
		logger.debug("Created new DefineScenesEditorInput for " + video); //$NON-NLS-1$
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
		return video.getTitle(Application.getCurrentLanguage());
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return video.getFile().getAbsolutePath();
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean equals(Object o) {

		// Einschränken auf Inputs der Klasse MediaObject Input
		if (o instanceof DefineScenesEditorInput) {

			Video otherVideo = ((DefineScenesEditorInput) o).getVideo();

			if (otherVideo.equals(this.video)) {
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
	public Video getVideo() {
		return video;
	}
}
