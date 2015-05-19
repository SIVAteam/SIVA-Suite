package org.iviPro.editors.toc;


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class TocPanel extends Composite {
	
//	private static Logger logger = Logger.getLogger(TocPanel.class);
	
	
	private ControlPanel control;
	private TreePanel trees;

	public TocPanel(Composite parent, int style) {
		super(parent, style);
		this.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_WHITE));
		
		GridLayout layout = new GridLayout(1, false);
		setLayout(layout);				
		control = new ControlPanel(this, SWT.None);

		trees = new TreePanel(this, SWT.None);
		control.setTC(trees);
		
	}
	
	public void refresh() {
		if (trees != null && !isDisposed()) {
			trees.drawTree();
			trees.drawSceneList();
		}
	}

}
