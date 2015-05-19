package org.iviPro.scenedetection.kf_evaluation;

import java.io.IOException;
import java.util.Collections;
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
import javax.media.NotConfiguredError;
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
import org.iviPro.scenedetection.kf_algorithm.MotionBasedKeyframeAlgorithm;
import org.iviPro.scenedetection.kf_algorithm.SimpleKeyframeAlgo;
import org.iviPro.scenedetection.sd_main.AlgorithmSettings;
import org.iviPro.scenedetection.sd_main.Keyframe;
import org.iviPro.scenedetection.sd_main.Shot;

public class KfEvaluationProzessor implements ControllerListener {

	private final Object waitSync = new Object();

	private static boolean STATE_TRANSITION_OK = true;

	private MediaLocator ml;

	private TrackControl videoTrack;

	/* More algorithms can be attached to determine shots */
	private List<EvalAlgorithm> algorithms;

	private Long fileSize;

	private Processor proc;

	private int threadId;

	private float frameRate;

	private long[] threadFrameNr;

	private long[] cuts;

	private long startFrame;

	private long endFrame;

	private boolean finalized;

	private boolean finished;

	private List<Shot> shotList;

	private List<Keyframe> keyframeList;

	private int bugCounter;

	private boolean interrupt;

	/* Processor is active if at least one shot is available */
	boolean isActive;

	private long size;

	KfEvaluationProzessor(List<Shot> shotList) {
		this.shotList = shotList;
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
		this.size = size;
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

		return true;
	}

	public void runEvaluation() throws IllegalStateException {
		if (isRealized()) {
			open(ml, fileSize);
		}

		if (proc.getState() != Processor.Configured) {
			throw new IllegalStateException("Action not allowed "
					+ "in that state!");
		}

		addAlgorithms();
		realize();

		proc.setMediaTime(new Time(startFrame / frameRate));
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
	private void addAlgorithms() {

			algorithms = new LinkedList<EvalAlgorithm>();
			algorithms.add(new EvalAlgorithm(shotList));
			Codec codec[] = new Codec[algorithms.size()];

			for (int i = 0; i < algorithms.size(); i++) {
				codec[i] = algorithms.get(i);
			}

			try {
				videoTrack.setCodecChain(codec);
			} catch (UnsupportedPlugInException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotConfiguredError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

	void setInterrupt() {
		this.interrupt = true;
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