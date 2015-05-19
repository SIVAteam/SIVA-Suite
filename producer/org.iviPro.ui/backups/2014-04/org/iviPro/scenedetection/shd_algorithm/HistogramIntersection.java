package org.iviPro.scenedetection.shd_algorithm;
/**
 * Berechnet zwischen 2 Frames den Farbwert Histogrammunterschied
 *
 * @author Stefan Zwicklbauer
 */
public class HistogramIntersection extends ThreeFeatures {

	private int amountblast[];

	private int amountglast[];

	private int amountrlast[];

	public HistogramIntersection(int width, int height) {
		super(width, height);
	}

	@Override
	public void initializeFirstFrameValues() {
		int[][] rgbamount = calculateCurrentAmounts();
		amountblast = rgbamount[0];
		amountglast = rgbamount[1];
		amountrlast = rgbamount[2];
	}

	@Override
	public float getPeakValue() {
		int[][] rgbamount = calculateCurrentAmounts();
		int[] amountb = rgbamount[0];
		int[] amountg = rgbamount[1];
		int[] amountr = rgbamount[2];

		float sumrgb = 0;
		for (int i = 0; i < 256; i++) {
			sumrgb += Math.min(amountb[i], amountblast[i]);
			sumrgb += Math.min(amountg[i], amountglast[i]);
			sumrgb += Math.min(amountr[i], amountrlast[i]);
		}
		float finalresult = 1 - (sumrgb / (3 * width * height));

		amountblast = amountb;
		amountglast = amountg;
		amountrlast = amountr;
		return (int)Math.pow(finalresult * 2000, 1.6);
	}

	private int[][] calculateCurrentAmounts() {
		int[][] rgbamount = new int[3][256];

		int[] amountb = new int[256];
		int[] amountg = new int[256];
		int[] amountr = new int[256];

		for (int i = 0; i < width * height; i++) {
			amountb[currentFrameRGB[2][i]]++;
			amountg[currentFrameRGB[1][i]]++;
			amountr[currentFrameRGB[0][i]]++;
		}
		rgbamount[0] = amountb;
		rgbamount[1] = amountg;
		rgbamount[2] = amountr;

		return rgbamount;
	}
}
