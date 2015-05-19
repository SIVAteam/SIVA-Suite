package org.iviPro.scenedetection.shd_algorithm;

import java.awt.image.BufferedImage;

/**
 * 
 * @author Stefan Zwicklbauer
 */
public class ThreeFeatures implements ThreeFeaturesInterface {

	protected BufferedImage image;

	protected int[][] currentFrameRGB;

	protected int width;

	protected int height;

	ThreeFeatures(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public void setCurrentRGB(int[][] rgb) {
		this.currentFrameRGB = rgb;
	}

	public float getPeakValue() {
		return 0;
	}

	public void initializeFirstFrameValues() {
	}

}
