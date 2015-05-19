package org.iviPro.scenedetection.shd_algorithm;

import javax.media.Buffer;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

import org.iviPro.scenedetection.sd_main.AlgorithmSettings;
import org.iviPro.scenedetection.sd_main.ProgressDetermination;
import org.iviPro.scenedetection.sd_main.SDTime;

import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.util.LinkedList;
import java.util.List;

/**
 * Shot Detection Algorithm: A fuzzy logic approach for detection of video shot
 * boundaries
 */
public class ShotDetectionFuzzyLogic extends ShotDetectionAlgorithm {

	private BufferedImage image;

	private int width;

	private int height;

	private ThreeFeaturesInterface[] internAlgorithm;

	private float[] currentPeakValues;

	private boolean firstFrame;

	private int[][] currentFrameRGB;

	private LinkedList<Frame> frameBuffer;

	private EdgeDetectionAlgorithm edgeAlgorithm;

	private SDTime timeLastFrame;

	private int threadId;

	private int amountThreads;

	private long[] threadFrames;

	private LinkedList<Cut> cutList;

	private long frameCounter;

	private long lastFrames;

	/**
	 * Konstruktor
	 */
	public ShotDetectionFuzzyLogic(int threadId, int amountThreads,
			long[] threadFrames) {
		super();
		internAlgorithm = new ThreeFeaturesInterface[ShotDetectionSettings.AMOUNTDETECTIONALGORITHM];
		currentPeakValues = new float[ShotDetectionSettings.AMOUNTDETECTIONALGORITHM];
		firstFrame = true;
		frameBuffer = new LinkedList<Frame>();
		edgeAlgorithm = new CannyEdgeDetector();
		this.frameCounter = 0;
		this.threadId = threadId;
		this.amountThreads = amountThreads;
		this.threadFrames = threadFrames;
		this.cutList = new LinkedList<Cut>();
		CLASS_NAME = "ShotDetectionFuzzyLogic";
	}

	@Override
	public int process(Buffer in, Buffer out) {
		// Zeit und Frameinfo zwischenspeichern
		storeInfo(in.getTimeStamp(), in.getSequenceNumber());
		frameCounter++;
		if (getCurrentFrame() >= 0 && getCurrentFrame() == frameCounter) {
			VideoFormat format = (VideoFormat) in.getFormat();
			try {
				Image scaledVersion = new BufferToImage(format).createImage(in)
						.getScaledInstance(320, 240, Image.SCALE_DEFAULT);
				img = convertImageToBufferedImage(scaledVersion);
			} catch (NullPointerException e) {
				return BUFFER_PROCESSED_FAILED;
			}

			if (currentFrame % everyXFrames == 0) {
				image = convertImageToBufferedImage(img);
				width = image.getWidth();
				height = image.getHeight();
				getCurrentRGBValues();
				if (firstFrame) {
					initializeFirstFrame();
					BufferedImage[] currentImage = new BufferedImage[2];
					currentImage[0] = null;
					currentImage[1] = this.image;
					if (threadId == 0) {
						SDTime[] time = { null, getCurrentMediaTime() };
						time[1] = new SDTime(0);
						cutList.add(new Cut(currentImage, 0, 1, time, 0, 1));
					}
					firstFrame = false;
				} else {
					threeFeaturesExtraction();
				}
			}
		}
		timeLastFrame = getCurrentMediaTime();
		passFrameThrough(in, out);

		// Progressbar update infos
		if (frameCounter % 10 == 0) {
			ProgressDetermination
					.setProcessedFrames((int) (frameCounter - lastFrames));
			lastFrames = frameCounter;
		}

		return BUFFER_PROCESSED_OK;
	}

	/**
	 * Extrahiert drei Merkmale aus den Bildern und speichert die Ergebnisse in
	 * einem Buffer
	 */
	private void threeFeaturesExtraction() {
		for (int i = 0; i < internAlgorithm.length; i++) {
			internAlgorithm[i].setImage(image);
			internAlgorithm[i].setCurrentRGB(currentFrameRGB);
			currentPeakValues[i] = internAlgorithm[i].getPeakValue();
		}
		float[] peak = new float[ShotDetectionSettings.AMOUNTDETECTIONALGORITHM];
		for (int i = 0; i < peak.length; i++) {
			peak[i] = currentPeakValues[i];
		}
		SDTime[] time = { timeLastFrame, getCurrentMediaTime() };

		frameBuffer.add(new Frame(image, peak, frameCounter, time, true));

		if (frameBuffer.size() > ShotDetectionSettings.DETECTIONWINDOWSIZE) {
			frameBuffer.removeFirst();
		}
		startDetectionModes();
	}

	/**
	 * Je nachdem welche Optionen vom Benutzer aufgewählt wurden, werden die
	 * entsprechenden Algorithmen aufgerufen
	 */
	private void startDetectionModes() {
		setModes();
		if (threadId == 0 && frameBuffer.size() > 10) {
			abruptShotCutDetection(frameBuffer.size() - 1);
		} else if (threadId != 0 && frameBuffer.size() >= 50) {
			abruptShotCutDetection(frameBuffer.size() - 1);
		}
		if (frameBuffer.size() >= 50) {
			if (ShotDetectionSettings.isFadeWithStandardVariation()) {
				fadeDetectionWithStandardVariation();
			}
			if (ShotDetectionSettings.isEnableDissolve()) {
				dissolveDetection();
			}
		}
	}

	private void setModes() {
		if (frameBuffer.size() > 2) {
			float[] peakDif1 = new float[ShotDetectionSettings.AMOUNTDETECTIONALGORITHM];
			float[] peakDif2 = new float[ShotDetectionSettings.AMOUNTDETECTIONALGORITHM];
			for (int i = 0; i < ShotDetectionSettings.AMOUNTDETECTIONALGORITHM; i++) {
				peakDif1[i] = frameBuffer.get(frameBuffer.size() - 2)
						.getPeakValues()[i]
						- frameBuffer.get(frameBuffer.size() - 3)
								.getPeakValues()[i];
				peakDif2[i] = frameBuffer.get(frameBuffer.size() - 1)
						.getPeakValues()[i]
						- frameBuffer.get(frameBuffer.size() - 2)
								.getPeakValues()[i];
			}
			for (int i = 0; i < ShotDetectionSettings.AMOUNTDETECTIONALGORITHM; i++) {
				if (peakDif1[i] > 0 && peakDif2[i] < 0) {
					frameBuffer.get(frameBuffer.size() - 2)
							.removeGradualSection();
					break;
				}
			}
		}
	}

	/**
	 * Startmethode der Hard-Cut-Erkennung. Die Varianten "Smooth" und "Rough"
	 * Video wurden hier hart codiert und müssen nicht mehr verändert werden.
	 */
	private void abruptShotCutDetection(int last) {
		if ((!frameBuffer.get(last - 5).isGradualSection)
				&& frameBuffer.get(last - 5).getIsPossible()) {
			float[] featureRatios;
			if (!AlgorithmSettings.getClassInstance().isOneSided()) {
				featureRatios = new float[ShotDetectionSettings.AMOUNTDETECTIONALGORITHM];
				for (int i = 0; i < featureRatios.length; i++) {
					float temp = frameBuffer.get(last - 10).getPeakValues()[i]
							+ frameBuffer.get(last - 9).getPeakValues()[i]
							+ frameBuffer.get(last - 8).getPeakValues()[i]
							+ frameBuffer.get(last - 7).getPeakValues()[i]
							+ frameBuffer.get(last - 6).getPeakValues()[i]
							+ frameBuffer.get(last - 4).getPeakValues()[i]
							+ frameBuffer.get(last - 3).getPeakValues()[i]
							+ frameBuffer.get(last - 2).getPeakValues()[i]
							+ frameBuffer.get(last - 1).getPeakValues()[i]
							+ frameBuffer.get(last).getPeakValues()[i];

					if (temp != 0) {
						featureRatios[i] = (frameBuffer.get(last - 5)
								.getPeakValues()[i] / (temp / 1.6f));
					}
				}
			} else {
				featureRatios = ratioFeaturesOneSided();
			}
			// Es wird noch geprueft ob auf den zu untersuchenden Frame ein
			// Hardcut mgl ist. Koennte unter Umstaende schon als HardCut im
			// OneSidedFeature erkannt worden sein!

			if (!frameBuffer.get(last - 5).isGradualSection) {

				float[][] slaFuzzySets = fuzzifyFeatures(featureRatios);
				float[] featureFusion = featureFusion(slaFuzzySets);
				// Einstellungen fuer normale Videos
				if (AlgorithmSettings.getClassInstance().isSmoothSettings()) {
					if (featureFusion[2] < 0.75) {
						if (featureFusion[2] < 0.2) {
							if (frameBuffer.get(last - 5).isPossible) {
								if (checkHardCut(last - 5, last - 6)) {
									setCut(last - 5, 0, last - 5, last - 5);
								}
							}
						} else if (featureFusion[1] < 0.1
								&& featureFusion[0] < 0.1
								&& featureFusion[2] < 0.5) {
							if (frameBuffer.get(last - 5).isPossible) {
								if (checkHardCut(last - 5, last - 6)) {
									setCut(last - 5, 0, last - 5, last - 5);
								}
							}
						} else if (featureFusion[1] < 0.4) {
							if (featureFusion[1] + featureFusion[2] > 0.65) {
								if (frameBuffer.get(last - 5).isPossible) {
									if (checkHardCut(last - 5, last - 6)) {
										setCut(last - 5, 0, last - 5, last - 5);
									}
								}
							}
						}
					} else if (featureFusion[1] > 0.2) {
						if (frameBuffer.get(last - 5).isPossible) {
							if (checkHardCut(last - 5, last - 6)) {
								setCut(last - 5, 0, last - 5, last - 5);
							}
						}
					}
				} else {
					if (featureFusion[2] < 0.7) {
						if (featureFusion[2] < 0.2) {
							if (frameBuffer.get(last - 5).isPossible) {
								if (checkHardCut(last - 5, last - 6)) {
									setCut(last - 5, 0, last - 5, last - 5);
								}
							}
						} else if (featureFusion[1] < 0.1
								&& featureFusion[0] < 0.1
								&& featureFusion[2] < 0.5) {
							if (frameBuffer.get(last - 5).isPossible) {
								if (checkHardCut(last - 5, last - 6)) {
									setCut(last - 5, 0, last - 5, last - 5);
								}
							}
						} else if (featureFusion[1] < 0.4) {
							if (featureFusion[1] + featureFusion[2] > 0.65) {
								if (frameBuffer.get(last - 5).isPossible) {
									if (checkHardCut(last - 5, last - 6)) {
										setCut(last - 5, 0, last - 5, last - 5);
									}
								}
							}
						}
					} else if (featureFusion[1] > 0.26) {
						if (frameBuffer.get(last - 5).isPossible) {
							if (checkHardCut(last - 5, last - 6)) {
								setCut(last - 5, 0, last - 5, last - 5);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Berechnet das Verhältnis der Features in Bezug auf ein bestimmtes Bild.
	 * Es werden die vorherigen und nachfolgenden Bilder zum Vergleich
	 * verwendet.
	 * 
	 * @return Feature Ratio Wert
	 */
	private float[] ratioFeaturesOneSided() {
		float[] featureRatios = new float[ShotDetectionSettings.AMOUNTDETECTIONALGORITHM];
		boolean isOneSided = false;
		int last = frameBuffer.size() - 1;
		for (int i = 0; i < featureRatios.length; i++) {
			float leftSide = frameBuffer.get(last - 8).getPeakValues()[i]
					+ frameBuffer.get(last - 7).getPeakValues()[i]
					+ frameBuffer.get(last - 6).getPeakValues()[i];
			float rightSide = frameBuffer.get(last - 4).getPeakValues()[i]
					+ frameBuffer.get(last - 3).getPeakValues()[i]
					+ frameBuffer.get(last - 2).getPeakValues()[i];
			float relationLeftSide = frameBuffer.get(last - 5).getPeakValues()[i]
					/ leftSide;
			float relationRightSide = frameBuffer.get(last - 5).getPeakValues()[i]
					/ rightSide;
			if (relationLeftSide > ShotDetectionSettings.ONESIDEDFEATURETHRESHOLD
					&& relationRightSide < 0.5) {
				isOneSided = true;
			} else if (relationRightSide > ShotDetectionSettings.ONESIDEDFEATURETHRESHOLD
					&& relationLeftSide < 0.5) {
				isOneSided = true;
			}
		}

		// Nun wird der EdgeChangeRatio Algorithmus als 4ter Algorithmus zur
		// Hilfe herangezogen, da vorher nicht eindeutig ist ob es einen Hardcut
		// gibt!
		// Es gibt 2 Moeglichkeiten. Das Bild hat nach einer schnellen evt
		// ruckeligen Bildabfolge einen Hardcut oder eben nicht.
		// Letzters waere zb wenn die Kamera aprupt stehen bleiben wuerde.
		if (isOneSided) {
			// Suche aus der GradualFrameList den richtigen Frame heraus!
			float average = 0;
			int counter = 0;
			for (int i = last - 8; i <= last - 2; i++) {
				frameBuffer.get(i).setDilatedImage(
						frameBuffer.get(i).setEdgeImage());
			}
			for (int i = last - 7; i <= last - 2; i++) {
				if (i != last - 5) {

					float xIN = calculateCorruptedPixels(frameBuffer.get(i)
							.getEdgeImage(), frameBuffer.get(i - 1)
							.getDilatedImage());
					float xOUT = calculateCorruptedPixels(frameBuffer
							.get(i - 1).getEdgeImage(), frameBuffer.get(i)
							.getDilatedImage());
					float ecrValue1 = (xIN / frameBuffer.get(i - 1)
							.getAmountEdgePoints());
					float ecrValue2 = (xOUT / frameBuffer.get(i)
							.getAmountEdgePoints());
					float ecr = Math.max(ecrValue1, ecrValue2);
					average += ecr;
					counter++;
				}
			}
			average = average / counter;
			float xIN = calculateCorruptedPixels(frameBuffer.get(last - 5)
					.getEdgeImage(), frameBuffer.get(last - 6)
					.getDilatedImage());
			float xOUT = calculateCorruptedPixels(frameBuffer.get(last - 6)
					.getEdgeImage(), frameBuffer.get(last - 5)
					.getDilatedImage());
			float ecrValue1 = (xIN / frameBuffer.get(last - 6)
					.getAmountEdgePoints());
			float ecrValue2 = (xOUT / frameBuffer.get(last - 5)
					.getAmountEdgePoints());
			float ecr = Math.max(ecrValue1, ecrValue2);
			float value = ecr / average;
			if (value > ShotDetectionSettings.HARDCUTTHRESHOLD
					&& (ecrValue1 > ShotDetectionSettings.HARDCUTSECONDTHRESHOLD || ecrValue2 > ShotDetectionSettings.HARDCUTSECONDTHRESHOLD)) {
				setCut(last - 5, 3, last - 5, last - 5);
				frameBuffer.get(last - 5).setUnknown();
			}
		} else {
			for (int i = 0; i < featureRatios.length; i++) {
				float temp = frameBuffer.get(last - 10).getPeakValues()[i]
						+ frameBuffer.get(last - 9).getPeakValues()[i]
						+ frameBuffer.get(last - 8).getPeakValues()[i]
						+ frameBuffer.get(last - 7).getPeakValues()[i]
						+ frameBuffer.get(last - 6).getPeakValues()[i]
						+ frameBuffer.get(last - 4).getPeakValues()[i]
						+ frameBuffer.get(last - 3).getPeakValues()[i]
						+ frameBuffer.get(last - 2).getPeakValues()[i]
						+ frameBuffer.get(last - 1).getPeakValues()[i]
						+ frameBuffer.get(last).getPeakValues()[i];
				if (temp != 0) {
					featureRatios[i] = (frameBuffer.get(last - 5)
							.getPeakValues()[i] / (temp / 1.6f));
				}
			}
		}
		return featureRatios;
	}

	/**
	 * Die Featurewerte werden fuzzifiziert. Details können in der
	 * Bachelorarbeit nachgelesen werden.
	 * 
	 * @param featureRatios
	 *            als Eingabeparameter welche fuzzifiziert werden
	 * @return Fuzzywerte
	 */
	private float[][] fuzzifyFeatures(float[] featureRatios) {
		float[][] fuzzySets = new float[ShotDetectionSettings.AMOUNTDETECTIONALGORITHM][3];
		for (int i = 0; i < ShotDetectionSettings.AMOUNTDETECTIONALGORITHM; i++) {
			if (featureRatios[i] > ShotDetectionSettings.fuzzySetVariableC) {
				fuzzySets[i][0] = 1;
			} else if (featureRatios[i] > ShotDetectionSettings.fuzzySetVariableB
					&& featureRatios[i] < ShotDetectionSettings.fuzzySetVariableC) {
				fuzzySets[i][0] = (featureRatios[i] - ShotDetectionSettings.fuzzySetVariableB)
						/ (ShotDetectionSettings.fuzzySetVariableC - ShotDetectionSettings.fuzzySetVariableB);
			} else {
				fuzzySets[i][0] = 0;
			}
			if (featureRatios[i] > ShotDetectionSettings.fuzzySetVariableB
					&& featureRatios[i] < ShotDetectionSettings.fuzzySetVariableC) {
				fuzzySets[i][1] = (ShotDetectionSettings.fuzzySetVariableC - featureRatios[i])
						/ (ShotDetectionSettings.fuzzySetVariableC - ShotDetectionSettings.fuzzySetVariableB);
			} else if (ShotDetectionSettings.fuzzySetVariableA < featureRatios[i]
					&& featureRatios[i] < ShotDetectionSettings.fuzzySetVariableB) {
				fuzzySets[i][1] = (featureRatios[i] - ShotDetectionSettings.fuzzySetVariableA)
						/ (ShotDetectionSettings.fuzzySetVariableB - ShotDetectionSettings.fuzzySetVariableA);
			} else {
				fuzzySets[i][1] = 0;
			}
			if (featureRatios[i] < ShotDetectionSettings.fuzzySetVariableA) {
				fuzzySets[i][2] = 1;
			} else if (ShotDetectionSettings.fuzzySetVariableA < featureRatios[i]
					&& featureRatios[i] < ShotDetectionSettings.fuzzySetVariableB) {
				fuzzySets[i][2] = (ShotDetectionSettings.fuzzySetVariableB - featureRatios[i])
						/ (ShotDetectionSettings.fuzzySetVariableB - ShotDetectionSettings.fuzzySetVariableA);
			} else {
				fuzzySets[i][2] = 0;
			}
		}
		return fuzzySets;
	}

	/**
	 * Anwendung der Fuzzy Logic Inference Method
	 * 
	 * @param slaFuzzySets
	 *            sind die errechneten Zugehörigekeiten der einzelnen Merkmale
	 *            zu den Möglichkeiten slight, abrupt und large.
	 * 
	 * @return Möglichkeiten ob es sich bei dem Bild um einen langsamen,
	 *         schnellen oder abrupten Übergang handelt.
	 */
	private float[] featureFusion(float[][] slaFuzzySets) {
		float[] and1 = new float[ShotDetectionSettings.AMOUNTDETECTIONALGORITHM];
		and1[0] = Math.max(slaFuzzySets[0][0], slaFuzzySets[1][0]);
		and1[1] = Math.max(slaFuzzySets[0][1], slaFuzzySets[1][1]);
		and1[2] = Math.max(slaFuzzySets[0][2], slaFuzzySets[1][2]);

		float[] and2 = new float[ShotDetectionSettings.AMOUNTDETECTIONALGORITHM];
		and2[0] = Math.max(slaFuzzySets[0][0], slaFuzzySets[2][0]);
		and2[1] = Math.max(slaFuzzySets[0][1], slaFuzzySets[2][1]);
		and2[2] = Math.max(slaFuzzySets[0][2], slaFuzzySets[2][2]);

		float[] and3 = new float[ShotDetectionSettings.AMOUNTDETECTIONALGORITHM];
		and3[0] = Math.max(slaFuzzySets[1][0], slaFuzzySets[2][0]);
		and3[1] = Math.max(slaFuzzySets[1][1], slaFuzzySets[2][1]);
		and3[2] = Math.max(slaFuzzySets[1][2], slaFuzzySets[2][2]);

		float[] result = new float[ShotDetectionSettings.AMOUNTDETECTIONALGORITHM];
		result[0] = Math.min(Math.min(and1[0], and2[0]), and3[0]);
		result[1] = Math.min(Math.min(and1[1], and2[1]), and3[1]);
		result[2] = Math.min(Math.min(and1[2], and2[2]), and3[2]);
		return result;
	}

	/**
	 * Erstes Bild der Videos wird verarbeitet
	 */
	private void initializeFirstFrame() {
		internAlgorithm[0] = new HistogramIntersection(width, height);
		if (ShotDetectionSettings.isFastMotionCompensation()) {
			internAlgorithm[1] = new MotionCompensationDiamondSearch(width,
					height);
		} else {
			internAlgorithm[1] = new MotionCompensationExponentialSearch(width,
					height);
		}
		internAlgorithm[2] = new TextureEnergyDifference(width, height);
		for (int i = 0; i < internAlgorithm.length; i++) {
			internAlgorithm[i].setImage(image);
			internAlgorithm[i].setCurrentRGB(currentFrameRGB);
			internAlgorithm[i].initializeFirstFrameValues();
		}
	}

	/**
	 * Schreibt die RGB Werte des Bildes in das dafür vorgesehene Array
	 */
	private void getCurrentRGBValues() {
		if (currentFrameRGB == null) {
			currentFrameRGB = new int[3][width * height];
		}
		Raster raster = image.getRaster();
		raster.getSamples(0, 0, width, height, 0, currentFrameRGB[2]);
		raster.getSamples(0, 0, width, height, 1, currentFrameRGB[1]);
		raster.getSamples(0, 0, width, height, 2, currentFrameRGB[0]);
	}

	/**
	 * Ein Übergang wurde gefunden und wird entsprechend gespeichert.
	 * 
	 * @param cutInBuffer
	 * @param category
	 * @param startPossible
	 * @param stopPossible
	 */
	public void setCut(int cutInBuffer, int category, int startPossible,
			int stopPossible) {
		BufferedImage[] images;
		if (category == 1 || category == 2) {
			images = new BufferedImage[1];
			images[0] = frameBuffer.get(cutInBuffer).getOriginalImage();
		} else {
			images = new BufferedImage[2];
			images[0] = frameBuffer.get(cutInBuffer - 1).getOriginalImage();
			images[1] = frameBuffer.get(cutInBuffer).getOriginalImage();
		}
		long frameNr = threadFrames[threadId]
				+ frameBuffer.get(cutInBuffer).getFrameNr() - 1;

		cutList.add(new Cut(images, category, calcFrame(frameNr) - 1,
				frameBuffer.get(cutInBuffer - 1).getMediaTime(), startPossible,
				stopPossible));
		if (category != 3) {
			for (int i = startPossible; i <= stopPossible; i++) {
				if (i < frameBuffer.size()) {
					frameBuffer.get(i).setNotPossible();
				}
			}
		}
	}

	public LinkedList<Frame> getFrameBuffer() {
		return frameBuffer;
	}

	void dissolveDetection() {
		int start = ShotDetectionSettings.DISSOLVEPOINTERPOSITION;
		// if (frameBuffer.size() == ShotDetectionSettings.DETECTIONWINDOWSIZE)
		// {
		int center = 0;
		// Linke Seite
		int counter = 1;
		float first = frameBuffer.get(start).getVariance();
		float dissolvestart = first;
		float second = frameBuffer.get(start + 1).getVariance();
		int unknownDetected = -1;
		int row = 0;
		while ((frameBuffer.size() > counter + 2
				+ ShotDetectionSettings.DISSOLVEPOINTERPOSITION)
				&& (row < 2)) {
			if ((first > second)) {
				counter++;
				first = second;
				second = frameBuffer.get(counter).getVariance();
				if (frameBuffer.get(counter).isUnknown()) {
					unknownDetected = counter;
				}
			} else {
				counter++;
				first = second;
				row++;
				second = frameBuffer.get(counter).getVariance();
				if (frameBuffer.get(counter).isUnknown()) {
					unknownDetected = counter;
				}
			}
		}
		center = counter - 2;
		if (center < 0) {
			center = 0;
		}
		float centerVariance = frameBuffer.get(center).getVariance();
		row = 0;
		// Rechte Seite
		while ((frameBuffer.size() > (counter + 1)) && (frameBuffer.get(counter + 1).getIsPossible() && (row < 2))) {
			if ((first < second)) {
				counter++;
				first = second;
				second = frameBuffer.get(counter).getVariance();
				if (frameBuffer.get(counter).isUnknown()) {
					unknownDetected = counter;
				}
			} else {
				counter++;
				first = second;
				second = frameBuffer.get(counter).getVariance();
				if (frameBuffer.get(counter).isUnknown()) {
					unknownDetected = counter;
				}
				row++;
			}
		}

		// DissolveCheck!
		// Threshold per Funktion errechnen
		double functionValue1 = Math.pow(50,
				(-(140 / Math.pow(dissolvestart, 1.25)) + 1.1)) / 100;
		double functionValue2 = Math.pow(50,
				(-(140 / Math.pow(first, 1.25)) + 1.1)) / 100;
		boolean check1 = false;
		boolean check2 = false;
		boolean updateUnknownCut = false;
		if (((centerVariance / dissolvestart) < functionValue1 * 1.45)
				&& ((centerVariance / first) < (functionValue2))) {
			check1 = true;
		}
		if (((centerVariance / dissolvestart) < functionValue1)
				&& ((centerVariance / first) < functionValue2 * 1.45)) {
			check2 = true;
		}

		if (counter >= 8 && (check1 || check2) && (center - start) >= 3
				&& (counter - start) >= 3) {
			if (unknownDetected == -1) {
				if (checkDissolvePossibility(frameBuffer, counter - 2)
						&& finalDissolveCheck(frameBuffer, center, counter - 2)) {
					setCut(center, 2, 0, counter - 1 + 15);
				}
			} else {
				updateUnknownCut = true;
			}
		} else if ((counter >= 8)
				&& (((centerVariance / dissolvestart) < functionValue1) || ((centerVariance / first) < functionValue2))
				&& (center - start) >= 3 && (counter - start) >= 3) {
			frameBuffer.get(start + counter).setDilatedImage(
					frameBuffer.get(start + counter).setEdgeImage());
			frameBuffer.get(start).setDilatedImage(
					frameBuffer.get(start).setEdgeImage());
			float xIN = calculateCorruptedPixels(
					frameBuffer.get(start + counter).getEdgeImage(),
					frameBuffer.get(start).getDilatedImage());
			float xOUT = calculateCorruptedPixels(frameBuffer.get(start)
					.getEdgeImage(), frameBuffer.get(start + counter)
					.getDilatedImage());
			float ecrValue1 = (xIN / frameBuffer.get(start)
					.getAmountEdgePoints());
			float ecrValue2 = (xOUT / frameBuffer.get(start + counter)
					.getAmountEdgePoints());
			float ecr = Math.max(ecrValue1, ecrValue2);
			if (ecr > 0.5) {
				if (unknownDetected == -1) {
					if (checkDissolvePossibility(frameBuffer, counter - 2)
							&& finalDissolveCheck(frameBuffer, center,
									counter - 2)) {
						setCut(center, 2, 0, counter - 1 + 15);
					}
				} else {
					updateUnknownCut = true;
				}
			}
		}
		if (updateUnknownCut) {
			long unknownFrame = frameBuffer.get(unknownDetected).getFrameNr();
			for (int i = cutList.size() - 1; i >= 0; i--) {
				if (cutList.get(i).getCutFrameNr() == unknownFrame) {
					BufferedImage[] images = new BufferedImage[1];
					images[0] = frameBuffer.get(center).getOriginalImage();
					cutList.get(i).update(images, 2, unknownFrame);
					for (int j = 0; j < counter; j++) {
						frameBuffer.get(j).setNotPossible();
					}
				}
			}
		}
		// }
	}

	private boolean checkDissolvePossibility(LinkedList<Frame> lst, int end) {
		for (int i = 0; i <= end; i++) {
			if (!lst.get(i).getIsPossible()) {
				return false;
			}
		}
		return true;
	}

	private boolean finalDissolveCheck(LinkedList<Frame> lst, int center,
			int end) {
		BufferedImage firstimg = lst.get(0).getOriginalImage();
		BufferedImage secondimg = lst.get(end).getOriginalImage();
		ThreeFeaturesInterface[] internAlgorithm = new ThreeFeaturesInterface[2];
		internAlgorithm[0] = new HistogramIntersection(width, height);
		internAlgorithm[1] = new TextureEnergyDifference(width, height);

		int[][] currentFrameRGB = new int[3][width * height];

		Raster raster = firstimg.getRaster();
		raster.getSamples(0, 0, width, height, 0, currentFrameRGB[2]);
		raster.getSamples(0, 0, width, height, 1, currentFrameRGB[1]);
		raster.getSamples(0, 0, width, height, 2, currentFrameRGB[0]);

		for (int i = 0; i < internAlgorithm.length; i++) {
			internAlgorithm[i].setImage(firstimg);
			internAlgorithm[i].setCurrentRGB(currentFrameRGB);
			internAlgorithm[i].initializeFirstFrameValues();
		}

		raster = secondimg.getRaster();
		raster.getSamples(0, 0, width, height, 0, currentFrameRGB[2]);
		raster.getSamples(0, 0, width, height, 1, currentFrameRGB[1]);
		raster.getSamples(0, 0, width, height, 2, currentFrameRGB[0]);

		float[] currentPeakValues = new float[3];

		for (int i = 0; i < internAlgorithm.length; i++) {
			internAlgorithm[i].setImage(secondimg);
			internAlgorithm[i].setCurrentRGB(currentFrameRGB);
			currentPeakValues[i] = internAlgorithm[i].getPeakValue();
		}
		float ecr = difference(lst, 0, end);
		// System.out.println("Histogramm: " + currentPeakValues[0]
		// + "Edge Change: " + ecr + "Center: "
		// + lst.get(center).getFrameNr() + "StartframeNr: "
		// + lst.get(0).getFrameNr() + "EndFrameNr: "
		// + lst.get(end).getFrameNr());
		if ((currentPeakValues[0] > 30000 && ecr > 0.25)
				|| (currentPeakValues[0] > 25000 && ecr > 0.45)
				|| (currentPeakValues[0] > 20000 && ecr > 0.5)
				|| (currentPeakValues[0] > 10000 && ecr > 0.7)) {
			if (checkMotionFlow(lst, end, center)
					&& checkMovementFails(lst, center, 0, end)
					&& checkVariance(lst.get(0), lst.get(center), lst.get(end))) {
				return true;
			}
		}
		return false;
	}

	private boolean checkMotionFlow(LinkedList<Frame> lst, int end, int center) {
		LinkedList<Float> vals = new LinkedList<Float>();
		boolean check = true;
		for (int i = 1; i < end; i++) {
			BufferedImage first = lst.get(i - 1).getOriginalImage();
			BufferedImage second = lst.get(i).getOriginalImage();
			HistogramIntersection histogram = new HistogramIntersection(width,
					height);

			int[][] currentFrameRGB = new int[3][width * height];

			Raster raster = first.getRaster();
			raster.getSamples(0, 0, width, height, 0, currentFrameRGB[2]);
			raster.getSamples(0, 0, width, height, 1, currentFrameRGB[1]);
			raster.getSamples(0, 0, width, height, 2, currentFrameRGB[0]);

			histogram.setImage(first);
			histogram.setCurrentRGB(currentFrameRGB);
			histogram.initializeFirstFrameValues();

			raster = second.getRaster();
			raster.getSamples(0, 0, width, height, 0, currentFrameRGB[2]);
			raster.getSamples(0, 0, width, height, 1, currentFrameRGB[1]);
			raster.getSamples(0, 0, width, height, 2, currentFrameRGB[0]);

			float currentPeakValue = 0;
			histogram.setImage(second);
			histogram.setCurrentRGB(currentFrameRGB);
			currentPeakValue = histogram.getPeakValue();

			float ecr = difference(lst, i - 1, i);

			vals.add(ecr);

			// System.out.println("i-Wert: " + i + " NEUDISSOLVE HISTOGRAMM: "
			// + currentPeakValue + " Edge Change Ratio: " + ecr
			// + "Center: " + center);
			if (currentPeakValue > 20000 && ecr > 20000) {
				check = false;
			}
		}
		// System.out.println("MOTIONFLOWAVERAGE: " + motionFlowAverage(vals));
		if (check) {
			float flow = motionFlowAverage(vals);
			// System.out.println("MOTIONFLOWAVERAGE: " + flow);
			if (flow > 0.30) {
				check = false;
			}
		}
		return check;
	}

	private float difference(LinkedList<Frame> frameBuffer, int start, int end) {

		frameBuffer.get(start).setDilatedImage(
				frameBuffer.get(start).setEdgeImage());
		frameBuffer.get(end).setDilatedImage(
				frameBuffer.get(end).setEdgeImage());
		float xIN = calculateCorruptedPixels(frameBuffer.get(end)
				.getEdgeImage(), frameBuffer.get(start).getDilatedImage());
		float xOUT = calculateCorruptedPixels(frameBuffer.get(start)
				.getEdgeImage(), frameBuffer.get(end).getDilatedImage());

		float ecrValue1 = (xIN / frameBuffer.get(start).getAmountEdgePoints());
		float ecrValue2 = (xOUT / frameBuffer.get(end).getAmountEdgePoints());
		float ecr = Math.max(ecrValue1, ecrValue2);

		return ecr;
	}

	/**
	 * Berechnet die Anzahl der sich unterscheidenden Kantenpixel beim Edge
	 * Change Ratio Algorithmus
	 * 
	 * Details kÃ¶nnen in der Beschreibung des Edge Change Ratio Algorithmus
	 * nachgelesen werden.
	 * 
	 * @param edgeImage
	 * @param dilatedImage
	 * @return Anzahl der unterschiedlichen Pixel
	 */
	private float calculateCorruptedPixels(int[] edgeImage, int[] dilatedImage) {
		int counterCorruptedPixels = 0;
		for (int i = 0; i < edgeImage.length; i++) {
			if (edgeImage[i] != ShotDetectionSettings.BLACK
					&& dilatedImage[i] != ShotDetectionSettings.BLACK) {
				counterCorruptedPixels++;
			}
		}
		return counterCorruptedPixels;
	}

	/**
	 * Function to avoid wrong dissolves according to zoom / movement fails
	 */
	private boolean checkMovementFails(LinkedList<Frame> lst, int center,
			int start, int end) {
		BufferedImage mainImg = lst.get(center).getOriginalImage();
		BufferedImage startImg = lst.get(start).getOriginalImage();
		BufferedImage endImg = lst.get(end).getOriginalImage();

		BufferedImage[][] parts = new BufferedImage[3][4];
		parts[0] = splitImage(startImg);
		parts[1] = splitImage(mainImg);
		parts[2] = splitImage(endImg);

		double[][] stdVar = new double[3][4];
		for (int i = 0; i < 4; i++) {
			stdVar[0][i] = getAmountEdgePoints(parts[0][i]);
		}
		for (int i = 0; i < 4; i++) {
			stdVar[1][i] = getAmountEdgePoints(parts[1][i]);
		}
		for (int i = 0; i < 4; i++) {
			stdVar[2][i] = getAmountEdgePoints(parts[2][i]);
		}

		double leftSideValue, rightSideValue, leftSidePicture, rightSidePicture;

		// Von vorne!
		double frontLeftSideAverage = (stdVar[0][0] + stdVar[0][2])
				/ (stdVar[0][1] + stdVar[0][3]);
		double backLeftSideAverage = (stdVar[2][0] + stdVar[2][2])
				/ (stdVar[2][1] + stdVar[2][3]);

		frontLeftSideAverage = smoothFunction(frontLeftSideAverage);
		backLeftSideAverage = smoothFunction(backLeftSideAverage);

		leftSidePicture = (stdVar[1][0] + stdVar[1][2]) * frontLeftSideAverage;
		rightSidePicture = (stdVar[1][0] + stdVar[1][2]) * backLeftSideAverage;

		leftSideValue = leftSidePicture;
		rightSideValue = (stdVar[1][1] + stdVar[1][3]);

		if (leftSideValue < rightSideValue) {
			return false;
		}

		// Von hinten

		double frontRightSideAverage = (stdVar[0][1] + stdVar[0][3])
				/ (stdVar[0][0] + stdVar[0][2]);
		double backRightSideAverage = (stdVar[2][1] + stdVar[2][3])
				/ (stdVar[2][0] + stdVar[2][2]);

		frontRightSideAverage = smoothFunction(frontRightSideAverage);
		backRightSideAverage = smoothFunction(backRightSideAverage);

		leftSidePicture = (stdVar[1][1] + stdVar[1][3]) * frontRightSideAverage;
		rightSidePicture = (stdVar[1][1] + stdVar[1][3]) * backRightSideAverage;

		leftSideValue = rightSidePicture;
		rightSideValue = (stdVar[1][0] + stdVar[1][2]);

		if (leftSideValue < rightSideValue) {
			return false;
		}

		return true;
	}

	private double smoothFunction(double value) {
		if (value > 1) {
			value = 2.5;
		} else {
			value = 2.5 - (1 - value) + (Math.log10(value) * 0.75);
		}
		// Smoothing Function
		value = Math.exp(value - 1) - 0.7;
		if (value > 2.5) {
			value = 2.5;
		}

		else if (value < 1.46) {
			value = 1.46;
		}

		return value;
	}

	private boolean checkVariance(Frame start, Frame center, Frame end) {
		double startVar = start.getStandardVariation();
		double centerVar = center.getStandardVariation();
		double endVar = end.getStandardVariation();

		return true;
	}

	private int getAmountEdgePoints(BufferedImage savedImage) {
		int[] rgbarray = new int[(width / 2) * (height / 2)];
		savedImage.getRGB(0, 0, width / 2, height / 2, rgbarray, 0, width / 2);
		int[] edgeImage = edgeAlgorithm.process(rgbarray, width / 2,
				height / 2, 9, 0.45f, 1, 14, 1, 0);

		int counter = 0;
		for (int i = 0; i < edgeImage.length; i++) {
			if (edgeImage[i] != ShotDetectionSettings.BLACK) {
				counter++;
			}
		}
		return counter;
	}

	private BufferedImage[] splitImage(BufferedImage img) {
		BufferedImage[] parts = new BufferedImage[4];
		// left upper
		parts[0] = img.getSubimage(0, 0, width / 2, height / 2);
		// right upper
		parts[1] = img.getSubimage(width / 2, 0, width / 2, height / 2);
		// left lower
		parts[2] = img.getSubimage(0, height / 2, width / 2, height / 2);
		// right lower
		parts[3] = img
				.getSubimage(width / 2, height / 2, width / 2, height / 2);

		return parts;
	}

	private float motionFlowAverage(LinkedList<Float> values) {
		int minIndex = 0;
		int maxIndex = 0;

		for (int k = 0; k < 2; k++) {
			for (int i = 0; i < values.size(); i++) {
				if (values.get(minIndex) > values.get(i)) {
					minIndex = i;
				}
			}
			values.remove(minIndex);
			minIndex = 0;
		}

		for (int k = 0; k < 2; k++) {
			for (int i = 0; i < values.size(); i++) {
				if (values.get(maxIndex) < values.get(i)) {
					maxIndex = i;
				}
			}
			values.remove(maxIndex);
			maxIndex = 0;
		}

		float result = 0;
		for (int i = 0; i < values.size(); i++) {
			result += values.get(i);
		}

		result = result / values.size();

		return result;
	}

	void fadeDetectionWithStandardVariation() {
		if (frameBuffer.get(ShotDetectionSettings.FVPP).getIsPossible()
				&& frameBuffer.get(ShotDetectionSettings.FVPP + 1)
						.getIsPossible()) {
			double starter = frameBuffer.get(ShotDetectionSettings.FVPP)
					.getStandardVariation();
			double second = frameBuffer.get(ShotDetectionSettings.FVPP + 1)
					.getStandardVariation();
			boolean increase = true;
			float rising = 0;
			if (starter > second) {
				rising += starter - second;
				increase = false;
			} else {
				rising += second - starter;
			}
			int counter = ShotDetectionSettings.FVPP + 2;
			double lastValue = second;
			while ((frameBuffer.size() > counter + ShotDetectionSettings.FVPP)
					&& (counter) < ShotDetectionSettings.DETECTIONWINDOWSIZE
					&& frameBuffer.get(counter).getIsPossible()) {
				if (counter == ShotDetectionSettings.FADEMAXLENGHT) {
					break;
				}
				double current = frameBuffer.get(counter)
						.getStandardVariation();
				if (increase) {
					if (current > lastValue) {
						rising += current - lastValue;
						lastValue = current;
						counter++;
					} else {
						break;
					}
				} else {
					if (current < lastValue) {
						rising += lastValue - current;
						lastValue = current;
						counter++;

					} else {
						break;
					}
				}
			}
			rising = rising / counter;
			if (lastValue < ShotDetectionSettings.FADESTANDARDVARIANCE
					&& starter > ShotDetectionSettings.FADESTANDARDVARIANCE
					&& counter > ShotDetectionSettings.FADEMINLENGTH
							+ ShotDetectionSettings.FVPP
					&& rising > ShotDetectionSettings.FADERAISINGTHRESHOLD) {
				if (checkFade(frameBuffer,
						(int) Math.ceil(((counter - 6) * 4) / 5)
								+ ShotDetectionSettings.FVPP,
						ShotDetectionSettings.FVPP, counter)) {
					setCut((int) Math.floor(((counter - 6) * 4) / 5)
							+ ShotDetectionSettings.FVPP, 1,
							ShotDetectionSettings.FVPP, counter);
				}
			} else if (starter < ShotDetectionSettings.FADESTANDARDVARIANCE
					&& lastValue > ShotDetectionSettings.FADESTANDARDVARIANCE
					&& counter > ShotDetectionSettings.FADEMINLENGTH
							+ ShotDetectionSettings.FVPP
					&& rising > ShotDetectionSettings.FADERAISINGTHRESHOLD) {
				if (checkFade(frameBuffer,
						(int) Math.ceil(((counter - 6) * 3) / 5)
								+ ShotDetectionSettings.FVPP,
						ShotDetectionSettings.FVPP, counter)) {
					setCut((int) Math.ceil(((counter - 6) * 3) / 5)
							+ ShotDetectionSettings.FVPP, 1,
							ShotDetectionSettings.FVPP, counter);
				}
			}
		}
	}

	/**
	 * Function to avoid wrong fades according to zoom / movement fails
	 */
	private boolean checkFade(LinkedList<Frame> lst, int center, int start,
			int end) {
		BufferedImage mainImg = lst.get(center).getOriginalImage();
		BufferedImage startImg = lst.get(start).getOriginalImage();
		BufferedImage endImg = lst.get(end).getOriginalImage();

		BufferedImage[][] parts = new BufferedImage[3][4];
		parts[0] = splitImage(startImg);
		parts[1] = splitImage(mainImg);
		parts[2] = splitImage(endImg);

		double[][] stdVar = new double[3][4];
		for (int i = 0; i < 4; i++) {
			stdVar[0][i] = getAmountEdgePoints(parts[0][i]);
		}
		for (int i = 0; i < 4; i++) {
			stdVar[1][i] = getAmountEdgePoints(parts[1][i]);
		}
		for (int i = 0; i < 4; i++) {
			stdVar[2][i] = getAmountEdgePoints(parts[2][i]);
		}

		// left side
		if ((stdVar[1][0] + stdVar[1][2]) * 2 < (stdVar[1][1] + stdVar[1][3])) {
			if (!((stdVar[0][0] + stdVar[0][2]) * 2 < (stdVar[0][1] + stdVar[0][3]))
					|| !((stdVar[2][0] + stdVar[2][2]) * 2 < (stdVar[2][1] + stdVar[2][3]))) {
				return false;
			}
		}
		// right side
		if ((stdVar[1][1] + stdVar[1][3]) * 2 < (stdVar[1][0] + stdVar[1][2])) {
			if (!((stdVar[0][1] + stdVar[0][3]) * 2 < (stdVar[0][0] + stdVar[0][2]))
					|| !((stdVar[2][1] + stdVar[2][3]) * 2 < (stdVar[2][0] + stdVar[2][2]))) {
				return false;
			}
		}
		// upper side
		if ((stdVar[1][0] + stdVar[1][1]) * 2 < (stdVar[1][2] + stdVar[1][3])) {
			if (!((stdVar[0][0] + stdVar[0][1]) * 2 < (stdVar[0][2] + stdVar[0][3]))
					|| !((stdVar[2][0] + stdVar[2][1]) * 2 < (stdVar[2][2] + stdVar[2][3]))) {
				return false;
			}
		}
		// lower side
		if ((stdVar[1][2] + stdVar[1][3]) * 2 < (stdVar[1][0] + stdVar[1][1])) {
			if (!((stdVar[0][2] + stdVar[0][3]) * 2 < (stdVar[0][0] + stdVar[0][1]))
					|| !((stdVar[2][2] + stdVar[2][3]) * 2 < (stdVar[2][0] + stdVar[2][1]))) {
				return false;
			}
		}

		float startEdges = checkEdges(startImg, -7000000);
		float endEdges = checkEdges(endImg, -7000000);
		if ((startEdges != 0) && (endEdges != 0)) {
			return false;
		}
		startEdges = checkEdges(startImg, -12000000);
		endEdges = checkEdges(endImg, -12000000);
		if ((startEdges / endEdges > 0.1) && (endEdges / startEdges > 0.1)) {
			return false;
		}
		return true;
	}

	private int checkEdges(BufferedImage img, int threshold) {
		int[] rgbarray = new int[(width / 2) * (height / 2)];
		img.getRGB(0, 0, width / 2, height / 2, rgbarray, 0, width / 2);
		int[] edgeImage = edgeAlgorithm.process(rgbarray, width / 2,
				height / 2, 9, 0.45f, 1, 14, 1, 0);

		for (int i = 0; i < edgeImage.length; i++) {
			if (edgeImage[i] < threshold) {
				edgeImage[i] = ShotDetectionSettings.BLACK;
			} else {
				edgeImage[i] = ShotDetectionSettings.WHITE;
			}
		}
		int amountPoints = 0;
		for (int i = 0; i < edgeImage.length; i++) {
			if (edgeImage[i] != ShotDetectionSettings.BLACK) {
				amountPoints++;
			}
		}
		return amountPoints;
	}

	public List<Cut> finalizeAlgorithm() {
		SDTime timeLast = frameBuffer.get(frameBuffer.size() - 1)
				.getMediaTime()[0];
		frameBuffer.remove(0);
		while (frameBuffer.size() > 20) {
			fadeDetectionWithStandardVariation();
			dissolveDetection();
			frameBuffer.remove(0);
		}

		// Add last frame as Hardcut
		if (threadId == amountThreads - 1) {
			BufferedImage[] currentImages = new BufferedImage[2];
			currentImages[0] = frameBuffer.getLast().getOriginalImage();
			currentImages[1] = null;
			long frameNr = threadFrames[threadId]
					+ frameBuffer.getLast().getFrameNr();
			SDTime[] time = { timeLast, null };
			cutList.add(new Cut(currentImages, 0, calcFrame(frameNr), time,
					frameNr, frameNr + 1));
		}
		return cutList;
	}

	/**
	 * Ein Bild mit seinen Berechnungen um es im Bildbuffer abzuspeichern
	 */
	class Frame {

		private BufferedImage savedImage;
		private boolean isGradualSection;
		private boolean isPossible;
		private boolean unknownCut;
		private int avgLuminance;
		private double standardVariation;
		private int[] edgeImage;
		private int[] dilatedImage;
		private int amountEdgePoints;
		private BufferedImage grayImage;
		private float[] peakValues;
		private float variance;
		private long frameNr;
		private SDTime[] time;
		private int width;
		private int height;

		Frame(BufferedImage savedImage, float[] peakValues, long frameNr,
				SDTime[] time, boolean standard) {
			this.time = time;
			this.savedImage = savedImage;
			this.peakValues = peakValues;
			this.frameNr = frameNr;
			this.isPossible = true;
			this.isGradualSection = true;
			this.unknownCut = false;
			this.width = savedImage.getWidth();
			this.height = savedImage.getHeight();
			if (ShotDetectionSettings.isFadeWithStandardVariation() && standard) {
				this.avgLuminance = detectAverageLuminance();
				this.standardVariation = detectStandardVariation();
			}
			if (ShotDetectionSettings.isEnableDissolve() && standard) {
				setEdgeImage();
			}
		}

		boolean getGradualSection() {
			return isGradualSection;
		}

		boolean isUnknown() {
			return unknownCut;
		}

		void setUnknown() {
			this.unknownCut = true;
		}

		void removeGradualSection() {
			isGradualSection = false;
		}

		double getStandardVariation() {
			return this.standardVariation;
		}

		int[] getEdgeImage() {
			return edgeImage;
		}

		int[] getDilatedImage() {
			return dilatedImage;
		}

		float[] getPeakValues() {
			return peakValues;
		}

		int getAmountEdgePoints() {
			return amountEdgePoints;
		}

		void setNotPossible() {
			this.isPossible = false;
		}

		boolean getIsPossible() {
			return isPossible;
		}

		float getVariance() {
			return this.variance;
		}

		long getFrameNr() {
			return frameNr;
		}

		BufferedImage getOriginalImage() {
			return savedImage;
		}

		SDTime[] getMediaTime() {
			return time;
		}

		/**
		 * Berechnet Durchschnittsluminanz des Bildes. Wird für Fadeerkennung
		 * benötigt
		 * 
		 * @return Durchschnittsluminanz
		 */
		private int detectAverageLuminance() {
			int[] luminance = new int[height * width];
			BufferedImage gray = new BufferedImage(width, height,
					BufferedImage.TYPE_BYTE_GRAY);
			ColorConvertOp grayScaleConversionOp = new ColorConvertOp(
					ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
			grayScaleConversionOp.filter((BufferedImage) savedImage, gray);
			grayImage = gray;
			Raster raster = gray.getRaster();
			raster.getSamples(0, 0, width, height, 0, luminance);
			int average = 0;
			for (int i = 0; i < luminance.length; i++) {
				average += luminance[i];
			}
			average = average / luminance.length;
			return average;
		}

		/**
		 * Berechnet Standardabweichung der Luminanz
		 */
		private double detectStandardVariation() {
			int[] luminance = new int[height * width];
			Raster raster = grayImage.getRaster();
			raster.getSamples(0, 0, width, height, 0, luminance);
			double sum = 0;
			for (int i = 0; i < luminance.length; i++) {
				sum += Math.pow(Math.abs((avgLuminance - luminance[i])), 2);
			}
			sum = sum / luminance.length;
			return Math.sqrt(sum);
		}

		/**
		 * Berechnet das zugehörige Kantenbild jeden Bildes
		 * 
		 * @return Kantenbild
		 */
		int[] setEdgeImage() {
			int[] rgbarray = new int[width * height];
			savedImage.getRGB(0, 0, width, height, rgbarray, 0, width);
			int[] edgeImage = edgeAlgorithm.process(rgbarray, width, height, 9,
					0.45f, 1, 14, 1, 0);

			int[] bild = new int[edgeImage.length];
			for (int i = 0; i < edgeImage.length; i++) {
				bild[i] = (edgeImage[i] + 16777217) * (-1);
			}

			int counter = 0;
			float variance = 0;
			int[] points = new int[edgeAlgorithm.getEdgeCounter()];
			for (int i = 0; i < edgeImage.length; i++) {
				if (edgeImage[i] != ShotDetectionSettings.BLACK) {
					if (ShotDetectionSettings.isEnableDissolve()) {
						variance += calculatePointVariance(i);
					}
					edgeImage[i] = ShotDetectionSettings.WHITE;
					points[counter] = i;
					counter++;
				}
			}
			variance = variance / counter;
			this.variance = variance;
			this.amountEdgePoints = counter;
			this.edgeImage = edgeImage;
			return points;
		}

		/**
		 * Berechnet Varianzen der Punkte innerhalb des 3*3 Fensters.
		 * Erläuterung => Bachelorarbeit
		 * 
		 * @param position
		 *            Zentrumspunkt des 3*3 Fensters.
		 * @return Varianz
		 */
		private float calculatePointVariance(int position) {
			float variance = 0;
			float windowaverage = 0;
			int startarrayindex = position - width - 1;
			float[] singlevalues = new float[9];
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					int arrayindex = startarrayindex + j + i * width;
					float positionYValue = ShotDetectionSettings.Y_VALUE_R
							* currentFrameRGB[0][arrayindex]
							+ ShotDetectionSettings.Y_VALUE_G
							* currentFrameRGB[1][arrayindex]
							+ ShotDetectionSettings.Y_VALUE_B
							* currentFrameRGB[2][arrayindex];
					windowaverage += positionYValue;
					singlevalues[3 * i + j] = positionYValue;
				}
			}
			windowaverage = windowaverage / 9;
			for (int i = 0; i < singlevalues.length; i++) {
				variance += Math.pow((singlevalues[i] - windowaverage), 2);
			}
			variance = variance / 9;
			return variance;
		}

		/**
		 * Wandelt das als Parameter übergebene Bild in eine dilatierte Version
		 * um.
		 * 
		 * @param points
		 *            Das Bild, welches umgewandelt werden soll.
		 */
		void setDilatedImage(int[] points) {
			int[] edgeImage = getEdgeImage();
			int[] dilation = new int[edgeImage.length];
			for (int i = 0; i < points.length; i++) {
				if (edgeImage[points[i]] == ShotDetectionSettings.WHITE) {
					int temp = points[i] - width - 1;
					for (int j = 0; j < 6; j++) {
						for (int j2 = 0; j2 < 6; j2++) {
							if (temp + j * width + j2 >= 0
									&& temp + j * width + j2 < dilation.length) {
								dilation[temp + j * width + j2] = ShotDetectionSettings.BLACK;
							}
						}
					}
				}
			}
			for (int i = 0; i < dilation.length; i++) {
				if (dilation[i] == 0) {
					dilation[i] = ShotDetectionSettings.WHITE;
				}
			}
			this.dilatedImage = dilation;
		}
	}

	private boolean checkHardCut(int start, int end) {

		Frame[] parts1 = splitHardCut(frameBuffer.get(start).getOriginalImage());
		Frame[] parts2 = splitHardCut(frameBuffer.get(end).getOriginalImage());
		int amountPartDifs = 0;
		float[] ecr = new float[9];
		for (int i = 0; i < 9; i++) {
			parts1[i].setDilatedImage(parts1[i].setEdgeImage());
			parts2[i].setDilatedImage(parts2[i].setEdgeImage());

			float xIn = calculateCorruptedPixels(parts2[i].getEdgeImage(),
					parts1[i].getDilatedImage());
			float xOut = calculateCorruptedPixels(parts1[i].getEdgeImage(),
					parts2[i].getDilatedImage());

			float ecrValue1 = (xIn / parts1[i].getAmountEdgePoints());
			float ecrValue2 = (xOut / parts2[i].getAmountEdgePoints());
			ecr[i] = Math.max(ecrValue1, ecrValue2);

			if (ecr[i] > 0.2) {
				amountPartDifs++;
			}
		}
		if (amountPartDifs == 3) {
			int tmp = 0;
			for (int i = 0; i < ecr.length; i++) {
				if (ecr[i] > 0.1) {
					tmp++;
				}
			}
			if (tmp > 7) {
				return true;
			}
		}
		if (amountPartDifs > 3) {
			return true;
		}
		return false;
	}

	private Frame[] splitHardCut(BufferedImage img) {
		Frame[] parts = new Frame[9];
		int divWidth = calcSplitSize(width, 3);
		int divHeight = calcSplitSize(height, 3);
		parts[0] = new Frame(img.getSubimage(0, 0, divWidth, divHeight), null,
				-1, null, false);
		parts[1] = new Frame(img.getSubimage(divWidth, 0, divWidth, divHeight),
				null, -1, null, false);
		parts[2] = new Frame(img.getSubimage(divWidth * 2, 0, divWidth,
				divHeight), null, -1, null, false);

		parts[3] = new Frame(
				img.getSubimage(0, divHeight, divWidth, divHeight), null, -1,
				null, false);
		parts[4] = new Frame(img.getSubimage(divWidth, divHeight, divWidth,
				divHeight), null, -1, null, false);
		parts[5] = new Frame(img.getSubimage(divWidth * 2, divHeight, divWidth,
				divHeight), null, -1, null, false);

		parts[6] = new Frame(img.getSubimage(0, divHeight * 2, divWidth,
				divHeight), null, -1, null, false);
		parts[7] = new Frame(img.getSubimage(divWidth, divHeight * 2, divWidth,
				divHeight), null, -1, null, false);
		parts[8] = new Frame(img.getSubimage(divWidth * 2, divHeight * 2,
				divWidth, divHeight), null, -1, null, false);

		return parts;
	}

	private int calcSplitSize(int size, int parts) {
		if (size % parts == 0) {
			return size / parts;
		} else {
			while (size % parts != 0) {
				size--;
			}
		}
		return size / parts;
	}

	@Override
	public String getName() {
		return "";
	}

	private long calcFrame(long input) {
		if (threadId == 0) {
			return input;
		} else {
			return input - 50;
		}
	}
}
