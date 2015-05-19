package org.iviPro.scenedetection.shd_core;

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

import org.iviPro.scenedetection.shd_algorithm.Cut;
import org.iviPro.scenedetection.shd_algorithm.ShotDetectionAlgorithm;
import org.iviPro.scenedetection.shd_algorithm.ShotDetectionFuzzyLogic;

/**
 * ShotDetection processor. Every thread creates an instance of this class to
 * determine its shots.
 * 
 */
public class ShotDetectionProcessor implements ControllerListener {

	public static final int AMOUNT_ADDITIONAL_DATA = 50;

	private final Object waitSync = new Object();
	
	private FuzzyLogicSubWorker sub;

	private boolean STATE_TRANSITION_OK = true;

	private boolean busy;

	private Processor proc;

	private MediaLocator ml;

	private TrackControl videoTrack;

	private Long fileSize;

	private List<ShotDetectionAlgorithm> algorithms;

	private List<Cut> resultSet;

	private boolean finalized;

	private long frameNr;

	private float frameRate;

	private int threadId;

	private int amountThreads;

	private long[] threadFrameNr;

	private long startFrame;

	private long endFrame;

	ShotDetectionProcessor(FuzzyLogicSubWorker sub, int threadId, int amountThreads) {
		this.finalized = false;
		resultSet = new LinkedList<Cut>();
		algorithms = new LinkedList<ShotDetectionAlgorithm>();
		this.threadId = threadId;
		this.amountThreads = amountThreads;
		this.threadFrameNr = new long[amountThreads];
		this.sub = sub;
	}

	/**
	 * Gibt an, ob der Prozessor realisiert und somit bereit zum abspielen ist. <br />
	 * Visuelle Komponenten k�nnen nur von einem realisierten Prozessor
	 * angefordert werden.
	 * 
	 * @return true wenn Prozessor abspielbereit
	 */
	public boolean isRealized() {
		return proc != null ? proc.getState() == Processor.Realized
				|| proc.getState() == Processor.Prefetched
				|| proc.getState() == Processor.Prefetching
				|| proc.getState() == Processor.Started : false;
	}

	/**
	 * Gibt an ob der Prozessor noch besch�ftigt oder bereits fertig ist
	 * 
	 * @return true wenn besch�ftigt, false wenn fertig
	 */
	public boolean isBusy() {
		return busy;
	}

	/**
	 * �ffne eine Mediendatei und lade sie in den Prozessor
	 * 
	 * @param MediaLocator
	 *            Medienlokator
	 * @param Long
	 *            Dateigr��e in Bytes, kann auch null sein
	 * @return true, wenn Video ge�ffnet werden konnte, false sonst
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
			System.out.println("JAJAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA: "+m.toExternalForm());
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

		long frames = (long) (proc.getDuration().getSeconds() * vFormat
				.getFrameRate());
		long start = threadId * ((long) (Math.floor(frames / amountThreads)));
		long end = 0;
		if (threadId == amountThreads - 1) {
			end = frames;
		} else {
			end = (threadId + 1)
					* ((long) (Math.floor(frames / amountThreads)));
		}

		for (int i = 0; i < amountThreads; i++) {
			threadFrameNr[i] = i
					* ((long) (Math.floor(frames / amountThreads)));
		}
		if (threadId == 0) {
			this.startFrame = start;
		} else {
			this.startFrame = start - AMOUNT_ADDITIONAL_DATA;
		}
		this.endFrame = end;

		return true;
	}

	/**
	 * Startet die Wiedergabe bzw. Verarbeitung<br />
	 * 
	 * Sorgt daf�r dass sich der Processor vorher im richtigen Status
	 * befindet.
	 * 
	 * @return true wenn Wiedergabe gestartet, false sonst
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

			// Warte bis Prozessor gestartet
			if (!waitForState(Processor.Started)) {
				System.err.println("Couldn't start processor..");
			}
			return true;
		} else if (proc.getState() == Processor.Realized) {

			// Daten cachen
			proc.prefetch();

			// Warte bis Daten gecached sind
			if (!waitForState(Processor.Prefetched)) {
				System.err.println("Couldn't cache data.");
			}

			// neuer Versuch
			return start();
		} else if (proc.getState() == Processor.Configured) {

			// Processor realisieren
			realize();

			// neuer Versuch
			return start();
		} else {
			return false;
		}
	}

	/**
	 * Verarbeitet das gesamte Video und generiert aus den Informationen ein
	 * MPEG7-XML Dokument, falls diese Option gew�hlt wurde.<br />
	 * <br />
	 * 
	 * Falls start == null wird angenommen dass von Anfang an verarbeitet wird. <br />
	 * Falls ende == null wird angenommen, dass bis zum Ende des Mediums
	 * verarbeitet wird.
	 * 
	 * @param start
	 *            Startmedienzeit der Verarbeitung
	 * @param ende
	 *            Endmedienzeit der Verarbeitung
	 */
	public void runDetection() throws IllegalStateException {
		if (isRealized()) {
			open(ml, fileSize);
		}

		if (proc.getState() != Processor.Configured) {
			throw new IllegalStateException("Action not allowed "
					+ "in that state!");
		}

		addAlgorithms();
		realize();

		proc.setMediaTime(new Time(new Time(startFrame / frameRate)
				.getNanoseconds()));
		proc.setStopTime(new Time(new Time(endFrame / frameRate)
				.getNanoseconds()));

		start();
	}

	/**
	 * F�gt die gew�hlten Algorithmen der Verarbeitungskette hinzu
	 */
	private void addAlgorithms() {

		try {
			algorithms = new LinkedList<ShotDetectionAlgorithm>();
			algorithms.add(new ShotDetectionFuzzyLogic(threadId, amountThreads,
					threadFrameNr));
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
	 * Initialisiert z.B. das Anzeigefenster, die Framenavigation, ...
	 */
	private void realize() {
		// Prozessor realisieren
		proc.realize();
		// Warte bis Prozessor realisiert ist
		if (!waitForState(Processor.Realized)) {
			System.out.println("Prozessor kann nicht realisiert werden");
			System.err.println("Couldn't implement processor!");
		}
	}

	/**
	 * Blockiert bis der Processor im �bergebenen Zustand ist
	 * 
	 * @param state
	 *            ben�tigter Status
	 * @return true, wenn Status erreicht
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
	 * Controller Listener, reagiert auf verschiedene Events des Processors
	 * 
	 * @param evt
	 *            Event
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

			// Prozessor ist besch�ftigt
			busy = true;

		} else if (evt instanceof StopEvent) {
			// Wenn Videoende erreicht gehe zum Anfang
			if (evt instanceof StopAtTimeEvent
					|| evt instanceof EndOfMediaEvent) {
				proc.setMediaTime(new Time(0));
			}

			for (int i = 0; i < algorithms.size(); i++) {
				if (algorithms.get(i) instanceof ShotDetectionFuzzyLogic) {
					if (!finalized) {
						ShotDetectionFuzzyLogic algo = (ShotDetectionFuzzyLogic) algorithms
								.get(i);
						List<Cut> cutsFromAlgo = algo.finalizeAlgorithm();
						resultSet = new LinkedList<Cut>();
						for (int j = 0; j < cutsFromAlgo.size(); j++) {
							resultSet.add(cutsFromAlgo.get(j));
						}
						cutsFromAlgo = null;
						finalized = true;
					}
					break;
				}
			}
			proc.close();
			synchronized (sub) {
				sub.notifyAll();
			}
		}
	}

	public List<Cut> getCuts() {
		return resultSet;
	}

	public long getDuration() {
		return frameNr;
	}
	
	public void exit() {
		proc.stop();
	}
	
	public void close() {
		for (int i = 0; i < resultSet.size(); i++) {
			resultSet.remove(i);
		}
	}
}
