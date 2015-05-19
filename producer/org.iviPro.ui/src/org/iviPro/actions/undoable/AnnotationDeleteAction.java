package org.iviPro.actions.undoable;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.operations.graph.NodeDeleteOperation;
import org.iviPro.theme.Icons;

/**
 * Action die eine Annotation löscht
 * 
 * @author juhoffma
 * 
 */
public class AnnotationDeleteAction extends AbstractUndoableAction implements
		IWorkbenchAction {

	public final static String ID = AnnotationDeleteAction.class.getName();

	/**
	 * Konstruktor...
	 * 
	 * 
	 * @param annotation
	 */
	public AnnotationDeleteAction(INodeAnnotation annotation) {
		this((IWorkbenchWindow) null);
		setOperation(new NodeDeleteOperation(annotation));
	}

	/**
	 * Konstruktor...
	 * 
	 * @param window
	 */
	public AnnotationDeleteAction(IWorkbenchWindow window) {
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