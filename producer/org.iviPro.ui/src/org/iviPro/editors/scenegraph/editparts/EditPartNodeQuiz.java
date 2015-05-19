package org.iviPro.editors.scenegraph.editparts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.iviPro.editors.scenegraph.figures.FigureNodeQuiz;
import org.iviPro.editors.scenegraph.policies.DefaultConnectionPolicy;
import org.iviPro.editors.scenegraph.policies.DefaultEditPolicy;
import org.iviPro.editors.scenegraph.subeditors.NodeQuizEditor;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.Test;

class EditPartNodeQuiz extends IEditPartNode {

	public EditPartNodeQuiz() {
	}

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
		IFigure f = new FigureNodeQuiz(getCastedModel().getPosition(),
				getCastedModel().getTitle());
		return f;
	}

	@Override
	protected void refreshVisuals() {
		// notify parent container of changed position & location
		getFigure().setLocation(getCastedModel().getPosition());
		// try to get the selected quiz via id
		Test activeTest = DbQueries.getTestData(getCastedModel().getTestId());
		if (activeTest == null) {
			// set the text of the quiz node to the title of the selected test
			getCastedFigure().setText(getCastedModel().getTitle());
		} else {
			// use default title
			getCastedFigure().setText(activeTest.getTitle());
		}
		Rectangle bounds = getFigure().getBounds();
		((GraphicalEditPart) getParent()).setLayoutConstraint(this,
				getFigure(), bounds);

	}

	private NodeQuiz getCastedModel() {
		return (NodeQuiz) getModel();
	}

	private FigureNodeQuiz getCastedFigure() {
		return (FigureNodeQuiz) getFigure();
	}

	@Override
	public void onDoubleClick() {
		NodeQuiz quiz = getCastedModel();
		new NodeQuizEditor(quiz).show();
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