package org.iviPro.operations;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class CompoundOperation<T extends IAbstractOperation> extends
		IAbstractOperation {

	private static final Logger logger = Logger
			.getLogger(CompoundOperation.class);

	public CompoundOperation(String label) {
		super(label);
	}

	private List<T> childOperations = new ArrayList<T>();
	private boolean executed = false;

	public void addOperation(T operation) throws IllegalStateException {
		if (!executed) {
			childOperations.add(operation);
		} else {
			String errorMsg = "Adding of child operations is not possible after the compound operation was executed."; //$NON-NLS-1$
			logger.error(errorMsg);
			throw new IllegalStateException(errorMsg);
		}
	}

	@Override
	public boolean canExecute() {
		// Mindestens eine Kind-Operation benoetigt
		if (childOperations.isEmpty()) {
			return false;
		}
		// Nur wenn alle Kind-Operationen ausfuehrbar sind, ist auch die
		// CompoundOperation ausfuehrbar
		for (IAbstractOperation operation : childOperations) {
			if (!operation.canExecute()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String getErrorMessage(Exception e) {
		if (childOperations.isEmpty()) {
			return "No operations where set."; //$NON-NLS-1$
		} else {
			return childOperations.get(0).getErrorMessage(e);
		}
	}

	private boolean allOperationsSuccessfull = false;

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {

		// Wenn nur eine Child-Operation da ist, dann wird einfach der Status
		// davon zurueck gegeben. Ist ja dann so, als haette man eigentlich
		// keine Compound-Operation.
		if (childOperations.size() == 1) {
			IStatus opStatus = childOperations.get(0).execute(monitor, info);
			allOperationsSuccessfull = Status.OK_STATUS.equals(opStatus);
			return opStatus;
		}
		// Es gibt mehrere Child-Operationen:
		// Wenn eine Fehlschlaegt, dann rollbacken wir die bereits ausgefuehrten
		// Child-Operationen.
		for (int i = 0; i < childOperations.size(); i++) {
			try {
				IStatus opStatus = childOperations.get(i)
						.execute(monitor, info);
				if (!Status.OK_STATUS.equals(opStatus)) {
					allOperationsSuccessfull = false;
					rollback(i - 1, false, monitor, info);
					if (Status.CANCEL_STATUS.equals(opStatus)) {
						throw new ExecutionException(
								Messages.CompoundOperation_ErrorRolledBackBecauseChildCanceled);
					} else {
						throw new ExecutionException(
								Messages.CompoundOperation_ErrorRolledBackBecauseChildFailed);
					}
				}
			} catch (ExecutionException e) {
				logger.error(e.getMessage(), e);
				allOperationsSuccessfull = false;
				rollback(i - 1, false, monitor, info);
				throw e;
			}
		}
		allOperationsSuccessfull = true;
		return Status.OK_STATUS;
	}

	/**
	 * Versucht im Fehlerfall die bisher erfolgreichen Kind-Operationen
	 * rueckgaengig zu machen.
	 * 
	 * @param lastOkIndex
	 *            Der Index der letzten erfolgreichen Kind-Operation
	 * @param monitor
	 *            Progress-Monitor
	 * @param countUpwards
	 *            Gibt an, ob die rueckgaengig zu machenden Kind-Operationen
	 *            oberhalb (true) oder unterhalb (false) des lastOkIndex liegen.
	 * @param info
	 * @throws ExecutionException
	 *             Falls der Rollback von mindestens einer Kind-Operation
	 *             fehlschlug.
	 */
	private void rollback(int lastOkIndex, boolean countUpwards,
			IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		logger.warn("Rolling back compound operation " //$NON-NLS-1$
				+ "because of a previous failure."); //$NON-NLS-1$
		ExecutionException exception = null;
		int diff = (countUpwards ? 1 : -1); // Nach oben oder unten zaehlen?
		for (int i = lastOkIndex; i >= 0 && i < childOperations.size(); i = i
				+ diff) {
			try {
				childOperations.get(i).undo(monitor, info);
			} catch (ExecutionException e) {
				exception = e;
				logger.error(
						"Rollback of child operation " + childOperations.get(i) //$NON-NLS-1$
								+ " failed: " + e.getMessage(), e); //$NON-NLS-1$
			}
		}
		if (exception != null) {
			throw exception;
		}
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (!allOperationsSuccessfull) {
			return Status.CANCEL_STATUS;
		}
		for (int i = 0; i < childOperations.size(); i++) {
			try {
				IStatus opStatus = childOperations.get(i).redo(monitor, info);
				if (!Status.OK_STATUS.equals(opStatus)) {
					allOperationsSuccessfull = false;
					rollback(i - 1, false, monitor, info);
					throw new ExecutionException(
							"The operation was rolled back, because a " //$NON-NLS-1$
									+ "child operation was not successfull."); //$NON-NLS-1$
				}
			} catch (ExecutionException e) {
				logger.error(e.getMessage(), e);
				allOperationsSuccessfull = false;
				rollback(i - 1, false, monitor, info);
				throw e;
			}
		}
		allOperationsSuccessfull = true;
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (!allOperationsSuccessfull) {
			return Status.CANCEL_STATUS;
		}
		// In umgekehrter Richtung rueckgaengig machen
		for (int i = childOperations.size() - 1; i >= 0; i--) {
			try {
				IStatus opStatus = childOperations.get(i).undo(monitor, info);
				if (!Status.OK_STATUS.equals(opStatus)) {
					allOperationsSuccessfull = false;
					rollback(i + 1, true, monitor, info);
					throw new ExecutionException(
							"The operation was rolled back, because a " //$NON-NLS-1$
									+ "child operation was not successfull."); //$NON-NLS-1$
				}
			} catch (ExecutionException e) {
				logger.error(e.getMessage(), e);
				allOperationsSuccessfull = false;
				rollback(i + 1, true, monitor, info);
				throw e;
			}
		}
		allOperationsSuccessfull = true;
		return Status.OK_STATUS;
	}

	/**
	 * Gibt die Liste mit den Kind-Operationen dieser Compound-Operation
	 * zurueck.
	 * 
	 * @return List mit den Kind-Operationen.
	 */
	public List<T> getChildOperations() {
		return childOperations;
	}

}
