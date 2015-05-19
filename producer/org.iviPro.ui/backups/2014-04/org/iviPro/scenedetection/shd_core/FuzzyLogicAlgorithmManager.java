package org.iviPro.scenedetection.shd_core;

import java.util.LinkedList;
import java.util.List;

import org.iviPro.scenedetection.shd_algorithm.Cut;

public class FuzzyLogicAlgorithmManager {

	private boolean parallel;

	private int amountWorkers;

	private FuzzyLogicSubWorker[] workers;

	private String filePath;

	FuzzyLogicAlgorithmManager(boolean parallel, String filePath) {
		this.parallel = parallel;
		this.filePath = filePath;
		this.amountWorkers = getAmountWorkers();
		workers = new FuzzyLogicSubWorker[amountWorkers];
	}
	
	void interruptCalculations() {
		for (int i = 0; i < workers.length; i++) {
			workers[i].kill();
		}
	}

	void start() {
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new FuzzyLogicSubWorker(i, workers.length);
			System.out.println("Filepath: " + filePath);
			if (!workers[i].openVideo(filePath)) {
				System.out.println("Worker "+i+" couldn't open file!");
			}
		}

		for (int i = 0; i < workers.length; i++) {
			workers[i].start();
		}
		
		for (int i = workers.length - 1; i >= 0; i--) {
			try {
				workers[i].join();
			} catch (InterruptedException e) {
			}
		}
	}

	private int getAmountWorkers() {
		if (parallel) {
			return Runtime.getRuntime().availableProcessors();
		} else {
			return 1;
		}
	}

	/**
	 * CutLists of all thread will be merged!
	 * 
	 * @return List of Cuts in scene detection specified format
	 */
	List<Cut> getCuts() {
		LinkedList<Cut> lst = new LinkedList<Cut>();
		for (int i = 0; i < amountWorkers; i++) {
			List<Cut> workerList = workers[i].getComputedCuts();
			for (int j = 0; j < workerList.size(); j++) {
				lst.add(workerList.get(j));
			}
		}
		return lst;
	}
	
	void close() {
		for (int i = 0; i < workers.length; i++) {
			workers[i].close();
		}
	}
}
