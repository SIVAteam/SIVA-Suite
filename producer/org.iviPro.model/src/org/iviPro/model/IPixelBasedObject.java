/**
 * 
 */
package org.iviPro.model;

import java.awt.Dimension;
import java.io.File;

/**
 * Abstrakte Basis-Klasse fuer alle pixelbasierten Medien in der SIVA Suite.
 * 
 * @author dellwo
 */
public abstract class IPixelBasedObject extends IMediaObject {

	/**
	 * Der Name des "dimension"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_DIMENSION = "dimension"; //$NON-NLS-1$

	/**
	 * Groesse (Breite und Hoehe) des pixelbasierten Medien-Objekts.
	 */
	private Dimension dimension;

	public IPixelBasedObject(File file, Dimension dimension, Project project) {
		super(file, project);
		// TODO: Vielleicht hier die Dimension gleich aus der Bilddatei
		// auslesen?
		this.dimension = dimension;
	}

	/**
	 * Setzt die Groesse (Breite und Hoehe) des pixelbasierten Medien-Objekts.<br>
	 * <br>
	 * Ein Aufruf dieser Methode resultiert in einem PropertyChangeEvent fuer
	 * das Property <tt>PROP_DIMENSION</tt> mit altem und neuem Wert des
	 * Properties.
	 * 
	 * @param dimension
	 *            Die Groesse.
	 * @uml.property name="dimension"
	 */
	public void setDimension(Dimension dimension) {
		Dimension oldValue = this.dimension;
		this.dimension = dimension;
		firePropertyChange(PROP_DIMENSION, oldValue, dimension);
	}

	/**
	 * Setzt die Groesse (Breite und Hoehe) des pixelbasierten Medien-Objekts.<br>
	 * <br>
	 * Ein Aufruf dieser Methode resultiert in einem PropertyChangeEvent fuer
	 * das Property <tt>PROP_DIMENSION</tt> mit altem und neuem Wert des
	 * Properties.
	 * 
	 * @param x
	 *            Die Breite.
	 * @param y
	 *            Die Hoehe.
	 */
	public void setDimension(int x, int y) {
		setDimension(new Dimension(x, y));
	}

	/**
	 * Gibt die Groesse (Breite und Hoehe) des pixelbasierten Medien-Objekts
	 * zurueck.
	 * 
	 * @return Groesse (Breite und Hoehe) des pixelbasierten Medien-Objekts.
	 * @uml.property name="dimension"
	 */
	public Dimension getDimension() {
		return dimension;
	}

}
