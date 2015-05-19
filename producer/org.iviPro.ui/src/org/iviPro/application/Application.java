package org.iviPro.application;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.iviPro.dnd.DragDropManager;
import org.iviPro.mediaaccess.mediameta.MediaMetaSystem;
import org.iviPro.model.Project;
import org.iviPro.operations.OperationHistory;
import org.iviPro.theme.Colors;
import org.iviPro.theme.Themes;
import org.iviPro.utils.PathHelper;

/**
 * ERCP basierendes Autorenwerkzeug für interaktive Videos
 * 
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	private static Logger logger = Logger.getLogger(Application.class);

	public static final String PLUGIN_ID = "org.iviPro.ui"; //$NON-NLS-1
	public static final String PLUGIN_NAME = "SIVA Producer"; //$NON-NLS-1$
	private static final String DEBUG_PARAMNAME = "-debugconfig"; //$NON-NLS-1$
	private static final String THEME_PARAMNAME = "-theme"; //$NON-NLS-1$
	private static final String LIBDIR_PARAMNAME = "-libdir"; //$NON-NLS-1$

	private static Application instance;
	
	/**
	 * Das momentan geoeffnete Projekte
	 */
	private Project currentProject;

	/**
	 * Alle angemeldeten Application-Listener.
	 */
	private Collection<ApplicationListener> applicationListeners;

	/**
	 * Drag-and-Drop Manager fuer Transfer von Java-Objekten per DND
	 */
	private DragDropManager dragDropManager;

	/**
	 * Erstellt eine neue SIVA Applikation
	 */
	public Application() {
		PropertyConfigurator.configure(PathHelper.getPathToLoggerIni());
		instance = this;
		currentProject = null;
		applicationListeners = new ArrayList<ApplicationListener>();
		dragDropManager = new DragDropManager();
		
	}

	/**
	 * Gibt aktuelle Instanz der SIVA-Applikation zurueck
	 * 
	 * @return
	 */
	public static Application getDefault() {
		return instance;
	}

	/**
	 * Gibt den Drag-and-Drop Manager der Anwendung zurueck.
	 * 
	 * @return
	 */
	public static DragDropManager getDragDropManager() {
		return Application.getDefault().dragDropManager;
	}

	/**
	 * Gibt die aktuelle verwendete Sprache des Projekts zurueck.
	 * 
	 * @return
	 */
	public static Locale getCurrentLanguage() {
		Application app = Application.getDefault();
		if (app.currentProject != null) {
			return app.currentProject.getCurrentLanguage();
		} else {
			return null;
		}
	}

	/**
	 * Fuegt einen Listener fuer diese Applikation hinzu, der ueber
	 * Applikationsveraenderungen informiert wird.
	 * 
	 * @param listener
	 *            Der neue Listener
	 */
	public void addApplicationListener(ApplicationListener listener) {
		if (!applicationListeners.contains(listener)) {
			applicationListeners.add(listener);
		}
	}

	/**
	 * Entfernt einen Listener wieder von dieser Applikation.
	 * 
	 * @param listener
	 *            Der zu entfernende Listener
	 */
	public void removeApplicationListener(ApplicationListener listener) {
		applicationListeners.remove(listener);
	}

	/**
	 * Benachrichtigt alle Konsumenten, dass ein neues Project geoeffnet wurde.
	 * 
	 * @param project
	 *            Das geoeffnete Projekt.
	 */
	private void fireProjectOpened(Project project) {
		for (ApplicationListener listener : applicationListeners) {
			listener.onProjectOpened(project);
		}
	}

	/**
	 * Benachrichtigt alle Konsumenten, dass ein Project geschlossen wurde.
	 * 
	 * @param project
	 *            Das geschlossene Projekt.
	 */
	private void fireProjectClosed(Project project) {
		for (ApplicationListener listener : applicationListeners) {
			listener.onProjectClosed(project);
		}
	}

	/**
	 * Gibt das geöffnete Projekt zurück.
	 * 
	 * @return das geöffnete Projekt
	 */
	public static Project getCurrentProject() {
		return getDefault().currentProject;
	}

	/**
	 * Setzt das geladene Projekt.
	 * 
	 * @param newProject
	 */
	public void setCurrentProject(Project newProject) {
		Project oldProject = getCurrentProject();
		String windowTitle = Messages.Application_WindowTitle;
		if (oldProject != null) {
			fireProjectClosed(oldProject);
			// falls beide Projekte gleich sind lösche die 
			// UNDO/REDO Historie nicht
			if (newProject != null) {
				if (!oldProject.equals(newProject)) {
					OperationHistory.clearHistory();
				}
			}
		} 
		currentProject = newProject;
		if (newProject != null) {
			windowTitle += " - " + newProject.getTitle(); //$NON-NLS-1$
			fireProjectOpened(newProject);
		} else {
			OperationHistory.clearHistory();
		}
		// Fenster-Titel setzten
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
				.setText(windowTitle);			
	}

	/**
	 * Returns a given view if it is open in the current workbench.
	 * 
	 * @param id
	 *            The ID of the view.
	 * @return The Viewpart of the given view or null, if the view is currently
	 *         not open.
	 */
	public IViewPart getView(String id) {
		IViewReference viewReferences[] = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getViewReferences();
		for (int i = 0; i < viewReferences.length; i++) {
			if (id.equals(viewReferences[i].getId())) {
				return viewReferences[i].getView(false);
			}
		}
		return null;
	}

	/**
	 * Gibt den Editor fuer einen bestimmten Input zurueck, wenn er instaniiert
	 * bzw geoeffnet ist. Ist er nicht offen, wird null zurueck gegeben.
	 * 
	 * @param input
	 *            Der Input des Editors.
	 * @return Den Editor-Part fuer den gegebenen Input.
	 */
	public static IEditorPart getEditor(IEditorInput input) {
		if (input == null) {
			return null;
		}
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null) {
			return null;
		}
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return null;
		}
		IEditorReference[] editorReferences = page.getEditorReferences();
		for (IEditorReference reference : editorReferences) {
			try {
				if (input.equals(reference.getEditorInput())) {
					return reference.getEditor(false);
				}
			} catch (PartInitException e) {
				logger.error("Could not get editor input for editor: " //$NON-NLS-1$
						+ reference.getId());
			}
		}
		return null;
	}

	/**
	 * Gibt an, ob momentan ein Projekt geoeffnet ist.
	 * 
	 * @return True, wenn Projekt geoeffnet, ansonsten False.
	 */
	public boolean isProjectOpen() {
		return currentProject != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.
	 * IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {
		// Parse application arguments
		String theme = null;
		PathHelper.setLibDir("."); //$NON-NLS-1$
		String[] args = Platform.getApplicationArgs();
				
		for (int i=0; i < args.length; i++) {
			String arg = args[i].toLowerCase();
			
			// all parameters need a value at the moment
			if (i+1 > args.length) {
				logger.error("Missing parameter value: " + arg); //$NON-NLS-1$
				MessageDialog errDialog = new MessageDialog(Display.getDefault()
						.getActiveShell(), Messages.Application_Msgbox_Title_Error,
						null, Messages.Application_Msgbox_MissingParameter 
								+ arg + "\"", //$NON-NLS-1$
						MessageDialog.ERROR,
						new String[] {Messages.Application_MsgBox_OkButton},
						0);
				errDialog.open();
				return IApplication.EXIT_OK;
			}			
			
			if (arg.equals(DEBUG_PARAMNAME)) {
				LogManager.resetConfiguration();
				File configFile = new File(args[++i]);
				if (configFile.exists()) {
					PropertyConfigurator.configure(configFile.getAbsolutePath());
				} else {
					logger.error("Could not find specified log4j property file: "  //$NON-NLS-1$
							+ configFile.getCanonicalPath()); //$NON-NLS-1$
					MessageDialog errDialog = new MessageDialog(Display.getDefault()
							.getActiveShell(), Messages.Application_Msgbox_Title_Error,
							null, Messages.Application_MsgBox_log4j_properties_1
									+ Messages.Application_MsgBox_log4j_properties_2
									+ configFile.getCanonicalPath() + "\"", //$NON-NLS-1$
									MessageDialog.ERROR,
									new String[] {Messages.Application_MsgBox_OkButton},
									0);
					errDialog.open();
					return IApplication.EXIT_OK;
				}

			} else if (arg.equals(LIBDIR_PARAMNAME)) {
				PathHelper.setLibDir(args[++i]);
			} else if (arg.equals(THEME_PARAMNAME)) {
				theme = args[++i];
			} else {
				logger.error("Unknown parameter: " + arg); //$NON-NLS-1$
				MessageDialog errDialog = new MessageDialog(Display.getDefault()
						.getActiveShell(), Messages.Application_Msgbox_Title_Error,
						null, Messages.Application_Msgbox_UnknownParameter 
								+ arg + "\"", //$NON-NLS-1$
						MessageDialog.ERROR,
						new String[] {Messages.Application_MsgBox_OkButton},
						0);
				errDialog.open();
				return IApplication.EXIT_OK;
			}
		}
				
		// Set the workspace directory where the Eclipse .metadata should
		// be stored.
		File appDataDirFile = PathHelper
				.getAppDataDirectory(Application.PLUGIN_NAME);
		appDataDirFile.mkdirs();
		String applicationDataDir = appDataDirFile.getAbsolutePath();
		Location instanceLoc = Platform.getInstanceLocation();
		instanceLoc.release();
		instanceLoc.set(new URL("file", null,  //$NON-NLS-1$
				applicationDataDir), false); //$NON-NLS-1$
		PathHelper.setSivaDir(applicationDataDir);		
		logger.info("Using data directory: "  //$NON-NLS-1$
				+ applicationDataDir); //$NON-NLS-1$
		
		Themes themeObj = Themes.getThemes(theme);
		Colors.setColorProvider(themeObj.getColorProvider());

		// Checking library directories for VLC, FFMPEG		
		String vlcPath = PathHelper.getPathToVLC().getPath();
		System.setProperty("jna.library.path", vlcPath); //$NON-NLS-1$
		
		File ffmpegExe = PathHelper.getPathToFFMpegExe();
		if (ffmpegExe.exists() && ffmpegExe.canRead()) {
			logger.info("Found FFMPEG executable: " //$NON-NLS-1$
					+ ffmpegExe.getAbsolutePath());
		} else {
			logger.error("Could not find FFMPEG executable at " //$NON-NLS-1$
					+ ffmpegExe.getAbsolutePath());
			MessageDialog errDialog = new MessageDialog(
					Display.getDefault().getActiveShell(),
					Messages.Application_Msgbox_Title_Error,
					null,
					Messages.Application_Msgbox_ffmpeg
							+ ffmpegExe.getAbsolutePath() + "\"", //$NON-NLS-1$
					MessageDialog.ERROR,
					new String[] {Messages.Application_MsgBox_OkButton},
					0);
			errDialog.open();
			return IApplication.EXIT_OK;
		}
		
		MediaMetaSystem.getInstance();		

		Display display = PlatformUI.createDisplay();
		try {
			int returnCode = PlatformUI.createAndRunWorkbench(display,
					new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART)
				return IApplication.EXIT_RESTART;
			else
				return IApplication.EXIT_OK;
		} finally {
			display.dispose();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
}
