package org.iviPro.scenedetection.shd_core;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.iviPro.scenedetection.sd_main.AlgorithmSettings;
import org.iviPro.scenedetection.sd_main.CutTypes;
import org.iviPro.scenedetection.sd_main.SDTime;
import org.iviPro.scenedetection.sd_main.Shot;
import org.iviPro.scenedetection.sd_main.ShotBoundaryDetectionAlgorithm;
import org.iviPro.scenedetection.shd_algorithm.Cut;
import org.iviPro.scenedetection.shd_algorithm.ShotDetectionSettings;

public class FuzzyLogicAlgorithm extends ShotBoundaryDetectionAlgorithm {

	private FuzzyLogicAlgorithmManager manager;

	private boolean gradual;

	private boolean fastMotionCompensation;

	private boolean interrupt;

	public FuzzyLogicAlgorithm() {
		this.interrupt = false;
		this.gradual = false;
		this.parallel = false;
		this.fastMotionCompensation = true;
		this.shotList = null;
	}

	@Override
	public void run() {
		calcShots();
	}

	@Override
	public void interruptCalculations() {
		this.interrupt = true;
		manager.interruptCalculations();

	}

	/**
	 * Function returns all shots founded by the Fuzzy Logic Algorithm. List is
	 * empty if callup occurs before algorithm is ready
	 */
	private void calcShots() {
		ShotDetectionSettings.setOnSidedFeature(true);
		ShotDetectionSettings.setEdgeDetection("Canny");
		ShotDetectionSettings.setEnableDissolve(gradual);
		ShotDetectionSettings.setFading(gradual);
		ShotDetectionSettings.setFastMotionCompensation(fastMotionCompensation);
		this.manager = new FuzzyLogicAlgorithmManager(parallel, filelink);
		manager.start();
		if (!interrupt) {
			shotList = new LinkedList<Shot>();
			List<Cut> lst = manager.getCuts();
			Collections.sort(lst);
			for (int i = 0; i < lst.size() - 1; i++) {
				shotList.add(createSceneDetectionShotObject(lst.get(i),
						lst.get(i + 1)));
			}
			if (AlgorithmSettings.getClassInstance().isShortShots()) {
				aggregateShortShots();
			}
		}
		manager.close();
	}

	@Override
	public void setGradualMode(boolean gradual) {
		this.gradual = gradual;
	}

	private void aggregateShortShots() {
		for (int i = 1; i < shotList.size(); i++) {
			if (shotList.get(i).getEndFrame() - shotList.get(i).getStartFrame() < 25) {
				Shot newShot = new Shot(shotList.get(i - 1).getCut1Type(),
						shotList.get(i).getCut2Type(), shotList.get(i - 1)
								.getStartFrame(),
						shotList.get(i).getEndFrame(), shotList.get(i - 1)
								.getTimeStart(), shotList.get(i).getTimeEnd());
				newShot.setStartImage(shotList.get(i - 1).getStartImage());
				newShot.setEndImage(shotList.get(i).getEndImage());
				newShot.setShotID(shotList.get(i - 1).getShotId());
				shotList.remove(i);
				shotList.remove(i - 1);
				shotList.add((i - 1), newShot);
				i--;
			}
		}
	}

	private Shot createSceneDetectionShotObject(Cut cut1, Cut cut2) {
		if (cut1.getFirstTime() != null) {
			System.out.println("Millisekunden: "
					+ cut1.getFirstTime().getHundertstelSekunden()
					+ "FrameNr: " + cut1.getCutFrameNr());
		}
		SDTime startFrame = cut1.getSecondTime();
		SDTime endFrame = cut2.getFirstTime();
		Shot shot = new Shot(chooseType(cut1.getCutCategory()),
				chooseType(cut2.getCutCategory()), cut1.getCutFrameNr(),
				cut2.getCutFrameNr() - 1, startFrame, endFrame);
		shot.setStartImage(cut1.getImage(true));
		shot.setEndImage(cut2.getImage(false));
		System.out.println("Cut2 Framenr: " + (cut2.getCutFrameNr() - 1));
		return shot;
	}

	private CutTypes chooseType(int nr) {
		if (nr == 0) {
			return CutTypes.HardCut;
		} else if (nr == 1) {
			return CutTypes.Fade;
		} else if (nr == 2) {
			return CutTypes.Dissolve;
		}
		return null;
	}
}
