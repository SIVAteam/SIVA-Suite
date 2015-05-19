package org.iviPro.scenedetection.sd_main;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Observable;

import javax.media.MediaLocator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.iviPro.scenedetection.sd_misc.FileCheck;
import org.iviPro.scenedetection.shd_core.FuzzyLogicAlgorithm;

public class ShotDetection extends Observable implements
		IRunnableWithProgress {


	private boolean parallel;

	private boolean gradualDetection;

	private ShotBoundaryDetectionAlgorithm shotDetectionAlgorithm;

	private List<Shot> shotResult;
	
	private MediaLocator locator;

	public ShotDetection(String filePath, boolean parallel,
			boolean gradualDetection, boolean mpeg7export, long duration,
			double framerate) {
		File file = new File("c:\\SDtry.mp4");
		if(!file.exists()) {
			throw new IndexOutOfBoundsException();
		}
		this.locator = new MediaLocator("file:/"+file.toURI().getPath());

		
		this.parallel = parallel;
		this.gradualDetection = gradualDetection;
		setShotDetectionAlgorithm(new FuzzyLogicAlgorithm());
		ProgressDetermination.reset();
		ProgressDetermination.setFrameRate(framerate);
		ProgressDetermination.setAmountFrames(duration);
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		// Check for media errors
		FileCheck fileChecker = new FileCheck(locator, false,
				null);
		try {
			fileChecker.check();
		} catch (NoValidSceneDetectionFile e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		try {
			shotResult = doShotDetection(monitor);
		} catch (CancelException e) {
			shotResult = null;
		}
		
		for (int i = 0; i < shotResult.size(); i++) {
			System.out.println("ShotNr: "+(i+1)+"StartFrame: "+shotResult.get(i).getStartFrame());
		}
		System.out.println("ShotNr: "+(shotResult.size())+"EndFrame: "+shotResult.get(shotResult.size() - 1).getEndFrame());
		
		long min = Long.MAX_VALUE;
		long max = 0;
		for (int i = 0; i < shotResult.size(); i++) {
			long val = shotResult.get(i).getEndFrame() - shotResult.get(i).getStartFrame();
			if(val > max) {
				max = val;
				System.out.println("MINTEMP: "+max);
			}
			if(val < min) {
				min = val;
				System.out.println("MAXTEMP: "+min);
			}
		}
		freeMemory();
	}

	public List<Shot> getShots() {
		return shotResult;
	}

	private List<Shot> doShotDetection(IProgressMonitor monitor) throws CancelException {
		WorkingThread thread = new WorkingThread(monitor, "shotdetection");
		monitor.beginTask("Step 1: Shot Detection",
				ProgressDetermination.getAmountFrames());
		this.addObserver(thread);
		thread.start();
		shotDetectionAlgorithm.start();
		while(true) {
			if(monitor.isCanceled()) {
				shotDetectionAlgorithm.interruptCalculations();
				System.gc();
				setChanged();
				notifyObservers();
				throw new CancelException();
			} 
			if(!shotDetectionAlgorithm.isAlive()) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		List<Shot> shotList = shotDetectionAlgorithm.getShots();
		System.gc();
		setChanged();
		notifyObservers();

		return shotList;
	}
	
	private void freeMemory() {
		this.shotDetectionAlgorithm = null;
		System.gc();
		System.runFinalization();
	}

	/**
	 * Shot Detection Algorithm Configuration.
	 * 
	 * @param algo
	 *            Shot Detection Algorithm
	 */
	public void setShotDetectionAlgorithm(ShotBoundaryDetectionAlgorithm algo) {
		this.shotDetectionAlgorithm = algo;
		algo.setGradualMode(gradualDetection);
		algo.setParallelMode(parallel);
		algo.setFilePath(locator.toExternalForm());
	}
}
