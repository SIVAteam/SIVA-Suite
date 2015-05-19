package org.iviPro.model.graph;

import org.iviPro.model.Project;

/**
 * Klasse die eine abhaengige Verbindung beschreibt. Bei einer abhaengigen
 * Verbindung ist der Zielknoten der Verbindung direkt abhaengig von dem
 * Quellknoten, d.h. wird der Quellknoten geloescht, muss der Zielknoten auch
 * geloescht werden aufgrund der Abhaengigkeit.
 * 
 * @author dellwo
 * 
 */
public class DependentConnection extends IConnection {

	/**
	 * Erstellt eine abhaengige Verbindung zwischen zwei Knoten.
	 * 
	 * @param source
	 *            Der Knoten von dem die Verbindung ausgeht.
	 * @param target
	 *            Der Zielknoten der Verbindung, der abhaengig vom Quellknoten
	 *            ist.
	 * @param project
	 *            Das zugehoerige Projekt.
	 */
	DependentConnection(IGraphNode source, IGraphNode target,
			Project project) {
		super(source, target, project);
	}

}
