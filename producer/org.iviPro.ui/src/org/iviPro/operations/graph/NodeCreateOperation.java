package org.iviPro.operations.graph;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.geometry.Rectangle;
import org.iviPro.editors.scenegraph.layout.LayoutManager;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Erstellen eines neuen Knotens in einem Szenen-Graph.
 * 
 * @author dellwo
 */
public class NodeCreateOperation extends IAbstractOperation {

	/** Der neue Knoten. */
	private final IGraphNode newNode;
	/** Der Graph zu dem der Knoten hinzugefuegt werden soll. */
	private final Graph graph;
	/** Die Position und Groesse des neuen Knotens. */
	private final Rectangle bounds;

	/**
	 * Erstellt eine neue Operation zum Hinzufuegen eines gegebenen Knotens zu
	 * einem gegebenen Graphen.
	 * 
	 * @param newNode
	 *            Der neue Knoten, der hinzugefuegt werden soll.
	 * @param graph
	 *            Der Graph zu dem der Knoten hinzugefuegt weren soll.
	 * @param bounds
	 *            Die Bounding-Box, die Position und Groesse des neuen Knotens
	 *            definiert. Die Groesse kann (-1,-1) sein, wenn nicht bekannt.
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter null ist.
	 */
	public NodeCreateOperation(IGraphNode newNode, Graph graph, Rectangle bounds)
			throws IllegalArgumentException {
		super(Messages.NodeCreateOperation_Label);
		if (newNode == null || graph == null || bounds == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		this.newNode = newNode;
		this.newNode.setPosition(bounds.getTopRight());
		this.graph = graph;
		this.bounds = bounds;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public boolean canExecute() {
		return newNode != null && graph != null && bounds != null;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		// graphNode.setSize(newBounds.getSize());
		graph.addNode(newNode);

		this.firePropChange(LayoutManager.PROP_NODE_CREATED, null, newNode);
		
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		graph.removeNode(newNode);
		return Status.OK_STATUS;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.NodeCreateOperation_ErrorMsg + e.getMessage();
	}

}