package org.iviPro.actions.undoable;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.resources.Video;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.video.SceneCreateOperation;
import org.iviPro.theme.Icons;
import org.iviPro.utils.SivaTime;
import org.iviPro.views.mediarepository.MediaTreeLeaf;

/**
 * Action zum Erstellen einer Szene die sich ueber das komplette Video
 * erstreckt.
 * 
 * @author Christian Dellwo
 */
public class SceneFromVideoAction extends Action implements ISelectionListener,
		IWorkbenchAction {
	private static Logger logger = Logger.getLogger(SceneFromVideoAction.class);
	public final static String ID = SceneFromVideoAction.class.getName(); //$NON-NLS-1$

	private final IWorkbenchWindow window;

	private Video selectedVideo;

	/**
	 * Konstruktor...
	 * 
	 * @param window
	 */
	public SceneFromVideoAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setEnabled(false);
		setText(Messages.SceneFromVideoAction_Label);
		setToolTipText(Messages.SceneFromVideoAction_ToolTip);
		setImageDescriptor(Icons.ACTION_EDITOR_SCENE.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_EDITOR_SCENE
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
		if (selectedVideo == null) {
			logger.error("Action was executed, but no video selected."); //$NON-NLS-1$
			return;
		}
		SceneCreateOperation op = new SceneCreateOperation("", selectedVideo, new SivaTime(0), new SivaTime( //$NON-NLS-1$
				selectedVideo.getDuration()), ""); //$NON-NLS-1$
		try {
			OperationHistory.execute(op);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
