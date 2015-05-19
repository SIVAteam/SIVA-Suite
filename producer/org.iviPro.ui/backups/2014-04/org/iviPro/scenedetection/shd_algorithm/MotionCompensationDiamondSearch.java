package org.iviPro.scenedetection.shd_algorithm;

/**
 * Berechnet den Bewegungsunterschied zwischen 2 Bildern.
 * 
 * @author Stefan Zwicklbauer
 */
public class MotionCompensationDiamondSearch extends ThreeFeatures {

	static final int SIZEBOX = 16;
	static final int BOUNDINGBOX = 32;
	static final int AMOUNTDIAMONDPOINTS = 13;
	static final int AMOUNTSMALLDIAMONDPOINTS = 5;
	final int[][] DIAMONDCOORDINATES = { { -2, 0 }, { -1, 0 }, { -1, 1 },
			{ -1, -1 }, { 0, 2 }, { 0, 1 }, { 0, -1 }, { 0, -2 }, { 1, 0 },
			{ 1, 1 }, { 1, -1 }, { 2, 0 }, { 0, 0 } };
	final int[][] SMALLDIAMONDCOORDINATES = { { -1, 0 }, { 0, -1 }, { 0, 1 },
			{ 1, 0 }, { 0, 0 } };
	private float[][] lastYValues;
	private int amountboxesX;
	private int amountboxesY;
	private int amountboxes;

	public MotionCompensationDiamondSearch(int width, int height) {
		super(width, height);
		lastYValues = new float[height][width];
		amountboxesX = (int) Math.floor(width / SIZEBOX);
		amountboxesY = (int) Math.floor(height / SIZEBOX);
		amountboxes = amountboxesX * amountboxesY;
	}

	@Override
	public float getPeakValue() {
		float[][] currentValues = new float[height][width];

		for (int j = 0; j < height; j++) {
			for (int j2 = 0; j2 < width; j2++) {
				int arrayindex = j * width + j2;
				currentValues[j][j2] = ShotDetectionSettings.Y_VALUE_R
						* currentFrameRGB[0][arrayindex]
						+ ShotDetectionSettings.Y_VALUE_G
						* currentFrameRGB[1][arrayindex]
						+ ShotDetectionSettings.Y_VALUE_B
						* currentFrameRGB[2][arrayindex];
			}
		}

		float result = 0;
		for (int i = 0; i < amountboxes; i++) {
			int diamondPoint = -1;

			int[] standbox = new int[2];
			standbox[0] = (i % amountboxesX) * SIZEBOX;
			standbox[1] = (int) Math.floor((i / amountboxesX)) * SIZEBOX;

			int[] startSearchPosition = new int[2];
			startSearchPosition[0] = calculateStartPosition(standbox[0]);
			startSearchPosition[1] = calculateStartPosition(standbox[1]);

			int[] moving = new int[2];
			moving[0] = calculateMoveX(standbox[0]);
			moving[1] = calculateMoveY(standbox[1]);

			int[] boxCenter = new int[2];
			boxCenter[0] = startSearchPosition[0] + (moving[0] / 2);
			boxCenter[1] = startSearchPosition[1] + (moving[1] / 2);

			int[] bestMatching = new int[2];

			int finalsum = 0;
			while (diamondPoint != AMOUNTDIAMONDPOINTS - 1) {
				diamondPoint = AMOUNTDIAMONDPOINTS - 1;
				int sum = 0;
				for (int j = 0; j < SIZEBOX; j++) {
					for (int j2 = 0; j2 < SIZEBOX; j2++) {
						sum += Math
								.abs(currentValues[standbox[1] + j][standbox[0]
										+ j2]
										- lastYValues[boxCenter[1] + j][boxCenter[0]
												+ j2]);
					}
				}
				int[] calculation = calculateSum(DIAMONDCOORDINATES,
						AMOUNTDIAMONDPOINTS, sum, currentValues,
						startSearchPosition, boxCenter, moving, standbox);
				diamondPoint = calculation[0];
				finalsum = calculation[1];
				boxCenter[0] = boxCenter[0]
						+ DIAMONDCOORDINATES[diamondPoint][0];
				boxCenter[1] = boxCenter[1]
						+ DIAMONDCOORDINATES[diamondPoint][1];
			}

			int[] finalcalculation = calculateSum(SMALLDIAMONDCOORDINATES,
					AMOUNTSMALLDIAMONDPOINTS, finalsum, currentValues,
					startSearchPosition, boxCenter, moving, standbox);
			int finalDiamondPoint = finalcalculation[0];
			bestMatching[0] = boxCenter[0]
					+ SMALLDIAMONDCOORDINATES[finalDiamondPoint][0];
			bestMatching[1] = boxCenter[1]
					+ SMALLDIAMONDCOORDINATES[finalDiamondPoint][1];

			float boxYvalues = 0;
			for (int j = 0; j < SIZEBOX; j++) {
				for (int j2 = 0; j2 < SIZEBOX; j2++) {
					boxYvalues += currentValues[standbox[1] + j][standbox[0]
							+ j2];
				}
			}

			float movedBoxValues = 0;
			for (int j = 0; j < SIZEBOX; j++) {
				for (int j2 = 0; j2 < SIZEBOX; j2++) {
					movedBoxValues += lastYValues[bestMatching[1] + j][bestMatching[0]
							+ j2];
				}
			}
			boxYvalues = boxYvalues / (SIZEBOX * SIZEBOX);
			movedBoxValues = movedBoxValues / (SIZEBOX * SIZEBOX);

			result += Math.abs(boxYvalues - movedBoxValues);
		}
		result = result / amountboxes;
		lastYValues = currentValues;
		return (int) Math.pow(result * 2000, 1);
	}

	private int[] calculateSum(int[][] diamondValues, int diamondamount,
			int sum, float[][] values, int[] realStartBoundBox,
			int[] upperLeftBoxCoordinates, int[] move, int[] defaultbox) {
		int nredge = diamondamount - 1;
		int[] sumamount = new int[diamondamount];
		sumamount[diamondamount - 1] = sum;
		for (int j = 0; j < diamondamount - 1; j++) {
			sum = 0;
			// Automatische BoundingBoxOutOfBoundsDetection
			if (boundingCheck(realStartBoundBox[0], realStartBoundBox[1],
					upperLeftBoxCoordinates[0], upperLeftBoxCoordinates[1],
					diamondValues[j][0], diamondValues[j][1], move[0], move[1])) {
				for (int i = 0; i < SIZEBOX; i++) {
					for (int k = 0; k < SIZEBOX; k++) {
						sum += Math
								.abs(values[defaultbox[1] + i][defaultbox[0]
										+ k]
										- lastYValues[upperLeftBoxCoordinates[1]
												+ i + diamondValues[j][1]][upperLeftBoxCoordinates[0]
												+ k + diamondValues[j][0]]);
					}
				}
				// Speichert die Summe von jedem Punkt im Array ab um nachher
				// die kleinste Summe zu ermitteln
				sumamount[j] = sum;
				if (sumamount[j] < sumamount[nredge]) {
					nredge = j;
				}
			}
		}
		int[] returnvalue = new int[2];
		returnvalue[0] = nredge;
		returnvalue[1] = sumamount[nredge];
		return returnvalue;
	}

	private boolean boundingCheck(int realSpawnX, int realSpawnY, int centerX,
			int centerY, int translateX, int translateY, int moveX, int moveY) {

		// Ueberprueft ob der jeweilige Diamondpunkt ausserhalb der horizontalen
		// Achse liegt!
		if (centerX + translateX < realSpawnX
				|| centerX + translateX + SIZEBOX > realSpawnX + SIZEBOX
						+ moveX) {
			return false;
		}
		// ueberprueft ob der jeweilige Diamondpunkt ausserhalb der vertikalen
		// Achse liegt!
		if (centerY + translateY < realSpawnY
				|| centerY + translateY + SIZEBOX > realSpawnY + SIZEBOX
						+ moveY) {
			return false;
		}
		return true;
	}

	@Override
	public void initializeFirstFrameValues() {
		for (int j = 0; j < height; j++) {
			for (int j2 = 0; j2 < width; j2++) {
				int arrayindex = j * width + j2;
				lastYValues[j][j2] = ShotDetectionSettings.Y_VALUE_R
						* currentFrameRGB[0][arrayindex]
						+ ShotDetectionSettings.Y_VALUE_G
						* currentFrameRGB[1][arrayindex]
						+ ShotDetectionSettings.Y_VALUE_B
						* currentFrameRGB[2][arrayindex];
			}
		}
	}

	private int calculateStartPosition(int standBox) {
		if (standBox - ((BOUNDINGBOX - SIZEBOX) / 2) < 0) {
			return 0;
		}
		return (standBox - ((BOUNDINGBOX - SIZEBOX) / 2));
	}

	private int calculateMoveX(int standBox) {
		int mover = BOUNDINGBOX - SIZEBOX;
		if (standBox - ((BOUNDINGBOX - SIZEBOX) / 2) < 0) {
			mover = BOUNDINGBOX - SIZEBOX
					- (Math.abs((standBox - ((BOUNDINGBOX - SIZEBOX) / 2))));
		} else if (standBox + SIZEBOX + ((BOUNDINGBOX - SIZEBOX) / 2) > width) {
			mover = BOUNDINGBOX
					- SIZEBOX
					- ((standBox + SIZEBOX + ((BOUNDINGBOX - SIZEBOX) / 2)) - width);
		}
		return mover;
	}

	private int calculateMoveY(int standBox) {
		int mover = BOUNDINGBOX - SIZEBOX;
		if (standBox - ((BOUNDINGBOX - SIZEBOX) / 2) < 0) {
			mover = BOUNDINGBOX - SIZEBOX
					- (Math.abs((standBox - ((BOUNDINGBOX - SIZEBOX) / 2))));
		} else if (standBox + SIZEBOX + ((BOUNDINGBOX - SIZEBOX) / 2) > height) {
			mover = BOUNDINGBOX
					- SIZEBOX
					- ((standBox + SIZEBOX + ((BOUNDINGBOX - SIZEBOX) / 2)) - height);
		}
		return mover;
	}
}
