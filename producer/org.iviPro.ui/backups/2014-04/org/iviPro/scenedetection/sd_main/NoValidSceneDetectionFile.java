package org.iviPro.scenedetection.sd_main;

public class NoValidSceneDetectionFile extends Exception {

	private static final long serialVersionUID = 1L;

	private int errorCode;

	public NoValidSceneDetectionFile(int errorCode) {
		super();
		this.errorCode = errorCode;
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		String error = "Unknown error code.";
		if (errorCode == 0) {
			error = "Unknowned file error detected. Scene Detection cannot be executed.";
		} else if (errorCode == 1) {
			error = "The videofile has no 16x16 macroblocks. No Scene Detection cannot be executed.";
		} else if (errorCode == 2) {
			error = "No video decoder has been found. No Scene Detection cannot be executed.";
		} else if (errorCode == 3) {
			error = "Framerate is not in specified range (20fps < framerate < 35). No Scene Detection cannot be executed.";
		}
		return error;
	}
}
