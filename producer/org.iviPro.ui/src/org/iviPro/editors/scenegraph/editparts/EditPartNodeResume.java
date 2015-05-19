package org.iviPro.editors.scenegraph.editparts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.iviPro.editors.scenegraph.figures.FigureNodeResume;
import org.iviPro.editors.scenegraph.policies.DefaultConnectionPolicy;
import org.iviPro.editors.scenegraph.policies.DefaultEditPolicy;
import org.iviPro.editors.scenegraph.subeditors.NodeResumeEditor;
import org.iviPro.model.graph.NodeResume;

public class EditPartNodeResume extends IEditPartNode {

	@Override
	protected DefaultEditPolicy getNodeEditPolicy() {
		return new DefaultEditPolicy(true);
	}

	@Override
	protected DefaultConnectionPolicy getNodeConnectionPolicy() {
		return new DefaultConnectionPolicy();
	}

	@Override
	public boolean isRenameable() {
		return false;
	}

	@Override
	protected IFigure createFigure() {
		IFigure f = new FigureNodeResume(getCastedModel().getPosition(),
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
	
	private NodeResume getCastedModel() {
		return (NodeResume) getModel();
	}

	private FigureNodeResume getCastedFigure() {
		return (FigureNodeResume) getFigure();
	}
	
	@Override
	public void onDoubleClick() {
		NodeResume resume = getCastedModel();
		new NodeResumeEditor(resume).show();
	}
}