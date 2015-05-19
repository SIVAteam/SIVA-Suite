package org.iviPro.operations.video;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.editors.common.BeanNameGenerator;
import org.iviPro.model.Project;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.global.ChangeImagesOperation;
import org.iviPro.utils.SivaTime;

/**
 * Operation zum Erstellen einer Szene.
 * 
 * @author dellwo
 * 
 */
public class SceneCreateOperation extends IAbstractOperation {

	// Die Variablen zum Speichern der Operations-Daten
	private final Video video;
	private final Scene scene;

	/**
	 * Erstellt eine neue Operation zum Hinzufuegen eines Scene-Objekts zum
	 * Video.
	 * 
	 * @param title
	 *            Der Titel der Szene
	 * @param video
	 *            Das Video der Szene.
	 * @param start
	 *            Der Start-Timestamp der Szene im Video.
	 * @param end
	 *            Der End-Timestamp der Szene im Video.
	 * @param keywords
	 * 			  Keywörter der Szene
	 * @throws IllegalArgumentException
	 *             Falls einer der Parameter null ist.
	 */
	public SceneCreateOperation(String title, Video video, SivaTime start,
			SivaTime end, String keywords) throws IllegalArgumentException {
		super(Messages.SceneCreateOperation_UndoLabel);
		Project project = Application.getCurrentProject();
		if (video == null || project == null || title == null || start == null
				|| end == null || keywords == null) {
			throw new IllegalArgumentException(
					"None of the parameters may be null."); //$NON-NLS-1$
		}
		this.video = video;
		this.scene = new Scene("", video, project); //$NON-NLS-1$
		BeanNameGenerator nameGen = new BeanNameGenerator(title, scene, video.getScenes(), ""); //$NON-NLS-1$
		String newTitle = nameGen.generateAuto();
		scene.setTitle(newTitle);
		this.scene.setStart(start.getNano());
		this.scene.setEnd(end.getNano());
		this.scene.setKeywords(keywords);
	}

	@Override
	public boolean canExecute() {
		return scene != null && video != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.SceneCreateOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		new ChangeImagesOperation(scene).execute(monitor, info);
		video.getScenes().add(scene);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		video.getScenes().remove(scene);
		return Status.OK_STATUS;
	}

}