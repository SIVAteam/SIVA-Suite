package org.iviPro.actions.undoable;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.operations.CompoundOperation;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.annotation.GlobalAnnotationDeleteOperation;
import org.iviPro.theme.Icons;

/**
 * Action die eine Annotation löscht
 * 
 * @author juhoffma
 * 
 */
public class GlobalAnnotationDeleteAction extends AbstractUndoableAction implements
		IWorkbenchAction {

	public final static String ID = GlobalAnnotationDeleteAction.class.getName();

	/**
	 * Konstruktor...
	 * 
	 * 
	 * @param annotation
	 */
	public GlobalAnnotationDeleteAction(INodeAnnotationLeaf annotation) {
		this((IWorkbenchWindow) null);
		setOperation(new GlobalAnnotationDeleteOperation(annotation));
	}
	
	public GlobalAnnotationDeleteAction(List<INodeAnnotationLeaf> leafs) {
		this((IWorkbenchWindow) null);
		CompoundOperation<IAbstractOperation> op = new CompoundOperation<IAbstractOperation>(Messages.GlobalAnnotationDeleteOperation_UndoLabel);
		for (INodeAnnotationLeaf leaf : leafs) {
			op.addOperation(new GlobalAnnotationDeleteOperation(leaf));
		}
		setOperation(op);
	}

	/**
	 * Konstruktor...
	 * 
	 * @param window
	 */
	public GlobalAnnotationDeleteAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText(Messages.AnnotationDeleteAction_Label);
		setToolTipText(Messages.AnnotationDeleteAction_Tooltip);
		setImageDescriptor(Icons.ACTION_ANNOTATION_DELETE.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_ANNOTATION_DELETE
				.getDisabledImageDescriptor());
	}

	@Override
	protected void onDispose() {
		// Nix zu tun, da keine Ressourcen oder Listener zum Freigeben vorhanden
	}

	@Override
	protected void onSelectionChange(IStructuredSelection selection,
			IWorkbenchPart workbenchPart) {
	}
}