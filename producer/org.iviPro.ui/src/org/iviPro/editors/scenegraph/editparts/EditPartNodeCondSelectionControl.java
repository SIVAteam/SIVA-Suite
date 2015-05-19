package org.iviPro.editors.scenegraph.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.iviPro.editors.scenegraph.figures.FigureNodeCondSelectionControl;
import org.iviPro.editors.scenegraph.policies.DefaultConnectionPolicy;
import org.iviPro.editors.scenegraph.policies.DefaultEditPolicy;
import org.iviPro.editors.scenegraph.subeditors.NodeSelectionControlEditor;
import org.iviPro.model.graph.AbstractNodeSelectionControl;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeCondSelection;
import org.iviPro.model.graph.NodeCondSelectionControl;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.resources.Picture;

class EditPartNodeCondSelectionControl extends IEditPartNode implements PropertyChangeListener {

	
	NodeCondSelectionControl nodeCondSelectionControl;//Model
	FigureNodeCondSelectionControl figureStandard;
	
	@Override
	protected DefaultConnectionPolicy getNodeConnectionPolicy() {
		return new DefaultConnectionPolicy();
	}

	@Override
	protected DefaultEditPolicy getNodeEditPolicy() {
		return new DefaultEditPolicy(true);
	}

	@Override
	protected IFigure createFigure() {
		nodeCondSelectionControl = getCastedModel();
		figureStandard = new FigureNodeCondSelectionControl(
				nodeCondSelectionControl.getPosition(), nodeCondSelectionControl.getTitle(),
				nodeCondSelectionControl.getSemZoomlevel(),getCastedModel().getButtonImage());
				
		return figureStandard;
		
	}
	
	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			IGraphNode node = (IGraphNode) getModel();
			Graph graph = node.getGraph();
			graph.addPropertyChangeListener(this);
		}		
	}
	
	@Override
	public void onGraphParentAdded(IGraphNode node, IGraphNode newParent) {
		if (newParent instanceof NodeCondSelection) {
			getCastedModel().setRank(newParent.getChildren().size());
		}
		refreshVisuals();
		refreshTargetConnections();
	}
	
	@Override
	public void onGraphParentRemoved(IGraphNode node, IGraphNode oldParent) {
		/**
		 * If the NodeSelectionControl loses its parent NodeSelection, reset its
		 * rank and adapt the ranks of the other children of the NodeSelection.
		 */
		if (oldParent instanceof NodeCondSelection) {
			int rank = getCastedModel().getRank();
			getCastedModel().setRank(0);
			for (IGraphNode child : oldParent.getChildren()) {
				AbstractNodeSelectionControl nsc = (AbstractNodeSelectionControl)child;
				if (nsc.getRank() > rank) {
					nsc.setRank(nsc.getRank()-1);
				}
			}
		}	
		refreshVisuals();
		refreshTargetConnections();
	}

	@Override
	protected void refreshVisuals() {
		// notify parent container of changed position & location
		FigureNodeCondSelectionControl figure = getCastedFigure();
		figure.setZoom(nodeCondSelectionControl.getSemZoomlevel());
		
		Picture buttonImage = getCastedModel().getButtonImage();
		figure.setButtonImage(buttonImage);
	
		String text = (nodeCondSelectionControl.getRank() + ": " + getCastedModel().getTitle()).trim(); //$NON-NLS-1$
		figure.setText(text);

		figure.setLocation(getCastedModel().getPosition());
		Rectangle bounds = figure.getBounds();
		((GraphicalEditPart) getParent()).setLayoutConstraint(this, figure,	bounds);
	}

	private FigureNodeCondSelectionControl getCastedFigure() {
		return (FigureNodeCondSelectionControl) getFigure();
	}

	private NodeCondSelectionControl getCastedModel() { 
		return (NodeCondSelectionControl) getModel();
	}

	@Override
	public void onDoubleClick() {
		NodeCondSelectionControl control = getCastedModel();
		new NodeSelectionControlEditor(control).show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.iviPro.editors.scenegraph.gef.parts.IEditPartNode#isRenameable()
	 */
	@Override
	public boolean isRenameable() {
		return true;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(Graph.PROP_NODE_REMOVED)) {
			if (evt.getOldValue() instanceof NodeScene) {
				getCastedModel().getPrerequisiteScenes().remove(evt.getOldValue());
			}
		}
	}
}