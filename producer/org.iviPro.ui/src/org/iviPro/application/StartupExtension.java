/**
 * 
 */
package org.iviPro.application;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.iviPro.actions.nondestructive.ProjectSaveAction;
import org.iviPro.model.Project;
import org.iviPro.operations.OperationHistory;
import org.iviPro.views.informationview.InformationView;
import org.iviPro.views.mediarepository.MediaRepository;
import org.iviPro.views.scenerepository.SceneRepository;

/**
 * @author dellwo
 * 
 */
public class StartupExtension implements IStartup, IWorkbenchListener {

	private static final Logger logger = Logger
			.getLogger(StartupExtension.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	@Override
	public void earlyStartup() {
		logger.info("Running StartupExtension..."); //$NON-NLS-1$

		// Feststellen welcher Editor gerade aktiv ist
		IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.addWorkbenchListener(this);
		workbench.addWindowListener(new IWindowListener() {

			@Override
			public void windowActivated(IWorkbenchWindow window) {
				logger.debug("Window activated: " + window); //$NON-NLS-1$
			}

			@Override
			public void windowClosed(IWorkbenchWindow window) {
				logger.info("Window closed: " + window); //$NON-NLS-1$

			}

			@Override
			public void windowDeactivated(IWorkbenchWindow window) {
				logger.debug("Window deactivated: " + window); //$NON-NLS-1$

			}

			@Override
			public void windowOpened(IWorkbenchWindow window) {
				logger.info("Window opened: " + window); //$NON-NLS-1$

			}

		});
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();
				logger.info("Preloading views..."); //$NON-NLS-1$
				page.findView(SceneRepository.ID);
				page.findView(MediaRepository.ID);
				page.findView(InformationView.ID);
			}

		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchListener#postShutdown(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void postShutdown(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchListener#preShutdown(org.eclipse.ui.IWorkbench,
	 * boolean)
	 */
	@Override
	public boolean preShutdown(IWorkbench workbench, boolean forced) {
		logger.debug("Running Pre-Shutdown hook..."); //$NON-NLS-1$
		// // Toolbar ausschalten, damit sie beim naechsten Start nicht wieder
		// IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
		// .getActivePage();
		// IViewPart toolbar = page.findView(Toolbar.ID);
		// // Am Anfang wird die Toolbar nicht benoetigt.
		// page.hideView(toolbar);
		// TODO Pruefen ob etwas gespeichert wurde. Wenn ja, Beenden verhindern
		if (OperationHistory.hasUnsavedChanges()) {
			MessageBox messageBox = new MessageBox(Display.getCurrent()
					.getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO
					| SWT.CANCEL);
			messageBox
					.setMessage(Messages.StartupExtension_MsgBox_SaveChangesBeforeQuit_Text);
			messageBox.setText(Messages.StartupExtension_MsgBox_SaveChangesBeforeQuit_Title);

			int option = messageBox.open();
			if (option == SWT.CANCEL) {
				// Abbruch
				return false;
			} else if (option == SWT.YES) {
				// Projekt vorher speichern.
				new ProjectSaveAction(workbench.getActiveWorkbenchWindow(), false)
						.run();
			}
		}
		
		// Delete backup file
		Project project = Application.getCurrentProject();
		if(project != null){
			try {
				project.getBackupFile().delete();
			} catch (Throwable t) {
				Logger logger = Logger.getLogger(StartupExtension.class);
				logger.error(t.getMessage(), t);
			}
		}
		
		return true;
	}

}
