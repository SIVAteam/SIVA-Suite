package org.iviPro.operations.video;

import java.util.ArrayList;
import java.util.List;

import org.iviPro.application.Application;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;
import org.iviPro.operations.IAbstractDeleteOperation;

/**
 * Operation zum Loeschen einer Szene
 * 
 * @author dellwo
 * 
 */
public class SceneDeleteOperation extends IAbstractDeleteOperation {

	// Die Variablen zum Speichern der Operations-Daten
	private final Video video;
	private final Scene scene;

	/**
	 * Erstellt eine neue Operation zum Löschen eines Szenen-Objekts aus dem
	 * Projekt.
	 * 
	 * @param scene
	 *            Die zu loeschende Szene.
	 * @throws IllegalArgumentException
	 *             Falls einer der Parameter null ist.
	 */
	public SceneDeleteOperation(Scene scene) throws IllegalArgumentException {
		super(Messages.SceneDeleteOperation_UndoLabel, Application
				.getCurrentProject());
		Video video = (scene == null ? null : scene.getVideo());
		if (video == null) {
			throw new IllegalArgumentException(
					"None of the parameters may be null."); //$NON-NLS-1$
		}
		this.scene = scene;
		this.video = video;
	}

	@Override
	public boolean canExecute() {
		return scene != null && video != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.SceneDeleteOperation_ErrorMsg + e.getMessage();
	}

	@Override
	protected boolean deleteObjects() {
		video.getScenes().remove(scene);
		return true;
	}

	@Override
	protected List<IAbstractBean> getObjectsToDelete() {
		List<IAbstractBean> toDelete = new ArrayList<IAbstractBean>();
		toDelete.add(scene);
		return toDelete;
	}

	@Override
	protected void restoreObjects() {
		video.getScenes().add(scene);
	}

}