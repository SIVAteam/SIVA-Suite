package org.iviPro.utils;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.iviPro.theme.Icons;

public class ImageHelper {	

	static BufferedImage convertToAWT(ImageData data) {
		ColorModel colorModel = null;
		PaletteData palette = data.palette;
		if (palette.isDirect) {
			colorModel = new DirectColorModel(data.depth, palette.redMask,
					palette.greenMask, palette.blueMask);
			BufferedImage bufferedImage = new BufferedImage(colorModel,
					colorModel.createCompatibleWritableRaster(data.width,
							data.height), false, null);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[3];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					RGB rgb = palette.getRGB(pixel);
					pixelArray[0] = rgb.red;
					pixelArray[1] = rgb.green;
					pixelArray[2] = rgb.blue;
					raster.setPixels(x, y, 1, 1, pixelArray);
				}
			}
			return bufferedImage;
		} else {
			RGB[] rgbs = palette.getRGBs();
			byte[] red = new byte[rgbs.length];
			byte[] green = new byte[rgbs.length];
			byte[] blue = new byte[rgbs.length];
			for (int i = 0; i < rgbs.length; i++) {
				RGB rgb = rgbs[i];
				red[i] = (byte) rgb.red;
				green[i] = (byte) rgb.green;
				blue[i] = (byte) rgb.blue;
			}
			if (data.transparentPixel != -1) {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red,
						green, blue, data.transparentPixel);
			} else {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red,
						green, blue);
			}
			BufferedImage bufferedImage = new BufferedImage(colorModel,
					colorModel.createCompatibleWritableRaster(data.width,
							data.height), false, null);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					pixelArray[0] = pixel;
					raster.setPixel(x, y, pixelArray);
				}
			}
			return bufferedImage;
		}
	}

	/**
	 * Konvertiert ein AWT BufferedImage in ein SWT ImageData Objekt
	 * 
	 * @param bufferedImage
	 *            Das zu konvertierende Bild
	 * @return Das SWT ImageData Objekt fuer das Bild
	 */
	public static ImageData convertToSWT(BufferedImage bufferedImage) {

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
			return data;		
		} else if (bufferedImage.getColorModel() instanceof ComponentColorModel) {
			ComponentColorModel colorModel = (ComponentColorModel) bufferedImage
					.getColorModel();

			PaletteData palette = new PaletteData(0x0000FF, 0x00FF00, 0xFF0000);
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);

			data.transparentPixel = -1;

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
			return data;
		}
		return null;
	}

	/*
	 * gibt das SWT-Image eines BufferedImage zur¸ck
	 */
	public static Image getSWTImage(BufferedImage img) {
		ImageData imgData = convertToSWT(img);
		return new Image(Display.getCurrent(), imgData);
	}

	/*
	 * setzt das Image eines Buttons
	 */
	public static void setButtonImage(Button but, Icons icon) {
		// Bild f¸r den Button
		Image butImg = icon.getImage();
		but.setImage(butImg);
	}

	/**
	 * Resized ein SWT Image auf eine bestimmte Grˆﬂe
	 * 
	 * @param image Das SWT-Image
	 * @param width Die gew¸nschte Breite
	 * @param height Die gew¸nschte Hˆhe
	 * @return Das ge‰nderte SWT-Image
	 */
	public static Image resize(Image image, int width, int height) {
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0,
				image.getBounds().width, image.getBounds().height,
				0, 0, width, height);
		gc.dispose();
		image.dispose(); // don't forget about me!
		return scaled;
	}

	public static Image transition(BufferedImage img1, BufferedImage img2, int width, Color color) {
		Image result = null;

		try {
			//Falls die gew¸nschte Outputbreite zu groﬂ ist, passe diese an die Bildbreiten an
			if(width > img1.getWidth() + img2.getWidth()) {
				width = img1.getWidth() + img2.getWidth() - 10;
			}

			int zzLineColor = convertRGBtoHex(color);

			BufferedImage output = new BufferedImage(width,img1.getHeight(),BufferedImage.TYPE_INT_RGB);
			int outputWidth = output.getWidth();
			int outputHeight = output.getHeight();
			//Startpunkt des 2ten Bild bezogen auf den X-Wert des Output-Bild
			int img2_xStart = output.getWidth() - img2.getWidth();

			//Einstellungen
			//Zickzack-Linie (Startpunkt, Breite, etc.)
			int zzLine = Math.round(outputWidth/2) - 7;
			int zzLineWidth = 2;
			//wenn Bild sehr klein => Trennlinie schmaler
			if(outputHeight < 35) {
				zzLineWidth = 1;
			}
			int xTurningPoint = Math.round(outputHeight / 9);
			int xcount = 0;
			boolean rightDirection = true;

			for(int i=0; i < outputHeight; i++) {
				for(int j=0; j< outputWidth; j++) {
					if(j < zzLine) {
						//zeichne bild1
						output.setRGB(j, i, img1.getRGB(j, i));
					} else if(j > zzLine + zzLineWidth) {
						//zeichne bild2
						output.setRGB(j, i, img2.getRGB(j - img2_xStart, i));
					} else {
						output.setRGB(j,i, zzLineColor);
					}
				}

				//Zeichne die weiﬂe Linie in Zickzack
				if(xcount < xTurningPoint) {
					xcount++;
					if(rightDirection) {
						zzLine++;
					} else {
						zzLine--;
					}
				} else if(xcount == xTurningPoint) {
					xcount++;
				} else if(xcount > xTurningPoint) {
					xcount = 1;
					rightDirection = !rightDirection;
					if(rightDirection) {
						zzLine++;
					} else {
						zzLine--;
					}
				}
			}
			result = getSWTImage(output);

		}catch (Exception e) {
			return null;
		}
		return result;
	}


	/** 
	 * Convert a RGB swt.graphics.color object to a hex-integer value
	 * 
	 * @param c Color object
	 * @return hex-integer value
	 */
	public static int convertRGBtoHex(Color c) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("0x"); //$NON-NLS-1$

		//RED
		if( Integer.toHexString( c.getRed() ).length() == 1  ) {
			buffer.append( "0" ); //$NON-NLS-1$
		}
		buffer.append( Integer.toHexString( c.getRed() ) );

		//GREEN
		if( Integer.toHexString( c.getGreen() ).length() == 1  ) {
			buffer.append( "0" ); //$NON-NLS-1$
		}
		buffer.append( Integer.toHexString( c.getGreen() ) );

		//BLUE
		if( Integer.toHexString( c.getBlue() ).length() == 1  ) {
			buffer.append( "0" ); //$NON-NLS-1$
		}
		buffer.append( Integer.toHexString( c.getBlue() ) );

		return Integer.decode(buffer.toString());
	}

	/**
	 * Resized ein Bild so dass es in eine vorgegebene Breite/Hoehe passt.
	 * 
	 * @param srcImg
	 *            Das zu skalierende Bild als BufferedImage
	 * 
	 * @param boundingBox
	 *            Die Bounding-Box in die das Bild skaliert werden soll
	 * @param preserveAspectRatio
	 *            Gibt an, ob das Seitenverhaeltnis des srcImg beibehalten
	 *            werden soll oder ob es so gestreckt werden soll, dass es die
	 *            gesamte Breite/Hoehe der BoundingBox ausfuellt.
	 * @return Das geresizte Bild als BufferedImage.
	 */
	public static BufferedImage getScaledImage(BufferedImage srcImg,
			Dimension boundingBox, boolean preserveAspectRatio) {

		// Wenn keine BoundingBox bekannt ist, geben wir einfach
		// das QUell-Bild zurueck
		if (boundingBox == null) {
			return srcImg;
		}

		Dimension srcSize = new Dimension(srcImg.getWidth(null), srcImg
				.getHeight(null));

		// Setze die Groesse so, dass es komplett in die
		// geforderte BoundingBox passt.
		Dimension newSize = new Dimension(boundingBox.width, boundingBox.height);
		if (preserveAspectRatio) {
			// Wenn die aspect ratio beibehalten werden soll, dann
			// skalieren wir die groesse des Bildes so, dass es genau in
			// die BoundingBox passt und das Seitenverhaeltnis dabei
			// gleich bleibt.
			float aspectRatio = (srcSize.width * 1.0f)
					/ (srcSize.height * 1.0f);
			float bbRatio = (boundingBox.width * 1.0f)
					/ (boundingBox.height * 1.0f);
			if (aspectRatio > bbRatio) {
				newSize.height = (int) (newSize.width / aspectRatio);
			} else if (bbRatio > aspectRatio) {
				newSize.width = (int) (newSize.height * aspectRatio);
			}

		}

		// Resize das Bild entsprechend der oben berechneten Groesse.
		BufferedImage resizedImg = new BufferedImage(newSize.width,
				newSize.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, newSize.width, newSize.height, null);
		g2.dispose();
		return resizedImg;

	}

	/**
	 * Resized ein Bild so dass es in eine vorgegebene Breite/Hoehe passt.
	 * 
	 * @param srcImg
	 *            Das zu skalierende Bild als SWT-Image
	 * 
	 * @param boundingBox
	 *            Die Bounding-Box in die das Bild skaliert werden soll
	 * @param preserveAspectRatio
	 *            Gibt an, ob das Seitenverhaeltnis des srcImg beibehalten
	 *            werden soll oder ob es so gestreckt werden soll, dass es die
	 *            gesamte Breite/Hoehe der BoundingBox ausfuellt.
	 * @return Das geresizte Bild als SWT-Image.
	 */
	public static Image getScaledImage(Image srcImg,
			Dimension boundingBox, boolean preserveAspectRatio) {

		// Wenn keine BoundingBox bekannt ist, geben wir einfach
		// das QUell-Bild zurueck
		if (boundingBox == null) {
			return srcImg;
		}

		Dimension srcSize = new Dimension(srcImg.getBounds().width, srcImg.getBounds().height);

		// Setze die Groesse so, dass es komplett in die
		// geforderte BoundingBox passt.
		Dimension newSize = new Dimension(boundingBox.width, boundingBox.height);
		if (preserveAspectRatio) {
			// Wenn die aspect ratio beibehalten werden soll, dann
			// skalieren wir die groesse des Bildes so, dass es genau in
			// die BoundingBox passt und das Seitenverhaeltnis dabei
			// gleich bleibt.
			float aspectRatio = (srcSize.width * 1.0f)
					/ (srcSize.height * 1.0f);
			float bbRatio = (boundingBox.width * 1.0f)
					/ (boundingBox.height * 1.0f);
			if (aspectRatio > bbRatio) {
				newSize.height = (int) (newSize.width / aspectRatio);
			} else if (bbRatio > aspectRatio) {
				newSize.width = (int) (newSize.height * aspectRatio);
			}

		}

		// Resize das Bild entsprechend der oben berechneten Groesse.
		Image resizedImg = resize(srcImg, newSize.width, newSize.height);
		return resizedImg;

	}
}
