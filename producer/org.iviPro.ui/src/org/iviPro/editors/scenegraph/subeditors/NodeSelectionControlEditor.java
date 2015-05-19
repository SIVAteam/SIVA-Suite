package org.iviPro.editors.scenegraph.subeditors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.iviPro.application.Application;
import org.iviPro.model.BeanList;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.graph.AbstractNodeSelectionControl;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeCondSelectionControl;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.resources.Picture;
import org.iviPro.operations.CompoundOperation;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.graph.ModifyAbstractNodeSelectionControlOperation;
import org.iviPro.operations.graph.ModifyNodeCondSelectionControlOperation;

public class NodeSelectionControlEditor extends TitledNodeEditor {
	private final AbstractNodeSelectionControl selectionControl;

	private final List<Picture> picturesInProject;

	private Combo fieldImage;
	private Button hideButton;
	private org.eclipse.swt.widgets.List sceneList;
	private ArrayList<NodeScene> scenes = new ArrayList<NodeScene>();
	private Text fieldMessage;
	
	
	private static int WIDTH = 300;

	public NodeSelectionControlEditor(AbstractNodeSelectionControl selectionControl) {
		super(Messages.NodeSelectionControlEditor_Shell_Title, 
				selectionControl, WIDTH);
		this.selectionControl = selectionControl;
		List<Picture> pictures = new ArrayList<Picture>();
		BeanList<IAbstractBean> mediaObjects = Application.getCurrentProject()
				.getMediaObjects();
		for (IAbstractBean mediaObj : mediaObjects) {
			if (mediaObj instanceof Picture) {
				pictures.add((Picture) mediaObj);
			}
		}
		picturesInProject = pictures;
		
		createImageSelector(contentComposite);
		if (selectionControl instanceof NodeCondSelectionControl) {
			createSceneConditions(contentComposite);
		}
	}

	private void createImageSelector(Composite parent) {
		Group imageGroup = new Group(parent, SWT.NONE);
		imageGroup.setLayout(new GridLayout(1, false));
		GridData groupGd = new GridData();
		groupGd.grabExcessHorizontalSpace = true;
		groupGd.horizontalAlignment = SWT.FILL;
		imageGroup.setLayoutData(groupGd);
		imageGroup.setText(Messages.NodeSelectionControlEditor_Label_Image);
		
		fieldImage = createImageCombo(imageGroup);
		GridData imageGd = new GridData();
		imageGd.horizontalSpan = 2;
		imageGd.grabExcessHorizontalSpace = true;
		imageGd.horizontalAlignment = SWT.FILL;
		fieldImage.setLayoutData(imageGd);
	}

	private void createSceneConditions(Composite parent) {
		Group condGroup = new Group(parent, SWT.NONE);
		condGroup.setLayout(new GridLayout(1, false));		
		GridData condGroupGd = new GridData();
		condGroupGd.grabExcessHorizontalSpace = true;
		condGroupGd.horizontalAlignment = SWT.FILL;
		condGroup.setLayoutData(condGroupGd);
		condGroup.setText(Messages.NodeSelectionControlEditor_Condition_Group);
		
		Composite visibileComp = new Composite(condGroup, SWT.NONE);
		visibileComp.setLayout(new GridLayout(2, false));		
		hideButton = new Button(visibileComp, SWT.CHECK);
		hideButton.setSelection(!selectionControl.isVisible());
		Label visibleLable = new Label(visibileComp, SWT.LEFT);
		visibleLable.setText(Messages.NodeSelectionControlEditor_Condition_Visibility);		
				
		NodeCondSelectionControl condControl = 
				(NodeCondSelectionControl) selectionControl;
		Group sceneGroup = new Group(condGroup, SWT.NONE);
		sceneGroup.setLayout(new GridLayout(1, false));
		GridData sceneGroupGd = new GridData();
		sceneGroupGd.grabExcessHorizontalSpace = true;
		sceneGroupGd.horizontalAlignment = SWT.FILL;
		sceneGroup.setLayoutData(sceneGroupGd);
		sceneGroup.setText(Messages.NodeSelectionControlEditor_Group_Prerequisite_Scenes);
		
		sceneList = new org.eclipse.swt.widgets.List(sceneGroup, 
				SWT.MULTI | SWT.V_SCROLL);
		GridData listGd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		listGd.heightHint = sceneList.getItemHeight() * 4;
		sceneList.setLayoutData(listGd);
		List<IGraphNode> nodes = 
				Application.getCurrentProject().getSceneGraph().getNodes();
		Collections.sort(nodes, new Comparator<IGraphNode>() {
			@Override
			public int compare(IGraphNode o1, IGraphNode o2) {
				return o1.getTitle().compareTo(o2.getTitle());
			}
		});
		
		for (IGraphNode n : nodes) {
			if (n instanceof NodeScene) {
				scenes.add(((NodeScene)n));
				sceneList.add(n.getTitle());
				if (condControl.getPrerequisiteScenes().contains(n)) {
					sceneList.select(sceneList.getItemCount()-1);
				}
			}
		}
		
		Group msgGroup = new Group(condGroup, SWT.NONE);
		msgGroup.setLayout(new GridLayout(1, false));
		GridData msgGroupGd = new GridData();
		msgGroupGd.grabExcessHorizontalSpace = true;
		msgGroupGd.horizontalAlignment = SWT.FILL;
		msgGroup.setLayoutData(msgGroupGd);
		msgGroup.setText(Messages.NodeSelectionControlEditor_Condition_Message);
		fieldMessage = new Text(msgGroup, SWT.SINGLE | SWT.BORDER);
		fieldMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, 
				true, false));
		String msg = selectionControl.getDescription();
		if (msg == null) {
			msg = ""; //$NON-NLS-1$
		}
		fieldMessage.setText(msg);
		
				
		// Need to discuss details of conditional statements
		// before implementation
//		Group exprGroup = new Group(parent, SWT.NONE);
//		exprGroup.setLayout(new GridLayout(1, false));		
//		GridData exprGroupGd = new GridData();
//		exprGroupGd.grabExcessHorizontalSpace = true;
//		exprGroupGd.horizontalAlignment = SWT.FILL;
//		exprGroup.setLayoutData(exprGroupGd);
//		exprGroup.setText(Messages.NodeSelectionControlEditor_Group_Conditional_Expressions);
	}
	
	private Combo createImageCombo(Composite parent) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		int selIndex = 0;
		String[] comboItems = new String[picturesInProject.size() + 1];
		comboItems[0] = ""; //$NON-NLS-1$
		for (int i = 0; i < picturesInProject.size(); i++) {
			Picture pic = picturesInProject.get(i);
			comboItems[i + 1] = pic.getTitle();
			if (pic == selectionControl.getButtonImage()) {
				selIndex = i+1;
			}
		}
		combo.setItems(comboItems);
		combo.select(selIndex);
		return combo;
	}
	
	@Override
	protected boolean validateInput() {
		if (titleComp.getTitle().isEmpty() &&
				fieldImage.getSelectionIndex()==0) {
			showWarning(Messages.NodeSelectionControlEditor_WarnMessage);
			return false;
		}
		return true;
	}
	
	@Override
	protected void executeChangeOperation() {
		Picture newImage = null;
		if (fieldImage.getEnabled()) {
			int selImageIdx = fieldImage.getSelectionIndex() - 1;
			if (selImageIdx >= 0) {
				newImage = picturesInProject.get(selImageIdx);
			} else {
				newImage = null;
			}
		}
		
		ModifyAbstractNodeSelectionControlOperation standardOp = 
				new ModifyAbstractNodeSelectionControlOperation(
						selectionControl, titleComp.getTitle(), newImage);
		
		CompoundOperation<IAbstractOperation> compOp =
				new CompoundOperation<IAbstractOperation>(Messages.NodeSelectionControlEditor_Compound_Modification_Operation);
		compOp.addOperation(standardOp);
		
		// Create modify operation needed for conditional selection controls
		ArrayList<NodeScene> prerequisiteScenes = new ArrayList<NodeScene>();
		if (selectionControl instanceof NodeCondSelectionControl) {
			for (int i : sceneList.getSelectionIndices()) {
				prerequisiteScenes.add(scenes.get(i));
			}
			String newMsg = fieldMessage.getText();
			if (newMsg.isEmpty()) {
				newMsg = null;
			}
			
			ModifyNodeCondSelectionControlOperation condOp = 
					new ModifyNodeCondSelectionControlOperation(
							(NodeCondSelectionControl)selectionControl,
							!hideButton.getSelection(),	newMsg,
							prerequisiteScenes);
			compOp.addOperation(condOp);
		}
		
		try {
			OperationHistory.execute(compOp);
		} catch (ExecutionException e) {
			Shell shell = Display.getCurrent().getActiveShell();
			MessageDialog.openError(shell, 
					Messages.Common_ErrorDialog_Title, e.getMessage());
		}
	}
}
