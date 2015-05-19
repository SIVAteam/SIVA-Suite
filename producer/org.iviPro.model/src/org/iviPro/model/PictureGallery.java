/**
 * 
 */
package org.iviPro.model;

import java.util.ArrayList;
import java.util.List;

import org.iviPro.model.resources.Picture;

/**
 * Kapselt mehrere Bilder und Infos der Bildergalerie
 * @author juhoffma
 */
public class PictureGallery extends IAbstractBean {

	/**
	 * Der Name des "column"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_COLUMNS_PICGALLERY = "columnspicturegallery";
	public static final String PROP_PICGALLERY = "picturegallery";
	
	public static final int PICGAL_COLS_STD = 3;
	
	/**
	 * Die Bilder der Bildergalerie
	 * @uml.property name="pictures"
	 */
	private ArrayList<Picture> pictures = new ArrayList<Picture>();
	
	/**
	 * Anzahl der Spalten für die Bildergalerie
	 * @uml.property name="numberColumns"
	 */
	private int numberColumns = PICGAL_COLS_STD;

	public PictureGallery(String title, Project project) {
		super(title, project);			
	}
	
	/**
	 * Setzt die Spaltenzahl der Bildergalerie 
	 * @return
	 * @uml.property name="numberColumns"
	 */
	public void setNumberColumns(int numberColumns) {
		int oldValue = this.numberColumns;
		this.numberColumns = numberColumns;
		firePropertyChange(PROP_COLUMNS_PICGALLERY, oldValue, numberColumns);
	}
	
	/**
	 * Liefert die Spaltenzahl der Bildergalerie
	 * @return
	 * @uml.property name="numberColumns"	 
	 */
	public int getNumberColumns() {
		return this.numberColumns;
	}

	/**
	 * Gibt die Liste der Bilder zurueck
	 * @return
	 * @uml.property name="pictures"
	 */
	public ArrayList<Picture> getPictures() {
		return pictures;
	}
	
	public void setPictures(List<Picture> pictures) {
		this.pictures.clear();
		for (Picture pic : pictures) {
			this.pictures.add(pic);
		}
		firePropertyChange(PROP_PICGALLERY, null, pictures);
	}
	
	/**
	 * 2 Galerien sind gleich wenn die selben Bilder in der selben Reihenfolge vorhanden sind
	 * @param gallery
	 * @return
	 */
	public boolean equalsGallery(PictureGallery gallery) {
		if (gallery == null) {
			return false;
		}
		if (gallery.pictures.size() != pictures.size()) {
			return false;
		}
		if (getNumberColumns() != gallery.getNumberColumns()) {
			return false;
		}
		for (int i=0; i < pictures.size(); i++) {
			if (!pictures.get(i).getFile().equals(gallery.pictures.get(i).getFile())) {
				return false;
			}			
		}
		if (!getTitle().equals(gallery.getTitle())) {
			return false;
		}
		return true;
	}
}
