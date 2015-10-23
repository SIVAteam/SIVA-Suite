package org.iviPro.editors.scenegraph.figures;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.iviPro.model.graph.NodeScene;

public class FigureNodeSemanticAdditionNodeId extends IFigureNode {	

	public static Color classColor = new Color(null,240,240,240);
	public static Color BORDERCOLOR = new Color(null,180,180,180);
	public Font classFont = new Font(null, "Arial", 10, SWT.BOLD); //$NON-NLS-1$
	private static final int MARGIN_HEIGHT = 3;

	public FigureNodeSemanticAdditionNodeId(Point pos, NodeScene nodeScene) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		setLayoutManager(layout);
		layout.marginHeight = MARGIN_HEIGHT;
		setBorder(new CustomBorder());
		setBackgroundColor(classColor);
		setForegroundColor(ColorConstants.gray);
		setOpaque(true);
		setLocation(pos);
		Label idLabel = new Label();
		idLabel.setText("id: " + nodeScene.getNodeID()); //$NON-NLS-1$
		add(idLabel);
		setSize(new Dimension(140, getFontHeight(classFont) + 2*MARGIN_HEIGHT ));
	}
	
	public class CustomBorder extends AbstractBorder {
		public Insets getInsets(IFigure figure) {
			return new Insets(0,0,0,0);
		}
		public void paint(IFigure figure, Graphics graphics, Insets insets) {
			graphics.setLineWidth(2);
			graphics.setForegroundColor(BORDERCOLOR);
			graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft(), getPaintRectangle(figure, insets).getBottomLeft());			
			graphics.drawLine(getPaintRectangle(figure, insets).getTopRight(), getPaintRectangle(figure, insets).getBottomRight());
			graphics.drawLine(getPaintRectangle(figure, insets).getBottomLeft(), getPaintRectangle(figure, insets).getBottomRight());
		}
	}
}

