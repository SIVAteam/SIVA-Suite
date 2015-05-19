/**
 * 
 */
package org.iviPro.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Abstrakte Basis-Klasse fuer alle auf Dateien basierenden Daten in der SIVA
 * Suite. Die Klasse bietet die Moeglichkeit auch sprach-abhaenige
 * Dateivarianten anzugeben.
 * 
 * @author dellwo
 * @uml.dependency supplier="org.iviPro.model.LocalizedFile"
 */
public abstract class IFileBasedObject extends IAbstractBean {

	/**
	 * Der Name des "file"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_FILE = "file"; //$NON-NLS-1$

	/**
	 * Lokalisierte Dateien die diesem Objekt zugrunde liegen. Der Key der Map
	 * ist dabei das Locale der Datei und der Wert der Map die mit dieser
	 * Sprache verknuepfte Datei.
	 * 
	 * @uml.property name="fileMap" readOnly="true"
	 */
	private Map<Locale, LocalizedFile> fileMap = 
			new HashMap<Locale, LocalizedFile>(2);
	
	/**
	 * Erstellt ein neues dateibasiertes Objekt.
	 * 
	 * @param file
	 *            Die Datei die dem Objekt zugrunde liegt. Als Sprache der Datei
	 *            wird die im angegebenen Projekt aktuell verwendete Sprache
	 *            benutzt. Als Titel des Objekts wird der Dateiname verwendet.
	 * @param project
	 *            Das Projekt.
	 */
	public IFileBasedObject(File file, Project project) {
		// Initialisieren mit Dateinamen als universellen Titel,
		// wobei Endungsteil weggeschnitten wurde
		super("", project); //$NON-NLS-1$
		String title = file.getName();
		if (title.indexOf(".") > 0) { //$NON-NLS-1$
			title = title.substring(0, title.lastIndexOf(".")); //$NON-NLS-1$
		}
		setTitle(title);
		Locale curLang = project.getCurrentLanguage();
		LocalizedFile locFile = new LocalizedFile(file, curLang);
		setFile(locFile);
	}

	/**
	 * Erstellt ein neues dateibasiertes Objekt.
	 * 
	 * @param file
	 *            Die Datei mit dem das Objekt initial verknuepft werden soll.
	 *            Als Sprache der Datei wird die Sprache des angegebenen Titels
	 *            verwendet.
	 * @param title
	 *            Der Titel des dateibasierten Objekts.
	 * @param project
	 *            Das zugehoerige Projekt.
	 */
	public IFileBasedObject(File file, LocalizedString title, Project project) {
		super(title, project);
		LocalizedFile locFile = new LocalizedFile(file, title.getLanguage());
		setFile(locFile);
	}

	/**
	 * Erstellt ein neues dateibasiertes Objekt.
	 * 
	 * @param file
	 *            Die Datei mit dem das Objekt initial verknuepft werden soll.
	 * @param title
	 *            Der Titel des Objekts.
	 * @param project
	 *            Das zugehoerige Projekt.
	 */
	public IFileBasedObject(LocalizedFile file, LocalizedString title,
			Project project) {
		super(title, project);
		setFile(file);
	}

	/**
	 * Gibt die mit diesem Objekt verknuepfte Datei zurueck, die in der aktuell
	 * verwendeten Sprache des Projektes gueltig ist oder null, falls das Objekt
	 * keine solche Datei besitzt.
	 * 
	 * @return Die verknuepfte Datei in der aktuellen Sprache des Projekts oder
	 *         null, falls das Objekt keine solche Datei besitzt.
	 * @uml.property name="file"
	 */
	public LocalizedFile getFile() {
		Locale langCode = project.getCurrentLanguage();
		return getFile(langCode);
	}

	/**
	 * Gibt die mit dem Objekt verknuepfte Datei zurueck, welche in der
	 * angefragten Sprache gueltig ist.
	 * 
	 * @param locale
	 *            Die gewuenschte Sprache.
	 * @return Die verknuepfte Datei die in der angefragten Sprache gueltig ist
	 *         oder <tt>null</tt> falls das Objekt keine solche Datei besitzt.
	 */
	public LocalizedFile getFile(Locale locale) {
		// TODO: Wenn Titel in ang. Sprache nicht existiert: Null oder default
		// language zurueck geben?
		if (fileMap.containsKey(locale)) {
			return fileMap.get(locale);

			// Falls ein sprach-unabhaengiger Titel existiert, geben wir
			// diesen als zweite Wahl zurueck oder eben null, falls auch dieser
			// nicht existiert.
		} else if (fileMap.containsKey(Locale.ROOT)) {
			return fileMap.get(Locale.ROOT);

			// Weder in der angefragten Sprache liegt ein Titel vor, noch in
			// einer sprach-unabhaengigen version
		} else {
			return null;
		}

	}

	/**
	 * Returns a list of the {@link LocalizedFile localized files} of this 
	 * IFileBasedObject.
	 * 
	 * @return list of localized files
	 */
	public Collection<LocalizedFile> getFiles() {
		ArrayList<LocalizedFile> files = new ArrayList<LocalizedFile>(fileMap
				.values());
		return files;
	}

	/**
	 * Setzt eine mit diesem Objekt verknuepfte Datei. Als Sprache der Datei
	 * wird die im aktuellen Projekt aktuell verwendete Sprache angenommen.<br>
	 * <br>
	 * Ein Aufruf dieser Methode resultiert in einem PropertyChangeEvent fuer
	 * das Property <tt>PROP_FILE</tt> mit altem und neuem Wert des Properties.
	 * 
	 * @param file
	 *            Die Datei.
	 * @uml.property name="file"
	 */
	public void setFile(File file) {
		Locale curLang = project.getCurrentLanguage();
		LocalizedFile newValue = new LocalizedFile(file, curLang);
		LocalizedFile oldValue = fileMap.put(curLang, newValue);
		firePropertyChange(PROP_FILE, oldValue, newValue);
	}

	/**
	 * Setzt eine mit diesem Objekt verknuepfte Datei.<br>
	 * <br>
	 * Ein Aufruf dieser Methode resultiert in einem PropertyChangeEvent fuer
	 * das Property <tt>PROP_FILE</tt> mit altem und neuem Wert des Properties.
	 * 
	 * @param file
	 *            Die Datei.
	 */
	public void setFile(LocalizedFile file) {
		LocalizedFile oldValue = fileMap.put(file.getLanguage(), file);
		firePropertyChange(PROP_FILE, oldValue, file);
	}

	/**
	 * Escaped einen Dateinamen und ersetzt alle Zeichnen die nicht a-z, A-Z,
	 * 0-9, -, _ oder ein Leerzeichen sind.
	 * 
	 * @param filename
	 * @return
	 */
	public static String escapeFilename(String filename) {
		filename = filename.replaceAll("[^a-zA-Z0-9-_ ]", "-").trim(); //$NON-NLS-1$ //$NON-NLS-2$
		return filename;
	}

	
// Wird nicht verwendet
	
//	/**
//	 * Löscht alle zum Objekt gehörenden Dateien
//	 * 
//	 * @param title
//	 * @param project
//	 * @return
//	 */
//	public void deleteFiles() {		
//		Collection<LocalizedFile> files = getFiles();
//		Iterator<LocalizedFile> fileIt = files.iterator();
//		while (fileIt.hasNext()) {
//			LocalizedFile curFile = fileIt.next();
//			curFile.getValue().delete();
//		}		
//	}

	@Override
	public boolean equals(Object obj) {		
		if (this == obj) {
			return true;
		}
		if (obj instanceof IFileBasedObject) {
			IFileBasedObject other = (IFileBasedObject) obj;
			for (LocalizedFile file : fileMap.values()) {
				if (other.fileMap.containsValue(file)) {
					return true;
				}
			}
		}
		return false;
	}
}
