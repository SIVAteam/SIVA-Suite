package org.iviPro.model;

import java.io.File;
import java.io.Serializable;
import java.util.Locale;

/**
 * Klasse zum Kapseln einer lokalisierten Datei fuer Rueckgabewerte von
 * Funktionen, PropertyChangeEvents und aehnliche Dinge, wo nur ein Objekt
 * uebergeben werden kann.
 * 
 * @author dellwo
 * 
 */
public class LocalizedFile extends LocalizedElement {

	public LocalizedFile(File file) {
		super(file, Locale.ROOT);
	}

	public LocalizedFile(File file, Locale language) {
		super(file, language);
	}

	/**
	 * Returns the absolute path of the contained <code>File</code> object.
	 * 
	 * @return absolute path of the contained <code>File</code> object
	 */
	public String getAbsolutePath() {
		return getValue().getAbsolutePath();
	}

	/**
	 * Returns the <code>File</code> object contained in this element.
	 * 
	 * @return <code>File</code> object contained in this element
	 */
	public File getValue() {
		return (File) value;
	}

	/**
	 * Checks whether the file denoted by the contained <code>File</code>
	 * object exists in the file system.
	 * @return true if a file exists in the file system - false otherwise
	 */
	public boolean exists() {
		return getValue().exists();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof LocalizedFile) {
			LocalizedFile other = (LocalizedFile) obj;
			return language.equals(other.language) 
					&& getValue().equals(other.getValue());
		} else {
			return false;
		}
	}

	/**
	 * Sets the <code>File</code> object contained in this element to the given
	 * file.
	 * @param file <code>File</code> object to store in this element  
	 */
	public void setValue(File file) {
		this.value = file;
	}
}
