package org.iviPro.actions.nondestructive;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.editors.scenegraph.SceneGraphValidator;
import org.iviPro.export.ExportDialog;
import org.iviPro.model.Project;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.theme.Icons;

/**
 * Diese Action ermöglicht es, das in dem Graphen erstellte Projekt zu
 * exportieren.
 * 
 * @author Christian Dellwo
 */
public class ProjectExportAction extends Action implements IWorkbenchAction,
		ApplicationListener {
	public final static String ID = ProjectExportAction.class.getName();
	public final static String ACTIONDEFINITIONID = "org.iviPro.ui.ProjectExportAction"; //$NON-NLS-1$

	private final IWorkbenchWindow window;

	/**
	 * Konstruktor...
	 * 
	 * @param window
	 */
	public ProjectExportAction(IWorkbenchWindow window) {
		this.window = window;
		setEnabled(false);
		setId(ID);
		// Uncomment this for old export
//		setText(Messages.ExportProject_ExportProject);
		// SMIL only export
		setText(Messages.ExportProject_ExportProject_SMIL_Only);
		setToolTipText(Messages.ExportProject_ExportProjectToolTip);
		setImageDescriptor(Icons.ACTION_PROJECT_EXPORT.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_PROJECT_EXPORT
				.getDisabledImageDescriptor());
		setActionDefinitionId(ACTIONDEFINITIONID);
		Application.getDefault().addApplicationListener(this);				
	}

	/**
	 * Die eigentliche Ausführungslogik der Action
	 */
	@Override
	public void run() {
		
		// Hier den Graph prüfen
		Application.getCurrentProject().getSceneGraph();
		SceneGraphValidator validator = new SceneGraphValidator();
		
		boolean validGraph = validator.validateSceneGraph();
		if (validGraph) {
			// Dann exportieren
			ExportDialog exportDialog = new ExportDialog(window);
			exportDialog.export();
		} else {
			Shell parentShell = window.getShell();
			MessageDialog messageDialog = new MessageDialog(parentShell, Messages.ProjectExportAction_InvalidSceneGraphErrorDialogTitle, null,
			        Messages.ProjectExportAction_InvalidSceneGraphErrorDialogMessage, MessageDialog.ERROR,
			        new String[] { Messages.ProjectExportAction_InvalidSceneGraphErrorDialogOk }, 0);
			messageDialog.open();
		}
		
	}

	@Override
	public void dispose() {
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
		});	
	}

}
