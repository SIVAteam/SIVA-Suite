package org.iviPro.model.quiz;

import java.io.File;

import org.iviPro.model.IQuizBean;
import org.iviPro.model.Project;

/**
 * Klasse zum Verwalten von ZusatzInformationen.
 * 
 * @author Sabine Gattermann
 * @modified Stefan Zwicklbauer
 * 
 * @uml.dependency supplier="org.iviPro.model.quiz"
 */
public class AdditionalInfo extends IQuizBean {

	private static final long serialVersionUID = 1L;

	/**
	 * @uml.property name="idAdditionalInfo"
	 */
	private int idAdditionalInfo;

	/**
	 * @uml.property name="address"
	 */
	private String address;

	/**
	 * @uml.property name="mediaName"
	 */
	private String mediaName;

	/**
	 * @uml.property name="mediaBlob"
	 */
	private File mediaBlob;

	/**
	 * @uml.property name="position"
	 */
	private int position;

	/**
	 * @uml.property name="type"
	 */
	private int type;

	private final int TYPE_LINK = 0;

	/*
	 * private final int TYPE_VIDEO = 1; private final int TYPE_AUDIO = 2;
	 * private final int TYPE_IMAGE = 3;
	 */

	/**
	 * Konstruktor.
	 * 
	 * @param idAdditionlInfo
	 *            Die ID.
	 * @param address
	 *            Die Adresse (falls vom Typ Link)
	 * @param mediaName
	 *            Der Name. (falls Datei)
	 * @param mediaBlob
	 *            Die Datei. (falls Datei)
	 * @param position
	 *            Die Position.
	 * @param type
	 *            Der Typ.
	 */
	public AdditionalInfo(Project project, int idAdditionlInfo, String address,
			String mediaName, File mediaBlob, int position, int type) {
		super(project);
		this.idAdditionalInfo = idAdditionlInfo;
		this.address = address;
		this.mediaName = mediaName;
		this.mediaBlob = mediaBlob;
		this.position = position;
		this.type = type;
	}

	/**
	 * Konstruktor (Typ: Link)
	 * 
	 * @param address
	 *            Die Adresse.
	 * @param position
	 *            Die Position.
	 */
	public AdditionalInfo(Project project, String address, int position) {
		super(project);
		this.idAdditionalInfo = -1;
		this.address = address;
		this.mediaName = null;
		this.mediaBlob = null;
		this.position = position;
		this.type = TYPE_LINK;
	}

	/**
	 * Konstruktor (Typ: Datei)
	 * 
	 * @param mediaName
	 *            Der Name.
	 * @param mediaBlob
	 *            Die Datei.
	 * @param position
	 *            Die Position.
	 * @param type
	 *            Der Typ.
	 */
	public AdditionalInfo(Project project, String mediaName, File mediaBlob,
			int position, int type) {
		super(project);
		this.idAdditionalInfo = -1;
		this.address = null;
		this.mediaName = mediaName;
		this.mediaBlob = mediaBlob;
		this.position = position;
		this.type = type;
	}

	/**
	 * Standard-Konstruktor
	 */
	public AdditionalInfo(Project project) {
		super(project);
		this.idAdditionalInfo = -1;
		this.address = null;
		this.mediaName = null;
		this.mediaBlob = null;
		this.position = -1;
		this.type = -1;
	}

	/**
	 * Getter fuer ZusatzInfo-ID.
	 * 
	 * @return Die ID.
	 * @uml.property name="idAdditionalInfo"
	 */
	public int getIdAdditionalInfo() {
		return idAdditionalInfo;
	}

	/**
	 * Setter fuer ZusatzInfo-ID.
	 * 
	 * @param idAdditionalInfo
	 *            Die ID.
	 * @uml.property name="idAdditionalInfo"
	 */
	public void setIdadditionalInfo(int idAdditionalInfo) {
		this.idAdditionalInfo = idAdditionalInfo;
	}

	/**
	 * Getter fuer Link-Adresse.
	 * 
	 * @return Die Adresse.
	 * @uml.property name="address"
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Setter fuer Link-Adresse.
	 * 
	 * @param address
	 *            Die Adresse.
	 * @uml.property name="address"
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * Getter fuer Datei.
	 * 
	 * @return Die Datei.
	 * @uml.property name="mediablob"
	 */
	public File getMediaBlob() {
		return mediaBlob;
	}

	/**
	 * Setter fuer Datei.
	 * 
	 * @param media
	 *            Die Datei.
	 * @uml.property name="mediablob"
	 */
	public void setMediaBlob(File media) {
		this.mediaBlob = media;
	}

	/**
	 * Getter fuer Typ.
	 * 
	 * @return Der Typ (Link=0, Video=1, Audio=2, Bild=3)
	 * @uml.property name="type"
	 */
	public int getType() {
		return type;
	}

	/**
	 * Setter fuer Typ.
	 * 
	 * @param type
	 *            Der Typ (Link=0, Video=1, Audio=2, Bild=3)
	 * @uml.property name="type"
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Setter fuer Name.
	 * 
	 * @param mediaName
	 *            Der Name.
	 * @uml.property name="mediaName"
	 */
	public void setMediaName(String mediaName) {
		this.mediaName = mediaName;
	}

	/**
	 * Getter fuer Name.
	 * 
	 * @return Der Name.
	 * @uml.property name="mediaName"
	 */
	public String getMediaName() {
		return mediaName;
	}

	/**
	 * Setter fuer Position.
	 * 
	 * @param position
	 *            Die Position.
	 * @uml.property name="position"
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * Getter fuer Position.
	 * 
	 * @return Die Position.
	 * @uml.property name="position"
	 */
	public int getPosition() {
		return position;
	}

}
