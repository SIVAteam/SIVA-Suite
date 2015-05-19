package org.iviPro.operations.toc;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.model.TocItem;
import org.iviPro.operations.IAbstractOperation;

public class MoveTocItemOperation extends IAbstractOperation {

	private ChangeTocItemOperation operationSource;
	private ChangeTocItemOperation operationTarget;
	/*private final TocItem itemSource;
	private final TocItem itemTarget;
	private final List<TocItem> oldChildrenSource;
	private final List<TocItem> newChildrenSource;
	private final List<TocItem> oldChildrenTarget;
	private final List<TocItem> newChildrenTarget;*/
	
	public MoveTocItemOperation(TocItem itemSource, List<TocItem> childrenSource, 
								TocItem itemTarget, List<TocItem> childrenTarget) {
		super(Messages.ChangeTocItemOperation_Title);
		operationSource = new ChangeTocItemOperation(itemSource, childrenSource);
		operationTarget = new ChangeTocItemOperation(itemTarget, childrenTarget);
		
	}

	@Override
	public boolean canExecute() {
		return operationSource.canExecute() && operationTarget.canExecute();
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ChangeTocItemOperation_Error + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		operationSource.redo(monitor, info);
		operationTarget.redo(monitor, info);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		operationSource.undo(monitor, info);
		operationTarget.undo(monitor, info);
		return Status.OK_STATUS;
	}

}
