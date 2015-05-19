package org.iviPro.scenedetection.sd_main;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import javax.media.MediaLocator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.iviPro.scenedetection.sd_graph.Dijkstra;
import org.iviPro.scenedetection.sd_graph.NoRouteFoundException;
import org.iviPro.scenedetection.sd_algorithm.Cluster;
import org.iviPro.scenedetection.sd_algorithm.SceneExtraction;
import org.iviPro.scenedetection.sd_misc.FileCheck;
import org.iviPro.scenedetection.sd_misc.Mpeg7Export;
import org.iviPro.scenedetection.kf_algorithm.MotionBasedKeyframeAlgorithm;
import org.iviPro.scenedetection.kf_evaluation.EvaluationMain;
import org.iviPro.scenedetection.kf_extractioncore.MotionBasedKeyframeExtraktion;
import org.iviPro.scenedetection.sd_algorithm.ConcurrentNCutAlgorithm;
import org.iviPro.scenedetection.sd_algorithm.TemporalGraphGeneration;
import org.iviPro.scenedetection.sd_algorithm.VideoDecomposition;
import org.iviPro.scenedetection.sd_graph.TemporalGraph;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class SceneDetection extends Observable implements IRunnableWithProgress {

	private MediaLocator locator;

	private boolean parallel;

	private boolean mpeg7export;

	private KeyframeExtractionAlgorithm keyframeExtractionAlgorithm;

	private Mpeg7Export mpeg7exporter;

	private List<Shot> shotList;

	private List<Scene> sceneList;

	public SceneDetection(String filePath, boolean parallel,
			boolean mpeg7export, List<Shot> list, long duration,
			double framerate) {
		this.locator = new MediaLocator("file:\\" + filePath);
		this.parallel = parallel;
		this.shotList = list;
		this.mpeg7export = mpeg7export;
		if (mpeg7export) {
			this.mpeg7exporter = new Mpeg7Export();
		}

		this.setKeyFrameExtractionAlgorithm(new MotionBasedKeyframeExtraktion());
		ProgressDetermination.reset();
		ProgressDetermination.setFrameRate(framerate);
		ProgressDetermination.setAmountFrames(duration);
	}

	public void setKeyFrameExtractionAlgorithm(KeyframeExtractionAlgorithm algo) {
		this.keyframeExtractionAlgorithm = algo;
		algo.setFilePath(locator.toExternalForm());
		algo.setParallelMode(parallel);
	}

	/**
	 * Start Scene Detection
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {

		// Check for media errors
		FileCheck fileChecker = new FileCheck(locator, mpeg7export,
				mpeg7exporter);
		try {
			fileChecker.check();
		} catch (NoValidSceneDetectionFile e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		// Second Step: Keyframe Extraction
		try {
			shotList = doKeyframeExtraction(shotList, monitor);
		} catch (CancelException e) {
			e.printStackTrace();
		}

		// Evaluierung Keyframe Extraction
		// EvaluationMain evalMain = new EvaluationMain(shotList, mediaPath);
		// evalMain.startEvaluation();

		// Third Step: VideoDecomposition
		List<Cluster> clusterLst = doVideoDecomposition(shotList, monitor);

		// Step Four: Temporal Graph Creation
		TemporalGraph<Cluster> graph = doTemporalGraphGeneration(clusterLst,
				monitor);

		// Step Five: Shortest Path
		List<Cluster> shortestPath = doShortestPath(graph, monitor);

		// Step Six: Scene Extraction
		List<Scene> scenes = doSceneExtraction(graph, shortestPath, monitor);

		int sceneNumber = 1;
		for (Iterator<Scene> iterator = scenes.iterator(); iterator.hasNext();) {
			Scene scene = (Scene) iterator.next();
			int numberShots = scene.getNumberofShots();
			for (int i = 0; i < numberShots; i++) {
				Shot shot = scene.getShotWithNr(i);
				System.out.println("Scene" + sceneNumber + " ShotId: "
						+ shot.getShotId());

			}
			sceneNumber++;

		}
		String scenesString = "";
		for (Iterator<Scene> iterator = scenes.iterator(); iterator.hasNext();) {
			Scene scene = (Scene) iterator.next();
			scenesString += scene.getShotWithNr(0).getStartFrame() + ",";
		}
		scenesString += scenes
				.get(scenes.size() - 1)
				.getShotWithNr(
						scenes.get(scenes.size() - 1).getNumberofShots() - 1)
				.getEndFrame();
		this.sceneList = scenes;
		new PerformanceCalculation(scenesString);

	}

	private List<Scene> doSceneExtraction(TemporalGraph<Cluster> graph,
			List<Cluster> shortestPath, IProgressMonitor monitor) {
		WorkingThread thread = new WorkingThread(monitor, "");
		monitor.beginTask("Step 6: Scene Extraction", 100);
		this.addObserver(thread);
		thread.start();
		List<Scene> scenes = new SceneExtraction(graph, shortestPath)
				.generateScenes();
		System.gc();
		setChanged();
		notifyObservers();

		return scenes;
	}

	private List<Cluster> doShortestPath(TemporalGraph<Cluster> graph,
			IProgressMonitor monitor) {
		WorkingThread thread = new WorkingThread(monitor, "");
		monitor.beginTask("Step 5: Shortest Path", 100);
		this.addObserver(thread);
		thread.start();

		Dijkstra<Cluster> dijkstra = new Dijkstra<Cluster>(graph);
		List<Cluster> shortestPath = new LinkedList<Cluster>();
		try {
			shortestPath = dijkstra.calculateRoute(graph.getStartObject(),
					graph.getEndObject());
		} catch (NoRouteFoundException e) {
			System.err.println(e.getMessage());
		}
		System.gc();
		setChanged();
		notifyObservers();

		return shortestPath;
	}

	private TemporalGraph<Cluster> doTemporalGraphGeneration(List<Cluster> lst,
			IProgressMonitor monitor) {
		WorkingThread thread = new WorkingThread(monitor, "");
		monitor.beginTask("Step 4: Temporal Graph Generation", 100);
		this.addObserver(thread);
		thread.start();
		TemporalGraph<Cluster> graph = new TemporalGraphGeneration(lst)
				.startTemporalGraphGeneration();
		System.gc();
		setChanged();
		notifyObservers();

		return graph;
	}

	private List<Cluster> doVideoDecomposition(List<Shot> lst,
			IProgressMonitor monitor) {
		WorkingThread thread = new WorkingThread(monitor, "");
		monitor.beginTask("Step 3: Video Decomposition", 100);
		this.addObserver(thread);
		thread.start();
		VideoDecomposition vidDecomp = new VideoDecomposition(lst, parallel);
		vidDecomp.start();

		while (true) {
			if (monitor.isCanceled()) {
				vidDecomp.interruptCalculations();
				System.gc();
				setChanged();
				notifyObservers();
			}
			if (!vidDecomp.isAlive()) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		ConcurrentNCutAlgorithm nCutAlgorithm = new ConcurrentNCutAlgorithm(
				vidDecomp.getMainCluster());
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

	private List<Shot> doKeyframeExtraction(List<Shot> lst,
			IProgressMonitor monitor) throws CancelException {
		MotionBasedKeyframeAlgorithm.lowerStripes = -1;
		MotionBasedKeyframeAlgorithm.upperStripes = -1;
		keyframeExtractionAlgorithm.setShots(lst);
		WorkingThread thread = new WorkingThread(monitor, "keyframeextraction");
		monitor.beginTask("Step 2: Keyframe Extraction",
				ProgressDetermination.getAmountFrames());
		this.addObserver(thread);
		thread.start();
		keyframeExtractionAlgorithm.start();
		while (true) {
			if (monitor.isCanceled()) {
				keyframeExtractionAlgorithm.interruptCalculations();
				System.gc();
				setChanged();
				notifyObservers();
				throw new CancelException();
			}
			if (!keyframeExtractionAlgorithm.isAlive()) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		List<Shot> shotList = keyframeExtractionAlgorithm.getShots();
		System.gc();
		setChanged();
		notifyObservers();

		return shotList;
	}

	public List<Scene> getScenes() {
		return sceneList;
	}

	public Mpeg7Export getExporter() {
		return mpeg7exporter;
	}

	public MediaLocator getMediaLocator() {
		return locator;
	}

}
