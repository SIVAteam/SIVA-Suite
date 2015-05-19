package org.iviPro.scenedetection.sd_graph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TemporalGraph<T extends Comparable<T>> extends AbstractGraph<T> {

	public TemporalGraph() {
		super();
		nodeLst = new LinkedList<Node<T>>();
	}

	public boolean checkConnection(T start, T end) {
		boolean backwardSearch = false;
		boolean exist = true;
		List<Node<T>> nodeLst = getNodeList();
		Node<T> startNode = null;
		Node<T> endNode = null;
		for (Iterator<Node<T>> iterator = nodeLst.iterator(); iterator
				.hasNext();) {
			Node<T> node = (Node<T>) iterator.next();
			if (node.getData().compareTo(start) == 0) {
				startNode = node;
			} else if (node.getData().compareTo(end) == 0) {
				endNode = node;
			}
		}

		Dijkstra<T> dijkstra = new Dijkstra<T>(this);

		// Edge between nodes must be invisible
		try {
			dijkstra.calculateRoute(startNode, endNode);
		} catch (NoRouteFoundException e) {
			backwardSearch = true;
		}

		// Backward way must be tested
		if (backwardSearch) {
			try {
				dijkstra.calculateRoute(endNode, startNode);
			} catch (NoRouteFoundException e) {
				exist = false;
			}
		}

		return exist;
	}

	public List<GraphContent<T>> getAllObjectsInCircle(T t, boolean coloring) {
		List<GraphContent<T>> endObjects = new LinkedList<GraphContent<T>>();
		for (Iterator<Node<T>> iterator1 = nodeLst.iterator(); iterator1
				.hasNext();) {
			Node<T> node = (Node<T>) iterator1.next();
			if (!node.getMarked() && node.getData().compareTo(t) == 0) {
				node.setMarked();
				if (coloring) {
					recursiveDepthFirstSearchColoring(node);
				} else {
					List<GraphContent<T>> unit = recursiveDepthFirstSearch(node);
					endObjects.addAll(unit);
				}
			}
		}
		return endObjects;
	}

	private void recursiveDepthFirstSearchColoring(Node<T> start) {
		List<Edge<T>> edgeLst = start.getOutgoingEdges();
		start.setMarked();
		for (Iterator<Edge<T>> iterator = edgeLst.iterator(); iterator
				.hasNext();) {
			Edge<T> edge = (Edge<T>) iterator.next();
			if (!edge.getVisibleStatus()) {
				Node<T> target = edge.getEndNode();
				// For further explanation look at algorithm in master thesis
				if (!target.getMarked()
						&& (((target.getType() == NodeTypes.WHITE)
								|| (target.getType() == NodeTypes.GREY) || (target
								.getType() == NodeTypes.RED)) || (start
								.getType() == NodeTypes.BLACK && target
								.getType() == NodeTypes.BLACK))) {
					if (target.getType() == NodeTypes.GREY) {
						target.setType(NodeTypes.RED);
					} else if (target.getType() == NodeTypes.WHITE) {
						target.setType(NodeTypes.GREY);
					}
					recursiveDepthFirstSearchColoring(target);
				}
			}
		}
	}

	private List<GraphContent<T>> recursiveDepthFirstSearch(Node<T> start) {
		List<GraphContent<T>> objects = new LinkedList<GraphContent<T>>();
		List<Edge<T>> edgeLst = start.getOutgoingEdges();
		boolean ambiguous = false;
		if (start.getType() == NodeTypes.RED) {
			ambiguous = true;
		}
		objects.add(new GraphContent<T>(start.getData(), ambiguous));
		start.setMarked();
		for (Iterator<Edge<T>> iterator = edgeLst.iterator(); iterator
				.hasNext();) {
			Edge<T> edge = (Edge<T>) iterator.next();
			if (!edge.getVisibleStatus()) {
				Node<T> target = edge.getEndNode();

				// For further explanation look at algorithm in master thesis
				if (!target.getMarked()
						&& (((target.getType() == NodeTypes.WHITE)
								|| (target.getType() == NodeTypes.GREY) || target
								.getType() == NodeTypes.RED) || (start
								.getType() == NodeTypes.BLACK && target
								.getType() == NodeTypes.BLACK))) {
					objects.addAll(recursiveDepthFirstSearch(target));
				}
			}
		}
		return objects;
	}

	public void unmarkAll() {
		for (Iterator<Node<T>> iterator = nodeLst.iterator(); iterator
				.hasNext();) {
			Node<T> type = (Node<T>) iterator.next();
			type.setUnmarked();
		}
	}

	public void changeVisibility(T t1, T t2, boolean visible) {
		for (Iterator<Node<T>> iterator = nodeLst.iterator(); iterator
				.hasNext();) {
			Node<T> node = (Node<T>) iterator.next();
			if (node.getData().compareTo(t1) == 0) {
				List<Edge<T>> edgeLst = node.getOutgoingEdges();
				for (Iterator<Edge<T>> iterator2 = edgeLst.iterator(); iterator2
						.hasNext();) {
					Edge<T> edge = (Edge<T>) iterator2.next();
					if (edge.getEndNode().getData().compareTo(t2) == 0) {
						if (visible) {
							edge.setVisible();
						} else {
							edge.setInvisible();
						}
					}
				}
			}
		}
	}

	public void markShortestPath(List<T> path) {
		for (Iterator<Node<T>> iterator = nodeLst.iterator(); iterator
				.hasNext();) {
			Node<T> t = (Node<T>) iterator.next();
			for (Iterator<T> iterator2 = path.iterator(); iterator2.hasNext();) {
				T t2 = (T) iterator2.next();
				if (t.getData().compareTo(t2) == 0) {
					t.setType(NodeTypes.BLACK);
				}
			}
		}
	}

	@Override
	public void resetDijkstraData() {
		for (Iterator<Node<T>> iterator = nodeLst.iterator(); iterator
				.hasNext();) {
			Node<T> node = (Node<T>) iterator.next();
			node.setDijkstraData(null);
		}
	}
}
