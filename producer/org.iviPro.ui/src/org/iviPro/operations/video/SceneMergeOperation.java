package org.iviPro.operations.video;

import java.util.LinkedList;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.iviPro.application.Application;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.views.scenerepository.SceneRepository;

/**
 * Operation zum Ändern einer Szene.
 * 
 * @author juhoffma
 * 
 */
public class SceneMergeOperation extends IAbstractOperation {

	// Die Variablen zum Speichern der Operations-Daten
	private final Video video;
	private final String sceneName1; //$NON-NLS-1$
	private final String sceneName2; //$NON-NLS-1$
	
	private Scene oldScene1;
	private Scene oldScene2;
	private Scene newScene;

	/**
	 * Erstellt eine neue Operation zum Hinzufuegen eines Szene-Objekts zum
	 * Video.
	 * 
	 * @param title1
	 *            Der Titel der ersten Szene
	 * @param title2
	 * 			  Der Titel der zweiten Szene
	 * @param video das Video zu denen die Szenen gehören
	 * @throws IllegalArgumentException
	 *             Falls einer der Parameter null ist.
	 */
	public SceneMergeOperation(String title1, String title2, Video video) throws IllegalArgumentException {
		super(Messages.SceneCreateOperation_UndoLabel);
		if (video == null || title1 == null || title2 == null) {
			throw new IllegalArgumentException(
					"None of the parameters may be null."); //$NON-NLS-1$
		}
		this.video = video;
		this.sceneName1 = title1;
		this.sceneName2 = title2;
		
		for (Scene scene : video.getScenes()) {
			if (scene.getTitle().equals(sceneName1)) {
				oldScene1 = new Scene(sceneName1, video, Application.getCurrentProject());
				oldScene1.setStart(scene.getStart());
				oldScene1.setEnd(scene.getEnd());
				oldScene1.setKeywords(scene.getKeywords());
				oldScene1.setDescription(scene.getDescription());
			} else if (scene.getTitle().equals(sceneName2)) {
				oldScene2 = new Scene(sceneName2, video, Application.getCurrentProject());
				oldScene2.setStart(scene.getStart());
				oldScene2.setEnd(scene.getEnd());
				oldScene2.setKeywords(scene.getKeywords());
				oldScene2.setDescription(scene.getDescription());
			}
			if (this.oldScene1 != null && this.oldScene2 != null) {
				if (!oldScene1.getTitle().equals(oldScene2.getTitle())) {
					newScene = new Scene(Messages.SceneMergeOperation_NewScenePrefix + sceneName1 + " + " + sceneName2, video, Application.getCurrentProject()); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
					
					long startTime = oldScene1.getStart();
					long endTime = oldScene2.getEnd();
	
					// erstelle die neue Szene
					if (oldScene1.getStart() > oldScene2.getStart()) {
						startTime = oldScene2.getStart();
					}
					if (oldScene1.getEnd() < oldScene2.getEnd()) {
						endTime = oldScene2.getEnd();
					}
					
					String keywords = oldScene1.getKeywords() + "\\," + oldScene2.getKeywords(); //$NON-NLS-1$
					newScene.setStart(startTime);
					newScene.setEnd(endTime);
					newScene.setKeywords(keywords);
					break;
				}
			}
		}		
		
		// Zeige das Szenen-Repository an, wenn eine Szene hinzugefuegt wurde.
		IViewPart sceneRepository = Application.getDefault().getView(
				SceneRepository.ID);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.activate(sceneRepository);
	}

	@Override
	public boolean canExecute() {
		return oldScene1 != null && oldScene2 != null;
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
		// Messagebox Style
		int style = SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL;

		MessageBox messageBox = new MessageBox(Display.getCurrent()
				.getActiveShell(), style);
		messageBox.setText(Messages.SceneMergeAction_MsgBox_Title);
		messageBox.setMessage(Messages.SceneMergeAction_MsgBox_Text1
				+ sceneName1 + Messages.SceneMergeAction_MsgBox_Text2
				+ sceneName2 + Messages.SceneMergeAction_MsgBox_Text3);

		// geklickter Button
		int result = messageBox.open();

		switch (result) {
		case SWT.OK:
			LinkedList<Scene> toRemove = new LinkedList<Scene>();
			for (Scene scene : video.getScenes()) {
				if (scene.getTitle().equals(sceneName1) || scene.getTitle().equals(sceneName2)) {
					toRemove.add(scene);
				}
			}
			video.getScenes().removeAll(toRemove);
			video.getScenes().add(newScene);
			return Status.OK_STATUS;
		}		
		return Status.CANCEL_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
			video.getScenes().remove(newScene);
			video.getScenes().add(oldScene1);
			video.getScenes().add(oldScene2);
		return Status.OK_STATUS;
	}

}