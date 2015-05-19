/**
 * 
 */
package org.iviPro.model;


/**
 * Abstrakte Basis-Klasse fuer alle zeitbasierten Medien in der SIVA Suite.
 * 
 * @author dellwo
 */
public interface ITimeBasedObject {

	/**
	 * Der Name des "duration"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_DURATION = "duration"; //$NON-NLS-1$

	/**
	 * Gibt die Dauer des Media-Objekts in Nano-Sekunden zurueck.
	 * 
	 * @return Dauer des Media-Objekts in Nano-Sekunden.
	 * @uml.property name="duration"
	 */
	public Long getDuration();

	/**
	 * Setzt die Dauer des Media-Objekts in Nano-Sekunden. <br>
	 * <br>
	 * Ein Aufruf dieser Methode resultiert in einem PropertyChangeEvent fuer
	 * das Property <tt>PROP_DURATION</tt> mit altem und neuem Wert des
	 * Properties.
	 * 
	 * @param duration
	 *            Die Dauer des Media-Objekts in Nano-Sekunden
	 * @uml.property name="duration"
	 */
	public void setDuration(Long duration);

}
