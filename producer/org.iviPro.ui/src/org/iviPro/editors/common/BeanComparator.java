package org.iviPro.editors.common;

import java.util.Comparator;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.resources.Scene;

/**
 * die Klasse kann aktuell zum Sortieren von Scenes und INodeAnnotations
 * verwendet werden. z.B. mit Collections.sort(List, Comparator)
 * 
 * @author juhoffma
 * 
 */
public enum BeanComparator implements Comparator<IAbstractBean> {

	SORT_BY_NAME, SORT_BY_STARTTIME, SORT_BY_ENDTIME, SORT_BY_DURATION, SORT_BY_TYPE;

	// Sortierrichtung
	private boolean sortUpwards = true;
	
	public static BeanComparator getDefault() {
		return SORT_BY_NAME;
	}
	
	public void setSortOrderUp(boolean up) {
		this.sortUpwards = up;
	}
	
	public boolean isSortUp() {
		return this.sortUpwards;
	}
	
	// liefert die Beschreibung zum Sortiertyp
	public String getDescription() {
		switch (this) {
		case SORT_BY_NAME:
			return Messages.BeanComparator_SortByName; //$NON-NLS-1$
		case SORT_BY_STARTTIME:
			return Messages.BeanComparator_SortByStarttime; //$NON-NLS-1$
		case SORT_BY_ENDTIME:
			return Messages.BeanComparator_SortByEndTime; //$NON-NLS-1$
		case SORT_BY_DURATION:
			return Messages.BeanComparator_SortByDuration; //$NON-NLS-1$
		case SORT_BY_TYPE: 
			return Messages.BeanComparator_SortByType; //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public int compare(IAbstractBean arg0, IAbstractBean arg1) {
		int direction = 1;
		if (!sortUpwards) {
			direction = -1;
		}
		switch (this) {
		case SORT_BY_NAME:
			return sortByName(arg0, arg1) * direction;
		case SORT_BY_STARTTIME:
			return sortByStartTime(arg0, arg1) * direction;
		case SORT_BY_ENDTIME:
			return sortByEndTime(arg0, arg1) * direction;
		case SORT_BY_DURATION:
			return sortByDuration(arg0, arg1) * direction;		
		case SORT_BY_TYPE:
			return sortByType(arg0, arg1) * direction;
		}
		return 0;
	}

	private int sortByName(Object arg0, Object arg1) {
		if (arg0 instanceof Scene && arg1 instanceof Scene) {
			Scene ns1 = (Scene) arg0;
			Scene ns2 = (Scene) arg1;
			return ns1.getTitle().compareTo(ns2.getTitle());
		} else if (arg0 instanceof INodeAnnotation
				&& arg1 instanceof INodeAnnotation) {
			INodeAnnotation na1 = (INodeAnnotation) arg0;
			INodeAnnotation na2 = (INodeAnnotation) arg1;
			return na1.getTitle().compareTo(na2.getTitle());
		}
		return 0;
	}

	private int sortByStartTime(Object arg0, Object arg1) {
		if (arg0 instanceof Scene && arg1 instanceof Scene) {
			Scene ns1 = (Scene) arg0;
			Scene ns2 = (Scene) arg1;
			return ns1.getStart().compareTo(ns2.getStart());
		} else if (arg0 instanceof INodeAnnotation
				&& arg1 instanceof INodeAnnotation) {
			INodeAnnotation na1 = (INodeAnnotation) arg0;
			INodeAnnotation na2 = (INodeAnnotation) arg1;
			return na1.getStart().compareTo(na2.getStart());
		}
		return 0;
	}

	private int sortByEndTime(Object arg0, Object arg1) {
		if (arg0 instanceof Scene && arg1 instanceof Scene) {
			Scene ns1 = (Scene) arg0;
			Scene ns2 = (Scene) arg1;
			return ns1.getEnd().compareTo(ns2.getEnd());
		} else if (arg0 instanceof INodeAnnotation
				&& arg1 instanceof INodeAnnotation) {
			INodeAnnotation na1 = (INodeAnnotation) arg0;
			INodeAnnotation na2 = (INodeAnnotation) arg1;
			return na1.getEnd().compareTo(na2.getEnd());
		}
		return 0;
	}
	
	private int sortByType(Object arg0, Object arg1) {
		if (arg0 instanceof Scene && arg1 instanceof Scene) {
			return 0;
		} else if (arg0 instanceof INodeAnnotation
				&& arg1 instanceof INodeAnnotation) {
			INodeAnnotation na1 = (INodeAnnotation) arg0;
			INodeAnnotation na2 = (INodeAnnotation) arg1;
			return na1.getClass().getName().compareTo(na2.getClass().getName());
		}
		return 0;
	}

	private int sortByDuration(Object arg0, Object arg1) {
		if (arg0 instanceof Scene && arg1 instanceof Scene) {
			Scene ns1 = (Scene) arg0;
			Scene ns2 = (Scene) arg1;
			Long duration1 = ns1.getEnd() - ns1.getStart();
			Long duration2 = ns2.getEnd() - ns2.getStart();
			return duration1.compareTo(duration2);
		} else if (arg0 instanceof INodeAnnotation
				&& arg1 instanceof INodeAnnotation) {
			INodeAnnotation na1 = (INodeAnnotation) arg0;
			INodeAnnotation na2 = (INodeAnnotation) arg1;
			Long duration1 = na1.getEnd() - na1.getStart();
			Long duration2 = na2.getEnd() - na2.getStart();
			return duration1.compareTo(duration2);
		}
		return 0;
	}

}
