package org.iviPro.actions.nondestructive;

import java.util.List;

import org.iviPro.editors.shotoverview.SceneOverviewEditor;
import org.iviPro.editors.shotoverview.SceneOverviewEditorInput;
import org.iviPro.scenedetection.sd_main.Scene;
import org.iviPro.scenedetection.sd_misc.Mpeg7Export;
import org.iviPro.theme.Icons;
import org.iviPro.utils.PathHelper;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.model.Video;

public class OpenSceneEditorAction extends Action implements
		ISelectionListener, IWorkbenchAction {

	private static Logger logger = Logger
			.getLogger(OpenSceneEditorAction.class);

	private IWorkbenchWindow window;

	private List<Scene> sceneList;

	private Video vid;

	private boolean mpeg7;

	private Mpeg7Export exporter;

	private String mediaPath;

	public OpenSceneEditorAction(IWorkbenchWindow window,
			List<Scene> sceneList, Video video, boolean mpeg7,
			Mpeg7Export exporter, String mediaPath) {
		PropertyConfigurator.configure(PathHelper.getPathToLoggerIni());
		this.window = window;
		this.vid = video;
		this.sceneList = sceneList;
		this.mpeg7 = mpeg7;
		this.exporter = exporter;
		this.mediaPath = mediaPath;
		setImageDescriptor(Icons.ACTION_EDITOR_IMAGE.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_EDITOR_IMAGE
				.getDisabledImageDescriptor());
		window.getSelectionService().addSelectionListener(this);
	}

	@Override
	public void run() {
		logger.debug("Running Scene Editor...");

		SceneOverviewEditorInput input = new SceneOverviewEditorInput(window,
				sceneList, vid, mpeg7, exporter, mediaPath);
		try {
			IWorkbenchPage page = window.getActivePage();
			page.openEditor(input, SceneOverviewEditor.ID, true);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

	@Override
	public void selectionChanged(IWorkbenchPart arg0, ISelection arg1) {
		// TODO Auto-generated method stub

	}

}
