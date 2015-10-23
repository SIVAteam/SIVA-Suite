package org.iviPro.operations.video;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.iviPro.application.Application;
import org.iviPro.model.Project;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.resources.Scene;
import org.iviPro.operations.CompoundOperation;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.global.ChangeImagesOperation;
import org.iviPro.operations.global.ChangeKeywordsOperation;
import org.iviPro.operations.global.ChangeTimeOperation;
import org.iviPro.operations.global.ChangeTitleOperation;
import org.iviPro.utils.SivaTime;
import org.iviPro.views.scenerepository.SceneRepository;

/**
 * Operation zum Ändern einer Szene.
 * 
 * @author juhoffma
 * 
 */
public class SceneChangeOperation extends IAbstractOperation {

	// Die Variablen zum Speichern der Operations-Daten
	private Scene scene;
	
	/**
	 * Time of the frame which should be used as thumbnail. 
	 * No separate operation is created for this value. 
	 */
	private long oldThumbTime;
	private long newThumbTime;
	
	// hält Operationen zum Ändern von Titel, Zeit ...
	CompoundOperation<IAbstractOperation> changeScene;
	
	private boolean isNew;

	/**
	 * Erstellt eine neue Operation zum Hinzufuegen eines Szene-Objekts zum
	 * Video.
	 * 
	 * @param scene
	 *            Die zugrundeliegende Szene.
	 * @param title
	 *            Der Titel der Szene
	 * @param start
	 *            Der Start-Timestamp der Szene im Video.
	 * @param end
	 *            Der End-Timestamp der Szene im Video.
	 * @param keywords
	 * 			  Keywörter der Szene
	 * @throws IllegalArgumentException
	 *             Falls einer der Parameter null ist.
	 */
	public SceneChangeOperation(Scene scene, String title, SivaTime start,
			SivaTime end, String keywords, long thumbTime) throws IllegalArgumentException {
		super(Messages.SceneCreateOperation_UndoLabel);
		Project project = Application.getCurrentProject();
		if (project == null || title == null || start == null || end == null || keywords == null) {
			throw new IllegalArgumentException(
					"None of the parameters may be null."); //$NON-NLS-1$
		}
		changeScene = new CompoundOperation<IAbstractOperation>(Messages.SceneCreateOperation_UndoLabel);
		changeScene.addOperation(new ChangeTitleOperation(scene, title));
		changeScene.addOperation(new ChangeKeywordsOperation(scene, keywords));
		changeScene.addOperation(new ChangeTimeOperation(scene, start.getNano(), end.getNano()));				
		changeScene.addOperation(new ChangeImagesOperation(scene));
		
		this.scene = scene;
		// Adapt timings of annotations relying on this scene
		List<IGraphNode> potentialAnnos = project.getSceneGraph()
				.searchNodes(INodeAnnotation.class);
		for (IGraphNode potAnno : potentialAnnos) {
			INodeAnnotation anno = (INodeAnnotation)potAnno;
			NodeScene parentScene = anno.getParentScene();
			if (parentScene != null && parentScene.getScene() == scene) {
				long annoStart = (start.getNano() > anno.getStart()?start.getNano()
						:anno.getStart());
				long annoEnd = (end.getNano() < anno.getEnd()?end.getNano()
						:anno.getEnd());
				changeScene.addOperation(new ChangeTimeOperation(anno, 
						annoStart, annoEnd));
			}
		}
		
		// Could be done in a separate operation but would be overhead
		this.oldThumbTime = scene.getThumbnail().getTime();
		this.newThumbTime = thumbTime;
		
		if (scene.getVideo().getScene(scene.getTitle(), Application.getCurrentLanguage()) == null)  {
			this.isNew = true;
		}
		// Zeige das Szenen-Repository an, wenn eine Szene hinzugefuegt wurde.
		IViewPart sceneRepository = Application.getDefault().getView(
				SceneRepository.ID);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.activate(sceneRepository);
	}

	@Override
	public boolean canExecute() {
		return scene != null;
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
		changeScene.execute(monitor, info);
		scene.changeThumbnailTime(newThumbTime);
		if (isNew) {
			scene.getVideo().getScenes().add(scene);	
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		changeScene.undo(monitor, info);
		scene.changeThumbnailTime(oldThumbTime);
		if (isNew) {
			scene.getVideo().getScenes().remove(scene);
		}
		return Status.OK_STATUS;
	}

}