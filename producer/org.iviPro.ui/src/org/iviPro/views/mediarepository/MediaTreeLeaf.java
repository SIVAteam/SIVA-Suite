package org.iviPro.views.mediarepository;

import org.apache.log4j.Logger;
import org.iviPro.application.Application;
import org.iviPro.model.IAbstractBean;

/**
 * Diese Klasse ist die letzte Hierarchiestufe in der Ansicht des
 * Medienrepositories.
 * 
 * @author Administrator
 * 
 */
public class MediaTreeLeaf implements MediaTreeNode {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MediaTreeLeaf.class);
	private MediaTreeGroup parent;
	private IAbstractBean mediaObject;

	/**
	 * Konstruktor...
	 * 
	 * @param newParent
	 *            Vater
	 * @param toAdd
	 *            Medienobjekt, das hinzugefügt werden soll
	 */
	public MediaTreeLeaf(MediaTreeGroup parent, IAbstractBean mediaObject) {
		this.parent = parent;		
		this.mediaObject = mediaObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.iviPro.internObjects.Media#getParent()
	 */
	public MediaTreeGroup getParent() {
		return parent;
	}

	/**
	 * Setzt die Obergruppe.
	 * 
	 * @param parent
	 */
	public void setParent(MediaTreeGroup parent) {
		this.parent = parent;
	}

	/**
	 * Liefert das gespeicherte Medienobjekt.
	 * 
	 * @return
	 */
	public IAbstractBean getMediaObject() {
		return mediaObject;
	}

	/**
	 * Speichert ein Medienobjekt.
	 * 
	 * @param media
	 */
	public void setMediaObject(IAbstractBean mediaObject) {
		this.mediaObject = mediaObject;
	}

	/**
	 * Gibt den Namen zurück, wie er angezeigt werden soll.
	 */
	public String getName() {
		return mediaObject.getTitle(Application.getCurrentLanguage());
	}
}
