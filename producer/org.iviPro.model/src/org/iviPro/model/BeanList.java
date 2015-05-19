package org.iviPro.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Diese Klasse kapselt ein Listen-Objekt, welches mittels
 * PropertyChangeListener auf Aenderungen ueberwachbar ist.
 * 
 * @author dellwo
 * 
 * @param <E>
 *            Der Typ der Listen-Elemente
 */
public class BeanList<E> extends IAbstractBean implements List<E> {

	/**
	 * Property-Name fuer ChangeEvents, die das Einfuegen eines Elements in die
	 * Liste uebermitteln. Das eingefuegte Element ist als newValue im
	 * PropertyChangeEvent gesetzt, oldValue ist null.
	 */
	public static final String PROP_ITEM_ADDED = "PROP_ITEM_ADDED"; //$NON-NLS-1$

	/**
	 * Property-Name fuer ChangeEvents, die das Entfernen eines Elements aus der
	 * Liste uebermitteln. Das entfernte Element ist als newValue im
	 * PropertyChangeEvent gesetzt, oldValue ist null.
	 */
	public static final String PROP_ITEM_REMOVED = "PROP_ITEM_REMOVED"; //$NON-NLS-1$

	/**
	 * Die eigentliche Liste zur Datenhaltung
	 */
	private List<E> list;

	/**
	 * Erstellt eine neue leere Bean-Liste
	 */
	public BeanList(Project project) {
		super("n/a", project); //$NON-NLS-1$
		init();
	}

	/**
	 * Initalisiert das Objekt. Nur zum Aufruf von den Konstruktoren gedacht.
	 */
	private void init() {
		list = new ArrayList<E>();
	}

	@Override
	public String toString() {
		return list.toString();
	}

	// #######################################################################
	// # METHODEN DIE ZU VERAENDERUNG DER LISTE FUEHREN
	// #######################################################################

	@Override
	public boolean add(E element) {
		boolean changed = false;
		if (!list.contains(element)) {
			changed = list.add(element);
			firePropertyChange(PROP_ITEM_ADDED, null, element);
		}
		return changed;
	}

	@Override
	public void add(int index, E element) {
		// TODO: Vetoable machen?
		if (!list.contains(element)) {
			list.add(index, element);
			firePropertyChange(PROP_ITEM_ADDED, null, element);
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		// TODO: Vetoable machen?
		boolean changed = false;
		for (E element : c) {
			changed = changed | add(element);
		}
		return changed;
	}

	@Override
	public boolean remove(Object o) {
		// TODO: Vetoable machen?
		boolean changed = list.remove(o);
		firePropertyChange(PROP_ITEM_REMOVED, null, o);
		return changed;
	}

	@Override
	public E remove(int index) {
		// TODO: Vetoable machen?
		E removed = list.remove(index);
		firePropertyChange(PROP_ITEM_REMOVED, null, removed);
		return removed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO: Vetoable machen?
		boolean changed = false;
		for (Object o : c) {
			changed = changed | remove(o);
		}
		return changed;
	}

	@Override
	public void clear() {
		// TODO: Vetoable machen?
		while (!list.isEmpty()) {
			remove(0);
		}
	}

	// #######################################################################
	// # METHODEN DIE KEINE VERAENDERUNG DER LISTE BEWIRKEN
	// #######################################################################

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public E get(int index) {
		return list.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return list.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	// #######################################################################
	// # ABER HIER: NICHT UNTERSTUETZTE METHODEN!
	// #######################################################################

	/**
	 * DONT USE: THIS METHOD IS NOT IMPLEMENTED!<br>
	 * An UnsupportedOperationException will be thrown if called.
	 */
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException("Not implemented."); //$NON-NLS-1$
	}

	/**
	 * DONT USE: THIS METHOD IS NOT IMPLEMENTED!<br>
	 * An UnsupportedOperationException will be thrown if called.
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Not implemented."); //$NON-NLS-1$
	}

	/**
	 * DONT USE: THIS METHOD IS NOT IMPLEMENTED!<br>
	 * An UnsupportedOperationException will be thrown if called.
	 */
	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException("Not implemented."); //$NON-NLS-1$
	}

}
