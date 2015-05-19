package org.iviPro.model.quiz;

import org.iviPro.model.IQuizBean;
import org.iviPro.model.Project;

/**
 * Klasse zum Verwalten einer Kante.
 * 
 * @author Sabine Gattermann
 * @modified Stefan Zwicklbauer
 * 
 * @uml.dependency supplier="org.iviPro.model.quiz"
 */
public class Edge extends IQuizBean {

	private static final long serialVersionUID = 1L;
	
	/**
	 * @uml.property name="idNodeSource"
	 */
	private int idNodeSource;

	/**
	 * @uml.property name="idNodeDestination"
	 */
	private int idNodeDestination;

	/**
	 * @uml.property name="idCondition"
	 */
	private int idCondition;

	// fuer Dijkstra:
	public final double weight = 1.0;
	
	/**
	 * @uml.property name="destinationDijkstraID"
	 */
	private int idDestinationDijkstra;

	/**
	 * Standard-Konstruktor.
	 */
	public Edge(Project project) {
		super(project);
		this.idNodeSource = -1;
		this.idNodeDestination = -1;
		this.idCondition = -1;
		this.idDestinationDijkstra = -1;
	}

	/**
	 * Konstruktor
	 * @param idNodeSource
	 *            Die Quellknoten-ID.
	 * @param idNodeDestination
	 *            Die Zielknoten-ID
	 * @param idCondition
	 *            Die Bedingungs-ID.
	 */
	public Edge(Project project, int idNodeSource, int idNodeDestination,
			int idCondition, Node destination) {
		super(project);
		this.idNodeSource = idNodeSource;
		this.idNodeDestination = idNodeDestination;
		this.idCondition = idCondition;
		this.idDestinationDijkstra = destination.getIdNode();
	}

	/**
	 * Getter fuer Quellknoten-ID.
	 * 
	 * @return Die ID.
	 */
	public int getIdNodeSource() {
		return idNodeSource;
	}

	/**
	 * Setter fuer Quellknoten-ID.
	 * 
	 * @param idNodeSource
	 *            Die ID.
	 */
	public void setIdNodeSource(int idNodeSource) {
		this.idNodeSource = idNodeSource;
	}

	/**
	 * Getter fuer Zielknoten-ID.
	 * 
	 * @return Die Id.
	 */
	public int getIdNodeDestination() {
		return idNodeDestination;
	}

	/**
	 * Setter fuer Zielknoten-ID.
	 * 
	 * @param idNodeDestination
	 *            Die ID.
	 */
	public void setIdNodeDestination(int idNodeDestination) {
		this.idNodeDestination = idNodeDestination;
	}

	/**
	 * Getter fuer Bedingungs-ID.
	 * 
	 * @return Die ID.
	 */
	public int getIdCondition() {
		return idCondition;
	}

	/**
	 * Setter fuer Bedingungs-ID.
	 * 
	 * @param idCondition
	 *            Die ID.
	 */
	public void setIdCondition(int idCondition) {
		this.idCondition = idCondition;
	}

	/**
	 * Setter fuer Zielknoten. Benoetigt fuer Dijkstra.
	 * 
	 * @param destination
	 *            Der Zielknoten.
	 */

	public void setDestination(Node destination) {
		this.idDestinationDijkstra = destination.getIdNode();
	}

	/**
	 * Getter fuer Zielknoten. Benoetigt fuer Dijkstra.
	 * 
	 * @return Der Zielknoten.
	 */
	public Node getDestination() {
		return NodeManager.getInstance().getNodeData(idDestinationDijkstra);
	}

}
