package org.iviPro.scenedetection.sd_graph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class UndirectedWeightedShotGraph<T extends Comparable<T>> extends AbstractGraph<T> {

	public UndirectedWeightedShotGraph() {
		super();
		nodeLst = new LinkedList<Node<T>>();
	}

	public List<Node<T>> getNodeList(List<T> lst) {
		List<Node<T>> result = new LinkedList<Node<T>>();
		for (Iterator<T> iterator = lst.iterator(); iterator.hasNext();) {
			T t = (T) iterator.next();
			for (int i = 0; i < nodeLst.size(); i++) {
				if (t.compareTo(nodeLst.get(i).getData()) == 0) {
					result.add(nodeLst.get(i));
				}
			}
		}
		return result;
	}

	@Override
	public void resetDijkstraData() {
	}
}
