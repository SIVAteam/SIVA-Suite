package org.iviPro.actions.nondestructive;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.editors.annotationeditor.AnnotationEditor;
import org.iviPro.editors.annotationeditor.AnnotationEditorInput;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.theme.Icons;

public class OpenAnnotationEditorAction extends Action implements
		IWorkbenchAction {

	public final static String ID = OpenAnnotationEditorAction.class.getName();

	private final IWorkbenchWindow window;

	/**
	 * Enthaelt den Namen der gerade ausgewaehlten Szene im Media-Repository.
	 * Wird in der Listener-Methode selectionChanged() gesetzt.
	 */
	private NodeScene sceneNode;

	/**
	 * Erstellt eine neue AnnotateSceneAction.
	 * 
	 * @param window
	 */
	public OpenAnnotationEditorAction(IWorkbenchWindow window,
			NodeScene sceneNode) {
		this.window = window;
		this.sceneNode = sceneNode;
		setEnabled(false);
		setId(ID);
		setText(Messages.AnnotateScene_AnnotateScene);
		setToolTipText(Messages.AnnotateScene_AnnotateSceneTooltip);
		setImageDescriptor(Icons.ACTION_EDITOR_ANNOTATION.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_EDITOR_ANNOTATION
				.getDisabledImageDescriptor());
	}

	/**
	 * Die eigentliche Ausführungslogik der Action.
	 */
	@Override
	public void run() {

		// Input-Daten fuer Szenen-Editor erstellen.
		AnnotationEditorInput input = new AnnotationEditorInput(sceneNode);

		// Szenen-Editor oeffnen.
		try {
			IWorkbenchPage page = window.getActivePage();
			page.openEditor(input, AnnotationEditor.ID, true);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dispose() {
	}
}
