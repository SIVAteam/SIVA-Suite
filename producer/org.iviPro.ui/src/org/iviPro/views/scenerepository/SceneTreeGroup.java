package org.iviPro.views.scenerepository;

import java.util.ArrayList;
import java.util.List;

/**
 * In diesem Container werden alle Szenenobjekte einer Sortierung gehalten.
 * 
 * @author Florian Stegmaier
 */
public class SceneTreeGroup extends SceneTreeNode implements Cloneable {
	
	private String name;
	
	// private static Logger logger = Logger.getLogger(SceneGroup.class);
	// Liste aller Szenenobjekte
	private ArrayList<SceneTreeNode> entries;

	/**
	 * Konstruktor...
	 * 
	 * @param parentObject
	 *            Vaterobjekt
	 * @param givenName
	 *            Name, wie er angezeigt werden soll
	 */
	public SceneTreeGroup(SceneTreeGroup parentObject, String givenName) {
		super(parentObject);
		this.name = givenName;
		entries = new ArrayList<SceneTreeNode>();
	}

	/**
	 * Entfernt den Eintrag aus dieser Gruppe, aber nur, wenn er ein direktes
	 * Kind ist.
	 * 
	 * @param obj
	 *            Der zu entfernende Eintrag
	 * @return Gibt true zurueck, falls der Eintrag ein direktes Kind war und
	 *         daher entfernt wurde, sonst false.
	 */
	public boolean removeEntry(SceneTreeNode obj) {
		return entries.remove(obj);
	}

	/**
	 * Liefert alle Einträge in diesem Container.
	 * 
	 * @return
	 */
	public List<SceneTreeNode> getEntries() {
		return entries;
	}

	/**
	 * Fügt dem Container Einträge hinzu.
	 * 
	 * @param o
	 */
	public void addEntries(SceneTreeNode o) {
		entries.add(o);
	}

	@Override
	public String getName() {
		return name;
	}
}
