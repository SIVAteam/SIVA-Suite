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
public class ChangePauseOperation extends IAbstractOperation {

	private final INodeAnnotation target;
	private final Boolean newVal;
	private final Boolean oldVal;

	/**
	 * Erstellt eine neue Operation zum Aendern der Pauseoption eines Model-Objekts.
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Pauseoption geaendert werden soll.
	 * @param pause
	 *            Der neue Wert für pause
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter null ist.
	 */
	public ChangePauseOperation(INodeAnnotation target, Boolean pause)
			throws IllegalArgumentException {
		super(Messages.ChangePauseOperation_Label);
		if (target == null || pause == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		this.newVal = new Boolean(pause);
		this.oldVal = new Boolean(target.isPauseVideo());
		this.target = target;
	}

	@Override
	public boolean canExecute() {
		return target != null && newVal != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ChangePauseOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setPauseVideo(newVal);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setPauseVideo(oldVal);
		return Status.OK_STATUS;
	}

}
