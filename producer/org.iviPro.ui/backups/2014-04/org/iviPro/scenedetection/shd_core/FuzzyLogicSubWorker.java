package org.iviPro.scenedetection.shd_core;

import java.io.File;
import java.util.List;
import javax.media.MediaLocator;

import org.iviPro.scenedetection.shd_algorithm.Cut;

public class FuzzyLogicSubWorker extends Thread implements Runnable {

	private ShotDetectionProcessor proc;

	FuzzyLogicSubWorker(int threadId, int amountThreads) {
		proc = new ShotDetectionProcessor(this, threadId, amountThreads);
	}

	boolean openVideo(String fileLink) {
		File file = new File(fileLink);
		MediaLocator mediaLocator = null;
		mediaLocator = new MediaLocator(fileLink);

		if (!proc.open(mediaLocator, new Long(file.length()))) {
			System.err.println("Could not open Mediafile!");
			return false;
		}
		return true;
	}
	
	void kill() {
		proc.exit();
		this.interrupt();
	}

	@Override
	public void run() {
		try {
			proc.runDetection();
		} catch (IllegalStateException e) {
			System.out.println("IllegalStateException!");
			e.printStackTrace();
		}
		synchronized (this) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
	}

	List<Cut> getComputedCuts() {
		return proc.getCuts();
	}
	
	void close() {
		proc.close();
	}
}
