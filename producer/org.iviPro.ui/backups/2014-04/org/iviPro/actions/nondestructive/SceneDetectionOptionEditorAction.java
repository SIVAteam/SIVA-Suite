package org.iviPro.actions.nondestructive;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Video;
import org.iviPro.theme.Icons;
import org.iviPro.views.mediarepository.MediaTreeLeaf;
import org.iviPro.editors.scenedetection.SceneDetectionOptionEditor;

public class SceneDetectionOptionEditorAction extends Action implements
		ISelectionListener, IWorkbenchAction {

	private static Logger logger = Logger.getLogger(OpenShotEditorAction.class);

	private final IWorkbenchWindow window;

	private Video selectedVideo;

	public SceneDetectionOptionEditorAction(IWorkbenchWindow window) {
		this.window = window;
		setText("New Scene Detection");
		setToolTipText(Messages.ViewImage_ViewImageToolTip);
		setImageDescriptor(Icons.ACTION_EDITOR_IMAGE.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_EDITOR_IMAGE
				.getDisabledImageDescriptor());
		window.getSelectionService().addSelectionListener(this);
	}

	@Override
	public void run() {
		logger.debug("Open Scene Detection Menu"); //$NON-NLS-1$
		SceneDetectionOptionEditor ed = new SceneDetectionOptionEditor(window, selectedVideo);
		ed.show();
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
