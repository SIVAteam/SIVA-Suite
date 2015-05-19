package org.iviPro.views.informationview;

import org.apache.log4j.Logger;

/**
 * Diese Klasse beschreibt die Objekte für die View Information. Die Informationen
 * die angezeigt werden sollen bestehen dabei immer aus einer Beschreibung der 
 * Information und dem tatsächlichen Wert.
 * 
 * @author Florian Stegmaier
 */
public class Information {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(Information.class);
	private String infoDescription;
	private String infoText;
	
	/**
	 * Konstruktor
	 * 
	 * @param desc Beschreibung der Information
	 * @param text Wert der Information
	 */
	public Information(String desc, String text) {
		this.infoDescription = desc;
		this.infoText = text;
	}
	
	/**
	 * Liefert die Beschreibung der Information.
	 * 
	 * @return
	 */
	public String getInfoDesc() {
		return this.infoDescription;
	}
	
	/**
	 * Liefert den tatsäclichen Wert der Information.
	 * 
	 * @return
	 */
	public String getInfoText() {
		return this.infoText;
	}
}
