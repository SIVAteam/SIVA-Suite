package org.iviPro.application;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * 
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static Logger logger = Logger
			.getLogger(ApplicationWorkbenchAdvisor.class);
	private static final String PERSPECTIVE_ID = "org.iviPro.perspective"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#createWorkbenchWindowAdvisor(org.eclipse.ui.application.IWorkbenchWindowConfigurer)
	 */
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#getInitialWindowPerspectiveId()
	 */
	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#initialize(org.eclipse.ui.application.IWorkbenchConfigurer)
	 */
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		configurer.setSaveAndRestore(true);

		// Abgerundete Tabs
		PlatformUI.getPreferenceStore().setValue(
				IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS,
				false);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#eventLoopException(java.lang.Throwable)
	 */
	@Override
	public void eventLoopException(Throwable ex) {
		// Diese Methode ist dazu da um Exceptions die im RCP-Eventloop
		// auftreten behandeln zu koennen. Diese wuerden sonst ueberhaupt
		// nicht angezeigt und man bekommt davon nichts mit.

		// Zunaechst loggen wir mal die Exception auf der Konsole
		logger.error("EXCEPTION OCCURED: " + ex.getClass().getSimpleName()); //$NON-NLS-1$
		logger.error("EXCEPTION MESSAGE: " + ex.getMessage(), ex); //$NON-NLS-1$

		// Beschriftungen der Labels fuer den Error-Dialog zusammenbauen
		String title = Messages.ErrorDialog_Title;
		String details = Messages.ErrorDialog_Details;
		String subtitle;
		if (ex.getMessage() == null) {
			subtitle = Messages.ErrorDialog_Subtitle_NoMsg;
			subtitle = subtitle.replace("{0}", ex.getClass().getSimpleName()); //$NON-NLS-1$
		} else {
			subtitle = Messages.ErrorDialog_Subtitle;
			subtitle = subtitle.replace("{0}", ex.getClass().getSimpleName()); //$NON-NLS-1$
			subtitle = subtitle.replace("{1}", ex.getMessage()); //$NON-NLS-1$
		}

		// Den Stack-Trace in ein MultiStatus-Object umwandeln, denn dann lassen
		// sich die einzelnen Zeilen des Stacktraces in den ausklappbaren
		// Details des Error-Dialogs anschauen
		StackTraceElement[] stackTrace = ex.getStackTrace();
		IStatus[] statusStackItems = new IStatus[stackTrace.length];
		for (int i = 0; i < stackTrace.length; i++) {
			StackTraceElement element = stackTrace[i];
			String curMessage = "at " + element.getClassName() + "." //$NON-NLS-1$ //$NON-NLS-2$
					+ element.getMethodName() + "(" + element.getFileName() //$NON-NLS-1$
					+ ":" + element.getLineNumber() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			statusStackItems[i] = new Status(IStatus.ERROR,
					Application.PLUGIN_ID, curMessage);
		}
		MultiStatus stackStraceStatus = new MultiStatus(Application.PLUGIN_ID,
				IStatus.ERROR, statusStackItems, subtitle, ex);

		// Error-Dialog anzeigen
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		ErrorDialog errorDialog = new ErrorDialog(window.getShell(), title,
				details, stackStraceStatus, IStatus.ERROR);
		errorDialog.open();

		// Wir reichen die Exception mal weiter, fuer den Fall dass RCP
		// damit noch was machen will.
		super.eventLoopException(ex);

	}
}
