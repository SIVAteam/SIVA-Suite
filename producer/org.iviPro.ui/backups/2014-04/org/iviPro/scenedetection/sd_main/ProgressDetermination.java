package org.iviPro.scenedetection.sd_main;

public class ProgressDetermination {

	private static ProgressDetermination instance;

	private static int amountFramesPerInstance;

	private static int processedFrames;
	
	private static double frameRate;

	private ProgressDetermination() {
		processedFrames = 0;
		amountFramesPerInstance = 1;
		frameRate = 1;
	}

	public synchronized static ProgressDetermination getInstance() {
		if (instance == null) {
			instance = new ProgressDetermination();
		}
		return instance;
	}

	public synchronized static void setAmountFrames(long nano) {
		amountFramesPerInstance = (int) ((nano / 1000000000L) * frameRate);
	}

	public synchronized static int getAmountFrames() {
		return amountFramesPerInstance;
	}

	public synchronized static void setProcessedFrames(int frames) {
		processedFrames += frames;
	}
	
	public synchronized static void setFrameRate(double rate) {
		frameRate = rate;
	}
	
	public static void reset() {
		processedFrames = 0;
	}

	public synchronized static int getWorked(String calculation) {
		if (calculation.equalsIgnoreCase("shotdetection")) {
			return processedFrames;
		} else if(calculation.equalsIgnoreCase("keyframeextraction")) {
			return processedFrames;
		}
		return 0;
	}
}
