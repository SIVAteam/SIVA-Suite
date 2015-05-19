package org.iviPro.model.graph;

import org.iviPro.model.Project;

/**
 * Standard-Implementierung einer Verbindung von einem Knoten (source) zu einem
 * anderen Knoten (target).
 * 
 * @author dellwo
 * 
 */
public class DefaultConnection extends IConnection {

	/**
	 * Erstellt eine Standard-Verbindung zwischen zwei Knoten.
	 * 
	 * @param source
	 *            Der Knoten von dem die Verbindung ausgeht.
	 * @param target
	 *            Der Zielknoten der Verbindung.
	 * @param project
	 *            Das zugehoerige Projekt.
	 */
	protected DefaultConnection(IGraphNode source, IGraphNode target,
			Project project) {
		super(source, target, project);
	}

}
