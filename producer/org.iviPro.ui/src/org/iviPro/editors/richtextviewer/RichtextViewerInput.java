package org.iviPro.editors.richtextviewer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.iviPro.model.resources.RichText;

/**
 * Diese Klasse versorgt einen Editor/Player mit einem MediaObject. Es wird
 * sicher gestellt, dass immer nur ein Editor/Player für ein bestimmtes
 * MediaObject offen ist. Aktuell unterstützt ein MediaObject die Mediatypen
 * Video, Audio, Image und Other (z.b.Text)
 * 
 * @author juhoffma
 * 
 */
public class RichtextViewerInput implements IEditorInput {

	/**
	 * Der anzuzeigende Richtext
	 */
	private RichText richtext;

	public RichtextViewerInput(RichText richtext) {
		this.richtext = richtext;
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
		return richtext.getTitle();
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return richtext.getFile().getAbsolutePath();
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	/**
	 * Hier wird die equals Standardimplementierung überschrieben. Es wird somit
	 * sichergestellt, dass nur dann ein neuer Editor erzeugt wird, wenn für
	 * dieses File noch keiner existiert.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof RichtextViewerInput) {
			RichText other = ((RichtextViewerInput) o).getRichtext();
			return richtext.equals(other);
		}
		return false;
	}

	public RichText getRichtext() {
		return this.richtext;
	}

}
