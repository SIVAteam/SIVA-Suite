package org.iviPro.actions.nondestructive;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.editors.annotationeditor.GlobalAnnotationEditor;
import org.iviPro.editors.annotationeditor.GlobalAnnotationEditorInput;
import org.iviPro.model.Project;
import org.iviPro.theme.Icons;

public class OpenGlobalAnnotationEditorAction extends Action implements
		IWorkbenchAction, ApplicationListener {

	public final static String ID = OpenGlobalAnnotationEditorAction.class.getName();

	private final IWorkbenchWindow window;

	/**
	 * Erstellt eine neue AnnotateSceneAction.
	 * 
	 * @param window
	 */
	public OpenGlobalAnnotationEditorAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText(Messages.AnnotateGlobalScene_AnnotateScene);
		setToolTipText(Messages.AnnotateGlobalScene_AnnotateSceneTooltip);
		setImageDescriptor(Icons.ACTION_EDITOR_ANNOTATION.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_EDITOR_ANNOTATION
				.getDisabledImageDescriptor());
		Application.getDefault().addApplicationListener(this);
		setEnabled(Application.getCurrentProject() != null);
	}

	/**
	 * Die eigentliche Ausführungslogik der Action.
	 */
	@Override
	public void run() {

		// Input-Daten fuer Szenen-Editor erstellen.
		GlobalAnnotationEditorInput input = new GlobalAnnotationEditorInput();

		// Szenen-Editor oeffnen.
		try {
			IWorkbenchPage page = window.getActivePage();
			page.openEditor(input, GlobalAnnotationEditor.ID, true);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onProjectClosed(Project project) {
		setEnabled(false);

	}

	@Override
	public void onProjectOpened(Project project) {
		setEnabled(true);

	}

	@Override
	public void dispose() {
	}
}
