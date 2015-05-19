package org.iviPro.actions.nondestructive;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.PictureGallery;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.RichText;
import org.iviPro.model.resources.Subtitle;
import org.iviPro.model.resources.Video;
import org.iviPro.theme.Icons;
import org.iviPro.views.mediarepository.MediaTreeLeaf;

/**
 * Action zum Anzeigen von Medien-Objekten.
 * 
 * @author dellwo
 */
public class MediaViewAction extends Action implements ISelectionListener,
		IWorkbenchAction {

	public static final String ID = MediaViewAction.class.getName();
	private final IWorkbenchWindow window;
	private IAbstractBean mediaObject;

	/**
	 * Konstruktor...
	 * 
	 * @param window
	 */
	public MediaViewAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setEnabled(false);
		updateTextAndIcon();
		window.getSelectionService().addSelectionListener(this);		
	}

	public MediaViewAction(IWorkbenchWindow window, IAbstractBean mediaObject) {
		this.window = window;
		this.mediaObject = mediaObject;
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
		Action action = null;
		if (mediaObject instanceof Video || mediaObject instanceof Audio || mediaObject instanceof AudioPart) {			
			action = new OpenMediaPlayerAction(window, mediaObject);
		} else if (mediaObject instanceof Picture || mediaObject instanceof PictureGallery) {
			action = new OpenImageViewerAction(window, mediaObject);
		} else if (mediaObject instanceof RichText) {
			action = new OpenRichtextViewerAction(window,
					(RichText) mediaObject);
		} else if (mediaObject instanceof Subtitle) {
			action = new OpenSubtitleViewerAction(window, (Subtitle) mediaObject);
		}		
		if (action != null) {
			action.run();
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IStructuredSelection incoming = (IStructuredSelection) selection;
		if (incoming.getFirstElement() instanceof MediaTreeLeaf) {
			mediaObject = ((MediaTreeLeaf) incoming.getFirstElement())
					.getMediaObject();
			setEnabled(true);
		} else {
			setEnabled(false);
		}
		updateTextAndIcon();
	}

	/**
	 * Aktualisiert Icon und Text der Action entsprechend dem gerade
	 * selektierten Medien-Objekt
	 */
	private void updateTextAndIcon() {
		String text = Messages.MediaViewAction_Default_Text;
		String tooltip = Messages.MediaViewAction_Default_Tooltip;
		Icons icon = Icons.ACTION_MEDIA_VIEW;

		if (mediaObject instanceof Video) {
			text = Messages.MediaViewAction_Video_Text;
			tooltip = Messages.MediaViewAction_Video_Tooltip;
			icon = Icons.ACTION_EDITOR_MEDIAPLAYER;
		} else if (mediaObject instanceof Audio) {
			text = Messages.MediaViewAction_Audio_Text;
			tooltip = Messages.MediaViewAction_Audio_Tooltip;
			icon = Icons.ACTION_EDITOR_MEDIAPLAYER;
		} else if (mediaObject instanceof Picture) {
			text = Messages.MediaViewAction_Image_Text;
			tooltip = Messages.MediaViewAction_Image_Tooltip;
			icon = Icons.ACTION_EDITOR_IMAGE;
		} else if (mediaObject instanceof RichText) {
			text = Messages.MediaViewAction_Richtext_Text;
			tooltip = Messages.MediaViewAction_Richtext_Tooltip;
			icon = Icons.ACTION_EDITOR_RICHTEXT;			
		} else if (mediaObject instanceof Subtitle) {
			text = Messages.MediaViewAction_Subtitle_Text;
			tooltip = Messages.MediaViewAction_Subtitle_Tooltip;
			icon = Icons.ACTION_EDITOR_SUBTITLE;
		}

		setText(text);
		setToolTipText(tooltip);
		setImageDescriptor(icon.getImageDescriptor());
		setDisabledImageDescriptor(icon.getDisabledImageDescriptor());
	}
}