package org.iviPro.scenedetection.sd_main;

import java.util.LinkedList;
import java.util.List;

public abstract class ShotBoundaryDetectionAlgorithm extends Thread {

	protected boolean parallel;

	protected String filelink;

	protected List<Shot> shotList;

	public abstract void setGradualMode(boolean gradual);

	public abstract void interruptCalculations();

	public void setParallelMode(boolean parallel) {
		this.parallel = parallel;
	}

	public void setFilePath(String path) {
		this.filelink = path;
	}

	public List<Shot> getShots() {
		// Current list will be cloned to improve garbage collection when ready!
		List<Shot> result = new LinkedList<Shot>();
		// Shot ids will be set!
		for (int i = 0; i < shotList.size(); i++) {
			shotList.get(i).setShotID(i);
			result.add(shotList.get(i).clone());
		}
		return result;
	}
}
