package org.iviPro.editors.scenegraph.subeditors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.iviPro.model.IAbstractBean;

public class TitleSelector {
	
	private Text title;
	
	public TitleSelector(Composite parent, IAbstractBean bean) {
		Group titleGroup = new Group(parent, SWT.NONE);
		titleGroup.setLayout(new GridLayout(1, false));
		GridData groupGd = new GridData();
		groupGd.grabExcessHorizontalSpace = true;
		groupGd.horizontalAlignment = SWT.FILL;
		titleGroup.setLayoutData(groupGd);
		titleGroup.setText(Messages.TitleSelector_Label_Title);
		
		title = new Text(titleGroup, SWT.SINGLE | SWT.BORDER);
		GridData titleGd = new GridData();
		titleGd.grabExcessHorizontalSpace = true;
		titleGd.horizontalAlignment = SWT.FILL;	
		title.setLayoutData(titleGd);
		if (bean.getTitle() != null) {
			title.setText(bean.getTitle());
		} else {
			title.setText("");
		}
		title.addFocusListener(new FocusListener() {			
			@Override
			public void focusLost(FocusEvent e) {
			}			
			@Override
			public void focusGained(FocusEvent e) {
				/* For some reason FocusEvent is called before MouseDownEvent.
				 * A selectAll during the FocusEvent will therefore be revoked
				 * by the later invocation of MouseDown. Using asyncExec for 
				 * selectAll ensures, that selection is executed after the queued 
				 * events (Focus and MouseDown) are finished.
				 */
				Display.getCurrent().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (!title.isDisposed()) {
							title.selectAll();
						}
					}
				});	
			}
		});
	}
	
	/**
	 * Returns the <code>Text</code> element containing the title.
	 * @return <code>Text</code> element containing the title.
	 */
	public Text getTitleText() {
		return title;
	}
	
	/**
	 * Returns the current string contained in the text element of this widget.
	 * @return title as string
	 */
	public String getTitle() {
		return title.getText();
	}
}
