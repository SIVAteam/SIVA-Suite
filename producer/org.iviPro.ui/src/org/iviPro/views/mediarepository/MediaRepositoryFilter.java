package org.iviPro.views.mediarepository;

import org.eclipse.jface.viewers.Viewer;
import org.iviPro.views.IAbstractRepositoryFilter;

/**
 * Filter fuer das Media-Repository, der nur die Eintraege anzeigt, welche einem
 * bestimmten Filter-Text entsprechen.
 * 
 * @author Christian Dellwo
 * 
 */
public class MediaRepositoryFilter extends IAbstractRepositoryFilter {

	@Override
	protected boolean filter(String filterText, Object element,
			Object parentElement, Viewer viewer) {
		// Wenn ein Filter-Text gesetzt ist und es sich bei dem zu pruefenden
		// Objekt um ein Medien-Objekt handelt, dann lassen wir es nur durch,
		// wenn es den Filtertext im Namen enthaelt.
		if (element instanceof MediaTreeLeaf) {
			String name = ((MediaTreeLeaf) element).getName().toLowerCase();
			return name.contains(filterText.toLowerCase());
		}
		// In allen anderen Faellen lassen wir das Objekt generell passieren.
		return true;
	}
}
