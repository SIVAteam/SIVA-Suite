package org.iviPro.operations.annotation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Aendern der Mute Option bei NodeAnnotationAudio
 * 
 * @author hoffmanj
 * 
 */
public class ChangeMuteOperation extends IAbstractOperation {

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
	public ChangeMuteOperation(INodeAnnotation target, Boolean mute)
			throws IllegalArgumentException {
		super(Messages.ChangeMuteOperation_Label);
		if (target == null || mute == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		this.newVal = new Boolean(mute);
		this.oldVal = new Boolean(target.isPauseVideo());
		this.target = target;
	}

	@Override
	public boolean canExecute() {
		return target != null && newVal != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ChangeMuteOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setMuteVideo(newVal);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setMuteVideo(oldVal);		
		return Status.OK_STATUS;
	}

}
