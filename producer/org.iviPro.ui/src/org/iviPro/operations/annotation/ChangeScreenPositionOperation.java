package org.iviPro.operations.annotation;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.model.annotation.OverlayPathItem;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.ScreenArea;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Aendern der Zeit von Szenne und Annotationen.
 * 
 * @author hoffmanj
 * 
 */
public class ChangeScreenPositionOperation extends IAbstractOperation {

	private final INodeAnnotation target;
	private final LinkedList<OverlayPathItem> oldItems;
	private final LinkedList<OverlayPathItem> newItems;
	private final ScreenArea newArea;
	private final ScreenArea oldArea;
	
	/**
	 * Erstellt eine neue Operation zum Aendern der Position eines Model-Objekts (Szenen und Annotationen)
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Titel geaendert werden soll.
	 * @param newTime
	 *            Die neuen Keywords
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter null ist.
	 */
	public ChangeScreenPositionOperation(INodeAnnotation target, ScreenArea newArea, List<OverlayPathItem> opItems)
			throws IllegalArgumentException {
		super(Messages.ChangeScreenPositionOperation_Label);
		if (target == null || opItems == null || newArea == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		
		this.newArea = newArea;
		this.oldArea = target.getScreenArea();		
		
		newItems = new LinkedList<OverlayPathItem>();
		oldItems = new LinkedList<OverlayPathItem>();
		if (opItems.size() > 0) {
			for (OverlayPathItem item : opItems) {			
				newItems.add(item.clone());
			}
		} else {
			long time = 0;
			if (target.getStart() != null) {
				time = target.getStart();
			}
			OverlayPathItem ovp = new OverlayPathItem(0.1f, 0.1f, 0.3f, 0.3f, time, Application.getCurrentProject());
			newItems.add(ovp);			
		}
		for (OverlayPathItem item : target.getOverlayPath()) {
			oldItems.add(item.clone());		
		}
		this.target = target;
	}

	@Override
	public boolean canExecute() {
		return target != null && newArea != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ChangeScreenPositionOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setScreenArea(newArea);
		target.getOverlayPath().clear();
		for (OverlayPathItem item : newItems) {
			target.addOverlayPathItem(item);
		}
		return Status.OK_STATUS;		
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setScreenArea(oldArea);
		target.getOverlayPath().clear();
		for (OverlayPathItem item : oldItems) {
			target.addOverlayPathItem(item);
		}
		return Status.OK_STATUS;
	}

}
