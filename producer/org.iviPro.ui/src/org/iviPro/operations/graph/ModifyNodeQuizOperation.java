package org.iviPro.operations.graph;

import java.util.Locale;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.operations.IAbstractOperation;

public class ModifyNodeQuizOperation extends IAbstractOperation {


	private final NodeQuiz target;
	private final LocalizedString newTitle;
	private final LocalizedString oldTitle;
	private final int newId;
	private final int oldId;

	/**
	 * @param target
	 * @param newTitle
	 * @throws IllegalArgumentException
	 */
	public ModifyNodeQuizOperation(NodeQuiz target,
			String newTitle, int testId) throws IllegalArgumentException {
		this(target, new LocalizedString(newTitle,
				Application.getCurrentLanguage()), testId);
	}

	/**
	 * Erstellt eine neue Operation zum Aendern des Titels eines Model-Objekts.
	 * Als Sprache f�r den Titel wird die aktuelle Projekt-Sprache verwendet.
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Titel geaendert werden soll.
	 * @param newTitle
	 *            Der neue Titel in der aktuellen Projekt-Sprache.
	 * @param testId	identifier of the test which should be associated with 
	 * 					the quiz model
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter (ausser newDefaultControl)
	 *             null ist.
	 */
	public ModifyNodeQuizOperation(NodeQuiz target,
			LocalizedString newTitle, int testId)
			throws IllegalArgumentException {
		super(Messages.ModifyAbstractNodeSelectionControlOperation_Title);
		if (target == null || newTitle == null) {
			throw new IllegalArgumentException(
					"Target and title must not be null."); //$NON-NLS-1$
		}
		Locale lang = Application.getCurrentLanguage();
		this.newTitle = newTitle;
		this.oldTitle = new LocalizedString(target.getTitle(lang), lang);
		this.newId = testId;
		this.oldId = target.getTestId();
		this.target = target;
	}

	@Override
	public boolean canExecute() {
		return target != null && newTitle != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ModifyAbstractNodeSelectionControlOperation_ErrorMsg
				+ e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setTitle(newTitle);
		target.setTestId(newId);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setTitle(oldTitle);
		target.setTestId(oldId);
		return Status.OK_STATUS;
	}
	
}
