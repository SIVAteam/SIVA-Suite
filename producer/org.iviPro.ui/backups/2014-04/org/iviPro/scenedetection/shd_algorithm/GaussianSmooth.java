package org.iviPro.scenedetection.shd_algorithm;

import java.awt.Color;

/**
 * Contains the functionality to generate a gaussian filter kernel and apply it
 * to an image.
 * 
 * @author Simon Horne.
 * 
 *         modified by Stefan Zwicklbauer
 */
public class GaussianSmooth extends Thread {

	/**
	 * Default no-args constructor.
	 */
	public GaussianSmooth() {
	}

	/**
	 * Calculates the discrete value at x,y of the 2D gaussian distribution.
	 * 
	 * @param theta
	 *            the theta value for the gaussian distribution
	 * @param x
	 *            the point at which to calculate the discrete value
	 * @param y
	 *            the point at which to calculate the discrete value
	 * @return the discrete gaussian value
	 */
	public static double gaussianDiscrete2D(double theta, int x, int y) {
		double g = 0;
		for (double ySubPixel = y - 0.5; ySubPixel < y + 0.55; ySubPixel += 0.1) {
			for (double xSubPixel = x - 0.5; xSubPixel < x + 0.55; xSubPixel += 0.1) {
				g = g
						+ ((1 / (2 * Math.PI * theta * theta)) * Math.pow(
								Math.E, -(xSubPixel * xSubPixel + ySubPixel
										* ySubPixel)
										/ (2 * theta * theta)));
			}
		}
		g = g / 121;
		return g;
	}

	/**
	 * Calculates several discrete values of the 2D gaussian distribution.
	 * 
	 * @param theta
	 *            the theta value for the gaussian distribution
	 * @param size
	 *            the number of discrete values to calculate (pixels)
	 * @return 2Darray (size*size) containing the calculated discrete values
	 */
	public static double[][] gaussian2D(double theta, int size) {
		double[][] kernel = new double[size][size];
		for (int j = 0; j < size; ++j) {
			for (int i = 0; i < size; ++i) {
				kernel[i][j] = gaussianDiscrete2D(theta, i - (size / 2), j
						- (size / 2));
			}
		}

		double sum = 0;
		for (int j = 0; j < size; ++j) {
			for (int i = 0; i < size; ++i) {
				sum = sum + kernel[i][j];

			}
		}

		return kernel;
	}

	/**
	 * Takes an image and a gaussian distribution, calculates an appropriate
	 * kernel and applies a convolution to smooth the image.
	 * 
	 * @param 2D array representing the input image
	 * @param w
	 *            width of the image
	 * @param h
	 *            height of the image
	 * @param ks
	 *            the required size of the kernel
	 * @param theta
	 *            the gaussian distribution
	 * @return 2D array representing the smoothed image
	 */
	public static double[][] smooth(double[][] input, int width, int height,
			int ks, double theta) {
		double[][] gaussianKernel = new double[ks][ks];
		double[][] output = new double[width][height];
		gaussianKernel = gaussian2D(theta, ks);
		output = Convolution.convolution2DPadded(input, width, height,
				gaussianKernel, ks, ks);
		return output;
	}

	/**
	 * Takes an input image and a gaussian distribution, calculates an
	 * appropriate kernel and applies a convolution to gaussian smooth the
	 * image.
	 * 
	 * @param input
	 *            the input image array
	 * @param w
	 *            the width of the image
	 * @param h
	 *            the height of the image
	 * @param ks
	 *            the size of the kernel to be generated
	 * @param theta
	 *            the gaussian distribution
	 * @return smoothed image array
	 */
	public static int[] smooth_image(int[] input, int w, int h, int ks,
			double theta) {
		double[][] input2D = new double[w][h];
		double[] output1D = new double[w * h];
		double[][] output2D = new double[w][h];
		int[] output = new int[w * h];
		// extract greys from input (1D array) and place in input2D
		for (int j = 0; j < h; ++j) {
			for (int i = 0; i < w; ++i) {
				input2D[i][j] = (new Color(input[j * w + i])).getRed();
			}
		}
		// now smooth this new 2D array
		output2D = smooth(input2D, w, h, ks, theta);

		for (int j = 0; j < h; ++j) {
			for (int i = 0; i < w; ++i) {
				output1D[j * w + i] = output2D[i][j];
			}
		}
		for (int i = 0; i < output1D.length; ++i) {
			int grey = (int) Math.round(output1D[i]);
			if (grey > 255) {
				grey = 255;
			}
			if (grey < 0) {
				grey = 0;
			}
			output[i] = (new Color(grey, grey, grey)).getRGB();
		}
		return output;
	}
}