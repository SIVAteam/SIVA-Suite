package org.iviPro.scenedetection.sd_main;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.iviPro.scenedetection.shd_algorithm.CannyEdgeDetector;

/**
 * Class offers some functions to extract global movie information like black
 * stripes at the upper/lower bound.
 * 
 */
public class MiscOperations {

	/* Threshold value to determine stripes */
	static final int AVERAGELUMINANCETHRESHOLD = 2;

	/* Bin amount of HSV color histogram */
	static final int HSVHISTOGRAMBINS = 6;

	/* Bin amount of RGB color histogram */
	static final int RGBHISTOGRAMBINS = 6;

	/* Bin amount of grey color histogram */
	static final int GREYHISTOGRAMBINS = 40;

	/* Bin amount of edge histogram */
	static final int EDGEHISTOGRAMBINS = 8;

	/* Black value of a pixel in edge picture */
	static final int MAXEDGEVALUE = -16777216;

	static final int STRONGEDGETHRESHOLD = -7000000;

	static final int MIDDLEEDGETHRESHOLD = -12500000;

	static final int LEIGHTEDGETHRESHOLD = -16000000;

	/**
	 * All methods are static. Constructor is not available
	 */
	private MiscOperations() {
	}

	/**
	 * Function to detect upper and lower blackstripes of arbitrary movieparts
	 * 
	 * @param images
	 *            the image in sequential ordering
	 * @param blocksize
	 *            macroblocksize. Must be a power of 2
	 * @param lower
	 *            false if upper stripes should be detected, true if lower
	 *            stripes should be detected
	 * @return
	 */
	public static int detectBlackStripes(BufferedImage[] images, int blocksize,
			boolean lower) {
		if (images.length == 0) {
			return 0;
		}
		int width = images[0].getWidth();
		int height = images[1].getHeight();

		int globalRow = 0;
		for (int i = images.length - 1; i >= 0; i--) {
			// Get luminance values
			BufferedImage gray = new BufferedImage(width, height,
					BufferedImage.TYPE_BYTE_GRAY);
			ColorConvertOp grayScaleConversionOp = new ColorConvertOp(
					ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
			grayScaleConversionOp.filter((BufferedImage) images[i], gray);
			Raster raster = gray.getRaster();

			if (!lower) {
				// Upper Stripes
				int row = calcUpperStripes(width, height, blocksize, raster);
				if (i == images.length - 1) {
					globalRow = row;
				} else {
					if (globalRow != row) {
						return 0;
					}
				}
			} else {
				// Lower Strippes
				int row = calcLowerStripes(width, height, blocksize, raster);
				if (i == images.length - 1) {
					globalRow = row;
				} else {
					if (globalRow != row) {
						return 0;
					}
				}
			}
		}
		return globalRow;
	}

	/**
	 * Iterates over the image and gets upper stripes. Those are detected with
	 * average luminance.
	 * 
	 * @param width
	 *            width of image
	 * @param height
	 *            height of image
	 * @param blocksize
	 *            size of macroblock
	 * @param raster
	 *            luminance raster of the operating image
	 * @return the amount of macroblock rows which has averageluminance <
	 *         AVERAGELUMINANCETHRESHOLD
	 */
	private static int calcUpperStripes(int width, int height, int blocksize,
			Raster raster) {
		boolean work = true;
		int row = 0;
		while (work) {
			// Get ArrayOutOfBoundsException
			int luminance[] = new int[(row + 1) * width * blocksize];
			try {
				raster.getSamples(0, 0, width, (row + 1) * blocksize, 0,
						luminance);
			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}
			// Get row luminance
			raster.getSamples(0, 0, width, (row + 1) * blocksize, 0, luminance);

			//
			int average = 0;
			for (int j = 0; j < luminance.length; j++) {
				average += luminance[j];
			}
			average = average / luminance.length;
			if (average < AVERAGELUMINANCETHRESHOLD) {
				row++;
			} else {
				work = false;
			}
		}
		return row;
	}

	/**
	 * Iterates over the image and gets lower stripes. Those are detected with
	 * average luminance.
	 * 
	 * @param width
	 *            width of image
	 * @param height
	 *            height of image
	 * @param blocksize
	 *            size of macroblock
	 * @param raster
	 *            luminance raster of the operating image
	 * @return the amount of macroblock rows which has averageluminance <
	 *         AVERAGELUMINANCETHRESHOLD
	 */
	private static int calcLowerStripes(int width, int height, int blocksize,
			Raster raster) {
		boolean work = true;
		int row = 0;
		while (work) {
			// Get ArrayOutOfBoundsException
			int luminance[] = new int[(row + 1) * width * blocksize];
			try {
				raster.getSamples(0, height - (row + 1) * blocksize, width,
						(row + 1) * blocksize, 0, luminance);
			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}
			// Get row luminance
			// int luminance[] = new int[(row + 1) * width * blocksize];
			raster.getSamples(0, height - (row + 1) * blocksize, width,
					(row + 1) * blocksize, 0, luminance);

			//
			int average = 0;
			for (int j = 0; j < luminance.length; j++) {
				average += luminance[j];
			}
			average = average / luminance.length;
			if (average < AVERAGELUMINANCETHRESHOLD) {
				row++;
			} else {
				work = false;
			}
		}
		return row;
	}

	/**
	 * RGB to HSV Conversion
	 * 
	 * @param R
	 *            red color value
	 * @param G
	 *            green color value
	 * @param B
	 *            blue color calue
	 * @return returns array with h, s and v colorvalues
	 */
	public static float[] toHSV(int r, int g, int b) {
		float compH = 0;
		float compS = 0;
		float compV = 0;

		float rnew = (float) r / 255f;
		float gnew = (float) g / 255f;
		float bnew = (float) b / 255f;

		float minRGB = Math.min(rnew, Math.min(gnew, bnew));
		float maxRGB = Math.max(rnew, Math.max(gnew, bnew));

		// Black-gray-white
		if (minRGB == maxRGB) {
			compV = minRGB;
			float[] val = { 0f, 0f, compV };
			return val;
		}

		// Colors other than black-gray-white:
		float d = (rnew == minRGB) ? gnew - bnew : ((bnew == minRGB) ? rnew
				- gnew : bnew - rnew);
		float h = (rnew == minRGB) ? 3 : ((bnew == minRGB) ? 1 : 5);
		compH = 60 * (h - d / (maxRGB - minRGB));
		compS = (maxRGB - minRGB) / maxRGB;
		compV = maxRGB;
		float[] vals = { compH, compS, compV };
		return vals;
	}

	/**
	 * RGB to LUV Conversion
	 * 
	 * @param R
	 *            red color value
	 * @param G
	 *            green color value
	 * @param B
	 *            blue color calue
	 * @return returns array with l, u and v colorvalues
	 */
	public static float[] RGBtoLUV(int R, int G, int B) {
		float[] luv = new float[3];
		// http://www.brucelindbloom.com

		float rf, gf, bf;
		float r, g, b, X_, Y_, Z_, X, Y, Z, fx, fy, fz, xr, yr, zr;
		float L;
		float eps = 216.f / 24389.f;
		float k = 24389.f / 27.f;

		float Xr = 0.964221f; // reference white D50
		float Yr = 1.0f;
		float Zr = 0.825211f;

		// RGB to XYZ

		r = R / 255.f; // R 0..1
		g = G / 255.f; // G 0..1
		b = B / 255.f; // B 0..1

		// assuming sRGB (D65)
		if (r <= 0.04045)
			r = r / 12;
		else
			r = (float) Math.pow((r + 0.055) / 1.055, 2.4);

		if (g <= 0.04045)
			g = g / 12;
		else
			g = (float) Math.pow((g + 0.055) / 1.055, 2.4);

		if (b <= 0.04045)
			b = b / 12;
		else
			b = (float) Math.pow((b + 0.055) / 1.055, 2.4);

		X = 0.436052025f * r + 0.385081593f * g + 0.143087414f * b;
		Y = 0.222491598f * r + 0.71688606f * g + 0.060621486f * b;
		Z = 0.013929122f * r + 0.097097002f * g + 0.71418547f * b;

		// XYZ to Luv

		float u, v, u_, v_, ur_, vr_;

		u_ = 4 * X / (X + 15 * Y + 3 * Z);
		v_ = 9 * Y / (X + 15 * Y + 3 * Z);

		ur_ = 4 * Xr / (Xr + 15 * Yr + 3 * Zr);
		vr_ = 9 * Yr / (Xr + 15 * Yr + 3 * Zr);

		yr = Y / Yr;

		if (yr > eps)
			L = (float) (116 * Math.pow(yr, 1 / 3.) - 16);
		else
			L = k * yr;

		u = 13 * L * (u_ - ur_);
		v = 13 * L * (v_ - vr_);

		luv[0] = (int) (2.55 * L + .5);
		luv[1] = (int) (u + .5);
		luv[2] = (int) (v + .5);
		return luv;
	}

	public static double getColorSimilarity(int r1, int r2, int g1, int g2,
			int b1, int b2) {
		float[] luv1 = MiscOperations.RGBtoLUV(r1, g1, b1);
		float[] luv2 = MiscOperations.RGBtoLUV(r2, g2, b2);
		double diff = Math.sqrt(Math.pow((luv1[1] - luv2[1]), 2)
				+ Math.pow((luv1[2] - luv2[2]), 2));
		// System.out.println("DIFFERENZ: "+diff);
		return (1 - (diff / 256));
	}

	public static void main(String[] args) {
		int r1 = 100;
		int g1 = 10;
		int b1 = 100;
		int r2 = 240;
		int g2 = 240;
		int b2 = 240;
		float[] luv1 = MiscOperations.RGBtoLUV(r1, g1, b1);
		float[] luv2 = MiscOperations.RGBtoLUV(r2, g2, b2);
		double diff = Math.pow((luv1[1] - luv2[1]), 2)
				+ Math.pow((luv1[2] - luv2[2]), 2);

		System.out.println("U1: " + luv1[1] + " V1" + luv1[2]);
		System.out.println("U2: " + luv2[1] + " V2" + luv2[2]);

		System.out.println("diff: " + diff + "test"
				+ Math.pow((luv1[2] - luv2[2]), 2));
	}

	public static float[] createHSVHistogram(BufferedImage image) {
		float[] hsvhistogram = new float[HSVHISTOGRAMBINS * HSVHISTOGRAMBINS
				* HSVHISTOGRAMBINS];
		float histogramdistance = 100f / (float) (HSVHISTOGRAMBINS);
		float histogramdistanceAngle = 360f / (float) (HSVHISTOGRAMBINS);
		int amountPixels = image.getHeight() * image.getWidth();
		int red, green, blue;
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
				int pixel = image.getRGB(j, i);
				red = (pixel >> 16) & 0xff;
				green = (pixel >> 8) & 0xff;
				blue = (pixel) & 0xff;
				float[] hsv = MiscOperations.toHSV(red, green, blue);
				int h = (int) (Math.floor(hsv[0] / histogramdistanceAngle));
				int s = (int) (Math.floor(((hsv[1] * 100f) % 100)
						/ histogramdistance));
				int v = (int) (Math.floor(((hsv[2] * 100f) % 100)
						/ histogramdistance));
				hsvhistogram[h * HSVHISTOGRAMBINS * HSVHISTOGRAMBINS + s
						* HSVHISTOGRAMBINS + v]++;
			}
		}

		// normalize histogram
		for (int j = 0; j < hsvhistogram.length; j++) {
			hsvhistogram[j] = hsvhistogram[j] / (float) amountPixels;
		}

		return hsvhistogram;
	}

	public static double[] createRGBHistogram(BufferedImage image) {
		double[] rgbhistogram = new double[RGBHISTOGRAMBINS * RGBHISTOGRAMBINS * RGBHISTOGRAMBINS];
		int red, green, blue;
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
				int pixel = image.getRGB(j, i);
				red = (pixel >> 16) & 0xff;
				green = (pixel >> 8) & 0xff;
				blue = (pixel) & 0xff;
				int r = (int) Math.floor((float) red / (256.f / (float) RGBHISTOGRAMBINS));
				int g = (int) Math.floor((float) green / (256.f / (float) RGBHISTOGRAMBINS));
				int b = (int) Math.floor((float) blue / (256.f / (float) RGBHISTOGRAMBINS));
//				int g = (int) Math.floor(green / RGBHISTOGRAMBINS);
//				int b = (int) Math.floor(blue / RGBHISTOGRAMBINS);
				rgbhistogram[r * RGBHISTOGRAMBINS * RGBHISTOGRAMBINS + g * RGBHISTOGRAMBINS + b]++;
			}
		}
		return rgbhistogram;
	}

	public static int[] setLuminanceProjection(BufferedImage image) {
		int[] luminanceProjection = new int[image.getWidth()
				+ image.getHeight()];
		int[] luminanceValues = new int[image.getWidth() * image.getHeight()];
		BufferedImage gray = new BufferedImage(image.getWidth(),
				image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		ColorConvertOp grayScaleConversionOp = new ColorConvertOp(
				ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		grayScaleConversionOp.filter(image, gray);
		Raster raster = gray.getRaster();
		raster.getSamples(0, 0, image.getWidth(), image.getHeight(), 0,
				luminanceValues);

		for (int i = 0; i < image.getHeight(); i++) {
			int sum = 0;
			for (int j = 0; j < image.getWidth(); j++) {
				sum += luminanceValues[i * image.getWidth() + j];
			}
			luminanceProjection[i] = sum;
		}

		for (int i = 0; i < image.getWidth(); i++) {
			int sum = 0;
			for (int j = 0; j < image.getHeight(); j++) {
				sum += luminanceValues[j * image.getWidth() + i];
			}
			luminanceProjection[image.getHeight() + i] = sum;
		}
		return luminanceProjection;
	}

	public static float[] setEdgeHistogram(BufferedImage image) {
		float[] edgeHistogram = getAmountEdges(image);
		// float cumulativeValue = calcCumulativeHistoValue(image);
		return edgeHistogram;
	}

	private static float[] getAmountEdges(BufferedImage image) {
		float[] edgeHistogram = new float[3];
		CannyEdgeDetector canny = new CannyEdgeDetector();
		int[] rgbarray = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), rgbarray, 0,
				image.getWidth());
		int[] edgeImage = canny.process(rgbarray, image.getWidth(),
				image.getHeight(), 9, 0.45f, 2, 7, 1, 0);

		for (int i = 0; i < edgeImage.length; i++) {
			if (edgeImage[i] > STRONGEDGETHRESHOLD) {
				edgeHistogram[0]++;
			} else if (edgeImage[i] <= STRONGEDGETHRESHOLD
					&& edgeImage[i] >= MIDDLEEDGETHRESHOLD) {
				edgeHistogram[1]++;
			} else if (edgeImage[i] < MIDDLEEDGETHRESHOLD
					&& edgeImage[i] > LEIGHTEDGETHRESHOLD) {
				edgeHistogram[2]++;
			}
		}
		return edgeHistogram;
	}

	public static ImageData convertToSWT(BufferedImage bufferedImage) {
		// System.out.println("Callup auf jedenfall");
		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel) bufferedImage
					.getColorModel();
			PaletteData palette = new PaletteData(colorModel.getRedMask(),
					colorModel.getGreenMask(), colorModel.getBlueMask());
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[3];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					int pixel = palette.getPixel(new RGB(pixelArray[0],
							pixelArray[1], pixelArray[2]));
					data.setPixel(x, y, pixel);
				}
			}
			// System.out.println("return 1");
			// System.out.println(data.height);
			// System.out.println(data.width);
			return data;
		} else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
			IndexColorModel colorModel = (IndexColorModel) bufferedImage
					.getColorModel();
			int size = colorModel.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);
			RGB[] rgbs = new RGB[size];
			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
						blues[i] & 0xFF);
			}
			PaletteData palette = new PaletteData(rgbs);
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			System.out.println("return 2");
			return data;
		}
		return null;
	}

}
