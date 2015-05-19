package org.iviPro.editors.scenegraph.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Display;

public class FigureNodeResume extends FigureRectangle {
	
	private static final Color DARKPINK = new Color(Display.getDefault(),
			235, 80, 80);
	private static final Color LIGHTPINK = new Color(Display.getDefault(),
			255, 128, 128);	
	private static final Color DARKBLACK = new Color(Display.getDefault(),
			48, 48, 48);
	
	private static final Dimension SIZE = new Dimension(100, 35);
	private static final Dimension ICONSIZE = new Dimension(16, 13);

	public FigureNodeResume(Point pos, String text) {
		super(pos, SIZE, text, DARKPINK, LIGHTPINK, DARKBLACK);
	}
	
	@Override
	protected void paintFigure(Graphics g) {
		Point pos = getLocation();
		paintGradient(g);
		paintSmoothBorder(g);

		// Draw icon
		Path iconPath = new Path(Display.getCurrent());
		iconPath.addRectangle(0, 0, ICONSIZE.width/4, ICONSIZE.height);
		iconPath.moveTo(ICONSIZE.width/2, 0);
		iconPath.lineTo(ICONSIZE.width, ICONSIZE.height/2);
		iconPath.lineTo(ICONSIZE.width/2, ICONSIZE.height);
		iconPath.lineTo(ICONSIZE.width/2, 0);
		
		g.translate(pos.x + (SIZE.width - ICONSIZE.width)/2,
				pos.y + (SIZE.height - ICONSIZE.height)/2);
		g.setBackgroundColor(DARKBLACK);
		g.fillPath(iconPath);
		super.paintFigure(g);
	}
}
