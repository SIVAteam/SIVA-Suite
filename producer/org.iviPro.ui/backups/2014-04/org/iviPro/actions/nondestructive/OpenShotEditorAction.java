package org.iviPro.actions.nondestructive;

import java.util.LinkedList;
import java.util.List;
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
import org.iviPro.editors.shotoverview.ShotOverviewEditor;
import org.iviPro.editors.shotoverview.ShotOverviewEditorInput;
import org.iviPro.model.Video;
import org.iviPro.scenedetection.sd_main.Shot;
import org.iviPro.theme.Icons;
import org.iviPro.utils.PathHelper;

public class OpenShotEditorAction extends Action implements ISelectionListener,
		IWorkbenchAction {

	private static Logger logger = Logger.getLogger(OpenShotEditorAction.class);

	private final IWorkbenchWindow window;

	private boolean parallel;

	private boolean mpeg7;

	private List<Shot> shotList;

	private Video selectedVideo;

	public OpenShotEditorAction(IWorkbenchWindow window, boolean parallel,
			boolean mpeg7, List<Shot> shotList, Video selectedVideo) {
		PropertyConfigurator.configure(PathHelper.getPathToLoggerIni());
		this.window = window;
		this.parallel = parallel;
		this.mpeg7 = mpeg7;
		this.shotList = shotList;
		this.selectedVideo = selectedVideo;
		setText("ShotEditor");
		
		// Removes memory leaks
		this.shotList = new LinkedList<Shot>();
		for (int i = 0; i < shotList.size(); i++) {
			this.shotList.add(shotList.get(i).clone());
		}
		
		setImageDescriptor(Icons.ACTION_EDITOR_IMAGE.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_EDITOR_IMAGE
				.getDisabledImageDescriptor());
		window.getSelectionService().addSelectionListener(this);
	}

	@Override
	public void run() {
		logger.debug("Running Shot Editor..."); //$NON-NLS-1$
		
		ShotOverviewEditorInput input = new ShotOverviewEditorInput(window,
				parallel, mpeg7, shotList, selectedVideo);
		try {
			IWorkbenchPage page = window.getActivePage();
			page.openEditor(input, ShotOverviewEditor.ID, true);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		shotList = null;
	}

	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
