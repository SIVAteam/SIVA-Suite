package org.iviPro.editors.scenegraph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class FigureRectangle extends IFigureNode {

	private final Color gradientDarkColor;
	private final Color gradientLightColor;
	private final Color fontColor;
	private static final int PADDING = 10;
	private static final Font TITLE_FONT = new Font(Display.getDefault(),
			new FontData("Sans serif", 10, SWT.BOLD)); //$NON-NLS-1$

	protected String title = ""; //$NON-NLS-1$

	/**
	 * Creates a rectangular node figure of predetermined size at the given
	 * position. The given text is truncated to fit into the figure bounds.
	 * The given colors define the gradient which is used to fill the
	 * figure.
	 * @param pos position of the figure
	 * @param title text which should be displayed on the figure
	 * @param gradientDarkColor dark color of the gradient
	 * @param gradientLightColor light color of the gradient
	 */
	public FigureRectangle(Point pos, Dimension size, String title,
			Color gradientDarkColor, Color gradientLightColor, Color fontColor) {
		setLocation(pos);
		setSize(size);
		setText(title);
		this.gradientDarkColor = gradientDarkColor;
		this.gradientLightColor = gradientLightColor;
		this.fontColor = fontColor;
		setLayoutManager(new XYLayout());
	}

	@Override
	protected void paintFigure(Graphics g) {
		super.paintFigure(g);
	}
	
	protected void paintGradient(Graphics g) {
		// Rechteck mit Gradient Fuellung zeichnen
		g.setBackgroundColor(gradientLightColor);
		g.setForegroundColor(gradientDarkColor);
		g.fillGradient(bounds, true);
	}
	
	protected void paintSmoothBorder(Graphics g) {
		// Draw smoothed border
		g.setAntialias(SWT.ON);
		g.setForegroundColor(ColorConstants.darkGray);
		Rectangle drawBounds = new Rectangle(bounds);
		drawBounds.width = drawBounds.width - 1;
		drawBounds.height = drawBounds.height - 1;
		g.drawRectangle(drawBounds);
	}
	
	protected void paintTitle(Graphics g, String title, int indent, boolean centered) {
		g.setForegroundColor(fontColor);
		g.setFont(TITLE_FONT);
		String truncated = getTruncatedText(title, TITLE_FONT,
				bounds.width - 2 * PADDING - indent, false);
		Dimension size = getStringSize(truncated, TITLE_FONT);
		if (centered) {
			indent = (bounds.width - size.width)/2 - PADDING;
		}
		g.drawText(truncated, bounds.x + PADDING + indent, 
				bounds.y + (bounds.height - size.height)/2);
	}

	public void setText(String text) {
		this.title = text;
		repaint();
	}
	
	protected int getPadding() {
		return PADDING;
	}
}


