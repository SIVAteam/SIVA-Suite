package org.iviPro.model;

import java.io.Serializable;
import java.util.Locale;

public abstract class LocalizedElement implements Serializable {
	
	/**
	 * The language of this element.
	 * 
	 * @uml.property name="language" readOnly="true"
	 */
	protected Locale language;
	
	/**
	 * The object contained in this localized element.
	 * 
	 * @uml.property name="value" readOnly="true"
	 */
	protected Object value;
	
	/**
	 * Constructs a localized element for the given language containing the
	 * given object.
	 * 
	 * @param value		object contained in the element
	 * @param language	language of the element
	 */
	public LocalizedElement(Object value, Locale language) {
		this.language = language;
		this.value = value;
	}
	
	/**
	 * Constructs a localized element for the language used by the given
	 * project containing the given object.
	 * 
	 * @param value		object contained in the element
	 * @param project	project which defines the language of the element 
	 */
	public LocalizedElement(Object value, Project project) {
		this.language = project.getCurrentLanguage();
		this.value = value;
	}
	
	/**
	 * Returns the language code of the element's language. In accordance with
	 * the SIVA XML-Format the returned code is a combination of language and
	 * country code with respect to ISO639 and ISO3166 (en-us, en-gb etc.).
	 * When the neutral locale ({@link Locale#ROOT}) is provided, 
	 * <code>null</code> is returned.
	 * 
	 * @locale locale for which the language code should be returned
	 * 
	 * @return language code for the given locale
	 */
	public static String getSivaLangcode(Locale locale) {
		if (locale.equals(Locale.ROOT)) {
			return null;
		} else {
			return locale.getLanguage() + "-" //$NON-NLS-1$
					+ locale.getCountry().toLowerCase();
		}
	}
	
	/**
	 * Returns the language code of the element's language. In accordance with
	 * the SIVA XML-Format the returned code is a combination of language and
	 * country code with respect to ISO639 and ISO3166 (en-us, en-gb etc.).
	 * If the element's language is set to the neutral locale 
	 * ({@link Locale#ROOT}), <code>null</code> is returned.
	 * 
	 * @return language code of the element
	 */
	public String getSivaLangcode() {
		return getSivaLangcode(language);
	}
	
	/**
	 * Returns the language of the element.
	 * 
	 * @return language of the element
	 * @uml.property name="language"
	 */
	public Locale getLanguage() {
		return language;
	}	
	
	/**
	 * Returns the object contained in this element.   
	 * 
	 * @return object contained in the element
	 */
	public Object getValue() {
		return value;
	}
}
