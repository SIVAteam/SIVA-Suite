package org.iviPro.actions.undoable;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.actions.nondestructive.OpenSceneEditorAction;
import org.iviPro.editors.shotoverview.ShotOverviewEditor;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Video;
import org.iviPro.scenedetection.sd_main.Scene;
import org.iviPro.scenedetection.sd_main.SceneDetection;
import org.iviPro.scenedetection.sd_main.Shot;
import org.iviPro.theme.Icons;
import org.iviPro.utils.PathHelper;
import org.iviPro.views.mediarepository.MediaTreeLeaf;

public class NewSceneDetectionAction extends Action implements
		ISelectionListener, IWorkbenchAction {

	private static Logger logger = Logger.getLogger(ShotDetectionAction.class);

	private final IWorkbenchWindow window;

	private Video selectedVideo;

	private List<Shot> shotList;

	private boolean parallel;

	private boolean mpeg7;

	private ShotOverviewEditor editorToClose;

	public NewSceneDetectionAction(ShotOverviewEditor editorToClose,
			IWorkbenchWindow window, Video video, List<Shot> shotList,
			boolean parallel, boolean mpeg7) {
		PropertyConfigurator.configure(PathHelper.getPathToLoggerIni());
		this.shotList = shotList;
		this.selectedVideo = video;
		this.window = window;
		this.parallel = parallel;
		this.mpeg7 = mpeg7;
		this.editorToClose = editorToClose;

		// Removes memory leaks
		this.shotList = new LinkedList<Shot>();
		for (int i = 0; i < shotList.size(); i++) {
			this.shotList.add(shotList.get(i).clone());
		}

		setText("New Scene Detection");
		setImageDescriptor(Icons.ACTION_EDITOR_SCENEDETECTION
				.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_EDITOR_SCENEDETECTION
				.getDisabledImageDescriptor());
		window.getSelectionService().addSelectionListener(this);
	}

	@Override
	public void run() {
		logger.debug("Running scene detection...");

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		page.activate(editorToClose);
		page.closeEditor(editorToClose, false);

		final ProgressMonitorDialog pd = new ProgressMonitorDialog(
				window.getShell());
		SceneDetection sdp = new SceneDetection(selectedVideo.getFile()
				.getAbsolutePath(), parallel, mpeg7, shotList,
				selectedVideo.getDuration(), selectedVideo.getFrameRate());
		try {
			pd.run(true, true, sdp);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		List<Scene> scenes = sdp.getScenes();

		List<Scene> currentScenes = new LinkedList<Scene>();
		for (int i = 0; i < scenes.size(); i++) {
			currentScenes.add(scenes.get(i).clone());
		}
		scenes = null;
		Action action = new OpenSceneEditorAction(window, currentScenes,
				selectedVideo, mpeg7, sdp.getExporter(), sdp.getMediaLocator()
						.toExternalForm());
		sdp = null;
		action.run();
	}

	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IStructuredSelection incoming = (IStructuredSelection) selection;
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
