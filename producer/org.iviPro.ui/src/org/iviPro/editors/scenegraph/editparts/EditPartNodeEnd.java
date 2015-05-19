package org.iviPro.editors.scenegraph.editparts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.iviPro.editors.scenegraph.figures.FigureNodeEnd;
import org.iviPro.editors.scenegraph.policies.DefaultConnectionPolicy;
import org.iviPro.editors.scenegraph.policies.DefaultEditPolicy;
import org.iviPro.model.graph.NodeEnd;

class EditPartNodeEnd extends IEditPartNode {

	@Override
	protected DefaultConnectionPolicy getNodeConnectionPolicy() {
		return new DefaultConnectionPolicy();
	}

	@Override
	protected DefaultEditPolicy getNodeEditPolicy() {
		return new DefaultEditPolicy(false);
	}

	@Override
	protected IFigure createFigure() {
		IFigure f = new FigureNodeEnd(getCastedModel().getPosition(),
				getCastedModel().getTitle());
		return f;
	}

	@Override
	protected void refreshVisuals() {
		// notify parent container of changed position & location
		getFigure().setLocation(getCastedModel().getPosition());
		getCastedFigure().setText(getCastedModel().getTitle());
		Rectangle bounds = getFigure().getBounds();
		((GraphicalEditPart) getParent()).setLayoutConstraint(this,
				getFigure(), bounds);

	}

	private NodeEnd getCastedModel() {
		return (NodeEnd) getModel();
	}
	
	private FigureNodeEnd getCastedFigure() {
		return (FigureNodeEnd) getFigure();
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