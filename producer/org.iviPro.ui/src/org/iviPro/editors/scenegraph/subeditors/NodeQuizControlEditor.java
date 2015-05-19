package org.iviPro.editors.scenegraph.subeditors;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.graph.NodeQuizControl;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.graph.ModifyNodeQuizControlOperation;

public class NodeQuizControlEditor extends AbstractNodeEditor {

	private final NodeQuizControl quizControl;

	private Text fieldMinValue;
	private Text fieldMaxValue;
	
	private static int WIDTH = 300;

	public NodeQuizControlEditor(NodeQuizControl quizControl) {
		super(Messages.NodeQuizControlEditor_Shell_Title, 
				quizControl.getNodeID(), WIDTH);
		this.quizControl = quizControl;
		
		GridData gd;

		// Header
		// boolean imageMode = selectionControl.getButtonImage() != null;
		gd = new GridData();
		gd.horizontalSpan = 3;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;

		String minValue = Integer.toString(quizControl.getMinValue());
		String maxValue = Integer.toString(quizControl.getMaxValue());

		// Text Field
		final Group minGroup = new Group(contentComposite, SWT.NONE);
		minGroup.setLayout(new GridLayout(1, false));
		minGroup.setText(Messages.NodeQuizControlEditor_Label_MinAmount);

		GridData minGroupGd = new GridData();
		minGroupGd.grabExcessHorizontalSpace = true;
		minGroupGd.horizontalAlignment = SWT.FILL;
		minGroup.setLayoutData(minGroupGd);
		fieldMinValue = new Text(minGroup, SWT.SINGLE | SWT.BORDER);
		fieldMinValue.setText(minValue);
		GridData minFieldGd = new GridData();
		minFieldGd.grabExcessHorizontalSpace = true;
		minFieldGd.horizontalAlignment = SWT.FILL;
		fieldMinValue.setLayoutData(minFieldGd);

		final Group maxGroup = new Group(contentComposite, SWT.NONE);
		maxGroup.setLayout(new GridLayout(1, false));
		maxGroup.setText(Messages.NodeQuizControlEditor_Label_MaxAmount);
		// buttonImage.setSelection(imageMode);
		GridData maxGroupGd = new GridData();
		maxGroupGd.grabExcessHorizontalSpace = true;
		maxGroupGd.horizontalAlignment = SWT.FILL;
		maxGroup.setLayoutData(maxGroupGd);
		fieldMaxValue = new Text(maxGroup, SWT.SINGLE | SWT.BORDER);
		fieldMaxValue.setText(maxValue);
		GridData maxFieldGd = new GridData();
		maxFieldGd.grabExcessHorizontalSpace = true;
		maxFieldGd.horizontalAlignment = SWT.FILL;
		fieldMaxValue.setLayoutData(maxFieldGd);
	}

	private boolean checkIntersection(String minValue, String maxValue) {
		
		int min = Integer.parseInt(minValue);
		int max = Integer.parseInt(maxValue);

		if (quizControl.getParents().isEmpty()) {
			// if quizControl has no parents, intersection is ok
			return true;
		} else {
			// find all parent nodeQuiz
			for (IGraphNode parentNode : quizControl.getParents()) {
				if (parentNode instanceof NodeQuiz) {
					// check all of the parents quizControl children (different
					// from this quizControl) for intersection
					for (IGraphNode childNode : parentNode.getChildren()) {
						if (childNode != quizControl
								&& childNode instanceof NodeQuizControl) {
							NodeQuizControl childQuizControl = (NodeQuizControl) childNode;
							// check for intersection
							if ((childQuizControl.getMinValue() <= min && min <= childQuizControl
									.getMaxValue())
									|| childQuizControl.getMinValue() <= max
									&& max <= childQuizControl.getMaxValue()) {
								return false;
							}
						}
					}
				}
			}
		}
		// no intersection found
		return true;
	}

	private boolean checkInputData(String minString, String maxString) {
		int min, max;
		try {
			min = Integer.parseInt(minString);
			max = Integer.parseInt(maxString);
		} catch (NumberFormatException nfe) {
			return false;
		}
		if (min > max) {
			return false;
		}
		return true;
	}

	@Override
	protected boolean validateInput() {
		if (!checkInputData(fieldMinValue.getText(), fieldMaxValue.getText())) {
			showWarning(Messages.NodeQuizControlEditor_ValuesWarnMessage);
			return false;
		}
		if (!checkIntersection(fieldMinValue.getText(), fieldMaxValue.getText())) {
			showWarning(Messages.NodeQuizControlEditor_IntersectionWarnMessage);
			return false;
		}
		return true;
	}

	@Override
	protected void executeChangeOperation() {
		String newAmount = fieldMinValue.getText() + "-" //$NON-NLS-1$
				+ fieldMaxValue.getText();

		ModifyNodeQuizControlOperation op = new ModifyNodeQuizControlOperation(
				quizControl, newAmount);
		try {
			OperationHistory.execute(op);
		} catch (ExecutionException e) {
			Shell shell = Display.getCurrent().getActiveShell();
			MessageDialog.openError(shell,
					Messages.Common_ErrorDialog_Title, e.getMessage());
		}		
	}
}