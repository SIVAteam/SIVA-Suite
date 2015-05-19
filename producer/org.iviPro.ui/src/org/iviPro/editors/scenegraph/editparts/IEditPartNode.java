package org.iviPro.editors.scenegraph.editparts;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.requests.SelectionRequest;
import org.iviPro.editors.scenegraph.policies.DefaultConnectionPolicy;
import org.iviPro.editors.scenegraph.policies.DefaultEditPolicy;
import org.iviPro.editors.scenegraph.subeditors.TitledNodeEditor;
import org.iviPro.listeners.GraphNodeEventConsumer;
import org.iviPro.listeners.GraphNodeListener;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IConnection;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.operations.annotation.AnnotationSaveOperation;
import org.iviPro.operations.graph.NodeDeleteOperation;

/**
 * @author dellwo
 * 
 */
public abstract class IEditPartNode extends AbstractGraphicalEditPart implements
		NodeEditPart, GraphNodeEventConsumer {

	private static Logger logger = Logger.getLogger(IEditPartNode.class);

	private ConnectionAnchor anchor;

	private GraphNodeListener nodeListener;

	/**
	 * Gibt die Policy zurueck, die regelt, ob dieser Knoten geloescht werden
	 * darf.
	 * 
	 * @return Policy zum Loeschen dieses Knotens.
	 */
	protected abstract DefaultEditPolicy getNodeEditPolicy();

	/**
	 * Gibt die Policy zurueck, die regelt, mit welchen anderen Knoten dieser
	 * Knoten verbunden werden darf.
	 * 
	 * @return Policy zur Erstellung von Verbindungen zu anderen Knoten.
	 */
	protected abstract DefaultConnectionPolicy getNodeConnectionPolicy();

	/**
	 * Gibt an, ob der dem Edit-Part zugrunde liegende Graph-Knoten umbenannt
	 * werden kann. Ist der Knoten umbenennbar, wird beim Doppelklick
	 * standard-maessig ein Diaog zum Umbennen eingeblendet.
	 * 
	 * @return True, falls umbenennbar, false sonst.
	 */
	public abstract boolean isRenameable();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected abstract IFigure createFigure();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
	 */
	@Override
	protected abstract void refreshVisuals();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
	 */
	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			nodeListener = new GraphNodeListener(this);
			IGraphNode node = (IGraphNode) getModel();
			Graph graph = node.getGraph();
			nodeListener.startListening(node, graph);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
	 */
	@Override
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			nodeListener.stopListening();
		}
	}

	/**
	 * Überschrieben, um Doppelklicks auf die Edit-Parts abzufangen.
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#performRequest(org.eclipse
	 *      .gef.Request)
	 */
	@Override
	public void performRequest(Request req) {
		super.performRequest(req);
		if (req instanceof SelectionRequest && REQ_OPEN.equals(req.getType())) {
			logger.info("Detected double-click on: " + getModel()); //$NON-NLS-1$
			onDoubleClick();
		}
	}

	/**
	 * Wird aufgerufen, wenn auf die dem EditPart entsprechende Diagramm-Figure
	 * ein Doppelklick gemacht wurde. Ruft standardmaessig den Dialog zum
	 * Umbenennen des Knotens auf, wenn der Edit-Part umbenennbar ist.
	 * 
	 * @see org.iviPro.editors.scenegraph.editparts.IEditPartNode#isRenameable()
	 */
	public void onDoubleClick() {
		if (isRenameable()) {
			new TitledNodeEditor(getCastedModel()).show();
		}
	}

	/**
	 * Gibt das Model-Objekt bereits auf die IGraphNode-Klasse gecastet zurueck.
	 * 
	 * @return Auf IGraphNode-Klasse gecastetes Model-Objekt.
	 */
	private IGraphNode getCastedModel() {
		return (IGraphNode) getModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {

		// allow removal of the associated model element
		installEditPolicy(EditPolicy.COMPONENT_ROLE, getNodeEditPolicy());
		// allow the creation of connections and
		// and the reconnection of connections between node instances
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
				getNodeConnectionPolicy());
	}

	/**
	 * 
	 * @return
	 */
	protected ConnectionAnchor getConnectionAnchor() {
		if (anchor == null) {
			anchor = new ChopboxAnchor(getFigure());
		}
		return anchor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelSourceConnections
	 * ()
	 */
	@Override
	protected List getModelSourceConnections() {
		IGraphNode node = (IGraphNode) getModel();
		// Liefert alle Verbindungen wo der Knoten dieses Edit-Parts der
		// Start-Knoten ist, aber alle Verbindungen zu INodeAnnotationLeafs
		// und NodeMark werden dabei rausgefiltert.
		List<IConnection> filteredConns = new ArrayList<IConnection>();
		List<IConnection> allConns = node.getGraph().getConnectionsBySource(
				node);
		for (IConnection conn : allConns) {
			if (!(conn.getTarget() instanceof INodeAnnotationLeaf) &&
				!(conn.getTarget() instanceof NodeMark)) {
				filteredConns.add(conn);
			}
		}
		return filteredConns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelTargetConnections
	 * ()
	 */
	@Override
	protected List getModelTargetConnections() {
		IGraphNode node = (IGraphNode) getModel();
		return node.getGraph().getConnectionsByTarget(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef
	 * .ConnectionEditPart)
	 */
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connection) {
		return getConnectionAnchor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef
	 * .Request)
	 */
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return getConnectionAnchor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef
	 * .ConnectionEditPart)
	 */
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connection) {
		return getConnectionAnchor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef
	 * .Request)
	 */
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return getConnectionAnchor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.iviPro.listeners.GraphNodeEventConsumer#onGraphChildAdded(org.iviPro
	 * .model.graph.IGraphNode, org.iviPro.model.graph.IGraphNode)
	 */
	@Override
	public void onGraphChildAdded(IGraphNode node, IGraphNode newChild) {
		refreshVisuals();
		refreshSourceConnections();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.iviPro.listeners.GraphNodeEventConsumer#onGraphChildRemoved(org.iviPro
	 * .model.graph.IGraphNode, org.iviPro.model.graph.IGraphNode)
	 */
	@Override
	public void onGraphChildRemoved(IGraphNode node, IGraphNode oldChild) {
		refreshVisuals();
		refreshSourceConnections();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.iviPro.listeners.GraphNodeEventConsumer#onGraphNodePropertyChanged
	 * (org.iviPro.model.graph.IGraphNode, java.lang.String, java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public void onGraphNodePropertyChanged(IGraphNode node, String property,
			Object oldValue, Object newValue) {
		//Updaten der Fisheye-Darstellung falls nötig
		if(property.equals(AnnotationSaveOperation.PROP_ANNO_ADDED) || property.equals(NodeDeleteOperation.PROP_ANNO_DELETED) ) {
			((EditPartNodeScene)this).updateSemanticFisheyeFigure();
		}
		refreshVisuals();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.iviPro.listeners.GraphNodeEventConsumer#onGraphParentAdded(org.iviPro
	 * .model.graph.IGraphNode, org.iviPro.model.graph.IGraphNode)
	 */
	@Override
	public void onGraphParentAdded(IGraphNode node, IGraphNode newParent) {
		refreshVisuals();
		refreshTargetConnections();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.iviPro.listeners.GraphNodeEventConsumer#onGraphParentRemoved(org.
	 * iviPro.model.graph.IGraphNode, org.iviPro.model.graph.IGraphNode)
	 */
	@Override
	public void onGraphParentRemoved(IGraphNode node, IGraphNode oldParent) {
		refreshVisuals();
		refreshTargetConnections();
	}
}