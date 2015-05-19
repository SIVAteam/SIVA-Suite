package org.iviPro.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.iviPro.export.ffmpeg.FfmpegTranscode;
import org.iviPro.export.smil.ExporterSIVAPlayerSMIL;
import org.iviPro.export.xml.ExporterSIVAPlayerXML;
import org.iviPro.export.xml.FileCopyInfo;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.Audio;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.LocalizedFile;
import org.iviPro.model.Picture;
import org.iviPro.model.Project;
import org.iviPro.model.RichText;
import org.iviPro.model.Scene;
import org.iviPro.model.Video;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.imageeditor.ImageObject;
import org.iviPro.utils.FileHelper;
import org.iviPro.utils.PathHelper;

/**
 * Diese Klasse dient zum Export des Projekts in das SIVA-Player XML-Format. Der
 * Export unterstuetzt dabei dir Fortschritts-Anzeige von Eclipse.
 * 
 * @author dellwo
 * 
 */
public class Exporter implements IRunnableWithProgress {

	private static Logger logger = Logger.getLogger(Exporter.class);

	private static final String EXPORT_XML_FILENAME = "export.xml"; //$NON-NLS-1$
	private static final String EXPORT_SMIL_FILENAME = "export.smil";
	private static final String EXPORT_ZIP_FILENAME = "player.zip"; //$NON-NLS-1$
	private static final String EXPORT_DIR_SUBDIR = "export_player"; //$NON-NLS-1$

	public static final String EXPORT_SUBDIR_PICTURES = "pix"; //$NON-NLS-1$
	public static final String EXPORT_SUBDIR_XML = "XML"; //$NON-NLS-1$
	public static final String EXPORT_SUBDIR_VIDEOS = "videos"; //$NON-NLS-1$
	public static final String EXPORT_SUBDIR_AUDIOS = "audios"; //$NON-NLS-1$
	public static final String EXPORT_SUBDIR_RICHTEXTS = "richpages"; //$NON-NLS-1$

	public static final String EXPORT_ZIP_TMP = "tmp"; //$NON-NLS-1$

	// Keys zum Speichern der Startzeit und Dauer des Videoinhalts der
	// Videoannotation
	// werden für Audio und Videoannotationen verwendet und in den
	// ExportParameters der Exporter gespeichert
	public static final String EXPORT_KEY_STARTTIME = "EXPORT_STARTTIME"; //$NON-NLS-1$
	public static final String EXPORT_KEY_ENDTIME = "EXPORT_ENDTIME"; //$NON-NLS-1$
	// zusätzlicher Parameter für den Pfad der exportierten Datei, ist dieser
	// gesetzt, wird
	// der Wert zum Exportpfad hinzugefügt
	public static final String EXPORT_ADDTOEXPORTFILE = "ADD_TO_EXPORT_FILE";

	/**
	 * The extension of the exported richtext files
	 */
	public final static String FILE_EXTENSION_RICHPAGE = ".html"; //$NON-NLS-1$

	/**
	 * Die Dateien in dieser Liste werden in copyFiles() nicht mit kopiert.
	 */
	private final static Set<String> FILE_COPY_BLACKLIST = new HashSet<String>();
	{
		FILE_COPY_BLACKLIST.add(".svn"); //$NON-NLS-1$

	}

	/**
	 * Ziel-Verzeichnis des Exports
	 */
	private final String exportDirectory;

	/**
	 * Ziel-Verzeichnis der Zip Datei
	 */
	private final String zipDirectory;
	/**
	 * Zu exportierendes Projekt.
	 */
	private final Project project;

	/**
	 * Art des Exports (Flash/Silverlight)
	 */
	private final ExportType type;

	/**
	 * Nur XML schreiben statt vollem XML mit Videokodierung
	 */
	private final boolean onlyXML;

	/**
	 * ExportTypes für HTML5 Export
	 */
	private ExportType[] html5Types = { ExportType.WEBM, ExportType.H264,
			ExportType.OGGTHEORA };
	private ExportType[] html5Audiotypes = { ExportType.WEBM, ExportType.H264,
			ExportType.FLASH };

	private IWorkbenchWindow window;

	/**
	 * Export als .zip
	 */
	private final boolean zip;

	/**
	 * Erstellt einen neuen Exporter fuer ein bestimmtes Projekt
	 * 
	 * @param project
	 *            Das Projekt das exportiert werden soll.
	 * @param exportDirectory
	 *            Das Verzeichnis in das exportiert werden soll.
	 * @param type
	 *            Art des Exports
	 * @param window
	 *            Das Workbenchwindow welches zur Anzeige des Fortschritts
	 *            genutzt werden soll.
	 */
	public Exporter(Project project, String exportDirectory, ExportType type,
			IWorkbenchWindow window, boolean onlyXML, boolean zip) {
		if (!exportDirectory.endsWith("" + File.separatorChar)) { //$NON-NLS-1$
			exportDirectory += File.separatorChar;
		}
		this.exportDirectory = zip ? exportDirectory + File.separator
				+ EXPORT_ZIP_TMP : exportDirectory + File.separator
				+ EXPORT_DIR_SUBDIR;
		this.zipDirectory = exportDirectory;
		this.project = project;
		this.type = type;
		this.onlyXML = onlyXML;
		this.zip = zip;
		this.window = window;
	}

	/**
	 * Erstellt ein Unterverzeichnis im Export-Verzeichnis.
	 * 
	 * @param subDirName
	 *            Name des Unterverzeichnisses
	 * @return File-Objekt des erstellten Unterverzeichnisses
	 * @throws ExportException
	 *             Falls das Unterverzeichnis nicht erstellt werden konnte.
	 */
	private File createSubDir(String subDirName) throws ExportException {
		String dir = "";
		if (type == ExportType.HTML5) {
			dir = exportDirectory + File.separator + "Files";
		} else {
			dir = exportDirectory;
		}
		File subDir = new File(dir + File.separator + subDirName);
		if (subDir.exists() && subDir.isDirectory()) {
			return subDir;
		} else if (!subDir.mkdirs()) {
			throw new ExportException(Messages.Exporter_Error_CantCreateDir_1
					+ subDir.getAbsolutePath());
		} else {
			return subDir;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core
	 * .runtime.IProgressMonitor)
	 */
	@Override
	public void run(final IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		monitor.beginTask(Messages.ExportProject_ExportInProgress, 1);

		try {
			IDManager idManager = new IDManager(type);

			File dirXMLs = createSubDir(EXPORT_SUBDIR_XML);

			Set<NodeScene> exportedSceneNodes = new HashSet<NodeScene>();
			File xmlFile = null;

			if (type == ExportType.SMIL) {
				File smilFile = new File(dirXMLs.getAbsolutePath()
						+ File.separator + EXPORT_SMIL_FILENAME);
				try {
					smilFile.createNewFile();
				} catch (IOException e1) {
					System.out.println("ioexception beim smilfile erstellen");
				}
				exportedSceneNodes = exportSMIL(project, smilFile, idManager);
			} else {
				xmlFile = new File(dirXMLs.getAbsolutePath() + File.separator
						+ EXPORT_XML_FILENAME);
				exportedSceneNodes = exportXML(project, xmlFile, idManager);
			}

			monitor.worked(1);
			// Check ob User Export abbrechen moechte
			if (monitor.isCanceled()) {
				return;
			}

			// Rest exportieren
			if (!onlyXML) {
				// // Annotations-Dateien exportieren
				monitor.subTask(Messages.ExportProject_Step3XML);
				Set<FileCopyInfo> filesToCopy = idManager
						.getGeneratedFilenames();
				copyFiles(filesToCopy, idManager, monitor);

				// Kopiere Player-Dateien
				File targetDir = new File(exportDirectory);
				File srcDir = PathHelper.getPathToPlayerFiles(type);
				try {
					PathHelper.copyFiles(srcDir, targetDir, true,
							FILE_COPY_BLACKLIST);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
					throw new ExportException(
							"Error while copying player files: " //$NON-NLS-1$
									+ e.getMessage());
				}

				// Check ob User Export abbrechen moechte
				if (monitor.isCanceled()) {
					return;
				}
				// Szenen-Videos transkodieren
				Set<Scene> exportedScenes = getUniqueScenes(exportedSceneNodes);
				transcodeVideos(exportedScenes, monitor, idManager);

				// Annotations-Videos transkodieren
				Set<FileCopyInfo> videoAnnotations = filterVideos(filesToCopy);
				transcodeVideoAnnotations(videoAnnotations, monitor, idManager);

				// Annotations-Audio transkodieren
				Set<FileCopyInfo> audioAnnotations = filterAudios(filesToCopy);
				transcodeAudioAnnotations(audioAnnotations, monitor, idManager);
			}
			// Check ob User Export abbrechen moechte
			if (monitor.isCanceled()) {
				return;
			}
			if (zip) {
				try {
					monitor.beginTask(Messages.Exporter_Monitor_Zip_File, 1);
					monitor.subTask(Messages.Exporter_Monitor_Zip_File);
					monitor.worked(1);
					new ExportZipper(zipDirectory, exportDirectory,
							EXPORT_ZIP_FILENAME);
					File dirToDelete = new File(exportDirectory);
					FileHelper.delete(dirToDelete);

				} catch (IOException e) {
					throw new ExportException(e.getMessage());
				}
			}
			window.getShell().getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openInformation(window.getShell(),
							Messages.Exporter_Success_Title,
							Messages.Exporter_Success_Message);

				}
			});

			// !TODO TEMPORARAY create copy of xml for videoplattform
			File xmlCopy = new File(dirXMLs.getAbsolutePath() + File.separator
					+ "export.xml.without-ms");

			if (type != ExportType.SMIL && xmlFile != null) {
				try {
					FileReader in = new FileReader(xmlFile);
					FileWriter out = new FileWriter(xmlCopy);
					int c;
					while ((c = in.read()) != -1) {
						out.write(c);
					}
					in.close();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			monitor.done();

		} catch (ExportException e) {

			final String finalErrMsg = e.getMessage(); //$NON-NLS-1$
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog errDialog = new MessageDialog(
							Display.getDefault().getActiveShell(),
							Messages.Exporter_MsgBox_ExportFailed_Title,
							null,
							finalErrMsg,
							MessageDialog.ERROR,
							new String[] { Messages.Exporter_MsgBox_ExportFailed_OkButton },
							0);
					errDialog.open();
				}

			});

			monitor.done();
			return;
		}

	}

	private void transcodeAudioAnnotations(Set<FileCopyInfo> audioAnnotations,
			IProgressMonitor monitor, IDManager idManager)
			throws ExportException {
		int numTranscodeSteps = audioAnnotations.size();
		int totalSteps = numTranscodeSteps;
		monitor.beginTask(Messages.ExportProject_ExportInProgress, totalSteps);
		monitor.subTask(Messages.ExportProject_Step1Preprocessing);

		File dirVids = createSubDir(EXPORT_SUBDIR_AUDIOS);

		String targetDir = dirVids.getAbsolutePath();

		int step = 0;
		for (FileCopyInfo copyInfo : audioAnnotations) {
			step++;
			if (monitor.isCanceled()) {
				return;
			}
			try {
				String outputFilename = targetDir + File.separator
						+ copyInfo.targetFilename;

				File outputFile = new File(outputFilename);
				File inputFile = new File(copyInfo.sourcePath);
				String title = copyInfo.sourceObject.getTitle();
				outputFile.delete();
				// Ausgabe-Datei vorher loeschen
				// Video umwandeln.
				// Video umwandeln.
				long startTime = -1;
				long duration = -1;
				Object startTimeObject = copyInfo.parameters
						.getValue(Exporter.EXPORT_KEY_STARTTIME);
				Object durTimeObject = copyInfo.parameters
						.getValue(Exporter.EXPORT_KEY_ENDTIME);
				if (startTimeObject instanceof Long
						&& durTimeObject instanceof Long) {
					startTime = (Long) startTimeObject;
					duration = (Long) durTimeObject;
				}
				String taskText = Messages.Exporter_TaskTranscodingAudio;
				taskText = taskText.replace("%1", "" + step); //$NON-NLS-1$ //$NON-NLS-2$
				taskText = taskText.replace("%2", "" + totalSteps); //$NON-NLS-1$ //$NON-NLS-2$
				taskText = taskText.replace("%3", "" + title); //$NON-NLS-1$ //$NON-NLS-2$	
				transcodeAudio(inputFile, outputFile, taskText, 0, monitor,
						step, numTranscodeSteps, startTime, duration);

			} catch (Exception e) {
				monitor.done();
				throw new ExportException(e);
			}
		}
		monitor.worked(1);
	}

	/**
	 * Transkodiert ein Audio
	 * 
	 * @param inputFile
	 *            Die Quell-Datei
	 * @param outputFile
	 *            Die Ziel-Datei
	 * @param taskText
	 *            Anzeigetext fuer den Task im Progress-Monitor
	 * @param monitor
	 *            Der Progress-Monitor zur Fortschrittsanzeige
	 * @param step
	 *            Der aktuelle Schritt fuer den Progress-Monitor
	 * @param totalSteps
	 *            Die Anzahl der gesamten Schritte
	 */
	private void transcodeAudio(final File inputFile, final File outputFile,
			String taskText, int i, final IProgressMonitor monitor, int step,
			int totalSteps, long startTime, long endTime) {

		// Step 2: Transcoding scene %1 of %2 into FLV format: %3
		monitor.subTask(taskText);
		// monitor.worked(1);

		try {
			if (type == ExportType.HTML5) {
				SubProgressMonitor submonitor = new SubProgressMonitor(monitor,
						1);
				submonitor.beginTask("Converting files", 3);
				for (ExportType currentType : html5Audiotypes) {
					submonitor.subTask(taskText + " in "
							+ currentType.getDisplayString());
					File currFile = new File(outputFile.getPath()
							+ currentType.getAudioExtension());
					FfmpegTranscode.transcodeAudio(inputFile, currFile,
							currentType, startTime, endTime);
					submonitor.worked(1);
				}
				submonitor.done();
			} else {
				FfmpegTranscode.transcodeAudio(inputFile, outputFile, type,
						startTime, endTime);
			}
			monitor.worked(1);
		} catch (ExportException e) {
			final String errorMsg = e.getMessage();
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					final Shell shell = new Shell(Display.getDefault());
					final MessageDialog errDialog = new MessageDialog(
							shell,
							Messages.Exporter_MsgBox_ExportFailed_Title,
							null,
							"Could not transcode Video '" //$NON-NLS-1$
									+ inputFile.getAbsolutePath()
									+ "' to file '" //$NON-NLS-1$
									+ outputFile.getAbsolutePath()
									+ "'.\n\nError:\n" + errorMsg, //$NON-NLS-1$
							MessageDialog.ERROR,
							new String[] { Messages.Exporter_MsgBox_ExportFailed_OkButton },
							0);
					errDialog.open();
					monitor.done();
				}
			});
		}
	}

	private void transcodeVideoAnnotations(Set<FileCopyInfo> videoAnnotations,
			IProgressMonitor monitor, IDManager idManager)
			throws ExportException {

		int numTranscodeSteps = videoAnnotations.size();
		int totalSteps = numTranscodeSteps;
		monitor.beginTask(Messages.ExportProject_ExportInProgress, totalSteps);
		monitor.subTask(Messages.ExportProject_Step1Preprocessing);

		File dirVids = createSubDir(EXPORT_SUBDIR_VIDEOS);

		String targetDir = dirVids.getAbsolutePath();

		int step = 0;
		for (FileCopyInfo copyInfo : videoAnnotations) {
			step++;
			if (monitor.isCanceled()) {
				return;
			}
			try {
				String outputFilename = targetDir + File.separator
						+ copyInfo.targetFilename;

				File outputFile = new File(outputFilename);
				File inputFile = new File(copyInfo.sourcePath);
				String title = copyInfo.sourceObject.getTitle();
				long startTime = -1;
				long duration = -1;
				Object startTimeObject = copyInfo.parameters
						.getValue(Exporter.EXPORT_KEY_STARTTIME);
				Object durTimeObject = copyInfo.parameters
						.getValue(Exporter.EXPORT_KEY_ENDTIME);
				if (startTimeObject instanceof Long
						&& durTimeObject instanceof Long) {
					startTime = (Long) startTimeObject;
					duration = (Long) durTimeObject;
				}
				outputFile.delete();
				// Ausgabe-Datei vorher loeschen
				// Video umwandeln.
				// Video umwandeln.
				String taskText = Messages.Exporter_Step_TranscodeVideoAnnotations;
				taskText = taskText.replace("%1", "" + step); //$NON-NLS-1$ //$NON-NLS-2$
				taskText = taskText.replace("%2", "" + totalSteps); //$NON-NLS-1$ //$NON-NLS-2$
				taskText = taskText.replace("%3", "" + title); //$NON-NLS-1$ //$NON-NLS-2$						
				transcodeVideo(inputFile, outputFile, taskText, startTime,
						duration, monitor, step, numTranscodeSteps);

			} catch (Exception e) {
				monitor.done();
				throw new ExportException(e);
			}
		}
		monitor.worked(1);
	}

	/**
	 * Filtert alle Videos aus einer Menge von FileCopyInfos heraus.
	 * 
	 * @param filesToCopy
	 * @return
	 */
	private Set<FileCopyInfo> filterVideos(Set<FileCopyInfo> filesToCopy) {
		Set<FileCopyInfo> result = new TreeSet<FileCopyInfo>();
		for (FileCopyInfo object : filesToCopy) {
			if (object.sourceObject instanceof Video) {
				result.add(object);
			}
		}
		return result;

	}

	/**
	 * Filtert alle Audios aus einer Menge von FileBasedObjects heraus.
	 * 
	 * @param filesToCopy
	 * @return
	 */
	private Set<FileCopyInfo> filterAudios(Set<FileCopyInfo> filesToCopy) {
		Set<FileCopyInfo> result = new TreeSet<FileCopyInfo>();
		for (FileCopyInfo object : filesToCopy) {
			if (object.sourceObject instanceof Audio) {
				result.add(object);
			}
		}
		return result;

	}

	/**
	 * Kopiert eine Datei
	 * 
	 * @param in
	 *            Eingabe-Datei
	 * @param out
	 *            Ausgabe-Datei
	 * @throws IOException
	 */
	private void copyFile(File in, File out) throws IOException {
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		logger.info("Copying file: " + in.getAbsolutePath() + " ==> " //$NON-NLS-1$ //$NON-NLS-2$
				+ out.getAbsolutePath());

		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			throw e;
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

	/**
	 * Kopiert alle in filesToCopy befindlichen Dateien in die entsprechenden
	 * Ausgabe-Ordner.
	 * 
	 * @param filesToCopy
	 * @param idManager
	 * @param monitor
	 * @throws ExportException
	 */
	private void copyFiles(Set<FileCopyInfo> filesToCopy, IDManager idManager,
			IProgressMonitor monitor) throws ExportException {

		File dirPics = createSubDir(EXPORT_SUBDIR_PICTURES);
		File dirRich = createSubDir(EXPORT_SUBDIR_RICHTEXTS);
		File dirAudios = createSubDir(EXPORT_SUBDIR_AUDIOS);

		for (FileCopyInfo copyInfo : filesToCopy) {
			IFileBasedObject fileBasedObject = copyInfo.sourceObject;
			String targetDir = null;
			if (fileBasedObject instanceof Video) {
				// Videos werden hier nicht kopiert, sondern spaeter extra
				// mit FFMPEG transkodiert.
				targetDir = null;
			} else if (fileBasedObject instanceof RichText) {
				targetDir = dirRich.getAbsolutePath();
			} else if (fileBasedObject instanceof Picture) {
				targetDir = dirPics.getAbsolutePath();
			} else if (fileBasedObject instanceof Audio) {
				targetDir = dirAudios.getAbsolutePath();
			} else {
				throw new ExportException(
						Messages.Exporter_Error_CantCreateDir_2
								+ fileBasedObject.getClass().getSimpleName()
								+ Messages.Exporter_Error_CantCreateDir_3);
			}

			try {
				String outputFilename = targetDir + File.separator
						+ copyInfo.targetFilename;
				File outputFile = new File(outputFilename);
				File inputFile = new File(copyInfo.sourcePath);
				try {
					if (fileBasedObject instanceof Video
							|| fileBasedObject instanceof Audio) {
						// Videos nicht kopieren, sondern die werden spaeter
						// mit FFMPEG transkodiert.
					} else if (fileBasedObject instanceof Picture) {
						// Bilder nicht einfach kopieren, sondern
						// eventuell vorhandene Zeichnungen im Bild hinein
						// rendern
						logger.info("Rendering image file: " //$NON-NLS-1$
								+ inputFile.getAbsolutePath() + " ==> " //$NON-NLS-1$
								+ outputFile.getAbsolutePath());

						renderPicture((Picture) fileBasedObject, inputFile,
								outputFile);

					} else {
						// Andere Annotationsdateien einfach kopieren
						copyFile(inputFile, outputFile);
					}

				} catch (IOException e) {
					monitor.done();
					throw new ExportException("Could not write to file '" //$NON-NLS-1$
							+ outputFile.getAbsolutePath() + "':" //$NON-NLS-1$
							+ e.getMessage());
				}
			} catch (ExportException e) {
				monitor.done();
				throw e;
			}
		}

	}

	private void renderPicture(Picture fileBasedObject, File inputFile,
			File outputFile) throws ExportException {
		ImageLoader loader = new ImageLoader();
		ImageData[] imgDataArray;
		try {
			imgDataArray = loader.load(fileBasedObject.getFile()
					.getAbsolutePath());
			if (imgDataArray.length == 0) {
				throw new ExportException("Image data seems empty"); //$NON-NLS-1$
			}
		} catch (Exception e) {
			throw new ExportException("Could not read image '" //$NON-NLS-1$
					+ fileBasedObject.getFile() + "': " + e.getMessage()); //$NON-NLS-1$
		}
		int imageType = imgDataArray[0].type;
		for (int i = 0; i < imgDataArray.length; i++) {
			Image img = new Image(Display.getDefault(), imgDataArray[i]);
			GC gc = new GC(img);
			gc.setAlpha(255);
			for (ImageObject o : ((Picture) fileBasedObject).getObjects()) {
				org.iviPro.editors.imageeditor.ImageEditWidget.drawObject(gc,
						o, img);
			}
			imgDataArray[i] = img.getImageData();

			// create thumbnail
			int imgWidth = img.getImageData().width;
			int imgHeight = img.getImageData().height;

			if (imgWidth > imgHeight) {
				imgWidth = imgHeight;
			} else {
				imgHeight = imgWidth;
			}
			Image image = new Image(Display.getCurrent(), imgWidth, imgHeight);
			gc.copyArea(image, 0, 0);
			ImageLoader imageLoader = new ImageLoader();
			imageLoader.data = new ImageData[] { image.getImageData().scaledTo(
					150, 150) };
			String path = outputFile.getAbsolutePath();
			int dotIndex = path.lastIndexOf(".");
			String newPath = path.substring(0, dotIndex) + "_thumb"
					+ path.substring(dotIndex);
			imageLoader.save(newPath, imageType);
			gc.dispose();
		}
		loader.save(outputFile.getAbsolutePath(), imageType);

	}

	/**
	 * Exportiert das angegebene Projekt in das SIVA-Player XML-Format und gibt
	 * eine Menge der Szenen-Knoten zurueck, die dabei verwendet wurden
	 * (ueblicherweise sind das die Szenen-Knoten, die im Szenengraph
	 * referenziert worden sind).
	 * 
	 * @param project
	 *            Das zu exportierende Projekt
	 * @param exportFile
	 *            Die Datei in die das XML-Dokument exportiert werden soll.
	 * @param idManager
	 *            Der ID-Manager der zur Generierung der IDs im XML-Dokument
	 *            verwendet werden soll.
	 * @return Menge der Szenen-Knoten, die beim Export verwendet worden sind.
	 * @throws ExportException
	 */
	private Set<NodeScene> exportXML(Project project, File exportFile,
			IDManager idManager) throws ExportException {
		// Datei ins XML-File exportieren
		ExporterSIVAPlayerXML exporter = new ExporterSIVAPlayerXML(idManager);
		Collection<NodeScene> exportedSceneNodes;
		exportedSceneNodes = exporter.export(exportFile);
		// Menge der Szenen-Knoten bestimmen (ohne Duplikate)
		HashSet<NodeScene> uniqueSceneNodes = new HashSet<NodeScene>();
		for (NodeScene nodeScene : exportedSceneNodes) {
			if (!uniqueSceneNodes.contains(nodeScene)) {
				uniqueSceneNodes.add(nodeScene);
			}
		}
		return uniqueSceneNodes;
	}

	private Set<NodeScene> exportSMIL(Project project, File exportFile,
			IDManager idManager) throws ExportException {
		ExporterSIVAPlayerSMIL exporter = new ExporterSIVAPlayerSMIL(idManager);
		Collection<NodeScene> exportedSceneNodes;
		exportedSceneNodes = exporter.export(exportFile);

		HashSet<NodeScene> uniqueSceneNodes = new HashSet<NodeScene>();
		for (NodeScene nodeScene : exportedSceneNodes) {
			if (!uniqueSceneNodes.contains(nodeScene)) {
				uniqueSceneNodes.add(nodeScene);
			}
		}
		return uniqueSceneNodes;
	}

	/**
	 * Gibt die Menge der eindeutigen Szenen in einer Menge von Szenen-Knoten
	 * zurueck.
	 * 
	 * @param exportedSceneNodes
	 *            Menge von Szenen-Knoten
	 * @return
	 */
	private Set<Scene> getUniqueScenes(Set<NodeScene> exportedSceneNodes) {
		HashSet<Scene> uniqueScenes = new HashSet<Scene>();
		for (NodeScene nodeScene : exportedSceneNodes) {
			Scene scene = nodeScene.getScene();
			if (!uniqueScenes.contains(scene)) {
				uniqueScenes.add(scene);
			}
		}
		return uniqueScenes;
	}

	/**
	 * Exportiert die angegebenen Video-Szenen mit FFMPEG
	 * 
	 * @param exportedScenes
	 *            Die Szenen die exportiert werden sollen.
	 * @param monitor
	 *            Progress-Monitor fuer Fortschrittsanzeige
	 * @param idManager
	 *            Der ID-Manager der zur Generierung der Dateinamen benutzt
	 *            werden soll.
	 */
	private void transcodeVideos(Set<Scene> exportedScenes,
			final IProgressMonitor monitor, IDManager idManager) {
		int numTranscodeSteps = getNumVideofilesToExportFromScenes(exportedScenes);
		int totalSteps = numTranscodeSteps;
		monitor.beginTask(Messages.ExportProject_ExportInProgress, totalSteps);
		monitor.subTask(Messages.ExportProject_Step1Preprocessing);
		int step = 1;
		for (Scene scene : exportedScenes) {
			final Video video = scene.getVideo();
			for (LocalizedFile file : video.getFiles()) {

				String outputFilename = exportDirectory + File.separator
						+ EXPORT_SUBDIR_VIDEOS + File.separator
						+ idManager.getFilename(scene, file.getLanguage());

				if (type == ExportType.HTML5) {
					outputFilename = exportDirectory + File.separator + "Files"
							+ File.separator + EXPORT_SUBDIR_VIDEOS
							+ File.separator
							+ idManager.getFilename(scene, file.getLanguage());
				} else {
					outputFilename = exportDirectory + File.separator
							+ EXPORT_SUBDIR_VIDEOS + File.separator
							+ idManager.getFilename(scene, file.getLanguage());
				}

				File outputFile = new File(outputFilename);
				File inputFile = file.getValue();
				// Ausgabe-Datei vorher loeschen
				outputFile.delete();
				// Video umwandeln.
				String taskText = Messages.ExportProject_Step2Transcoding;
				taskText = taskText.replace("%1", "" + step); //$NON-NLS-1$ //$NON-NLS-2$
				taskText = taskText.replace("%2", "" + totalSteps); //$NON-NLS-1$ //$NON-NLS-2$
				taskText = taskText.replace("%3", "" + scene.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$				
				transcodeVideo(inputFile, outputFile, taskText,
						scene.getStart(), scene.getEnd(), monitor, step,
						numTranscodeSteps);
				if (monitor.isCanceled()) {
					return;
				}
				step++;
			}
		}

		monitor.worked(1);
	}

	/**
	 * Transkodiert ein Video
	 * 
	 * @param inputFile
	 *            Die Quell-Datei
	 * @param outputFile
	 *            Die Ziel-Datei
	 * @param scene
	 *            Die zugehoerige Szene
	 * @param monitor
	 *            Der Progress-Monitor zur Fortschrittsanzeige
	 * @param step
	 *            Der aktuelle Schritt fuer den Progress-Monitor
	 * @param totalSteps
	 *            Die Anzahl der gesamten Schritte
	 */
	private void transcodeVideo(final File inputFile, final File outputFile,
			String taskText, long startTime, long endTime,
			final IProgressMonitor monitor, int step, int totalSteps) {

		// Step 2: Transcoding scene %1 of %2 into FLV format: %3
		monitor.subTask(taskText);
		// monitor.worked(1);

		try {
			if (type == ExportType.HTML5) {
				SubProgressMonitor submonitor = new SubProgressMonitor(monitor,
						1);
				submonitor.beginTask("Converting files", 3);

				for (ExportType currentType : html5Types) {
					submonitor.subTask(taskText + " in "
							+ currentType.getDisplayString());
					File currFile = new File(outputFile.getPath()
							+ currentType.getVideoExtension());
					FfmpegTranscode.transcodeVideo(inputFile, currFile,
							currentType, startTime, endTime);
					submonitor.worked(1);
				}
				submonitor.done();
				outputFile.delete();
			} else {
				FfmpegTranscode.transcodeVideo(inputFile, outputFile, type,
						startTime, endTime);
			}
			monitor.worked(1);
		} catch (ExportException e) {
			final String errorMsg = e.getMessage();
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					final Shell shell = new Shell(Display.getDefault());
					final MessageDialog errDialog = new MessageDialog(
							shell,
							Messages.Exporter_MsgBox_ExportFailed_Title,
							null,
							Messages.Exporter_MsgBox_TranscodeError_Text1
									+ inputFile.getAbsolutePath()
									+ Messages.Exporter_MsgBox_TranscodeError_Text2
									+ outputFile.getAbsolutePath()
									+ Messages.Exporter_MsgBox_TranscodeError_Text3
									+ errorMsg,
							MessageDialog.ERROR,
							new String[] { Messages.Exporter_MsgBox_ExportFailed_OkButton },
							0);
					errDialog.open();
					monitor.done();

				}

			});
		}

	}

	/**
	 * Gibt die Anzahl der Video-Dateien an, die transkodiert werden muessen.
	 * 
	 * @param exportedScenes
	 * @return
	 */
	private int getNumVideofilesToExportFromScenes(Set<Scene> exportedScenes) {
		int num = 0;
		for (Scene scene : exportedScenes) {
			num += scene.getVideo().getFiles().size();
		}
		return num;
	}

}
