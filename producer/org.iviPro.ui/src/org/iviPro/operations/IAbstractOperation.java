package org.iviPro.operations;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.iviPro.editors.scenegraph.layout.LayoutManager;

/**
 * Abstrakte Basisklasse fuer alle Operationen. Alle Aenderungen am Model
 * muessen ueber Operationen abgebildet werden, damit der Redo/Undo Mechanismus
 * von Eclipse greifen kann.
 * 
 * @author dellwo
 * 
 */
public abstract class IAbstractOperation extends AbstractOperation {

	/**
	 * Erstellt eine neue Instanz der abstrakten Operation.
	 */
	public IAbstractOperation(String label) {
		super(label);
		addContext(OperationHistory.getContext());
		this.addPropertyChangeListener(new LayoutManager());
	}

	/**
	 * Liefert eine Fehlermeldung, wenn es beim Ausfuehren der Operation zu
	 * einem Fehler kommt. Dieser Text wird dem Benutzer dann in einer Dialogbox
	 * angezeigt.
	 * 
	 * @param e
	 *            Die Exception, die beim Ausfuehren auftrat.
	 * @return Fehlermeldungstext fuer den Benutzer.
	 */
	public abstract String getErrorMessage(Exception e);

	/**
	 * Gibt an, ob die Operation ausgefuehrt werden kann, d.h. dass alle
	 * noetigen Daten zum Ausfuehren der Operation vorliegen. Wenn hier true
	 * zurueck geliefert wird, dann wird im Anschluss die execute() Methode der
	 * Operation aufgerufen.
	 * 
	 * @see org.eclipse.core.commands.operations.AbstractOperation#canExecute()
	 */
	public abstract boolean canExecute();
	
	
	/**
	 * Dieses Objekt wird benutzt um die PropertyChangeListener zu verwalten und
	 * sie ueber Aenderungen zu informieren. Es wird erst instantiiert wenn sich
	 * zum ersten Mal ein Listener bei dieser Klasse registriert.
	 * 
	 * @uml.property name="changeSupport"
	 */
	private PropertyChangeSupport changeSupport = null;
	
	/**
	 * Fuegt einen neuen PropertyChangeListener zu dieser Klasse hinzu. Der
	 * PropertyChangeListener wird ueber Aenderungen der Klasse informiert.
	 * 
	 * @param listener
	 *            Der neue Listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		// Falls noch kein Listener registriert ist bei dieser Klasse erstellen
		// wir zuerst den PropertyChangeSupport der die Listener verwaltet.
		if (changeSupport == null) {
			changeSupport = new PropertyChangeSupport(this);
		}
		// Listener hinzufuegen
		changeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Entfernt einen PropertyChangeListener wieder von dieser Klasse.
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
	protected void firePropChange(String propName, Object oldValue,
			Object newValue) {
		if (changeSupport != null) {
			changeSupport.firePropertyChange(propName, oldValue, newValue);	
		}
	}
}
