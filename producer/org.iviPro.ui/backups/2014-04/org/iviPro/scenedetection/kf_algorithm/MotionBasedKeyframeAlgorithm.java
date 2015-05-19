package org.iviPro.scenedetection.kf_algorithm;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.media.Buffer;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import org.iviPro.scenedetection.sd_main.MiscOperations;
import org.iviPro.scenedetection.sd_main.ProgressDetermination;

/**
 * MotionBased keyframe extraction algorithm
 * 
 * @author Stefan Zwicklbauer
 */
public class MotionBasedKeyframeAlgorithm extends KeyframeExtractionAlgorithm {

	// Width of the filter window
	public static final int FWWIDTH = 80; // Standard 80

	// Height of the filter window
	public static final int FWHEIGHT = 80; // Standard 80

	public static final int ANGLEHISTOGRAMBINS = 16;

	public static final int TIMEWINDOW = 50;

	public static final int ANALYSEWINDOW = 10;

	private static final float PMETHRESHOLD = 0.10f;

	private static final int PMENORMALIZINGVALUE = 5;

	public static final float ALPHA = 0.4f;

	public static final int SHOTKEYFRAMESPLITTING = 10;

	public static int upperStripes = -1;

	public static int lowerStripes = -1;

	private BufferedImage image;

	private int width;

	private int height;

	private int threadId;

	private long[] threadFrames;

	private long[] cuts;

	private boolean firstFrame;

	private boolean isFilledUp;

	private MotionVectorAnalysis vectorAnalysis;

	private long framecounter;

	private BufferedImage lastImage;

	private List<Frame> frameBuffer;

	private Hashtable<Long, Float> pmeValues;

	private long lastFrames;

	/**
	 * Constructor
	 * 
	 * @param threadId
	 *            current threadId. If amount of cores is one, threadid is 0 by
	 *            default
	 * @param threadFrames
	 *            the amount of frames in video after current thread starts
	 * @param cuts
	 *            the framenumbers of all cuts respected to the current thread
	 *            working timeline
	 */
	public MotionBasedKeyframeAlgorithm(int threadId, long[] threadFrames,
			long[] cuts) {
		super();
		this.threadId = threadId;
		this.threadFrames = threadFrames;
		this.cuts = cuts;
		this.isFilledUp = false;
		this.firstFrame = true;
		this.framecounter = 0;
		this.frameBuffer = new LinkedList<Frame>();
		this.vectorAnalysis = new MotionVectorAnalysis();
		this.pmeValues = new Hashtable<Long, Float>(
				(int) (Math.floor(threadFrames[threadId] * (1 / 3))));
		CLASS_NAME = "MotionBasedKeyframeAlgorithm";
		if(threadId == 3) {
			System.out.println("ThreadFrames: "+threadFrames[3]);
			for (int i = 0; i < cuts.length; i++) {
				System.out.println("CutToDo: "+cuts[i]);
			}
		}
	}

	/**
	 * Function will be called up with each frame which is assigned to the
	 * specific thread. Analyses the current image. Calculates the motion
	 * direction and average motion vector field magnitude. The resulting PME
	 * value denotes the motion energy.
	 */
	@Override
	public int process(Buffer in, Buffer out) {
		// Store time and frameinfo
		storeInfo(in.getTimeStamp(), in.getSequenceNumber());
		framecounter++;
		if (getCurrentFrame() == framecounter) {
			VideoFormat format = (VideoFormat) in.getFormat();
			try {
				img = convertImageToBufferedImage(rescaleImage(in, format));
			} catch (NullPointerException e) {
				return BUFFER_PROCESSED_FAILED;
			}

			if (!firstFrame) {
				lastImage = image;
			}

			if (currentFrame % everyXFrames == 0) {
				image = convertImageToBufferedImage(img);

				width = image.getWidth();
				height = image.getHeight();
				if (firstFrame) {
					vectorAnalysis.setImageProperties(width, height);
					firstFrame = false;
				}
				calcPME();
			}
		}
		if (framecounter % 10 == 0) {
			ProgressDetermination
					.setProcessedFrames((int) (framecounter - lastFrames));
			lastFrames = framecounter;
		}

		passFrameThrough(in, out);
		return BUFFER_PROCESSED_OK;
	}

	private Image rescaleImage(Buffer in, VideoFormat format) {
		BufferedImage img = convertImageToBufferedImage(new BufferToImage(
				format).createImage(in));
		int width = img.getWidth();
		int height = img.getHeight();
		int newWidth = img.getWidth();
		int newHeight = img.getHeight();
		if (width % MotionVectorAnalysis.blocksize != 0) {
			newWidth = width / MotionVectorAnalysis.blocksize;
			newWidth *= MotionVectorAnalysis.blocksize;
		}
		if (height % MotionVectorAnalysis.blocksize != 0) {
			newHeight = height / MotionVectorAnalysis.blocksize;
			newHeight *= MotionVectorAnalysis.blocksize;
		}

		Image scaledVersion = new BufferToImage(format).createImage(in)
				.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
		return scaledVersion;
	}

	/**
	 * Returns the classname
	 */
	@Override
	public String getName() {
		return CLASS_NAME;
	}

	/**
	 * Calculates the described motion energy value and stores it in a hashmap.
	 * Later the values are used to determine the maximum pme value.
	 */
	private void calcPME() {
		// Framebuffer management
		if (framecounter != 0) {
			frameBuffer.add(new Frame(image, lastImage, framecounter));
		}

		if (frameBuffer.size() > TIMEWINDOW) {
			// Detect black stripes at upper and lower bound of movie
			if (upperStripes == -1 || lowerStripes == -1) {
				if (threadId == 0) {
					setStrippingParts();
				} else {
					threadWaitForStrippingParts();
				}
				updateFramesWithStrippingParameter(frameBuffer.size() - 1);
				isFilledUp = true;
			}

			if (!isFilledUp) {
				updateFramesWithStrippingParameter(frameBuffer.size() - 1);
				isFilledUp = true;
			}

			frameBuffer.get(frameBuffer.size() - 1).filterBlackMovieStripes(
					upperStripes, lowerStripes);
			float frameMagnitude = calcFrameMagnitude();
			float motionDirection = frameBuffer.get(0)
					.getMotionDirectionPercentage();
			float pme = frameMagnitude * motionDirection;

//			System.out.println("frameMagniude: "+frameMagnitude +"MotionDirection"+motionDirection);
			
			long position = frameBuffer.get(0).getFrameNr();
			if (threadId != 0) {
				position = threadFrames[threadId - 1]
						+ frameBuffer.get(0).getFrameNr();
			}

			// Check whether a cut is near of current position
			for (int i = 0; i < cuts.length; i++) {
				if (position < cuts[i] && (position + ANALYSEWINDOW) > cuts[i]) {
					pme = 0;
				}
			}

			// Add normalized value to Hashmap
//			System.out.println("PME: "+(pme / PMENORMALIZINGVALUE)+"POSITION: "+position);
			pmeValues.put(position, (pme / PMENORMALIZINGVALUE));
		}

		// MotionAnalyse reset
		vectorAnalysis.reset();

		// Framebuffer Management - Keep framebuffer.size() = 50
		if (frameBuffer.size() > TIMEWINDOW) {
			frameBuffer.remove(0);
		}
	}

	/**
	 * Thread 0 calculates the stripping parts.
	 */
	private void setStrippingParts() {
		BufferedImage[] images = new BufferedImage[frameBuffer.size()];
		for (int i = 0; i < frameBuffer.size(); i++) {
			images[i] = frameBuffer.get(i).getImage();
		}
		upperStripes = MiscOperations.detectBlackStripes(images,
				MotionVectorAnalysis.blocksize, false);
		lowerStripes = MiscOperations.detectBlackStripes(images,
				MotionVectorAnalysis.blocksize, true);
	}

	/**
	 * Iterates over the framebuffer and activates the stripping filter.
	 */
	private void updateFramesWithStrippingParameter(int size) {
		for (int i = 0; i < size; i++) {
			frameBuffer.get(i).filterBlackMovieStripes(upperStripes,
					lowerStripes);
		}
	}

	/**
	 * Thread is waiting for thread 0 has calculated the upper and lower
	 * stripping values
	 */
	private void threadWaitForStrippingParts() {
		while (upperStripes == -1 || lowerStripes == -1) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Calculates the average frame magnitude. All blocks which are not filtered
	 * by the black stripes algorithm are used.
	 * 
	 * @return the average frame magnitude of the first frame in framebuffer.
	 */
	private float calcFrameMagnitude() {
		if (frameBuffer.size() >= ANALYSEWINDOW) {
			float sum = 0;
			List<Macroblock> macroBlockMagnitudes = frameBuffer.get(0)
					.getMacroBlockList();
			for (Iterator<Macroblock> iterator = macroBlockMagnitudes
					.iterator(); iterator.hasNext();) {
				Macroblock macroblock = (Macroblock) iterator.next();
				sum += getMixtureEnergy(macroblock);
			}
			if (macroBlockMagnitudes.size() == 0) {
				return 0;
			} else {
				return (sum / (float) macroBlockMagnitudes.size());
			}
		} else {
			return -1;
		}
	}

	/**
	 * Calculates the mixture energy of a specific macroblock. The constant
	 * ANALYSEWINDOW is used to determine the best average value. The minimum
	 * and maximum value of the descending sorted list will be removed.
	 * 
	 * @param block
	 *            the specific block number
	 * @return average magnitude of a macroblock
	 */
	private float getMixtureEnergy(Macroblock block) {
		// Fill up tracking volume
		List<Macroblock> lst = new LinkedList<Macroblock>();
		for (int i = 0; i < ANALYSEWINDOW; i++) {
			lst.add(frameBuffer.get(i).getSpecifiedBlock(block.getBlockNr()));
		}
		Collections.sort(lst);
		// Trimming
		lst.remove(0);
		lst.remove(lst.size() - 1);

		int start = ((int) Math.floor(ANALYSEWINDOW * ALPHA)) + 1;
		int end = ANALYSEWINDOW - ((int) Math.floor(ANALYSEWINDOW * ALPHA));
		float sum = 0;
		for (int i = start; i <= end; i++) {
			sum += lst.get(i).getMagnitude();
		}
		float firstProduct = (1 / ((float) ANALYSEWINDOW - 2 * ((float) Math
				.floor((float) ANALYSEWINDOW * ALPHA))));
		return firstProduct * sum;
	}

	/**
	 * To finish the algorithm, the list of keyframe (framenumber) will be
	 * returned. First of all the remaining framebuffer will be worked. Then the
	 * keyframe extraction determines the framenumber of the calculated
	 * keyframe.
	 * 
	 * 
	 * @return list of keyframe framenumbers
	 */
	public List<Long> finalizeAlgorithm() {
		// Finish calculations

		// Detect black stripes at upper and lower bound of movie
		if (upperStripes == -1 || lowerStripes == -1) {
			if (threadId == 0) {
				upperStripes = 0;
				lowerStripes = 0;
			} else {
				threadWaitForStrippingParts();
			}
			updateFramesWithStrippingParameter(frameBuffer.size());
		}

		while (frameBuffer.size() >= ANALYSEWINDOW) {
			float frameMagnitude = calcFrameMagnitude();
			float motionDirection = frameBuffer.get(0)
					.getMotionDirectionPercentage();
			float pme = frameMagnitude * motionDirection;
			long position = frameBuffer.get(0).getFrameNr();
			if (threadId != 0) {
				position = threadFrames[threadId - 1]
						+ frameBuffer.get(0).getFrameNr();
			}
			pmeValues.put(position, (pme / PMENORMALIZINGVALUE));
			frameBuffer.remove(0);
		}
		for (int i = 0; i < frameBuffer.size(); i++) {
			if (threadId == 0) {
				pmeValues.put(frameBuffer.get(i).getFrameNr(), 0.0f);
			} else {
				pmeValues.put(threadFrames[threadId - 1]
						+ frameBuffer.get(i).getFrameNr(), 0.0f);
			}
		}

		// Keyframe extraction
		List<Long> result = new LinkedList<Long>();
		// Create triangles in each shot
		long startPosition = 1;
		long endPosition = 0;
		boolean cutAvailableInThread = false;
		for (int i = 0; i < cuts.length; i++) {
			if (isCutInThreadWork(cuts[i])) {
				cutAvailableInThread = true;
			}
		}
		if (cuts.length == 0 || (!cutAvailableInThread)) {
			if (threadId != 0) {
				startPosition += threadFrames[threadId - 1];
			}
			endPosition = threadFrames[threadId] - ANALYSEWINDOW + 2;
			result.addAll(calcKeyFrames(startPosition, endPosition));
		} else {
			// Iterate over cuts and extract keyframe numbers
			long lastCutPosition = 0;
			if (threadId == 0) {
				lastCutPosition = 1;
			} else {
				lastCutPosition = threadFrames[threadId - 1] + 1;
			}
			for (int i = 0; i < cuts.length; i++) {
				if (isCutInThreadWork(cuts[i])) {
					startPosition = lastCutPosition;
					endPosition = cuts[i] - 1;
					result.addAll(calcKeyFrames(startPosition, endPosition));
					lastCutPosition = cuts[i];
				}
			}
			// Last shot
			startPosition = lastCutPosition;
			endPosition = threadFrames[threadId] - ANALYSEWINDOW + 2;
			result.addAll(calcKeyFrames(startPosition, endPosition));
		}

		return result;
	}

	/**
	 * Checks whether the framenumber of a cut must be regarded in the current
	 * thread
	 * 
	 * @param cutFrame
	 *            the framenumber
	 * @return true if framenumber is relevant in this thread
	 */
	private boolean isCutInThreadWork(long cutFrame) {
		long startPosition = 1;
		if (threadId != 0) {
			startPosition = threadFrames[threadId - 1] + 1;
		}
		if (startPosition <= cutFrame && threadFrames[threadId] >= cutFrame) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Calculates keyframes within a given range. If no value exceeds the
	 * minimum pme threshold value, the maximum value will be chosen. More than
	 * one keyframe within a range can be chosen to represent a shot.
	 * 
	 * @param startCut
	 *            startcut of detection range
	 * @param endCut
	 *            endcut of detection range
	 * @return returns a list of frame numbers which represent a keyframe
	 */
	private List<Long> calcKeyFrames(long startCut, long endCut) {
		List<Long> lst = new LinkedList<Long>();
		long iterator = startCut;
		long position = -1;
		// If cutstart != 0, set startcut value to zero to simulate the triangle
		// problem
		if (pmeValues.get(startCut) != 0) {
			pmeValues.put(startCut, 0.0f);
		}
		long diff = endCut - startCut;
		long amountKF =  Math.round(Math.log10(diff) / Math.log10(5));
		if(amountKF == 0) {
			amountKF = 1;
		}
		long toDo = diff / amountKF;
		System.out.println("todo: "+toDo);
		
		for (int i = 0; i < amountKF; i++) {
			System.out.println("KEYFRAMEOUTPUTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"+calcMaxPme(startCut + i * toDo, startCut + (i + 1) * toDo));
			lst.add(calcMaxPme(startCut + i * toDo, startCut + (i + 1) * toDo));
		}
		
		if (lst.size() == 0) {
			iterator = startCut;
			float maxVal = 0.0f;
			while (iterator <= endCut) {
				if (pmeValues.get(iterator) > maxVal) {
					maxVal = pmeValues.get(iterator);
					position = iterator;
				}
				iterator++;
			}
			if (position == -1) {
				position = startCut
						+ ((int) Math.floor((endCut - startCut) / 2));
			}
			System.out.println("Spontanadding: "+position);
			lst.add(position);
		}
		
		return lst;
	}

	/**
	 * Calculates the maximum pme value of a given framerange. If the maximum
	 * value does not increase the threshold no possible keyframe number will be
	 * returned.
	 * 
	 * @param startCut
	 *            startcut of the given range
	 * @param endCut
	 *            endcut of given range
	 * @return Returns the relevant keyframe number. -1 if no keyframe number is
	 *         relevant.
	 */
	private long calcMaxPme(long startCut, long endCut) {
		float maxVal = 0;
		long position = 0;
		long iterator = startCut;
		while (iterator < endCut) {
//			System.out.println("AM ITERATOR GETTEN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!+"+pmeValues.get(iterator)+"IT: "+iterator);
			if (pmeValues.get(iterator) > maxVal) {
				maxVal = pmeValues.get(iterator);
				position = iterator;
			}
			iterator++;
		}
		if(position == 0) {
			position = startCut
			+ ((int) Math.floor((endCut - startCut) / 2));
		}
//		System.out.println("RETURN POSITIOn BY START:"+startCut +"AND"+endCut);
		return position;
	}

	/**
	 * Intern representation of frames
	 */
	private class Frame {

		private BufferedImage image;

		private long frameNr;

		private List<Macroblock> macroblockLst;

		private float motionDirectionPercentage;

		private int temporalHeight;

		/**
		 * Constructor
		 * 
		 * @param image
		 *            Image which represents the frame
		 * @param lastImage
		 *            image of the frame before
		 * @param frameNr
		 *            framenumber of current frame
		 */
		private Frame(BufferedImage image, BufferedImage lastImage, long frameNr) {
			this.image = image;
			this.frameNr = frameNr;
			this.macroblockLst = new LinkedList<Macroblock>();
			this.temporalHeight = height;
			vectorAnalysis.analyse(image);
			if (lastImage != null) {
				this.macroblockLst = vectorAnalysis.generateMacroblocks();
			}
		}

		/**
		 * Gets the frame image
		 * 
		 * @return frame image
		 */
		BufferedImage getImage() {
			return this.image;
		}

		/**
		 * Gets motion direction of this frame
		 * 
		 * @return motion direction
		 */
		float getMotionDirectionPercentage() {
			return motionDirectionPercentage;
		}

		/**
		 * Gets framenumber of the current frame
		 * 
		 * @returns the framenumber
		 */
		private long getFrameNr() {
			return frameNr;
		}

		/**
		 * Gets the macroblock list
		 * 
		 * @return list of macroblock
		 */
		List<Macroblock> getMacroBlockList() {
			return this.macroblockLst;
		}

		/**
		 * Gets a specified block
		 * 
		 * @param nr
		 *            number of the block which should be returned
		 * @return the block value
		 */
		Macroblock getSpecifiedBlock(int nr) {
			return macroblockLst.get(nr);
		}

		/**
		 * Creates a filterwindow of specific macroblock in a frame. The
		 * filterwindow is used to average the frame magnitude of a block.
		 * 
		 * @param idBlock
		 *            the block which filter should be created
		 * @return A list of referenced macroblocks belonging to the specified
		 *         block
		 */
		private List<Macroblock> createFilterWindowList(int idBlock) {
			int amountX = FWWIDTH / MotionVectorAnalysis.blocksize;
			int amountY = FWHEIGHT / MotionVectorAnalysis.blocksize;
			if ((amountX % 2 == 0) || (amountY % 2 == 0)) {
				System.err.println("Not able to calculate filterwindow!");
			}
			int blocksPerRow = width / MotionVectorAnalysis.blocksize;
			List<Macroblock> lst = new LinkedList<Macroblock>();
			int blockrow = (int) (Math.floor(idBlock / blocksPerRow));
			int blockcolumn = idBlock % blocksPerRow;

			for (int i = blockrow - ((int) Math.floor(amountY / 2)); i <= blockrow
					+ ((int) Math.floor(amountY / 2)); i++) {
				for (int j = blockcolumn - ((int) Math.floor(amountX / 2)); j <= blockcolumn
						+ ((int) Math.floor(amountX / 2)); j++) {
					int index = i * blocksPerRow + j;
					if ((i >= 0)
							&& (j >= 0)
							&& (index >= 0)
							&& (i < temporalHeight
									/ MotionVectorAnalysis.blocksize)
							&& (j < width / MotionVectorAnalysis.blocksize)) {
						lst.add(macroblockLst.get(index));
					}
				}
			}
			return lst;
		}

		/**
		 * Filters black movie stripes at the top and bottom of the video. The
		 * respected macroblocks are not in use anymore and will be deleted.
		 * After calculations are repeated.
		 * 
		 * @param upper
		 *            amount of upper rows of macroblocks which should be
		 *            removed
		 * @param lower
		 *            amount of ulower rows of macroblocks which should be
		 *            removed
		 */
		void filterBlackMovieStripes(int upper, int lower) {
//			int blocksInRow = width / MotionVectorAnalysis.blocksize;
//			int upperDelete = upper * blocksInRow;
//			int lowerDelete = lower * blocksInRow;
//			for (int i = 0; i < upperDelete; i++) {
//				if (macroblockLst.size() > 0) {
//					macroblockLst.remove(0);
//				}
//			}
//
//			// Decrease blocknumber
//			for (int i = 0; i < macroblockLst.size(); i++) {
//				macroblockLst.get(i).setBlocknr(
//						macroblockLst.get(i).getBlockNr() - upperDelete);
//			}
//
//			for (int i = 0; i < lowerDelete; i++) {
//				if (macroblockLst.size() > 0) {
//					macroblockLst.remove(macroblockLst.size() - 1);
//				}
//			}
//			this.temporalHeight = temporalHeight - upper
//					* MotionVectorAnalysis.blocksize - lower
//					* MotionVectorAnalysis.blocksize;
			reCalculate();
		}

		/**
		 * Makes some recalculations after filtering stripes.
		 */
		private void reCalculate() {
			for (Iterator<Macroblock> iterator = macroblockLst.iterator(); iterator
					.hasNext();) {
				Macroblock block = (Macroblock) iterator.next();
				block.setFilterWindow(createFilterWindowList(block.getBlockNr()));
			}
			this.motionDirectionPercentage = calcMotionDirectionPercentage(setAngleHistogram());
			this.filterMagnitude();
		}

		/**
		 * Creates a histogram of all macroblock angles in a frame. The amount
		 * of bins is 16 by default
		 * 
		 * @return histogram in array form
		 */
		private int[] setAngleHistogram() {
			int[] histogram = new int[ANGLEHISTOGRAMBINS - 1];
			for (Iterator<Macroblock> iterator = macroblockLst.iterator(); iterator
					.hasNext();) {
				Macroblock block = (Macroblock) iterator.next();
				float angle = block.getAngle();
				if (angle != -1) {
					float temp = ((float) 360)
							/ ((float) ANGLEHISTOGRAMBINS - 1);
					int val = (int) (Math.floor(angle / temp));
					histogram[val]++;
				}
			}
			return histogram;
		}

		/**
		 * Function to calculate the motion direction. The highest histogram
		 * value is used to calculate the direction percentage
		 * 
		 * @param histogram
		 *            the angle histogram
		 * @return motion direction value
		 */
		private float calcMotionDirectionPercentage(int[] histogram) {
			int sum = 0;
			int max = 0;
			for (int i = 0; i < histogram.length; i++) {
				if (histogram[i] > max) {
					max = histogram[i];
				}
				sum += histogram[i];
			}
			if (sum == 0) {
				return 0;
			} else {
				return ((float) max) / ((float) sum);
			}
		}

		/**
		 * Filters a macroblock. Removes highest and lowest magnitude value in
		 * the filter window and choses the fourth value in this list. The
		 * fourth value is determined from experiments.
		 */
		private void filterMagnitude() {
			for (Iterator<Macroblock> iterator = macroblockLst.iterator(); iterator
					.hasNext();) {
				Macroblock block = (Macroblock) iterator.next();
				float mag = block.getMagnitude();
				/*
				 * Filter: See paper A NEW PERCEIVED MOTION BASED SHOT CONTENT
				 * REPRESENTATION The choice of returning the fourth value is
				 * determined from experiments.
				 */
				float fourthVal = block.getFourthFilterWindowValue();
				if (mag > fourthVal) {
					block.setMagnitude(fourthVal);
				}
			}
		}
	}
}
