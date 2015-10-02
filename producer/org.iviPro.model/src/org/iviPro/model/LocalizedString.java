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
		if (this == obj) {
			return true;
		}
		
		if (!(obj instanceof LocalizedString)) {
			return false;
		}	
		
		LocalizedString lsObj = (LocalizedString) obj;
		return language.equals(lsObj.language) 
				&& value.equals(lsObj.value);
	}
	
	@Override
	public int hashCode() {
		return language.hashCode() + value.hashCode();
	}

}
