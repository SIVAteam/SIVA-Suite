package org.iviPro.scenedetection.kf_evaluation;

import java.util.List;

import javax.media.MediaLocator;

import org.iviPro.scenedetection.sd_main.Shot;

public class EvaluationMain {

	private List<Shot> shotList;
	
	private MediaLocator m;
	
	public EvaluationMain(List<Shot> shotList, String filePath) {
		this.shotList = shotList;		
		this.m = new MediaLocator("file://" + filePath);
	}
	
	public void startEvaluation() {
		KfEvaluationProzessor proc = new KfEvaluationProzessor(shotList);
		proc.open(m, 0L);
		proc.runEvaluation();
	}

}
