package org.iviPro.editors.toc;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.iviPro.editors.IAbstractEditor;
import org.iviPro.operations.OperationHistory;
import org.iviPro.theme.Colors;
import org.iviPro.theme.Icons;

public class TocEditor extends IAbstractEditor {

//	private static Logger logger = Logger.getLogger(TocEditor.class);
	private TocPanel panel;
	
	public static final String ID = TocEditor.class.getName();
	private Observer operationObserver;
	@Override
	protected void createPartControlImpl(Composite parent) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL);
		Composite root = new Composite(sc, SWT.None);
		GridLayout layout = new GridLayout(1, false);
		root.setLayout(layout);
		root.setBackground(Colors.EDITOR_BG.getColor());
		GridData rootData = new GridData();
		rootData.grabExcessHorizontalSpace = true;
		rootData.grabExcessVerticalSpace = true;
		root.setLayoutData(rootData);
		panel = new TocPanel(root, SWT.NONE);
		
		operationObserver = new Observer() {
			
			@Override
			public void update(Observable o, Object arg) {
				panel.refresh();
				
			}
		};
		sc.setContent(root);
		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		sc.setMinSize(root.computeSize(800, 650));
		OperationHistory.addOperationObserver(operationObserver);
	}
	
	@Override
	public Image getDefaultImage() {
		return Icons.EDITOR_TOC.getImage();
	}

	@Override
	public void dispose() {
		OperationHistory.removeOperationObserver(operationObserver);
		super.dispose();
	}

	@Override
	public void doSave(IProgressMonitor arg0) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(Messages.TocEditor_EditorTitle);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setFocus() {
		panel.refresh();
	}

}
