package org.iviPro.operations.graph;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.iviPro.editors.scenegraph.layout.LayoutManager;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Verschieben der Position eines Knotens. Diese Operation
 * unterstuetzt Undo/Redo.
 * 
 * @author dellwo
 */
public class NodeMoveOperation extends IAbstractOperation {

	/** Neue Bounds des Knotens. */
	private final Rectangle newBounds;

	/**
	 * Der Request zum Verschieben/Resizen des Knotens. Wird derzeit nicht
	 * verwendet.
	 */
	@SuppressWarnings("unused")
	private final ChangeBoundsRequest request;

	/** Der Knoten der verschoben werden soll. */
	private final IGraphNode graphNode;

	/** Alte Bounds des Knotens. */
	private Rectangle oldBounds;

	/**
	 * Erstellt ein neues Kommando zum Verschieben/Resizen eines Knotens.
	 * 
	 * @param graphNode
	 *            Der Knoten der verschoben/resized werden soll.
	 * @param req
	 *            Der Request zum Verschieben/Resizen.
	 * @param newBounds
	 *            Die neuen Bounds des Knotens.
	 * @throws IllegalArgumentException
	 *             Falls einer der Parameter null sein sollte.
	 */
	public NodeMoveOperation(IGraphNode graphNode, ChangeBoundsRequest req,
			Rectangle newBounds) {
		super(Messages.NodeMoveOperation_UndoRedoLabel);
		if (graphNode == null || req == null || newBounds == null) {
			throw new IllegalArgumentException();
		}
		this.graphNode = graphNode;
		this.request = req;
		this.newBounds = newBounds.getCopy();

	}

	@Override
	public boolean canExecute() {
		return graphNode != null && newBounds != null;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		this.oldBounds = new Rectangle(graphNode.getPosition(), new Dimension(
				newBounds.width, newBounds.height));
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		// graphNode.setSize(newBounds.getSize());
		graphNode.setPosition(newBounds.getLocation());
		
		//Feuern eines PropertyChangeEvents für den Layoutmanager
		this.firePropChange(LayoutManager.PROP_NODE_MOVED, null, graphNode);
		
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		// graphNode.setSize(oldBounds.getSize());
		graphNode.setPosition(oldBounds.getLocation());
		return Status.OK_STATUS;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.NodeMoveOperation_ErrorMsg + e.getMessage();
	}

}
