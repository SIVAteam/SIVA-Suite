package org.iviPro.operations.toc;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.model.TocItem;
import org.iviPro.operations.IAbstractOperation;

public class ChangeTocItemOperation extends IAbstractOperation {

	private final TocItem item;
	private final List<TocItem> oldChildren;
	private final List<TocItem> newChildren;
	
	public ChangeTocItemOperation(TocItem item, List<TocItem> children) {
		super(Messages.ChangeTocItemOperation_Title);
		
		this.item = item;
		this.oldChildren = new LinkedList<TocItem>();
		this.newChildren = new LinkedList<TocItem>();
		
		for (TocItem t : item.getChildren()) {
			oldChildren.add(t);
		}
		
		for (TocItem t : children) {
			newChildren.add(t);
		}
	}

	@Override
	public boolean canExecute() {
		return item != null && oldChildren != null && newChildren != null;
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
		List<TocItem> chil = item.getChildren();
		chil.clear();
		for (TocItem t : newChildren) {
			chil.add(t);
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		List<TocItem> chil = item.getChildren();
		chil.clear();
		for (TocItem t : oldChildren) {
			chil.add(t);
		}
		return Status.OK_STATUS;
	}

}
