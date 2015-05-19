package org.iviPro.editors.scenegraph.actions;

import java.util.List;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.iviPro.editors.scenegraph.editparts.IEditPartNode;
import org.iviPro.editors.scenegraph.editparts.EditPartNodeStart;
import org.iviPro.editors.scenegraph.editparts.Messages;
import org.iviPro.model.IAbstractBean;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.global.ChangeTitleOperation;
import org.iviPro.theme.Icons;

/**
 * Action, die das gerade selektierte Objekt umbenennt. Dazu muss exakt ein
 * umbennenbarer Edit-Part selektiert sein.
 * 
 * @author dellwo
 * 
 */
public class NodeEditAction extends SelectionAction {
	
	public static final String ID = NodeEditAction.class.getName();
	
	private IEditPartNode selectedEditPart;

	public NodeEditAction(IWorkbenchPart part) {
		super(part);
		setId(ID);
		setText(org.iviPro.editors.scenegraph.actions.Messages.NodeEditAction_ContextEntry_Edit);
		setToolTipText(org.iviPro.editors.scenegraph.actions.Messages.NodeEditAction_ContextEntry_Edit_Tooltip);
		setImageDescriptor(Icons.ACTION_RENAME.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_RENAME
				.getDisabledImageDescriptor());
	}

	@Override
	public void run() {
		selectedEditPart.onDoubleClick();
	}

	@Override
	protected boolean calculateEnabled() {
		boolean singleNodeSelection = false;
		List graphSelection = getSelectedObjects();
		if (graphSelection.size() == 1) {
			Object selection = graphSelection.get(0);
			if (selection instanceof IEditPartNode
					&& !(selection instanceof EditPartNodeStart)) { 
				selectedEditPart = (IEditPartNode) selection;
				singleNodeSelection = true;
			}
		}
		return singleNodeSelection;
	}

}
