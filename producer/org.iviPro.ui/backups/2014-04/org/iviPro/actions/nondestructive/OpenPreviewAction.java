package org.iviPro.actions.nondestructive;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.model.Project;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.preview.Preview;
import org.iviPro.preview.PreviewInput;

public class OpenPreviewAction extends Action implements ApplicationListener,
		IWorkbenchAction {

	public static final String ID = OpenPreviewAction.class.getName();
	
	private final IWorkbenchWindow window;
	private NodeScene scene;
	
	public OpenPreviewAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
	}
	
	public OpenPreviewAction(IWorkbenchWindow window, NodeScene scene) {
		this.scene = scene;
		this.window = window;
		Application.getDefault().addApplicationListener(this);
		setId(ID);
	}
	
	public void run() {
		if (scene != null) {
			IWorkbenchPage page = window.getActivePage();
			PreviewInput input = new PreviewInput(scene);
			try {
				page.openEditor(input, Preview.ID, true);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
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
		// TODO Auto-generated method stub

	}

}
