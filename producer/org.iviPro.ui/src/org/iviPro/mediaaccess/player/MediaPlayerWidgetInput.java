package org.iviPro.mediaaccess.player;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.iviPro.application.Application;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IMediaObject;

/**
 * Diese Klasse versorgt einen Editor/Player mit einem MediaObject. Es wird
 * sicher gestellt, dass immer nur ein Editor/Player für ein bestimmtes
 * MediaObject offen ist. Aktuell unterstützt ein MediaObject die Mediatypen
 * Video, Audio, Image und Other (z.b.Text)
 * 
 * @author juhoffma
 * 
 */
public class MediaPlayerWidgetInput implements IEditorInput {
	private static Logger logger = Logger.getLogger(MediaPlayerWidgetInput.class);

	/**
	 * Das Media Object das im Editor/Player geladen werden soll
	 */
	private IAbstractBean mediaObject;

	public MediaPlayerWidgetInput(IAbstractBean mediaObject) {
		super();
		this.mediaObject = mediaObject;
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
		return mediaObject.getTitle(Application.getCurrentLanguage());
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		if (mediaObject instanceof IMediaObject) {
			return ((IMediaObject) mediaObject).getFile().getAbsolutePath();
		}
		return mediaObject.getTitle();
	}

	@Override
	public Object getAdapter(Class adapter) {
		logger.debug("---------" + adapter.getName()); //$NON-NLS-1$
		return null;
	}

	/**
	 * Hier wird die equals Standardimplementierung überschrieben. Es wird somit
	 * sichergestellt, dass nur dann ein neuer Editor erzeugt wird, wenn für
	 * dieses File noch keiner existiert.
	 */
	@Override
	public boolean equals(Object o) {

		// Einschränken auf Inputs der Klasse PlayerInput Input
		if (o instanceof MediaPlayerWidgetInput) {

			IAbstractBean otherMedia = ((MediaPlayerWidgetInput) o).getMediaObject();

			if (mediaObject.equals(otherMedia)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public IAbstractBean getMediaObject() {
		return this.mediaObject;
	}

}
