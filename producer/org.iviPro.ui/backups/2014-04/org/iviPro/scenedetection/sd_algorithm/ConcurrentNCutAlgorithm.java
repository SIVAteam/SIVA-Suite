package org.iviPro.scenedetection.sd_algorithm;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.iviPro.scenedetection.sd_main.Shot;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;

public class ConcurrentNCutAlgorithm {

	private static final int SPLITTINGPOINT = 0;

	private Cluster mainCluster;
	
	private double threshold;
	
private double average;

private double standard;

	public ConcurrentNCutAlgorithm(Cluster mainCluster) {
		this.mainCluster = mainCluster;
		average = mainCluster.calcAverage();
		standard = mainCluster.calcStandardDeviation(average);
		this.threshold = average + (standard / 2) ;
	}

	public List<Cluster> startClustering() {
		List<Cluster> clusters = recursive2WayNCut(mainCluster);
		return clusters;
	}

	private List<Cluster> recursive2WayNCut(Cluster main) {
		List<Cluster> leafClusters = new LinkedList<Cluster>();
//		double average = main.calcAverage();
//		double temp = ((Math.exp(Math.E))/2.5 - average);
//		if(temp < 0) {
//			temp = 1 / Math.abs(temp);
//		}
//		double threshold = average - 1 / temp;
//		double threshold = average + main.calcStandardDeviation(average);
//		System.out.println("Average: "+average);
//		System.out.println("StandardVariation: "+main.calcStandardDeviation(average));
//		System.out.println("THRESHOLD :"+threshold);
//		System.out.println("THRESHOLD :"+threshold);
//		System.out.println("--------------------------------------------------------");
		if (main.isOverThreshold(threshold) || main.getShotList().size() == 1) {
			leafClusters.add(main);
			return leafClusters;
		} else {
			NCutMatrix matrix = main.getMatrix();
			EigenvalueDecomposition lanczos = new EigenvalueDecomposition(
					matrix);

			DoubleMatrix1D eigenvalues = lanczos.getRealEigenvalues();
			DoubleMatrix2D eigenvectors = lanczos.getV();
			DoubleMatrix1D useableEigenvector = getEigenvectorOfEigenValue(
					eigenvalues, eigenvectors);
			Cluster[] subClusters = splitUp(main, useableEigenvector);
			leafClusters.addAll(recursive2WayNCut(subClusters[0]));
			leafClusters.addAll(recursive2WayNCut(subClusters[1]));
			return leafClusters;
		}
	}

	private Cluster[] splitUp(Cluster main, DoubleMatrix1D eigenvector) {
		Cluster[] clusters = new Cluster[2];
		List<Shot> lst = main.getShotList();
		List<Shot> a = new LinkedList<Shot>();
		List<Shot> b = new LinkedList<Shot>();
		for (int i = 0; i < lst.size(); i++) {
			double vecVal = eigenvector.getQuick(i);
			if (vecVal < SPLITTINGPOINT) {
				a.add(lst.get(i));
			} else {
				b.add(lst.get(i));
			}
		}
		clusters[0] = main.createSubCluster(a);
		clusters[1] = main.createSubCluster(b);
		return clusters;
	}

	private DoubleMatrix1D getEigenvectorOfEigenValue(
			DoubleMatrix1D eigenvalues, DoubleMatrix2D eigenvectors) {
		// Sort eigenvalues to get the second smallest eigenvalue
		double[] evArr = new double[eigenvalues.size()];
		for (int i = 0; i < eigenvalues.size(); i++) {
			evArr[i] = eigenvalues.getQuick(i);
		}
		Arrays.sort(evArr);

		// Get second smallest eigenvalue
		double ssEigenvalue = evArr[1];
		int position = 0;
		for (int i = 0; i < eigenvalues.size(); i++) {
			if (ssEigenvalue == eigenvalues.getQuick(i)) {
				position = i;
				break;
			}
		}
		return eigenvectors.viewColumn(position);
	}
}
