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

public class FigureNodeRandomSelection extends IFigureNode {

	private static final Color LIGHTGREEN = new Color(Display.getDefault(),
			190, 250, 190);
	private static final Color DARKGREEN = new Color(Display.getDefault(),
			155, 210, 155);

	private static final Dimension ICON_SIZE = new Dimension(35, 35);
	private static final Dimension OVERALL_SIZE = new Dimension(121, 54);

	private static final Color FONT_COLOR = ColorConstants.black;
	private static final Font TITLE_FONT = new Font(Display.getDefault(),
			new FontData("Sans serif", 10, SWT.NORMAL)); //$NON-NLS-1$
	private static final Font MARK_FONT = new Font(Display.getDefault(),
			new FontData("Sans serif", 10, SWT.NORMAL)); //$NON-NLS-1$
	private static final int PADDING = 5;

	private String text;

	public FigureNodeRandomSelection(Point pos, String text) {
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
		g.setBackgroundColor(LIGHTGREEN);
		g.setForegroundColor(DARKGREEN);
		float[] pathBounds = new float[4];
		path.getBounds(pathBounds);
		g.fillGradient((int)pathBounds[0], (int)pathBounds[1],
				(int)pathBounds[2], (int)pathBounds[3], true);
		//g.fillGradient(pos.x, pos.y, figureSize.width, figureSize.height, true);
		
		g.setClip(getBounds());
		g.setForegroundColor(ColorConstants.darkGray);
		g.drawPath(path);
		
		// Draw title
		g.setForegroundColor(FONT_COLOR);
		int maxTextWidth = bounds.width - 2 * PADDING;
		String truncatedText = getTruncatedText(text, TITLE_FONT, maxTextWidth, false);
		Dimension textSize = getStringSize(truncatedText, TITLE_FONT);
		Rectangle textBounds = new Rectangle(getLocation(), textSize);
		textBounds.y = textBounds.y + bounds.height - textSize.height;
		centerHorizontally(textBounds);
		g.setFont(TITLE_FONT);
		g.drawText(truncatedText, textBounds.x, textBounds.y);
		
		// Draw question mark
		Dimension markSize = getStringSize("?", MARK_FONT); //$NON-NLS-1$
		Rectangle markBounds = new Rectangle(getLocation(), markSize);
		markBounds.y = markBounds.y + (ICON_SIZE.height - markSize.height)/2;
		centerHorizontally(markBounds);
		g.setFont(MARK_FONT);
		g.drawText("?", markBounds.x, markBounds.y);
		//		g.drawText("?", pos.x + (OVERALL_SIZE.width - markSize.width) / 2, //$NON-NLS-1$
//				pos.y + (ICON_SIZE.height - markSize.height) / 2);

	}

	public void setText(String text) {
		this.text = text;
		repaint();
	}
}

