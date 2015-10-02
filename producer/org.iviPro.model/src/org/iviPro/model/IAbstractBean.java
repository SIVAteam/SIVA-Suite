/**
 * 
 */
package org.iviPro.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

import org.apache.log4j.Logger;

/**
 * Abstrakte Basis-Klasse fuer alle Beans in der SIVA Suite. Die Klasse stellt
 * Grundfunktionen fuer die Beans zur Verfuegung wie z.B. Unterstuetzung fuer
 * PropertyChangeListener.
 * 
 * @author dellwo
 */
public abstract class IAbstractBean implements Serializable {

	private static Logger logger = Logger.getLogger(IAbstractBean.class);

	/**
	 * Der Name des "title"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_TITLE = "title"; //$NON-NLS-1$

	/**
	 * Der Name des "description"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_DESCRIPTION = "description"; //$NON-NLS-1$

	/**
	 * Der Name des "keywords"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_KEYWORDS = "keywords"; //$NON-NLS-1$

	/**
	 * Der Name des "images"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_IMAGES = "images"; //$NON-NLS-1$
		
	/**
	 * Keywords in komma-separierter Form. Kommata selbst werden durch
	 * Backslashes escaped, z.B. der Wert "Einstein\, Albert,
	 * Physiker" ergibt zwei Keywords: "Einstein, Albert" und "Physiker".
	 * 
	 * @uml.property name="keywords"
	 * 
	 */
	private String keywords = ""; //$NON-NLS-1$

	/**
	 * Referenz auf das zugehoerige Projekt.
	 */
	protected Project project;

	/**
	 * Mehrsprachiger Titel des Objekts. Die Map speichert dabei die Titel als
	 * Werte der Map und das jeweils zugehoerige Locale des Titels dient als
	 * Key.
	 */
	private HashMap<Locale, LocalizedString> titleMap;
	
	/**
	 * Bilder zum Repräsentieren eines Beans z.B. Szene oder Videoannotation
	 */
	private LinkedList<SivaImage> images;

	/**
	 * Map for the storage of a multilingual description of the bean.
	 * While the existence of a title is mandatory, the map of descriptions may
	 * be empty.
	 */
	private HashMap<Locale, LocalizedString> descrMap;
		
	/** Datenhashmap für beliebige zusätzliche Daten
	 * z.B. für den Export von VideoAnnotationen zum Speichern der zu
	 * exportierenden Start/Endzeiten
	 */
	private HashMap<String, Object> dataMap;

	/**
	 * Erstellt ein neues IAbstractBean
	 * 
	 * @param title
	 *            Der Titel des Beans in einer bestimmten Sprache
	 */
	public IAbstractBean(LocalizedString title, Project project) {
		this.project = project;
		titleMap = new HashMap<Locale, LocalizedString>(2);
		descrMap = new HashMap<Locale, LocalizedString>(0);
		dataMap = new HashMap<String, Object>();
		images = new LinkedList<SivaImage>();
		if (title != null) {
			setTitle(title);
		}
	}

	/**
	 * Erstellt ein neues IAbstractBean
	 * 
	 * @param title
	 *            Der Titel des Beans. Als Sprache wird die aktuelle Sprache
	 *            (currentLocale) des Projekts verwendet.
	 */
	public IAbstractBean(String title, Project project) {
		this.project = project;
		titleMap = new HashMap<Locale, LocalizedString>(2);
		descrMap = new HashMap<Locale, LocalizedString>(0);
		dataMap = new HashMap<String, Object>();
		if (title != null) {
			setTitle(title);
		}
	}

//	/**
//	 * Gibt eine Liste mit den von diesem Model-Objekt abhaengigen Objekten
//	 * zurueck. Beispielsweise sind von einem Video die abhaengigen Objekte alle
//	 * darauf basierenden Szenen und Annotationen.
//	 * 
//	 * @return
//	 */
////	public abstract List<IAbstractBean> getDependentObjects();

	@Override
	public String toString() {
		return this.getClass().getSimpleName() +
				"[" + (project != null && getTitle() != null //$NON-NLS-1$
				? getTitle() : "") + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Returns a tag which can be used to identify the bean. However, the tag needs 
	 * not to be unique. Standard implementation returns the simple name of the
	 * beans class. 
	 * @return tag used to identify the bean
	 */
	public String getBeanTag() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Dieses Objekt wird benutzt um die PropertyChangeListener zu verwalten und
	 * sie ueber Aenderungen zu informieren. Es wird erst instantiiert wenn sich
	 * zum ersten Mal ein Listener bei diesem Bean registriert.
	 * 
	 * @uml.property name="changeSupport"
	 */
	private transient PropertyChangeSupport changeSupport = null;

	/**
	 * Returns a string containing the keywords stored for this bean.
	 * The format of the string is not constricted.
	 * 
	 * @return string containing keywords
	 * @uml.property name="keywords"
	 */
	public String getKeywords() {
		return keywords;
	}
	
	/**
	 * add an Image to the bean
	 * @param image
	 */
	public void setImages(LinkedList<SivaImage> images) {
		LinkedList<SivaImage> oldValue = new LinkedList<SivaImage>();
		this.images = images;
		firePropertyChange(PROP_IMAGES, oldValue, images);
	}
	
	/**
	 * return bean images
	 * @return
	 */
	public LinkedList<SivaImage> getImages() {
		return this.images;
	}

	/**
	 * Sets the keywords of this bean to the given string representing a list 
	 * of keywords. The format of the string has not to constricted.
	 * Fires a PropertyChangeEvent for property <tt>PROP_KEYWORDS</tt>.
	 * 
	 * @param keywords
	 *            string containing the new keywords 
	 * @uml.property name="keywords"
	 */
	public void setKeywords(String keywords) {
		String oldValue = this.keywords;
		this.keywords = keywords;
		firePropertyChange(PROP_KEYWORDS, oldValue, keywords);
	}

	/**
	 * Gibt den Titel des Objekts in einer bestimmten Sprache als lokalisierter
	 * String zurueck.
	 * 
	 * @param locale
	 *            Die gewuenschte Sprache.
	 * @return Der Titel des Objekts in der angefragten Sprache oder
	 *         <tt>null</tt> falls in dieser Sprache kein Titel gesetzt ist.
	 */
	public LocalizedString getLocalizedTitle(Locale locale) {
		// TODO: Wenn Titel in ang. Sprache nicht existiert: Null oder default
		// language zurueck geben?
		if (titleMap.containsKey(locale)) {
			return titleMap.get(locale);

			// Falls ein sprach-unabhaengiger Titel existiert, geben wir
			// diesen als zweite Wahl zurueck oder eben null, falls auch dieser
			// nicht existiert.
		} else if (titleMap.containsKey(Locale.ROOT)) {
			return titleMap.get(Locale.ROOT);

			// Weder in der angefragten Sprache liegt ein Titel vor, noch in
			// einer sprach-unabhaengigen version
		} else {
			return null;
		}

	}

	/**
	 * Gibt den Titel des Objekts in einer bestimmten Sprache zurueck.
	 * 
	 * @param locale
	 *            Die gewuenschte Sprache.
	 * @return Der Titel des Objekts in der angefragten Sprache oder
	 *         <tt>null</tt> falls in dieser Sprache kein Titel gesetzt ist.
	 */
	public String getTitle(Locale locale) {
		LocalizedString locTitle = getLocalizedTitle(locale);
		if (locTitle == null) {
			return null;
		} else {
			return locTitle.getValue();
		}
	}

	/**
	 * Gibt den Titel des Objekts in der aktuellen currentLanguage des Projekts
	 * zurueck.
	 * 
	 * @return Der Titel des Objekts in der angefragten Sprache oder
	 *         <tt>null</tt> falls in dieser Sprache kein Titel gesetzt ist.
	 */
	public String getTitle() {
		Locale langCode = project.getCurrentLanguage();
		return getTitle(langCode);
	}

	/**
	 * Gibt den Titel des Objekts in der aktuellen currentLanguage des Projekts
	 * als LocalizedString zurueck.
	 * 
	 * @return Der Titel des Objekts in der angefragten Sprache oder
	 *         <tt>null</tt> falls in dieser Sprache kein Titel gesetzt ist.
	 */
	public LocalizedString getLocalizedTitle() {
		return getLocalizedTitle(project.getCurrentLanguage());
	}

	/**
	 * Gibt eine Liste aller Titel dieses Beans zurueck.
	 * 
	 * @return
	 */
	public Collection<LocalizedString> getTitles() {
		ArrayList<LocalizedString> titles = new ArrayList<LocalizedString>(
				titleMap.size());
		for (LocalizedString title : titleMap.values()) {
			titles.add(title);
		}
		return titles;
	}

	/**
	 * Gibt eine Liste aller Beschreibungen dieses Beans zurueck.
	 * 
	 * @return
	 */
	public Collection<LocalizedString> getDescriptions() {
		ArrayList<LocalizedString> descriptions = new ArrayList<LocalizedString>(
				descrMap.size());
		for (LocalizedString description : descrMap.values()) {
			descriptions.add(description);
		}
		return descriptions;
	}

	/**
	 * Setzt den Titel des Objekts in der als currentLanguage im Projekt
	 * gesetzten Sprache.<br>
	 * <br>
	 * Ein Aufruf dieser Methode resultiert in einem PropertyChangeEvent fuer
	 * das Property <tt>PROP_TITLE</tt> mit altem und neuem Wert des Properties
	 * in dieser Sprache.
	 * 
	 * @param title
	 *            Der neue Titel in einer bestimmten Sprache.
	 * 
	 */
	public void setTitle(String title) {
		Locale curLang = project.getCurrentLanguage();
		setTitle( new LocalizedString(title, curLang));
	}

	/**
	 * Setzt den Titel des Objekts in einer bestimmten Sprache.<br>
	 * <br>
	 * Ein Aufruf dieser Methode resultiert in einem PropertyChangeEvent fuer
	 * das Property <tt>PROP_TITLE</tt> mit altem und neuem Wert des Properties
	 * in dieser Sprache.
	 * 
	 * @param title
	 *            Der neue Titel in einer bestimmten Sprache.
	 * 
	 */
	public void setTitle(LocalizedString title) {
		/* TODO At some points in code (e.q. IXMLExporter.createTitleLabels) 
		 * checks are done if the title map is null. In the following implementation
		 * entries in the title map are never deleted. It might be useful to 
		 * implement deletion, however, all calls to getTitle() need to be checked
		 * in this case.
		 */
		LocalizedString oldValue = titleMap.put(title.getLanguage(), title);
		firePropertyChange(PROP_TITLE, oldValue, title);
	}

	/**
	 * Gibt die Beschreibung des Objekts in einer bestimmten Sprache zurueck.
	 * 
	 * @param language
	 *            Die gewuenschte Sprache.
	 * @return Die Beschreibung des Objekts in der angefragten Sprache oder
	 *         <tt>null</tt> falls in dieser Sprache keine Beschreibung gesetzt
	 *         ist.
	 */
	public String getDescription(Locale language) {
		// TODO: Wenn Description in ang. Sprache nicht existiert: Null oder
		// default language zurueck geben?
		if (descrMap.containsKey(language)) {
			return descrMap.get(language).getValue();

			// Falls eine sprach-unabhaengige Description existiert, geben wir
			// diese als zweite Wahl zurueck oder eben null, falls auch diese
			// nicht existiert.
		} else if (descrMap.containsKey(Locale.ROOT)) {
			return descrMap.get(Locale.ROOT).getValue();

			// Weder in der angefragten Sprache liegt eine Description vor,
			// noch in einer sprach-unabhaengigen Version
		} else {
			return null;
		}

	}

	/**
	 * Gibt die Beschreibung des Objekts in der aktuellen currentLanguage des
	 * Projekts zurueck.
	 * 
	 * @return Die Beschreibung des Objekts in der angefragten Sprache oder
	 *         <tt>null</tt> falls in dieser Sprache keine Beschreibung gesetzt
	 *         ist.
	 */
	public String getDescription() {
		Locale langCode = project.getCurrentLanguage();
		return getDescription(langCode);
	}

	/**
	 * Setzt die Beschreibung des Objekts in einer bestimmten Sprache.<br>
	 * <br>
	 * Ein Aufruf dieser Methode resultiert in einem PropertyChangeEvent fuer
	 * das Property <tt>PROP_DESCRIPTION</tt> mit altem und neuem Wert des
	 * Properties in dieser Sprache.
	 * 
	 * @param description
	 *            Die neue Beschreibung in einer bestimmten Sprache
	 */
	public void setDescription(LocalizedString description) {
		LocalizedString oldValue;
		if (description.getValue() == null) {
			oldValue = descrMap.remove(description.getLanguage());
		} else {
			oldValue = descrMap.put(description.getLanguage(), 
					description);
		}
		firePropertyChange(PROP_DESCRIPTION, oldValue, description);

	}

	/**
	 * Setzt die Beschreibung des Objekts in einer bestimmten Sprache.<br>
	 * <br>
	 * Ein Aufruf dieser Methode resultiert in einem PropertyChangeEvent fuer
	 * das Property <tt>PROP_DESCRIPTION</tt> mit altem und neuem Wert des
	 * Properties in dieser Sprache.
	 * 
	 * @param description
	 *            Die neue Beschreibung in einer bestimmten Sprache
	 */
	public void setDescription(String description) {
		Locale curLang = project.getCurrentLanguage();
		setDescription(new LocalizedString(description, curLang));
	}

	/**
	 * Returns the project this bean belongs to.
	 * 
	 * @return project of the bean
	 */
	public Project getProject() {
		return project;
	}

	/**
	 * Setzt das zugehoerige Projekt zurueck. Diese Methode ist nur zur internen
	 * Nutzung innerhalb des Models gedacht.
	 * 
	 * @param project
	 *            Das zugehoerige Projekt.
	 */
	void setProject(Project project) {
		this.project = project;
	}

	/**
	 * Fuegt einen neuen PropertyChangeListener zu diesem Bean hinzu. Der
	 * PropertyChangeListener wird ueber Aenderungen des Beans informiert.
	 * 
	 * @param listener
	 *            Der neue Listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		// Falls noch kein Listener registriert ist bei diesem Bean erstellen
		// wir zuerst den PropertyChangeSupport der die Listener verwaltet.
		if (changeSupport == null) {
			changeSupport = new PropertyChangeSupport(this);
		}
		// Listener nur hinzufuegen falls sie noch nicht existieren.
		boolean contains = false;
		for (PropertyChangeListener pcl : changeSupport.getPropertyChangeListeners()) {
			if (pcl.equals(listener)) {
				contains = true;
				break;
			}
		}
		if (!contains) {
			changeSupport.addPropertyChangeListener(listener);
		}
		logger.debug("Added " + listener + " as listener of: " + this); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Fuegt einen neuen PropertyChangeListener zu diesem Bean hinzu. Der
	 * PropertyChangeListener wird ueber Aenderungen des Beans informiert.
	 * 
	 * @param listener
	 *            Der neue Listener
	 */
	public void addPropertyChangeListener(String property,
			PropertyChangeListener listener) {
		// Falls noch kein Listener registriert ist bei diesem Bean erstellen
		// wir zuerst den PropertyChangeSupport der die Listener verwaltet.
		if (changeSupport == null) {
			changeSupport = new PropertyChangeSupport(this);
		}
		// Listener hinzufuegen
		changeSupport.addPropertyChangeListener(property, listener);
		logger.debug("Added " + listener + " as listener of property '" //$NON-NLS-1$ //$NON-NLS-2$
				+ property + "' of object: " + this); //$NON-NLS-1$
	}

	/**
	 * Entfernt einen PropertyChangeListener wieder von diesem Bean.
	 * 
	 * @param listener
	 *            Der Listener der getrennt werden soll.
	 */
	public synchronized void removePropertyChangeListener(
			PropertyChangeListener listener) {
		// Entferne den Listener, falls ueberhaupt einer registriert ist
		if (changeSupport != null) {
			changeSupport.removePropertyChangeListener(listener);			
		}
	}
	
	public PropertyChangeListener[] getPropertyChangeListeners() {
		if (this.changeSupport != null) {
			return this.changeSupport.getPropertyChangeListeners();
		} else {
			return null;
		}
	}

	/**
	 * Feuert einen PropertyChangeEvent an alle registrierten Listener.
	 * 
	 * @param propName
	 *            Der Name des Properties die sich geaendert hat.
	 * @param oldValue
	 *            Der alte Wert des Properties.
	 * @param newValue
	 *            Der neue Wert des Properties.
	 */
	public void firePropertyChange(String propName, Object oldValue,
			Object newValue) {
		if (changeSupport != null) {
			changeSupport.firePropertyChange(propName, oldValue, newValue);	
		}
		// Werte sollten auch null werden können, für Undo/Redo da bestimmte Werte z.B.
		// Videos auch null sein können
		/* 
		// 1) Zuerst pruefen wir ob ueberhaupt Listener registriert sind und
		// ob alter Wert und neuer Wert des Properties ungleich null sind.
		// Sind beide null, hat sich nichts geaendert und wir muessen die
		// Listener gar nicht informieren
		if (changeSupport != null && !(oldValue == null && newValue == null)) {
			// 2) Wenn die beiden Werte nicht null sind pruefen wir, ob sie
			// unterschiedlich sind. Nur wenn sie ungleich sind muessen wir
			// die Listener ueber eine Aenderung informieren
			if ((oldValue != null && !oldValue.equals(newValue))
					|| (newValue != null && !newValue.equals(oldValue))) {
				logger.debug(this + "::PropertyChange::" + propName + "  -  " //$NON-NLS-1$ //$NON-NLS-2$
						+ oldValue + " -> " + newValue); //$NON-NLS-1$
				changeSupport.firePropertyChange(propName, oldValue, newValue);
			}
		}
		*/
	}
}
