package org.iviPro.scenedetection.sd_main;

import java.util.List;

public abstract class KeyframeExtractionAlgorithm extends Thread {

	protected boolean parallel;
	
	protected String filelink;
	
	protected List<Shot> shotList;
	
	protected List<Shot> result;
	
	public abstract void interruptCalculations();
	
	public void setParallelMode(boolean parallel) {
		this.parallel = parallel;
	}
	
	public void setFilePath(String path) {
		this.filelink = path;
	}
	
	public void setShots(List<Shot> lst) {
		this.shotList = lst;
	}
	
	public List<Shot> getShots() {
		return result;
	}
}
