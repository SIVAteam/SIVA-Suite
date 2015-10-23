package org.iviPro.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;

/**
 * List extension triggering {@link java.beans.PropertyChangeEvent 
 * PropertyChangeEvents} when elements of the list are added, removed or changed.
 * Apart from the events created by the <code>set</code> method, the 
 * <code>oldValue</code> of triggered events is always <code>null</code> while any
 * changes to the list are reflected by the <code>newValue</code> of an event. 
 * <p />
 * <b>Note:</b>
 * In contrast to {@link org.iviPro.model.BeanList BeanList}, which is for some
 * reason implemented as a set, this is a real (lightweight) list implementation. 
 * @author John
 */
public class NotifyingList<E> extends ArrayList<E> {

	private static final long serialVersionUID = 1L;
	
	private static final String PROP_ADDED = "Element added";
	private static final String PROP_REMOVED = "Element removed";
	private static final String PROP_CHANGED = "Element changed";
	private static final String PROP_CLEARED = "List cleared";

	private final transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	public NotifyingList() {
	}
	
	public NotifyingList(int i) {
		super(i);
	}
	
	public NotifyingList(Collection<? extends E> c) {
		super(c);
	}
	

	@Override
	public boolean add(E e) {
		boolean changed = super.add(e);
		pcs.firePropertyChange(PROP_ADDED, null, e);
		return changed;
	}

	@Override
	public void add(int index, E element) {
		super.add(index, element);
		pcs.firePropertyChange(PROP_ADDED, null, element);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean changed = super.addAll(c);
		pcs.firePropertyChange(PROP_ADDED, null, c);
		return changed;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		boolean changed = super.addAll(index, c);
		pcs.firePropertyChange(PROP_ADDED, null, c);
		return changed;
	}

	@Override
	public boolean remove(Object o) {
		boolean changed = super.remove(o);
		pcs.firePropertyChange(PROP_REMOVED, null, o);
		return changed;
	}

	@Override
	public E remove(int index) {
		E removed = super.remove(index);
		pcs.firePropertyChange(PROP_REMOVED, null, removed);
		return removed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = super.removeAll(c);
		pcs.firePropertyChange(PROP_REMOVED, null, c);
		return changed;
	}

	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		ArrayList<E> removed = new ArrayList<E>();
		for (int i=fromIndex; i<toIndex; i++) {
			removed.add(get(i));
		}
		super.removeRange(fromIndex, toIndex);
		pcs.firePropertyChange(PROP_REMOVED, null, removed);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		ArrayList<E> removed = new ArrayList<E>(this);
		removed.removeAll(c);
		boolean changed = super.retainAll(c);
		pcs.firePropertyChange(PROP_REMOVED, null, removed);
		return changed;
	}

	@Override
	public E set(int index, E element) {
		E oldValue = super.set(index, element);
		pcs.firePropertyChange(PROP_CHANGED, oldValue, element);
		return oldValue;
	}

	@Override
	public void clear() {
		super.clear();
		pcs.firePropertyChange(PROP_CLEARED, null, null);
	}		 

	public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }
}
