package org.iviPro.scenedetection.kf_algorithm;

import java.util.Collections;
import java.util.List;

/**
 * Frame consists of Macroblock. Intern representation of a macroblock which
 * contains vector properties like magnitude
 */
class Macroblock implements Comparable<Macroblock> {

	private int blockNr;

	private List<Macroblock> filterWindowMacroblocks;

	private float magnitude;

	private float angle;

	/**
	 * Constructor
	 * 
	 * @param blockNr
	 *            blocknumber
	 * @param magnitude
	 *            motion vector magnitude
	 * @param angle
	 *            angle in motion vector field in relation to x axis.
	 */
	Macroblock(int blockNr, float magnitude, float angle) {
		this.blockNr = blockNr;
		this.magnitude = magnitude;
		this.angle = angle;
	}

	/**
	 * Gets the blocknumber
	 * 
	 * @return blocknumber
	 */
	int getBlockNr() {
		return blockNr;
	}

	/**
	 * Gets motion vector magnitude
	 * 
	 * @return magnitude
	 */
	float getMagnitude() {
		return this.magnitude;
	}

	/**
	 * Updates blocknumber
	 * 
	 * @param nr
	 *            blocknumber
	 */
	void setBlocknr(int nr) {
		this.blockNr = nr;
	}

	/**
	 * Gets angle
	 * 
	 * @return angle
	 */
	float getAngle() {
		return this.angle;
	}

	/**
	 * Updates Magnitude
	 * 
	 * @param val
	 *            magnitude
	 */
	void setMagnitude(float val) {
		this.magnitude = val;
	}

	/**
	 * List of macroblocks in the filterwindow of this block
	 * 
	 * @param lst
	 *            List of macroblocks
	 */
	void setFilterWindow(List<Macroblock> lst) {
		this.filterWindowMacroblocks = lst;
		Collections.sort(this.filterWindowMacroblocks);
	}

	/**
	 * Gets the fourth highest magnitude in the filterwindow
	 * 
	 * @return magnitude
	 */
	float getFourthFilterWindowValue() {
		return this.filterWindowMacroblocks.get(3).getMagnitude();
	}

	/**
	 * Comparing two macroblocks according to their magnitude values.
	 */
	@Override
	public int compareTo(Macroblock o) {
		if (this.magnitude < o.getMagnitude()) {
			return 1;
		} else if (this.magnitude > o.getMagnitude()) {
			return -1;
		} else {
			return 0;
		}
	}
}