package org.iviPro.editors.scenegraph.figures;


import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.iviPro.utils.SivaTime;

/**
 * Klasse zur Anzeige der Szeneninfo (Dauer, Quellvideo,...)
 * (Verwendet in der sem. Zoomstufe 3)
 * @author grillc
 *
 */
public class FigureNodeSemanticAdditionSceneInfo extends IFigureNode {

	public static Color classColor = new Color(null,240,240,240);
	public static Color BORDERCOLOR = new Color(null,180,180,180);
	public Font classFont = new Font(null, "Arial", 10, SWT.BOLD); //$NON-NLS-1$

	public FigureNodeSemanticAdditionSceneInfo(Point pos) {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);	
		setBorder(new CustomBorder());
		setBackgroundColor(classColor);
		setOpaque(true);

		setLocation(pos);
		setSize(new Dimension(140,45));
	}
	
	/**
	 * Setzen der Dauer
	 * @param duration Zeit in ms
	 */
	public void setDuration(long duration) {
		Label labelDuration = new Label(Messages.FigureNodeSemanticAdditionSceneInfo_Duration + ": " + SivaTime.getTimeString(duration)); //$NON-NLS-1$
		labelDuration.setSize(130, 20);
		labelDuration.setLabelAlignment(Label.LEFT);
		Dimension offset = new Dimension(5,0);
		Point pos = getLocation();
		pos.x += offset.width;
		pos.y += offset.height;
		labelDuration.setLocation(pos);
		add(labelDuration);
	}
	
	/**
	 * Setzen des Quellvideonamens
	 * @param source Name des Videos
	 */
	public void setSource(String source) {
		Label labelSource = new Label(Messages.FigureNodeSemanticAdditionSceneInfo_Source + ": " + source); //$NON-NLS-1$
		labelSource.setSize(130, 20);
		labelSource.setLabelAlignment(Label.LEFT);
		Dimension offset = new Dimension(5,20);
		Point pos = getLocation();
		pos.x += offset.width;
		pos.y += offset.height;
		labelSource.setLocation(pos);
		add(labelSource);
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
			graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft(), getPaintRectangle(figure, insets).getBottomLeft());			
			graphics.drawLine(getPaintRectangle(figure, insets).getTopRight(), getPaintRectangle(figure, insets).getBottomRight());
			graphics.drawLine(getPaintRectangle(figure, insets).getBottomLeft(), getPaintRectangle(figure, insets).getBottomRight());
		}
	}
}