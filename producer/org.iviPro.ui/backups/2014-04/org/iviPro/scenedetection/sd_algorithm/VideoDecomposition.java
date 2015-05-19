package org.iviPro.scenedetection.sd_algorithm;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.iviPro.scenedetection.sd_graph.Dijkstra;
import org.iviPro.scenedetection.sd_graph.Edge;
import org.iviPro.scenedetection.sd_graph.NoRouteFoundException;
import org.iviPro.scenedetection.sd_graph.Node;
import org.iviPro.scenedetection.sd_graph.TemporalGraph;
import org.iviPro.scenedetection.sd_graph.UndirectedWeightedShotGraph;
import org.iviPro.scenedetection.sd_main.CutTypes;
import org.iviPro.scenedetection.sd_main.SDTime;
import org.iviPro.scenedetection.sd_main.Scene;
import org.iviPro.scenedetection.sd_main.Shot;

public class VideoDecomposition extends Thread {

	public static final float SIMILARITYTHRESHOLD = 0.75f;

	public static final int DIFFERENCETHRESHOLD = 500;

	private List<Shot> shotLstOverall;

	private boolean parallel;

	private UndirectedWeightedShotGraph<Shot> graph;

	private FillAdjazencyMatrixWorker[] adjacencyWorker;

	private PreprocessingWorker[] preWorker;

	private MaxValueQuadraticFormDistanceWorker[] maxValueWorker;

	private double[][] similarityMatrix;

	private boolean interrupt;

	private GraphGrammar grammar;

	public VideoDecomposition(List<Shot> shotLst, boolean parallel) {
		this.shotLstOverall = shotLst;
		this.parallel = parallel;
		int amountWorker = getAmountWorkers();
		this.adjacencyWorker = new FillAdjazencyMatrixWorker[amountWorker];
		this.preWorker = new PreprocessingWorker[amountWorker];
		this.maxValueWorker = new MaxValueQuadraticFormDistanceWorker[amountWorker];
		this.similarityMatrix = new double[shotLst.size()][shotLst.size()];
		this.interrupt = false;
		this.grammar = new GraphGrammar(shotLst);
		// Standard initialization with -1
		for (int i = 0; i < similarityMatrix.length; i++) {
			for (int j = 0; j < similarityMatrix[i].length; j++) {
				similarityMatrix[i][j] = -1;
			}
		}

		for (int i = 0; i < amountWorker; i++) {
			List<Shot> lst = createWorkerList(amountWorker, i);
			adjacencyWorker[i] = new FillAdjazencyMatrixWorker(lst);
			preWorker[i] = new PreprocessingWorker(lst);
			maxValueWorker[i] = new MaxValueQuadraticFormDistanceWorker(lst);
		}
	}

	public void interruptCalculations() {
		for (int i = 0; i < preWorker.length; i++) {
			preWorker[i].setInterrupt();
		}
		for (int i = 0; i < adjacencyWorker.length; i++) {
			adjacencyWorker[i].setInterrupt();
		}
	}

	@Override
	public void run() {
		// do some preprocessing
		if (!interrupt)
			;
		for (int i = 0; i < preWorker.length; i++) {
			preWorker[i].start();
		}

		if (!interrupt)
			;
		for (int i = 0; i < preWorker.length; i++) {
			try {
				preWorker[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// fill maxValQuadraticFormDistance
		if (!interrupt)
			;
		for (int i = 0; i < maxValueWorker.length; i++) {
			maxValueWorker[i].start();
		}

		if (!interrupt)
			;
		for (int i = 0; i < maxValueWorker.length; i++) {
			try {
				maxValueWorker[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Calculate Similarities
		if (!interrupt)
			;
		for (int i = 0; i < adjacencyWorker.length; i++) {
			adjacencyWorker[i].start();
		}

		if (!interrupt)
			;
		for (int i = 0; i < adjacencyWorker.length; i++) {
			try {
				adjacencyWorker[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (!interrupt)
			;
		updateSimilarityMatrix();
		createUndirectedWeightedGraph();

	}

	// NUR ZU TESTZWECKEN!!!
	public void setSimilarityMatrix(double[][] matrix) {
		this.similarityMatrix = matrix;
	}

	public Cluster getMainCluster() {
		return new Cluster(shotLstOverall, graph);
	}

	private void createUndirectedWeightedGraph() {
		graph = new UndirectedWeightedShotGraph<Shot>();
		for (int i = 0; i < shotLstOverall.size(); i++) {
			graph.setNode(shotLstOverall.get(i));
		}

		List<Node<Shot>> lst = graph.getNodeList(shotLstOverall);

		for (Iterator<Node<Shot>> iterator = lst.iterator(); iterator.hasNext();) {
			Node<Shot> node = (Node<Shot>) iterator.next();
			for (int i = 0; i < lst.size(); i++) {
				Node<Shot> toNode = lst.get(i);
				if (node.compareTo(toNode) != 0) {
					Edge<Shot> edge = toNode.hasEdgeToNode(node);
					if (edge != null) {
						node.addAdjacencyNode(new Edge<Shot>(toNode, edge
								.getWeight()));
					} else {
						double val = similarityMatrix[node.getData()
								.getShotId()][lst.get(i).getData().getShotId()];
						node.addAdjacencyNode(lst.get(i),
								(float) Math.exp(Math.exp(val)));
					}
				} else {
					Edge<Shot> edge = toNode.hasEdgeToNode(node);
					if (edge != null) {
						node.addAdjacencyNode(new Edge<Shot>(toNode, edge
								.getWeight()));
					} else {
						node.addAdjacencyNode(lst.get(i),
								(float) Math.exp(Math.E));
					}
				}
			}
		}

		// //////////TESTUMGEBUNG////////////////////////
		lst = graph.getNodeList();
		for (Iterator<Node<Shot>> iterator = lst.iterator(); iterator.hasNext();) {
			Node<Shot> node = (Node<Shot>) iterator.next();
			ConcurrentSkipListMap<Integer, Edge<Shot>> map = node.getEdgeMap();
			for (Map.Entry<Integer, Edge<Shot>> entry : map.entrySet()) {
				Edge<Shot> edge = entry.getValue();
				// System.out.println("Value: " + edge.getWeight() + "Shot1: "
				// + node.getData().getStartFrame() + " Shot2: "
				// + edge.getEndNode().getData().getStartFrame());
			}
		}
		// System.out.println("Durchl�ufe: " + testsum);
	}

	// METHODE IST REIN ZU TESTZWECKEN!!!!
	public void createUndirectedWeightedGraphTEST() {
		graph = new UndirectedWeightedShotGraph<Shot>();
		for (int i = 0; i < shotLstOverall.size(); i++) {
			graph.setNode(shotLstOverall.get(i));
		}

		for (int i = 0; i < adjacencyWorker.length; i++) {
			adjacencyWorker[i].start();
		}

		for (int i = 0; i < adjacencyWorker.length; i++) {
			try {
				adjacencyWorker[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		List<Node<Shot>> lst = graph.getNodeList(shotLstOverall);

		for (Iterator<Node<Shot>> iterator = lst.iterator(); iterator.hasNext();) {
			Node<Shot> node = (Node<Shot>) iterator.next();
			for (int i = 0; i < lst.size(); i++) {
				Node<Shot> toNode = lst.get(i);
				if (node.compareTo(toNode) != 0) {
					Edge<Shot> edge = toNode.hasEdgeToNode(node);
					if (edge != null) {
						node.addAdjacencyNode(new Edge<Shot>(toNode, edge
								.getWeight()));
					} else {
						node.addAdjacencyNode(lst.get(i),
								(float) similarityMatrix[node.getData()
										.getShotId()][i]);
					}
				} else {
					Edge<Shot> edge = toNode.hasEdgeToNode(node);
					if (edge != null) {
						node.addAdjacencyNode(new Edge<Shot>(toNode, edge
								.getWeight()));
					} else {
						node.addAdjacencyNode(lst.get(i), (float) 2.718);
					}
				}
			}
		}

		// //////////TESTUMGEBUNG////////////////////////
		lst = graph.getNodeList();
		for (Iterator<Node<Shot>> iterator = lst.iterator(); iterator.hasNext();) {
			Node<Shot> node = (Node<Shot>) iterator.next();
			ConcurrentSkipListMap<Integer, Edge<Shot>> map = node.getEdgeMap();
			for (Map.Entry<Integer, Edge<Shot>> entry : map.entrySet()) {
				Edge<Shot> edge = entry.getValue();
				// System.out.println("Value: " + edge.getWeight() + "Shot1: "
				// + node.getData().getStartFrame() + " Shot2: "
				// + edge.getEndNode().getData().getStartFrame());
			}
		}
		// System.out.println("Durchl�ufe: " + testsum);
	}

	private double calculateSimilarity(Shot shot1, Shot shot2) {
		// double featureDifferenceColor = Math.abs(shot1
		// .calculateColorSimilarity(shot2));
		double featureComplexity = Math.abs(shot1
				.calculateComplexitySimilarity(shot2)) * 3;
		double featureDifferenceColor = Math.abs(shot1
				.calculateWeightedChiDistance(shot2));

		// if(shot1.getShotId() == 1 && shot2.getShotId() == 4) {
		// System.out.println("ShotId:"+shot1.getShotId() +
		// "ShotId2:"+shot2.getShotId()+ "Die Spontane Colordifferenz ist: "+
		// featureDifferenceColor);
		// }

		if (featureDifferenceColor > 1.9) {
			if (featureComplexity < 2.1) {
				featureDifferenceColor *= 0.85;
			}
		}
		return featureDifferenceColor;
		// return (Math.abs(shot1.calculateWeightedChiDistance(shot2)));
	}

	public void updateSimilarityMatrix() {
		for (int i = 0; i < similarityMatrix.length; i++) {
			for (int j = 0; j < similarityMatrix[i].length; j++) {
				if (j > i) {
					double val = checkShotRoute(i, j);
					// System.out.println("Grammar First Value: "+(val));
					// val = grammar.involveGrammarValue((val), i, j);
					// System.out.println("Grammar Value: "+val);
					similarityMatrix[i][j] = val;
					similarityMatrix[j][i] = val;
				}
			}
		}
		// pushShotsTogether();
	}

	private double checkShotRoute(int srcIndex, int tgtIndex) {
		// return setDistanceFunction(similarityMatrix[srcIndex][tgtIndex],
		// shotLstOverall.get(tgtIndex).getStartFrame() -
		// shotLstOverall.get(srcIndex).getEndFrame());
		long frameDif = shotLstOverall.get(tgtIndex).getStartFrame()
				- shotLstOverall.get(srcIndex).getEndFrame();
		double distance = similarityMatrix[srcIndex][tgtIndex];
		if (srcIndex == 1 && tgtIndex == 4) {
			System.out.println("Der gewollte Index MANAAANANNANANAANA: "
					+ similarityMatrix[srcIndex][tgtIndex]);
		}
		if (distance < 0.7) {
			// return (float) Math.exp(distance);
			// System.out.println("SrcIndex: "+srcIndex+" TgtIndex: "+tgtIndex+" Val: "
			// +(Math.exp(setDistanceFunction(distance, frameDif))));
			return setDistanceFunction(distance, frameDif);
		}
		int pointer = tgtIndex;
		int distanceMeasure = 0;
		boolean update = true;
		while (pointer != srcIndex) {
			if (distanceMeasure > 2) {
				update = false;
				break;
			}
			distanceMeasure++;
			if (similarityMatrix[srcIndex][pointer] > 0.70
					&& similarityMatrix[pointer][tgtIndex] > 0.70) {
				distanceMeasure = 0;
			}
			pointer--;
		}
		if (!update) {
			// return (float) Math.exp(distance);
			// System.out.println("SrcIndex: "+srcIndex+" TgtIndex: "+tgtIndex+" Val: "
			// +(Math.exp(setDistanceFunction(distance, frameDif))));
			return setDistanceFunction(distance, frameDif);
		} else {
			// return (float) Math.E;
			// System.out.println("SrcIndex: "+srcIndex+" TgtIndex: "+tgtIndex+" Val: "
			// +((float)Math.exp(distance)));
			return (float) distance;
		}
	}

	private float setDistanceFunction(double val, double distance) {
		double firstResult = 0;
		if (distance < 4000) {
			firstResult = (Math.exp(((-8 * distance) / 4000) * val));
		}
		double result = firstResult * val;

		// System.out.println("InpuvAL :"+val+"Output"+result+"Distanz: "+distance);
		return (float) result;

		// ((float) ((-8 * (12000 - distance)) / 12000) * val);
		// return (float) ((1.0 / Math.sqrt(Math.log(distance + Math.E - 1))) *
		// val);
	}

	private int getAmountWorkers() {
		if (parallel) {
			return Runtime.getRuntime().availableProcessors();
		} else {
			return 1;
		}
	}

	private List<Shot> createWorkerList(int amountWorker, int worker) {
		int mainSplitter = (int) Math.floor(shotLstOverall.size()
				/ amountWorker);
		int rest = shotLstOverall.size() % amountWorker;
		int start = 0;
		for (int i = 0; i < worker; i++) {
			start += mainSplitter;
			if (rest > 0) {
				start++;
				rest--;
			}
		}
		int end = start + mainSplitter;
		if (rest > 0) {
			end++;
		}
		List<Shot> result = shotLstOverall.subList(start, end);
		return result;
	}

	class FillAdjazencyMatrixWorker extends Thread {

		private List<Shot> shotList;

		private boolean interrupt;

		public FillAdjazencyMatrixWorker(List<Shot> shotList) {
			this.shotList = shotList;
			this.interrupt = false;
		}

		public void setInterrupt() {
			this.interrupt = true;
		}

		@Override
		public void run() {
			List<Shot> toDo = shotList;
			List<Shot> lst = shotLstOverall;

			for (Iterator<Shot> iterator = toDo.iterator(); iterator.hasNext();) {
				if (interrupt) {
					break;
				}
				Shot shotToDo = (Shot) iterator.next();
				for (Iterator<Shot> iterator2 = lst.iterator(); iterator2
						.hasNext();) {
					if (interrupt) {
						break;
					}
					Shot shot = (Shot) iterator2.next();
					if (similarityMatrix[shot.getShotId()][shotToDo.getShotId()] == -1) {
						similarityMatrix[shotToDo.getShotId()][shot.getShotId()] = calculateSimilarity(
								shotToDo, shot);
					} else {
						similarityMatrix[shotToDo.getShotId()][shot.getShotId()] = similarityMatrix[shot
								.getShotId()][shotToDo.getShotId()];
					}
				}

			}
		}
	}

	class MaxValueQuadraticFormDistanceWorker extends Thread {

		private List<Shot> shotList;

		private boolean interrupt;

		public MaxValueQuadraticFormDistanceWorker(List<Shot> shotList) {
			this.shotList = shotList;
			this.interrupt = false;
		}

		public void setInterrupt() {
			this.interrupt = true;
		}

		@Override
		public void run() {
			List<Shot> toDo = shotList;
			List<Shot> lst = shotLstOverall;

			for (Iterator<Shot> iterator = toDo.iterator(); iterator.hasNext();) {
				if (interrupt) {
					break;
				}
				Shot shotToDo = (Shot) iterator.next();
				for (Iterator<Shot> iterator2 = lst.iterator(); iterator2
						.hasNext();) {
					if (interrupt) {
						break;
					}
					Shot shot = (Shot) iterator2.next();
					shotToDo.setMaxVal(shot);
				}
			}
		}
	}

	class PreprocessingWorker extends Thread {

		private List<Shot> shotList;

		private boolean interrupt;

		public PreprocessingWorker(List<Shot> shotList) {
			this.shotList = shotList;
			this.interrupt = false;
		}

		public void setInterrupt() {
			this.interrupt = true;
		}

		@Override
		public void run() {
			for (int i = 0; i < shotList.size(); i++) {
				if (!interrupt) {
					shotList.get(i).setSimilarityFeatures();
				} else
					break;
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] args) {

		List<Shot> shotList = new LinkedList<Shot>();
		Shot shot1 = new Shot(CutTypes.HardCut, CutTypes.HardCut, 0, 100,
				new SDTime(0), new SDTime(100));
		shot1.setShotID(0);
		Shot shot2 = new Shot(CutTypes.HardCut, CutTypes.HardCut, 101, 200,
				new SDTime(0), new SDTime(100));
		shot2.setShotID(1);
		Shot shot3 = new Shot(CutTypes.HardCut, CutTypes.HardCut, 201, 300,
				new SDTime(0), new SDTime(100));
		shot3.setShotID(2);
		Shot shot4 = new Shot(CutTypes.HardCut, CutTypes.HardCut, 301, 400,
				new SDTime(0), new SDTime(100));
		shot4.setShotID(3);
		Shot shot5 = new Shot(CutTypes.HardCut, CutTypes.HardCut, 401, 500,
				new SDTime(0), new SDTime(100));
		shot5.setShotID(4);
		Shot shot6 = new Shot(CutTypes.HardCut, CutTypes.HardCut, 501, 600,
				new SDTime(0), new SDTime(100));
		shot6.setShotID(5);
		shotList.add(shot1);
		shotList.add(shot2);
		shotList.add(shot3);
		shotList.add(shot4);
		shotList.add(shot5);
		shotList.add(shot6);
		VideoDecomposition decomp = new VideoDecomposition(shotList, true);

		double[][] similarityMatrix = new double[6][6];
		similarityMatrix[0][0] = 1;
		similarityMatrix[0][1] = 0.5;
		similarityMatrix[0][2] = 0.5;
		similarityMatrix[0][3] = 0.8;
		similarityMatrix[0][4] = 0.4;
		similarityMatrix[0][5] = 0.8;

		similarityMatrix[1][0] = 0.5;
		similarityMatrix[1][1] = 1.0;
		similarityMatrix[1][2] = 0.5;
		similarityMatrix[1][3] = 0.5;
		similarityMatrix[1][4] = 0.5;
		similarityMatrix[1][5] = 0.5;

		similarityMatrix[2][0] = 0.5;
		similarityMatrix[2][1] = 0.5;
		similarityMatrix[2][2] = 1;
		similarityMatrix[2][3] = 0.5;
		similarityMatrix[2][4] = 0.5;
		similarityMatrix[2][5] = 0.5;

		similarityMatrix[3][0] = 0.8;
		similarityMatrix[3][1] = 0.5;
		similarityMatrix[3][2] = 0.5;
		similarityMatrix[3][3] = 1;
		similarityMatrix[3][4] = 0.5;
		similarityMatrix[3][5] = 0.8;

		similarityMatrix[4][0] = 0.5;
		similarityMatrix[4][1] = 0.5;
		similarityMatrix[4][2] = 1;
		similarityMatrix[4][3] = 0.5;
		similarityMatrix[4][4] = 0.5;
		similarityMatrix[4][5] = 0.5;

		similarityMatrix[5][0] = 0.8;
		similarityMatrix[5][1] = 0.5;
		similarityMatrix[5][2] = 0.5;
		similarityMatrix[5][3] = 0.8;
		similarityMatrix[5][4] = 0.5;
		similarityMatrix[5][5] = 1;

		decomp.setSimilarityMatrix(similarityMatrix);
		decomp.updateSimilarityMatrix();
		decomp.createUndirectedWeightedGraphTEST();

		// Third Step: VideoDecomposition
		List<Cluster> clusterLst = doVideoDecomposition(decomp, shotList);

		// Step Four: Temporal Graph Creation
		TemporalGraph<Cluster> tempGraph = doTemporalGraphGeneration(clusterLst);

		// Step Five: Shortest Path
		List<Cluster> shortestPath = doShortestPath(tempGraph);

		// Step Six: Scene Extraction
		doSceneExtraction(tempGraph, shortestPath);
	}

	static List<Scene> doSceneExtraction(TemporalGraph<Cluster> graph,
			List<Cluster> shortestPath) {
		List<Scene> scenes = new SceneExtraction(graph, shortestPath)
				.generateScenes();
		System.gc();

		return scenes;
	}

	static List<Cluster> doShortestPath(TemporalGraph<Cluster> graph) {
		Dijkstra<Cluster> dijkstra = new Dijkstra<Cluster>(graph);
		List<Cluster> shortestPath = new LinkedList<Cluster>();
		try {
			shortestPath = dijkstra.calculateRoute(graph.getStartObject(),
					graph.getEndObject());
		} catch (NoRouteFoundException e) {
			System.err.println(e.getMessage());
		}
		System.gc();

		return shortestPath;
	}

	static TemporalGraph<Cluster> doTemporalGraphGeneration(List<Cluster> lst) {
		TemporalGraph<Cluster> graph = new TemporalGraphGeneration(lst)
				.startTemporalGraphGeneration();
		System.gc();
		return graph;
	}

	static List<Cluster> doVideoDecomposition(VideoDecomposition decomp,
			List<Shot> lst) {
		ConcurrentNCutAlgorithm nCutAlgorithm = new ConcurrentNCutAlgorithm(
				decomp.getMainCluster());
		List<Cluster> clusterList = nCutAlgorithm.startClustering();
		for (Iterator<Cluster> iterator = clusterList.iterator(); iterator
				.hasNext();) {
			Cluster cluster = (Cluster) iterator.next();
			List<Shot> liste = cluster.getShotList();
			for (Iterator<Shot> iterator2 = liste.iterator(); iterator2
					.hasNext();) {
				Shot shot = (Shot) iterator2.next();
				// System.out.println("Shot id: " + shot.getShotId());
			}
			// System.out
			// .println("------------------------------------------------------------");
		}

		return clusterList;
	}
}
