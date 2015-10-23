package org.iviPro.actions.nondestructive;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTMLDocument;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.IMediaObject;
import org.iviPro.model.LocalizedFile;
import org.iviPro.model.Project;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.resources.RichText;
import org.iviPro.model.resources.Subtitle;
import org.iviPro.operations.OperationHistory;
import org.iviPro.theme.Icons;
import org.iviPro.utils.HtmlDocumentUtil;
import org.iviPro.utils.PathHelper;
import org.iviPro.utils.PreferencesHelper;
import org.iviPro.views.mediarepository.MediaRepository;

import com.thoughtworks.xstream.XStream;

/**
 * Action zum Laden eines Projekts aus einer Datei.
 * 
 * @author dellwo
 */
public class ProjectOpenAction extends Action implements ISelectionListener,
		IWorkbenchAction, ApplicationListener {

	private static Logger logger = Logger.getLogger(ProjectOpenAction.class);
	public final static String ID = ProjectOpenAction.class.getName();
	public final static String ACTIONDEFINITIONID = "org.iviPro.ui.ProjectOpenAction"; //$NON-NLS-1$

	// Moegliche Werte fuer die Rueckgabe von checkPaths()
	private static final int PATH_OK = 0;
	private static final int PATH_CORRECTED = 1;
	private static final int PATH_NOT_OK = 2;

	private final IWorkbenchWindow window;

	/**
	 * Liste der Verzeichnisse die der Benutzer manuell bei der Relokation der
	 * Dateien gewaehlt hat. In diesen Ordnern suchen wir dann andere fehlende
	 * Dateien ebenfalls und passen deren Pfade auf diese Weise semi-automatisch
	 * an.
	 * 
	 * @see #semiAutomaticallyRelocatedFiles
	 */
	private List<File> manuallySelectedDirs = new ArrayList<File>();

	/**
	 * List der semi-automatisch relokierten Dateien. Key ist der alte
	 * Original-Dateipfad, Value ist das LocalizedFile Objekt das veraendert
	 * wurde und den automatisch gewaehlten Pfad beinhaltet.
	 * 
	 * @see #manuallySelectedDirs
	 */
	private Map<File, LocalizedFile> semiAutomaticallyRelocatedFiles = new HashMap<File, LocalizedFile>();

	/**
	 * TODO DOK
	 */
	private List<IAbstractBean> relocatedFiles = new LinkedList<IAbstractBean>();

	/**
	 * Konstruktor...
	 * 
	 * @param window
	 */
	public ProjectOpenAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText(Messages.LoadProjectAction_LoadProject);
		setToolTipText(Messages.LoadProjectAction_LoadProjectToolTip);
		setImageDescriptor(Icons.ACTION_PROJECT_OPEN.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_PROJECT_OPEN
				.getDisabledImageDescriptor());
		setActionDefinitionId(ACTIONDEFINITIONID);
		window.getSelectionService().addSelectionListener(this);
		Application.getDefault().addApplicationListener(this);
		setAccelerator(SWT.CTRL | 'o');
	}

	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

	private static final String PREF_PROJECT_OPEN_DIALOG_PATH = "PREF_PROJECT_OPEN_DIALOG_PATH"; //$NON-NLS-1$

	/**
	 * Die eiegentliche Ausführungslogik der Action.
	 */
	@Override
	public void run() {

		// öffnet den Filedialog
		Shell shell = new Shell(Display.getDefault());
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		String defaultPath = PreferencesHelper.getPreference(
				PREF_PROJECT_OPEN_DIALOG_PATH, null);
		if (defaultPath != null) {
			dialog.setFilterPath(defaultPath);
		}

		// TODO hier nur windoof filter!
		dialog.setFilterExtensions(new String[] { "*." //$NON-NLS-1$
				+ Project.PROJECT_FILE_EXTENSION });
		String selectedPath = dialog.open();
		
		if (selectedPath == null) {
			// Keine Projekt-Datei gewaehlt => Abbruch
			return;
		}
		
		String usedPath = selectedPath;
		
		File backupFile = new File(selectedPath + "." + Project.PROJECT_BACKUP_FILE_EXTENSION); //$NON-NLS-1$
		if(backupFile.exists()){
			MessageBox messageBox = new MessageBox(Display.getCurrent()
					.getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO
					| SWT.CANCEL);
			messageBox
					.setMessage(org.iviPro.actions.nondestructive.Messages.ProjectOpenAction_MsgBox_BackupAvailable_Text);
			messageBox.setText(org.iviPro.actions.nondestructive.Messages.ProjectOpenAction_MsgBox_BackupAvailable_Title);

			int option = messageBox.open();
			if (option == SWT.YES) {
				// Backup benutzen
				usedPath = usedPath + "." + Project.PROJECT_BACKUP_FILE_EXTENSION; //$NON-NLS-1$
			}
		}
		File selectedFile = new File(selectedPath);
		
		PreferencesHelper.storePreference(PREF_PROJECT_OPEN_DIALOG_PATH,
				selectedFile.getParentFile().getAbsolutePath());
		
		// Oeffene serialisierte Projekt-Datei
		logger.debug("Unserializing project information."); //$NON-NLS-1$
		Project project = null;
		try {

			// Serialisierte Projekt-Daten aus XML-Datei mit XStream einlesen
			FileInputStream fis = new FileInputStream(usedPath);
			InputStreamReader in = new InputStreamReader(fis, "UTF-8"); //$NON-NLS-1$
			XStream xstream = new XStream();
			Object obj = xstream.fromXML(in);
			in.close();
			fis.close();
			
			if (obj instanceof Project) {
				project = (Project) obj;
				project.setFile(selectedFile);
				project.setTitle(selectedFile.getName().replaceAll("." + Project.PROJECT_FILE_EXTENSION, "")); //$NON-NLS-1$ //$NON-NLS-2$
				int checkResult = checkPaths(project, selectedFile);
				if (checkResult == PATH_NOT_OK) {
					return;
				} else {
					if (!semiAutomaticallyRelocatedFiles.isEmpty()) {
						showRelocationConfirmationDialog(semiAutomaticallyRelocatedFiles);
					}
					Application.getDefault().setCurrentProject(project);
					if (checkResult == PATH_CORRECTED) {
						// Pfade in Annotationen aendern
						updateRichPaths(project, relocatedFiles);
						// enable save button for persisting path changes
						if (!OperationHistory.hasUnsavedChanges()) {
							try {
								OperationHistory.execute(new DummyOperation());
							} catch (ExecutionException e) {
								logger.error(e);
							}
						}
					}
				}

			}
		} catch (Exception e) {
			MessageDialog.openError(shell,
					Messages.ProjectOpenAction_ErrorMsgBox_Title,
					e.getMessage());
		}

		// Oeffne Szenen-Graph, falls Projekt erfolgreich geoeffnet wurde:
		if (project != null) {
			project.openProjectSetData();

			// Zu Beginn gleich Szenen-Graph oeffnen, falls Projekt mehr
			// als den Start- und Endknoten im Graphen enthaelt.
			if (project.getSceneGraph().getNodes().size() >= 2) {

				new OpenSceneGraphAction(window).run();
			} else {
				// Ansonsten sollte am Anfang das Media-Repository aktiviert
				// sein
				IViewPart mediaRepository = Application.getDefault().getView(
						MediaRepository.ID);
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().activate(mediaRepository);
			}

			// damit alte Subtitle Annotationen auch funktionieren, müssen diese
			// zu den MediaObjects hinzugefügt werden
			List<IGraphNode> subtitleNodes = Application.getCurrentProject()
					.getSceneGraph().searchNodes(NodeAnnotationSubtitle.class);
			for (IGraphNode gn : subtitleNodes) {
				if (gn instanceof NodeAnnotationSubtitle) {
					Subtitle newSub = ((NodeAnnotationSubtitle) gn)
							.getSubtitle();
					project.getMediaObjects().add(newSub);
				}
			}
		}

	}

	/**
	 * Prueft ob die Pfade in dem gegebenen Projekt noch stimmen. Insbesondere
	 * die relativen Pfade unterhalb der Projekt-Datei werden entsprechend
	 * angepasst.
	 * 
	 * @param project
	 * @param currentProjectFile
	 * @return Gibt einen int Wert zurück, der das Ergebnis der Pruefung 
	 * repraesentiert.
	 * <li> 0=Pfade waren korrekt</li>
	 * <li> 1=Pfade konnten angepasst werden</li>
	 * <li> 2=Pfade konnten nicht angepasst werden </li>  
	 */
	private int checkPaths(Project project, File currentProjectFile) {

		// Altes und neues Projekt-Verzeichnis bestimmen
		String oldProjectDir = project.getFile().getValue().getParentFile()
				.getAbsolutePath();
		String curProjectDir = currentProjectFile.getParentFile()
				.getAbsolutePath();

		// Alle dateibasierten Objekte in Work-Queue einfuegen
		Queue<IFileBasedObject> workQueue = new LinkedList<IFileBasedObject>();
		workQueue.add(project);
		for (IAbstractBean bean : project.getMediaObjects()) {
			if (bean instanceof IFileBasedObject) {
				workQueue.add((IFileBasedObject) bean);
			}
		}

		// Datei-Dialog der zur manuellen Relokalisierung von Dateien
		// verwendet werden soll.
		Shell shell = new Shell(Display.getDefault());
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);

		// Alle dateibasierten Objekte in der Workqueue abarbeiten
		int result = PATH_OK;
		while (!workQueue.isEmpty()) {
			IFileBasedObject fileBasedObj = workQueue.poll();
			logger.debug("Checking paths of: " + fileBasedObj); //$NON-NLS-1$
			for (LocalizedFile locFile : fileBasedObj.getFiles()) {
				String filePath = locFile.getAbsolutePath();
				logger.debug("Checking file path: " + filePath); //$NON-NLS-1$

				// Wenn Datei nicht im Projekt-Ordner war und existiert, dann
				// ist der Datei-Pfad OK
				boolean fileOK = false;
				if (filePath.startsWith(oldProjectDir)) {
					fileOK = oldProjectDir.equals(curProjectDir)
							&& locFile.exists();
				} else {
					fileOK = locFile.exists();
				}
				if (!fileOK) {
					// wenn eines der files nicht relocated werden kann den
					// ladevorgang abbrechen sonst weiter pruefen
					if (relocateFile(locFile, oldProjectDir, curProjectDir,
							dialog)) {
						if (fileBasedObj instanceof IMediaObject) {
							// Liste von geaenderten Dateien
							relocatedFiles.add((IMediaObject) fileBasedObj);
						}
						result = PATH_CORRECTED;
					} else {
						return PATH_NOT_OK;
					}
				}
			}
		}
		return result;

	}

	/**
	 * Checkt ob eine Datei korrekt ist, d.h. ob sie existiert usw.
	 * 
	 * @param locFile
	 *            Die zu checkende Datei.
	 * @param oldProjectDir
	 *            Das alte Projekt-Verzeichnis, so wie es in der Projekt-Datei
	 *            stand.
	 * @param curProjectDir
	 *            Das aktuelle reale Projekt-Verzeichnis, wo die Projekt-Datei
	 *            wirklcih liegt.
	 * @param dialog
	 *            Der Datei-Dialog der zur Relokalisierung von Dateien benutzt
	 *            werden soll.
	 * @return True, wenn die Datei valide ist und erfolgreich geprueft wurde.
	 */
	private boolean relocateFile(LocalizedFile locFile, String oldProjectDir,
			String curProjectDir, FileDialog dialog) {
		String filePath = locFile.getAbsolutePath();
		logger.debug("Checking file path: " + filePath); //$NON-NLS-1$
		// Wenn Datei nicht im Projekt-Ordner war und existiert, dann
		// ist der Datei-Pfad OK
		if (!filePath.startsWith(oldProjectDir) && locFile.exists()) {
			return true;
		}
		// Datei existiert nicht oder liegt im Projekt-Ordner.
		// Falls Datei in Ordner unterhalb des Projekt-Ordners lag,
		// versuchen wir erstmal, den Dateipfad an aktuellen Projekt-Ordner
		// anzupassen.
		filePath = filePath.replace(oldProjectDir, curProjectDir);

		File newFile = new File(filePath);
		if (newFile.exists()) {
			logger.info("Relocated file automatically '" //$NON-NLS-1$
					+ locFile.getAbsolutePath() + "' ===> '" + filePath + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			locFile.setValue(newFile);
			return true;
		} else {
			logger.warn("File '" + locFile + "' not found."); //$NON-NLS-1$ //$NON-NLS-2$
			return relocateSemiAutomatically(locFile, dialog);
		}

	}

	private boolean relocateSemiAutomatically(LocalizedFile locFile,
			FileDialog dialog) {
		// Probieren ob wir die Datei in einem der bereits vom Benutzer manuell
		// gewaehlten Verzeichnisse finden.
		for (File dir : manuallySelectedDirs) {
			String filename = locFile.getValue().getName();
			File newFile = new File(dir, filename);
			if (newFile.exists() && newFile.isFile() && newFile.canRead()) {
				// Datein gefunden
				logger.info("Relocated file semi-automatically '" //$NON-NLS-1$
						+ locFile.getAbsolutePath() + "' ===> '" //$NON-NLS-1$
						+ newFile.getAbsolutePath() + "'"); //$NON-NLS-1$
				File oldFile = locFile.getValue();
				locFile.setValue(newFile);
				semiAutomaticallyRelocatedFiles.put(oldFile, locFile);
				return true;
			}
		}
		// Datei in keinem Verzeichnis gefunden -> Muss manuell relokalisiert
		// werden.
		return relocateManually(locFile, dialog);
	}

	/**
	 * Relocates the given file manually, by showing the user an file dialog
	 * where he can choose the new file location manually.
	 * 
	 * @param oldFile
	 *            The file to be relocated.
	 * @param dialog
	 *            The file dialog that should be used.
	 * @return True, when user has successfully relocated the file or false, if
	 *         the user has canceled the relocation.
	 */
	private boolean relocateManually(LocalizedFile oldFile, FileDialog dialog) {
		String extension = PathHelper.getExtension(oldFile.getValue());
		if (extension == null) {
			extension = "*"; //$NON-NLS-1$
		}

		// öffnet den Filedialog

		// TODO hier nur windoof filter!
		dialog.setFilterExtensions(new String[] { "*." //$NON-NLS-1$
				+ extension });
		File selectedFile = null;
		while (selectedFile == null) {
			MessageDialog.openWarning(dialog.getParent(),
					Messages.ProjectOpenAction_0, Messages.ProjectOpenAction_18
							+ oldFile.getAbsolutePath()
							+ Messages.ProjectOpenAction_19);
			dialog.setText(Messages.ProjectOpenAction_20 //
					+ oldFile.getValue().getName());
			String selectedFilePath = dialog.open();
			if (selectedFilePath != null) {
				File tmpFile = new File(selectedFilePath);
				if (tmpFile.exists() && tmpFile.canRead() && tmpFile.isFile()) {
					selectedFile = tmpFile;
				} else {
					MessageDialog.openError(dialog.getParent(),
							Messages.ProjectOpenAction_21,
							Messages.ProjectOpenAction_22);
				}
			} else {
				if (MessageDialog.openQuestion(dialog.getParent(),
						Messages.ProjectOpenAction_23,
						Messages.ProjectOpenAction_24
								+ Messages.ProjectOpenAction_25)) {
					return false;
				}
			}
		}
		// User has chosen valid file. Relocate localized file to new
		// file
		logger.info("Relocated manually:'" + oldFile.getAbsolutePath() //$NON-NLS-1$
				+ "' ===> '" + selectedFile.getAbsolutePath() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		oldFile.setValue(selectedFile);
		// Parent-Directory zur Liste der manuell gewaehlten Verzeichnisse
		// hinzufuegen.
		File parentDir = selectedFile.getParentFile().getAbsoluteFile();
		if (!manuallySelectedDirs.contains(parentDir)) {
			manuallySelectedDirs.add(parentDir);
		}

		// Fertig mit manueller relokation der datei
		return true;
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	}

	@Override
	public void onProjectClosed(Project project) {
		setEnabled(true);
	}

	@Override
	public void onProjectOpened(Project project) {
		setEnabled(false);
	}

	/**
	 * Dummy-Operation, nur damit die OperationHistory einen Eintrag hat und
	 * damit geaendert wurde. Dadurch ist das Projekt ebenfalls als geaendert
	 * markiert und z.B. die Speichern-Action ist dann erst aktiviert.
	 * 
	 * @author dellwo
	 * 
	 */
	private class DummyOperation extends AbstractOperation {

		public DummyOperation() {
			super(DummyOperation.class.getName());
		}

		@Override
		public boolean canRedo() {
			return false;
		}

		@Override
		public boolean canUndo() {
			return false;
		}

		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info)
				throws ExecutionException {
			return Status.OK_STATUS;

		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info)
				throws ExecutionException {
			return Status.OK_STATUS;

		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info)
				throws ExecutionException {
			return Status.OK_STATUS;

		}

	}

	/**
	 * Zeigt den Dialog an, wo der Benutzer die semi-automatisch relokalisierten
	 * Dateien gezeigt bekommt, um diese zu Pruefen und zu bestaetigen.
	 * 
	 * @param semiAutomaticallyRelocatedFiles2
	 *            Liste der semi-automatisch relokalisierten Dateien.
	 */
	private void showRelocationConfirmationDialog(
			Map<File, LocalizedFile> semiAutomaticallyRelocatedFiles2) {

		// Fenster erstellen
		final Shell shell = new Shell(window.getShell(), SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL | SWT.RESIZE);
		shell.setLayout(new GridLayout());
		shell.setText(Messages.ProjectOpenAction_4);
		Display display = shell.getDisplay();

		// Hinweis-Text ueber der Tabelle
		Label label = new Label(shell, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		label.setText(Messages.ProjectOpenAction_1);

		// Tabelle mit den automatisch relokalisierten Dateien befuellen
		Table table = new Table(shell, SWT.MULTI | SWT.BORDER
				| SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 200;
		table.setLayoutData(layoutData);
		String[] titles = { Messages.ProjectOpenAction_TableHeaderPreviousPath,
				Messages.ProjectOpenAction_TableHeaderAdjustedPath };
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titles[i]);
		}
		for (File oldFile : semiAutomaticallyRelocatedFiles.keySet()) {
			LocalizedFile newFile = semiAutomaticallyRelocatedFiles
					.get(oldFile);
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, oldFile.getAbsolutePath());
			item.setText(1, newFile.getAbsolutePath());
		}
		for (int i = 0; i < titles.length; i++) {
			table.getColumn(i).pack();
		}

		// OK-Button
		Button okButton = new Button(shell, SWT.PUSH);
		layoutData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		layoutData.widthHint = 100;
		okButton.setLayoutData(layoutData);
		okButton.setText(Messages.ProjectOpenAction_ButtonOK);
		okButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		shell.setDefaultButton(okButton);

		// Fenster oeffnen
		shell.pack();
		shell.open();

		// Warten bis Fenster geschlossen wird.
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Funktion aktuallisiert img.src und a.href Attribute der in der Liste
	 * angegebenen Objekte in den RichtextHTML-Dateien.
	 * 
	 * @param project
	 *            Betroffenes Objekt
	 * @param dirtyObjects
	 *            MediaObjects die aktuallisiert werden sollen.
	 */
	private void updateRichPaths(Project project,
			List<IAbstractBean> dirtyObjects) {
		// MediaObjekte aus dem aktuellen Projekt
		List<IAbstractBean> mediaObjects = project.getMediaObjects();
		// Alle RichText-Dateien ueberarbeiten
		for (IAbstractBean mediaObject : mediaObjects) {
			if (mediaObject instanceof RichText) {
				try {
					// Laden der Html-Datei
					HTMLDocument doc = ((RichText) mediaObject).getDocument();
					// Alle Objekte aus der Liste durchgehen in dem
					// RichText-File suchen und gegebenenfalls aktuallisieren
					for (IAbstractBean object : dirtyObjects) {
						if (object instanceof IMediaObject) {
							IMediaObject relocatedObject = (IMediaObject) object;
							ElementIterator iterator = new ElementIterator(doc);
						    Element element;
						    while ((element = iterator.next()) != null) {
								if (relocatedObject.getId().equals(
										element.getAttributes().getAttribute(
												Attribute.ID))) {
									File file = relocatedObject.getFile()
											.getValue();
									HtmlDocumentUtil.setAttribute(element,
											Attribute.SRC, file.toURI());
									element = doc.getElement(
											doc.getDefaultRootElement(),
											Attribute.SRC,
											relocatedObject.getId());
								}
							}

						}
					}
				} catch (Exception e) {
					logger.error("Error while updating Richtextfiles.", e); //$NON-NLS-1$
				}

			}
		}
	}
}
