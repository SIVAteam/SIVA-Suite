package org.iviPro.editors.scenegraph.figures;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Font;

public abstract class IFigureNode extends Figure {

	private static final TextUtilities TEXT_UTIL = new TextUtilities();

	/**
	 * Gibt einen Text zurueck, der in der angegebenen Schriftart in den
	 * gegebenen Platz hinein passt. Ist der Text zu lang, wird in der Mitte
	 * oder am Ende entsprechend viel Text durch ... ersetzt.
	 * 
	 * @param text Inputtext
	 * @param font Schriftfont
	 * @param maxWidth Maximale Breite
	 * @param middle true, falls der Text in der Mitte gekürzt werden soll; false, falls am Ende
	 * @return
	 */
	protected String getTruncatedText(String text, Font font, int maxWidth, boolean middle) {

		String truncatedText = text;

		if(middle) {
			int textMiddle = truncatedText.length() / 2;
			int numOfTruncatedChars = 0;
			while (TEXT_UTIL.getStringExtents(truncatedText, font).width > maxWidth) {
				numOfTruncatedChars += 2;
				int cutStart = textMiddle - numOfTruncatedChars / 2;
				int cutEnd = cutStart + numOfTruncatedChars;
				truncatedText = text.substring(0, cutStart) + "..." //$NON-NLS-1$
						+ text.substring(cutEnd);
			}
		} else {
			int cuttingPoint;
			boolean cutted = false;
			while (TEXT_UTIL.getStringExtents(truncatedText, font).width > maxWidth) {
				if(cutted) {
					cuttingPoint = truncatedText.length()-4;
				} else {
					cuttingPoint = truncatedText.length()-1;
				}
				truncatedText = truncatedText.substring(0, cuttingPoint);
				truncatedText = truncatedText + "..."; //$NON-NLS-1$
				cutted = true;
			}
		}
		return truncatedText;
	}

	/**
	 * Berechnet, wie gross ein Text unter Benutzung einer bestimmten Schrift
	 * gerendert wird.
	 * 
	 * @param text
	 *            Der Text.
	 * @param font
	 *            Die Schrift.
	 * @return Groesse des Textes unter Verwendung der gegebenen Schrift.
	 */
	protected Dimension getStringSize(String text, Font font) {
		return TEXT_UTIL.getStringExtents(text, font);
	}
	
	/**
	 * Returns the height of the largest letter in the font. 
	 * @param font font
	 * @return height of the largest letter of the font
	 */
	protected int getFontHeight(Font font) {
		return TEXT_UTIL.getAscent(font);
	}

	/**
	 * Verschiebt alle Punkte in dem point-Array um transformX in horizontaler
	 * Richtung.
	 * 
	 * @param translateX
	 *            Anzahl der Pixel um die alle Punkte im angegebenen Array
	 *            horizontal verschoben werden sollen.
	 * @param points
	 *            Array mit den Punkten. Jeweils abwechselnd x- und
	 *            y-Koordinaten.
	 */
	protected void translateHorizontally(int translateX, int[] points) {
		for (int i = 0; i < points.length; i = i + 2) {
			points[i] += translateX;
		}
	}

	/**
	 * Zentriert die gegebenen Punkte horizontal in der Figure.
	 * 
	 * @param points
	 *            Array mit den Punkten. Jeweils abwechselnd x- und
	 *            y-Koordinaten.
	 */
	protected void centerHorizontally(int[] points) {
		// Minimalen und maximalen X-Wert im Array feststellen
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < points.length; i = i + 2) {
			min = Math.min(min, points[i]);
			max = Math.max(max, points[i]);
		}

		int pointsWidth = max - min;
		int figureWidth = getBounds().width;

		int translateX = (figureWidth - pointsWidth) / 2 - (min - getBounds().x);
		translateHorizontally(translateX, points);
	}

	protected void centerHorizontally(Rectangle rect) {
		int[] points = new int[] { rect.x, rect.y, rect.x + rect.width,
				rect.y + rect.height };
		centerHorizontally(points);
		rect.x = points[0];
		rect.y = points[1];
		rect.width = points[2] - points[0];
		rect.height = points[3] - points[1];
	}
}
