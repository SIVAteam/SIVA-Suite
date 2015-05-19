package org.iviPro.scenedetection.sd_main;

import java.awt.image.BufferedImage;

public class Keyframe implements Comparable<Keyframe>, Cloneable {

	private long frameNr;

	private BufferedImage image;

	private float[] hsvhistogram;
	
	private double[] rgbhistogram;

	private int[] luminanceProjection;
	
	private float[] complexity;

	public Keyframe(long frameNr, BufferedImage image) {
		this.frameNr = frameNr;
		this.image = image;
	}

	public long getFramenr() {
		return frameNr;
	}

	public BufferedImage getImage() {
		return image;
	}

	public float[] getColorHistogram() {
		return hsvhistogram;
	}
	
	public double[] getRGBcolorHistogram() {
		return rgbhistogram;
	}
	
	public int[] getLuminanceProjection() {
		return luminanceProjection;
	}
	
	public float[] getComplexity() {
		return complexity;
	}
	
	public void setSimilarityFeatures() {
		this.hsvhistogram = MiscOperations.createHSVHistogram(image);
		this.luminanceProjection = MiscOperations.setLuminanceProjection(image);
		this.complexity = MiscOperations.setEdgeHistogram(image);
		this.rgbhistogram = MiscOperations.createRGBHistogram(image);
	}

	@Override
	public int compareTo(Keyframe o) {
		if (this.frameNr < o.getFramenr()) {
			return -1;
		} else if (this.frameNr > o.getFramenr()) {
			return 1;
		} else {
			return 0;
		}
	}
}
