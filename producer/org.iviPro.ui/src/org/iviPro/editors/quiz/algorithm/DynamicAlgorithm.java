package org.iviPro.editors.quiz.algorithm;

import java.util.LinkedList;

import org.iviPro.application.Application;
import org.iviPro.editors.quiz.std.NodeModel;
import org.iviPro.model.quiz.Condition;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.Edge;
import org.iviPro.model.quiz.Node;
import org.iviPro.model.quiz.Test;

/**
 * Diese Klasse implementiert einen dynamischen Einfuege-Algorithmus. Dabei kann
 * jeder Knoten mehrere Nachfolger haben. Auch Kanten der Tiefe n-1 sind
 * moeglich (bei einer Tiefe n). An jede Kante ist eine Bedingung (im Sinn
 * erreichter Punkte) geknuepft, sowie eine Angabe, wieviele Knoten rueckwirkend
 * fuer diese Bedingung betrachtet werden sollen.
 * 
 * @author Sabine Gattermann
 * 
 */
public class DynamicAlgorithm implements InsertAlgorithm {

	/*
	 * festgesetzt auf 2, da bei dieser Implementierung das unmittelbare
	 * Kanteneinfuegen nur solange moeglich ist, wie die Kanten auf erster Ebene
	 * nicht allesamt besetzt sind. 2 ergibt sich kombinationen aus richtig,
	 * falsch und default (richtig & falsch, richtig % default, falsch &
	 * default). Tiefere Bedingungen koennen jedoch manuell durch Aendern einer
	 * Kante gesetzt werden.
	 */
	private static final int MAX_EDGES_PER_NODE = 2;

	private Condition dummyCondition;
	private String[] pointsList;
	private Test test;
	private int lookback;
	private String[] currentConditionPointsList;
	private String[] currentConditionLookbackList;
	private NodeModel nodeModel;
	private LinkedList<String> edgeNumbersForGui;
	private int type;
	private Node nodeToCheck;

	/**
	 * Konstruktor
	 * 
	 * @param nodeModel
	 *            Die aufrufende Model-Klasse.
	 * @param test
	 *            Der Test.
	 */
	public DynamicAlgorithm(NodeModel nodeModel, Test test) {
		this.type = 1;
		this.test = test;
		this.nodeModel = nodeModel;
		init();
	}

	/**
	 * Initialisierung von Variablen.
	 */
	private void init() {
		initPointsList();
		initDummyCondition();
		initEdgeNumbersForGui();
	}

	/**
	 * Initialisierung der Punkte-AuswahlListe. Diese ist jedoch beim
	 * dynamischen Algorithmus fixiert und enthaelt somit nur einen Wert.
	 */
	private void initPointsList() {
		this.pointsList = new String[1];

		pointsList[0] = String.valueOf(test.getMaxPoints());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#getPunkteListe()
	 */
	@Override
	public String[] getPointsList() {
		return pointsList;
	}

	/**
	 * Initialisierung der Dummy-Bedingung.
	 */
	private void initDummyCondition() {
		this.dummyCondition = new Condition(Application.getCurrentProject(), -1, 1);
		dummyCondition.setIdCondition(DbQueries
				.setConditionData(dummyCondition));
	}

	/**
	 * Initialisierung der edgeNumbersForGui-Liste.
	 */
	private void initEdgeNumbersForGui() {
		edgeNumbersForGui = new LinkedList<String>();
		for (int i = 0; i < 2; i++) {
			edgeNumbersForGui.add("");

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#getEdgeNumbersForGui()
	 */
	@Override
	public LinkedList<String> getEdgeNumbersForGui() {
		return edgeNumbersForGui;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#setEdgeNumbersForGui(int, int,
	 * utilities.beans.Kante)
	 */
	@Override
	public void setEdgeNumbersForGui(int from, int to, Edge edge) {

		if (from == to && to == -1) {
			edgeNumbersForGui.set(0, "");
			edgeNumbersForGui.set(1, "");
			nodeModel.currentEdge = null;

		} else {

			edgeNumbersForGui.set(0, String.valueOf(from));
			edgeNumbersForGui.set(1, String.valueOf(to));

			if (edge != null) {
				nodeModel.currentEdge = edge;
				nodeModel.currentCondition = DbQueries.getConditionData(edge
						.getIdCondition());
			}

			if (nodeModel.currentNode.getPosition() > 0) {
				currentConditionLookbackList = getPossibleLookback(from);
				currentConditionPointsList = getPossiblePoints(
						from,
						to,
						Integer.parseInt(currentConditionLookbackList[currentConditionLookbackList.length - 1]));

			} else {
				// falls zyklus auf pos 0 besteht
				edgeNumbersForGui.set(0, "");
				edgeNumbersForGui.set(1, "");
				nodeModel.currentEdge = null;
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#getDummyBedingung()
	 */
	@Override
	public Condition getDummyCondition() {
		return dummyCondition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#deleteNode()
	 */
	@Override
	public void deleteNode() {

		DbQueries.deleteNode(nodeModel.currentNode.getIdNode());

		if (nodeModel.getNumberOfNodesInTest() - nodeModel.currentPosition == 1) {
			// letzter knoten wurde gelöscht --> lade vorherigen
			// FIXME vorgänger nachfolger
			nodeModel.currentPosition--;

		} else {
			// knoten mit nachfolger(n) wurde geloescht --> shiftLeft
			int startPosition = nodeModel.currentPosition;
			int testId = nodeModel.currentNode.getIdTest();
			DbQueries.shiftTestNodesLeft(testId, startPosition);
		}
		nodeModel
				.setNumberOfNodesInTest(nodeModel.getNumberOfNodesInTest() - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#insertNode()
	 */
	@Override
	public void insertNode() {
		this.nodeModel.currentPosition++;
		DbQueries.shiftTestNodesRight(test.getIdTest(),
				this.nodeModel.currentPosition,
				this.nodeModel.getNumberOfNodesInTest());
		this.nodeModel.initNewNode();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#getPossibleLookback(int)
	 */
	@Override
	public String[] getPossibleLookback(int nodeNumber) {
		nodeToCheck = nodeModel.getNodeList().get(nodeNumber);
		int x = (int) DijkstraAlgorithm.getDistanceFromStartNode(nodeToCheck);
		x++;

		String[] look;
		if (x == -1 || x == 0) {
			look = new String[1];
			look[0] = "1";
			return look;
		} else {
			look = new String[x];
			for (int i = 0; i < x; i++) {
				look[i] = String.valueOf(i + 1);
			}
		}
		return look;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#getPossiblePoints(int)
	 */
	@Override
	public String[] getPossiblePoints(int positionNode1, int positionNode2,
			int lookback) {
		String[] points = new String[0];
		if (positionNode1 >= 0) {
			LinkedList<Integer> existingEdgesOut = DbQueries
					.getConditionOutgoingListByLookbackByNode(
							nodeModel.getNodeList().get(positionNode1).getIdNode(),
							lookback);

			LinkedList<String> toReturn = new LinkedList<String>();

			if (!existingEdgesOut.contains(-1)) {
				toReturn.add("D"); // Symbol fuer Standart-Bedingung
			}
			for (int i = 0; i <= lookback; i++) {
				int x = i * test.getMaxPoints();
				if (!existingEdgesOut.contains(x))
					toReturn.add(String.valueOf(x));

			}

			points = new String[toReturn.size()];
			for (int i = 0; i < toReturn.size(); i++) {
				points[i] = toReturn.get(i);
			}
		} else {
			// es handelt sich um den 1. knoten, alle bedingungen moeglich
			points = new String[3];
			points[0] = "D";
			points[1] = "0";
			points[2] = String.valueOf(test.getMaxPoints());
		}

		return points;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#setLookback(int)
	 */
	@Override
	public void setLookback(int lookback) {
		this.lookback = lookback;
		calcCuurrentConditionPoints();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#calculateEdge()
	 */
	@Override
	public void calculateEdge() {
		calcCurrentConditionLookback();
		calcCuurrentConditionPoints();
	}

	/**
	 * Berechnet die BedingungsPunkte-Liste unter Beruecksichtigung von bereits
	 * existierenden ausgehenden Kanten des Knotens sowie des Lookbacks.
	 */
	private void calcCuurrentConditionPoints() {
		if (currentConditionLookbackList == null) {
			currentConditionPointsList = new String[0];
		} else {
			if (!canInsertAnotherEdge(nodeModel.currentEdge.getIdNodeSource())) {
				currentConditionPointsList = new String[1];
				currentConditionPointsList[0] = "";

			} else {
				// moegliche bedingungspunkte
				LinkedList<Integer> tempPointList = new LinkedList<Integer>();
				tempPointList.add(-1);
				for (int i = 0; i <= lookback; i++) {
					tempPointList.add(i * test.getMaxPoints());
				}

				// werte die schon vergeben sind
				LinkedList<Integer> occupiedConditionPoints = DbQueries
						.getConditionOutgoingListByLookbackByNode(
								nodeModel.currentEdge.getIdNodeSource(),
								lookback);

				// streiche vergebene werte (ausser aktuellen)
				if (occupiedConditionPoints.size() > 0) {
					for (int i = 0; i < occupiedConditionPoints.size(); i++) {
						for (int j = 0; j < tempPointList.size(); j++) {
							if (occupiedConditionPoints.get(i) == tempPointList
									.get(j)
									&& occupiedConditionPoints.get(i) != nodeModel.currentCondition
											.getConditionPoints()) {
								tempPointList.remove(j);
							}
						}
					}

					// erstelle liste fuer uebergabe
					currentConditionPointsList = new String[tempPointList
							.size()];
					for (int i = 0; i < tempPointList.size(); i++) {
						if (tempPointList.get(i) == -1) {
							currentConditionPointsList[i] = "D";
						} else {
							currentConditionPointsList[i] = String
									.valueOf(tempPointList.get(i));
						}
					}
				}

			}

		}

	}

	/**
	 * Berechnet den moeglichen Lookback.
	 */
	private void calcCurrentConditionLookback() {
		int look = (int) DijkstraAlgorithm
				.getDistanceFromStartNode(nodeModel.currentNode);

		if (look == -1 || look == 0) {
			currentConditionLookbackList = null;
		} else {
			currentConditionLookbackList = new String[look + 1];
			for (int i = 0; i <= look; i++) {
				currentConditionLookbackList[i] = String.valueOf(i + 1);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#getPunkteBedingung()
	 */
	@Override
	public String[] getConditionPointsForField() {
		if (currentConditionPointsList == null) {
			String[] none = { "" };
			return none;
		}
		return currentConditionPointsList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#insertEdge(int, int, int, int)
	 */
	@Override
	public void insertEdge(int positionSourceInNodeList,
			int positionDestinationInNodeList, int conditionLookback,
			int conditionPoints) {
		int idSource = (nodeModel.getNodeList().get(positionSourceInNodeList))
				.getIdNode();
		int idDestination = (nodeModel.getNodeList()
				.get(positionDestinationInNodeList)).getIdNode();

		nodeModel.currentEdge = new Edge(Application.getCurrentProject());
		nodeModel.currentEdge.setIdNodeSource(idSource);
		nodeModel.currentEdge.setIdNodeDestination(idDestination);

		int idCondition = DbQueries.setConditionData(new Condition(Application.getCurrentProject(), 
				conditionPoints, conditionLookback));
		nodeModel.currentEdge.setIdCondition(idCondition);

		DbQueries.setEdgeData(nodeModel.currentEdge);

		// checkChildConditions(idDestination);

	}

	/**
	 * Ueberprueft, ob weitere Kanten eingefuegt werden koennen.
	 * 
	 * @param idNode
	 *            Die Knoten-ID.
	 */
	@SuppressWarnings("unused")
	private void checkChildConditions(int idNode) {
		Node node = DbQueries.getNodeData(idNode);

		// aktuell max. moeglicher Lookback
		int maxLook = (int) DijkstraAlgorithm.getDistanceFromStartNode(node) + 1;

		LinkedList<Edge> edges = DbQueries.getEdgeListByNode(idNode, true);
		LinkedList<Condition> conditions = new LinkedList<Condition>();

		for (int i = 0; i < edges.size(); i++) {
			conditions.add(DbQueries.getConditionData(edges.get(i)
					.getIdCondition()));
		}

		for (int i = 0; i < conditions.size(); i++) {
			// Fehlerfall: in bedingung gespeicherter lookback ist groesser als
			// aktuell moeglich
			if (conditions.get(i).getConditionLookback() > maxLook) {
				// ggf. anpassen der bedingungs-punkte
				int points = -1;
				if (conditions.get(i).getConditionPoints() > test
						.getMaxPoints()) {
					// anpassen
					int diff = conditions.get(i).getConditionLookback()
							- maxLook;
					points = conditions.get(i).getConditionPoints() - diff;
				} else {
					// uebernehmen (bei punkte == test.maxPunkte, 0 oder -1
					points = conditions.get(i).getConditionPoints();
				}
				// neue bedingung mit maxLook erstellen und in db einfuegen
				Condition newCondition = new Condition(Application.getCurrentProject(), points, maxLook);
				int idNewCondition = DbQueries.setConditionData(newCondition);
				// entsprechende neue kante
				Edge eNew = new Edge(Application.getCurrentProject(), edges.get(i).getIdNodeSource(), edges.get(
						i).getIdNodeDestination(), idNewCondition, DbQueries.getNodeData(edges.get(
								i).getIdNodeDestination()));
				// alte kante loeschen
				DbQueries.deleteKante(edges.get(i).getIdNodeSource(),
						conditions.get(i).getIdCondition());
				// neue einfuegen
				DbQueries.setEdgeData(eNew);

			}
		}

		for (int i = 0; i < edges.size(); i++) {
			checkChildConditions(edges.get(i).getIdNodeDestination());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#deleteEdge(int, int)
	 */
	@Override
	public void deleteEdge(int positionSourceInNodeList,
			int positionDestinationInNodeList) {
		int idSource = (nodeModel.getNodeList().get(positionSourceInNodeList))
				.getIdNode();
		int idDestination = (nodeModel.getNodeList()
				.get(positionDestinationInNodeList)).getIdNode();
		nodeModel.currentEdge.setIdNodeSource(idSource);
		nodeModel.currentEdge.setIdNodeDestination(idDestination);

		DbQueries.deleteEdge(nodeModel.currentEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#getType()
	 */
	@Override
	public int getType() {
		return this.type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#getLookbackBedingung()
	 */
	@Override
	public String[] getConditionLookbackForField(Node node) {
		int look = (int) DijkstraAlgorithm.getDistanceFromStartNode(node);

		if (look == -1 || look == 0) {
			String[] none = { "" };
			return none;
		} else {
			String[] result = new String[look];
			for (int i = 0; i < look; i++) {
				result[i] = String.valueOf(i + 1);
			}
			return result;
		}
		/*
		 * if (currentLookbackListe == null) { String[] none = { "" }; return
		 * none; }
		 */
		// return currentLookbackListe;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#updateEdge(int, int, int, int)
	 */
	@Override
	public void updateEdge(int positionSourceInNodeList,
			int positionDestinationInNodeList, int conditionPoints,
			int conditionLookback) {
		int idSource = (nodeModel.getNodeList().get(positionSourceInNodeList))
				.getIdNode();
		int idDestination = (nodeModel.getNodeList()
				.get(positionDestinationInNodeList)).getIdNode();
		Condition newCondition = new Condition(Application.getCurrentProject(), conditionPoints,
				conditionLookback);
		DbQueries.updateEdgeCondition(newCondition, idSource, idDestination);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#updateEdgeCondition()
	 */
	@Override
	public void updateEdgeCondition() {
		// unused
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see node.InsertAlgorithmus#canInsertAnotherEdge(int)
	 */
	@Override
	public boolean canInsertAnotherEdge(int id) {

		if (DbQueries.getConditionOutgoingListByLookbackByNode(id, 1).size() < MAX_EDGES_PER_NODE)
			return true;
		return false;
	}
}
