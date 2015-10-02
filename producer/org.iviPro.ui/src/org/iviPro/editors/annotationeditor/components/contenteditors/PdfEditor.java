package org.iviPro.editors.annotationeditor.components.contenteditors;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.resources.PdfDocument;

public class PdfEditor extends DragAndDropEditor {
	
	private final static Class[] CONTENTCLASSES = new Class[] {PdfDocument.class};
	
	private Text summary;
	
	public PdfEditor(Composite parent, int style, PdfDocument editorContent) {
		super(parent, style, editorContent, CONTENTCLASSES);
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	protected void createComponents() {
		super.createComponents();
		// Add description elements
		Group summaryGroup = new Group(this, SWT.NONE);
		GridData groupGD = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		groupGD.heightHint = 100;
		groupGD.widthHint = PREVIEW_MAXWIDTH;
		summaryGroup.setLayoutData(groupGD);
		summaryGroup.setLayout(new GridLayout(1, false));		
		summaryGroup.setText(Messages.PdfEditor_Label_Description);
		
		summary = new Text(summaryGroup, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		GridData summaryGD = new GridData(SWT.FILL, SWT.FILL, true, true);
		summary.setLayoutData(summaryGD);
		summary.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				// Inform AbstractAnnotationDefineWidget about content description change
				SivaEvent edEvent = new SivaEvent(PdfEditor.this, 
						SivaEventType.DESCRIPTION_CHANGED, summary.getText());
				notifySivaEventConsumers(edEvent);				
			}
		});
		summary.setEnabled(content!=null);
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public void setContent(IAbstractBean newContent) {
		summary.setEnabled(newContent!=null);
		if (newContent == null) {
			summary.setText("");
		}
		super.setContent(newContent);
	};
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	protected void displayContent(IAbstractBean content) {
		String sum = ((PdfDocument)content).getSummary();
		if (sum != null) {
			summary.setText(sum);
		} else {
			summary.setText("");
		}
		previewComponent.setLabel(Messages.PdfEditor_Label_Pdf + content.getTitle());
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	protected void setInfoLabel() {
		previewComponent.setLabel( Messages.PdfEditor_Label_DragMedia);
	}
}