package org.iviPro.scenedetection.kf_algorithm;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Extracts motion information between two consecutive frames E.g. the magnitude
 * and angle of a motion vector used for motion estimation
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class MotionVectorAnalysis {

	/* Diamond Position of LDSP */
	final static int[][] DIAMONDCOORDINATES = { { 0, 0 }, { -2, 0 }, { -1, 1 },
			{ -1, -1 }, { 0, 2 }, { 0, -2 }, { 1, 1 }, { 1, -1 }, { 2, 0 } };

	/* Diamond Position of SDSP */
	final static int[][] SMALLDIAMONDCOORDINATES = { { 0, 0 }, { -1, 0 },
			{ 0, -1 }, { 0, 1 }, { 1, 0 } };

	/* Size of Macroblock */
	final static int blocksize = 16;

	/* Max Length of a Moionb vector */
	final static int MAXLENGTH = 10;

	private int width;

	private int height;

	private int[][] luminanceTargetMatrix;

	private int[][] luminanceSourceMatrix;

	private List<ShiftedMacroblock> blockLst;

	private boolean initialized;

	/**
	 * Constructor
	 * 
	 */
	MotionVectorAnalysis() {
		blockLst = new LinkedList<ShiftedMacroblock>();
		initialized = false;
	}

	/**
	 * Sets image width / height
	 * 
	 * @param width
	 *            width of comparing images
	 * @param height
	 *            height of comparing images
	 */
	void setImageProperties(int width, int height) {
		this.width = width;
		this.height = height;
		if (!checkValidity()) {
			this.width = 0;
			this.height = 0;
			System.err
					.println("Image Size is wrong. Width/Height mod blocksize must be zero!");
		}
	}

	/**
	 * Returns a list of available Macroblocks
	 * 
	 * @return list of macroblocks
	 */
	LinkedList<Macroblock> generateMacroblocks() {
		LinkedList<Macroblock> result = new LinkedList<Macroblock>();
		for (Iterator<ShiftedMacroblock> iterator = blockLst.iterator(); iterator
				.hasNext();) {
			ShiftedMacroblock block = (ShiftedMacroblock) iterator.next();
			result.add(new Macroblock(block.getBlockNr(), block.getMagnitude(),
					block.getAngle()));
		}
		return result;
	}

	/**
	 * Starts the analyzing of the two images
	 * 
	 * @param image1
	 *            targetimage
	 * @param image2
	 *            sourceimage
	 */
	void analyse(BufferedImage image) {
		if (!initialized) {
			luminanceTargetMatrix = setTargetMatrix(transformLuminance(image));
			initialized = true;
		} else {
			luminanceSourceMatrix = setTargetMatrix(transformLuminance(image));
			extractMacroblocks();
			luminanceTargetMatrix = luminanceSourceMatrix;
		}
	}

	/**
	 * Transforms an arbitrary image into a gray scale image and saved this an
	 * array.
	 * 
	 * @param image
	 *            standardimage
	 * @return array of grayvalues with size width * height. Arrangement: row by
	 *         row
	 */
	private int[] transformLuminance(BufferedImage image) {
		int[] luminance = new int[height * width];
		BufferedImage gray = new BufferedImage(width, height,
				BufferedImage.TYPE_BYTE_GRAY);
		ColorConvertOp grayScaleConversionOp = new ColorConvertOp(
				ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		grayScaleConversionOp.filter(image, gray);
		Raster raster = gray.getRaster();
		raster.getSamples(0, 0, width, height, 0, luminance);
		return luminance;
	}

	/**
	 * Iterate over all existing macroblocks and search the best matching block
	 */
	private void extractMacroblocks() {
		int amountBlocks = (width * height) / (blocksize * blocksize);
		for (int i = 0; i < amountBlocks; i++) {
			ShiftedMacroblock block = searchBestMacroblock(i);
			// If return value is null no macroblock will be saved
			if (block != null) {
				blockLst.add(block);
			}
		}
	}

	/**
	 * DiamondSearch Pattern
	 * 
	 * @param blockNr
	 *            the blockNr which is computed
	 * @return the best ShiftedMacroblock which coult be found with
	 *         DiamondSearch Pattern
	 */
	private ShiftedMacroblock searchBestMacroblock(int blockNr) {
		int[] sourceBlockValues = new int[blocksize * blocksize];
		int blocksPerRow = width / blocksize;
		int currentStartY = ((int) Math.floor(blockNr / blocksPerRow))
				* blocksize;
		int currentStartX = (blockNr % blocksPerRow) * blocksize;

		int shiftedValueX = currentStartX;
		int shiftedValueY = currentStartY;

		// Fill Source Matrix
		for (int i = 0; i < blocksize; i++) {
			for (int j = 0; j < blocksize; j++) {
				sourceBlockValues[i * blocksize + j] = luminanceSourceMatrix[currentStartY
						+ i][currentStartX + j];
			}
		}

		// Big diamond search pattern
		boolean isCenterPoint = false;
		while (!isCenterPoint) {
			int bestSAD = Integer.MAX_VALUE;
			int bestSADPosition = 0;
			for (int i = 0; i < DIAMONDCOORDINATES.length; i++) {
				if (checkDiamondValidity(shiftedValueX, shiftedValueY, i, true)) {
					int[] targetValues = new int[blocksize * blocksize];
					for (int j = 0; j < blocksize; j++) {
						for (int j2 = 0; j2 < blocksize; j2++) {
							targetValues[j * blocksize + j2] = luminanceTargetMatrix[shiftedValueY
									+ j + DIAMONDCOORDINATES[i][1]][shiftedValueX
									+ j2 + DIAMONDCOORDINATES[i][0]];
						}
					}
					int currentSAD = calcSADBlocks(sourceBlockValues,
							targetValues);

					if (currentSAD < bestSAD) {
						bestSAD = currentSAD;
						bestSADPosition = i;
					}
				}
			}
			if (bestSADPosition == 0) {
				isCenterPoint = true;
			} else {
				shiftedValueX += DIAMONDCOORDINATES[bestSADPosition][0];
				shiftedValueY += DIAMONDCOORDINATES[bestSADPosition][1];
			}
		}

		// small diamond search pattern
		int bestSAD = Integer.MAX_VALUE;
		int bestSADPosition = 0;
		for (int i = 0; i < SMALLDIAMONDCOORDINATES.length; i++) {
			if (checkDiamondValidity(shiftedValueX, shiftedValueY, i, false)) {
				int[] targetValues = new int[blocksize * blocksize];
				for (int j = 0; j < blocksize; j++) {
					for (int j2 = 0; j2 < blocksize; j2++) {
						targetValues[j * blocksize + j2] = luminanceTargetMatrix[shiftedValueY
								+ j + SMALLDIAMONDCOORDINATES[i][1]][shiftedValueX
								+ j2 + SMALLDIAMONDCOORDINATES[i][0]];
					}
				}
				int currentSAD = calcSADBlocks(sourceBlockValues, targetValues);
				if (currentSAD < bestSAD) {
					bestSAD = currentSAD;
					bestSADPosition = i;
				}
			}
		}
		ShiftedMacroblock block = new ShiftedMacroblock(currentStartX,
				currentStartY, shiftedValueX
						+ SMALLDIAMONDCOORDINATES[bestSADPosition][0],
				shiftedValueY + SMALLDIAMONDCOORDINATES[bestSADPosition][1],
				blockNr);

		return block;
	}

	/**
	 * Check of coordinates are out of bound
	 * 
	 * @param posX
	 *            left boxvalue
	 * @param posY
	 *            upper boxvalue
	 * @return false if not possible
	 */
	private boolean checkDiamondValidity(int posX, int posY,
			int diamondPosition, boolean largeDiamonds) {
		int positionX = 0, positionY = 0;
		if (largeDiamonds) {
			positionX = posX + DIAMONDCOORDINATES[diamondPosition][0];
			positionY = posY + DIAMONDCOORDINATES[diamondPosition][1];
		} else {
			positionX = posX + SMALLDIAMONDCOORDINATES[diamondPosition][0];
			positionY = posY + SMALLDIAMONDCOORDINATES[diamondPosition][1];
		}
		if (positionX < 0 || positionY < 0) {
			return false;
		} else if ((positionX + blocksize > width)
				|| (positionY + blocksize > height)) {
			return false;
		}
		return true;
	}

	/**
	 * Calculates the Sum of absolut differences between two blocks.
	 * 
	 * @param block1
	 *            the first block
	 * @param block2
	 *            the second block
	 * @return the sum of absolut differences
	 */
	private int calcSADBlocks(int[] block1, int[] block2) {
		if (block1.length != block2.length) {
			return 0;
		}
		int sum = 0;
		for (int i = 0; i < block1.length; i++) {
			sum += Math.abs(block1[i] - block2[i]);
		}
		return sum;
	}

	/**
	 * Resets all information to start a new image comparison
	 */
	void reset() {
		this.blockLst = new LinkedList<ShiftedMacroblock>();
	}

	/**
	 * Checks whether the video format has the necessary properties. Width and
	 * height must be divisible through blocksize
	 * 
	 * @return true if divisible
	 */
	private boolean checkValidity() {
		if ((width % blocksize != 0) || (height % blocksize != 0)) {
			return false;
		}
		return true;
	}

	/**
	 * Sets the targetmatrix. First dimension is height. Second dimension is
	 * width.
	 * 
	 * @param luminance
	 *            luminance values of the picture
	 * @return the targetmatrix
	 */
	private int[][] setTargetMatrix(int[] luminance) {
		int[][] matrix = new int[height][width];
		for (int i = 0; i < luminance.length; i++) {
			int y = (int) (Math.floor(i / width));
			int x = i % width;
			matrix[y][x] = luminance[i];
		}
		return matrix;
	}

	/**
	 * 
	 * Intern representation of a macroblock. Provides magnitude and angle
	 * calculation
	 * 
	 */
	private class ShiftedMacroblock {

		private int originalX;

		private int originalY;

		private int shiftedX;

		private int shiftedY;

		private float magnitude;

		private float angle;

		private int blockNr;

		/**
		 * Constructor
		 * 
		 * @param originalX
		 *            x value of the original block (left value)
		 * @param originalY
		 *            y value of the original block (upper value)
		 * @param shiftedX
		 *            x value of the shifted block (left value)
		 * @param shiftedY
		 *            y value of the shifted block (upper value)
		 */
		private ShiftedMacroblock(int originalX, int originalY, int shiftedX,
				int shiftedY, int blockNr) {
			this.originalX = originalX;
			this.originalY = originalY;
			this.shiftedX = shiftedX;
			this.shiftedY = shiftedY;
			this.magnitude = calcMagnitude();
			this.angle = calcAngle();
			this.blockNr = blockNr;
		}

		/**
		 * Returns magnitude of the motionvector
		 * 
		 * @return magnitude
		 */
		float getMagnitude() {
			return this.magnitude;
		}

		/**
		 * Returns angle of the motionvector
		 * 
		 * @return magnitude
		 */
		float getAngle() {
			return this.angle;
		}

		/**
		 * Returns blockNr of the this block
		 * 
		 * @return blockNr
		 */
		int getBlockNr() {
			return this.blockNr;
		}

		/**
		 * Calculates the magnitude of the motionvector
		 * 
		 * @return magnitude
		 */
		private float calcMagnitude() {
			int xdif = shiftedX - originalX;
			int ydif = shiftedY - originalY;
			float result = (float) Math.abs(Math.sqrt((xdif * xdif)
					+ (ydif * ydif)));
			if (result > MAXLENGTH) {
				return 10.0f;
			} else {
				return result;
			}
		}

		/**
		 * Calculates the angle of the motionvector
		 * 
		 * @return angle
		 */
		private float calcAngle() {
			if (shiftedX == originalX && shiftedY == originalY) {
				return -1;
			}
			double value = Math.atan2(Math.abs((originalY - shiftedY)),
					Math.abs(originalX - shiftedX));

			if (originalX > shiftedX && originalY < shiftedY) {
				value = Math.PI / 2 + Math.PI / 2 - value;
			}

			if (originalY >= shiftedY && originalX > shiftedX) {
				value = Math.PI + value;
			}

			if (originalY > shiftedY && originalX <= shiftedX) {
				value = Math.PI + Math.PI / 2 + Math.PI / 2 - value;
			}
			return (float) Math.toDegrees(value);
		}
	}
}
