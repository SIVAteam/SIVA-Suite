package org.iviPro.editors.scenegraph.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class FigureNodeQuizControl extends FigureRectangle {
	
	private static final Color LIGHTORANGE = new Color(Display.getDefault(),
			255, 205, 55);
	private static final Color DARKORANGE = new Color(Display.getDefault(),
			245, 180, 65);
	private static final Color DARKBLACK = new Color(Display.getDefault(),
			48, 48, 48);
	
	private static final Dimension SIZE = new Dimension(100, 35);

	public FigureNodeQuizControl(Point pos, String text) {
		super(pos, SIZE, text, DARKORANGE, LIGHTORANGE, DARKBLACK);
	}
	
	@Override
	protected void paintFigure(Graphics g) {
		paintGradient(g);
		paintSmoothBorder(g);				
		paintTitle(g, title, 0, true);
		super.paintFigure(g);
	}
}
