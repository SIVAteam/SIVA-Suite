package org.iviPro.scenedetection.sd_main;

public class AlgorithmSettings {
	
	private static AlgorithmSettings classInstance = new AlgorithmSettings();

	private boolean oneSided;
	
	private boolean smoothSettings;
	
	private boolean keyframePME;
	
	private boolean clusteringAdaptive;
	
	private boolean shortShots;

	private AlgorithmSettings() {
		this.oneSided = true;
		this.smoothSettings = true;
		this.keyframePME = true;
		this.clusteringAdaptive = true;
		this.shortShots = true;
	}
	
	public boolean isShortShots() {
		return shortShots;
	}

	public void setShortShots(boolean shortShots) {
		this.shortShots = shortShots;
	}
	
	public static AlgorithmSettings getClassInstance() {
		return classInstance;
	}

	public boolean isOneSided() {
		return oneSided;
	}

	public void setOneSided(boolean oneSided) {
		this.oneSided = oneSided;
	}

	public boolean isSmoothSettings() {
		return smoothSettings;
	}

	public void setSmoothSettings(String smoothSettings) {
		if(smoothSettings.equalsIgnoreCase("Smooth")) {
			this.smoothSettings = true;
		} else {
			this.smoothSettings = false;
		}
	}

	public boolean isKeyframePME() {
		return keyframePME;
	}

	public void setKeyframePME(String keyframePME) {
		if(keyframePME.equalsIgnoreCase("PME")) {
			this.keyframePME = true;
		} else {
			this.keyframePME = false;
		}
	}

	public boolean isClusteringAdaptive() {
		return clusteringAdaptive;
	}

	public void setClusteringAdaptive(String clusteringAdaptive) {
		if(clusteringAdaptive.equalsIgnoreCase("adaptive")) {
			this.clusteringAdaptive = true;
		} else {
			this.clusteringAdaptive = false;
		}
	}
}
