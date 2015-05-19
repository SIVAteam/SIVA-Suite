package org.iviPro.operations.toc;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.model.TocItem;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.operations.IAbstractOperation;

public class ChangeSceneOperation extends IAbstractOperation {

	private final TocItem item;
	private final NodeScene oldScene;
	private final NodeScene newScene;
	
	public ChangeSceneOperation(TocItem item, NodeScene scene) {
		super(Messages.ChangeSceneOperation_Title);
		
		this.item = item;
		this.oldScene = item.getScene();
		this.newScene = scene;
	}

	@Override
	public boolean canExecute() {
		return item != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		// TODO Auto-generated method stub
		return Messages.ChangeSceneOperation_Error + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		item.setScene(newScene);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		item.setScene(oldScene);
		return Status.OK_STATUS;
	}

}
