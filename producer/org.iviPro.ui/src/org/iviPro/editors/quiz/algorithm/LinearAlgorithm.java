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
 * Diese Klasse implementiert einen dynamischen Einfuege-Algorithmus. Jeder
 * Knoten kann nur einen Nachfolger haben. Die Kanten haben stets die
 * Dummy-Bedingung (Standart).
 * 
 * @author Sabine Gattermann
 * 
 */
public class LinearAlgorithm implements InsertAlgorithm {

    private Condition dummyCondition;
    private String[] pointsList;
    private Test test;
    private NodeModel nodeModel;
    private int type;

    /**
     * Konstruktor
     * 
     * @param nodeModel
     *            Die aufrufende Model-Klasse.
     * @param test
     *            Der Test.
     */
    public LinearAlgorithm(NodeModel nodeModel, Test test) {
	this.type = 0;
	this.test = test;
	this.nodeModel = nodeModel;
	init();
    }

    /**
     * Initialisierung.
     */
    private void init() {
	initPunkteListe();
	initDummyCondition();
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

    /*
     * (non-Javadoc)
     * 
     * @see node.InsertAlgorithmus#insertNode()
     */
    @Override
    public void insertNode() {

	LinkedList<Edge> out = DbQueries.getEdgeListByNode(
		this.nodeModel.currentNode.getIdNode(), true);
	if (out.size() > 0) {
	    Edge edgeOut = out.get(0);
	    int oldCurrentNodeId = edgeOut.getIdNodeSource();
	    int oldNextNodeId = edgeOut.getIdNodeDestination();
	    this.nodeModel.currentPosition++;

	    DbQueries.shiftTestNodesRight(test.getIdTest(),
		    this.nodeModel.currentPosition, this.nodeModel
			    .getNumberOfNodesInTest());
	    this.nodeModel.initNewNode();

	    DbQueries.deleteEdge(edgeOut);

	    DbQueries.setEdgeData(new Edge(Application.getCurrentProject(), oldCurrentNodeId,
		    this.nodeModel.currentNode.getIdNode(), dummyCondition
			    .getIdCondition(), DbQueries.getNodeData(dummyCondition
					    .getIdCondition())));
	    DbQueries.setEdgeData(new Edge(Application.getCurrentProject(), this.nodeModel.currentNode
		    .getIdNode(), oldNextNodeId, dummyCondition
		    .getIdCondition(), DbQueries.getNodeData(oldNextNodeId)));

	} else {
	    this.nodeModel.currentPosition++;
	    this.nodeModel.initNewNode();
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

    /**
     * Initialisierung der Punkte-Auswahl-Liste von 0 bis maxPunkte.
     */
    private void initPunkteListe() {
	this.pointsList = new String[test.getMaxPoints()];
	for (int i = 1; i <= test.getMaxPoints(); i++) {
	    pointsList[i - 1] = String.valueOf(i);
	}
    }

    /**
     * Initialisierung der Dummy-Bedingung.
     */
    private void initDummyCondition() {
	this.dummyCondition = new Condition(Application.getCurrentProject(), -1, 1);
	dummyCondition.setIdCondition(DbQueries
		.setConditionData(dummyCondition));
    }

    /*
     * (non-Javadoc)
     * 
     * @see node.InsertAlgorithmus#updateEdgeCondition()
     */
    @Override
    public void updateEdgeCondition() {
	Condition condition = new Condition(Application.getCurrentProject(), -1, 1);
	int id = DbQueries.setConditionData(condition);
	condition.setIdCondition(id);

	DbQueries.updateEdgeCondition(condition, nodeModel.idNodePredecessor,
		nodeModel.currentNode.getIdNode());

    }

    /*
     * (non-Javadoc)
     * 
     * @see node.InsertAlgorithmus#deleteNode()
     */
    @Override
    public void deleteNode() {

	if (nodeModel.getNumberOfNodesInTest() - nodeModel.currentPosition == 1) {
	    // letzter Knoten
	    DbQueries.deleteNode(nodeModel.currentNode.getIdNode());
	    nodeModel.currentPosition--;
	} else if (nodeModel.currentPosition == 0) {
	    // erster Knoten
	    DbQueries.deleteNode(nodeModel.currentNode.getIdNode());
	    DbQueries.shiftTestNodesLeft(
		    this.nodeModel.currentNode.getIdTest(),
		    this.nodeModel.currentPosition);

	} else {
	    // Knoten mit Vorgaenger und Nachfolger
	    LinkedList<Edge> in = DbQueries.getEdgeListByNode(
		    nodeModel.currentNode.getIdNode(), false);
	    LinkedList<Edge> out = DbQueries.getEdgeListByNode(
		    nodeModel.currentNode.getIdNode(), true);
	    int vorgaengerId = in.get(0).getIdNodeSource();
	    int nachfolgerId = out.get(0).getIdNodeDestination();
	    // neue Kante
	    Edge newEdge = new Edge(Application.getCurrentProject(), vorgaengerId, nachfolgerId, dummyCondition
		    .getIdCondition(), DbQueries.getNodeData(nachfolgerId));
	    // Knoten loeschen
	    DbQueries.deleteNode(nodeModel.currentNode.getIdNode());
	    // shiftLeft
	    DbQueries.shiftTestNodesLeft(
		    this.nodeModel.currentNode.getIdTest(),
		    this.nodeModel.currentPosition);
	    // neue Kante schreiben
	    DbQueries.setEdgeData(newEdge);

	}
	nodeModel
		.setNumberOfNodesInTest(nodeModel.getNumberOfNodesInTest() - 1);
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

    // folgende Interface Methoden werden nicht benoetigt und sind deshalb nicht
    // implementiert!

    /*
     * (non-Javadoc)
     * 
     * @see node.InsertAlgorithmus#insertEdge(int, int, int, int)
     */
    @Override
    public void insertEdge(int positionSourceInNodeList,
	    int positionDestinationInNodeList, int conditionLookback,
	    int conditionPoints) {
	// not used
    }

    /*
     * (non-Javadoc)
     * 
     * @see node.InsertAlgorithmus#setEdgeNumbersForGui(int, int,
     * utilities.beans.Kante)
     */
    @Override
    public void setEdgeNumbersForGui(int from, int to, Edge edge) {
	// not used
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
	// not used
    }

    /*
     * (non-Javadoc)
     * 
     * @see node.InsertAlgorithmus#getPossibleLookback(int)
     */
    @Override
    public String[] getPossibleLookback(int nodeNumber) {
	// not used
	String[] dummy = new String[0];
	return dummy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see node.InsertAlgorithmus#getEdgeNumbersForGui()
     */
    @Override
    public LinkedList<String> getEdgeNumbersForGui() {
	// not used
	LinkedList<String> dummy = new LinkedList<String>();
	return dummy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see node.InsertAlgorithmus#getLookbackBedingung()
     */
    @Override
    public String[] getConditionLookbackForField(Node knoten) {
	// not used
	String[] dummy = new String[0];
	return dummy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see node.InsertAlgorithmus#deleteEdge(int, int)
     */
    @Override
    public void deleteEdge(int positionSourceInNodeList,
	    int positionDestinationInNodeList) {
	// not used
    }

    /*
     * (non-Javadoc)
     * 
     * @see node.InsertAlgorithmus#getPossiblePoints(int)
     */
    @Override
    public String[] getPossiblePoints(int positionSource,
	    int positionDestination, int lookback) {
	// not used
	String[] dummy = new String[0];
	return dummy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see node.InsertAlgorithmus#calculateEdge()
     */
    @Override
    public void calculateEdge() {
	// not used
    }

    /*
     * (non-Javadoc)
     * 
     * @see node.InsertAlgorithmus#getPunkteBedingung()
     */
    @Override
    public String[] getConditionPointsForField() {
	// not used
	String[] dummy = new String[0];
	return dummy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see node.InsertAlgorithmus#setLookback(int)
     */
    @Override
    public void setLookback(int lookback) {
	// not used
    }

    /*
     * (non-Javadoc)
     * 
     * @see node.InsertAlgorithmus#canInsertAnotherEdge(int)
     */
    @Override
    public boolean canInsertAnotherEdge(int id) {
	// linear kann immer eingefuegt werden
	return true;
    }

}
