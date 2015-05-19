package org.iviPro.editors.imageviewer;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.iviPro.application.Application;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.PictureGallery;
import org.iviPro.model.resources.Picture;

public class ImageWidgetInput implements IEditorInput {
	private static Logger logger = Logger.getLogger(ImageWidgetInput.class);

	/**
	 * Das Media Object das im Image-Viewer geladen werden soll
	 * entweder Picture oder PictureGallery
	 */
	private IAbstractBean content;

	/**
	 * Erstellt ein Input-Objekt fuer den ImageViewer
	 * @param das Bild 
	 */
	public ImageWidgetInput(IAbstractBean selectedContent) {
		super();
		this.content = selectedContent;
		logger.debug("Created new ImageEditorInput for " + selectedContent); //$NON-NLS-1$
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
		return content.getTitle(Application.getCurrentLanguage());
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		if (content instanceof Picture) {
			return ((Picture) content).getFile().getAbsolutePath();
		} else
		if (content instanceof PictureGallery) {
			return content.getTitle();
		}
		return "";
	}

	@Override
	public boolean equals(Object o) {

		// Einschränken auf Inputs der Klasse MediaObject Input
		if (o instanceof ImageWidgetInput) {

			IAbstractBean other = ((ImageWidgetInput) o).getInput();

			if (content.equals(other)) {
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

	public IAbstractBean getInput() {
		return content;
	}
}
