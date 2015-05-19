package org.iviPro.operations.global;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.model.IAbstractBean;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Aendern der Keywords eines Model-Objekts.
 * 
 * @author hoffmanj
 * 
 */
public class ChangeKeywordsOperation extends IAbstractOperation {

	private final IAbstractBean target;
	private final String newKeywords;
	private final String oldKeywords;

	/**
	 * Erstellt eine neue Operation zum Aendern der Keywords eines Model-Objekts.
	 * Als Sprache für den Titel wird die aktuelle Projekt-Sprache verwendet.
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Titel geaendert werden soll.
	 * @param newKeywords
	 *            Die neuen Keywords
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter null ist.
	 */
	public ChangeKeywordsOperation(IAbstractBean target, String newKeywords)
			throws IllegalArgumentException {
		super(Messages.ChangeTitleOperation_Label);
		if (target == null || newKeywords == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		this.newKeywords = newKeywords;
		this.oldKeywords = target.getKeywords();
		this.target = target;
	}

	@Override
	public boolean canExecute() {
		return target != null && newKeywords != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ChangeKeywordsOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setKeywords(newKeywords);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setKeywords(oldKeywords);
		return Status.OK_STATUS;
	}

}
