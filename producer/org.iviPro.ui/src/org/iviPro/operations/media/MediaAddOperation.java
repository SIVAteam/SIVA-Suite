package org.iviPro.operations.media;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.model.IMediaObject;
import org.iviPro.model.Project;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.PdfDocument;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.RichText;
import org.iviPro.model.resources.Video;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Hinzufuegen eines Medien-Objekts zu einem Projekt.
 * 
 * @author dellwo
 * 
 */
public class MediaAddOperation extends IAbstractOperation {

	private final Project project;
	private final IMediaObject mediaObject;

	/**
	 * Erstellt eine neue Operation zum Hinzufuegen eines Medien-Objekts zum
	 * Projekt.
	 * 
	 * @param mediaObject
	 *            Das Medien-Objekt das hinzugefuegt werden soll.
	 * @throws IllegalArgumentException
	 *             Falls dass Medien-Objekt null ist oder kein Projekt geoeffnet
	 *             ist.
	 */
	public MediaAddOperation(IMediaObject mediaObject)
			throws IllegalArgumentException {
		super(Messages.MediaAddOperation_LabelGeneralMediaObject);
		Project project = Application.getCurrentProject();
		if (mediaObject == null || project == null) {
			throw new IllegalArgumentException(
					"None of the parameters may be null."); //$NON-NLS-1$
		}
		this.project = project;
		this.mediaObject = mediaObject;
		// Label anpassen, je nach Medien-Objekt-Typ
		if (mediaObject instanceof Video) {
			setLabel(Messages.MediaAddOperation_LabelVideo);
		} else if (mediaObject instanceof Picture) {
			setLabel(Messages.MediaAddOperation_LabelPicture);
		} else if (mediaObject instanceof Audio) {
			setLabel(Messages.MediaAddOperation_LabelAudio);
		} else if (mediaObject instanceof RichText) {
			setLabel(Messages.MediaAddOperation_LabelRichtext);
		} else if (mediaObject instanceof PdfDocument) {
			setLabel(Messages.MediaAddOperation_LabelPdf);
		}
	}

	@Override
	public boolean canExecute() {
		return project != null && mediaObject != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.MediaAddOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if(mediaObject instanceof Video){
			Thread thread = new Thread(new VideoDimensionSetter((Video) mediaObject));
			thread.start();
		}
		
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		project.getMediaObjects().add(mediaObject);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		project.getMediaObjects().remove(mediaObject);
		return Status.OK_STATUS;
	}

}
