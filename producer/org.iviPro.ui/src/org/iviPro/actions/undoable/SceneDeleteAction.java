package org.iviPro.actions.undoable;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.model.resources.Scene;
import org.iviPro.operations.CompoundOperation;
import org.iviPro.operations.video.SceneDeleteOperation;
import org.iviPro.theme.Icons;
import org.iviPro.views.scenerepository.SceneTreeLeaf;

/**
 * Action zum Loeschen von Szenen.
 * 
 * @author dellwo
 * 
 */
public class SceneDeleteAction extends AbstractUndoableAction implements
		IWorkbenchAction {
	public final static String ID = SceneDeleteAction.class.getName();

	/**
	 * Konstruktor zum loeschen einer einzelnen Szene.
	 * 
	 * @param sceneToDelete
	 *            Die zu loeschende Szene.
	 */
	public SceneDeleteAction(Scene sceneToDelete) {
		this((IWorkbenchWindow) null);
		setOperation(new SceneDeleteOperation(sceneToDelete));
	}

	/**
	 * Konstruktor fuer eine Action die auf die jeweils selektierten Szenen in
	 * der Workbench anwendbar ist.
	 * 
	 * 
	 * @param window
	 *            Das Workbench-Fenster in dem auf Selektion von Szenen
	 *            gelistened werden soll.
	 */
	public SceneDeleteAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText(Messages.SceneDeleteAction_Text);
		setToolTipText(Messages.SceneDeleteAction_ToolTip);
		setImageDescriptor(Icons.ACTION_SCENE_DELETE.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_SCENE_DELETE
				.getDisabledImageDescriptor());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.iviPro.actions.undoable.AbstractUndoableAction#onDispose()
	 */
	@Override
	protected void onDispose() {
		// Nix zu tun, da keine Ressourcen oder Listener zum Freigeben vorhanden
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.iviPro.actions.undoable.AbstractUndoableAction#onSelectionChange(
	 * org.eclipse.jface.viewers.IStructuredSelection,
	 * org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	protected void onSelectionChange(IStructuredSelection selection,
			IWorkbenchPart workbenchPart) {

		// Suche alle Szenen in der aktuellen Selektion
		Iterator it = selection.iterator();
		LinkedList<Scene> selectedScenes = new LinkedList<Scene>();
		while (it.hasNext()) {
			Object next = it.next();
			if (next instanceof SceneTreeLeaf) {
				Scene scene = ((SceneTreeLeaf) next).getScene();
				selectedScenes.add(scene);
			}
		}

		// Wenn Szenen selektiert sind, aktiviere die Aktion, in dem eine
		// Operation gesetzt wird, die die selektierten Szenen loeschen wuerde.
		if (selectedScenes.size() > 0) {
			CompoundOperation<SceneDeleteOperation> deleteOp = new CompoundOperation<SceneDeleteOperation>(
					Messages.SceneDeleteAction_0);
			for (Scene scene : selectedScenes) {
				deleteOp.addOperation(new SceneDeleteOperation(scene));
			}
			setOperation(deleteOp);
		} else {
			setOperation(null);
		}

	}
}
