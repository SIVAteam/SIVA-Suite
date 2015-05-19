package org.iviPro.editors.scenegraph.editparts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.iviPro.editors.scenegraph.figures.FigureNodeCondSelection;
import org.iviPro.editors.scenegraph.policies.DefaultConnectionPolicy;
import org.iviPro.editors.scenegraph.policies.DefaultEditPolicy;
import org.iviPro.editors.scenegraph.subeditors.NodeSelectionEditor;
import org.iviPro.model.graph.AbstractNodeSelection;
import org.iviPro.model.graph.NodeCondSelection;

class EditPartNodeCondSelection extends IEditPartNode {

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
		IFigure f = new FigureNodeCondSelection(getCastedModel().getPosition(),
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

	private NodeCondSelection getCastedModel() {
		return (NodeCondSelection) getModel();
	}

	private FigureNodeCondSelection getCastedFigure() {
		return (FigureNodeCondSelection) getFigure();
	}

	@Override
	public void onDoubleClick() {
		AbstractNodeSelection selection = (AbstractNodeSelection)getModel();
		new NodeSelectionEditor(selection).show();
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