package org.iviPro.actions.nondestructive;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.editors.toc.TocEditor;
import org.iviPro.editors.toc.TocEditorInput;
import org.iviPro.model.Project;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.theme.Icons;

public class OpenTocEditorAction extends Action implements ApplicationListener,
		IWorkbenchAction {

	public final static String ID = OpenTocEditorAction.class.getName();
	public final static String ACTIONDEFINITIONID = "org.iviPro.ui.OpenTocEditorAction"; //$NON-NLS-1$


	private final IWorkbenchWindow window;
	
	public OpenTocEditorAction(IWorkbenchWindow window ) {
		this.window = window;
		setId(ID);
		setText(Messages.OpenTocEditorAction_Title);
		setToolTipText(Messages.OpenTocEditorAction_ToolTip);
		setImageDescriptor(Icons.ACTION_TABLE_OF_CONTENTS.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_TABLE_OF_CONTENTS
				.getDisabledImageDescriptor());
		setActionDefinitionId(ACTIONDEFINITIONID);
		Application.getDefault().addApplicationListener(this);
		setEnabled(Application.getCurrentProject() != null);
	}
	@Override
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		TocEditorInput input = new TocEditorInput();
		try {
			page.openEditor(input, TocEditor.ID, true);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	@Override
	public void onProjectClosed(Project project) {
		setEnabled(false);

	}

	@Override
	public void onProjectOpened(final Project project) {

		// falls mehr als 2 Knoten (da Start und Endknoten immer vorhanden sind
		// schalte den Export ein
		if (project.getSceneGraph().searchNodes(NodeScene.class, true).size() > 0) {
			setEnabled(true);	
		}
		
		// die Action ist nur aktiv wenn mehr als 2 Szenen im Szenengraph sind
		Application.getCurrentProject().getSceneGraph().addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				if (project.getSceneGraph().searchNodes(NodeScene.class, true).size() > 0) {
					setEnabled(true);	
				} else {
					setEnabled(false);
				}
			}			
		});	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
