package org.iviPro.operations.audio;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.iviPro.application.Application;
import org.iviPro.model.Project;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.operations.CompoundOperation;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.global.ChangeKeywordsOperation;
import org.iviPro.operations.global.ChangeTimeOperation;
import org.iviPro.operations.global.ChangeTitleOperation;
import org.iviPro.utils.SivaTime;
import org.iviPro.views.mediarepository.MediaRepository;

/**
 * Operation zum Ändern/Erstellen eines Audio-Part.
 * 
 * @author juhoffma
 * 
 */
public class AudioPartChangeOperation extends IAbstractOperation {

	// Die Variablen zum Speichern der Operations-Daten
	private AudioPart audioPart;
	
	// hält Operationen zum Ändern von Titel, Zeit ...
	CompoundOperation<IAbstractOperation> changeAudioPart;
	
	private boolean isNew;

	/**
	 * Erstellt eine neue Operation zum Hinzufuegen eines AudioPart-Objekts zum
	 * Audio File
	 * 
	 * @param title
	 *            Der Titel des Audio-Part
	 * @param video
	 *            Das Audio-File des Audio-Part.
	 * @param start
	 *            Der Start-Timestamp des Audio-Part im Audio-File
	 * @param end
	 *            Der End-Timestamp des Audio-Part im Audio-File.
	 * @param keywords
	 * 			  Keywörter des Audio-Part
	 * @throws IllegalArgumentException
	 *             Falls einer der Parameter null ist.
	 */
	public AudioPartChangeOperation(AudioPart audioPart, String title, SivaTime start,
			SivaTime end, String keywords) throws IllegalArgumentException {
		super(Messages.AudioPartCreateOperation_UndoLabel);
		Project project = Application.getCurrentProject();
		if (project == null || title == null || start == null || end == null || keywords == null) {
			throw new IllegalArgumentException(
					"None of the parameters may be null."); //$NON-NLS-1$
		}
		changeAudioPart = new CompoundOperation<IAbstractOperation>(Messages.AudioPartCreateOperation_UndoLabel);
		changeAudioPart.addOperation(new ChangeTitleOperation(audioPart, title));
		changeAudioPart.addOperation(new ChangeKeywordsOperation(audioPart, keywords));
		changeAudioPart.addOperation(new ChangeTimeOperation(audioPart, start.getNano(), end.getNano()));
		
		this.audioPart = audioPart;
		
		if (audioPart.getAudio().getAudioPart(audioPart.getTitle(), Application.getCurrentLanguage()) == null)  {
			this.isNew = true;
		}
		// Zeige das Media-Repository an, wenn ein Audio-Part hinzugefuegt wurde.
		IViewPart sceneRepository = Application.getDefault().getView(MediaRepository.ID);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(sceneRepository);
	}

	@Override
	public boolean canExecute() {
		return audioPart != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.AudioPartCreateOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		changeAudioPart.execute(monitor, info);
		if (isNew) {
			audioPart.getAudio().getAudioParts().add(audioPart);	
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		changeAudioPart.undo(monitor, info);
		if (isNew) {
			audioPart.getAudio().getAudioParts().remove(audioPart);
		}
		return Status.OK_STATUS;
	}

}