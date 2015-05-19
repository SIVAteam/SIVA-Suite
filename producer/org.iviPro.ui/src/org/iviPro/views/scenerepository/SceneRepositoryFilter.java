package org.iviPro.views.scenerepository;

import org.eclipse.jface.viewers.Viewer;
import org.iviPro.views.IAbstractRepositoryFilter;

/**
 * Filter fuer das Media-Repository, der nur die Eintraege anzeigt, welche einem
 * bestimmten Filter-Text entsprechen.
 * 
 * @author Christian Dellwo
 * 
 */
public class SceneRepositoryFilter extends IAbstractRepositoryFilter {

	@Override
	protected boolean filter(String filterText, Object element,
			Object parentElement, Viewer viewer) {
		// Wenn ein Filter-Text gesetzt ist und es sich bei dem zu pruefenden
		// Objekt um ein Szenen-Objekt handelt, dann lassen wir es nur durch,
		// wenn es den Filtertext im Namen enthaelt bzw. entsprechende Keywords gesetzt sind
		if (element instanceof SceneTreeLeaf) {
			String name = ((SceneTreeLeaf) element).getName().toLowerCase();
			if (name.contains(filterText.toLowerCase())) {
				return true;
			}
			// prüfe ob die Keywords vorkommen			
			String keywords = ((SceneTreeLeaf) element).getScene().getKeywords().toLowerCase();
			if (keywords.contains(filterText.toLowerCase())) {
				return true;
			}
			return false;
		}
		// In allen anderen Faellen lassen wir das Objekt generell passieren.
		return true;
	}
}
