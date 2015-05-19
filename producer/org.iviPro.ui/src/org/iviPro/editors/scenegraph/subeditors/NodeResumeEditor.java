package org.iviPro.editors.scenegraph.subeditors;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.iviPro.model.graph.NodeResume;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.graph.ModifyNodeResumeOperation;

public class NodeResumeEditor extends AbstractNodeEditor {
	
	private NodeResume resume;
	private Spinner fieldTimeout;
	private Button timeoutButton;
	
	private static int WIDTH = 300;
	
	public NodeResumeEditor(NodeResume resume) {
		super(Messages.NodeResumeEditor_Shell_Title, 
				resume.getNodeID(), WIDTH);
		this.resume = resume;
		
		// TIMEOUT
		Group timeoutGroup = new Group(contentComposite, SWT.NONE);
		timeoutGroup.setLayout(new GridLayout(2, false));
		GridData groupGd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		timeoutGroup.setLayoutData(groupGd);		
		timeoutGroup.setText(Messages.NodeResumeEditor_Group_Timeout);

		Composite buttonComp = new Composite(timeoutGroup, SWT.NONE);
		buttonComp.setLayout(new GridLayout(2, false));

		timeoutButton = new Button(buttonComp, SWT.CHECK);	
		timeoutButton.setSelection(resume.useTimeout());		
		Label label = new Label(buttonComp, SWT.CENTER);
		label.setText(Messages.NodeResumeEditor_Button_Timeout);


		fieldTimeout = new Spinner(timeoutGroup, SWT.SINGLE | SWT.BORDER);
		GridData timeoutGd = new GridData();
		timeoutGd.grabExcessHorizontalSpace = false;
		timeoutGd.horizontalAlignment = SWT.FILL;
		fieldTimeout.setLayoutData(timeoutGd);
		fieldTimeout.setMinimum(1);
		fieldTimeout.setSelection(resume.getTimeout());
		fieldTimeout.setEnabled(timeoutButton.getSelection());

		timeoutButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				fieldTimeout.setEnabled(timeoutButton.getSelection());
			}			
		});
	}

	/** 
	 * The resume editor needs no validation. 
	 */
	@Override
	protected boolean validateInput() {
		return true;
	}

	@Override
	protected void executeChangeOperation() {
		ModifyNodeResumeOperation op = new ModifyNodeResumeOperation(
				resume, resume.getTitle(),
				fieldTimeout.getSelection(), timeoutButton.getSelection());
		try {
			OperationHistory.execute(op);
		} catch (ExecutionException e) {
			Shell shell = Display.getCurrent().getActiveShell();
			MessageDialog.openError(shell,
					Messages.Common_ErrorDialog_Title, e
							.getMessage());
		}
	}
}
