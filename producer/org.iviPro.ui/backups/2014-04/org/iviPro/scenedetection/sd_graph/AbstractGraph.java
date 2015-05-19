package org.iviPro.scenedetection.sd_graph;

import java.util.List;

/**
 * Abstract graph class to provide functionality with an arbitrary datatype. Is
 * used for video decomposition graph and temporal graph.
 * 
 * @author Stefan Zwicklbauer
 */
public abstract class AbstractGraph<T extends Comparable<T>> {

	protected List<Node<T>> nodeLst;

	private Node<T> startObject;

	private Node<T> endObject;
	
	public void setNode(T data) {
		nodeLst.add(new Node<T>(data));
	}

	public List<Node<T>> getNodeList() {
		return nodeLst;
	}

	public void removeNode(Node<T> node) {
		for (int i = 0; i < nodeLst.size(); i++) {
			Node<T> no = nodeLst.get(i);
			if (node.compareTo(no) == 0) {
				nodeLst.remove(i);
				break;
			}
		}
	}
	
	public Node<T> getStartObject() {
		return startObject;
	}

	public void setStartObject(Node<T> startObject) {
		this.startObject = startObject;
	}

	public Node<T> getEndObject() {
		return endObject;
	}

	public void setEndObject(Node<T> endObject) {
		this.endObject = endObject;
	}
	
	/**
	 * Sets the AlgorithmData object of all nodes in adjArray to null.
	 */
	abstract public void resetDijkstraData(); 	
}
