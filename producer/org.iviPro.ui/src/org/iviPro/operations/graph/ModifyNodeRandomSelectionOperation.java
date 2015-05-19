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
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeRandomSelection;
import org.iviPro.operations.IAbstractOperation;

public class ModifyNodeRandomSelectionOperation extends IAbstractOperation {

	private final NodeRandomSelection target;
	private final LocalizedString newTitle;
	private final LocalizedString oldTitle;
	private final HashMap<IGraphNode, Integer> newProbabilityMap;
	private final HashMap<IGraphNode, Integer> oldProbabilityMap;
	private final boolean newUseEqualProbability;
	private final boolean oldUseEqualProbability;

	

	/**
	 * @param target
	 * @param newTitle
	 * @throws IllegalArgumentException
	 */
	public ModifyNodeRandomSelectionOperation(NodeRandomSelection target,
			String newTitle, boolean useEqualProbability,
			HashMap<IGraphNode, Integer> newProbabilityMap)
			throws IllegalArgumentException {
		this(target, new LocalizedString(newTitle, Application
				.getCurrentLanguage()), useEqualProbability, newProbabilityMap);
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
	public ModifyNodeRandomSelectionOperation(NodeRandomSelection target,
			LocalizedString newTitle, boolean useEqualProbability,
			HashMap<IGraphNode, Integer> newProbabilityMap)
			throws IllegalArgumentException {
		super(Messages.ModifyNodeRandomSelectionOperation_Modification_Title);
		if (target == null || newTitle == null) {
			throw new IllegalArgumentException(
					"Some parameters may not be null."); //$NON-NLS-1$
		}
		Locale lang = Application.getCurrentLanguage();
		this.newTitle = newTitle;
		this.oldTitle = new LocalizedString(target.getTitle(lang), lang);
		this.newProbabilityMap = newProbabilityMap;
		this.oldProbabilityMap = target.getProbabilityMap();
		this.newUseEqualProbability = useEqualProbability;
		this.oldUseEqualProbability = target.useEqualProbability();
		this.target = target;
	}

	@Override
	public boolean canExecute() {
		return target != null && newTitle != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ModifyNodeRandomSelectionOperation_Error_Message + e.getMessage();
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
		target.setEqualProbability(newUseEqualProbability);
		target.setProbabilityMap(newProbabilityMap);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setTitle(oldTitle);
		target.setEqualProbability(oldUseEqualProbability);
		target.setProbabilityMap(oldProbabilityMap);
		return Status.OK_STATUS;
	}

}