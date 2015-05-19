package org.iviPro.operations.global;

import java.util.Locale;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Aendern des Titels eines Model-Objekts.
 * 
 * @author dellwo
 * 
 */
public class ChangeTitleOperation extends IAbstractOperation {

	private final IAbstractBean target;
	private final LocalizedString newTitle;
	private final LocalizedString oldTitle;

	/**
	 * Erstellt eine neue Operation zum Aendern des Titels eines Model-Objekts.
	 * Als Sprache für den Titel wird die aktuelle Projekt-Sprache verwendet.
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Titel geaendert werden soll.
	 * @param newTitle
	 *            Der neue Titel in der aktuellen Projekt-Sprache.
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter null ist.
	 */
	public ChangeTitleOperation(IAbstractBean target, String newTitle)
			throws IllegalArgumentException {
		super(Messages.ChangeTitleOperation_Label);
		if (target == null || newTitle == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		Locale lang = Application.getCurrentLanguage();
		this.newTitle = new LocalizedString(newTitle, lang);
		this.oldTitle = new LocalizedString(target.getTitle(lang), lang);
		this.target = target;
	}

	/**
	 * Erstellt eine neue Operation zum Aendern des Titels eines Model-Objekts.
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Titel geaendert werden soll.
	 * @param newTitle
	 *            Der neue Titel.
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter null ist.
	 */
	public ChangeTitleOperation(IAbstractBean target, LocalizedString newTitle)
			throws IllegalArgumentException {
		super(Messages.ChangeTitleOperation_Label);
		if (target == null || newTitle == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		this.target = target;
		this.newTitle = newTitle;
		Locale lang = newTitle.getLanguage();
		this.oldTitle = new LocalizedString(target.getTitle(lang), lang);
	}

	/**
	 * Erstellt eine neue Operation zum Aendern des Titels eines Model-Objekts
	 * mit der Möglichkeit, das Label zu definieren, mit dem die Operation z.B.
	 * in der Undo/Redo Liste auftaucht.
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Titel geaendert werden soll.
	 * @param newTitle
	 *            Der neue Titel.
	 * @param label
	 *            Das Label der Operation z.B. fuer die Undo/Redo Menuepunkte.
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter null ist.
	 */
	public ChangeTitleOperation(IAbstractBean target, LocalizedString newTitle,
			String label) throws IllegalArgumentException {
		super(label);
		if (target == null || newTitle == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		this.target = target;
		this.newTitle = newTitle;
		Locale lang = newTitle.getLanguage();
		this.oldTitle = new LocalizedString(target.getTitle(lang), lang);
	}

	@Override
	public boolean canExecute() {
		return target != null && newTitle != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ChangeTitleOperation_ErrorMsg + e.getMessage();
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
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setTitle(oldTitle);
		return Status.OK_STATUS;
	}

}
