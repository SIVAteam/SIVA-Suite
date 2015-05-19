package org.iviPro.actions.nondestructive;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.editors.scenegraph.SceneGraphValidator;
import org.iviPro.model.Project;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.graph.NodeQuizControl;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.NodeStart;
import org.iviPro.theme.Icons;

public class GraphValidationAction extends Action implements IWorkbenchAction,
		ApplicationListener {

	private IWorkbenchWindow window;

	public GraphValidationAction(IWorkbenchWindow window) {
		this.window = window;
		setToolTipText(Messages.GraphValidationAction_Tooltip);
		setText(Messages.GraphValidationAction_Text);
		setImageDescriptor(Icons.ACTION_GRAPHVALIDATION.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_GRAPHVALIDATION
				.getDisabledImageDescriptor());
		Application.getDefault().addApplicationListener(this);
		setEnabled(Application.getCurrentProject() != null);
	}

	public void run() {
		SceneGraphValidator validator = new SceneGraphValidator();
		validator.requestValidationDialog(window.getShell());
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProjectClosed(Project project) {
		setEnabled(false);
	}

	@Override
	public void onProjectOpened(Project project) {
		setEnabled(true);
	}

}
