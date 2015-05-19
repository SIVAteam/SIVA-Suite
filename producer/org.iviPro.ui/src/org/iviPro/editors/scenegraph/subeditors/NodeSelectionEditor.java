package org.iviPro.editors.scenegraph.subeditors;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.iviPro.model.graph.AbstractNodeSelection;
import org.iviPro.model.graph.AbstractNodeSelectionControl;
import org.iviPro.model.graph.ButtonType;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.graph.ModifyAbstractNodeSelectionOperation;

public class NodeSelectionEditor extends TitledNodeEditor {
	private HashMap<Integer, AbstractNodeSelectionControl> controlRanks;
	private final AbstractNodeSelection selection;
	private Combo fieldType;
	private Combo fieldDefaultControl;
	private Spinner fieldTimeout;
	private Button stdButton;
	private Button upButton;
	private Button downButton;
	private List pathList;
	
	private static int WIDTH = 300;
	
	
	public NodeSelectionEditor(AbstractNodeSelection selection) {
		super(Messages.NodeSelectionEditor_Shell_Title,
				selection, WIDTH);
		this.selection = selection;
		java.util.List<IGraphNode> controls = 
				selection.getChildren(AbstractNodeSelectionControl.class);
		controlRanks = new HashMap<Integer,
				AbstractNodeSelectionControl>(controls.size(), 1);
		for (IGraphNode child : controls) {
			AbstractNodeSelectionControl control = 
					(AbstractNodeSelectionControl)child;
			controlRanks.put(control.getRank(), control);
		}
		
		createRankingControls(contentComposite);
		
		createStandardPathControls(contentComposite);
	}

	private void createRankingControls(Composite parent) {
		// Ranking of SelectionControls
		Group rankingGroup = new Group(parent, SWT.NONE);
		rankingGroup.setText(Messages.NodeSelectionEditor_Group_Order);
		rankingGroup.setLayout(new GridLayout(2, false));
		rankingGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));

		// Ranking: List of paths
		pathList = new List(rankingGroup, SWT.SINGLE | SWT.V_SCROLL);
		GridData pathListData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		pathListData.heightHint = pathList.getItemHeight() * 4;
		pathList.setLayoutData(pathListData);
		for (int i=1; i <= controlRanks.size(); i++) {
			pathList.add(truncatedCompleteTitle(controlRanks.get(i), 32));
		}

		pathList.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				changeButtonStatus();
			}	

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

		});

		// Ranking: Buttons for moving items up and down
		Composite buttonComp = new Composite(rankingGroup, SWT.NONE);
		buttonComp.setLayout(new GridLayout(1, false));
		upButton = new Button(buttonComp, SWT.FLAT);
		upButton.setText(Messages.NodeSelectionEditor_Button_Up);
		downButton = new Button(buttonComp, SWT.PUSH);
		downButton.setText(Messages.NodeSelectionEditor_Button_Down);
		int bWidth = Math.max(upButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
				downButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
		GridData upData = new GridData();
		upData.widthHint = bWidth;
		upButton.setLayoutData(upData);
		GridData downData = new GridData();
		downData.widthHint = bWidth;
		downButton.setLayoutData(downData);
		upButton.setEnabled(false);
		downButton.setEnabled(false);
		upButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				int selectionIndex = pathList.getSelectionIndex();
				// Index and rank are differ by one!
				AbstractNodeSelectionControl selected = 
						controlRanks.get(selectionIndex + 1);
				controlRanks.put(selectionIndex + 1 ,
						controlRanks.get(selectionIndex));
				controlRanks.put(selectionIndex, selected);

				pathList.remove(selectionIndex);
				pathList.add(truncatedCompleteTitle(selected, 32),
						selectionIndex - 1);
				pathList.setSelection(selectionIndex - 1);
				changeButtonStatus();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

		});

		downButton.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = pathList.getSelectionIndex();
				// Index and rank are differ by one!
				AbstractNodeSelectionControl selected = 
						controlRanks.get(selectionIndex + 1);
				controlRanks.put(selectionIndex + 1, 
						controlRanks.get(selectionIndex + 2));
				controlRanks.put(selectionIndex + 2, selected);

				pathList.remove(selectionIndex);
				pathList.add(truncatedCompleteTitle(selected, 32),
						selectionIndex + 1);
				pathList.setSelection(selectionIndex + 1);
				changeButtonStatus();
			}


			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

		});
	}
	
	private void createStandardPathControls(Composite parent) {
		// Standard path
		Group stdGroup = new Group(parent, SWT.CENTER);
		stdGroup.setText(Messages.NodeSelectionEditor_Group_DefaultPath);
		stdGroup.setLayout(new GridLayout(2, false));
		GridData stdGroupGd = new GridData();
		stdGroupGd.horizontalSpan = 1;
		stdGroupGd.horizontalAlignment = SWT.FILL;
		stdGroup.setLayoutData(stdGroupGd);
		Composite stdComposite = new Composite(stdGroup, SWT.CENTER);
		stdComposite.setLayout(new GridLayout(2, false));
		stdComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 2, 1));

		// Button zum Verwenden des Standard Pfads
		stdButton = new Button(stdComposite, SWT.CHECK);	
		if (selection.getDefaultControl() != null) {
			stdButton.setSelection(true);
		}
		Label label = new Label(stdComposite, SWT.CENTER);
		label.setText(Messages.NodeSelectionEditor_Label_DefaultPath);
		
		// TYPE
		/* TODO wird aktuell nicht vom Player unterstützt => nicht anbieten
		label = new Label(parent, SWT.NONE);
		label.setText(Messages.NodeSelectionEditor_FieldType);
		fieldType = createTypeCombo(parent);
		fieldType.pack();
		*/
		

		// DEFAULT CONTROL
		Group stdSubGroup = new Group(stdGroup, SWT.NONE);
		stdSubGroup.setLayout(new GridLayout(1, false));
		GridData stdSubGroupGd = new GridData();
		stdSubGroupGd.grabExcessHorizontalSpace = true;
		stdSubGroupGd.horizontalAlignment = SWT.FILL;
		stdSubGroup.setLayoutData(stdSubGroupGd);
		stdSubGroup.setText(Messages.NodeSelectionEditor_Group_Subgroup_DefaultPath);
		fieldDefaultControl = createDefaultControlCombo(stdSubGroup);
		GridData stdFieldGd = new GridData();
		stdFieldGd.grabExcessHorizontalSpace = true;
		stdFieldGd.horizontalAlignment = SWT.FILL;
		fieldDefaultControl.setLayoutData(stdFieldGd);
		fieldDefaultControl.setEnabled(stdButton.getSelection());
		fieldDefaultControl.pack();

		// TIMEOUT
		Group timeoutGroup = new Group(stdGroup, SWT.NONE);
		timeoutGroup.setLayout(new GridLayout(1, false));
		GridData timeoutGd = new GridData();
		timeoutGd.grabExcessHorizontalSpace = false;
		timeoutGd.horizontalAlignment = SWT.FILL;
		timeoutGroup.setLayoutData(timeoutGd);		
		timeoutGroup.setText(Messages.NodeSelectionEditor_Label_Timeout);
		fieldTimeout = new Spinner(timeoutGroup, SWT.SINGLE | SWT.BORDER);
		fieldTimeout.setMinimum(1);
		fieldTimeout.setSelection(selection.getTimeout());
		fieldTimeout.setEnabled(false);
		GridData timeoutFieldGd = new GridData();
		timeoutFieldGd.grabExcessHorizontalSpace = false;
		timeoutFieldGd.horizontalAlignment = SWT.FILL;
		fieldTimeout.setLayoutData(timeoutFieldGd);
		fieldTimeout.setEnabled(stdButton.getSelection());
		fieldTimeout.pack();

		stdButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				fieldDefaultControl.setEnabled(stdButton.getSelection());
				fieldTimeout.setEnabled(stdButton.getSelection());
			}			
		});
	}
	
	private String truncatedCompleteTitle(AbstractNodeSelectionControl control,
			int length) {
		String title = control.getTitle();
		if (control.getButtonImage() != null) {
			title += "/"  //$NON-NLS-1$
					+ control.getButtonImage().getTitle();
		}
		if (title.length() > length) {
			title = title.substring(0, length-3) 
					+ "..."; //$NON-NLS-1$
		}
		return title;
	}
	
	/**
	 * Changes the status of the up and down buttons according to the item
	 * selected in the path list.
	 */
	private void changeButtonStatus(){
		if (pathList.getSelectionIndex() > 0) {
			upButton.setEnabled(true);
		} else {
			upButton.setEnabled(false);
		}
		if (pathList.getSelectionIndex() < pathList.getItemCount()-1) {
			downButton.setEnabled(true);
		} else {
			downButton.setEnabled(false);
		}
	}
	
	private Combo createDefaultControlCombo(Composite parent) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		int selIndex = 0;
		java.util.List<AbstractNodeSelectionControl> controls = 
				selection.getControls();
		AbstractNodeSelectionControl defaultControl = 
				selection.getDefaultControl();
		String[] comboItems = new String[controls.size()];
		Iterator<AbstractNodeSelectionControl> iter = controls.iterator();
		for (int i=0; i<controls.size(); i++) {
			AbstractNodeSelectionControl control = iter.next();
			comboItems[i] = truncatedCompleteTitle(control, 26);
			if (control == defaultControl) {
				selIndex = i;
			}
		}
		combo.setItems(comboItems);
		combo.select(selIndex);
		return combo;
	}

	private Combo createTypeCombo(Composite parent) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		int selIndex = 0;
		ButtonType[] types = ButtonType.values();
		String[] comboItems = new String[types.length];
		for (int i = 0; i < types.length; i++) {
			comboItems[i] = types[i].toString();
			if (types[i].equals(selection.getButtonType())) {
				selIndex = i;
			}
		}
		combo.setItems(comboItems);
		combo.select(selIndex);
		return combo;
	}
	
	@Override
	protected void executeChangeOperation() {
		
		AbstractNodeSelectionControl newDefaultControl = null;
		int defaultControlIndex = fieldDefaultControl.getSelectionIndex();
		if (stdButton.getSelection()) {
			newDefaultControl = selection.getControls().get(defaultControlIndex);
		}
		
		// Auslesen: Button type
		int typeIndex = 0;
		if (fieldType != null) {
			typeIndex = fieldType.getSelectionIndex();
		}
		ButtonType newType = ButtonType.values()[typeIndex];
		
		ModifyAbstractNodeSelectionOperation op = 
				new ModifyAbstractNodeSelectionOperation(selection, 
						titleComp.getTitle(), controlRanks, newDefaultControl,
						newType, fieldTimeout.getSelection(), stdButton.getSelection());
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
