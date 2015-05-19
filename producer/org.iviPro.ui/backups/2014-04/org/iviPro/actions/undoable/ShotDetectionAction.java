package org.iviPro.actions.undoable;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.actions.nondestructive.OpenShotEditorAction;
import org.iviPro.model.Video;
import org.iviPro.scenedetection.sd_main.Shot;
import org.iviPro.scenedetection.sd_main.ShotDetection;
import org.iviPro.theme.Icons;
import org.iviPro.utils.PathHelper;

public class ShotDetectionAction extends Action implements ISelectionListener,
		IWorkbenchAction {

	private static Logger logger = Logger.getLogger(ShotDetectionAction.class);

	private final IWorkbenchWindow window;

	private Video selectedVideo;

	private boolean parallel;

	private boolean gradual;

	private boolean mpeg7;

	public ShotDetectionAction(IWorkbenchWindow window, Video video) {
		PropertyConfigurator.configure(PathHelper.getPathToLoggerIni());
		this.selectedVideo = video;
		this.window = window;
		this.parallel = false;
		this.gradual = false;
		this.mpeg7 = false;
		// Shot detection belongs to Scene Detection
		setText("New Scene Detection");
		setImageDescriptor(Icons.ACTION_EDITOR_SCENEDETECTION
				.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_EDITOR_SCENEDETECTION
				.getDisabledImageDescriptor());
		window.getSelectionService().addSelectionListener(this);
	}

	@Override
	public void run() {
		logger.debug("Running shot detection...");

		final ProgressMonitorDialog pd = new ProgressMonitorDialog(
				window.getShell());
		ShotDetection sdp = new ShotDetection(selectedVideo.getFile()
				.getAbsolutePath(), parallel, gradual, mpeg7,
				selectedVideo.getDuration(), selectedVideo.getFrameRate());
		try {
			pd.run(true, true, sdp);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Removes memory leaks
		List<Shot> shots = new LinkedList<Shot>();
		List<Shot> result = sdp.getShots();
		for (int i = 0; i < result.size(); i++) {
			shots.add(result.get(i).clone());
		}
		result = null;
		sdp = null;

		Action action = new OpenShotEditorAction(window, parallel, mpeg7, shots,
				selectedVideo);
		shots = null;
		action.run();
	}

	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	}

	public void setParallel(boolean parallel) {
		this.parallel = parallel;
	}

	public void setGradual(boolean gradual) {
		this.gradual = gradual;
	}

	public void setMpeg7(boolean mpeg7) {
		this.mpeg7 = mpeg7;
	}
}
