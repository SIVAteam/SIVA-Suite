package org.iviPro.theme.colorprovider;

import org.eclipse.swt.graphics.Color;
import org.iviPro.theme.Colors;

/**
 * Interface fuer Color-Provider, die der Colors-Klasse die noetige
 * Farbinformation zur Verfuegung stellt.
 * 
 * @author dellwo
 * 
 */
public interface IColorProvider {

	/**
	 * Gibt die SWT-Farbe fuer einen bestimmten Farb-Typ zurueck.
	 * 
	 * @param colorType
	 *            Der Farb-Typ.
	 * @return Die zugehoerige SWT-Farbe.
	 */
	public Color getColor(Colors colorType);

}
