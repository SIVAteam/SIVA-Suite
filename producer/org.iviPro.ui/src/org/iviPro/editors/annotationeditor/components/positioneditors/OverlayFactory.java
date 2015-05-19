package org.iviPro.editors.annotationeditor.components.positioneditors;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.iviPro.editors.annotationeditor.components.Messages;

public class OverlayFactory {
	
	/**
	 * Figure representing a button with text on it.
	 * @author John
	 */
	public class MarkButtonFigure extends Figure {
		
		private static final int MINWIDTH = 50;
		private static final int MINHEIGHT = 30;
		private static final int MARGIN = 12;
		
		private Label label;
		
		/**
		 * Creates a button without text on it using the class's minimum size.
		 */
		private MarkButtonFigure() {
			this("");
		}
		
		/**
		 * Creates a button labeled with the given text. The button is sized
		 * so as to fit the contained text. 
		 * @param text text of the button
		 */
		private MarkButtonFigure(String text) {
			super();
			// Get default font needed for calculation of button size
			Font f = PlatformUI.getWorkbench().getDisplay().getSystemFont();
			setFont(f);
			setLayoutManager(new BorderLayout());
			label = new Label(text); //$NON-NLS-1$
			label.setFont(f);
			add(label, BorderLayout.CENTER);
			updateDimension();
		}
		
		/**
		 * Gets the label of the button.
		 * @return label of the button
		 */
		public String getText() {
			return label.getText();
		}
		
		/**
		 * Sets the label of the button to given text.
		 * @param text new label of the button
		 */
		public void setText(String text) {
			this.label.setText(text);
		}
		
		/**
		 * Updates the size of the button so as to fit the sizie of the text.
		 */
		public void updateDimension() {
			int textWidth = label.getTextBounds().width + MARGIN;
			if (textWidth < MINWIDTH) {
				textWidth = MINWIDTH;
			}
			int textHeight = label.getTextBounds().height + MARGIN;
			if (textHeight < MINHEIGHT) {
				textHeight = MINHEIGHT;
			}
			label.setBounds(new Rectangle(getLocation().x, getLocation().y,
					textWidth, textHeight));
			setBounds(new Rectangle(getLocation().x, getLocation().y,
					textWidth, textHeight));
		}	
	}

	private static OverlayFactory factory = null;
	
	public static OverlayFactory getInstance() {
		if (factory == null) {
			factory = new OverlayFactory();
		}
		return factory;
	}
	
	/**
	 * liefert eine OverlayFigure für eine vorhandene ContentFigure
	 * von der Content Figure wird wieder eine Copy erstellt
	 * @param canvas
	 * @param figure
	 * @return
	 */
	public OverlayFigure createOF(OverlayCanvas canvas, Figure figure) {
		return new OverlayFigure(canvas, getCopy(figure));
	}
	
	/**
	 * liefert für den übergebenen OverlayType die Default-OverlayFigure
	 * @param canvas
	 * @param overlayType
	 * @return
	 */
	public OverlayFigure createDefaultOF(OverlayCanvas canvas, OverlayType overlayType) {
		switch(overlayType) {
			case POLYGON:
				PointList points = new PointList();
				points.addPoint(10, 10);
				points.addPoint(200, 10);
				points.addPoint(10, 100);
				points.addPoint(200, 100);
				return createPointBasedOF(canvas, points, overlayType);
			case ELLIPSE: return createRecBasedOF(canvas, 50, 50, 150, 150, overlayType);
			case RECTANGLE: return createRecBasedOF(canvas, 50, 50, 150, 150, overlayType);
			case BUTTON: return createButtonOF(canvas, 50, 50, Messages.OverlayFactory_Default_ButtonText);
		}
		return null;		
	}
		
	/**
	 * Erzeugt eine Rechteck basierte OverlayFigure => Ellipse, Rechteck, Button 
	 * @param canvas
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param overlayType
	 * @return
	 */
	public OverlayFigure createRecBasedOF(OverlayCanvas canvas, int x, int y, int width, int height, OverlayType overlayType) {
		return new OverlayFigure(canvas, createRectangleBoundFigure(x, y, width, height, overlayType));		
	}
	
	/**
	 * Erzeugt eine Punkt Liste basierte OverlayFigure => Polygon
	 * @param canvas
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param overlayType
	 * @return
	 */
	public OverlayFigure createPointBasedOF(OverlayCanvas canvas, PointList pointList, OverlayType overlayType) {
		return new OverlayFigure(canvas, createPointListFigure(pointList, overlayType));
	}	
	
	/**
	 * Creates a button overlay figure.
	 * @param canvas the canvas on which the figure will be drawn
	 * @param x the x position of the figure
	 * @param y the y position of the figure
	 * @return new button overlay figure
	 */
	public OverlayFigure createButtonOF(OverlayCanvas canvas, int x, int y, String text) {
		return new OverlayFigure(canvas, createButtonFigure(x, y, text));
	}
	
	/**
	 * Creates a button figure with the given text as label.
	 * @param canvas
	 * @param x
	 * @param y
	 * @return
	 */
	public Figure createButtonFigure(int x, int y, String text) {
		MarkButtonFigure button = new MarkButtonFigure(text);
		button.setOpaque(true);
		button.setLocation(new Point(x,y));
		button.setBackgroundColor(Display.getCurrent()
				.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		button.setForegroundColor(Display.getCurrent()
				.getSystemColor(SWT.COLOR_BLACK));
		return button; 
	}
	
	/**
	 * erzeugt eine Rechteck-basierte Figure = ContentFigure (Rechteck, Ellipse, Button)
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param overlayType
	 * @return
	 */
	public Figure createRectangleBoundFigure(int x, int y, int width, int height, OverlayType overlayType) {
		Figure fig = null;
		switch(overlayType) {
			case BUTTON:
				//MarkButtonFigure button = new MarkButtonFigure();
				//button.setFont(canvas.getFont());
				//fig = button;
//				Dimension d = button.getNeededBounds();
//				System.out.println("LABEL SIZE: " + d.width + "," + d.height);
//				// Keep a minimum dimension
//				if (d.width() < width || d.height() < height) {
//					fig.setBounds(new Rectangle(x, y, width, height));
//				} else {
//					fig.setBounds(new Rectangle(x, y, d.width, d.height));
//				}
				break;
			case RECTANGLE:
				fig = new RectangleFigure();
				fig.setBounds(new Rectangle(x, y, width, height));
				break;
			case ELLIPSE:
				fig = new Ellipse();
				fig.setBounds(new Rectangle(x, y, width, height));
				break;
		}
		fig.setOpaque(false);
		fig.setBackgroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
		fig.setForegroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
		return fig;
		
	}
	
	/**
	 * erzeugt eine PointList-basierte Figure = contentFigure (Polygon)
	 * @param pointList
	 * @param overlayType
	 * @return
	 */
	public Figure createPointListFigure(PointList pointList, OverlayType overlayType) {
		Figure fig = null;
		switch(overlayType) {
			case POLYGON: 
				fig = new Polygon();
				((Polygon) fig).setPoints(pointList);
		}
		fig.setOpaque(false);
		return fig;		
	}	
	
	/**
	 * erzeugt eine Kopie der ContentFigure d.h. der eigentlich Figure(Ellipse, Button, Polygon, Rechteck)!!
	 * nicht aber der gesamten OverlayFigure!!
	 * @param contentFigure
	 * @return copy contentFigure
	 */
	public Figure getCopy(Figure contentFigure) {
		Figure copy = null;
		if (contentFigure instanceof OverlayFigure) {
			contentFigure = ((OverlayFigure) contentFigure).getContentFigure();
		}
		if (contentFigure instanceof Ellipse) {
			copy = new Ellipse();
			copy.setBounds(new Rectangle(contentFigure.getBounds().x,
					contentFigure.getBounds().y, contentFigure.getBounds().width,
					contentFigure.getBounds().height));
		} else
		if (contentFigure instanceof MarkButtonFigure) {
			copy = new MarkButtonFigure(((MarkButtonFigure) contentFigure).getText());
			copy.setBounds(new Rectangle(contentFigure.getBounds().x,
					contentFigure.getBounds().y, contentFigure.getBounds().width,
					contentFigure.getBounds().height));	
		}
		if (contentFigure instanceof RectangleFigure) {
			copy = new RectangleFigure();
			copy.setBounds(new Rectangle(contentFigure.getBounds().x, 
					contentFigure.getBounds().y, contentFigure.getBounds().width,
					contentFigure.getBounds().height));
		} else  			
		if (contentFigure instanceof Polygon) {
			copy = new Polygon();
			PointList points = new PointList();
			PointList oldPoints = ((Polygon) contentFigure).getPoints();
			for (int i = 0; i < oldPoints.size(); i++) {
				Point curPoint = oldPoints.getPoint(i);
				points.addPoint(curPoint.x, curPoint.y);
			}
			((Polygon) copy).setPoints(points);
		}
		
		copy.setOpaque(false);
		return copy;
	}
}
