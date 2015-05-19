package org.iviPro.scenedetection.kf_algorithm;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Date;
import javax.media.Buffer;
import javax.media.Effect;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

import org.iviPro.scenedetection.sd_main.SDTime;

public abstract class KeyframeExtractionAlgorithm implements Effect {

	protected String CLASS_NAME;

	protected Format inputFormat;

	protected Format[] inputFormats;

	protected Format outputFormat;

	protected Format[] outputFormats;

	protected Image img;

	protected static boolean lastAlgorithmDone;

	protected int everyXFrames = 1;

	protected SDTime currentMediaTime;

	protected long currentFrame = 1;

	protected long workingTime;

	protected Date date;

	protected boolean lastAlgorithm;

	public KeyframeExtractionAlgorithm() {

		inputFormats = new Format[] { new RGBFormat() };
		outputFormats = new Format[] { new VideoFormat(null) };

		lastAlgorithmDone = true;
		lastAlgorithm = false;
	}

	/**
	 * Returns current mediatime
	 * 
	 * @return time object
	 */
	public SDTime getCurrentMediaTime() {
		return currentMediaTime;
	}

	/**
	 * Return current framenumber of current thread
	 * 
	 * @return current framenumber
	 */
	public Long getCurrentFrame() {
		return new Long(currentFrame);
	}

	/**
	 * Sets important flags. Avoids codedublication
	 * 
	 * Ends timemeasuring
	 * 
	 * @param in
	 *            inputbuffer
	 * @param out
	 *            outputbuffer
	 */
	protected void passFrameThrough(Buffer in, Buffer out) {

		in.setFlags(Buffer.FLAG_NO_DROP);
		in.setFlags(Buffer.FLAG_NO_SYNC);

		out.copy(in);

		out.setFlags(Buffer.FLAG_NO_DROP);
		out.setFlags(Buffer.FLAG_NO_SYNC);

		if (lastAlgorithm) {
			lastAlgorithmDone = true;
		}

		Date end = new Date();
		workingTime += end.getTime() - date.getTime();
	}

	/**
	 * Saved current time and framenumber Starts timemeasuring for
	 * timeinformation
	 * 
	 * @param time
	 *            Time
	 * @param frameNr
	 *            frameNumber
	 */
	protected void storeInfo(long time, long frameNr) {
		date = new Date();
		currentMediaTime = new SDTime(time);
		currentFrame = frameNr + 1;
	}

	@Override
	public void close() {
		reset();
	}

	@Override
	public Object getControl(String arg0) {
		return null;
	}

	@Override
	public Object[] getControls() {
		return null;
	}

	/**
	 * Returns all supported formats
	 * 
	 * @return
	 */
	@Override
	public Format[] getSupportedInputFormats() {
		return inputFormats;
	}

	/**
	 * Returns all supported outputformats of an inputformat
	 * 
	 * @param input
	 *            inputformat
	 * @return outputformats
	 */
	@Override
	public Format[] getSupportedOutputFormats(Format input) {
		if (inputFormat == null)
			return outputFormats;
		else {
			Format outs[] = new Format[1];
			outs[0] = input;
			return outs;
		}
	}

	/**
	 * 
	 * @throws javax.media.ResourceUnavailableException
	 */
	@Override
	public void open() throws ResourceUnavailableException {
	}

	@Override
	public void reset() {
	}

	/**
	 * Set inputformat
	 * 
	 * @param input
	 *            inputformat
	 * @return inputformat
	 */
	@Override
	public Format setInputFormat(Format input) {
		inputFormat = input;
		return inputFormat;
	}

	/**
	 * Set outputformat
	 * 
	 * @param output
	 *            outputformat
	 * @return outputformat
	 */
	@Override
	public Format setOutputFormat(Format output) {
		outputFormat = output;
		return outputFormat;
	}

	/**
	 * Transforms a Java-Image into a BufferedImage object
	 * 
	 * @param image
	 *            Image
	 * @return BufferedImage
	 */
	public static BufferedImage convertImageToBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();

		try {
			// Create a BufferedImage
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null),
					image.getHeight(null));
		} catch (HeadlessException e) {
			e.getLocalizedMessage();
		}

		if (bimage == null) {
			// Create a buffered standardimage
			int type = BufferedImage.TYPE_INT_RGB;
			bimage = new BufferedImage(image.getWidth(null),
					image.getHeight(null), type);
		}

		// Copy the image in buffered image
		Graphics g = bimage.createGraphics();

		// Draw image in buffer
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return bimage;
	}

	@Override
	public int process(Buffer arg0, Buffer arg1) {
		return 0;
	}
}
