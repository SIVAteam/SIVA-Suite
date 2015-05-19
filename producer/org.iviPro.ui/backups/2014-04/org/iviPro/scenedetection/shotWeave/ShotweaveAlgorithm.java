package org.iviPro.scenedetection.shotWeave;

import java.util.LinkedList;
import java.util.List;
import org.iviPro.scenedetection.sd_algorithm.Cluster;
import org.iviPro.scenedetection.sd_main.Scene;
import org.iviPro.scenedetection.sd_main.Shot;

public class ShotweaveAlgorithm {

	public static final int FORWARDRANGE = 3;

	public static final int BACKWARDRANGE = 3;

	public static final double MATCHINGTHRESHOLD = 0.0;

	private List<Shot> shotList;

	private Cluster mainCluster;

	private List<Scene> sceneList;

	public ShotweaveAlgorithm(Cluster mainCluster) {
		this.mainCluster = mainCluster;
		this.shotList = mainCluster.getShotList();
		sceneList = new LinkedList<Scene>();
	}

	public List<Scene> extractScenes() {
		return null;
	}

	private void doAlgorithm() {
		int currentSceneStart = 0;
		for (int i = 0; i < shotList.size(); i++) {
			// Step 2a Paper:
			boolean matched = false;
			for (int j = 1; j < FORWARDRANGE; j++) {
				if (i + j < shotList.size()) {
					double similarity = mainCluster.getMatchingValue(
							shotList.get(i), shotList.get(i + j));
					if (similarity > MATCHINGTHRESHOLD) {
						matched = true;
						// Set CurrentShot!
						i = i + j;
						break;
					}
				}
			}
			// Step 2b Paper:
			boolean matched2 = false;
			if (!matched) {
				for (int j = 1; j < BACKWARDRANGE; j++) {
					if (!matched2) {
						for (int k = 1; k < FORWARDRANGE; k++) {
							if (i - j >= 0) {
								double similarity = mainCluster
										.getMatchingValue(shotList.get(i - j),
												shotList.get(i + k));
								if (similarity > MATCHINGTHRESHOLD) {
									matched2 = true;
									// Set CurrentShot!
									i = i + k;
									break;
								}
							}
						}
					}
				}
			}
			// Step 2c Paper:
			if (!matched2) {
				i++;
				// Figur 5a
				if (sceneList.size() > 0
						&& sceneList.get(sceneList.size() - 1)
								.getNumberofShots() == 1
						&& i - currentSceneStart > FORWARDRANGE) {
					double similarity = mainCluster.getMatchingValue(sceneList
							.get(sceneList.size() - 1).getShotWithNr(0),
							shotList.get(i));
					if (similarity > MATCHINGTHRESHOLD) {
						// Scene ID will be changed later!
						for (int j = currentSceneStart; j <= i; j++) {
							sceneList.get(sceneList.size() - 1).addShots(
									shotList.get(j), false);
						}
					}
				}  else {
					// Scene comparison with min(log diff, diff) preceding
					// shots
					int start = i
							- (int) Math.min((2.0 / Math.log10(i
									- currentSceneStart)) + 1,
									(i - currentSceneStart));
					boolean isIn = false;
					for (int j = start; j <= i; j++) {
						double similarity = mainCluster.getMatchingValue(shotList
								.get(j),
								shotList.get(i));
						if(similarity > MATCHINGTHRESHOLD) {
							isIn = true;
							break;
						}
					}
					Scene scene = new Scene(0);
					if (isIn) {
						for (int k = currentSceneStart; k <= i; k++) {
							scene.addShots(shotList.get(k), false);
						}
						currentSceneStart = i + 1;
					} else {
						for (int k = currentSceneStart; k < i; k++) {
							scene.addShots(shotList.get(k), false);
						}
						currentSceneStart = i;
					}
				}
			}
		}
	}
}
