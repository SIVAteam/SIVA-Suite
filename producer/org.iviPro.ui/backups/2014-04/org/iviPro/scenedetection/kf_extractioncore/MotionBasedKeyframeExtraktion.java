package org.iviPro.scenedetection.kf_extractioncore;

import org.iviPro.scenedetection.sd_main.KeyframeExtractionAlgorithm;

public class MotionBasedKeyframeExtraktion extends KeyframeExtractionAlgorithm {

	private MotionBasedKeyframeManager manager;

	private boolean interrupt;

	public MotionBasedKeyframeExtraktion() {
		this.interrupt = false;
	}

	@Override
	public void run() {
		this.manager = new MotionBasedKeyframeManager(parallel, filelink,
				shotList);
		manager.start();
		if (!interrupt) {
			result = manager.getNewShots();
			for (int i = 0; i < result.size(); i++) {
				result.get(i).setShotID(i);
			}
		}
		manager.close();
	}

	@Override
	public void interruptCalculations() {
		this.interrupt = true;
		manager.interruptCalculations();
	}
}
