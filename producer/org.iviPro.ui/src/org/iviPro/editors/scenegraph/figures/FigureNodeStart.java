package org.iviPro.editors.scenegraph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class FigureNodeStart extends IFigureNode {

	private static final Dimension FIGURE_SIZE = new Dimension(121, 45);
	private static final Dimension TRIANGLE_SIZE = new Dimension(25, 25);
	private static final Point TRIANGLE_OFFSET = new Point(2, 0);
	private static final Font FONT = new Font(Display.getDefault(),
			new FontData("Sans serif", 10, SWT.NORMAL)); //$NON-NLS-1$

	public FigureNodeStart(Point pos) {
		setLocation(pos);
		setSize(FIGURE_SIZE);
	}

	@Override
	protected void paintFigure(Graphics g) {
		Dimension figureSize = getSize();
		
		Rectangle triangleBounds = new Rectangle(getBounds());
		triangleBounds.setSize(TRIANGLE_SIZE);
		triangleBounds.x += TRIANGLE_OFFSET.x;
		triangleBounds.y += TRIANGLE_OFFSET.y;
		centerHorizontally(triangleBounds);
		
		int[] points = { triangleBounds.x, triangleBounds.y + triangleBounds.height - 1, // Links unten
				triangleBounds.x + triangleBounds.width / 2, triangleBounds.y, // Mitte oben
				triangleBounds.x + triangleBounds.width, triangleBounds.y + triangleBounds.height - 1 // Rechts unten
		};
		

		// Gradient fill: Umrisse mit path definieren.
		AdvancedPath path = new AdvancedPath();
		path.addPolygon(points);
		g.setClip(path);
		g.setBackgroundColor(ColorConstants.orange);
		g.setForegroundColor(ColorConstants.yellow);
		g.fillGradient(triangleBounds.x, triangleBounds.y,
				triangleBounds.width, triangleBounds.height, true);

		// Rahmen zeichnen
		g.setForegroundColor(ColorConstants.darkGray);
		g.setClip(getBounds());
		g.drawPolygon(points);
		g.setAntialias(SWT.ON);
		g.drawPolygon(points);

		// Beschreibung fuer den Start-Knoten zeichnen
		String text = Messages.FigureNodeStart_Title;
		g.setFont(FONT);
		g.setForegroundColor(ColorConstants.black);
		Dimension textSize = FigureUtilities.getTextExtents(text, FONT);
		int textOffsetX = (figureSize.width - textSize.width) / 2;
		g.drawString(text, getLocation().x + textOffsetX, triangleBounds.y
				+ triangleBounds.height);

	}
}
