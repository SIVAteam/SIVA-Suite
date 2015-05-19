/**
 * 
 */
package org.iviPro.model;

import java.io.File;
import java.util.UUID;

/**
 * Abstrakte Basis-Klasse fuer alle Media-Daten in der SIVA Suite.
 * 
 * @author dellwo
 */
public abstract class IMediaObject extends IFileBasedObject {

	/**
	 * Der Name des "codec"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_CODEC = "codec"; //$NON-NLS-1$

	//TODO Florian Dok and Test
	private String id;
	
	/**
	 * Der Codec mit dem das Medien-Objekt kodiert ist.
	 * 
	 * @uml.property name="codec"
	 */
	private String codec = ""; //$NON-NLS-1$

	/**
	 * 
	 * @param file
	 */
	public IMediaObject(File file, Project project) {
		super(file, project);
		id = UUID.randomUUID().toString();
	}

	/**
	 * Gibt den Codec mit dem das Medien-Objekt kodiert ist zurueck.
	 * 
	 * @return Codec mit dem das Medien-Objekt kodiert ist.
	 * @uml.property name="codec"
	 */
	public String getCodec() {
		return codec;
	}

	/**
	 * Setzt den Codec mit dem das Medien-Objekt kodiert ist.<br>
	 * <br>
	 * Ein Aufruf dieser Methode resultiert in einem PropertyChangeEvent fuer
	 * das Property <tt>PROP_CODEC</tt> mit altem und neuem Wert des Properties.
	 * 
	 * @param codec
	 *            The codec to set.
	 * @uml.property name="codec"
	 */
	public void setCodec(String codec) {
		String oldValue = this.codec;
		this.codec = codec;
		firePropertyChange(PROP_CODEC, oldValue, codec);
	}

	/**
	 * TODO DOK
	 * @return
	 */
	public String getId() {
		// kompatibilitaet zu aelteren projekten
		if (id == null || id.isEmpty()) {
			id = UUID.randomUUID().toString();
		}
		return id;
	}

	/**
	 * TODO DOK
	 * @return
	 */
	public void setId(String id) {
		this.id = id;
	}
}
