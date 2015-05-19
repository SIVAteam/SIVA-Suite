package org.iviPro.newExport.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.iviPro.model.imageeditor.ICircle;
import org.iviPro.model.imageeditor.IRectangle;
import org.iviPro.model.imageeditor.IText;
import org.iviPro.model.imageeditor.ImageObject;

// TODO: Extract all utilities in a own plug-in!

public class DrawingUtils {

	/**
	 * Draws an Object with a given GC directly on a image
	 * 
	 * @param gc
	 *            GC to be drawn with
	 * @param imgObj
	 *            Object to be drawn
	 * @param image
	 *            Image to be drawn on
	 */
	public static void drawObject(GC gc, ImageObject imgObj, Image image) {
		int imageWidth = image.getBounds().width;
		int imageHeight = image.getBounds().height;
		int x = getHorizontalFtI(imgObj.x, imageWidth);
		int y = getVerticalFtI(imgObj.y, imageHeight);
		drawObject(gc, imgObj, image, new Point(x, y));

	}

	/**
	 * Does the actual drawing
	 */
	private static void drawObject(GC gc, ImageObject imgObj, Image image,
			Point position) {
		int imageWidth = image.getBounds().width;
		int imageHeight = image.getBounds().height;
		int x = position.x;
		int y = position.y;
		int width = getHorizontalFtI(imgObj.width, imageWidth);
		int height = getVerticalFtI(imgObj.height, imageHeight);
		gc.setForeground(new Color(Display.getCurrent(), imgObj.color));
		gc.setLineStyle(SWT.LINE_SOLID);
		// Draw object according to class
		if (imgObj instanceof IRectangle) {
			gc.setLineWidth(getHorizontalFtI(imgObj.linewidth, imageWidth) + 1);
			gc.drawRectangle(x, y, width, height);
		} else if (imgObj instanceof ICircle) {
			gc.setLineWidth(getHorizontalFtI(imgObj.linewidth, imageWidth) + 1);
			gc.drawOval(x, y, width, height);
		} else if (imgObj instanceof IText) {
			IText imgText = (IText) imgObj;
			int style = SWT.NORMAL;
			if (imgText.bold && imgText.italic) {
				style = (SWT.BOLD | SWT.ITALIC);
			} else if (imgText.bold && !imgText.italic) {
				style = SWT.BOLD;
			} else if (!imgText.bold && imgText.italic) {
				style = SWT.ITALIC;
			}
			Font font = new Font(Display.getCurrent(), "Arial", //$NON-NLS-1$
					getHorizontalFtI(imgText.fontsize, imageWidth), style);
			gc.setFont(font);
			Point extent = gc.textExtent(imgText.text);
			width = extent.x;
			height = extent.y;
			gc.drawText(imgText.text, x, y, true);
			gc.setLineWidth(1);
			if (imgText.underline) {
				gc.drawLine(x, y + extent.y - 1, x + extent.x, y + extent.y - 1);
			}
			font.dispose();
		}
	}

	private static int getHorizontalFtI(float x, int imageWidth) {
		return (int) (x * imageWidth);
	}

	private static int getVerticalFtI(float y, int imageHeight) {
		return (int) (y * imageHeight);
	}
}
