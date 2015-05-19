package org.iviPro.actions.nondestructive;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.model.Project;
import org.iviPro.theme.Icons;

public class OpenQuiz extends Action implements IWorkbenchAction, ISelectionListener,
		ApplicationListener {
	
	public final static String ID = OpenQuiz.class.getName();
	
	private final IWorkbenchWindow window;

	public OpenQuiz(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText(Messages.OpenQuiz_Action_Title);
		setToolTipText(Messages.OpenQuiz_Action_Tooltip);
		setImageDescriptor(Icons.ACTION_PROJECT_OPEN.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_PROJECT_OPEN
				.getDisabledImageDescriptor());
		window.getSelectionService().addSelectionListener(this);
		Application.getDefault().addApplicationListener(this);
	}
	
	@Override
	public void run() {
//		QuizGenerator.main(null);
	}
	
	@Override
	public void onProjectOpened(Project project) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProjectClosed(Project project) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// TODO Auto-generated method stub
		
	}
}
