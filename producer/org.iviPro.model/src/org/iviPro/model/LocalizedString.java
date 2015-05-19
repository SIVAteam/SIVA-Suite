package org.iviPro.model;

import java.io.Serializable;
import java.util.Locale;

/**
 * Klasse zum Kapseln eines lokalisierten Strings fuer Rueckgabewerte von
 * Funktionen, PropertyChangeEvents und aehnliche Dinge, wo nur ein Objekt
 * uebergeben werden kann.
 * 
 * @author dellwo
 * 
 */
public class LocalizedString extends LocalizedElement {

	
	public LocalizedString(String value, Locale language) {
		super(value, language);
	}
	
	public LocalizedString(String value, Project project) {
		super(value, project);
	}

	/**
	 * Returns the string contained in this element.
	 * 
	 * @return string contained in the element
	 */
	public String getValue() {
		return (String)value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LocalizedString) {
			LocalizedString other = (LocalizedString) obj;
			return language.equals(other.language) && value.equals(other.value);
		} else {
			return false;
		}
	}

}
