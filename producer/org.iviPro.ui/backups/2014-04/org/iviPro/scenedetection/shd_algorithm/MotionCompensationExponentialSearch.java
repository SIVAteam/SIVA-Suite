package org.iviPro.scenedetection.shd_algorithm;

/**
 * Berechnet den Bewegungsunterschied zwischen 2 Bildern.
 *
 * @author Stefan Zwicklbauer
 */
public class MotionCompensationExponentialSearch extends ThreeFeatures {

    static final int SIZEBOX = 16;
    static final int BOUNDINGBOX = 32;
    private float[][] lastYValues;
    private int amountboxesX;
    private int amountboxesY;
    private int amountboxes;

    public MotionCompensationExponentialSearch(int width, int height) {
        super(width, height);
        lastYValues = new float[height][width];
        amountboxesX = (int) Math.floor(width / SIZEBOX);
        amountboxesY = (int) Math.floor(height / SIZEBOX);
        amountboxes = amountboxesX * amountboxesY;
    }

    @Override
    public float getPeakValue() {
        float[][] currentYValues = new float[height][width];
        for (int j = 0; j < height; j++) {
            for (int j2 = 0; j2 < width; j2++) {
                int arrayindex = j * width + j2;
                currentYValues[j][j2] = ShotDetectionSettings.Y_VALUE_R * currentFrameRGB[0][arrayindex] + ShotDetectionSettings.Y_VALUE_G * currentFrameRGB[1][arrayindex] + ShotDetectionSettings.Y_VALUE_B * currentFrameRGB[2][arrayindex];
            }
        }

        float result = 0;
        for (int i = 0; i < amountboxes; i++) {
            int[] standbox = new int[2];
            standbox[0] = (i % amountboxesX) * SIZEBOX;
            standbox[1] = (int) Math.floor((i / amountboxesX)) * SIZEBOX;

            int[] startSearchPosition = new int[2];
            startSearchPosition[0] = calculateStartPosition(standbox[0]);
            startSearchPosition[1] = calculateStartPosition(standbox[1]);

            int[] moving = new int[2];
            moving[0] = calculateMoveX(standbox[0]);
            moving[1] = calculateMoveY(standbox[1]);

            int[] bestMatching = new int[2];

            int min = Integer.MAX_VALUE;
            for (int j = 0; j < moving[1]; j++) {
                for (int j2 = 0; j2 < moving[0]; j2++) {
                    int sum = 0;
                    for (int k = 0; k < SIZEBOX; k++) {
                        for (int k2 = 0; k2 < SIZEBOX; k2++) {
                            sum += Math.abs(currentYValues[standbox[1] + k][standbox[0] + k2] - lastYValues[startSearchPosition[1] + j + k][startSearchPosition[0] + j2 + k2]);
                        }
                    }
                    if (sum < min) {
                        min = sum;
                        bestMatching[0] = startSearchPosition[0] + j2;
                        bestMatching[1] = startSearchPosition[1] + j;
                    }
                }
            }

            float boxYvalues = 0;
            for (int j = 0; j < SIZEBOX; j++) {
                for (int j2 = 0; j2 < SIZEBOX; j2++) {
                    boxYvalues += currentYValues[standbox[1] + j][standbox[0] + j2];
                }
            }

            float movedBoxValues = 0;
            for (int j = 0; j < SIZEBOX; j++) {
                for (int j2 = 0; j2 < SIZEBOX; j2++) {
                    movedBoxValues += lastYValues[bestMatching[1] + j][bestMatching[0] + j2];
                }
            }
            boxYvalues = boxYvalues / (SIZEBOX * SIZEBOX);
            movedBoxValues = movedBoxValues / (SIZEBOX * SIZEBOX);
            result += Math.abs(boxYvalues - movedBoxValues);
        }
        result = result / amountboxes;
        lastYValues = currentYValues;
        return (int) Math.pow(result * 2000, 1);
    }

    @Override
    public void initializeFirstFrameValues() {
        for (int j = 0; j < height; j++) {
            for (int j2 = 0; j2 < width; j2++) {
                int arrayindex = j * width + j2;
                lastYValues[j][j2] = ShotDetectionSettings.Y_VALUE_R * currentFrameRGB[0][arrayindex] + ShotDetectionSettings.Y_VALUE_G * currentFrameRGB[1][arrayindex] + ShotDetectionSettings.Y_VALUE_B * currentFrameRGB[2][arrayindex];
            }
        }
    }

    private int calculateStartPosition(int standBox) {
        if (standBox - ((BOUNDINGBOX - SIZEBOX) / 2) < 0) {
            return 0;
        }
        return (standBox - ((BOUNDINGBOX - SIZEBOX) / 2));
    }

    private int calculateMoveX(int standBox) {
        int mover = BOUNDINGBOX - SIZEBOX;
        if (standBox - ((BOUNDINGBOX - SIZEBOX) / 2) < 0) {
            mover = BOUNDINGBOX - SIZEBOX - (Math.abs((standBox - ((BOUNDINGBOX - SIZEBOX) / 2))));
        } else if (standBox + SIZEBOX + ((BOUNDINGBOX - SIZEBOX) / 2) > width) {
            mover = BOUNDINGBOX - SIZEBOX - ((standBox + SIZEBOX + ((BOUNDINGBOX - SIZEBOX) / 2)) - width);
        }
        return mover;
    }

    private int calculateMoveY(int standBox) {
        int mover = BOUNDINGBOX - SIZEBOX;
        if (standBox - ((BOUNDINGBOX - SIZEBOX) / 2) < 0) {
            mover = BOUNDINGBOX - SIZEBOX - (Math.abs((standBox - ((BOUNDINGBOX - SIZEBOX) / 2))));
        } else if (standBox + SIZEBOX + ((BOUNDINGBOX - SIZEBOX) / 2) > height) {
            mover = BOUNDINGBOX - SIZEBOX - ((standBox + SIZEBOX + ((BOUNDINGBOX - SIZEBOX) / 2)) - height);
        }
        return mover;
    }
}
