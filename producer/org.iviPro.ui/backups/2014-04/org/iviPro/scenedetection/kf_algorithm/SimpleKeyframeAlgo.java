package org.iviPro.scenedetection.kf_algorithm;

import java.util.LinkedList;
import java.util.List;
import javax.media.Buffer;
import org.iviPro.scenedetection.sd_main.ProgressDetermination;

// Vorsicht bei dieser Klasse! Ist nur zu Testzwecken vorhanden. Ein Bug
// im Processor bei der Frameaddressierung sorgt dafür dass im
// MotionBasedImageExtraction im Parallelmodus und Sequenziellen
// Modus teils Framedifferenzen +-1 auftauchen.  
// Exakte Erkennung im sequenziellen Modus.
// Paralleler Modus kann bei manchen Frames eine Abweichung von -1 haben.
// Sollte allerdings praktisch keine Auswirkungen auf das Ergebnis haben!
public class SimpleKeyframeAlgo extends KeyframeExtractionAlgorithm {

	private long framecounter;

	private long[] cuts;

	private long lastFrames;

	private List<Long> keyframeList;

	private int cutPosition;

	private long[] threadFrames;

	private boolean firstFrame;

	private int threadId;

	public SimpleKeyframeAlgo(int threadId, long[] threadFrames, long[] cuts) {
		super();
		this.cutPosition = 0;
		this.cuts = cuts;
		if (threadId == 0) {
			framecounter = 0;
		} else {
			framecounter = threadFrames[threadId - 1];
		}
		this.firstFrame = true;
		this.threadFrames = threadFrames;
		this.threadId = threadId;
		keyframeList = new LinkedList<Long>();
		dummdi();
		CLASS_NAME = "SimpleKeyframeAlgo";
	}

	@Override
	public int process(Buffer in, Buffer out) {
		storeInfo(in.getTimeStamp(), in.getSequenceNumber());
		framecounter++;
//
//		// FirstFrame vorne
//		if (firstFrame) {
//			keyframeList.add(framecounter + 1);
//			firstFrame = false;
//		} else if (framecounter == threadFrames[threadId]) {
//			// System.out.println("Ich pack den letzetn Keyframe noch drauf mann!"+framecounter);
//			// Pack den letzten Keyframe dazu
//			if (threadId != 0) {
//				keyframeList.add(framecounter - 2);
//			} else {
//				keyframeList.add(framecounter - 2);
//			}
//		} else {
//			long framePosition = 0;
//			if (threadId == 0) {
//				framePosition = getCurrentFrame();
//			} else {
//				framePosition = getCurrentFrame() + threadFrames[threadId - 1];
//			}
//			// System.out.println("Framecounter: "+framecounter+"CurrentPos: "+framePosition
//			// + "threadId: "+threadId);
//			if (framePosition == framecounter && cutPosition < cuts.length) {
////				System.out.println("Framecounter: " + framecounter
////						+ "CurrentPos: " + framePosition + "threadId: "
////						+ threadId);
//				if (framecounter == (cuts[cutPosition] - 1)) {
//					keyframeList.add(framecounter - 1);
//				} else if (framecounter == (cuts[cutPosition])) {
//					keyframeList.add(framecounter + 1);
//					cutPosition++;
//					// Last Frame of Video
//				} else if (framecounter == threadFrames[threadFrames.length - 1]) {
//					keyframeList.add(framecounter - 1);
//				}
//			}
//		}

		if (framecounter % 10 == 0) {
			ProgressDetermination
					.setProcessedFrames((int) (framecounter - lastFrames));
			lastFrames = framecounter;
		}

		passFrameThrough(in, out);
		return BUFFER_PROCESSED_OK;
	}

	private void dummdi() {
		long currentFrame = 1;
		if(threadId != 0) {
			currentFrame = threadFrames[threadId - 1] + 1;
		}
		keyframeList.add(currentFrame + 2);
		System.out.println("AddKeyframeStart: "+currentFrame);
		for (int i = 0; i < cuts.length; i++) {
			keyframeList.add(cuts[i] + 2);
			System.out.println("AddKeyframe: "+(cuts[i] - 1));
			System.out.println("AddKeyframe: "+cuts[i]);
		}
		keyframeList.add(threadFrames[threadId] - 2);
		System.out.println("AddKeyframeEnd: "+(threadFrames[threadId]));
	}
	
	@Override
	public String getName() {
		return CLASS_NAME;
	}

	public List<Long> finalizeAlgorithm() {
		return keyframeList;
	}
}
