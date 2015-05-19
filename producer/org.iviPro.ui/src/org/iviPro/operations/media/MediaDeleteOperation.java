package org.iviPro.operations.media;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.iviPro.application.Application;
import org.iviPro.model.BeanList;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.RichText;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;
import org.iviPro.operations.IAbstractDeleteOperation;

/**
 * Operation zum Loeschen eines Medien-Objekts aus einem Projekt.
 * 
 * @author dellwo
 * 
 */
public class MediaDeleteOperation extends IAbstractDeleteOperation {

	/** Das Projekt aus dem das Medien-Objekt geloescht werden soll. */
	private final Project project;

	/** Das zu loeschende Medien-Objekt. */
	private final IAbstractBean mediaObject;
	
	private Map<String, String> documentContents;
	
	/**
	 * Erstellt eine neue Operation zum Loeschen eines Medien-Objekts aus einem
	 * Projekt.
	 * 
	 * @param mediaObject
	 *            Das Medien-Objekt das geloescht werden soll.
	 * @throws IllegalArgumentException
	 *             Falls dass Medien-Objekt null ist oder kein Projekt geoeffnet
	 *             ist.
	 */
	public MediaDeleteOperation(IAbstractBean mediaObject)
			throws IllegalArgumentException {
		super(Messages.MediaDeleteOperation_LabelGeneralObject, Application
				.getCurrentProject());
		Project project = Application.getCurrentProject();
		if (mediaObject == null || project == null) {
			throw new IllegalArgumentException(
					"None of the parameters may be null."); //$NON-NLS-1$
		}
		this.project = project;
		this.mediaObject = mediaObject;
		// Label anpassen, je nach Medien-Objekt-Typ
		if (mediaObject instanceof Video) {
			setLabel(Messages.MediaDeleteOperation_LabelVideo);
		} else if (mediaObject instanceof Picture) {
			setLabel(Messages.MediaDeleteOperation_LabelImage);
		} else if (mediaObject instanceof RichText) {
			setLabel(Messages.MediaDeleteOperation_LabelRichtext);
		} else if (mediaObject instanceof Audio
				| mediaObject instanceof AudioPart) {
			setLabel(Messages.MediaDeleteOperation_LabelAudio);
		}
	}

	@Override
	public boolean canExecute() {
		return project != null && mediaObject != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.MediaDeleteOperation_ErrorMsg + e.getMessage();
	}

	@Override
	protected boolean deleteObjects() {
		if (mediaObject instanceof AudioPart) {
			Audio audio = ((AudioPart) mediaObject).getAudio();
			audio.getAudioParts().remove(mediaObject);
			return true;
		} else if (mediaObject instanceof Audio) {
			BeanList<AudioPart> audioParts = ((Audio) mediaObject).getAudioParts();
			StringBuilder message = new StringBuilder();
			if (audioParts.size() > 0) {
				message.append(Messages.MediaDeleteOperation_AudioPartDeleteMessage);
				message.append("\n"); //$NON-NLS-1$
				for (AudioPart part : audioParts) {
					message.append(part.getTitle() + "\n"); //$NON-NLS-1$
				}
				boolean doIt = MessageDialog.openConfirm(Display.getCurrent()
						.getActiveShell(),
						Messages.MediaDeleteOperation_AudioPartDeleteText,
						message.toString());
				if (!doIt) {
					return false;
				}
			}
		} else if (mediaObject instanceof Picture) {
				documentContents = RichText.removeAll(((Picture) mediaObject).getId(), project);
				((Picture) mediaObject).clearThumbnails();
			// TODO force reload if any richtext editor is opened
			
		} else if (mediaObject instanceof RichText) {
			RichText richtext = ((RichText) mediaObject);
			if (!richtext.isFromMedia()) {
				// Let the file be deleted on next project save operation
				project.getUnusedFiles().add(richtext.getFile().getValue());
			}
		}
		
		project.getMediaObjects().remove(mediaObject);			
		return true;
	}

	@Override
	protected List<IAbstractBean> getObjectsToDelete() {
		List<IAbstractBean> toDelete = new ArrayList<IAbstractBean>();
		toDelete.add(mediaObject);
		if (mediaObject instanceof Audio) {
			for (AudioPart part : ((Audio) mediaObject).getAudioParts()) {
				toDelete.add(part);
			}
		} else if (mediaObject instanceof Video) {
			for (Scene part : ((Video) mediaObject).getScenes()) {
				toDelete.add(part);
			}
		}
		return toDelete;
	}

	@Override
	protected void restoreObjects() {
		if (mediaObject instanceof AudioPart) {
			Audio audio = ((AudioPart) mediaObject).getAudio();
			audio.getAudioParts().add((AudioPart) mediaObject);
		} else if (mediaObject instanceof Picture) {
			RichText.restoreAll(documentContents, project);
			project.getMediaObjects().add(mediaObject);
		} else if (mediaObject instanceof RichText) {
			RichText richtext = (RichText)mediaObject;
			if (!richtext.isFromMedia()) {
				// Don't delete file on next project save operation
				project.getUnusedFiles().remove(richtext.getFile().getValue());
			}
		} else {
			project.getMediaObjects().add(mediaObject);
		}
	}
}
