package org.iviPro.operations.annotation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Aendern der Pause Option eines Model-Objekts.
 * 
 * @author hoffmanj
 * 
 */
public class ChangeDisableableOperation extends IAbstractOperation {

	private final INodeAnnotation target;
	private final Boolean newVal;
	private final Boolean oldVal;

	/**
	 * Erstellt eine neue Operation zum Aendern der Disableable-Option eines 
	 * Model-Objekts.
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Disableable-Option geaendert werden 
	 *            soll.
	 * @param pause
	 *            Der neue Wert für disableable
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter null ist.
	 */
	public ChangeDisableableOperation(INodeAnnotation target, Boolean disableable)
			throws IllegalArgumentException {
		super(Messages.ChangeDisableableOperation_Label);
		if (target == null || disableable == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		this.newVal = new Boolean(disableable);
		this.oldVal = new Boolean(target.isDisableable());
		this.target = target;
	}

	@Override
	public boolean canExecute() {
		return target != null && newVal != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ChangeDisableableOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setDisableable(newVal);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setDisableable(oldVal);
		return Status.OK_STATUS;
	}

}
