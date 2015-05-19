package org.iviPro.actions.nondestructive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTMLDocument;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.IMediaObject;
import org.iviPro.model.LocalizedFile;
import org.iviPro.model.Project;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.RichText;
import org.iviPro.model.resources.Video;
import org.iviPro.theme.Icons;
import org.iviPro.utils.HtmlDocumentUtil;
import org.iviPro.utils.PathHelper;
import org.iviPro.utils.PreferencesHelper;

import com.thoughtworks.xstream.XStream;

/**
 * Action zum Weitergeben des Projekts an jemand anderem. Dabei wird das Projekt
 * inkl. aller Dateien zusammengepackt in einen einzige Ordner, der sich dann
 * z.B. auf einem anderen Computer oeffnen laesst.
 * 
 * @author Christian Dellwo
 */
public class ProjectHandOverAction extends Action implements IWorkbenchAction,
		ApplicationListener {
	private static Logger logger = Logger
			.getLogger(ProjectHandOverAction.class);
	public final static String ID = ProjectHandOverAction.class.getName();
	public final static String ACTIONDEFINITIONID = "org.iviPro.ui.ProjectHandOverAction"; //$NON-NLS-1$

	private final IWorkbenchWindow window;

	/**
	 * Konstruktor...
	 * 
	 * @param window
	 */
	public ProjectHandOverAction(IWorkbenchWindow window) {
		this.window = window;
		setEnabled(false);
		setId(ID);
		setText(Messages.ProjectHandOverAction_Title);
		setToolTipText(Messages.ProjectHandOverAction_Tooltip);
		setImageDescriptor(Icons.ACTION_PROJECT_HANDOVER.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_PROJECT_HANDOVER
				.getDisabledImageDescriptor());
		setActionDefinitionId(ACTIONDEFINITIONID);
		Application.getDefault().addApplicationListener(this);

	}

	private static final String PREF_PROJECT_HANDOVER_PATH = "PREF_PROJECT_HANDOVER_PATH"; //$NON-NLS-1$

	/**
	 * Die eiegentliche Ausführungslogik der Action.
	 */
	@Override
	public void run() {
		Project project = Application.getCurrentProject();
		File projectDir = project.getFile().getValue().getParentFile();

		String targetDirPath = selectTargetDirectory(projectDir);
		if (targetDirPath == null) {
			// Benutzer hat den Vorgang abgebrochen
			logger.info("Action aborted by user"); //$NON-NLS-1$
			return;
		}
		File targetDir = new File(targetDirPath);
		PreferencesHelper.storePreference(PREF_PROJECT_HANDOVER_PATH, targetDir
				.getParentFile().getAbsolutePath());

		if (!targetDir.exists()) {
			// Zielverzeichnis existiert nicht oder konnte nicht erstellt
			// werden.
			MessageDialog
					.openError(
							window.getShell(),
							Messages.ProjectHandOverAction_ErrorCouldNotCreateTargetDir_Title,
							Messages.ProjectHandOverAction_ErrorCouldNotCreateTargetDir_Msg1
									+ targetDir.getAbsolutePath()
									+ Messages.ProjectHandOverAction_ErrorCouldNotCreateTargetDir_Msg2);
			return;
		}
		logger.info("Target directory: " + targetDirPath); //$NON-NLS-1$

		// Kopiere alle Dateien.
		HandOverProgress progress = new HandOverProgress(targetDir);
		try {
			new ProgressMonitorDialog(window.getShell()).run(true, true,
					progress);
		} catch (InvocationTargetException e) {
			// TODO: Fehler ausgeben
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO: Fehler ausgeben
			e.printStackTrace();
		}

	}

	/**
	 * Erstelle eine Liste mit allen zu kopierenden Dateien.
	 * 
	 * @param project
	 *            Aktuell verwendetes Projekt
	 * @param targetDirPath
	 *            Ziel fuer neues Projektverzeichnis
	 * 
	 * @return Eine Liste der zu kopierenden Dateien
	 */
	private Map<String, String> calculateFilesToCopy(Project project,
			String targetDirPath) {
		// Neue rueckgabe Liste erstellen
		Map<String, String> filesToCopy = new HashMap<String, String>();
		// Alle Eintraege aus dem Mediarepo. holen
		List<IAbstractBean> mediaObjects = project.getMediaObjects();
		// Neue Pfade der Eintraege ermitteln
		for (IAbstractBean mediaObj : mediaObjects) {
			if (mediaObj instanceof IMediaObject) {
				IMediaObject medObj = (IMediaObject) mediaObj;
				// Passendes Unterverzeichnis ermitteln
				String subTargetDir = targetDirPath + File.separator
						+ getSubdir(medObj) + File.separator;
				Collection<LocalizedFile> files = medObj.getFiles();
				for (LocalizedFile file : files) {
					String targetFilePath = subTargetDir
							+ file.getValue().getName();
					filesToCopy.put(file.getAbsolutePath(), targetFilePath);
					logger.info("Copy " + file.getAbsolutePath() + "  ==>  " //$NON-NLS-1$ //$NON-NLS-2$
							+ targetFilePath);
				}
			}
		}
		return filesToCopy;
	}

	/**
	 * Ermittelt das Unterverzeichnis fuer die uebergebene Datei aus dem
	 * Medienrepo.
	 * 
	 * @param fileBasedObj
	 *            Die Datei aus dem Medienrepo.
	 * @return Gibt das Unterverzeihnis fuer den Typ von Datei zurueck, wenn der
	 *         Typ nich bekannt ist wird null zurueck gegeben.
	 */
	private String getSubdir(IFileBasedObject fileBasedObj) {
		if (fileBasedObj instanceof Video) {
			return Project.SUBDIRECTORY_MEDIA + File.separator
					+ Project.SUBDIRECTORY_VIDEO;
		} else if (fileBasedObj instanceof Audio) {
			return Project.SUBDIRECTORY_MEDIA + File.separator
					+ Project.SUBDIRECTORY_AUDIO;
		} else if (fileBasedObj instanceof RichText) {
			return Project.SUBDIRECTORY_MEDIA + File.separator
					+ Project.SUBDIRECTORY_RICHTEXT;
		} else if (fileBasedObj instanceof Picture) {
			return Project.SUBDIRECTORY_MEDIA + File.separator
					+ Project.SUBDIRECTORY_PICTURE;
		}
		return null;
	}

	private String selectTargetDirectory(File projectDir) {
		boolean done = false;
		while (!done) {
			DirectoryDialog dialog = new DirectoryDialog(window.getShell(),
					SWT.OPEN);
			dialog.setMessage(Messages.ProjectHandOverAction_DirectoryDialogText);
			String defaultPath = PreferencesHelper.getPreference(
					PREF_PROJECT_HANDOVER_PATH, null);
			if (defaultPath != null) {
				dialog.setFilterPath(defaultPath);
			}
			String path = dialog.open();
			if (path == null) {
				// Benutzer hat den Dialog abgebrochen.
				done = true;
			} else {
				File targetDir = new File(path + File.separator
						+ projectDir.getName() + File.separator);

				if (targetDir.getAbsolutePath().equals(
						projectDir.getAbsolutePath())) {
					// Zielverzeichnis ungueltig:
					// Man kann nicht in das gleiche Verzeichnis exportieren,
					// wo das aktuelle Projekt liegt.
					MessageDialog
							.openError(
									window.getShell(),
									Messages.ProjectHandOverAction_ErrorSameDirectory_Title,
									Messages.ProjectHandOverAction_ErrorSameDirectory_Msg);
				} else {
					// Zielverzeichnis ist gueltig:
					// Wenn Zielverzeichnis bereits existiert, dann Frage
					// Benutzer ob es ueberschrieben werden soll.
					if (targetDir.exists()) {
						if (!MessageDialog
								.openQuestion(
										window.getShell(),
										Messages.ProjectHandOverAction_WarningOverwrite_Title,
										Messages.ProjectHandOverAction_WarningOverwrite_Msg)) {
							// Nicht ueberschreiben => Abbruch
							return null;
						}
					}
					// Verzeichnis anlegen und pruefen ob es valide ist
					targetDir.mkdirs();
					if (targetDir.exists() && targetDir.canWrite()
							&& targetDir.isDirectory()) {
						return targetDir.getAbsolutePath();
					} else {
						MessageDialog
								.openError(
										window.getShell(),
										Messages.ProjectHandOverAction_ErrorDirectoryInvalid_Title,
										Messages.ProjectHandOverAction_ErrorDirectoryInvalid_Msg);
					}

				}
			}
		}
		// Benutzer hat den Dialog abgebrochen und kein Verzeichnis gewaehlt.
		return null;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void onProjectClosed(Project project) {
		setEnabled(false);
	}

	@Override
	public void onProjectOpened(Project project) {
		setEnabled(true);
	}

	private void writeProjectInformation(String xml, File file)
			throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		OutputStreamWriter writer = new OutputStreamWriter(out,
				Charset.forName("UTF-8")); //$NON-NLS-1$
		writer.write(xml);
		writer.flush();
		writer.close();
		out.close();

	}

	/**
	 * Aktuallisiert von allen img-Tags das src-Attribut mit dem neuen
	 * Verzeichnis.
	 * 
	 * @param targetDirPath
	 *            Pfad zum neuen Projektverzeichnis.
	 * @throws BadLocationException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void updateRichtext(Project project, String targetDirPath)
			throws FileNotFoundException, IOException, BadLocationException {
		List<IAbstractBean> mediaObjects = project.getMediaObjects();
		for (IAbstractBean richtext : mediaObjects) {
			if (richtext instanceof RichText) {
				boolean changed = false;
				String fileName = ((RichText) richtext).getFile().getValue()
						.getName();
				File htmlFile = new File(targetDirPath + File.separator
						+ getSubdir((RichText) richtext) + File.separator
						+ fileName);
				HTMLDocument doc = HtmlDocumentUtil.loadDocument(htmlFile);
				for (IAbstractBean picture : mediaObjects) {
					if (picture instanceof Picture) {
						Picture mediaObject = (Picture) picture;
						ElementIterator iterator = new ElementIterator(doc);
					    Element element;
					    while ((element = iterator.next()) != null) {
							if (mediaObject.getId().equals(
									element.getAttributes().getAttribute(
											Attribute.ID))) {
								fileName = mediaObject.getFile().getValue()
										.getName();
								File newFile = new File(targetDirPath
										+ File.separator
										+ getSubdir(mediaObject)
										+ File.separator + fileName);
								HtmlDocumentUtil.setAttribute(element, Attribute.SRC,
										newFile.toURI());
								changed = true;
							}
						}
					}
				}
				if (changed) {
					HtmlDocumentUtil.saveDocument(htmlFile, doc);
				}
			}
		}
	}

	private class HandOverProgress implements IRunnableWithProgress {

		private final File targetDir;

		HandOverProgress(File targetDir) {
			this.targetDir = targetDir;
		}

		@Override
		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
			String taskTitle = Messages.ProjectHandOverAction_ProgressTitle;

			// Schritt 1: Erstelle Liste der zu kopierenden Dateien
			monitor.beginTask(taskTitle, 1);
			monitor.subTask(Messages.ProjectHandOverAction_ProgressTaskPreparing);
			Project project = Application.getCurrentProject();
			String targetDirPath = targetDir.getAbsolutePath();
			Map<String, String> filesToCopy = calculateFilesToCopy(project,
					targetDirPath);
			monitor.beginTask(taskTitle, filesToCopy.size() + 2);

			// Check ob User Export abbrechen moechte
			if (monitor.isCanceled()) {
				return;
			}

			// Schritt 2: Serialisiere Projekt-Datei
			monitor.subTask(Messages.ProjectHandOverAction_ProgressTaskSerialising);
			XStream xstream = new XStream();
			String xml = xstream.toXML(project);

			// Check ob User Export abbrechen moechte
			if (monitor.isCanceled()) {
				return;
			}

			// Schritt 3: Kopiere Dateien
			Display display = window.getShell().getDisplay();
			Set<String> originalFilePaths = filesToCopy.keySet();
			for (final String origFilePath : originalFilePaths) {
				// Check ob User Export abbrechen moechte
				if (monitor.isCanceled()) {
					return;
				}
				File origFile = new File(origFilePath);
				File targetFile = new File(filesToCopy.get(origFilePath));
				monitor.subTask(Messages.ProjectHandOverAction_ProgressTaskProcessingFile
						+ origFile.getName());
				try {
					// Kopiere Datei und ersetze deren Pfad im XML der
					// serialisierten Projekt-Information
					targetFile.getParentFile().mkdirs();
					PathHelper.copyFile(origFile, targetFile, true);
					String oldLink = "<file>" + origFile.getAbsolutePath() //$NON-NLS-1$
							+ "</file>"; //$NON-NLS-1$
					String newLink = "<file>" + targetFile.getAbsolutePath() //$NON-NLS-1$
							+ "</file>"; //$NON-NLS-1$
					xml = xml.replace(oldLink, newLink);
				} catch (IOException e) {
					logger.error("Could not copy File: " + origFilePath //$NON-NLS-1$
							+ " ==> " + targetFile.getAbsolutePath()); //$NON-NLS-1$
					display.syncExec(new Runnable() {

						@Override
						public void run() {
							MessageDialog.openError(
									window.getShell(),
									Messages.ProjectHandOverAction_ErrorFailedCopyFile_Title,
									Messages.ProjectHandOverAction_ErrorFailedCopyFile_Msg
											+ origFilePath);
						}

					});
					monitor.done();
					return;
				}
				monitor.worked(1);
			}

			// Bilder-Pfade in Richtext-Annotationen aktualisieren
			try {
				updateRichtext(project, targetDirPath);
			} catch (Exception e) {
				logger.error("Error while updateing Richtext-Files: " + targetDirPath); //$NON-NLS-1$
				display.syncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openError(
								window.getShell(),
								Messages.ProjectHandOverAction_ErrorFailedRichtextImageSource_Title,
								Messages.ProjectHandOverAction_ErrorFailedRichtextImageSource_Msg);
					}
				});
			}

			// Check ob User Export abbrechen moechte
			if (monitor.isCanceled()) {
				return;
			}
			// Schritt 4: Schreibe Projekt-Datei
			String projectFilename = project.getFile().getValue().getName();
			final File targetProjectFile = new File(targetDir, projectFilename);
			try {
				// Alten Verweis auf Projekt-Datei in der Projekt-Datei selbst
				// ersetzen.
				xml = xml.replace("<file>" //$NON-NLS-1$
						+ project.getFile().getAbsolutePath() + "</file>", //$NON-NLS-1$
						"<file>" + targetProjectFile.getAbsolutePath() //$NON-NLS-1$
								+ "</file>"); //$NON-NLS-1$
				writeProjectInformation(xml, targetProjectFile);
			} catch (IOException e) {
				display.syncExec(new Runnable() {

					@Override
					public void run() {
						MessageDialog.openError(
								window.getShell(),
								Messages.ProjectHandOverAction_ErrorFailedWriteXML_Title,
								Messages.ProjectHandOverAction_ErrorFailedWriteXML_Msg
										+ targetProjectFile.getAbsolutePath());
					}
				});
				monitor.done();
				return;
			}
			monitor.worked(1);

			// Check ob User Export abbrechen moechte
			if (monitor.isCanceled()) {
				return;
			}
			// Alle Schritte sind jetzt beendet.
			display.syncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openInformation(window.getShell(),
							Messages.ProjectHandOverAction_InfoFinished_Title,
							Messages.ProjectHandOverAction_InfoFinished_Msg);
				}
			});

			monitor.done();

		}
	}

}
