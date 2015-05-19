package org.iviPro.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Abstrakte Basisklasse fuer die TreeView-Filter in den Repositories.
 * 
 * @author dellwo
 * 
 */
public abstract class IAbstractRepositoryFilter extends ViewerFilter {

	/** Der aktuelle Filter-Text */
	private String filterText = null;

	/**
	 * Setzt den Filter-Text.
	 * 
	 * @param filterText
	 *            Der neue Filtertext.
	 */
	public void setFilterText(String filterText) {
		this.filterText = filterText;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers
	 * .Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (filterText == null || filterText.isEmpty()) {
			return true;
		} else {
			return filter(filterText, element, parentElement, viewer);
		}
	}

	/**
	 * Gibt an, ob ein gegebenes Element dem Filter-Text entspricht oder nicht.
	 * 
	 * @param filterText
	 *            Der Filter-Text.
	 * @param element
	 *            Das Element.
	 * @param parentElement
	 *            Das Vater-Element (falls vorhanden)
	 * @param viewer
	 *            Der Tree-Viewer.
	 * @return True, wenn das Element dem Filter entspricht und daher angezeigt
	 *         werden soll, ansonsten false.
	 */
	protected abstract boolean filter(String filterText, Object element,
			Object parentElement, Viewer viewer);
}
