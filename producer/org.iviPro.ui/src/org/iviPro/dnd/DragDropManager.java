package org.iviPro.dnd;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Klasse die per Drag-and-Drop uebertragene Objekte verwaltet, damit man
 * Drag-and-Drop mit realen Java-Objekte durchfuehren kann. Die Quelle des
 * DND-Transfers registriert das Objekt beim DragDropManager und ubertraegt nur
 * einen Objekt-Key per Drag-and-Drop. Das Ziel kann sich ueber den
 * uebertragenen Key dann das reale Java-Objekt vom DragDropManager holen.
 * 
 * @author dellwo
 * 
 */
public class DragDropManager {

	private static Logger logger = Logger.getLogger(DragDropManager.class);

	/**
	 * Map die die transferierten Objekte verwaltet.
	 */
	private Map<String, Object[]> objectMap;

	//test
	
	/**
	 * Erstellt einen neuen Drag-and-Drop Handler
	 */
	public DragDropManager() {
		objectMap = new HashMap<String, Object[]>();
	}

	/**
	 * Beendet den Drag-and-Drop Transfer eines Objektes und gibt einen Verweis
	 * auf dieses Objekt zurueck.
	 * 
	 * @param key
	 *            Der Transfer-Key des Objekts
	 * @return
	 */
	public Object[] endTransfer(String key) {
		Object[] objects = objectMap.remove(key);
		logger.debug("Transfer ended: " + Arrays.toString(objects)); //$NON-NLS-1$
		return objects;
	}

	/**
	 * Startet einen Drag-and-Drop Transfer und gibt den Transfer-Key zurueck,
	 * der dann per fuer den SWT-Drag-und-Drop Mechanismus mittels TextTransfer
	 * genutzt werden soll.
	 * 
	 * @param object
	 *            Das zu uebertragende Objekt
	 * @return
	 */
	public String startTransfer(Object object) {
		Object[] objects = (Object[]) Array.newInstance(object.getClass(), 1);
		objects[0] = object;
		return startTransfer(objects);
	}

	/**
	 * Startet einen Drag-and-Drop Transfer fuer mehrere Objekte und gibt den
	 * Transfer-Key zurueck, der dann per fuer den SWT-Drag-und-Drop Mechanismus
	 * mittels TextTransfer genutzt werden soll.
	 * 
	 * @param objects
	 *            Liste von zu uebertragenden Objekten
	 * @return
	 */
	public String startTransfer(Object[] objects) {
		String key = new String() + objects.hashCode();
		objectMap.put(key, objects);
		logger.debug("Transfer started: " + Arrays.toString(objects)); //$NON-NLS-1$
		return key;

	}

}
