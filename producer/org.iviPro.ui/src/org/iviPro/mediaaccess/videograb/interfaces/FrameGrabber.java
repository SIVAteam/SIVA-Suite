package org.iviPro.mediaaccess.videograb.interfaces;

import org.iviPro.mediaaccess.videograb.FrameGrabingJob;

public interface FrameGrabber {

	// Methode holt f�r den Job das Bild und setzt das Bild im Job
	public void grabFrame(FrameGrabingJob job);
}
