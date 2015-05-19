package org.iviPro.actions.nondestructive;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.mediaaccess.player.MediaPlayerView;
import org.iviPro.mediaaccess.player.MediaPlayerWidgetInput;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.model.resources.Video;
import org.iviPro.theme.Icons;
import org.iviPro.views.mediarepository.MediaTreeLeaf;

/**
 * Diese Action ermöglicht es, ein Video in einem PopUp abzuspielen. Dabei wird
 * sie nur aktiv, wenn wirklich ein Video in einem Repository ausgewählt ist.
 * 
 * @author Florian Stegmaier
 */
public class OpenMediaPlayerAction extends Action implements
		ISelectionListener, IWorkbenchAction {
	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(OpenMediaPlayerAction.class);
	public final static String ID = OpenMediaPlayerAction.class.getName();

	private final IWorkbenchWindow window;
	private IAbstractBean selectedMedia;

	// private String selectedMedia;

	/**
	 * Konstruktor...
	 * 
	 * @param window
	 */
	public OpenMediaPlayerAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setEnabled(false);
		setText(Messages.PlayMovieClip_PlayMovieClip);
		setToolTipText(Messages.PlayMovieClip_PlayMovieClipToolTip);
		setImageDescriptor(Icons.ACTION_EDITOR_MEDIAPLAYER.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_EDITOR_MEDIAPLAYER
				.getDisabledImageDescriptor());
		window.getSelectionService().addSelectionListener(this);
	}

	/**
	 * Wird verwendet, wenn nur der entsprechende Viewer geöffnet werden soll
	 * 
	 * @param window
	 * @param mediaObject
	 */
	public OpenMediaPlayerAction(IWorkbenchWindow window,
			IAbstractBean mediaObject) {
		this.window = window;
		this.selectedMedia = mediaObject;
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

		if (selectedMedia != null) {
			MediaPlayerWidgetInput input = new MediaPlayerWidgetInput(selectedMedia);
			try {
				IWorkbenchPage page = window.getActivePage();
				page.openEditor(input, MediaPlayerView.ID, true);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		
		IStructuredSelection incoming = (IStructuredSelection) selection;
		if (incoming.getFirstElement() instanceof MediaTreeLeaf) {
			IAbstractBean mo = ((MediaTreeLeaf) incoming.getFirstElement())
					.getMediaObject();
			if (mo instanceof Video) {
				setText(Messages.PlayMovieClip_PlayMovieClip);
				selectedMedia = mo;
				setEnabled(true);
			} else if (mo instanceof Audio || mo instanceof AudioPart) {
				setText(Messages.PlayAudioClip_PlayAudioClip);
				selectedMedia = mo;
				setEnabled(true);
			} else {
				setEnabled(false);
			}
		} else {
			setEnabled(false);
			setText(Messages.PlayClip_PlayClip);
		}
	}
}