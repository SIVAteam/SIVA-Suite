package org.iviPro.views.mediarepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.iviPro.theme.Icons;

/**
 * In diesem Container werden alle Medienobjekte eines Typs gehalten.
 * 
 * @author Florian Stegmaier
 */
public class MediaTreeGroup implements MediaTreeNode {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MediaTreeGroup.class);
	// Liste aller Medienobjekte
	private List<Object> entries;
	// Name des Containers (== Typbezeichnung)
	private String name;
	private Icons icon;
	// Vorgänger
	private MediaTreeGroup parent;

	/**
	 * Konstruktor
	 * 
	 * @param parentObject
	 *            Vaterobjekt
	 * @param givenName
	 *            Name, der angezeigt werden soll.
	 */
	public MediaTreeGroup(MediaTreeGroup parentObject, String givenName,
			Icons icon) {
		this.icon = icon;
		entries = new ArrayList<Object>();
		parent = parentObject;
		name = givenName;
	}

	public Icons getIcon() {
		return icon;
	}

	/**
	 * Liefert alle Einträge die dieser knoten enthält.
	 * 
	 * @return
	 */
	public List<Object> getEntries() {
		return this.entries;
	}

	/**
	 * Fügt ein neues Mediaobkjekt hinzu.
	 * 
	 * @param mediaToAdd
	 */
	public void addElement(Object toAdd) {
		this.entries.add(toAdd);
	}

	/**
	 * Liefert den Namen des Containers zurück.
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Setzt den Namen neu.
	 * 
	 * @param nameToSet
	 */
	public void setName(String nameToSet) {
		this.name = nameToSet;
	}

	/**
	 * Liefert den "Vatercontainer" zurück.
	 * 
	 * @return
	 */
	public MediaTreeGroup getParent() {
		return this.parent;
	}

	/**
	 * Setzt den "Vatercontainer".
	 * 
	 * @param newParent
	 */
	public void setParent(MediaTreeGroup newParent) {
		this.parent = newParent;
	}
}
