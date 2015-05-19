package org.iviPro.scenedetection.kf_extractioncore;

import java.io.File;
import java.util.List;
import javax.media.MediaLocator;
import javax.swing.JFrame;

import org.iviPro.scenedetection.kf_algorithm.ViewPanel;
import org.iviPro.scenedetection.sd_main.Keyframe;
import org.iviPro.scenedetection.sd_main.Shot;

public class MotionBasedKeyframeSubWorker extends Thread implements Runnable {

	private MotionBasedKeyframeProcessor proc;
	
	private ImageExtractionProcessor imageProc;

	private List<Shot> shotList;
	
	private Long[] threadFrames;

	private int threadId;
	
	private String fileLink;

	public MotionBasedKeyframeSubWorker(int threadId, List<Shot> shotList,
			long[] threadFrames) {
		this.shotList = shotList;
		proc = new MotionBasedKeyframeProcessor(this, threadId, shotList,
				threadFrames);
		this.threadId = threadId;
		this.threadFrames = new Long[threadFrames.length];
		for (int i = 0; i < threadFrames.length; i++) {
			this.threadFrames[i] = threadFrames[i];
		}
	}

	boolean openVideo(String fileLink) {
		this.fileLink = fileLink;
		File file = new File(fileLink);
		MediaLocator mediaLocator = null;
		mediaLocator = new MediaLocator(fileLink);

		if (!proc.open(mediaLocator, new Long(file.length()))) {
			System.err.println("Could not open Mediafile!");
			return false;
		}
		return true;
	}

	void setInterrupt() {
		proc.setInterrupt();
	}

	void kill() {
		proc.exit();
		this.interrupt();
	}

	@Override
	public void run() {
		try {
			proc.runKeyFrameDetection();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		synchronized (this) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		try {
			long[] tf = new long[threadFrames.length];
			for (int i = 0; i < tf.length; i++) {
				tf[i] = threadFrames[i];
			}
			
			imageProc = new ImageExtractionProcessor(this, threadId, shotList, tf, proc.getFrames());
			File file = new File(fileLink);
			MediaLocator mediaLocator = null;
			mediaLocator = new MediaLocator(fileLink);
			imageProc.open(mediaLocator, new Long(file.length()));
			imageProc.runImageExtraction();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		synchronized (this) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
	}

	public List<Shot> setKeyFrames() {
		List<Keyframe> kfList = imageProc.getKeyframes();
		for (int i = 0; i < shotList.size(); i++) {
			Shot shot = shotList.get(i);
			for (int j = 0; j < kfList.size(); j++) {
				long nr = kfList.get(j).getFramenr();
				if (nr >= (shot.getStartFrame() + 2) && nr <= (shot.getEndFrame() - 2)) {
					shot.setKeyFrame(kfList.get(j));
					if(shot.getShotId() == 5) {
						JFrame pictureViewer = new JFrame();

						pictureViewer.setResizable(false);
						pictureViewer.add(new ViewPanel(kfList.get(j).getImage(), ""));
						pictureViewer.pack();
						pictureViewer.setVisible(true);
						System.out.println("Dazugehörige Framenummer: "+kfList.get(j).getFramenr());
						
					}
					System.out.println("Shot keyframesetting: "+shot.getShotId());
				}
			}
		}
		return shotList;
	}

	void close() {
		proc.close();
	}
}
