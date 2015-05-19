package org.iviPro.editors.annotationeditor.components.contenteditors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.iviPro.editors.common.SivaComposite;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;

/**
 * wird für Text Annotationen verwendet
 * @author juhoffma
 */
public class TextEditor extends SivaComposite {

	private Text editor;
	
	public TextEditor(Composite parent, int style, String text) {
		super(parent, style);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setLayout(new GridLayout(1, false));	
		editor = new Text(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		editor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		editor.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				SivaEvent edEvent = new SivaEvent(TextEditor.this, SivaEventType.EDITOR_CHANGED, editor.getText());
				notifySivaEventConsumers(edEvent);
			}			
		});
		
		setText(text);					
	}
	
	public void setText(String text) {
		if (editor != null) {
			if (text != null) {
				editor.setText(text);
				editor.setTopIndex(text.length());
			} else {
				editor.setText(""); //$NON-NLS-1$
			}
		}
	}
}