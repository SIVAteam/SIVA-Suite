package org.iviPro.operations.global;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.model.resources.Scene;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Aendern der Zeit von Szenne und Annotationen.
 * 
 * @author hoffmanj
 * 
 */
public class ChangeTimeOperation extends IAbstractOperation {

	private final IAbstractBean target;
	private final Long newStartTime;
	private final Long newEndTime;
	private Long oldStartTime;
	private Long oldEndTime;

	/**
	 * Erstellt eine neue Operation zum Aendern der Zeit eines Model-Objekts (Szenen und Annotationen)
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Titel geaendert werden soll.
	 * @param newTime
	 *            Die neuen Keywords
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter null ist.
	 */
	public ChangeTimeOperation(IAbstractBean target, Long newStartTime, Long newEndTime)
			throws IllegalArgumentException {
		super(Messages.ChangeTimeOperation_Label);
		if (target == null || newStartTime == null || newEndTime == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		this.newStartTime = newStartTime;
		this.newEndTime = newEndTime;
		if (target instanceof Scene) {
			this.oldStartTime = ((Scene) target).getStart();
			this.oldEndTime = ((Scene) target).getEnd();			
		} else
		if (target instanceof INodeAnnotation) {
			this.oldStartTime = ((INodeAnnotation) target).getStart();
			this.oldEndTime = ((INodeAnnotation) target).getEnd();
		}
		if (target instanceof AudioPart) {
			this.oldStartTime = ((AudioPart) target).getStart();
			this.oldEndTime = ((AudioPart) target).getEnd();
		}
		this.target = target;
	}

	@Override
	public boolean canExecute() {
		if (target instanceof Scene || target instanceof INodeAnnotation) {
			return true;
		}
		return target != null && newStartTime != null && newEndTime != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ChangeTimeOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (target instanceof Scene) {
			((Scene) target).setStart(newStartTime);
			((Scene) target).setEnd(newEndTime);
		} else
		if (target instanceof INodeAnnotation) {
			((INodeAnnotation) target).setStart(newStartTime);
			((INodeAnnotation) target).setEnd(newEndTime);
		} else
		if (target instanceof AudioPart) {
			((AudioPart) target).setStart(newStartTime);
			((AudioPart) target).setEnd(newEndTime);	
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (target instanceof Scene) {
			((Scene) target).setStart(oldStartTime);
			((Scene) target).setEnd(oldEndTime);
		} else
		if (target instanceof INodeAnnotation) {
			((INodeAnnotation) target).setStart(oldStartTime);
			((INodeAnnotation) target).setEnd(oldEndTime);
		} else
		if (target instanceof AudioPart) {
			((AudioPart) target).setStart(oldStartTime);
			((AudioPart) target).setEnd(oldEndTime);	
		}
		return Status.OK_STATUS;
	}

}
