package org.iviPro.scenedetection.kf_algorithm;

import java.util.LinkedList;
import java.util.List;
import javax.media.Buffer;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import javax.swing.JFrame;

import org.iviPro.scenedetection.sd_main.Keyframe;

/**
 * Algorithm to extract specified frames to save them together with a
 * framenumber as a keyframe object.
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class MotionBasedImageExtraction extends KeyframeExtractionAlgorithm {

	private List<Long> framesToExtract;

	private long framecounter;

	private int position;

	private List<Keyframe> keyframeLst;

	private int threadId;

	private long[] threadFrames;

	/**
	 * Constructor
	 * 
	 * @param threadId
	 *            current threadId. If amount of cores is one, threadid is 0 by
	 *            default
	 * @param threadFrames
	 *            amount of cores in use
	 * @param frames
	 *            the frames which should be extracted. The image if each frame
	 *            will be saved with the framenumberF
	 */
	public MotionBasedImageExtraction(int threadId, long[] threadFrames,
			List<Long> frames) {
		super();
		this.framesToExtract = frames;
		this.framecounter = 0;
		this.position = 0;
		this.keyframeLst = new LinkedList<Keyframe>();
		this.threadId = threadId;
		this.threadFrames = threadFrames;
	}
	
	/**
	 * Function will be called up with each frame which is assigned to the
	 * specific thread
	 * 
	 * SimpleKeyframe Algorithm
	 */
	@Override
	public int process(Buffer in, Buffer out) {
		storeInfo(in.getTimeStamp(), in.getSequenceNumber());
		System.out.println("MotionBased wird immer aufgerufen!");
		framecounter++;
		if (getCurrentFrame() == framecounter
				&& framesToExtract.size() > position) {
			long tempCounter = getFrameNr(framecounter);
			// WARNING!!! Bug Fixing!!! Must be Counter - 2 to fix a bug in
			// processor. Process method will be called up two times before
			// first frame is processed!
			if ((tempCounter) == framesToExtract.get(position)) {
				
				VideoFormat format = (VideoFormat) in.getFormat();
				try {
					img = convertImageToBufferedImage(new BufferToImage(format)
							.createImage(in));
				} catch (NullPointerException e) {
					return BUFFER_PROCESSED_FAILED;
				}
				keyframeLst.add(new Keyframe(framesToExtract.get(position),
						convertImageToBufferedImage(img)));
				
				position++;
			}
		}
		passFrameThrough(in, out);
		return BUFFER_PROCESSED_OK;
	}

	/**
	 * To finish the algorithm, the list of extracted keyframes will be returned.
	 * 
	 * @return the list of keyframes
	 */
	public List<Keyframe> finalizeAlgorithm() {
		return keyframeLst;
	}

	/**
	 * The current framenumber of the respected core is transformed into the
	 * overall video framenumber
	 * 
	 * @param position
	 *            the current framenumber
	 * @return the transformed video framenumber
	 */
	private long getFrameNr(long position) {
		if (threadId != 0) {
			position = threadFrames[threadId - 1] + position;
		}
		return position;
	}

	/**
	 * Name of the Algorithm
	 */
	@Override
	public String getName() {
		return "MotionBasedImageExtraction";
	}

}
