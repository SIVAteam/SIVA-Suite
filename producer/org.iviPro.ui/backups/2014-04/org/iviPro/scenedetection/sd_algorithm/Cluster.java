package org.iviPro.scenedetection.sd_algorithm;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import org.iviPro.scenedetection.sd_graph.Edge;
import org.iviPro.scenedetection.sd_graph.Node;
import org.iviPro.scenedetection.sd_graph.UndirectedWeightedShotGraph;
import org.iviPro.scenedetection.sd_main.Shot;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

public class Cluster implements Comparable<Cluster> {

	private List<Shot> shotList;

	private UndirectedWeightedShotGraph<Shot> graph;

	private NCutMatrix weightMatrix;

	private NCutMatrix dMatrix;

	private NCutMatrix matrix;

	Cluster(List<Shot> shotList, UndirectedWeightedShotGraph<Shot> graph) {
		this.shotList = shotList;
		this.graph = graph;
		this.weightMatrix = createMatrix();
		this.dMatrix = createDMatrix();
		this.matrix = calcFinalMatrix();
//		for (int i = 0; i < shotList.size(); i++) {
//			System.out.println("In diesem Cluster Shot Nr: "+shotList.get(i).getShotId());
//		}
	}

	NCutMatrix getMatrix() {
		return matrix;
	}

	public List<Shot> getShotList() {
		return shotList;
	}

	private NCutMatrix createMatrix() {
		double[][] values = new double[shotList.size()][shotList.size()];
//		System.out.println("SIZE: "+shotList.size());
		int xkey = 0;
		int ykey = 0;
		List<Node<Shot>> lst = graph.getNodeList();
		for (Iterator<Node<Shot>> iterator = lst.iterator(); iterator.hasNext();) {
			Node<Shot> node = (Node<Shot>) iterator.next();
			int matchingId = isInShotList(node.getData());
			ykey = 0;
			if (matchingId != -1) {
				ConcurrentSkipListMap<Integer, Edge<Shot>> map = node
						.getEdgeMap();
				for (Map.Entry<Integer, Edge<Shot>> entry : map.entrySet()) {
					Edge<Shot> edge = entry.getValue();
					int innerMatchingId = isInShotList(edge.getEndNode()
							.getData());
					if (innerMatchingId != -1) {
						values[xkey]
						       [ykey] = edge.getWeight();
						ykey++;
					}
				}
				xkey++;
			}
		}
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values[i].length; j++) {
//				System.out.println("CurrentVal: "+values[i][j]);
			}
		}
		NCutMatrix matrix = new NCutMatrix(values);

//		System.out.println("------------------------------------------------------------------------");
		return matrix;
	}

	private int isInShotList(Shot shot) {
		int id = -1;
		for (Iterator<Shot> iterator = shotList.iterator(); iterator.hasNext();) {
			Shot currentShot = (Shot) iterator.next();
			if (currentShot.compareTo(shot) == 0) {
				id = currentShot.getShotId();
				break;
			}
		}
		return id;
	}

	private NCutMatrix createDMatrix() {
		double[][] values = new double[shotList.size()][shotList.size()];

		for (int i = 0; i < shotList.size(); i++) {
			for (int j = 0; j < shotList.size(); j++) {
				values[i][j] = 0;
				if (i == j) {
					double sum = 0;
					for (int j2 = 0; j2 < shotList.size(); j2++) {
						sum += weightMatrix.getQuick(i, j2);
					}
					values[i][j] = sum;
				}
			}
		}

		NCutMatrix dMatrix = new NCutMatrix(values);
		return dMatrix;
	}

	private NCutMatrix calcFinalMatrix() {
		NCutMatrix dSQRT = dMatrix.SQRTMatrix();
		NCutMatrix subtraction = dMatrix.subtractMatrix(weightMatrix);
		NCutMatrix product = mult(dSQRT, subtraction);
		NCutMatrix result = mult(product, dSQRT);

		return result;
	}

	private NCutMatrix mult(NCutMatrix matrix1, NCutMatrix matrix2) {
		DoubleMatrix2D result = Algebra.DEFAULT.mult(matrix1, matrix2);
		double[][] values = new double[matrix1.rows()][matrix1.columns()];
		for (int i = 0; i < result.rows(); i++) {
			for (int j = 0; j < result.columns(); j++) {
				values[i][j] = result.getQuick(i, j);
			}
		}
		return new NCutMatrix(values);
	}

	double calcAverage() {
		double sum = 0;
		int amount = 0;
		double[][] values = weightMatrix.getValues();
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < i + 1; j++) {
//				if (i != j) {
					sum += values[i][j];
					amount++;
//					System.out.println("sum: "+sum + "Value: "+values[i][j]);
//				}
			}
		}
//		System.out.println("--------------------------------------------------------------");
//		System.out.println("Summe:"+sum+" Amount: "+amount);
		if(amount == 0) {
			return sum;
		} else {
			return (sum / amount);
		}
	}

	double calcStandardDeviation(double average) {
		double sum = 0;
		double[][] values = weightMatrix.getValues();
		int amount = 0;
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < i + 1; j++) {
//				if (i != j) {
					sum += Math.pow(Math.abs((average - values[i][j])), 2);
					amount++;
				}
//			}
		}
		if(amount == 0) { 
//			sum = (sum / amount);
			return Math.sqrt(sum);
		} else {
			sum = (sum / amount);
			return Math.sqrt(sum);
		}
	}

	Cluster createSubCluster(List<Shot> shotList) {
		return new Cluster(shotList, graph);
	}

	boolean isOverThreshold(double threshold) {
		double[][] values = weightMatrix.getValues();
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values[i].length; j++) {
				if (values[i][j] < threshold) {
					return false;
				}
			}
		}
		return true;
	}

	void sortClusterShots() {
		Collections.sort(shotList);
	}

	boolean clusterHasLinkTo(Cluster target) {
		List<Shot> targetLst = target.getShotList();
		for (Iterator<Shot> iterator = shotList.iterator(); iterator.hasNext();) {
			Shot shot = (Shot) iterator.next();
			for (Iterator<Shot> iterator2 = targetLst.iterator(); iterator2
					.hasNext();) {
				Shot targetShot = (Shot) iterator2.next();
				if (shot.getShotId() == (targetShot.getShotId() + 1)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int compareTo(Cluster o) {
		if(shotList.size() != o.getShotList().size()) {
			return 1;
		}
		for (int i = 0; i < shotList.size(); i++) {
			if (shotList.get(i).compareTo(o.getShotList().get(i)) == 0) {
				return 0;
			}
		}
		return 1;
	}
	
	/////////////////////////////////////////////////// Methods for ShotWeave Algorithm ///////////////////////////////////////
	public double getMatchingValue(Shot shot1, Shot shot2) {
		List<Node<Shot>> lst = graph.getNodeList(shotList);
		for (Iterator<Node<Shot>> iterator = lst.iterator(); iterator.hasNext();) {
			Node<Shot> node = (Node<Shot>) iterator.next();
			if(node.getData().compareTo(shot1) != 0) {
				ConcurrentSkipListMap<Integer, Edge<Shot>> map = node.getEdgeMap();
				for (Map.Entry<Integer, Edge<Shot>> entry : map.entrySet()) {
					Edge<Shot> edge = entry.getValue();
					if(edge.getEndNode().getData().compareTo(shot2) != 0) {
						return edge.getWeight();
					}
				}
			}
		}
		return -1;
	}
}
