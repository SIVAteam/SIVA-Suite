package org.iviPro.views.informationview;

import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * In dieser Klasse werden alle Informationen zu einem Objekt in einer Liste
 * gespeichert.
 * 
 * @author Florian Stegmaier
 */
public class InformationContainer {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(InformationContainer.class);
	private ArrayList<Information> infoList;
	
	/**
	 * Konstruktor...
	 */
	public InformationContainer() {
		infoList = new ArrayList<Information>();
	}
	
	/**
	 * Speichert eine Information in der Liste.
	 * 
	 * @param info
	 */
	public void addInfoToList(Information info) {
		this.infoList.add(info);
	}
	
	/**
	 * Liefert alle Informationen als Liste zurück.
	 * 
	 * @return
	 */
	public ArrayList<Information> getList() {
		return this.infoList;
	}
	
	/**
	 * Löscht alle Elemente aus der Liste.
	 */
	public void clearList() {
		this.infoList.clear();
	}
}
