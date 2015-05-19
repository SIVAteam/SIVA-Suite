package org.iviPro.projectsettings;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public class SemanticZoom extends Composite {

	public boolean fullSemanticZoomLevels;
	private Button szCheck;

	public SemanticZoom(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(1, false));
		
		
		Composite szComp = new Composite(this, SWT.NONE);
		szComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		szComp.setLayout(new GridLayout(2, false));
		szCheck = new Button(szComp, SWT.CHECK);
		szCheck.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				fullSemanticZoomLevels = szCheck.getSelection();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		Label szLabel = new Label(szComp, SWT.NONE);
		szLabel.setText("Ganzheitliches Stufen-Zooming aktivieren");
		
		Label szWarning = new Label(this, SWT.None);
		szWarning.setText("ACHTUNG: Das Deaktivieren dieser Funktion führt eventuell zu einer Vergrößerung des Graphen!");
	}
	
	public void init() {
		fullSemanticZoomLevels = false;
		szCheck.setSelection(fullSemanticZoomLevels);
		notifyListeners(SWT.Modify, new Event());
	}
	
	public void setFullZoomLevels(boolean fullZoomLevels) {
		this.fullSemanticZoomLevels = fullZoomLevels;
		szCheck.setSelection(fullZoomLevels);
	}
}
