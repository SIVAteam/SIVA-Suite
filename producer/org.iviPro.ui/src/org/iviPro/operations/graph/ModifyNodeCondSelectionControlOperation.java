package org.iviPro.operations.graph;

import java.util.List;
import java.util.Locale;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.graph.NodeCondSelectionControl;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Aendern der Daten eines Selektions-Knotens.
 * 
 * @author dellwo
 * 
 */
public class ModifyNodeCondSelectionControlOperation extends IAbstractOperation {

	private final NodeCondSelectionControl target;
	private final boolean newIsVisible;
	private final boolean oldIsVisible;
	private final LocalizedString newDesc;
	private final LocalizedString oldDesc;
	private final List<NodeScene> newPrerequisiteScenes;
	private final List<NodeScene> oldPrerequisiteScenes;

	/**
	 * Constructs a modification operation for the conditional part of a 
	 * <code>NodeCondSelectionControl</code>. 
	 * 
	 * @param target
	 *            <code>NodeCondSelectionControl</code> to be modified 
	 * @param newIsVisible whether or not control should be visible when
	 * conditions are not met
	 * @param newDesc description of condition
	 * @param newPrerequisiteScenes list of scenes which need to be visited first
	 * @throws IllegalArgumentException if arguments are null
	 */
	public ModifyNodeCondSelectionControlOperation(NodeCondSelectionControl target,
			boolean newIsVisible, String newDesc,
			List<NodeScene> newPrerequisiteScenes)
					throws IllegalArgumentException {
		this(target, newIsVisible, new LocalizedString(newDesc, 
				Application.getCurrentLanguage()), newPrerequisiteScenes);
	}
	
	/**
	 * Constructs a modification operation for the conditional part of a 
	 * <code>NodeCondSelectionControl</code>. 
	 * 
	 * @param target
	 *            <code>NodeCondSelectionControl</code> to be modified 
	 * @param newIsVisible whether or not control should be visible when
	 * conditions are not met
	 * @param newDesc description of condition
	 * @param newPrerequisiteScenes list of scenes which need to be visited first
	 * @throws IllegalArgumentException if arguments are null
	 */
	public ModifyNodeCondSelectionControlOperation(NodeCondSelectionControl target,
			boolean newIsVisible, LocalizedString newDesc,
			List<NodeScene> newPrerequisiteScenes)
			throws IllegalArgumentException {
		super(Messages.ModifyNodeCondSelectionControlOperation_Title);
		if (target == null || newDesc == null || newPrerequisiteScenes == null) {
			throw new IllegalArgumentException(
					"Arguments must not be null."); //$NON-NLS-1$
		}
		Locale lang = Application.getCurrentLanguage();
		this.target = target;
		this.newIsVisible = newIsVisible;
		this.oldIsVisible = target.isVisible();
		this.newDesc = newDesc;
		this.oldDesc = new LocalizedString(target.getDescription(lang), lang);
		this.newPrerequisiteScenes = newPrerequisiteScenes;
		this.oldPrerequisiteScenes = target.getPrerequisiteScenes();
	}

	@Override
	public boolean canExecute() {
		return target != null && newDesc != null && newPrerequisiteScenes != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ModifyNodeCondSelectionControlOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setVisibile(newIsVisible);
		target.setDescription(newDesc);
		target.setPrequisiteScenes(newPrerequisiteScenes);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setVisibile(oldIsVisible);
		target.setDescription(oldDesc);
		target.setPrequisiteScenes(oldPrerequisiteScenes);
		return Status.OK_STATUS;
	}
}
