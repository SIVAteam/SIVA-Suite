package org.iviPro.scenedetection.kf_evaluation;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.media.Buffer;
import javax.media.Effect;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import org.iviPro.scenedetection.sd_main.Keyframe;
import org.iviPro.scenedetection.sd_main.MiscOperations;
import org.iviPro.scenedetection.sd_main.SDTime;
import org.iviPro.scenedetection.sd_main.Shot;

public class EvalAlgorithm implements Effect {

	private List<Shot> shotList;

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

	private long framecounter;

	private int currentShot;

	private ArrayList<Float[]> keyframeLowest;
	
	private LinkedList<Float> shotValues;
	
	private LinkedList<Float> keyframesRatioTogether;
	
	private int keyframeAmount;
	
	private boolean resultShow;

	public EvalAlgorithm(List<Shot> shotList) {
		framecounter = 0;
		this.shotList = shotList;
		inputFormats = new Format[] { new RGBFormat() };
		outputFormats = new Format[] { new VideoFormat(null) };
		this.currentShot = 0;
		lastAlgorithmDone = true;
		lastAlgorithm = false;
		keyframeLowest = new ArrayList<Float[]>();
		shotValues = new LinkedList<Float>();
		keyframesRatioTogether = new LinkedList<Float>();
		resultShow = false;
		keyframeAmount = 0;
	}

	@Override
	public int process(Buffer in, Buffer out) {
		storeInfo(in.getTimeStamp(), in.getSequenceNumber());
		framecounter++;
		VideoFormat format = (VideoFormat) in.getFormat();
		if (framecounter == shotList.get(currentShot).getEndFrame()) {
			setNewShot();
		} else if (framecounter >= shotList.get(currentShot).getStartFrame()
				&& framecounter < shotList.get(currentShot).getEndFrame()) {
			List<Keyframe> kfList = shotList.get(currentShot).getKeyFrameLst();
			Float[] tempMinVals = new Float[kfList.size()];
			for (int i = 0; i < kfList.size(); i++) {
				Keyframe kf = kfList.get(i);
				tempMinVals[i] = calcHistogramDiff(
						convertImageToBufferedImage(new BufferToImage(format)
								.createImage(in)),
						kf.getImage());
			}
			keyframeLowest.add(tempMinVals);
		} else {
			if(!resultShow) {
				float valsTogether = 0;
				for (int i = 0; i < shotValues.size(); i++) {
					valsTogether += shotValues.get(i);
				}
				float result = valsTogether / shotValues.size();
				System.out.println("ShotFidelity: "+result);
				//Varianz berechnen
				float var = 0;
				for (int i = 0; i < shotValues.size(); i++) {
					var += Math.pow((shotValues.get(i) - result), 2);
				}
				var /= shotValues.size();
				double standardVariation = Math.sqrt(var);
				System.out.println("Standardabweichung: "+standardVariation);
				resultShow = true;
				//Compression Ratio overall
				float ratioTogether = 0;
				for (int i = 0; i < keyframesRatioTogether.size(); i++) {
					ratioTogether += keyframesRatioTogether.get(i);
				}
				System.out.println("CompressionRatioInsgesamt: "+ratioTogether / keyframesRatioTogether.size());
				System.out.println("KeyframesInsgesamt:" +keyframeAmount);
				System.out.println("Framecounter: "+framecounter);
			}
		}

		passFrameThrough(in, out);
		return BUFFER_PROCESSED_OK;
	}

	private void setNewShot() {
		setFidelityVal();
		if (currentShot + 1 <= shotList.size() - 1) {
			currentShot++;
			keyframeLowest.clear();
		}
	}

	private void setFidelityVal() {
		ArrayList<Float> minVals = new ArrayList<Float>();
		for (int i = 0; i < keyframeLowest.size(); i++) {
			Float[] current = keyframeLowest.get(i);
			float maxVal = 0;
			for (int j = 0; j < current.length; j++) {
				if(maxVal < current[j]) {
					maxVal = current[j];
				}
			}
			minVals.add(maxVal);
		}
		float[] arr = new float[minVals.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = minVals.get(i);
		}
		Arrays.sort(arr);
		if(arr.length == 0) {
			// Defekter Shot
			System.out.println("Der Shot is defekt");
		} else if(arr.length < 3) {
			shotValues.add(arr[0]);
			System.out.println("Fidelity of Shot: " + currentShot + " : " + arr[2]);
			System.out.println("AnfangsframeNr: "+shotList.get(currentShot).getStartFrame()+"EndframeNr: "+shotList.get(currentShot).getEndFrame());
		} else {
			shotValues.add(arr[2]);
			System.out.println("Fidelity of Shot: " + currentShot + " : " + arr[2]);
			System.out.println("AnfangsframeNr: "+shotList.get(currentShot).getStartFrame()+"EndframeNr: "+shotList.get(currentShot).getEndFrame());
			//Compression Ratio
			Shot shot = shotList.get(currentShot);
			long frames = shot.getEndFrame() - shot.getStartFrame();
			float ratio = 1 - ((shot.getKeyFrameLst().size()) /((float) frames));
			System.out.println("CompressionRatio: "+ratio);
			keyframesRatioTogether.add(ratio);
			keyframeAmount += shotList.get(currentShot).getKeyFrameLst().size();
		}
	}

	private float calcHistogramDiff(BufferedImage img0, BufferedImage img1) {
//		return (colorIntersection(MiscOperations.createHSVHistogram(img0),
//				MiscOperations.createHSVHistogram(img1)) / 3);
		return 0;
	}

	private float colorIntersection(float[][] histogram1, float[][] histogram2) {
		float sum = 0;
		for (int i = 0; i < histogram1.length; i++) {
			for (int j = 0; j < histogram1[i].length; j++) {
				sum += Math.min(histogram1[i][j], histogram2[i][j]);
			}
		}
		return sum;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public Format[] getSupportedInputFormats() {
		return inputFormats;
	}

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

	@Override
	public Format setInputFormat(Format input) {
		inputFormat = input;
		return inputFormat;
	}

	@Override
	public Format setOutputFormat(Format output) {
		outputFormat = output;
		return outputFormat;
	}

	@Override
	public void close() {
		reset();
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public void open() throws ResourceUnavailableException {
	}

	@Override
	public void reset() {
	}

	@Override
	public Object getControl(String arg0) {
		return null;
	}

	@Override
	public Object[] getControls() {
		return null;
	}

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

	public void storeInfo(long time, long frameNr) {
		date = new Date();
		currentMediaTime = new SDTime(time);
		currentFrame = frameNr + 1;
	}

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

}
