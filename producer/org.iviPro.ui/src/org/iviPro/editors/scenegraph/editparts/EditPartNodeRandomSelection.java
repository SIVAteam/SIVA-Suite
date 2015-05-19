package org.iviPro.editors.scenegraph.editparts;

import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.iviPro.editors.scenegraph.figures.FigureNodeRandomSelection;
import org.iviPro.editors.scenegraph.policies.DefaultConnectionPolicy;
import org.iviPro.editors.scenegraph.policies.DefaultEditPolicy;
import org.iviPro.editors.scenegraph.subeditors.NodeRandomSelectionEditor;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeRandomSelection;

public class EditPartNodeRandomSelection extends IEditPartNode {

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
		return true;
	}

	@Override
	protected IFigure createFigure() {
		IFigure f = new FigureNodeRandomSelection(getCastedModel().getPosition(),
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
	
	private NodeRandomSelection getCastedModel() {
		return (NodeRandomSelection) getModel();
	}

	private FigureNodeRandomSelection getCastedFigure() {
		return (FigureNodeRandomSelection) getFigure();
	}
	
	@Override
	public void onDoubleClick() {
		NodeRandomSelection randomSelection = getCastedModel();
		new NodeRandomSelectionEditor(randomSelection).show();
	}
	
	@Override
	public void onGraphChildAdded(IGraphNode node, IGraphNode newChild) {
		NodeRandomSelection randomSelection = getCastedModel();
		if (!randomSelection.useEqualProbability()) {
			randomSelection.getProbabilityMap().put(newChild, 0);
		}
		super.onGraphChildAdded(node, newChild);
	}
	
	@Override
	public void onGraphChildRemoved(IGraphNode node, IGraphNode oldChild) {
		NodeRandomSelection randomSelection = getCastedModel();
		if (randomSelection.getChildren().isEmpty()) {
			randomSelection.setEqualProbability(true);
			randomSelection.setProbabilityMap(null);
		} else if (!randomSelection.useEqualProbability()) {
			Map<IGraphNode, Integer> map = randomSelection.getProbabilityMap();
			int deletedProb = map.get(oldChild);
			map.remove(oldChild);
			double share = (double)deletedProb / map.keySet().size();
			boolean roundUpOnce = true;
			for (IGraphNode child : map.keySet()) {
				int oldValue = map.get(child);
				if (roundUpOnce) {
					map.put(child, oldValue + (int)Math.ceil(share));
					roundUpOnce = false;
				} else {
					map.put(child, oldValue + (int)Math.floor(share));
				}
			}
		}
		super.onGraphChildRemoved(node, oldChild);
	}

}
