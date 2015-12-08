package org.iviPro.operations.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.editors.scenegraph.editparts.IEditPartNode;
import org.iviPro.editors.scenegraph.editparts.PartFactory;
import org.iviPro.editors.scenegraph.layout.ElementChangeReport;
import org.iviPro.editors.scenegraph.layout.LayoutManager;
import org.iviPro.model.TocItem;
import org.iviPro.model.graph.DependentConnection;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IConnection;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Loeschen eines Knotens aus einem Graphen. Dabei werden die
 * Verbindungen des Knotens mitgeloescht.
 */
public class NodeDeleteOperation extends IAbstractOperation {

	//Identifikator fuer PropertyChangeEvent
	public static final String PROP_ANNO_DELETED = "annotationDeleted"; //$NON-NLS-1$

	/** Graph node to remove. */
	private final IGraphNode node;
	
	/** Der Graph in dem der Knoten sich befindet. */
	private final Graph graph;

	/** Set of nodes which have been deleted */
	private HashSet<IGraphNode> deletedNodes;
	
	/** Holds a copy of the outgoing connections of child. */
	private HashSet<IConnection> deletedConnections;
	
	/** List of TOC entries where a reference had to be deleted */
	private List<TocItem> affectedTocEntries;

	/** True, if child was removed from its parent. */
	private boolean wasRemoved;

	/**
	 * Erstellt ein neues Kommando zum Loeschen eines Knotens.
	 * 
	 * @param node
	 *            Der zu loeschende Knoten.
	 */
	public NodeDeleteOperation(IGraphNode node) {
		super(Messages.NodeDeleteOperation_UndoRedoLabel);
		if (node == null) {
			throw new IllegalArgumentException();
		}
		this.node = node;
		this.graph = node.getGraph();
		affectedTocEntries = new ArrayList<TocItem>();
		deletedNodes = new HashSet<IGraphNode>();
		deletedConnections = new HashSet<IConnection>();
	}

	@Override
	public boolean canUndo() {
		return wasRemoved;
	}

	@Override
	public boolean canExecute() {
		return node != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.NodeDeleteOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (node instanceof INodeAnnotationLeaf || node instanceof NodeMark) {
			setLabel(Messages.NodeDeleteOperation_Label);
		}
		
		// Collect dependent nodes which have to be deleted
		deletedNodes.add(node);
		graph.searchDependentNodes(node, deletedNodes);
		
		// Collect all connections which will be deleted
		for (IGraphNode node : deletedNodes) {
		deletedConnections.addAll(graph.getConnectionsBySource(node));
		deletedConnections.addAll(graph.getConnectionsByTarget(node));
		}
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		IEditPartNode editPartNode = null;
		int oldWidth = 0;
		int oldHeight = 0;
		NodeScene scene = null;
		
		// Get editpart sizes of scene if annotation is deleted
		if (node instanceof NodeMark || (node instanceof INodeAnnotationLeaf 
				&& !((INodeAnnotationLeaf)node).isTriggerAnnotation())) {
			scene = ((INodeAnnotation)node).getParentScene();			
			editPartNode = PartFactory.getReferingEditPart(scene);
			oldWidth = editPartNode.getFigure().getBounds().width;
			oldHeight = editPartNode.getFigure().getBounds().height;
		}
				
		// Remove all nodes (connections are removed during node deletion)
		for (IGraphNode n : deletedNodes) {
			graph.removeNode(n);
		}
		wasRemoved = true;
		
		// Tweak those fishy figures of NodeScenes after deleting an annotation
		if (scene != null) {
			//PropertyChange zum Refresh der Figure
			//(benötigt wenn Semantic Fisheye aktiv und Annotation gelöscht wurde)
			node.firePropertyChange(PROP_ANNO_DELETED, null, node);

			//hole neue größe
			int newWidth = editPartNode.getFigure().getBounds().width;
			int newHeight = editPartNode.getFigure().getBounds().height;
			//erzeuge changereport für layoutmanager
			ElementChangeReport ecp = new ElementChangeReport(editPartNode,newWidth-oldWidth,newHeight-oldHeight);
			List<ElementChangeReport> editPartSizeChangedList = new LinkedList<ElementChangeReport>();
			editPartSizeChangedList.add(ecp);

			//PropertyChange für den Layoutmanager
			this.firePropChange(LayoutManager.PROP_DELETED_ANNOTATION, null, editPartSizeChangedList);
		}
		
		if (node instanceof NodeScene) {
			// Update table of contents deleting references to the NodeScene
			TocItem root = Application.getCurrentProject().getTableOfContents();
			checkTocSubtree(root);
		}
		return Status.OK_STATUS;
	}
	
	/**
	 * Checks for the table of contents subtree rooted at the given TocItem if
	 * there are references to the deleted node and removes them.
	 * @param parent root of the subtree which should be checked
	 */
	private void checkTocSubtree(TocItem parent) {
		for (TocItem child : parent.getChildren()) {
			if (node.equals(child.getScene())) {
				affectedTocEntries.add(child);
				child.setScene(null);
			}
			checkTocSubtree(child);
		}	
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		// Add deleted nodes
		graph.addNode(node);
		for (IGraphNode n : deletedNodes) {
			graph.addNode(n);
		}
		// Add deleted connections
		for (IConnection conn : deletedConnections) {
			graph.addConnection(conn);
		}		
		
		// Undo changes to the table of contents 
		if (!affectedTocEntries.isEmpty()) {
			for (TocItem item : affectedTocEntries) {
				item.setScene((NodeScene)node);
			}
		}
		return Status.OK_STATUS;
	}
}