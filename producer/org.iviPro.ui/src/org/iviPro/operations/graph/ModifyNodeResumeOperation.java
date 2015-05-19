package org.iviPro.operations.graph;

import java.util.Locale;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.graph.NodeResume;
import org.iviPro.operations.IAbstractOperation;

public class ModifyNodeResumeOperation  extends IAbstractOperation {
	private final NodeResume target;
	private final LocalizedString newTitle;
	private final LocalizedString oldTitle;
	private final int newTimeout;
	private final int oldTimeout;
	private final boolean newUseTimeout;
	private final boolean oldUseTimeout;
	

	/**
	 * @param target
	 * @param newTitle
	 * @throws IllegalArgumentException
	 */
	public ModifyNodeResumeOperation(NodeResume target, String newTitle,
			int newTimeout, boolean useTimeout)
			throws IllegalArgumentException {
		this(target, new LocalizedString(newTitle, Application
				.getCurrentLanguage()),	newTimeout, useTimeout);
	}

	/**
	 * Erstellt eine neue Operation zum Aendern des Titels eines Model-Objekts.
	 * Als Sprache für den Titel wird die aktuelle Projekt-Sprache verwendet.
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Titel geaendert werden soll.
	 * @param newTitle
	 *            Der neue Titel in der aktuellen Projekt-Sprache.
	 * @param newDefaultControl
	 *            Das neue Default-Control oder null, falls kein
	 *            Default-Control.
	 * @param newButtonType
	 *            Der neue Button-Typ.
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter (ausser newDefaultControl)
	 *             null ist.
	 */
	public ModifyNodeResumeOperation(NodeResume target,
			LocalizedString newTitle, int newTimeout, boolean useTimeout)
			throws IllegalArgumentException {
		super(Messages.ModifyNodeResumeOperation_Modify_Message);
		if (target == null || newTitle == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		Locale lang = Application.getCurrentLanguage();
		this.newTitle = newTitle;
		this.oldTitle = new LocalizedString(target.getTitle(lang), lang);
		this.newTimeout = newTimeout;
		this.oldTimeout = target.getTimeout();
		this.target = target;
		this.newUseTimeout = useTimeout;
		this.oldUseTimeout = target.useTimeout();
	}

	@Override
	public boolean canExecute() {
		return target != null && newTitle != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ModifyNodeResumeOperation_Modify_Error + e.getMessage();
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
		target.setTimeout(newTimeout);
		target.setUseTimeout(newUseTimeout);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setTitle(oldTitle);
		target.setTimeout(oldTimeout);
		target.setUseTimeout(oldUseTimeout);
		return Status.OK_STATUS;
	}
}