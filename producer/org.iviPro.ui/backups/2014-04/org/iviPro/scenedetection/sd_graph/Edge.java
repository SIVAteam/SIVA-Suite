package org.iviPro.scenedetection.sd_graph;

public class Edge<T extends Comparable<T>> {

	private Node<T> endNode;

	private float weight;
	
	private boolean invisible;

	public Edge(Node<T> node, float weight) {
		this.endNode = node;
		this.weight = weight;
		this.invisible = false;
	}

	public float getWeight() {
		return weight;
	}

	float getDijkstraWeight() {
		return 1f;
	}

	public Node<T> getEndNode() {
		return endNode;
	}
	
	void setVisible() {
		invisible = false;
	}
	
	void setInvisible() {
		invisible = true;
	}
	
	boolean getVisibleStatus() {
		return invisible;
	}
}
