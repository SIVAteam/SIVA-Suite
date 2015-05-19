package org.iviPro.scenedetection.sd_algorithm;

import java.util.Iterator;
import java.util.List;

import org.iviPro.scenedetection.sd_graph.Node;
import org.iviPro.scenedetection.sd_graph.TemporalGraph;
import org.iviPro.scenedetection.sd_main.Shot;

public class TemporalGraphGeneration {

	private TemporalGraph<Cluster> graph;

	private List<Cluster> clusterLst;

	public TemporalGraphGeneration(List<Cluster> clusterLst) {
		this.clusterLst = clusterLst;
		this.graph = new TemporalGraph<Cluster>();
	}

	public TemporalGraph<Cluster> startTemporalGraphGeneration() {
		// Set graph nodes
		createGraphNodes();

		List<Node<Cluster>> nodeLst = graph.getNodeList();
		Node<Cluster> startCluster = getFirst();
		Node<Cluster> endCluster = getLast();
		checking(startCluster.getData());
		checking(endCluster.getData());
		for (Iterator<Node<Cluster>> iterator = nodeLst.iterator(); iterator
				.hasNext();) {
			Node<Cluster> node = (Node<Cluster>) iterator.next();
			Cluster sourceCluster = node.getData();
			for (Iterator<Node<Cluster>> iterator2 = nodeLst.iterator(); iterator2
					.hasNext();) {
				Node<Cluster> targetNode = (Node<Cluster>) iterator2.next();
				Cluster targetCluster = targetNode.getData();
				if (sourceCluster.clusterHasLinkTo(targetCluster)) {
					targetNode.addAdjacencyNode(node, 1);
				}
			}
		}
		graph.setStartObject(startCluster);
		graph.setEndObject(endCluster);
		return graph;
	}

	private void createGraphNodes() {
		for (Iterator<Cluster> iterator = clusterLst.iterator(); iterator
				.hasNext();) {
			Cluster cluster = (Cluster) iterator.next();
			graph.setNode(cluster);
		}
	}

	private float calcTransitionWeight(Cluster source, Cluster target) {
		float sum = 0f;
		List<Shot> sourceShots = source.getShotList();
		List<Shot> targetShots = target.getShotList();
		for (Iterator<Shot> iterator = sourceShots.iterator(); iterator
				.hasNext();) {
			Shot sourceShot = (Shot) iterator.next();
			for (Iterator<Shot> iterator2 = targetShots.iterator(); iterator2
					.hasNext();) {
				Shot targetShot = (Shot) iterator2.next();
				if ((sourceShot.getShotId() + 1) == targetShot.getShotId()) {
					sum++;
				}
			}
		}
		return (sum / targetShots.size());
	}

	// Testing
	public void checking(Cluster cluster) {
		List<Shot> shotLst = cluster.getShotList();
		for (Iterator<Shot> iterator = shotLst.iterator(); iterator.hasNext();) {
			Shot shot = (Shot) iterator.next();
			System.out.println("shotid: " + shot.getShotId());
		}
		System.out.println("-------------------------------------------------");
	}

	private Node<Cluster> getFirst() {
		List<Node<Cluster>> lst = graph.getNodeList();
		Node<Cluster> result = lst.get(0);
		for (Iterator<Node<Cluster>> iterator = lst.iterator(); iterator
				.hasNext();) {
			Node<Cluster> node = (Node<Cluster>) iterator.next();
			Cluster cluster = node.getData();
			List<Shot> shotList = cluster.getShotList();
			for (Iterator<Shot> iterator2 = shotList.iterator(); iterator2
					.hasNext();) {
				Shot shot = (Shot) iterator2.next();
				if (shot.getShotId() == 0) {
					result = node;
					System.out.println("StartId" + shot.getShotId());
					break;
				}
			}
		}
		return result;
	}

	private Node<Cluster> getLast() {
		List<Node<Cluster>> lst = graph.getNodeList();
		Node<Cluster> result = lst.get(0);
		int id = 0;
		for (Iterator<Node<Cluster>> iterator = lst.iterator(); iterator
				.hasNext();) {
			Node<Cluster> node = (Node<Cluster>) iterator.next();
			Cluster cluster = node.getData();
			List<Shot> shotList = cluster.getShotList();
			for (Iterator<Shot> iterator2 = shotList.iterator(); iterator2
					.hasNext();) {
				Shot shot = (Shot) iterator2.next();
				if (shot.getShotId() > id) {
					result = node;
					id = shot.getShotId();
					System.out.println("EndIdHighest" + shot.getShotId());
				}
			}
		}
		return result;
	}
}
