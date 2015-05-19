package org.iviPro.editors.scenegraph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class FigureNodeEnd extends IFigureNode {

	private static final Dimension FIGURE_SIZE = new Dimension(121, 45);
	private static final Dimension CIRCLE_SIZE = new Dimension(25, 25);
	private static final Point CIRCLE_OFFSET = new Point(2, 0);
	private static final Font FONT = new Font(Display.getDefault(),
			new FontData("Sans serif", 10, SWT.NORMAL)); //$NON-NLS-1$
	private static final int PADDING = 5;
	
	private String text;

	public FigureNodeEnd(Point pos, String text) {
		setLocation(pos);
		setSize(FIGURE_SIZE);
		setText(text);
	}

	@Override
	protected void paintFigure(Graphics g) {

		Dimension figureSize = getSize();
		Rectangle circleBounds = new Rectangle(getBounds());
		circleBounds.setSize(CIRCLE_SIZE);
		circleBounds.x += CIRCLE_OFFSET.x;
		circleBounds.y += CIRCLE_OFFSET.y;
		centerHorizontally(circleBounds);

		// Kreis mit Gradient Fuellung zeichnen
		AdvancedPath path = new AdvancedPath();
		path.addEllipse(circleBounds);
		g.setClip(path);
		g.setBackgroundColor(ColorConstants.red);
		g.setForegroundColor(ColorConstants.orange);
		g.fillGradient(circleBounds, true);

		// Rahmen zeichnen
		g.setForegroundColor(ColorConstants.darkGray);
		g.setClip(getBounds());
		g.setAntialias(SWT.ON);
		g.drawOval(circleBounds.x, circleBounds.y, circleBounds.width - 1,
				circleBounds.height - 1);

		// Beschreibung fuer den Start-Knoten zeichnen
		
		g.setForegroundColor(ColorConstants.black);
		int maxTextWidth = bounds.width - 2 * PADDING;
		String truncatedText = getTruncatedText(text, FONT, maxTextWidth, false);
		Dimension textSize = getStringSize(truncatedText, FONT);
		Rectangle textBounds = new Rectangle(getLocation(), textSize);
		textBounds.y = textBounds.y + bounds.height - textSize.height;
		centerHorizontally(textBounds);
		g.setFont(FONT);
		g.drawText(truncatedText, textBounds.x, textBounds.y);
	}
	
	public void setText(String text) {
		this.text = text;
		repaint();
	}

}
