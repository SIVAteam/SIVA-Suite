package org.iviPro.actions.undoable;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.resources.Video;
import org.iviPro.operations.CompoundOperation;
import org.iviPro.operations.video.SceneCreateOperation;
import org.iviPro.theme.Icons;
import org.iviPro.utils.SivaTime;
import org.iviPro.views.mediarepository.MediaTreeLeaf;

/**
 * Diese Action stellt die Schnittstelle zur Scenenerkennung dar.
 * 
 * @author Florian Stegmaier
 */
public class SceneDetectionAction extends Action implements ISelectionListener,
		IWorkbenchAction {
	private static Logger logger = Logger.getLogger(SceneDetectionAction.class);
	public final static String ID = SceneDetectionAction.class.getName(); //$NON-NLS-1$

	private final IWorkbenchWindow window;

	private Video selectedVideo;

	/**
	 * Konstruktor...
	 * 
	 * @param window
	 */
	public SceneDetectionAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setEnabled(false);
		setText(Messages.OpenSceneDetectionEditorAction_Text);
		setToolTipText(Messages.OpenSceneDetectionEditorAction_Tooltip);
		setImageDescriptor(Icons.ACTION_EDITOR_SCENEDETECTION
				.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_EDITOR_SCENEDETECTION
				.getDisabledImageDescriptor());
		window.getSelectionService().addSelectionListener(this);
	}

	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

	/**
	 * Die eigentliche Ausführungslogik der Action.
	 */
	@Override
	public void run() {
		/*
		logger.debug("Running scene detection..."); //$NON-NLS-1$

		// Progress dialog
		final ProgressMonitorDialog pd = new ProgressMonitorDialog(window
				.getShell());
		final SceneDetectionProgress sdp = new SceneDetectionProgress(
				selectedVideo);
		try {
			pd.run(true, true, sdp);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// holen der Ergebnisse und weiteres Sperren der Applikation
		BusyIndicator.showWhile(window.getShell().getDisplay(), new Runnable() {
			public void run() {
				SceneDetection sd = SceneDetection.getInstance();
				List<Long> sceneBreakPoints = sd.getSceneBreakPoints();
				if (pd.getProgressMonitor().isCanceled()) {
					return;
				}
				if (sceneBreakPoints == null || sceneBreakPoints.isEmpty()) {
					MessageDialog
							.openInformation(window.getShell(), Messages.SceneDetectionAction_InfoNoScenesFound_Title,
									Messages.SceneDetectionAction_InfoNoScenesFound_Msg);
					return;
				}

				CompoundOperation<SceneCreateOperation> operation = buildScenesFromSceneBreaks(
						sceneBreakPoints, selectedVideo);
				int numScenes = operation.getChildOperations().size();
				try {
					OperationHistory.execute(operation);
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sd.resetSceneDetection();
				String msgSingular = numScenes
						+ Messages.OpenSceneDetectionEditorAction_MsgFinishSingular
						+ selectedVideo.getTitle()
						+ Messages.OpenSceneDetectionEditorAction_MsgFinishSingular2;
				String msgPlural = numScenes
						+ Messages.OpenSceneDetectionEditorAction_MsgFinishPlural
						+ selectedVideo.getTitle()
						+ Messages.OpenSceneDetectionEditorAction_MsgFinishPlural2;
				String msg = (numScenes == 1 ? msgSingular : msgPlural);

				MessageDialog.openInformation(window.getShell(),
						Messages.OpenSceneDetectionEditorAction_MsgFinishTitle,
						msg);
			}
		});
		*/
	}

	/**
	 * Erstellt eine Liste von SceneObjects aus einer gegebenen Liste von
	 * Szenen-Breakpoints.
	 * 
	 * @param sceneBreakPoints
	 *            Die Szenen-Breakpoints
	 * @param video
	 *            Das MediaObject fuer das die Szenen erstellt werden sollen.
	 * @return List mit SceneObjects die den Breakpoints entsprechen.
	 */
	private CompoundOperation<SceneCreateOperation> buildScenesFromSceneBreaks(
			List<Long> sceneBreakPoints, final Video video) {
		CompoundOperation<SceneCreateOperation> createOperation = new CompoundOperation<SceneCreateOperation>(
				Messages.SceneDetectionAction_UndoLabel);
		long oldFirst = sceneBreakPoints.get(0);
		long oldLast = sceneBreakPoints.get(sceneBreakPoints.size() - 1);
		long duration = video.getDuration();

		// Fuege neue Szene vom Anfang bis zum ersten Schnittpunkt ein, falls
		// der erste Schnittpunkt nicht ohnehin der Anfang des Videos ist.
		if (oldFirst != 0) {
			logger.info("First detected scene does not start at beginning" //$NON-NLS-1$
					+ " -> Creating new first scene from 0.0 - " + oldFirst //$NON-NLS-1$
					/ 1000000000.0 + "s"); //$NON-NLS-1$
			sceneBreakPoints.add(0, 0L);
		}
		// Fuege neue Szene vom letzten Schnittpunkt bis zum Ende des Videos
		// ein, falls der letzte Schnittpunkt nicht ohnehin das Video-Ende ist.
		if (oldLast != duration) {
			logger.info("Last detected scene does not last till the video end" //$NON-NLS-1$
					+ " -> Creating new last scene from " + oldLast //$NON-NLS-1$
					/ 1000000000.0 + " - " + duration / 1000000000.0 + "s"); //$NON-NLS-1$ //$NON-NLS-2$
			sceneBreakPoints.add(duration);
		}
		// Generiere fuer die Zeit zwischen je zwei Schnittenpunkten eine Szene
		for (int i = 0 + 0; i < sceneBreakPoints.size() - 1; i++) {
			String title = video.getTitle()
					+ Messages.OpenSceneDetectionEditorAction_SceneTitlePrefix
					+ i;
			SivaTime start = new SivaTime(sceneBreakPoints.get(i));
			SivaTime end = new SivaTime(sceneBreakPoints.get(i + 1));
			SceneCreateOperation subOperation = new SceneCreateOperation(title,
					video, start, end, ""); //$NON-NLS-1$
			createOperation.addOperation(subOperation);
		}
		return createOperation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.
	 * IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {

		IStructuredSelection incoming = (IStructuredSelection) selection;
		// Das eintreffende Objekt muss vom Typ MediaLeaf sei und zugleich
		// als Obergruppe "VIDEO" haben...
		if (incoming.getFirstElement() instanceof MediaTreeLeaf) {
			IAbstractBean mo = ((MediaTreeLeaf) incoming.getFirstElement())
					.getMediaObject();
			if (mo instanceof Video) {
				selectedVideo = (Video) mo;
				setEnabled(true);
			} else {
				setEnabled(false);
			}
		} else {
			setEnabled(false);
		}
	}
}
