package org.iviPro.operations.graph;

import java.util.Locale;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.graph.NodeQuizControl;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Aendern der Daten eines Selektions-Knotens.
 * 
 * @author zwicklbauer
 * 
 */

public class ModifyNodeQuizControlOperation extends IAbstractOperation {

	private final NodeQuizControl target;
	private final LocalizedString newAmount;
	private final LocalizedString oldAmount;

	/**
	 * @param target
	 * @throws IllegalArgumentException
	 */
	public ModifyNodeQuizControlOperation(NodeQuizControl target,
			String newAmount) throws IllegalArgumentException {
		this(target, new LocalizedString(newAmount,
				Application.getCurrentLanguage()));
	}

	/**
	 * Erstellt eine neue Operation zum Aendern des Titels eines Model-Objekts.
	 * Als Sprache für den Titel wird die aktuelle Projekt-Sprache verwendet.
	 * Zusätzlich wird die aktuelle Punktezahl des Fensters gespeichert.
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Titel geaendert werden soll.
	 * @param newAmount
	 *            Der neue Titel in der aktuellen Projekt-Sprache.
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter (ausser newDefaultControl)
	 *             null ist.
	 */
	public ModifyNodeQuizControlOperation(NodeQuizControl target,
			LocalizedString newAmount) throws IllegalArgumentException {
		super(Messages.ModifyAbstractNodeSelectionControlOperation_Title);
		if (target == null || newAmount == null) {
			throw new IllegalArgumentException(
					"Target and title resp. amount must not be null."); //$NON-NLS-1$
		}
		Locale lang = Application.getCurrentLanguage();
		//this.oldTitle = new LocalizedString(target.getTitle(lang), lang);
		this.newAmount = newAmount;
		this.oldAmount = new LocalizedString(target.getMinValue()+"-"+target.getMaxValue(), lang);
		System.out.println("Wird aber gespeichert!");
		this.target = target;
	}

	@Override
	public boolean canExecute() {
		return target != null && newAmount != null;
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
		String[] split = newAmount.getValue().split("-"); 
		System.out.println(newAmount.getValue());
		try {
			target.setMinValue(Integer.parseInt(split[0]));
			target.setMaxValue(Integer.parseInt(split[1]));
		} catch (NumberFormatException e) {
			System.out.println("kaputt da");
		}
		//target.setAmountPoints(newAmount);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		String[] split = oldAmount.getValue().split("-");
		try {
			target.setMinValue(Integer.parseInt(split[0]));
			target.setMaxValue(Integer.parseInt(split[1]));
		} catch (NumberFormatException e) {
			System.out.println("kaputt hier");
		}
		//target.setAmountPoints(oldAmount);
		return Status.OK_STATUS;
	}

}
