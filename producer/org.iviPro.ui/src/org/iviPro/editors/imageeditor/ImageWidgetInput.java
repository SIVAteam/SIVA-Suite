package org.iviPro.editors.imageeditor;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.iviPro.application.Application;
import org.iviPro.model.resources.Picture;

public class ImageWidgetInput implements IEditorInput {
	private static Logger logger = Logger.getLogger(ImageWidgetInput.class);

	/**
	 * Das Media Object das im Image-Editor geladen werden soll
	 */
	private Picture picture;

	/**
	 * Erstellt ein Input-Objekt fuer den Szenen-Editor
	 * 
	 * @param mediaLocator
	 *            Der Media-Locator der auf die Video-Datei verweist, die im
	 *            Szenen-Editor geladen werden soll.
	 * 
	 */
	public ImageWidgetInput(Picture picture) {
		super();
		this.picture = picture;
		logger.debug("Created new ImageEditorInput for " + picture); //$NON-NLS-1$
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
		return picture.getTitle(Application.getCurrentLanguage());
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return picture.getFile().getAbsolutePath();
	}

	@Override
	public boolean equals(Object o) {

		// Einschränken auf Inputs der Klasse MediaObject Input
		if (o instanceof ImageWidgetInput) {

			Picture other = ((ImageWidgetInput) o).getPicture();

			if (picture.equals(other)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	public Picture getPicture() {
		return picture;
	}
}
