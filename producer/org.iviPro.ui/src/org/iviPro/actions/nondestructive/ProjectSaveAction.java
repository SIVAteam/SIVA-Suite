package org.iviPro.actions.nondestructive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.model.Project;
import org.iviPro.operations.OperationHistory;
import org.iviPro.theme.Icons;

import com.thoughtworks.xstream.XStream;

/**
 * Action zum Abspeichern des Projekts. Die Action ist genau dann enabled, wenn
 * ein Projekt geoeffent ist und die OperationHistory keine ungespeicherten
 * Aenderungen enthaelt.
 * 
 * @author dellwo
 */
public class ProjectSaveAction extends Action implements ISelectionListener,
		IWorkbenchAction, ApplicationListener {
	private static Logger logger = Logger.getLogger(ProjectSaveAction.class);
	public final static String ID = ProjectSaveAction.class.getName();
	public final static String ACTIONDEFINITIONID = "org.iviPro.ui.ProjectSaveAction"; //$NON-NLS-1$
	public final static String ACTIONDEFINITIONID2 = "org.iviPro.ui.ProjectSaveAsAction"; //$NON-NLS-1$

	private final IWorkbenchWindow window;
	
	// falls saveAs gesetzt ist, kann der User einen Pfad/Dateinamen zum Speichern wählen
	private boolean saveAs;

	/**
	 * Konstruktor...
	 * 
	 * @param window
	 */
	public ProjectSaveAction(IWorkbenchWindow window, boolean saveAs) {
		this.window = window;
		this.saveAs = saveAs;
		setEnabled(false);
		setId(ID);
		if (saveAs) {
			setText(Messages.SaveOpenedProject_SaveAsProject);
			setToolTipText(Messages.SaveOpenedProject_SaveAsProjectToolTip);
			setImageDescriptor(Icons.ACTION_PROJECT_SAVE_AS.getImageDescriptor());
			setDisabledImageDescriptor(Icons.ACTION_PROJECT_SAVE_AS
					.getDisabledImageDescriptor());
			setActionDefinitionId(ACTIONDEFINITIONID2);
			setAccelerator(SWT.CTRL | SWT.SHIFT | 's');
		} else {
			setText(Messages.SaveOpenedProject_SaveProject);
			setToolTipText(Messages.SaveOpenedProject_SaveProjectToolTip);
			setImageDescriptor(Icons.ACTION_PROJECT_SAVE.getImageDescriptor());
			setDisabledImageDescriptor(Icons.ACTION_PROJECT_SAVE
					.getDisabledImageDescriptor());
			setActionDefinitionId(ACTIONDEFINITIONID);
			setAccelerator(SWT.CTRL | 's');
		}		
		window.getSelectionService().addSelectionListener(this);
		Application.getDefault().addApplicationListener(this);
		
	
		
		
		OperationHistory.addOperationObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				// save as ist immer verfügbar auch wenn sich am Projekt nichts geändert hat.
				if (!ProjectSaveAction.this.saveAs) {
					onHistoryChange();
				}
			}
		});
	}

	/**
	 * Wird aufgerufen, wenn sich der Operation-Manager aendert.
	 */
	private void onHistoryChange() {
		boolean unsavedChanges = OperationHistory.hasUnsavedChanges();
		logger.debug("Detected history change: unsaved = " + unsavedChanges); //$NON-NLS-1$
		setEnabled(unsavedChanges);
	}

	/**
	 * Die eiegentliche Ausführungslogik der Action.
	 */
	@Override
	public void run() {
		logger.debug("Saving project information..."); //$NON-NLS-1$

		Shell shell = new Shell(Display.getDefault());

		Project project = Application.getCurrentProject();
		
		deleteUnusedFiles(project);

		logger.debug("Serializing project information."); //$NON-NLS-1$
		try {
			File projectFile = project.getFile().getValue();
			File backupFile = project.getBackupFile();
			if (saveAs) {
				FileDialog fd = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
		        fd.setText(Messages.ProjectSaveAction_FileDialog_Title);
		        fd.setFilterPath(project.getFile().toString());
		        String[] filterExt = { "*." + Project.PROJECT_FILE_EXTENSION }; //$NON-NLS-1$
		        fd.setFilterExtensions(filterExt);
		        String newFile = fd.open();
		        if (newFile == null) {
		        	return;
		        }
		        projectFile = new File(newFile);
				project.setTitle(projectFile.getName().replaceAll("." + Project.PROJECT_FILE_EXTENSION, "")); //$NON-NLS-1$ //$NON-NLS-2$
				project.setFile(projectFile);
			}
			
			doSave(project, projectFile);
			
			// Delete backup after successful saving
			try {
				backupFile.delete();
			} catch (Throwable t) {
				Logger logger = Logger.getLogger(ProjectSaveAction.class);
				logger.error(t.getMessage(), t);
			}

			// Save-Point setzen, damit dieser Zustand fuer die Undo/Redo
			// History als gespeichert markiert ist
			OperationHistory.setSavePoint();
			
			Application.getDefault().setCurrentProject(project);

		} catch (Exception e) {
			MessageDialog.openError(shell,
					Messages.ProjectSaveAction_ErrorBox_ProjectSaved_Title,
					Messages.ProjectSaveAction_ErrorBox_ProjectSaved_Text
							+ e.getLocalizedMessage());
			logger.error(e);
			if (logger.isDebugEnabled()) {
				e.printStackTrace();
			}
		}

	}
	
	/**
	 * Deletes unused files of the given project.
	 * @param project project to delete from
	 */
	private void deleteUnusedFiles(Project project) {
		for (File f : project.getUnusedFiles()) {
			f.delete();
		}
		
	}

	/**
	 * Speichert das Projekt in der angegebenen Datei.
	 * 
	 * @param project
	 * @param targetFile
	 * @throws IOException
	 */
	public static void doSave(Project project, File targetFile)
			throws IOException {
		XStream xstream = new XStream();
		FileOutputStream fos = new FileOutputStream(targetFile);
		OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8"); //$NON-NLS-1$
		xstream.toXML(project, writer);
		writer.flush();
		writer.close();
		fos.flush();
		fos.close();
	}
	
	/**
	 * Speichert ein Backup in der angegebenen Datei und versteckt diese.
	 * 
	 * @param project
	 * @param targetFile
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static void doSaveBackup(Project project, File targetFile)
			throws IOException, InterruptedException {
		Process p = Runtime.getRuntime().exec("attrib -h " + targetFile.getPath()); //$NON-NLS-1$
		p.waitFor();
		ProjectSaveAction.doSave(project, targetFile);
	    p = Runtime.getRuntime().exec("attrib +h " + targetFile.getPath()); //$NON-NLS-1$
	    p.waitFor();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	}

	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

	@Override
	public void onProjectOpened(Project project) {
		setEnabled(this.saveAs);
	}

	@Override
	public void onProjectClosed(Project project) {
		setEnabled(false);
	}

}
