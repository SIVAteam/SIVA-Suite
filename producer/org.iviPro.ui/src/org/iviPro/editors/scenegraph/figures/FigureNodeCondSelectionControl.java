package org.iviPro.editors.scenegraph.figures;


import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.iviPro.model.resources.Picture;

public class FigureNodeCondSelectionControl extends FigureAbstractNodeSelectionControl {
	
	private static final Color LIGHTBLUE = new Color(Display.getDefault(),
			170, 205, 250);
	private static final Color DARKBLUE = new Color(Display.getDefault(),
			120, 155, 200);

	public FigureNodeCondSelectionControl(Point pos, String text, int zoom, 
			Picture buttonImage) {
		super(pos, text, zoom, buttonImage, DARKBLUE, LIGHTBLUE);
	}
}
