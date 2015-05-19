package org.iviPro.scenedetection.sd_misc;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoProcessorException;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;

import org.iviPro.scenedetection.sd_main.NoValidSceneDetectionFile;

public class FileCheck implements ControllerListener {

	public static final int MACROBLOCKSIZE = 16;

	public static final double MINFRAMERATE = 20.0;

	public static final double MAXFRAMERATE = 35.0;

	private final Object waitSync = new Object();

	private static boolean STATE_TRANSITION_OK = true;

	private MediaLocator locator;

	private Processor proc;

	private TrackControl videoTrack;

	private Mpeg7Export mpeg7exporter;

	private boolean mpeg7;

	private Long fileSize;

	public FileCheck(MediaLocator locator, boolean mpeg7, Mpeg7Export export) {		
		this.locator = locator;
		File file = new File("SDtry.mp4");
		this.fileSize = file.length();
		this.mpeg7 = mpeg7;
		this.mpeg7exporter = export;
		System.out.println("LINK: "+file.getAbsolutePath());
	}

	/**
	 * Opens a videofile and checks whether it can be processed for scene
	 * detection.
	 */
	public void check() throws NoValidSceneDetectionFile {

		try {
			System.out.println("Localtor: "+locator.toExternalForm());
			proc = Manager.createProcessor(locator);
		} catch (IOException e) {
			throw new NoValidSceneDetectionFile(0);
		} catch (NoProcessorException e) {
			throw new NoValidSceneDetectionFile(2);
		}

		proc.addControllerListener(this);
		proc.configure();

		if (!waitForState(Processor.Configured)) {
			throw new NoValidSceneDetectionFile(0);
		}

		TrackControl tc[] = proc.getTrackControls();
		if (tc == null) {
			throw new NoValidSceneDetectionFile(0);
		}

		for (int i = 0; i < tc.length; i++) {
			if (tc[i].getFormat() instanceof VideoFormat) {
				videoTrack = tc[i];
			} else {
				tc[i].setEnabled(false);
			}
		}

		if (videoTrack == null) {
			throw new NoValidSceneDetectionFile(0);
		}
		
		
		VideoFormat vFormat = (VideoFormat) videoTrack.getFormat();
		Dimension dim = vFormat.getSize();
		if (dim.getHeight() % MACROBLOCKSIZE != 0
				|| dim.getWidth() % MACROBLOCKSIZE != 0) {
			throw new NoValidSceneDetectionFile(1);
		}

		double frameRate = vFormat.getFrameRate();
		if (frameRate > MAXFRAMERATE || frameRate < MINFRAMERATE) {
			throw new NoValidSceneDetectionFile(3);
		}

		if (mpeg7) {
			initializeMpeg7(vFormat.getEncoding());
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

	private void initializeMpeg7(String videoFormat) {
		String tmp = locator.toExternalForm();
		String fileEnding = tmp.substring(tmp.lastIndexOf(".") + 1);
		String fileName = tmp.substring(tmp.lastIndexOf("/") + 1,
				tmp.lastIndexOf("."));

		MediaInfo mi = new MediaInfo(videoFormat, "RGB", locator.toString());
		mi.setFileType(fileEnding);
		System.out.println("Fileending:" + fileEnding);
		if (fileSize != null) {
			mi.setFileSize(new BigInteger(fileSize.toString()));
		}
		mpeg7exporter.addGeneralMediaInfo(mi);

		mpeg7exporter
				.addMetadataCreationInfo(fileName, "Scene Detection v.1.0");
	}

	@Override
	public void controllerUpdate(ControllerEvent evt) {
		if (evt instanceof ConfigureCompleteEvent
				|| evt instanceof RealizeCompleteEvent
				|| evt instanceof PrefetchCompleteEvent) {
			synchronized (waitSync) {
				STATE_TRANSITION_OK = true;
				waitSync.notifyAll();
			}
		}
	}

}
