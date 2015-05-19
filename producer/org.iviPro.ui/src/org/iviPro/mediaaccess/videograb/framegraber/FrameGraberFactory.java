package org.iviPro.mediaaccess.videograb.framegraber;

import org.iviPro.mediaaccess.videograb.interfaces.I_FrameGrabber;

public class FrameGraberFactory {
	
	private static enum FacType {VLC1, VLC2, FFMPEG};
	
	private static FacType type = FacType.FFMPEG;

	public static I_FrameGrabber getFrameGrabber() {			
		switch (type) {
			case FFMPEG: return new FFMpegVideoGrabber();
		}
		return new FFMpegVideoGrabber();
	}
}
