package org.iviPro.scenedetection.kf_extractioncore;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.media.Codec;
import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoProcessorException;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.StartEvent;
import javax.media.StopAtTimeEvent;
import javax.media.StopEvent;
import javax.media.Time;
import javax.media.UnsupportedPlugInException;
import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;
import org.iviPro.scenedetection.kf_algorithm.KeyframeExtractionAlgorithm;
import org.iviPro.scenedetection.kf_algorithm.MotionBasedImageExtraction;
import org.iviPro.scenedetection.sd_main.Keyframe;
import org.iviPro.scenedetection.sd_main.Shot;

/**
 * Keyframeextraction processor. Every thread creates an instance of this class
 * to determine its keyframes.
 */
public class ImageExtractionProcessor implements ControllerListener {

	private final Object waitSync = new Object();

	private static boolean STATE_TRANSITION_OK = true;

	private MotionBasedKeyframeSubWorker sub;

	private MediaLocator ml;

	private TrackControl videoTrack;

	/* More algorithms can be attached to determine shots */
	private List<KeyframeExtractionAlgorithm> algorithms;

	private Long fileSize;

	private Processor proc;

	private int threadId;

	private float frameRate;

	private long[] threadFrameNr;

	private long[] cuts;

	private long startFrame;

	private long endFrame;

	private boolean finalized;


	private List<Shot> shotList;

	private List<Keyframe> keyframeList;

	/* Processor is active if at least one shot is available */
	boolean isActive;
	
	private List<Long> frames;

	
	/**
	 * Constructor
	 * 
	 * @param threadId
	 *            current threadId. If amount of cores is one, threadid is 0 by
	 *            default
	 * @param shotList
	 *            shotlist of each thread should be iterated
	 * @param threadFrames
	 *            the amount of frames in video after current thread starts
	 */
	ImageExtractionProcessor(MotionBasedKeyframeSubWorker sub,
			int threadId, List<Shot> shotList, long[] threadFrames, List<Long> frames) {
		this.sub = sub;
		this.finalized = false;
		this.threadId = threadId;
		this.threadFrameNr = threadFrames;
		this.keyframeList = new LinkedList<Keyframe>();
		this.shotList = shotList;
		this.frames = frames;

		/*
		 * First and last cut are video start / endpoint. The first frame of a
		 * shot represents the cut.
		 */
		int cutSize = shotList.size() - 1;
		cuts = new long[cutSize];
		for (int i = 1; i < shotList.size(); i++) {
			System.out.println("Cutgrï¿½ï¿½e: " + cutSize
					+ "Shotlistgrï¿½ï¿½e: " + shotList.size());
			cuts[i - 1] = shotList.get(i).getStartFrame();
		}
	}

	/**
	 * Checks whether the processor has been realized and is ready to play.
	 * Visual components can be requested only if the processor is realized.
	 * 
	 * @return true if processor is ready
	 */
	public boolean isRealized() {
		return proc != null ? proc.getState() == Processor.Realized
				|| proc.getState() == Processor.Prefetched
				|| proc.getState() == Processor.Prefetching
				|| proc.getState() == Processor.Started : false;
	}

	/**
	 * Opens a new media file to be processed
	 * 
	 * @param MediaLocator
	 *            medialocator
	 * @param Long
	 *            filesize in byte, can be null
	 * @return true, if processor was able to open file, else false
	 */
	public boolean open(MediaLocator m, Long size) {
		ml = m;
		algorithms = null;
		if (proc != null) {
			proc.stop();
			proc.deallocate();
			proc = null;
		}

		if (size != null) {
			fileSize = size;
		}

		try {
			proc = Manager.createProcessor(m);
		} catch (IOException e) {
			System.err.println("Couldn't read from file!");
			return false;
		} catch (NoProcessorException e) {
			e.printStackTrace();
			System.err.println("Couldn't find a processor for the media ");
			return false;
		}

		proc.addControllerListener(this);
		proc.configure();

		if (!waitForState(Processor.Configured)) {
			System.err.println("Couldn't set up processor.");
			return false;
		}

		TrackControl tc[] = proc.getTrackControls();
		if (tc == null) {
			System.err.println("Failed to get TrackControl!");
			return false;
		}

		for (int i = 0; i < tc.length; i++) {
			if (tc[i].getFormat() instanceof VideoFormat) {
				videoTrack = tc[i];
			} else {
				tc[i].setEnabled(false);
			}
		}

		if (videoTrack == null) {
			System.err.println("File contains no videostream.");
			return false;
		}

		VideoFormat vFormat = (VideoFormat) videoTrack.getFormat();
		proc.setContentDescriptor(null);

		this.frameRate = vFormat.getFrameRate();

		this.startFrame = shotList.get(0).getStartFrame() - 1;
		this.endFrame = shotList.get(shotList.size() - 1).getEndFrame();
		if (threadId == 0) {
			System.out
					.println("TESTINNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNGGGG: "
							+ this.startFrame + "EndFrame: " + this.endFrame);
		}
		return true;
	}

	/**
	 * Starts the keyframe extraction. Sets the correct mediatime of each thread
	 * and starts the procedure
	 * 
	 * @throws IllegalStateException
	 *             if processor is not in configured state
	 */
	public void runImageExtraction() throws IllegalStateException {
		if (isRealized()) {
			open(ml, fileSize);
		}

		if (proc.getState() != Processor.Configured) {
			throw new IllegalStateException("Action not allowed "
					+ "in that state!");
		}

		// Startprocedure of keyframe extraction
		addAlgorithms(true, frames);
		realize();

		if (threadId != 0) {
			proc.setMediaTime(new Time((startFrame) / frameRate));
		} else {
			proc.setMediaTime(new Time(startFrame / frameRate));
		}
		proc.setStopTime(new Time(((endFrame) / frameRate)));

		start();
	}

	/**
	 * Starts the calculations
	 * 
	 * Checks whether the processor has the correct state to start video
	 * processing
	 * 
	 * @return true if rendering has started, false else
	 */
	public boolean start() {
		if (proc == null) {
			return false;
		}

		if (proc.getState() == Processor.Prefetched) {
			if (algorithms == null) {
				proc.setStopTime(proc.getDuration());
			}
			proc.start();

			if (!waitForState(Processor.Started)) {
				System.err.println("Couldn't start processor..");
			}
			return true;
		} else if (proc.getState() == Processor.Realized) {
			proc.prefetch();

			if (!waitForState(Processor.Prefetched)) {
				System.err.println("Couldn't cache data.");
			}
			return start();
		} else if (proc.getState() == Processor.Configured) {
			realize();
			return start();
		} else {
			return false;
		}
	}

	/**
	 * Adds the chosen algorithm to the working chain
	 * 
	 * @param start
	 *            adds motion based keyframe extraction algorithm (True). Adds
	 *            image extraction algorithm if false.
	 * @param frames
	 *            the framenumbers of keyframes where images should be extracted
	 */
	private void addAlgorithms(boolean start, List<Long> frames) {

		try {
			algorithms = new LinkedList<KeyframeExtractionAlgorithm>();
				algorithms.add(new MotionBasedImageExtraction(threadId,
						threadFrameNr, frames));
			Codec codec[] = new Codec[algorithms.size()];

			for (int i = 0; i < algorithms.size(); i++) {
				codec[i] = algorithms.get(i);
			}

			videoTrack.setCodecChain(codec);

		} catch (UnsupportedPlugInException e) {
			System.err.println("This processor has not support"
					+ "for algorithms");
		}
	}

	/**
	 * Initialises the processor
	 */
	private void realize() {
		// Prozessor realisieren
		proc.realize();
		// Warte bis Prozessor realisiert ist
		if (!waitForState(Processor.Realized)) {
			System.err.println("Couldn't implement processor!");
		}
	}

	/**
	 * Eventhandler to control video processing and endoperations
	 */
	@Override
	public void controllerUpdate(ControllerEvent evt) {
		if (evt instanceof ConfigureCompleteEvent
				|| evt instanceof RealizeCompleteEvent
				|| evt instanceof PrefetchCompleteEvent) {
			synchronized (waitSync) {
				STATE_TRANSITION_OK = true;
				waitSync.notifyAll();
			}
		} else if (evt instanceof ResourceUnavailableEvent) {
			synchronized (waitSync) {
				STATE_TRANSITION_OK = false;
				waitSync.notifyAll();
			}
		} else if (evt instanceof StartEvent) {
			synchronized (waitSync) {
				STATE_TRANSITION_OK = true;
				waitSync.notifyAll();
			}

		} else if (evt instanceof StopEvent) {
			// Videoend: Go to startposition
			if (evt instanceof StopAtTimeEvent
					|| evt instanceof EndOfMediaEvent) {
				proc.setMediaTime(new Time(0));
			}
    		for (int i = 0; i < algorithms.size(); i++) {
    			if (algorithms.get(i) instanceof MotionBasedImageExtraction) {
    				if (!finalized) {
    					MotionBasedImageExtraction algo = (MotionBasedImageExtraction) algorithms.get(i);
    					keyframeList = algo.finalizeAlgorithm();
    					finalized = true;
    					break;
    				}
				}
			}
    		System.out.println("Der eine Prozessor wird schonmal geclosed");
			proc.close();
			synchronized (sub) {
				sub.notifyAll();
			}
		}
	}

	/**
	 * Blocks until the processor is in an optimal state
	 * 
	 * @param status
	 *            needed status
	 * @return true, if status is active
	 */
	private boolean waitForState(int state) {
		synchronized (waitSync) {
			try {
				while (proc.getState() != state && STATE_TRANSITION_OK) {
					waitSync.wait();
				}
			} catch (Exception e) {
				System.err.println(e.getLocalizedMessage());
			}
		}
		return STATE_TRANSITION_OK;
	}

	/**
	 * Gets the extracted keyframe objects
	 * 
	 * @return list of keyframes
	 */
	public List<Keyframe> getKeyframes() {
		System.out.println("Die KeyframeListe von Thread: "+threadId+" hat die Größe: "+keyframeList.size());
		for (int i = 0; i < keyframeList.size(); i++) {
			System.out.println("Keyframe von Frame: "+keyframeList.get(i).getFramenr());
		}
		return keyframeList;
	}

	void setInterrupt() {
	}

	void exit() {
		proc.stop();
	}

	public void close() {
		for (int i = 0; i < shotList.size(); i++) {
			shotList.remove(i);
		}
		for (int i = 0; i < keyframeList.size(); i++) {
			keyframeList.remove(i);
		}
	}
}
