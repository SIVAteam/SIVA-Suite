package org.iviPro.editors.quiz.std;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.iviPro.model.quiz.Condition;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.Edge;

/**
 * Hilfs-Klasse um Kanten-Eigenschaften im Zest-Graphen darzustellen.
 * 
 * @author Sabine Gattermann
 * 
 */
public class ZestEdge extends GraphConnection {

    private Edge edge;
    private Condition condition;
    // der Insert-Algorithmus-Typ
    private int algorithm;

    /**
     * Konstruktor
     * 
     * @param graphModel
     *            Der Graph.
     * @param style
     *            Der Style.
     * @param source
     *            Der Quellknoten.
     * @param destination
     *            Der Zielknoten.
     * @param edge
     *            Die Kante.
     * @param insertsAlgorithm
     *            Der Insert-Algorithmus.
     */
    public ZestEdge(Graph graphModel, int style, ZestNode source,
	    ZestNode destination, Edge edge, int insertsAlgorithm) {
	super(graphModel, style, source, destination);
	this.edge = edge;
	this.condition = DbQueries.getConditionData(edge.getIdCondition());
	this.algorithm = insertsAlgorithm;
	setEdgeText();
	setEdgeTooltip();
    }

    private ZestEdge() {
	super(null, 0, null, null);
    }

    public Edge getEdge() {
	return edge;
    }

    private void setEdgeText() {
	if (algorithm == 1
		&& (condition.getConditionPoints() != -1 || (condition
			.getConditionPoints() == -1 && condition
			.getConditionLookback() > 1)))
	    if (condition.getConditionPoints() == -1)
		this.setText("(" + condition.getConditionLookback() + ":D)");
	    else
		this.setText("(" + condition.getConditionLookback() + ":"
			+ condition.getConditionPoints() + ")");
    }

    private void setEdgeTooltip() {
	String tip = "";
	if (algorithm == 1) {
	    if (condition.getConditionPoints() == -1) {
		tip = "keine Bedingung definiert";
	    } else {
		tip = "vorherige Knoten: " + condition.getConditionLookback()
			+ "\n" + "erreichte Punkte: "
			+ condition.getConditionPoints();
	    }
	    IFigure tooltip = new Figure();
	    tooltip.setLayoutManager(new FlowLayout(false));
	    tooltip.setBorder(new MarginBorder(3, 3, 3, 3));
	    tooltip.add(new org.eclipse.draw2d.Label(tip));
	    this.setTooltip(tooltip);
	}
    }

    public static ZestEdge instanceOf() {
	return new ZestEdge();
    }
}
