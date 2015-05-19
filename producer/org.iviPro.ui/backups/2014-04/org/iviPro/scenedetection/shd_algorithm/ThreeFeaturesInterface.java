package org.iviPro.scenedetection.shd_algorithm;

import java.awt.image.BufferedImage;

/**
 * Interface welche die ThreeFeatures Klassen Histogram Intersection, Motion
 * Compensation und Texture Energy Difference beinhaltet.
 * 
 * @ Author Stefan Zwicklbauer
 */
public interface ThreeFeaturesInterface {

	public void initializeFirstFrameValues();
	
	public float getPeakValue();
	
	public void setImage(BufferedImage image);
	
	public void setCurrentRGB(int[][] rgb);
}
