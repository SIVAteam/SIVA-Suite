package org.iviPro.editors.quiz.std;

import java.util.LinkedList;

import org.iviPro.model.quiz.Condition;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.Edge;
import org.iviPro.model.quiz.Node;
import org.iviPro.model.quiz.Test;

/**
 * Diese Klasse dient zur Ueberpruefung der Konsistenz eines Tests bevor dessen
 * Bearbeitung abgeschlossen werden kann.
 * 
 * @author Sabine Gattermann
 * 
 */
public class NodeModelHelpClass {

	private Test test;
	private int idDefaultCondition;

	/**
	 * Konstruktor
	 */
	public NodeModelHelpClass() {
	}

	/**
	 * Ueberpruefung der Konsistenz
	 * 
	 * @param testToCheck
	 *            Der Test.
	 * @return 0, falls okay, Knoten-ID im Fehlerfall
	 */
	public int checkConsistence(Test testToCheck) {
		this.test = testToCheck;
		boolean okay = true;
		int index = 0;

		LinkedList<Node> nodeList = DbQueries.getNodeListByTest(test
				.getIdTest());
		idDefaultCondition = DbQueries.getConditionId(1, -1);

		// Anzahl Knoten ohne eingehende Kante
		int numberOfNodesWithoutEdgesIn = 0;

		for (int i = 0; i < nodeList.size(); i++) {
			LinkedList<Edge> edgeListIn = DbQueries.getEdgeListByNode(nodeList
					.get(i).getIdNode(), false);

			if (edgeListIn.size() == 0) {
				if (numberOfNodesWithoutEdgesIn == 1) {
					return nodeList.get(i).getIdNode();
				}
				numberOfNodesWithoutEdgesIn++;
			}
		}

		while (okay && index < nodeList.size()) {
			LinkedList<Edge> edgeListOut = new LinkedList<Edge>();
			edgeListOut.addAll(DbQueries.getEdgeListByNode(nodeList.get(index)
					.getIdNode(), true));

			// teste als erstes auf default-bedingung
			if (!containsDefaultId(edgeListOut)) {
				if (!checkByLookback(edgeListOut))
					return nodeList.get(index).getIdNode();
			}

			index++;
		}

		return 0;
	}

	/**
	 * Testet die Konsistenz der nicht-default Bedingungen von ausgehenden
	 * Kanten eines Knotens
	 * 
	 * @param edgeListOut
	 *            Die KantenListe (ausgehend).
	 * @return true, falls konsistent, false sonst.
	 */
	private boolean checkByLookback(LinkedList<Edge> edgeListOut) {
		LinkedList<Condition> conditionList = new LinkedList<Condition>();
		for (int i = 0; i < edgeListOut.size(); i++) {
			conditionList.add(DbQueries.getConditionData(edgeListOut.get(i)
					.getIdCondition()));
		}

		while (conditionList.size() > 0) {
			Condition condition = conditionList.poll();
			int tmpLookback = condition.getConditionLookback();
			int numberOfConditionsForConsistency = tmpLookback;

			for (int i = 0; i < conditionList.size(); i++) {
				if (conditionList.get(i).getConditionLookback() == tmpLookback) {
					numberOfConditionsForConsistency--;
					conditionList.remove(i);
				}
			}

			if (numberOfConditionsForConsistency > 0) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Testet, ob ein Knoten eine ausgehende Default-Bedingungskante hat.
	 * 
	 * @param edgeListOut
	 *            Die Kantenliste (ausgehend).
	 * @return true, falls Default-Bedingung gefunden, false sonst.
	 */
	private boolean containsDefaultId(LinkedList<Edge> edgeListOut) {
		for (int i = 0; i < edgeListOut.size(); i++) {
			if (edgeListOut.get(i).getIdCondition() == idDefaultCondition) {
				return true;
			}
		}
		return false;
	}
}
