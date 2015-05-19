package org.iviPro.editors.scenegraph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class FigureNodeSelection extends IFigureNode {

	private static final Color LIGHTGRAY = new Color(Display.getDefault(),
			207, 207, 207);
	private static final Color DARKGRAY = new Color(Display.getDefault(),
			159, 159, 159);

	private static final Dimension ICON_SIZE = new Dimension(35, 35);
	private static final Dimension OVERALL_SIZE = new Dimension(121, 54);

	private static final Color FONT_COLOR = ColorConstants.black;
	private static final Font TITLE_FONT = new Font(Display.getDefault(),
			new FontData("Sans serif", 10, SWT.NORMAL)); //$NON-NLS-1$
	private static final int PADDING = 5;

	private String text;

	public FigureNodeSelection(Point pos, String text) {
		setLocation(pos);
		setSize(OVERALL_SIZE);
		setText(text);

	}

	@Override
	protected void paintFigure(Graphics g) {
		g.setAntialias(SWT.ON);
		Dimension figureSize = OVERALL_SIZE;
		Point pos = getLocation();

		// Diamond shape
		int[] points = {
				//
				pos.x + ICON_SIZE.width / 2, pos.y + ICON_SIZE.height - 1, // Unten
				pos.x, pos.y + ICON_SIZE.height / 2, // Links
				pos.x + ICON_SIZE.width / 2 , pos.y, // Oben
				pos.x + ICON_SIZE.width - 1, pos.y + ICON_SIZE.height / 2 // Rechts
		};
		centerHorizontally(points);
		AdvancedPath path = new AdvancedPath();
		path.addPolygon(points);

		// Draw diamond
		g.clipPath(path);
		g.setBackgroundColor(LIGHTGRAY);
		g.setForegroundColor(DARKGRAY);
		float[] pathBounds = new float[4];
		path.getBounds(pathBounds);
		g.fillGradient((int)pathBounds[0], (int)pathBounds[1],
				(int)pathBounds[2], (int)pathBounds[3], true);

		g.setClip(getBounds());
		g.setForegroundColor(ColorConstants.black);
		g.drawPath(path);

		// Draw title
		g.setForegroundColor(FONT_COLOR);
		int maxTextWidth = getBounds().width - 2 * PADDING;
		String truncatedText = getTruncatedText(text, TITLE_FONT, maxTextWidth, false);
		Dimension textSize = getStringSize(truncatedText, TITLE_FONT);
		Rectangle textBounds = new Rectangle(getLocation(), textSize);
		textBounds.y = textBounds.y + bounds.height - textSize.height;
		centerHorizontally(textBounds);
		g.setFont(TITLE_FONT);
		g.drawText(truncatedText, textBounds.x, textBounds.y);

	}

	public void setText(String text) {
		this.text = text;
		repaint();
	}
}
