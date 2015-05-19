package org.iviPro.editors.quiz.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.Edge;
import org.iviPro.model.quiz.Node;

/**
 * Implementierung des Dijkstra-Algorithmus als Hilfsfunktion zum Berechnen des
 * Lookbacks bei dynamischen Tests.
 * 
 * @author Sabine Gattermann
 * 
 */
public class DijkstraAlgorithm {

    private static LinkedList<Node> vertices;

    /**
     * Konstruktor
     */
    public DijkstraAlgorithm() {
    }

    /**
     * Berechnet minimale Distanz zwischen Startknoten und angegebenen
     * Zielknoten.
     * 
     * @param toNode
     *            Der Zielknoten.
     * @return Die Distanz.
     */
    public static double getDistanceFromStartNode(Node toNode) {
	refresh(toNode.getIdTest());
	int i = 0;
	while (toNode.getIdNode() != vertices.get(i).getIdNode())
	    i++;
	double res = vertices.get(i).getMinDistance();
	if (res == Double.POSITIVE_INFINITY)
	    res = -1;
	return res;
    }

    /**
     * Baut den Graphen auf.
     * 
     * @param idTest
     *            Die Test-ID.
     */
    private static void refresh(int idTest) {
	vertices = DbQueries.getNodeListByTest(idTest);

	for (int i = 0; i < vertices.size(); i++) {
	    LinkedList<Edge> edgeList = DbQueries.getEdgeListByNode(vertices
		    .get(i).getIdNode(), true);
	    for (int e = 0; e < edgeList.size(); e++) {
		int j = 0;
		while (edgeList.get(e).getIdNodeDestination() != vertices
			.get(j).getIdNode()) {
		    j++;
		}

		edgeList.get(e).setDestination(vertices.get(j));
	    }
	    vertices.get(i).setAdjacencies(edgeList);
	}
	if (vertices.size() > 0)
	    computePaths(vertices.get(0));

    }

    /**
     * Berechnet die Pfade.
     * 
     * @param source
     *            Startknoten.
     */
    private static void computePaths(Node source) {
	source.setMinDistance(0.0);
	PriorityQueue<Node> vertexQueue = new PriorityQueue<Node>();
	vertexQueue.add(source);

	while (!vertexQueue.isEmpty()) {
	    Node u = vertexQueue.poll();

	    // Visit each edge exiting u
	    for (int i = 0; i < u.getAdjacencies().size(); i++) {
		Edge e = u.getAdjacencies().get(i);
		Node v = e.getDestination();
		double weight = e.weight;
		double distanceThroughU = u.getMinDistance() + weight;
		if (distanceThroughU < v.getMinDistance()) {
		    vertexQueue.remove(v);
		    v.setMinDistance(distanceThroughU);
		    v.setPrevious(u);
		    vertexQueue.add(v);
		}
	    }
	}
    }

    /**
     * Liefert Liste kürzester Pfade.
     * 
     * @param target
     *            Zielknoten.
     * @return Die Liste kürzester Pfade.
     */
    public static List<Node> getShortestPathTo(Node target) {
	List<Node> path = new ArrayList<Node>();
	for (Node vertex = target; vertex != null; vertex = vertex
		.getPrevious()) {
	    path.add(vertex);
	}

	Collections.reverse(path);
	return path;
    }

}
