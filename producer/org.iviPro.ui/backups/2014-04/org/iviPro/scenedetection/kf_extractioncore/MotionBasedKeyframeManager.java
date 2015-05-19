package org.iviPro.scenedetection.kf_extractioncore;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import org.iviPro.scenedetection.sd_main.Shot;

public class MotionBasedKeyframeManager extends Observable {

	private boolean parallel;

	private int amountWorkers;

	private MotionBasedKeyframeSubWorker[] workers;

	private String filePath;

	private long[] threadFrames;

	private List<Shot> shotList;
	
	private int threadShots;

	public MotionBasedKeyframeManager(boolean parallel, String filePath,
			List<Shot> shotList) {
		this.parallel = parallel;
		this.filePath = filePath;
		this.shotList = shotList;
		this.amountWorkers = getAmountWorkers();
		this.threadFrames = new long[amountWorkers];
		workers = new MotionBasedKeyframeSubWorker[amountWorkers];
		this.threadShots = (int) Math.floor(shotList.size() / amountWorkers);
	}

	void start() {
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new MotionBasedKeyframeSubWorker(i,
					splitUpShotList(i), threadFrames);
			if (!workers[i].openVideo(filePath)) {
				System.out.println("Worker " + i + " couldn't open file!");
			}
		}

		for (int i = 0; i < threadFrames.length; i++) {
			System.out.println("Frames: " + threadFrames[i]);
		}

		for (int i = 0; i < workers.length; i++) {
			workers[i].start();
		}

		for (int i = 0; i < workers.length; i++) {
			try {
				workers[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	void interruptCalculations() {
		for (int i = 0; i < workers.length; i++) {
			workers[i].setInterrupt();
		}
		for (int i = 0; i < workers.length; i++) {
			workers[i].kill();
		}
	}

	private int getAmountWorkers() {
		if (parallel) {
			if (shotList.size() < Runtime.getRuntime().availableProcessors()) {
				return shotList.size();
			} else {
//				return 1;
				return Runtime.getRuntime().availableProcessors();
			}
		} else {
			return 1;
		}
	}

	List<Shot> getNewShots() {
		LinkedList<Shot> lst = new LinkedList<Shot>();
		for (int i = 0; i < amountWorkers; i++) {
			List<Shot> workerList = workers[i].setKeyFrames();
			lst.addAll(workerList);
		}
		return lst;
	}

	/**
	 * Splits up the shotlist according to the amount of workers
	 * 
	 * @param threadId
	 *            threadID
	 * @return shotlist to be calculated by the thread with ID threadId
	 */
	private List<Shot> splitUpShotList(int workerId) {
		List<Shot> result = new LinkedList<Shot>();
//		long amountFrames = shotList.get(shotList.size() - 1).getEndFrame();
//		long threadFrames = (long) Math.floor(amountFrames / amountWorkers);
//		int threadShots = (int) Math.floor(shotList.size() / amountWorkers);
		System.out.println("ThreadShots: "+threadShots);
			if((workerId + 1) != amountWorkers) {
				List<Shot> subList = new LinkedList<Shot>();
				subList = shotList.subList(0, threadShots);
				for (int j = 0; j < subList.size(); j++) {
					result.add(subList.get(j).clone());
				}
				this.threadFrames[workerId] = result.get(result.size() - 1).getEndFrame();
				for (int i = 0; i < threadShots; i++) {
					shotList.remove(0);
				}
			} else {
				List<Shot> subList = new LinkedList<Shot>();
				subList = shotList.subList(0, shotList.size());
				for (int j = 0; j < subList.size(); j++) {
					result.add(subList.get(j).clone());
				}
				this.threadFrames[workerId] = result.get(result.size() - 1).getEndFrame();
				for (int i = 0; i < threadShots; i++) {
					shotList.remove(0);
				}
			}
		System.out.println("ThreadFRAMES: "+this.threadFrames[workerId]);
//
//		int toDelete = 0;
//		for (int i = 0; i < shotList.size(); i++) {
//			Shot shot = shotList.get(i);
//			if (shot.getStartFrame() < ((workerId + 1) * threadFrames)
//					&& shot.getEndFrame() >= ((workerId + 1) * threadFrames)) {
//				// All shots must be cloned to prevent a later
//				// ConcurrentModificationException because of more threads
//				// accessing the list
//				List<Shot> subList = new LinkedList<Shot>();
//				subList = shotList.subList(0, i + 1);
//				for (int j = 0; j < subList.size(); j++) {
//					result.add(subList.get(j).clone());
//				}
//				toDelete = i + 1;
//				break;
//			}
//		}
//		System.out.println("Resultsize: "+result.size());
//		this.threadFrames[workerId] = result.get(result.size() - 1).getEndFrame();
//		for (int i = 0; i < toDelete; i++) {
//			shotList.remove(0);
//		}
		return result;
	}
	
	void close() {
		for (int i = 0; i < workers.length; i++) {
			workers[i].close();
		}
	}
}
