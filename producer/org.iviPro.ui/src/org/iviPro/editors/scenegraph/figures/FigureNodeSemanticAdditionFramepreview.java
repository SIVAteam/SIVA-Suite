package org.iviPro.editors.scenegraph.figures;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * Klasse zur Anzeige der Framevorschau
 * (Verwendet in der sem. Zoomstufe 2)
 * @author grillc
 *
 */
public class FigureNodeSemanticAdditionFramepreview extends IFigureNode {

	public static Color classColor = new Color(null,240,240,240);
	public static Color BORDERCOLOR = new Color(null,180,180,180);
	public Font classFont = new Font(null, "Arial", 10, SWT.BOLD); //$NON-NLS-1$

	public FigureNodeSemanticAdditionFramepreview(Point pos) {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);	
		setBorder(new CustomBorder());
		setBackgroundColor(classColor);
		setOpaque(true);

		setLocation(pos);
		setSize(new Dimension(140,45));
	}

	//Setzen eines Bildes innerhalb der Figure
	private void setImage(Image img, Dimension OFFSET, int width, int height) {
		ImageFigure imgfig = new ImageFigure(img);
		imgfig.setSize(width, height);
		Point pos = getLocation();
		pos.x += OFFSET.width;
		pos.y += OFFSET.height;
		imgfig.setLocation(pos);
		add(imgfig);
	}

	/**
	 * Setzen des Vorschaubildes
	 * @param img Vorschaubild
	 */
	public void setTransitionPreviewFrame(Image img) {
		Dimension OFFSET = new Dimension(0,5);
		setImage(img, OFFSET, 140, img.getBounds().height);
		setSize(140, img.getBounds().height + 9);
	}

	/**
	 * Selbst erstellter Rahmen für die Figure
	 * @author grillc
	 *
	 */
	public class CustomBorder extends AbstractBorder {
		public Insets getInsets(IFigure figure) {
			return new Insets(0,0,0,0);
		}
		public void paint(IFigure figure, Graphics graphics, Insets insets) {
			graphics.setLineWidth(2);
			graphics.setForegroundColor(BORDERCOLOR);
			graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft(), getPaintRectangle(figure, insets).getTopRight());
			graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft(), getPaintRectangle(figure, insets).getBottomLeft());			
			graphics.drawLine(getPaintRectangle(figure, insets).getTopRight(), getPaintRectangle(figure, insets).getBottomRight());
		}
	}
}