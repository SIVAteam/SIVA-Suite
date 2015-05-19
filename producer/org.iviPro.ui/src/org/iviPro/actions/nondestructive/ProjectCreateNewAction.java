package org.iviPro.actions.nondestructive;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.actions.undoable.MediaLoadAction;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.dialogs.projectcreate.ProjectCreateWizard;
import org.iviPro.editors.scenegraph.SceneGraphEditor;
import org.iviPro.editors.scenegraph.SceneGraphEditorInput;
import org.iviPro.model.Project;
import org.iviPro.theme.Icons;
import org.iviPro.views.mediarepository.MediaRepository;
import org.iviPro.views.miniaturegraph.MiniatureGraph;

/**
 * Diese Aktion erstellt ein leeres Projekt.
 * 
 * @author Florian Stegmaier
 */
public class ProjectCreateNewAction extends Action implements IWorkbenchAction,
		ApplicationListener {
	private static Logger logger = Logger
			.getLogger(ProjectCreateNewAction.class);
	public final static String ID = ProjectCreateNewAction.class.getName();
	public final static String ACTIONDEFINITIONID = "org.iviPro.ui.ProjectCreateNewAction"; //$NON-NLS-1$

	private IWorkbenchWindow window;

	/**
	 * Erstellt einen neue Action zum Erstellen eines neuen Projekts.
	 * 
	 * @param window
	 */
	public ProjectCreateNewAction(IWorkbenchWindow window) {
		this.window = window;
		setEnabled(true);
		setId(ID);
		setText(Messages.BuildNewProject_NewProject);
		setToolTipText(Messages.BuildNewProject_NewProjectToolTip);
		setImageDescriptor(Icons.ACTION_PROJECT_NEW.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_PROJECT_NEW
				.getDisabledImageDescriptor());
		setActionDefinitionId(ACTIONDEFINITIONID);
		Application.getDefault().addApplicationListener(this);
		setAccelerator(SWT.CTRL | 'n');
	}

	/**
	 * Die eiegentliche Ausführungslogik der Action.
	 */
	@Override
	public void run() {
		logger.debug("Creating new project..."); //$NON-NLS-1$

		// Zuerst muessen wir ggfs ein noch offenes Projekt schliessen
		ProjectCloseAction closeAction = new ProjectCloseAction(window);
		closeAction.run();

		// Wenn Projekt nicht geschlossen wurde, weil Benutzer dies z.B. abbrach
		// dann ist das currentProjekt immer noch gesetzt und wir beenden
		if (Application.getCurrentProject() != null) {
			return;
		}

		// Projekt geschlossen, wir erstellen nun neues Projekt mit dem Wizard
		Project project = createProjectWithWizard();

		// Wenn Wizard abgebrochen wurde, dann beenden wir ebenfalls
		if (project == null) {
			return;
		}

		// Projekt wurde erstellt -> Wir oeffnen es
		Application.getDefault().setCurrentProject(project);

		// Am Anfang sollte das Media-Repository aktiviert sein
		IViewPart mediaRepository = Application.getDefault().getView(
				MediaRepository.ID);
		window.getActivePage().activate(mediaRepository);
		
		// Hinzufügen von Medien wird zunächst abgefragt
        MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        messageBox.setText(Messages.ProjectCreateNewAction_ADD_Media_MSGBOX_Title);
        messageBox.setMessage(Messages.ProjectCreateNewAction_Add_Media_MSGBOX);
        int val = messageBox.open();
        
        if (val == SWT.YES) {        	
        	MediaLoadAction ml = new MediaLoadAction(window);
			ml.run();
        }
		
		// öffne den Szenengraph
		IWorkbenchPage page = window.getActivePage();
		IEditorPart editor = page.getActiveEditor();
		if (!(editor instanceof SceneGraphEditor)) {
			// Falls SzenenGraph nicht der aktive Editor ist, oeffne
			// ihn und den Miniature-Graph.
			SceneGraphEditorInput input = 
				new SceneGraphEditorInput(Application.getCurrentProject().getSceneGraph());							
			try {
				editor = page.openEditor(input, SceneGraphEditor.ID);
				page.showView(MiniatureGraph.ID);
				page.activate(editor);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}		
	}

	/**
	 * Erstellt ein neues Projekt mit dem Projekt-Wizard
	 * 
	 * @return Das erstellte Projekt oder null, falls des Wizard abgebrochen
	 *         wurde.
	 */
	private Project createProjectWithWizard() {
		Project createdProject = null;
		ProjectCreateWizard wizard = new ProjectCreateWizard(window);
		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		dialog.setBlockOnOpen(true);
		int returnCode = dialog.open();
		if (returnCode == Dialog.OK) {
			createdProject = wizard.getCreatedProject();
			logger.debug("Dialog OK -> New project created: " + createdProject); //$NON-NLS-1$
		} else {
			logger.debug("Dialog cancelled"); //$NON-NLS-1$
		}
		return createdProject;

	}

	@Override
	public void dispose() {
		Application.getDefault().removeApplicationListener(this);
	}

	@Override
	public void onProjectClosed(Project project) {
		setEnabled(true);
	}

	@Override
	public void onProjectOpened(Project project) {
		setEnabled(false);
	}

}
