package org.iviPro.actions.undoable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.model.BeanList;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IMediaObject;
import org.iviPro.model.Project;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.PdfDocument;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.RichText;
import org.iviPro.model.resources.Video;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.media.MediaAddOperation;
import org.iviPro.theme.Icons;
import org.iviPro.views.mediarepository.MediaRepository;

/**
 * Action zum Laden von Medien-Dateien in das Projekt.
 * 
 */
public class MediaLoadAction extends Action implements IWorkbenchAction,
		ApplicationListener {

	public final static String ID = "org.iviPro.actions.loadMediaFiles"; //$NON-NLS-1$
	public final static String ACTIONDEFINITIONID = "org.iviPro.ui.MediaLoadAction"; //$NON-NLS-1$
	private static Logger logger = Logger.getLogger(MediaLoadAction.class);

	/** Liste der unterstuetzten Video-Formate */
	private static final List<String> SUPPORTED_VIDEO_FILES = new ArrayList<String>(
			Arrays.asList(new String[] { "mpg", "mpeg", "avi", "mp4", "wmv", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					"mov" })); //$NON-NLS-1$
	/** Liste der unterstuetzten Audio-Formate */
	private static final List<String> SUPPORTED_AUDIO_FILES = new ArrayList<String>(
			Arrays.asList(new String[] { "wav", "mp3" })); //$NON-NLS-1$ //$NON-NLS-2$
	/** Liste der unterstuetzten Bild-Formate */
	private static final List<String> SUPPORTED_PICTURE_FILES = new ArrayList<String>(
			Arrays.asList(new String[] { "jpg", "jpeg", "png", "gif" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	/** Liste der unterstuetzten Richtext-Formate */
	private static final List<String> SUPPORTED_TEXT_FILES = new ArrayList<String>(
			Arrays.asList(new String[] { "html", "htm", "txt" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$	
	/** List of supported PDF-formats. */
	private static final List<String> SUPPORTED_PDF_FILES = new ArrayList<String>(
			Arrays.asList(new String[] { "pdf" })); //$NON-NLS-1$
	
	private IWorkbenchWindow window;

	private String[] filenames;

	/**
	 * Erstellt eine neue MediaLoadAction, die beim Ausfuehren eine gegebene
	 * Mengen an Medien-Dateien dem aktuellen Projekt hinzufuegt.
	 * 
	 * @param window
	 *            Das Workbench-Fenster
	 * @param filenames
	 *            Liste mit den Dateinamen der hinzufuegenden Medien-Dateien.
	 */
	public MediaLoadAction(IWorkbenchWindow window, String[] filenames) {
		this(window);
		this.filenames = filenames;
	}

	/**
	 * Erstellt eine neue MediaLoadAction, die dem Benutzer einen Dialog zur
	 * Auswahl der zu ladenden Medien-Dateien anzeigt, und diese Medien-Datien
	 * dann dem aktuellen Projekt hinzufuegt.
	 * 
	 * @param window
	 *            Das Workbench-Fenster
	 */
	public MediaLoadAction(IWorkbenchWindow window) {
		this.window = window;
		setEnabled(false);
		setId(ID);
		setText(Messages.MediaLoadAction_LoadMedia);
		setToolTipText(Messages.MediaLoadAction_LoadMediaToolTip);
		setImageDescriptor(Icons.ACTION_MEDIA_LOAD.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_MEDIA_LOAD
				.getDisabledImageDescriptor());
		setActionDefinitionId(ACTIONDEFINITIONID);
		Application.getDefault().addApplicationListener(this);
		this.filenames = null;
		setAccelerator(SWT.CTRL | 'L');
	}

	/**
	 * Die eiegentliche Ausführungslogik der Action.
	 */
	@Override
	public void run() {

		// Dialog nur anzeigen wenn keine Dateinamens-Liste vorliegt
		if (filenames == null) {
			// Dateidialog oeffnen
			Shell shell = new Shell(Display.getDefault());
			FileDialog dialog = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
			createFilter(dialog);
			dialog.open();
			// Gewaehlten Pfad und Dateiliste holen
			String path = dialog.getFilterPath() + "\\"; //$NON-NLS-1$
			filenames = dialog.getFileNames();
			// Die Dateinamens-Liste in absolute Pfade umbauen
			for (int i = 0; i < filenames.length; i++) {
				filenames[i] = path + filenames[i];
			}
		}

		// Medien-Objekte hinzufuegen
		boolean added = false;
		for (String filename : filenames) {
			// Der Benutzer hat eine Datei gewaehlt
			addMediaObject(filename);
			added = true;
		}

		// Zeige das Media-Repository an, wenn ein Medium hinzugefuegt wurde.
		if (added) {
			IViewPart mediaRepository = Application.getDefault().getView(
					MediaRepository.ID);
			try {
				window.getActivePage().activate(mediaRepository);
			} catch (Exception e) {
				logger.warn("Media-Repository could not" //$NON-NLS-1$ 
						+ " be brought to foreground."); //$NON-NLS-1$
			}
		}
		// Liste der hinzuzufügenden Dateien wieder auf null setzen
		// da sonst jedes mal versucht wird die alte Liste wieder hinzuzufügen
		this.filenames = null;
	}

	private void createFilter(FileDialog dialog) {
		// Anzahl der gesamten Filter-Eintraege im FileDialog:
		// Fuenf Format-Gruppen (Alle, Video, Audio, Picture, Other) + die
		// einzelnen Formate
		int numFormats = 5 + SUPPORTED_AUDIO_FILES.size()
				+ SUPPORTED_VIDEO_FILES.size() + SUPPORTED_PICTURE_FILES.size()
				+ SUPPORTED_TEXT_FILES.size() + SUPPORTED_PDF_FILES.size();

		// Fuege alle Datei-Endungen der jeweiligen MediaTypes in ein
		// Filter-Array ein:
		String[] filterExtensions = new String[numFormats];
		String allExtensions = ""; //$NON-NLS-1$

		int arrayPos = 5;
		// Add all Video-Types
		String extsVideo = processMediaType(SUPPORTED_VIDEO_FILES,
				filterExtensions, arrayPos);
		filterExtensions[1] = extsVideo;
		allExtensions += extsVideo + "; "; //$NON-NLS-1$
		arrayPos += SUPPORTED_VIDEO_FILES.size();
		logger.debug("Supported video files: " + extsVideo); //$NON-NLS-1$

		// Add all Audio-Types
		String extsAudio = processMediaType(SUPPORTED_AUDIO_FILES,
				filterExtensions, arrayPos);
		filterExtensions[2] = extsAudio;
		allExtensions += extsAudio + "; "; //$NON-NLS-1$
		arrayPos += SUPPORTED_AUDIO_FILES.size();
		logger.debug("Supported audio files: " + extsAudio); //$NON-NLS-1$

		// Add all Image-Types
		String extsPic = processMediaType(SUPPORTED_PICTURE_FILES,
				filterExtensions, arrayPos);
		filterExtensions[3] = extsPic;
		allExtensions += extsPic + "; "; //$NON-NLS-1$
		arrayPos += SUPPORTED_PICTURE_FILES.size();
		logger.debug("Supported picture files: " + extsPic); //$NON-NLS-1$

		// Add all other Types
		String extsText = processMediaType(SUPPORTED_TEXT_FILES,
				filterExtensions, arrayPos);
		filterExtensions[4] = extsText;
		allExtensions += extsText + "; ";
		arrayPos += SUPPORTED_TEXT_FILES.size();
		logger.debug("Supported text files: " + extsText); //$NON-NLS-1$
		
		// Add all other Types
		String extsPdf = processMediaType(SUPPORTED_PDF_FILES,
				filterExtensions, arrayPos);
		filterExtensions[5] = extsPdf;
		allExtensions += extsPdf;
		arrayPos += SUPPORTED_PDF_FILES.size();
		logger.debug("Supported pdf Files: " + extsPdf); //$NON-NLS-1$

		

		filterExtensions[0] = allExtensions;

		// Setze fuer dier MediaType-Gruppen noch aussagekraeftige Namen
		String[] filterNames = filterExtensions.clone();
		filterNames[0] = Messages.MediaLoadAction_AllMediaTypes;
		filterNames[1] = Messages.MediaLoadAction_AllVideoTypes;
		filterNames[2] = Messages.MediaLoadAction_AllAudioTypes;
		filterNames[3] = Messages.MediaLoadAction_AllPictureTypes;
		filterNames[4] = Messages.MediaLoadAction_AllTextTypes;
		filterNames[5] = Messages.MediaLoadAction_AllPdfTypes;
		
		// Aktiviere den Datei-Filter im Dialog
		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);
	}

	/**
	 * Baut die Datei-Endungen eines Media-Types in den Datei-Filter mit ein.
	 * Dazu wird jede Datei-Endung des Media-Types in das filterArray
	 * geschrieben (beginnend bei arrayPos) und ein String mit einem
	 * Filter-Ausdruck fuer alle Formate dieses Media-Types erstellt in der Form
	 * <tt>*.ext1; *.ext2; *.ext3;... *.extN;</tt> zurueck gegeben.
	 * 
	 * @param mediaTypeFiles
	 *            Liste der Datei-Endungen des Media-Types (siehe
	 *            MediaTypes.SUPPORTED_XYZ_FILES).
	 * @param filterArray
	 *            String-Array das alle Extensions fuer den File-Dialog
	 *            beinhaltet. Hier werden die einzelnen Endungen der Formate
	 *            dieses Media-Types eingefuegt.
	 * @param arrayPos
	 *            Position, ab der die Endungen in das filterArray-Array
	 *            geschrieben werden.
	 * @return
	 */
	private String processMediaType(List<String> mediaTypeFiles,
			String[] filterArray, int arrayPos) {
		Iterator<String> types = mediaTypeFiles.iterator();
		StringBuilder extensions = new StringBuilder(mediaTypeFiles.size() * 7);
		while (types.hasNext()) {
			String currentExtension = "*." + types.next(); //$NON-NLS-1$
			extensions.append(currentExtension);
			if (types.hasNext()) {
				extensions.append("; "); //$NON-NLS-1$
			}
			filterArray[arrayPos] = currentExtension;
			arrayPos++;
		}
		return extensions.toString();
	}

	/**
	 * 
	 * fügt ein Medienobjekt anhand dem Pfad in ein Projekt ein
	 * 
	 * @param path
	 */
	public void addMediaObject(String path) {

		File file = new File(path);

		// Dateien werden nur hinzugefügt falls ein Projekt geöffnet ist
		if (Application.getDefault().isProjectOpen()) {

			// Ist eine zugehörige Gruppe gefunden worden?
			IMediaObject mediaObj = createMediaObject(file);
			if (mediaObj != null) {
				BeanList<IAbstractBean> mediaObjects = Application
						.getCurrentProject().getMediaObjects();
				// Uses equals of IFileBasedObject
				if (!mediaObjects.contains(mediaObj)) {
					try {
						OperationHistory
								.execute(new MediaAddOperation(mediaObj));
					} catch (Exception e) {
						Shell errorShell = new Shell(Display.getDefault());
						MessageDialog
								.openInformation(
										errorShell,
										Messages.MediaLoadAction_ErrorTitle,
										Messages.MediaLoadAction_ErrorMsg_TheFollowingErrorOccured
												+ e.getMessage());
					}
				} else {
					Shell errorShell = new Shell(Display.getDefault());
					MessageDialog.openInformation(errorShell,
							Messages.MediaLoadAction_ErrorTitle,
							Messages.MediaLoadAction_ErrorMsg_DuplicateFile
									+ Messages.MediaLoadAction_1 + path);

				}
			} else {
				Shell errorShell = new Shell(Display.getDefault());
				MessageDialog.openInformation(errorShell,
						Messages.MediaLoadAction_ErrorTitle,
						Messages.MediaLoadAction_TypeMsg);
			}
		} else {
			Shell errorShell = new Shell(Display.getDefault());
			MessageDialog.openInformation(errorShell,
					Messages.MediaLoadAction_ErrorTitle,
					Messages.MediaLoadAction_NameMsg);
		}
	}

	@Override
	public void dispose() {
		Application.getDefault().removeApplicationListener(this);
	}

	@Override
	public void onProjectOpened(Project project) {
		setEnabled(true);
	}

	/**
	 * Determines the type of the file parameter with respect to the available
	 * <code>IResource</code> types and returns an appropriate 
	 * <code>IMediaObject</code> or <code>null</code> if an appropriate type
	 * could not be determined. 
	 * @param file file for which a media object is created
	 * @return appropriate <code>IMediaObject</code> for the given file
	 */
	private IMediaObject createMediaObject(File file) {

		String fileName = file.getName();
		String fileFormat = fileName
				.substring(fileName.lastIndexOf(".") + 1).toLowerCase(); //$NON-NLS-1$

		Project project = Application.getCurrentProject();
		if (SUPPORTED_VIDEO_FILES.contains(fileFormat)) {
			return new Video(file, project);
		} else if (SUPPORTED_AUDIO_FILES.contains(fileFormat)) {
			return new Audio(file, project);
		} else if (SUPPORTED_PICTURE_FILES.contains(fileFormat)
				&& Picture.isReadableImage(file)) {
			return new Picture(file, project);
		} else if (SUPPORTED_TEXT_FILES.contains(fileFormat)) {
			return new RichText(file, project);
		} else if (SUPPORTED_PDF_FILES.contains(fileFormat)) {
			return new PdfDocument(file, project);
		}

		return null;
	}

	@Override
	public void onProjectClosed(Project project) {
		setEnabled(false);
	}
}
