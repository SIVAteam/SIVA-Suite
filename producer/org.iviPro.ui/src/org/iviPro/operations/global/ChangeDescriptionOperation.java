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
 * Operation zum Aendern der Beschreibung eines Model-Objekts.
 * 
 * @author dellwo
 * 
 */
public class ChangeDescriptionOperation extends IAbstractOperation {

	private final IAbstractBean target;
	private final LocalizedString newDescription;
	private final LocalizedString oldDescription;

	/**
	 * Erstellt eine neue Operation zum Aendern der Beschreibung eines
	 * Model-Objekts. Als Sprache für die Beschreibung wird die aktuelle
	 * Projekt-Sprache verwendet.
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Beschreibung geaendert werden soll.
	 * @param newDescription
	 *            Die neue Beschreibung in der aktuellen Projekt-Sprache.
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter null ist.
	 */
	public ChangeDescriptionOperation(IAbstractBean target,
			String newDescription) throws IllegalArgumentException {
		super(Messages.ChangeDescriptionOperation_Label);
		if (target == null || newDescription == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		Locale lang = Application.getCurrentLanguage();
		this.newDescription = new LocalizedString(newDescription, lang);
		this.oldDescription = new LocalizedString(target.getDescription(lang),
				lang);
		this.target = target;
	}

	/**
	 * Erstellt eine neue Operation zum Aendern der Beschreibung eines
	 * Model-Objekts.
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Beschreibung geaendert werden soll.
	 * @param newDescription
	 *            Die neue Beschreibung.
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter null ist.
	 */
	public ChangeDescriptionOperation(IAbstractBean target,
			LocalizedString newDescription) throws IllegalArgumentException {
		super(Messages.ChangeDescriptionOperation_Label);
		if (target == null || newDescription == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		this.target = target;
		this.newDescription = newDescription;
		Locale lang = newDescription.getLanguage();
		this.oldDescription = new LocalizedString(target.getDescription(lang),
				lang);
	}

	/**
	 * Erstellt eine neue Operation zum Aendern der Beschreibung eines
	 * Model-Objekts, mit der Moeglichkeit, dass Label der Operation zu setzen,
	 * dass z.B. in der Undo/Redo History verwendet wird.
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Beschreibung geaendert werden soll.
	 * @param newDescription
	 *            Die neue Beschreibung.
	 * @param label
	 *            Das Label der Operation, wie es z.B. in den Undo/Redo
	 *            Menuepunkten auftaucht.
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter null ist.
	 */
	public ChangeDescriptionOperation(IAbstractBean target,
			LocalizedString newDescription, String label)
			throws IllegalArgumentException {
		super(label);
		if (target == null || newDescription == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		this.target = target;
		this.newDescription = newDescription;
		Locale lang = newDescription.getLanguage();
		this.oldDescription = new LocalizedString(target.getDescription(lang),
				lang);
	}

	@Override
	public boolean canExecute() {
		return target != null && newDescription != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ChangeDescriptionOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setDescription(newDescription);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setDescription(oldDescription);
		return Status.OK_STATUS;
	}

}
