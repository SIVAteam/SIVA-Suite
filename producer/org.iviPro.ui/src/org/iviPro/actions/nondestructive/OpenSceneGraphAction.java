package org.iviPro.actions.nondestructive;

import org.apache.log4j.Logger;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.editors.scenegraph.SceneGraphEditor;
import org.iviPro.editors.scenegraph.SceneGraphEditorInput;
import org.iviPro.editors.scenegraph.editparts.EditPartGraph;
import org.iviPro.model.Project;
import org.iviPro.views.miniaturegraph.MiniatureGraph;

/**
 */
public class OpenSceneGraphAction extends Action implements IWorkbenchAction,
ApplicationListener {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(OpenSceneGraphAction.class);
	public final static String ID = OpenSceneGraphAction.class.getName();
	private final IWorkbenchWindow window;
	
	/**
	 * Konstruktor...
	 * 
	 * @param window
	 */
	public OpenSceneGraphAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText(Messages.OpenSceneGraphAction_Text);
		setToolTipText(Messages.OpenSceneGraphAction_Tooltip);
		Application.getDefault().addApplicationListener(this);		
		setEnabled(Application.getCurrentProject() != null);	
		window.getPartService().addPartListener(new IPartListener() {

			@Override
			public void partActivated(IWorkbenchPart arg0) {				
			}

			@Override
			public void partBroughtToTop(IWorkbenchPart arg0) {
			}

			@Override
			public void partClosed(IWorkbenchPart arg0) {
				if (arg0.getSite().getId().equals(SceneGraphEditor.ID)) {
					setEnabled(true);
				}				
			}

			@Override
			public void partDeactivated(IWorkbenchPart arg0) {				
			}

			@Override
			public void partOpened(IWorkbenchPart arg0) {
				if (arg0.getSite().getId().equals(SceneGraphEditor.ID)) {
					setEnabled(false);
				}
			}			
		});
	}

	/**
	 * Die eigentliche Ausführungslogik der Action.
	 */
	@Override
	public void run() {	
		Project project = Application.getCurrentProject();		
		if (project != null) {
			IWorkbenchPage page = window.getActivePage();
			IEditorPart editor = page.getActiveEditor();
			if (!(editor instanceof SceneGraphEditor)) {
				// Falls SzenenGraph nicht der aktive Editor ist, oeffne
				// ihn und den Miniature-Graph.
				SceneGraphEditorInput input = new SceneGraphEditorInput(project
						.getSceneGraph());	
				try {
					editor = page.openEditor(input, SceneGraphEditor.ID);					
					page.showView(MiniatureGraph.ID);
					page.activate(editor);
					setEnabled(false);					
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}

			//setze konsistentes sem. Zoomlevel wenn ProjectSettings.completeZoomLevels == true
			if(editor instanceof SceneGraphEditor) {
				try {
					ScalableFreeformRootEditPart rootEditPart = ((SceneGraphEditor) editor).getRootEditPart();
					EditPartGraph editPartGraph = (EditPartGraph)rootEditPart.getChildren().get(0);
					editPartGraph.setConsistentSemZoomLevels();
				} catch (Exception e) {
					e.printStackTrace();
				}
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
		Application.getDefault().removeApplicationListener(this);
	}
}
