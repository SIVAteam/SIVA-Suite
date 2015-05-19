package org.iviPro.scenedetection.sd_main;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

public class Shot implements Cloneable, Comparable<Shot> {

	static final int blocksize = 16;

	static final float bestBlockPercentage = 0.5f;

	private CutTypes cut1Type;

	private CutTypes cut2Type;

	private SDTime timeStart;

	private SDTime timeEnd;

	private long startFrame;

	private long endFrame;

	private BufferedImage startImage;

	private BufferedImage endImage;

	private List<Keyframe> keyFrameLst;

	private int shotId;
	
	private static double maxQuadraticDistance = 0;

	private static DoubleMatrix2D colorSimilarityMatrix = calculateColorSimilarityMatrix();

	public Shot(CutTypes cut1Type, CutTypes cut2Type, long startFrame,
			long endFrame, SDTime timeStart, SDTime timeEnd) {
		this.cut1Type = cut1Type;
		this.cut2Type = cut2Type;
		this.startFrame = startFrame;
		this.endFrame = endFrame;
		this.timeStart = timeStart;
		this.timeEnd = timeEnd;
		this.keyFrameLst = new LinkedList<Keyframe>();
	}

	public CutTypes getCut1Type() {
		return cut1Type;
	}

	public CutTypes getCut2Type() {
		return cut2Type;
	}

	public long getStartFrame() {
		return startFrame;
	}

	public void setEndFrame(long frame) {
		this.endFrame = frame;
	}

	public long getEndFrame() {
		return endFrame;
	}

	public BufferedImage getStartImage() {
		if (startImage == null) {
			return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		}
		return startImage;
	}

	public void setStartImage(BufferedImage startImage) {
		this.startImage = startImage;
	}

	public BufferedImage getEndImage() {
		if (endImage == null) {
			// return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
			return null;
		}
		return endImage;
	}

	public void setEndImage(BufferedImage endImage) {
		this.endImage = endImage;
	}

	public List<Keyframe> getKeyFrameLst() {
		return keyFrameLst;
	}

	public void setKeyFrame(Keyframe keyframe) {
		// if (keyFrameLst.size() == 0) {
		keyFrameLst.add(keyframe);
		// } else {
		// for (int i = 0; i < keyFrameLst.size() - 1; i++) {
		// if (keyFrameLst.get(0).getFramenr() > keyframe.getFramenr()) {
		// keyFrameLst.add(0, keyframe);
		// break;
		// } else if (keyFrameLst.get(i).getFramenr() < keyframe
		// .getFramenr()
		// && keyFrameLst.get(i + 1).getFramenr() > keyframe
		// .getFramenr()) {
		// keyFrameLst.add(i + 1, keyframe);
		// break;
		// } else if (i == (keyFrameLst.size() - 2)) {
		// keyFrameLst.add(keyframe);
		// break;
		// }
		// }
		// }
	}

	public long getShotLength() {
		long temp = endFrame - startFrame;
		if (temp < 0) {
			return 0;
		} else {
			return temp;
		}
	}

	public void setShotID(int id) {
		this.shotId = id;
	}

	public int getShotId() {
		return shotId;
	}

	public long getStartTimeNano() {
		return timeStart.getNanoseconds();
	}

	public long getEndTimeNano() {
		return timeEnd.getNanoseconds();
	}

	public SDTime getTimeStart() {
		return timeStart;
	}

	public SDTime getTimeEnd() {
		return timeEnd;
	}

	public void setStartTimeNano(long start) {
		this.timeStart = new SDTime(start);
	}

	public void setEndTimeNano(long end) {
		this.timeEnd = new SDTime(end);
	}

	public SDTime getEndTimeObject() {
		return timeEnd;
	}

	@Override
	public Shot clone() {
		BufferedImage[] images = new BufferedImage[2];
		if (startImage != null) {
			images[0] = startImage.getSubimage(0, 0, startImage.getWidth(),
					startImage.getHeight());
		}
		if (endImage != null) {
			images[1] = endImage.getSubimage(0, 0, endImage.getWidth(),
					endImage.getHeight());
		}
		Shot clone = new Shot(this.cut1Type, this.cut2Type, this.startFrame,
				this.endFrame, new SDTime(timeStart.getNanoseconds()),
				new SDTime(timeEnd.getNanoseconds()));

		clone.setStartImage(images[0]);
		clone.setEndImage(images[1]);

		// Clone keyframelist
		for (int i = 0; i < keyFrameLst.size(); i++) {
			BufferedImage source = keyFrameLst.get(i).getImage();
			BufferedImage img = source.getSubimage(0, 0, source.getWidth(),
					source.getHeight());
			Keyframe key = new Keyframe(keyFrameLst.get(i).getFramenr(), img);
			clone.setKeyFrame(key);
		}
		clone.setShotID(this.getShotId());
		return clone;
	}

	@Override
	public int compareTo(Shot o) {
		if (startFrame == o.startFrame && endFrame == o.endFrame) {
			return 0;
		} else if (startFrame < o.startFrame && endFrame < o.endFrame) {
			return -1;
		} else {
			return 1;
		}
	}

	public void setSimilarityFeatures() {
		for (int i = 0; i < keyFrameLst.size(); i++) {
			keyFrameLst.get(i).setSimilarityFeatures();
		}
	}

	// MAINFUNCTIONS OF SIMILARITY CALCULATIONS
	public float calculateColorSimilarity(Shot toCompare) {
		float max = 0;
		for (int i = 0; i < keyFrameLst.size(); i++) {
			for (int j = 0; j < toCompare.getKeyFrameLst().size(); j++) {
				float colorIntersection = colorIntersection(keyFrameLst.get(i)
						.getColorHistogram(), toCompare.getKeyFrameLst().get(j)
						.getColorHistogram());
				if (max < colorIntersection) {
					max = colorIntersection;
				}
			}
		}
		return max;
	}
	

	public float calculateQuadraticFormDistance(Shot toCompare) {		
		double maxVal = Double.MAX_VALUE;
		for (int i = 0; i < keyFrameLst.size(); i++) {
			for (int j = 0; j < toCompare.getKeyFrameLst().size(); j++) {
				double[] histo1 = keyFrameLst.get(i).getRGBcolorHistogram();
				double[] histo2 = toCompare.getKeyFrameLst().get(j).getRGBcolorHistogram();
				double[] sub = new double[histo1.length];
				for (int k = 0; k < histo1.length; k++) {
					sub[k] = histo1[k] - histo2[k];
				}
				DenseDoubleMatrix1D binVec = new DenseDoubleMatrix1D(sub);
				DoubleMatrix1D firstResult = Algebra.DEFAULT.mult(colorSimilarityMatrix, binVec);
				double distance = Math.sqrt(Algebra.DEFAULT.mult(binVec, firstResult));
//				System.out.println("Distance: "+distance);
				if(maxVal > distance) {
					maxVal = distance;
				}
//				if(Shot.maxQuadraticDistance < maxVal) {
//					Shot.maxQuadraticDistance = maxVal;
//				}
			}
		}
		double val = maxVal / (Shot.maxQuadraticDistance * 0.5);
		if(val > 1.0) {
			val = 1.0;
		}
//		System.out.println("returnVal von Shot : "+getShotId()+" zu "+toCompare.getShotId()+"val: "+(1 - val));
		return (float) (1 - val);
	}
	
	public void setMaxVal(Shot toCompare) {
		for (int i = 0; i < keyFrameLst.size(); i++) {
			for (int j = 0; j < toCompare.getKeyFrameLst().size(); j++) {
				double[] histo1 = keyFrameLst.get(i).getRGBcolorHistogram();
				double[] histo2 = toCompare.getKeyFrameLst().get(j).getRGBcolorHistogram();
				double[] sub = new double[histo1.length];
				for (int k = 0; k < histo1.length; k++) {
					sub[k] = histo1[k] - histo2[k];
				}
				DenseDoubleMatrix1D binVec = new DenseDoubleMatrix1D(sub);
				DoubleMatrix1D firstResult = Algebra.DEFAULT.mult(colorSimilarityMatrix, binVec);
				double distance = Math.sqrt(Algebra.DEFAULT.mult(binVec, firstResult));
//				System.out.println("Distance: "+distance);
			    synchronized(this) {
					if(distance > maxQuadraticDistance) {
//						System.out.println("MaxQuadratic wird gesetzt!"+maxQuadraticDistance);
						maxQuadraticDistance = distance;
					}
			    }
			}
		}
	}
	
	public float calculateWeightedChiDistance(Shot toCompare) {
		float max = 0;
		for (int i = 0; i < keyFrameLst.size(); i++) {
			for (int j = 0; j < toCompare.getKeyFrameLst().size(); j++) {
				float[] histo1 = keyFrameLst.get(i).getColorHistogram();
				float[] histo2 = toCompare.getKeyFrameLst().get(j).getColorHistogram();
				float sum = 0;
				for (int k = 0; k < histo1.length; k++) {
					double function = Math.abs((histo1[k] + histo2[k]) / 2);
					if(function != 0) {
						sum += (Math.pow((histo1[k] - function), 2) / function);
					} else {
						sum += 0;
					}
				}
				sum = 1 - sum;
				if(max < sum) {
					max = sum;
				}
					
			}
		}
		return max;
	}

	public float calculateComplexitySimilarity(Shot toCompare) {
		float complexity = 0;
		for (int i = 0; i < keyFrameLst.size(); i++) {
			for (int j = 0; j < toCompare.getKeyFrameLst().size(); j++) {
				float similarity = calculateComplexitySimilarityBetweenFrames(
						keyFrameLst.get(i), toCompare.getKeyFrameLst().get(j));
				if (similarity > complexity) {
					complexity = similarity;
				}
			}
		}
		return complexity;
	}

	public float calculateLuminanceProjectionSimilarity(Shot toCompare) {
		float max = 0;
		for (int i = 0; i < keyFrameLst.size(); i++) {
			for (int j = 0; j < toCompare.getKeyFrameLst().size(); j++) {
				Keyframe key1 = keyFrameLst.get(i);
				Keyframe key2 = toCompare.getKeyFrameLst().get(j);
				int width = key1.getImage().getWidth();
				int height = key1.getImage().getHeight();
				float sub1 = 0;
				// Calculate sum of absolut differences
				for (int k = 0; k < height; k++) {
					sub1 += Math.abs(key1.getLuminanceProjection()[k]
							- key2.getLuminanceProjection()[k]);
//					System.out.println("Difference: "
//							+ (Math.abs(key1.getLuminanceProjection()[k]
//									- key2.getLuminanceProjection()[k])));
				}
				float sub2 = 0;
				for (int k = 0; k < width; k++) {
					sub2 += Math.abs(key1.getLuminanceProjection()[height + k]
							- key2.getLuminanceProjection()[height + k]);
				}

				float sub = Math.max(sub1, sub2);

				float distance = 1f - (sub / (255f * ((float) (width * height))));
				if (max < distance) {
					max = distance;
				}
			}
		}
		// We take the smallest value as best value! Maximum similarity is 3;
		return (3f - max);
	}

	public float calculateShotweaveComplexity() {
		return 0;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////

	private float calculateComplexitySimilarityBetweenFrames(Keyframe frame1,
			Keyframe frame2) {
		// Strong Edges
		float minComplexity = Math.min(frame1.getComplexity()[0],
				frame2.getComplexity()[0]);
		float maxComplexity = Math.max(frame1.getComplexity()[0],
				frame2.getComplexity()[0]);
		float valStrong = (minComplexity / maxComplexity) * 3;

		// Mid Edges
		minComplexity = Math.min(frame1.getComplexity()[1],
				frame2.getComplexity()[1]);
		maxComplexity = Math.max(frame1.getComplexity()[1],
				frame2.getComplexity()[1]);
		float valMid = (minComplexity / maxComplexity) * 2;

		// Small Edges
		minComplexity = Math.min(frame1.getComplexity()[2],
				frame2.getComplexity()[2]);
		maxComplexity = Math.max(frame1.getComplexity()[2],
				frame2.getComplexity()[2]);
		float valSmall = (minComplexity / maxComplexity);

		return (valStrong + valMid + valSmall) / 6;
	}

	private float colorIntersection(float[] histogram1, float[] histogram2) {
		float sum = 0;
		for (int j = 0; j < histogram1.length; j++) {
//			System.out.println("J: "+j+"Histogram1"+histogram1[j]+"Histogram2: "+histogram2[j]);
			sum += Math.min(histogram1[j], histogram2[j]);
		}
//		System.out.println("sum"+(sum * 3));
		return sum * 3;
	}

	public String getStartTime() {
		String hours, min, sec, hun;
		String date = "";
		int h = timeStart.getStunden();
		hours = h < 10 ? "0" + h : "" + h;
		int m = timeStart.getMinuten();
		min = m < 10 ? "0" + m : "" + m;
		int s = timeStart.getSekunden();
		sec = s < 10 ? "0" + s : "" + s;
		int hund = timeStart.getHundertstelSekunden();
		hun = hund < 10 ? "0" + hund : "" + hund;
		date = timeStart.getYear() + "-" + timeStart.getMonth() + "-"
				+ timeStart.getDays();
		return date + "T" + hours + ":" + min + ":" + sec + ":" + hun
				+ "0F1000" + "";
	}

	public String getEndTime() {
		String hours, min, sec, hun;
		String date = "";
		int h = timeEnd.getStunden();
		hours = h < 10 ? "0" + h : "" + h;
		int m = timeEnd.getMinuten();
		min = m < 10 ? "0" + m : "" + m;
		int s = timeEnd.getSekunden();
		sec = s < 10 ? "0" + s : "" + s;
		int hund = timeEnd.getHundertstelSekunden();
		hun = hund < 10 ? "0" + hund : "" + hund;
		date = timeEnd.getYear() + "-" + timeEnd.getMonth() + "-"
				+ timeEnd.getDays();
		return date + "T" + hours + ":" + min + ":" + sec + ":" + hun
				+ "0F1000" + "";
	}

	public static DoubleMatrix2D calculateColorSimilarityMatrix() {
		double[][] colorSimilarityDoubleMatrix = new double[216][216];
//		double minsim = Double.MAX_VALUE;
		for (int i = 0; i < colorSimilarityDoubleMatrix.length; i++) {
			for (int j = 0; j < colorSimilarityDoubleMatrix.length; j++) {
				int[] rgb1 = transformMatrixPosition(i);
				int[] rgb2 = transformMatrixPosition(j);
				colorSimilarityDoubleMatrix[i][j] = MiscOperations.getColorSimilarity(rgb1[0], rgb2[0], rgb1[1], rgb2[1], rgb1[2], rgb2[2]);
//				if(colorSimilarityDoubleMatrix[i][j] < minsim) {
//					minsim = colorSimilarityDoubleMatrix[i][j];
//					System.out.println("Sim: "+colorSimilarityDoubleMatrix[i][j]+"bei RGB VGL: RGB1 "+ rgb1[0]+" "+rgb1[1]+" "+rgb1[2]+" "+rgb2[0]+" "+rgb2[1]+" "+rgb2[2]);
//				}
//				System.out.println("Sim: "+colorSimilarityDoubleMatrix[i][j]+"bei RGB VGL: RGB1 "+ rgb1[0]+" "+rgb1[1]+" "+rgb1[2]+" "+rgb2[0]+" "+rgb2[1]+" "+rgb2[2]);
			}
		}
		return new DenseDoubleMatrix2D(colorSimilarityDoubleMatrix);
	}
	
	public static void main(String[] args) {
		Shot.calculateColorSimilarityMatrix();
	}
	
	private static int[] transformMatrixPosition(int pos) {
		int[] rgb = new int[3];
		int firstVal = (int) Math.floor(((float) pos) / 36f);
		rgb[0] = firstVal * 6;
		
		int temp = pos - firstVal * 36;
		int secondVal = (int) Math.floor(((float)temp) / 6);
		rgb[1] = secondVal * 6;
		
		rgb[2] = (pos - firstVal * 36 - secondVal * 6) * 6;
		return rgb;
	}

}
