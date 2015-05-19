package org.iviPro.scenedetection.shd_algorithm;

public interface EdgeDetectionAlgorithm {

	public int[] process(int[] src_1d, int width, int height, int size,
			float theta, int lowthresh, int highthresh, float scale, int offset);
	
	public int getEdgeCounter();
	
}
