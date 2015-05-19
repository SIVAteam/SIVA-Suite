package org.iviPro.model.quiz;

import java.util.Iterator;
import java.util.LinkedList;

import org.iviPro.model.IQuizBean;
import org.iviPro.model.Project;

/**
 * Klasse zum Verwalten eine Knotens.
 * 
 * @author Sabine Gattermann
 * @modified Stefan Zwicklbauer
 * 
 */
public class Node extends IQuizBean implements Comparable<Node> {

	private static final long serialVersionUID = 1L;
	
	/**
	 * @uml.property name="idNode"
	 */
	private int idNode;
	
	/**
	 * @uml.property name="idTest"
	 */
	private int idTest;
	
	/**
	 * @uml.property name="NodePosition"
	 */
	private int position;

	/**
	 * @uml.property name="NodePoints"
	 */
	private int points;
	
	/**
	 * @uml.property name="hasQuestion"
	 */
	private int hasQuestion;
	
	/**
	 * @uml.property name="idNodePredecessor"
	 */
	private int idNodePredecessor;
	
	/**
	 * @uml.property name="idNodePredecessor"
	 */
	private boolean random;
	
//	private LinkedList<Integer> addInfo;

	/**
	 * benoetigt fuer Dijkstra:
	 * 
	 * @uml.property name="minDistance"
	 */
	private double minDistance;
	
//	private LinkedList<Edge> adjacencies;
	
	/**
	 * @uml.property name="source"
	 */
	private LinkedList<Integer> source;
	
	/**
	 * @uml.property name="target"
	 */
	private LinkedList<Integer> target;
	
	/**
	 * @uml.property name="addInfo"
	 */
	private LinkedList<Integer> addInfo;
	
	/**
	 * @uml.property name="previous"
	 */
	private int previous;

	/**
	 * Konstruktor.
	 * 
	 * @param idNode
	 *            Die Knoten-ID.
	 * @param idTest
	 *            Die Test-ID.
	 * @param position
	 *            Die Position.
	 * @param points
	 *            Die Punkte.
	 * @param hasQuestion
	 *            Die Frage-ID.
	 * @param idNodePredecessor
	 *            Die Vorgaengerknoten-ID.
	 * @param random
	 *            Zufallsverteilung von Antworten.
	 */
	public Node(Project project, int idNode, int idTest, int position, int points,
			int hasQuestion, int idNodePredecessor, boolean random) {
		super(project);
		this.idNode = idNode;
		this.idTest = idTest;
		this.position = position;
		this.points = points;
		this.hasQuestion = hasQuestion;
		this.idNodePredecessor = idNodePredecessor;
		minDistance = Double.POSITIVE_INFINITY;
//		adjacencies = new LinkedList<Edge>();
		previous = -1;
		this.random = random;
		this.source = new LinkedList<Integer>();
		this.target = new LinkedList<Integer>();
		addInfo = new LinkedList<Integer>();
	}

	/**
	 * Konstruktor.
	 * 
	 * @param idTest
	 *            Die Test-ID.
	 * @param position
	 *            Die Position.
	 * @param points
	 *            Die Punkte.
	 * @param hasQuestion
	 *            Die Frage-ID.
	 * @param idNodePredecessor
	 *            Die Vorgaengerknoten-ID.
	 * @param random
	 *            Zufallsverteilung von Antworten.
	 */
	public Node(Project project, int idTest, int position, int points, int hasQuestion,
			int idNodePredecessor, boolean random) {
		super(project);
		this.idNode = -1;
		this.idTest = idTest;
		this.position = position;
		this.points = points;
		this.hasQuestion = hasQuestion;
		this.idNodePredecessor = idNodePredecessor;
		minDistance = Double.POSITIVE_INFINITY;
//		adjacencies = new LinkedList<Edge>();
		previous = -1;
		this.random = random;
		this.source = new LinkedList<Integer>();
		this.target = new LinkedList<Integer>();
		addInfo = new LinkedList<Integer>();
	}

	/**
	 * Standard-Konstruktor.
	 */
	public Node(Project project) {
		super(project);
		this.idNode = -1;
		this.idTest = -1;
		this.position = -1;
		this.points = 1;
		this.hasQuestion = -1;
		this.idNodePredecessor = -1;
		minDistance = Double.POSITIVE_INFINITY;
//		adjacencies = new LinkedList<Edge>();
		previous = -1;
		this.random = true;
		this.source = new LinkedList<Integer>();
		this.target = new LinkedList<Integer>();
		addInfo = new LinkedList<Integer>();
	}

	/**
	 * Getter fuer Knoten-ID.
	 * 
	 * @return Die ID.
	 */
	public int getIdNode() {
		return idNode;
	}

	/**
	 * Setter fuer Knoten-ID.
	 * 
	 * @param idNode
	 *            Die ID.
	 */
	public void setIdNode(int idNode) {
		this.idNode = idNode;
	}

	/**
	 * Getter fuer die Test-ID.
	 * 
	 * @return Die ID.
	 */
	public int getIdTest() {
		return idTest;
	}

	/**
	 * Setter fuer die Test-ID.
	 * 
	 * @param idTest
	 *            Die ID.
	 */
	public void setIdTest(int idTest) {
		this.idTest = idTest;
	}

	/**
	 * Getter fuer Punkte.
	 * 
	 * @return Die Punkte.
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * Setter fuer Punkte.
	 * 
	 * @param points
	 *            Die Punkte.
	 */
	public void setPoints(int points) {
		this.points = points;
	}

	/**
	 * Getter fuer Position.
	 * 
	 * @return Die Position.
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Setter fuer Position.
	 * 
	 * @param position
	 *            Die Position.
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * Setter fuer Frage-ID.
	 * 
	 * @param hasQuestion
	 *            Die Frage-ID.
	 */
	public void setHasQuestion(int hasQuestion) {
		this.hasQuestion = hasQuestion;
	}

	/**
	 * Getter fuer Frage-ID.
	 * 
	 * @return Die Frage-ID.
	 */
	public int getHasQuestion() {
		return hasQuestion;
	}

	/**
	 * Getter fuer VorgaengerKnoten-ID.
	 * 
	 * @return Die VorgaengerKnoten-ID.
	 */
	public int getIdNodePredecessor() {
		return idNodePredecessor;
	}

	/**
	 * Setter fuer VorgaengerKnoten-ID.
	 * 
	 * @param idNodePredecessor
	 *            Die VorgaengerKnoten-ID.
	 */
	public void setIdNodePredecessor(int idNodePredecessor) {
		this.idNodePredecessor = idNodePredecessor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Node otherNode) {
		return Double.compare(minDistance, otherNode.minDistance);
	}

	/**
	 * Getter fuer MinDistance. (Benoetigt fuer Dijkstra)
	 * 
	 * @return Die minimale Distanz.
	 */
	public double getMinDistance() {
		return minDistance;
	}

	/**
	 * Setter fuer MinDistance. (Benoetigt fuer Dijkstra)
	 * 
	 * @param minDistance
	 *            Die minimale Distanz.
	 */
	public void setMinDistance(double minDistance) {
		this.minDistance = minDistance;
	}

	/**
	 * Getter fuer Adjaszenzliste. (Benoetigt fuer Dijkstra)
	 * 
	 * @return Die Adjaszenzliste.
	 */
	public LinkedList<Edge> getAdjacencies() {
		LinkedList<Edge> lst = new LinkedList<Edge>();
		for (int i = 0; i < source.size(); ++i) {
			Edge ed = EdgeManager.getInstance().getEdgeBySourceAndTarget(source.get(i), target.get(i));
			if(ed != null) {
				lst.add(ed);
			}
		}
		return lst;
	}

	/**
	 * Setter fuer Adjaszenzliste. (Benoetigt fuer Dijkstra)
	 * 
	 * @param adjacencies
	 *            Die Adjaszenzliste.
	 */
	public void setAdjacencies(LinkedList<Edge> adjacencies) {
		for (Iterator<Edge> iterator = adjacencies.iterator(); iterator.hasNext();) {
			Edge edge = (Edge) iterator.next();
			source.add(edge.getIdNodeSource());
			target.add(edge.getIdNodeDestination());
		}
	}

	/**
	 * Getter fuer Vorgaengerknoten. (Benoetigt fuer Dijkstra)
	 * 
	 * @return Der Vorgaengerknoten.
	 */
	public Node getPrevious() {
		return NodeManager.getInstance().getNodeData(previous);
	}

	/**
	 * Setter fuer Vorgaengerknoten. (Benoetigt fuer Dijkstra)
	 * 
	 * @param previous
	 *            Der Vorgaengerknoten.
	 */
	public void setPrevious(Node previous) {
		this.previous = previous.getIdNode();
	}

	/**
	 * Setter fuer Randomisierung von Antworten.
	 * 
	 * @param random
	 *            Indikator fuer Randomisierung.
	 */
	public void setRandom(boolean random) {
		this.random = random;
	}

	/**
	 * Getter fuer Randomisierung von Antworten.
	 * 
	 * @return Indikator fuer Randomisierung.
	 */
	public boolean isRandom() {
		return random;
	}

	public void removeAdditionalInfo(int idInfo) {
		for (Iterator<Integer> iterator = addInfo.iterator(); iterator
				.hasNext();) {
			int info = (Integer) iterator.next();
			if (info == idInfo) {
				iterator.remove();
			}
		}
	}

	public LinkedList<AdditionalInfo> getNodeInfos() {
		LinkedList<AdditionalInfo> lst = new LinkedList<AdditionalInfo>();
		for (Iterator<Integer> iterator = addInfo.iterator(); iterator.hasNext();) {
			int id = (int) iterator.next();
			lst.add(AdditionalInfoManager.getInstance().getAdditionalInfoById(id));
		}
		return lst;
	}

}
