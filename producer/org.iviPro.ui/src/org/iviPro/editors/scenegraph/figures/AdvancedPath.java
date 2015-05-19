package org.iviPro.editors.scenegraph.figures;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Path;

class AdvancedPath extends Path {

	public AdvancedPath() {
		super(null);
	}

	public AdvancedPath(Device device) {
		super(device);
	}

	public void addPolygon(int[] points) {
		if (isDisposed()) {
			SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
		}
		moveTo(points[0], points[1]);
		for (int i = 0; i < points.length; i = i + 2) {
			lineTo(points[i], points[i + 1]);
		}
		if (points.length >= 2) {
			lineTo(points[0], points[1]);
		}
	}

	/**
	 * Adds a circle specified by x, y, radius to this path.
	 * 
	 * @param x
	 *            the x coordinate of the circles bounding box
	 * @param y
	 *            the y coordinate of the circles bounding box
	 * @param diameter
	 *            the diameter of the circle
	 * 
	 * @exception SWTException
	 *                ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed
	 */
	public void addCircle(float x, float y, float diameter) {
		if (isDisposed()) {
			SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
		}
		addArc(x, y, diameter, diameter, 0, 360);
	}

	/**
	 * Adds a ellipse to this path.
	 * 
	 * @param bounds
	 *            The bounding box of the ellipse.
	 * @exception SWTException
	 *                ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed
	 */
	public void addEllipse(Rectangle bounds) {
		if (isDisposed()) {
			SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
		}
		addArc(bounds.x, bounds.y, bounds.width, bounds.height, 0, 360);
	}

	/**
	 * Adds to the receiver the round-cornered rectangle specified by x, y,
	 * width and height.
	 * 
	 * @param bounds
	 *            The bounding box of the round-cornered rectangle.
	 * @param cornerRadius
	 *            The radius of the arc building the corners.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void addRoundRectangle(Rectangle bounds, int cornerRadius) {
		if (isDisposed()) {
			SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
		}

		float x = bounds.x;
		float y = bounds.y;
		float width = bounds.width;
		float height = bounds.height;
		float radius = cornerRadius;

		lineTo(x + radius, y);
		lineTo(x + width - radius, y); // Gerade oben
		cubicTo(x + width - radius, y, // Anfang Eckbogen
				x + width + 1, y - 1, // Ecke rechts oben
				x + width, y + radius); // Ende Eckbogen
		lineTo(x + width, y + height - radius); // Gerade rechts
		cubicTo(x + width, y + height - radius, // Start Eckbogen
				x + width + 1, y + height + 1, // Ecke rechts unten
				x + width - radius, y + height); // Ende Eckbogen
		lineTo(x + radius, y + height); // Gerade unten
		cubicTo(x + radius, y + height, // Start Eckbogen
				x - 1, y + height + 1, // Ecke links unten
				x, y + height - radius); // Ende Eckbogen
		lineTo(x, y + radius); // Gerade links
		cubicTo(x, y + radius,// Start Eckbogen
				x - 1, y - 1, // Ecke links oben
				x + radius, y); // Ende Eckbogen
	}

	/**
	 * Adds to the receiver the upper half round-cornered rectangle specified by x, y,
	 * width and height.
	 * 
	 * @param bounds
	 *            The bounding box of the upper half round-cornered rectangle.
	 * @param cornerRadius
	 *            The radius of the arc building the corners.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void addHalfRoundRectangle(Rectangle bounds, int cornerRadius) {
		if (isDisposed()) {
			SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
		}

		float x = bounds.x;
		float y = bounds.y;
		float width = bounds.width;
		float height = bounds.height;
		float radius = cornerRadius;

		lineTo(x + radius, y);
		lineTo(x + width - radius, y); // Gerade oben
		cubicTo(x + width - radius, y, // Anfang Eckbogen
				x + width + 1, y - 1, // Ecke rechts oben
				x + width, y + radius); // Ende Eckbogen
		lineTo(x + width, y + height); // Gerade rechts
		lineTo(x, y + height); // Gerade unten
		lineTo(x, y + radius); // Gerade links
		cubicTo(x, y + radius,// Start Eckbogen
				x - 1, y - 1, // Ecke links oben
				x + radius, y); // Ende Eckbogen
	}
}
