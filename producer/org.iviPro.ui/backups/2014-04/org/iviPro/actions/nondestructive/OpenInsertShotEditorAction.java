package org.iviPro.actions.nondestructive;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.editors.sceneeditor.DefineScenesEditor;
import org.iviPro.editors.sceneeditor.DefineScenesShotInsertEditorInput;
import org.iviPro.editors.shotoverview.ShotOverviewEditor;
import org.iviPro.model.Video;

/**
 * 
 * @author zwicklba
 * 
 */
public class OpenInsertShotEditorAction extends Action implements
		ISelectionListener, IWorkbenchAction {

	public final static String ID = OpenInsertShotEditorAction.class.getName();

	private final IWorkbenchWindow window;

	private Video video;

	private long start;

	private long end;
	
	private long framePosToAdd;
	
	private ShotOverviewEditor ed;
	
	private int shotId;

	public OpenInsertShotEditorAction(IWorkbenchWindow window, ShotOverviewEditor ed, Video video,
			long start, long end, long framePosToAdd, int shotId) {
		this.ed = ed;
		this.window = window;
		this.video = video;
		this.start = start;
		this.end = end;
		this.framePosToAdd = framePosToAdd;
		this.shotId = shotId;
		window.getSelectionService().addSelectionListener(this);
	}

	@Override
	public void run() {
		DefineScenesShotInsertEditorInput input = new DefineScenesShotInsertEditorInput(
				video, start, end, ed, framePosToAdd, shotId);
		IWorkbenchPage page = window.getActivePage();
		try {
			page.openEditor(input, DefineScenesEditor.ID, true);
			if (page.getActiveEditor() instanceof DefineScenesEditor) {
				((DefineScenesEditor) page.getActiveEditor()).createNewScene();
			}
		} catch (PartInitException e) {
			e.printStackTrace();
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
