package org.iviPro.actions.undoable;

import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.application.Application;
import org.iviPro.editors.common.BeanNameGenerator;
import org.iviPro.editors.shotoverview.SceneOverviewEditor;
import org.iviPro.editors.shotoverview.SceneTransformation;
import org.iviPro.scenedetection.sd_main.Scene;
import org.iviPro.utils.SivaTime;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Video;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.video.SceneChangeOperation;

public class SaveSceneDetectionResults extends Action implements
		ISelectionListener, IWorkbenchAction {

	private IWorkbenchWindow window;

	private static Logger logger = Logger
			.getLogger(SaveSceneDetectionResults.class);

	public SaveSceneDetectionResults(SceneOverviewEditor ed,
			IWorkbenchWindow window, Video video, List<Scene> sceneListToConvert) {
		this.window = window;

		IWorkbenchWindow windowToClose = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = windowToClose.getActivePage();
		page.activate(ed);
		page.closeEditor(ed, false);

		for (Iterator<Scene> iterator = sceneListToConvert.iterator(); iterator
				.hasNext();) {
			Scene sceneToSave = (Scene) iterator.next();
			logger.debug("Creating/Changing scene: " + sceneToSave.getName());

			org.iviPro.model.Scene newSceneObj = SceneTransformation
					.createModelScene(
							new LocalizedString((sceneToSave.getName()),
									Application.getCurrentLanguage()), video,
							Application.getCurrentProject());

			BeanNameGenerator nameGen = new BeanNameGenerator(
					sceneToSave.getName(), newSceneObj, newSceneObj.getVideo()
							.getScenes(), "");
			String newTitle = nameGen.generate();
			// falls die Namensgenerierung abgebrochen wurde, wird nicht
			// gespeichert
			if (!nameGen.getCancelState()) {
				IAbstractOperation op = new SceneChangeOperation(newSceneObj,
						newTitle, new SivaTime(sceneToSave.getStartTimeNano()),
						new SivaTime(sceneToSave.getEndTimeNano()), "");
				try {
					OperationHistory.execute(op);
				} catch (ExecutionException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

	@Override
	public void selectionChanged(IWorkbenchPart arg0, ISelection arg1) {
		// TODO Auto-generated method stub

	}

}
