package org.iviPro.editors.scenegraph.figures;


import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.iviPro.model.resources.Picture;

public class FigureNodeSelectionControl extends FigureAbstractNodeSelectionControl {
	
	private static final Color LIGHTGRAY = new Color(Display.getDefault(),
			207, 207, 207);
	private static final Color DARKGRAY = new Color(Display.getDefault(),
			159, 159, 159);
	
	public FigureNodeSelectionControl(Point pos, String text, int zoom, 
			Picture buttonImage) {
		super(pos, text, zoom, buttonImage, DARKGRAY, LIGHTGRAY);
	}
}
