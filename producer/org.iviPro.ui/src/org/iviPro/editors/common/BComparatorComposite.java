package org.iviPro.editors.common;

import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.theme.Icons;

/**
 * Composite zur Auswahl der Sortiermöglichkeiten
 * Klassen die Bean-Sortierung unterstützen wollen, können sich als Listener an der 
 * Klasse anmelden und werden entsprechend benachrichtigt
 * @author juhoffma
 *
 */
public class BComparatorComposite extends SivaComposite {
	
	private String sortTooltip = "Sort descending";
	public BComparatorComposite(Composite parent, int style, List<BeanComparator> comparators) {
		super(parent, style);
		setLayout(new GridLayout(3, false));
		createSortSelection(parent, comparators);
	}
	
	public BComparatorComposite(Composite parent, int style, String sortTooltip, List<BeanComparator> comparators) {
		super(parent, style);
		this.sortTooltip = sortTooltip;
		setLayout(new GridLayout(3, false));
		createSortSelection(parent, comparators);
		
	}
	
	// erstellt eine Toolbar zur Auswahl der Sortierung
	private void createSortSelection(Composite parent, List<BeanComparator> comparators) {
		
		Label label = new Label(this, SWT.CENTER);
		label.setText(Messages.Label_Sort_By);
		
		// Mapping zwischen Auswahl und BeanComparator
		final HashMap<Integer, BeanComparator> bcMap = new HashMap<Integer, BeanComparator>();
				
		final Combo combo = new Combo (this, SWT.CENTER | SWT.READ_ONLY);
		final Button switchDir = new Button(this, SWT.PUSH);
		switchDir.setImage(Icons.SORT_DIRECTION_ASC.getImage());
		switchDir.setToolTipText(sortTooltip);
		switchDir.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {				
			}

			@Override
			public void widgetSelected(SelectionEvent se) {	
				BeanComparator selectBC = bcMap.get(combo.getSelectionIndex());
				if (selectBC.isSortUp()) {
					selectBC.setSortOrderUp(false);
					switchDir.setImage(Icons.SORT_DIRECTION_DESC.getImage());
				} else {
					selectBC.setSortOrderUp(true);
					switchDir.setImage(Icons.SORT_DIRECTION_ASC.getImage());
				}
				notifySivaEventConsumers(new SivaEvent(BComparatorComposite.this, SivaEventType.BEANCOMPARATOR_CHANGED, selectBC));
				
			}			
		});
		
		// die Standardauswahl, definiert in BeanComparator (getDefault())
		int stdSelection=0;
		for (int i = 0; i < comparators.size(); i++) {
			BeanComparator bc = comparators.get(i);
			combo.add(bc.getDescription());
			bcMap.put(i, bc);
			if (bc.equals(BeanComparator.getDefault())) {
				stdSelection = i;
			}
		}
		
		combo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {				
			}

			@Override
			public void widgetSelected(SelectionEvent se) {
				BeanComparator selectBC = bcMap.get(combo.getSelectionIndex());
				selectBC.setSortOrderUp(true);
				switchDir.setAlignment(SWT.UP);
				notifySivaEventConsumers(new SivaEvent(BComparatorComposite.this, SivaEventType.BEANCOMPARATOR_CHANGED, selectBC));				
			}			
		});
		combo.select(stdSelection);
	}
	
}
