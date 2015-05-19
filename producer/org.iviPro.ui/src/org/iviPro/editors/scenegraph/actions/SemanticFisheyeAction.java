package org.iviPro.editors.scenegraph.actions;

import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.editors.scenegraph.SceneGraphEditor;
import org.iviPro.editors.scenegraph.editparts.EditPartNodeScene;
import org.iviPro.editors.scenegraph.editparts.IEditPartNode;
import org.iviPro.model.Project;
import org.iviPro.theme.Icons;

public class SemanticFisheyeAction extends SelectionAction implements ApplicationListener {
	
	public static final String ID = SemanticFisheyeAction.class.getName();
	private boolean fisheyeActivated = false;
	IWorkbenchPart workbenchpart;

	public SemanticFisheyeAction(IWorkbenchPart part) {
		super(part);
		workbenchpart = part;
		setId(ID);
		setText("Semantic Fisheye Action"); //$NON-NLS-1$
		setToolTipText(Messages.SemanticFisheyeAction_EnableFisheye);
		setImageDescriptor(Icons.GRAPH_FISHEYE.getImageDescriptor());
		setDisabledImageDescriptor(Icons.GRAPH_FISHEYE
				.getDisabledImageDescriptor());
		Application.getDefault().addApplicationListener(this);
		setEnabled(Application.getCurrentProject() != null);
		setChecked(false);
	}
	
	/**
	 * Die eigentliche Ausführungslogik der Action.
	 */
	@Override
	public void run() {

		try {
			//Switche den Aktivierungsstatus
			if(fisheyeActivated) {
				fisheyeActivated = !fisheyeActivated;
				this.setChecked(false);
				setToolTipText(Messages.SemanticFisheyeAction_EnableFisheye); //$NON-NLS-1$
			} else {
				fisheyeActivated = !fisheyeActivated;
				this.setChecked(true);
				setToolTipText(Messages.SemanticFisheyeAction_DisableFisheye);
			}
			
			Project project = Application.getCurrentProject();		
			if (project != null) {
				if(workbenchpart instanceof SceneGraphEditor) {
					ScalableFreeformRootEditPart rootEditPart = ((SceneGraphEditor) workbenchpart).getRootEditPart();
					AbstractGraphicalEditPart editPartGraph = (AbstractGraphicalEditPart)rootEditPart.getChildren().get(0);
					for(int i=0; i < editPartGraph.getChildren().size(); i++) {
						IEditPartNode editPartNode = (IEditPartNode)editPartGraph.getChildren().get(i);
						if(editPartNode instanceof EditPartNodeScene) {
							((EditPartNodeScene) editPartNode).setSemanticFisheye(fisheyeActivated);
							((EditPartNodeScene) editPartNode).updateSemanticFisheyeFigure();
						}
					}
				}
			}
		} catch (Exception e) {
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
		Application.getDefault().removeApplicationListener(this);
	}
	
	public boolean isFisheyeActivated() {
		return fisheyeActivated;
	}

	@Override
	protected boolean calculateEnabled() {
		// TODO Auto-generated method stub
		return true;
	}
}
