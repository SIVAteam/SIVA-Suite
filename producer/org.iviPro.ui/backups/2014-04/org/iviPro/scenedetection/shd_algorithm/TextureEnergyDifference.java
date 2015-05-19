package org.iviPro.scenedetection.shd_algorithm;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;

/**
 * Berechnet zwischen 2 Frames den Texturunterschied mit Hilfe einer Grey Level Co-Occurence Matrix
 *
 * @author Stefan Zwicklbauer
 */
public class TextureEnergyDifference extends ThreeFeatures {

    public static final int amountGrayValues = 16;
    public static final int amountDegreeCheck = 4;
    private Raster greyRaster;
    private float energyLastFrame;

    public TextureEnergyDifference(int width, int height) {
        super(width, height);
    }

    @Override
    public void initializeFirstFrameValues() {
        energyLastFrame = calculateCurrentEnergy();
    }

    @Override
    public float getPeakValue() {
        float energyCurrentFrame = calculateCurrentEnergy();
        float threshold = (float) 0.25 * Math.abs(energyCurrentFrame - energyLastFrame);
        energyLastFrame = energyCurrentFrame;
        return (int) Math.pow(threshold * 100000, 0.80);
    }

    private float[] amountOccurences(int[][][] cooccurenceMatrix) {
        float[] occurences = new float[amountDegreeCheck];

        for (int i = 0; i < amountGrayValues; i++) {
            for (int j = 0; j < amountGrayValues; j++) {
                for (int j2 = 0; j2 < amountDegreeCheck; j2++) {
                    occurences[j2] += cooccurenceMatrix[j2][i][j];
                }
            }
        }
        return occurences;
    }

    private float[] calculateEnergy(int[][][] cooccurenceMatrix,
            float[] occurences) {
        float[] energy = new float[amountDegreeCheck];
        for (int i = 0; i < amountGrayValues; i++) {
            for (int j = 0; j < amountGrayValues; j++) {
                for (int j2 = 0; j2 < amountDegreeCheck; j2++) {
                    energy[j2] += Math.pow(
                            (cooccurenceMatrix[j2][i][j] / occurences[j2]),
                            2);
                }
            }
        }
        return energy;
    }

    private float calculateCurrentEnergy() {
        int[][] valuesCurrentFrame = new int[height][width];

        int[][][] cooccurencematrix = new int[amountDegreeCheck][amountGrayValues][amountGrayValues];

        BufferedImage gray = new BufferedImage(width, height,
                BufferedImage.TYPE_BYTE_GRAY);
        ColorConvertOp grayScaleConversionOp = new ColorConvertOp(
                ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        grayScaleConversionOp.filter((BufferedImage) image, gray);
        greyRaster = gray.getRaster();

        for (int i = 0; i < height; i++) {
            greyRaster.getSamples(0, i, width, 1, 0, valuesCurrentFrame[i]);
        }

        for (int i = 0; i < valuesCurrentFrame.length; i++) {
            for (int j = 0; j < width; j++) {
                valuesCurrentFrame[i][j] = (int) (valuesCurrentFrame[i][j] / amountGrayValues);
            }
        }

        for (int i = 1; i < valuesCurrentFrame.length; i++) {
            for (int j = 1; j < width - 1; j++) {
                cooccurencematrix[0][valuesCurrentFrame[i][j]][valuesCurrentFrame[i][j + 1]]++;
                cooccurencematrix[1][valuesCurrentFrame[i][j]][valuesCurrentFrame[i - 1][j + 1]]++;
                cooccurencematrix[2][valuesCurrentFrame[i][j]][valuesCurrentFrame[i - 1][j]]++;
                cooccurencematrix[3][valuesCurrentFrame[i][j]][valuesCurrentFrame[i - 1][j - 1]]++;
            }
        }

        // Obere Reihe ohne den letzten Pixel!
        for (int i = 0; i < width - 1; i++) {
            cooccurencematrix[0][valuesCurrentFrame[0][i]][valuesCurrentFrame[0][i + 1]]++;
        }

        // Linke Reihe ohne den obersten Pixel
        for (int i = 1; i < valuesCurrentFrame.length; i++) {
            cooccurencematrix[0][valuesCurrentFrame[i][0]][valuesCurrentFrame[i][1]]++;
            cooccurencematrix[1][valuesCurrentFrame[i][0]][valuesCurrentFrame[i - 1][1]]++;
            cooccurencematrix[2][valuesCurrentFrame[i][0]][valuesCurrentFrame[i - 1][0]]++;
        }

        // Rechte Reihe ohne den obersten Pixel
        for (int i = 1; i < valuesCurrentFrame.length; i++) {
            cooccurencematrix[2][valuesCurrentFrame[i][width - 1]][valuesCurrentFrame[i - 1][width - 1]]++;
            cooccurencematrix[3][valuesCurrentFrame[i][width - 1]][valuesCurrentFrame[i - 1][width - 2]]++;
        }
        // Oberster Pixel auf der linken seite!
        cooccurencematrix[0][valuesCurrentFrame[0][0]][valuesCurrentFrame[0][1]]++;

        float[] amountOccurences = amountOccurences(cooccurencematrix);
        float[] energy = calculateEnergy(cooccurencematrix,
                amountOccurences);
        float energyTogether = 0;
        for (int i = 0; i < energy.length; i++) {
            energyTogether += energy[i];
        }

        return energyTogether;
    }
}
