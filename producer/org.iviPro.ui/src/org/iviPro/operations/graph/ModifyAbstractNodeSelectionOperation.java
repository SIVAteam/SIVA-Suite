package org.iviPro.operations.graph;

import java.util.HashMap;
import java.util.Locale;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.graph.AbstractNodeSelection;
import org.iviPro.model.graph.AbstractNodeSelectionControl;
import org.iviPro.model.graph.ButtonType;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation for changing the properties of an 
 * <code>AbstractNodeSelection</code>.
 * 
 */
public class ModifyAbstractNodeSelectionOperation extends IAbstractOperation {

	private final AbstractNodeSelection target;
	private final LocalizedString newTitle;
	private final LocalizedString oldTitle;
	private final HashMap<Integer, AbstractNodeSelectionControl> newControlRanks;
	private final HashMap<Integer, AbstractNodeSelectionControl> oldControlRanks;
	private final AbstractNodeSelectionControl oldDefaultControl;
	private final AbstractNodeSelectionControl newDefaultControl;
	private final ButtonType oldButtonType;
	private final ButtonType newButtonType;
	private final int newTimeout;
	private final int oldTimeout;
	private final boolean newUseStandardPath;
	private final boolean oldUseStandardPath;

	/**
	 * @param target
	 * @param newTitle
	 * @throws IllegalArgumentException
	 */
	public ModifyAbstractNodeSelectionOperation(AbstractNodeSelection target,
			String newTitle,
			HashMap<Integer, AbstractNodeSelectionControl> newControlRanks,
			AbstractNodeSelectionControl newDefaultControl,
			ButtonType newButtonType, int newTimeout, boolean useStandardPath)
			throws IllegalArgumentException {
		this(target, new LocalizedString(newTitle, Application
				.getCurrentLanguage()), newControlRanks, newDefaultControl, 
				newButtonType,	newTimeout, useStandardPath);
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
	public ModifyAbstractNodeSelectionOperation(AbstractNodeSelection target,
			LocalizedString newTitle,
			HashMap<Integer, AbstractNodeSelectionControl> newControlRanks,
			AbstractNodeSelectionControl newDefaultControl,	
			ButtonType newButtonType, int newTimeout, boolean useStandardPath)
			throws IllegalArgumentException {
		super(Messages.ModifyNodeSelectionOperation_Title);
		if (target == null || newTitle == null || newButtonType == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		Locale lang = Application.getCurrentLanguage();
		this.newTitle = newTitle;
		this.oldTitle = new LocalizedString(target.getTitle(lang), lang);
		this.newControlRanks = newControlRanks;
		this.oldControlRanks =
				new HashMap<Integer,
					AbstractNodeSelectionControl>(newControlRanks.size(), 1);
		for (AbstractNodeSelectionControl control : newControlRanks.values()) {
			oldControlRanks.put(control.getRank(), control);
		}
		this.newDefaultControl = newDefaultControl;
		this.oldDefaultControl = target.getDefaultControl();
		this.newButtonType = newButtonType;
		this.oldButtonType = target.getButtonType();
		this.newTimeout = newTimeout;
		this.oldTimeout = target.getTimeout();
		this.target = target;
		this.newUseStandardPath = useStandardPath;
		this.oldUseStandardPath = target.getUseStandardPath();
	}

	@Override
	public boolean canExecute() {
		return target != null && newTitle != null && newButtonType != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ModifyNodeSelectionOperation_ErrorMsg + e.getMessage();
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
		target.setButtonType(newButtonType);
		target.setDefaultControl(newDefaultControl);
		target.setUseStandardPath(newUseStandardPath);
		for (int i=1; i<=newControlRanks.size(); i++) {
			newControlRanks.get(i).setRank(i);
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setTitle(oldTitle);
		target.setTimeout(oldTimeout);
		target.setButtonType(oldButtonType);
		target.setDefaultControl(oldDefaultControl);
		target.setUseStandardPath(oldUseStandardPath);
		for (int i=1; i<=oldControlRanks.size(); i++) {
			oldControlRanks.get(i).setRank(i);
		}
		return Status.OK_STATUS;
	}

}
