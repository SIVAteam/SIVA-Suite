package org.iviPro.editors.scenegraph.editparts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.iviPro.editors.scenegraph.figures.FigureNodeQuizControl;
import org.iviPro.editors.scenegraph.policies.DefaultConnectionPolicy;
import org.iviPro.editors.scenegraph.policies.DefaultEditPolicy;
import org.iviPro.editors.scenegraph.subeditors.NodeQuizControlEditor;
import org.iviPro.model.graph.NodeQuizControl;

class EditPartNodeQuizControl extends IEditPartNode {

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
		NodeQuizControl nodeQuizControl = getCastedModel();
		FigureNodeQuizControl f = new FigureNodeQuizControl(
				nodeQuizControl.getPosition(), nodeQuizControl
						.getTitle());
		return f;
	}

	@Override
	protected void refreshVisuals() {
		getFigure().setLocation(getCastedModel().getPosition());
		getCastedFigure().setText(getCastedModel().getMinValue()+"-"+getCastedModel().getMaxValue()); //$NON-NLS-1$
		Rectangle bounds = getFigure().getBounds();
		((GraphicalEditPart) getParent()).setLayoutConstraint(this,
				getFigure(), bounds);
		// notify parent container of changed position & location
//		FigureNodeQuizControl figure = getCastedFigure();
//		Picture buttonImage = getCastedModel().getButtonImage();
//		String text = ("" + getCastedModel().getTitle()).trim(); //$NON-NLS-1$
//		if (buttonImage != null) {
//			if (text.length() > 0) {
//				text += " / "; //$NON-NLS-1$
//			}
//			text += buttonImage.getTitle();
//		}
//		figure.setText(text);
//
//		figure.setLocation(getCastedModel().getPosition());
//		Rectangle bounds = figure.getBounds();
//		((GraphicalEditPart) getParent()).setLayoutConstraint(this, figure,
//				bounds);
	}

	private FigureNodeQuizControl getCastedFigure() {
		return (FigureNodeQuizControl) getFigure();
	}

	private NodeQuizControl getCastedModel() {
		return (NodeQuizControl) getModel();
	}

	@Override
	public void onDoubleClick() {
		NodeQuizControl control = getCastedModel();
		new NodeQuizControlEditor(control).show();
		refreshVisuals();
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