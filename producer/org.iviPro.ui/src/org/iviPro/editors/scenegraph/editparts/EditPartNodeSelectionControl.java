package org.iviPro.editors.scenegraph.editparts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.iviPro.editors.scenegraph.figures.FigureNodeSelectionControl;
import org.iviPro.editors.scenegraph.policies.DefaultConnectionPolicy;
import org.iviPro.editors.scenegraph.policies.DefaultEditPolicy;
import org.iviPro.editors.scenegraph.subeditors.NodeSelectionControlEditor;
import org.iviPro.model.graph.AbstractNodeSelection;
import org.iviPro.model.graph.AbstractNodeSelectionControl;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeSelectionControl;
import org.iviPro.model.resources.Picture;

class EditPartNodeSelectionControl extends IEditPartNode {

	
	NodeSelectionControl nodeSelectionControl;//Model
	FigureNodeSelectionControl figureStandard;
	
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
		nodeSelectionControl = getCastedModel();
		figureStandard = new FigureNodeSelectionControl(
				nodeSelectionControl.getPosition(), nodeSelectionControl.getTitle(),
				nodeSelectionControl.getSemZoomlevel(),getCastedModel().getButtonImage());
				
		return figureStandard;
		
	}
	
	@Override
	public void onGraphParentAdded(IGraphNode node, IGraphNode newParent) {
		if (newParent instanceof AbstractNodeSelection) {
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
		if (oldParent instanceof AbstractNodeSelection) {
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
	};

	@Override
	protected void refreshVisuals() {
		// notify parent container of changed position & location
		FigureNodeSelectionControl figure = getCastedFigure();
		figure.setZoom(nodeSelectionControl.getSemZoomlevel());
		
		Picture buttonImage = getCastedModel().getButtonImage();
		figure.setButtonImage(buttonImage);
	
		String text = (nodeSelectionControl.getRank() + ": " + getCastedModel().getTitle()).trim(); //$NON-NLS-1$
		figure.setText(text);

		figure.setLocation(getCastedModel().getPosition());
		Rectangle bounds = figure.getBounds();
		((GraphicalEditPart) getParent()).setLayoutConstraint(this, figure,	bounds);
	}

	private FigureNodeSelectionControl getCastedFigure() { //TEST from figurenodeslesctioncontrol to ifigurenode
		return (FigureNodeSelectionControl) getFigure();
	}

	private NodeSelectionControl getCastedModel() { 
		return (NodeSelectionControl) getModel();
	}

	@Override
	public void onDoubleClick() {
		NodeSelectionControl control = getCastedModel();
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

}