package org.iviPro.utils;

import org.eclipse.draw2d.TextUtilities;
import org.eclipse.swt.graphics.Font;

public class TextUtils {
	
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
	public static String getTruncatedText(String text, Font font, int maxWidth, boolean middle) {

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
}
