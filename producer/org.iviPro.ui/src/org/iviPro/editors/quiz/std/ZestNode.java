package org.iviPro.editors.quiz.std;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.IContainer;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.Node;

/**
 * Hilfs-Klasse um Knoten-Eigenschaften im Zest-Graphen darzustellen.
 * 
 * @author Sabine Gattermann
 * 
 */
public class ZestNode extends GraphNode {

	private Node node;

	/**
	 * Konstruktor
	 * 
	 * @param graphModel
	 *            Der Graph.
	 * @param style
	 *            Der Sytyle.
	 * @param data
	 *            Der Knoten.
	 */
	public ZestNode(IContainer graphModel, int style, Object data) {
		super(graphModel, style, data);
		if (data instanceof Node) {
			node = (Node) data;
			setNodeLabel();
			setNodeTooltip();
		}
	}

	public Node getNode() {
		return node;
	}

	private void setNodeLabel() {
		this.setText("Frage " + (node.getPosition() + 1));
	}

	private void setNodeTooltip() {
		String frage = DbQueries.getQuestionByNode(node).getQuestionText();

		IFigure tooltip = new Figure();
		tooltip.setLayoutManager(new FlowLayout(false));
		tooltip.setBorder(new MarginBorder(3, 3, 3, 3));
		tooltip.add(new org.eclipse.draw2d.Label(frage));
		this.setTooltip(tooltip);
	}

}
