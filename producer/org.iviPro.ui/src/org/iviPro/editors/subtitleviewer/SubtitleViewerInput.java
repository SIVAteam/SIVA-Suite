package org.iviPro.editors.subtitleviewer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.iviPro.model.resources.Subtitle;

/** 
 * @author juhoffma
 * 
 */
public class SubtitleViewerInput implements IEditorInput {

	/**
	 * Der anzuzeigende Untertitel
	 */
	private Subtitle subtitle;

	public SubtitleViewerInput(Subtitle subtitle) {
		this.subtitle = subtitle;
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
		return subtitle.getTitle();
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return subtitle.getTitle();
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
		if (o instanceof SubtitleViewerInput) {
			Subtitle other = ((SubtitleViewerInput) o).getSubtitle();
			if (other.getDescription().equals(subtitle.getDescription()) &&
				other.getTitle().equals(subtitle.getTitle())) {
				return true;
			}
			return false;
		}
		return false;
	}

	public Subtitle getSubtitle() {
		return this.subtitle;
	}

}
