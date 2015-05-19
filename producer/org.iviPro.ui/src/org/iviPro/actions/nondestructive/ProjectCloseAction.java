package org.iviPro.actions.nondestructive;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.editors.annotationeditor.components.AnnotationDefineWidget;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.resources.Picture;
import org.iviPro.operations.OperationHistory;
import org.iviPro.theme.Icons;

/**
 * Mithilfe dieser Aktion kann man ein geöffnetes Projekt wieder schließen.
 * 
 * @author Florian Stegmaier
 */
public class ProjectCloseAction extends Action implements IWorkbenchAction,
		ApplicationListener {
	private static Logger logger = Logger.getLogger(ProjectCloseAction.class);
	public final static String ID = ProjectCloseAction.class.getName(); //$NON-NLS-1$
	public final static String ACTIONDEFINITIONID = "org.iviPro.ui.ProjectCloseAction"; //$NON-NLS-1$
	private final IWorkbenchWindow window;

	/**
	 * Konstruktor...
	 * 
	 * @param window
	 */
	public ProjectCloseAction(IWorkbenchWindow window) {
		this.window = window;
		setEnabled(false);
		setId(ID);
		setText(Messages.CloseProjectAction_CloseProject);
		setToolTipText(Messages.CloseProjectAction_CloseProjectToolTip);
		setImageDescriptor(Icons.ACTION_PROJECT_CLOSE.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_PROJECT_CLOSE
				.getDisabledImageDescriptor());
		setActionDefinitionId(ACTIONDEFINITIONID);
		Application.getDefault().addApplicationListener(this);
	}

	/**
	 * Wird beim schließen ausgeführt.
	 */
	@Override
	public void dispose() {
		Application.getDefault().removeApplicationListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		logger.debug("Closing project..."); //$NON-NLS-1$

		if (OperationHistory.hasUnsavedChanges()) {
			MessageBox messageBox = new MessageBox(Display.getCurrent()
					.getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO
					| SWT.CANCEL);
			messageBox
					.setMessage(Messages.ProjectCloseAction_MsgBox_UnsavedChanges_Text);
			messageBox.setText(Messages.ProjectCloseAction_MsgBox_UnsavedChanges_Title);

			int option = messageBox.open();
			if (option == SWT.CANCEL) {
				// Abbruch
				return;
			} else if (option == SWT.YES) {
				// Projekt vorher speichern.
				new ProjectSaveAction(window, false).run();
			}
		}

		// Ressourcen frei geben
		if (Application.getCurrentProject() != null) {
			for (IAbstractBean bean : Application.getCurrentProject().getMediaObjects()) {
				if (bean instanceof Picture) {
					((Picture) bean).clearThumbnails();
				}
			}
		}
		
		// Alle Editoren schliessen.
		IWorkbenchPage page = window.getActivePage();
		IEditorReference[] editorReferences = page.getEditorReferences();
		for (IEditorReference editorRef : editorReferences) {
			IEditorPart editor = editorRef.getEditor(false);		
			if (editor != null) {
				page.closeEditor(editor, false);
				editor.dispose();
			}
		}
		
		// Delete backup file
		Project project = Application.getCurrentProject();
		if(project != null){
			try {
				project.getBackupFile().delete();
			} catch (Throwable t) {
				Logger logger = Logger.getLogger(AnnotationDefineWidget.class);
				logger.error(t.getMessage(), t);
			}
		}
		
		// Aktuelles Projekt verwerfen (wichtig: erst nach Schließen der Editoren)
		Application.getDefault().setCurrentProject(null);
		
		// starte auf jeden Fall beim Projekt schließen die
		// Garbage Collection
		System.gc();
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
