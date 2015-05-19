package org.iviPro.operations.annotation;

import java.util.Locale;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.global.Messages;

public class ChangeDescriptionOperation extends IAbstractOperation {

	private final INodeAnnotation target;
	private final LocalizedString newDescription;
	private final LocalizedString oldDescription;

	/**
	 * Creates an operation for changing the name of the given annotation.
	 *  
	 * @param target
	 *            annotation for which the name should be changed
	 * @param newDescription
	 *            new internal name of the annotation.
	 * @throws IllegalArgumentException
	 *            if one of the parameters is null.
	 */
	public ChangeDescriptionOperation(INodeAnnotation target, String newDescription)
			throws IllegalArgumentException {
		super("description");
		if (target == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		Locale lang = Application.getCurrentLanguage();
		this.newDescription = new LocalizedString(newDescription, lang);
		this.oldDescription = new LocalizedString(target.getDescription(lang), lang);
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
	public ChangeDescriptionOperation(INodeAnnotation target, LocalizedString newDescription)
			throws IllegalArgumentException {
		super(Messages.ChangeTitleOperation_Label);
		if (target == null || newDescription == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		this.target = target;
		this.newDescription = newDescription;
		Locale lang = newDescription.getLanguage();
		this.oldDescription = new LocalizedString(target.getDescription(lang), lang);
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
	public ChangeDescriptionOperation(INodeAnnotation target, LocalizedString newDescription,
			String label) throws IllegalArgumentException {
		super(label);
		if (target == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		this.target = target;
		this.newDescription = newDescription;
		Locale lang = newDescription.getLanguage();
		this.oldDescription = new LocalizedString(target.getDescription(lang), lang);
	}

	@Override
	public boolean canExecute() {
		return target != null && newDescription != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return "Description change operation interrupted." + e.getMessage();
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

