package org.iviPro.editors.annotationeditor.components.positioneditors;

import org.eclipse.draw2d.AbstractPointListShape;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.iviPro.editors.annotationeditor.components.positioneditors.OverlayFactory.MarkButtonFigure;

/**
 * Eine Overlay Figure wird verwendet um ein Objekt über das Video zu legen z.B.
 * Markierungen und den Overlay Path Die Overlay Figure ist eine Figure und
 * kapselt die eigentliche Figure(Ellipse, Rechteck, Polygon...) und die
 * zugehörigen Controls und bildet somit die Top-Level Figure des Lightweight
 * System des Canvas
 * 
 * @author juhoffma
 */
public class OverlayFigure extends Figure {

	// finals für die Position der Controls bei Figuren die von einem
	// Rechteck umgeben werden z.B. Ellipse, Rechteck
	// entspricht dem Index in resizeControls
	private final int C_TOP = 0;
	private final int C_RIGHT = 1;
	private final int C_BOTTOM = 2;
	private final int C_LEFT = 3;
	private final int C_TOPRIGHT = 4;
	private final int C_BOTTOMRIGHT = 5;
	private final int C_BOTTOMLEFT = 6;
	private final int C_TOPLEFT = 7;
	private final int C_NO_CONTROL = -1;

	// das Canvas auf das die Figur gezeichnet werden soll
	private OverlayCanvas canvas;

	// die Figure + die Region der Figure (Region inkl. ResizeControls)
	private Figure contentFigure;
	private Region region;

	// die Resize Controls, entsprechend den Punkten wie ein Polyline basiertes
	// Objekt aufgebaut ist
	private ResizeControl[] resizeControls;

	// Die Höhe der Control Elemente
	private int controlWidth = 8;
	private int controlHeight = 8;

	// der Punkt auf den beim Drag geklickt wird
	private Point clickPoint;

	// gibt an ob die Figure aktuell verschoben wird
	// das ist nur für die Figure true die wirklich verschoben wird
	// für alle anderen false
	private boolean dragActive = false;

	// das aktuell gewählte Resize Control
	private int currentRC;

	// ist true für alle AbstractPointListShape basierenden Figuren z.B. Polygon
	private boolean pointListShape = false;
	/**
	 * True if the figure stored inside is a button
	 */
	private boolean isButton = false;

	// Grenzen für die Figures ab x == 0 und y == 0
	// entspricht der Größe der Canvas Bounds, falls diese gesetzt sind
	private Rectangle figureBounds;

	private boolean keepAspect;
	
	private double contentAspectRatio = 1;

	// gleicht die Abstände zum Parent des Canvas beim Berechenen der Region aus
	// => Region liegt genau über der Figur
	// 5 ist das Standardmargin ... bei Andersweitiger Verwendung muss der Wert
	// programmatisch/manuel umgesetzt werden
	int poffX = 0;
	int poffY = 0;

	private Composite regionComp;

	/**
	 * 
	 * @param canvas
	 * @param figure
	 * @param useControls
	 */
	public OverlayFigure(final OverlayCanvas canvas, Figure contentFigure) {
		this.region = new Region();
		this.canvas = canvas;
		if (contentFigure instanceof AbstractPointListShape) {
			this.pointListShape = true;
		} else if (contentFigure instanceof OverlayFactory.MarkButtonFigure) {
			this.isButton = true;
		}
		if (canvas.getParent() instanceof Shell) {
			this.regionComp = canvas.getParent();
			this.poffX = 5;
			this.poffY = 5;
		} else {
			this.regionComp = canvas;
		}
		this.regionComp.setRegion(region);
		setContentFigure(contentFigure);
		figureBounds = canvas.getFigureBounds();
	}

	void setContentFigure(Figure newContentFigure) {
		if (contentFigure != null) {
			remove(contentFigure);
			contentFigure.erase();
			for (int i = 0; i < resizeControls.length; i++) {
				remove(resizeControls[i]);
				resizeControls[i].erase();
			}
		}

		this.contentFigure = newContentFigure;
		add(newContentFigure);

		initControls();
		// berechne die Region der Figures/Controls
		calculateRegion();
		addListeners();

		// zeichne das Canvas neu, damit beim Ändern der Figur, nichts mehr von
		// der alten Figur
		// sichtbar bleibt
		canvas.notifyContentFigureChanged(contentFigure);
		canvas.redraw();		
	}
	
	public void increasePolygonPoints() {
		if (this.pointListShape) {
			if (this.contentFigure instanceof Polygon) {
				Polygon poly = (Polygon) contentFigure;
				poly.addPoint(new Point(50, 50));
				this.setContentFigure(poly);
			}
		}
	}
	
	public void decreasePolygonPoints() {
		if (this.pointListShape) {
			if (this.contentFigure instanceof Polygon) {
				Polygon poly = (Polygon) contentFigure;
				if (poly.getPoints().size() > 4) {
					poly.removePoint(poly.getPoints().size()-1);
				}
				this.setContentFigure(poly);
			}
		}
	}
	
	public void setKeepAspect(boolean keepAspect) {
		this.keepAspect = keepAspect;
	}
	
	/**
	 * initialisiert die Controls, controls werden im resizeControls array
	 * verwaltet
	 */
	private void initControls() {
		// The button element should not be resizeable
		if (isButton) {
			resizeControls = new ResizeControl[0];
		// Controls für Point basierte Figuren z.B. Polygone
		// => jeder Punkt wird ein Control Element
		} else if (contentFigure instanceof AbstractPointListShape) {
			PointList points = ((AbstractPointListShape) contentFigure)
					.getPoints();
			resizeControls = new ResizeControl[points.size()];
			for (int i = 0; i < points.size(); i++) {
				ResizeControl newControl = new ResizeControl(i);
				newControl.setRCCursor(Display.getCurrent().getSystemCursor(
						SWT.CURSOR_SIZEALL));
				Point curPoint = points.getPoint(i);
				newControl.setBounds(new Rectangle(curPoint.x - controlWidth
						/ 2, curPoint.y - controlHeight / 2, controlWidth,
						controlHeight));
				resizeControls[i] = newControl;
			}
		} else {
			resizeControls = new ResizeControl[8];
			for (int i = 0; i < 8; i++) {
				resizeControls[i] = new ResizeControl(i);
				switch (i) {
				case C_TOP:
					resizeControls[i].setRCCursor(Display.getCurrent()
							.getSystemCursor(SWT.CURSOR_SIZENS));
					break;
				case C_RIGHT:
					resizeControls[i].setRCCursor(Display.getCurrent()
							.getSystemCursor(SWT.CURSOR_SIZEW));
					break;
				case C_BOTTOM:
					resizeControls[i].setRCCursor(Display.getCurrent()
							.getSystemCursor(SWT.CURSOR_SIZENS));
					break;
				case C_LEFT:
					resizeControls[i].setRCCursor(Display.getCurrent()
							.getSystemCursor(SWT.CURSOR_SIZEWE));
					break;
				case C_TOPRIGHT:
					resizeControls[i].setRCCursor(Display.getCurrent()
							.getSystemCursor(SWT.CURSOR_SIZENE));
					break;
				case C_BOTTOMRIGHT:
					resizeControls[i].setRCCursor(Display.getCurrent()
							.getSystemCursor(SWT.CURSOR_SIZESE));
					break;
				case C_BOTTOMLEFT:
					resizeControls[i].setRCCursor(Display.getCurrent()
							.getSystemCursor(SWT.CURSOR_SIZESW));
					break;
				case C_TOPLEFT:
					resizeControls[i].setRCCursor(Display.getCurrent()
							.getSystemCursor(SWT.CURSOR_SIZENW));
					break;
				}
			}

			// Ellipse verwendet nicht alle Controls
			// diese werden einfach unsichtbar gesetzt
			if (contentFigure instanceof Ellipse) {
				resizeControls[this.C_TOPLEFT].setVisible(false);
				resizeControls[this.C_TOPRIGHT].setVisible(false);
				resizeControls[this.C_BOTTOMRIGHT].setVisible(false);
				resizeControls[this.C_BOTTOMLEFT].setVisible(false);
			}
			setRectangleControls();
		}
	}

	/**
	 * setzt die Positionen der Resize Controls bei Figures die von einem
	 * Rechteck eingeschlossen werden => Rechteck + Ellipse
	 */
	private void setRectangleControls() {
		
		// die Positionen der einzelnen Controls
		int halfControlWidth = controlWidth / 2;
		int halfControlHeight = controlHeight / 2;
		int figureX = contentFigure.getBounds().x;
		int figureY = contentFigure.getBounds().y;
		int figureWidth = contentFigure.getBounds().width;
		int figureHeight = contentFigure.getBounds().height;
		int leftX = figureX - halfControlWidth;
		int middleX = figureX + figureWidth / 2 - halfControlWidth;
		int rightX = figureX + figureWidth - halfControlWidth;
		int topY = figureY - halfControlHeight;
		int middleY = figureY + figureHeight / 2 - halfControlHeight;
		int bottomY = figureY + figureHeight - halfControlHeight;

		// setze die Controls entsprechend der Figure
		resizeControls[this.C_TOP].setBounds(new Rectangle(middleX, topY,
				controlWidth, controlHeight));
		resizeControls[this.C_RIGHT].setBounds(new Rectangle(rightX, middleY,
				controlWidth, controlHeight));
		resizeControls[this.C_BOTTOM].setBounds(new Rectangle(middleX, bottomY,
				controlWidth, controlHeight));
		resizeControls[this.C_LEFT].setBounds(new Rectangle(leftX, middleY,
				controlWidth, controlHeight));
		resizeControls[this.C_TOPLEFT].setBounds(new Rectangle(leftX, topY,
				controlWidth, controlHeight));
		resizeControls[this.C_TOPRIGHT].setBounds(new Rectangle(rightX, topY,
				controlWidth, controlHeight));
		resizeControls[this.C_BOTTOMRIGHT].setBounds(new Rectangle(rightX,
				bottomY, controlWidth, controlHeight));
		resizeControls[this.C_BOTTOMLEFT].setBounds(new Rectangle(leftX,
				bottomY, controlWidth, controlHeight));
	}
	
	public boolean isDragActive() {
		return this.dragActive;
	}

	private void addListeners() {

		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (dragActive) {
					translateOF(e.x, e.y);
				} else {
					resizeOF(e.x, e.y);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseHover(MouseEvent e) {
			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}
		});

		addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (dragActive) {
					dragActive = false;
				}
				currentRC = C_NO_CONTROL;
			}
		});

		/**
		 * prüfe ob sie die Position der Content Figure geändert hat und gib das
		 * an das Canvas weiter
		 */
		contentFigure.addFigureListener(new FigureListener() {

			@Override
			public void figureMoved(IFigure source) {
				canvas.notifyContentFigureChanged(source);
			}
		});

		/**
		 * hört auf Maus Events auf der Figur
		 */
		contentFigure.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// setze den angeklickten Punkt
				clickPoint = new Point(e.x, e.y);
				dragActive = true;
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				dragActive = false;
				currentRC = C_NO_CONTROL;
			}
		});

		/**
		 * Listener auf Maus Bewegungs Events z.B. Drag
		 */
		contentFigure.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (dragActive) {
					translateOF(e.x, e.y);
				} else {
					resizeOF(e.x, e.y);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// setze den Cursor wenn die Maus über der Figure ist
				contentFigure.setCursor(Display.getCurrent().getSystemCursor(
						SWT.CURSOR_HAND));
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseHover(MouseEvent e) {
			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}
		});

		// setze Drag Active auf jeden Fall auf false
		// falls die Maus das Canvas verlässt
		canvas.addListener(SWT.MouseExit, new Listener() {
			@Override
			public void handleEvent(Event e) {
				dragActive = false;
				currentRC = C_NO_CONTROL;
			}
		});		
	}

	/**
	 * berechnet die Region der Figure
	 * 
	 */
	public void calculateRegion() {
		// entferne die alte Region um schlieren etc zu vermeiden
		if (this.regionComp.getRegion() != null) {
			this.regionComp.getRegion().subtract(this.regionComp.getRegion());
		}

		// Figur ist ein Rechteck
		if (contentFigure instanceof RectangleFigure ||
				contentFigure instanceof MarkButtonFigure) {
			region.add(contentFigure.getBounds().x + poffX,
					contentFigure.getBounds().y + poffY,
					contentFigure.getBounds().width,
					contentFigure.getBounds().height);
		} else
		// Figure ist eine Ellipse
		if (contentFigure instanceof Ellipse) {
			// für jeden Quadranten wird eine eigene Liste verwendet
			PointList pointsTR = new PointList();
			PointList pointsBR = new PointList();
			PointList pointsTL = new PointList();
			PointList pointsBL = new PointList();
			int width = contentFigure.getBounds().width;
			int height = contentFigure.getBounds().height;
			int a = width / 2;
			int b = height / 2;

			int ellipseMidX = contentFigure.getBounds().x + poffX + a;
			int ellipseMidY = contentFigure.getBounds().y + +poffY + b;
						
			PointList pointsTRR = new PointList();
			PointList pointsBRR = new PointList();
			PointList pointsTLR = new PointList();
			PointList pointsBLR = new PointList();
			for (int x = 0; x <= width / 2; x++) {
				double y = Math.sqrt((1 - Math.pow(x, 2) / Math.pow(a, 2))
						* Math.pow(b, 2));				
				pointsTR.addPoint(ellipseMidX + x, ellipseMidY - (int) y);					
				pointsBR.addPoint(ellipseMidX + x, ellipseMidY + (int) y);				
				pointsTL.addPoint(ellipseMidX - x, ellipseMidY - (int) y);
				pointsBL.addPoint(ellipseMidX - x, ellipseMidY + (int) y);
			}						

			for (int x = 0; x <= width / 2 - 8; x++) {
				double y = Math.sqrt((1 - Math.pow(x, 2) / Math.pow(a, 2))
						* Math.pow(b, 2));							
				pointsTLR.addPoint(ellipseMidX - x, ellipseMidY - (int) y + 4);
				pointsTRR.addPoint(ellipseMidX + x, ellipseMidY - (int) y + 4);
				pointsBRR.addPoint(ellipseMidX + x, ellipseMidY + (int) y - 4);
				pointsBLR.addPoint(ellipseMidX - x, ellipseMidY + (int) y - 4);				
			}

			PointList points = new PointList();
			pointsTL.reverse();
			pointsBR.reverse();
			points.addAll(pointsTL);
			points.addAll(pointsTR);
			points.addAll(pointsBR);
			points.addAll(pointsBL);
			
			PointList remPoints = new PointList();
			pointsTLR.reverse();
			pointsBRR.reverse();
			remPoints.addAll(pointsTLR);
			remPoints.addAll(pointsTRR);
			remPoints.addAll(pointsBRR);
			remPoints.addAll(pointsBLR);
			region.add(points.toIntArray());
			region.subtract(remPoints.toIntArray());
		} else
		// Figure ist Punktbasiert
		if (contentFigure instanceof AbstractPointListShape) {
			PointList pl = ((AbstractPointListShape) contentFigure).getPoints().getCopy();
			pl.translate(poffX, poffY);
			
			int pixOff = 4;
			
			// prüfe den Quadrant in dem der Punkt ist und passe je nachdem
			// die Koordinate so an, dass sie innerhalb des Polygons liegt.
			for (int i = 0; i < pl.size(); i++) {
				Point p = pl.getPoint(i);
				Point np;
				if ( i < pl.size() - 1) {
					np = pl.getPoint(i+1);
				} else {
					np = pl.getFirstPoint();
				}
				int[] points = new int[8];
				points[0] = p.x;
				points[1] = p.y;
				points[2] = p.x+pixOff;
				points[3] = p.y;								
				points[4] = np.x+pixOff;
				points[5] = np.y;
				points[6] = np.x;
				points[7] = np.y;
				
				if (p.y - np.y < 80 && p.y - np.y > - 80 || np.y - p.y < 80 && np.y - p.y > - 80) {
					points[3] = points[3] + pixOff;
					points[5] = points[5] + pixOff;
				}
				
				region.add(points);				
			}			
		}
		// sollte vermieden werden ... tastet das Rechteck im Endeffekt ab ...
		// sehr ineffizient
		else {
			PointList points = new PointList();
			int offx = contentFigure.getBounds().x + poffX;
			int offy = contentFigure.getBounds().y + poffY;
			int width = contentFigure.getBounds().width;
			int height = contentFigure.getBounds().height;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int coX = offx + x;
					int coY = offy + y;
					if (contentFigure.containsPoint(coX, coY)) {
						points.insertPoint(new Point(coX, coY), y);
						points.insertPoint(new Point(width + offx - x, coY),
								points.size() - y);
						break;
					}
				}
			}
			region.add(points.toIntArray());
		}

		// setze die Region der Controls
		for (ResizeControl control : resizeControls) {
			if (control.isVisible()) {
				region.add(control.getRegion());
			}
		}
		this.regionComp.setRegion(region);
	}

	/**
	 * berechnet die neue Größe eines Objekts, wenn die Controls gezogen werden
	 * 
	 * @param x
	 *            Bewegung in X Richtung
	 * @param y
	 *            Bewegung in Y Richtung
	 */
	public void resizeOF(int x, int y) {
		// wenn aktuell keine Resize Control gewählt ist
		// mach nichts
		
		if (currentRC == C_NO_CONTROL) {
			return;
		}
		
		// prüfe die Bounds
		if (x > figureBounds.width || x < figureBounds.x || y < figureBounds.y
				|| y > figureBounds.height + 2) {
			return;
		}
		
		// berechne die neuen Koordinate bzw. die Höhe oder Weite
		int diffX = x - clickPoint.x;
		int diffY = y - clickPoint.y;
		
		if (pointListShape) {			
			resizeControls[currentRC].translate(diffX, diffY);

			PointList points = new PointList();
			for (ResizeControl rc : this.resizeControls) {
				points.addPoint(rc.getBounds().x + controlWidth / 2,
						rc.getBounds().y + controlHeight / 2);
			}
			((Polyline) contentFigure).setPoints(points);
		} else {
			int actualWidth = contentFigure.getBounds().width;
			int actualHeight = contentFigure.getBounds().height;
			switch (currentRC) {
				case C_TOP:					
					{
						if (keepAspect) {
							int widthChange = (int)Math.round(((actualHeight - diffY) * contentAspectRatio)) - actualWidth;
							resizeControls[C_TOPLEFT].translate(-widthChange, diffY);
						}
						resizeControls[C_TOPLEFT].translate(0, diffY);
					}
					break;
				case C_RIGHT:
					{
						if (keepAspect) {
							int heightChange = (int)Math.round(((actualWidth + diffX) / contentAspectRatio)) - actualHeight;
							resizeControls[C_BOTTOMRIGHT].translate(diffX, heightChange);
						}
						
						resizeControls[C_BOTTOMRIGHT].translate(diffX, 0);
					}
					break;
				case C_BOTTOM:
					{
						if (keepAspect) {
							int widthChange = (int)Math.round(((actualHeight - diffY) * contentAspectRatio)) - actualWidth;
							resizeControls[C_BOTTOMRIGHT].translate(widthChange, diffY);
						}
						resizeControls[C_BOTTOMRIGHT].translate(0, diffY);
					}
					break;
				case C_LEFT:
					{
						if (keepAspect) {
							int heightChange = (int)Math.round(((actualWidth + diffX) / contentAspectRatio)) - actualHeight;
							resizeControls[C_TOPLEFT].translate(diffX, -heightChange);
						}
						resizeControls[C_TOPLEFT].translate(diffX, 0);
					} 
					break;
				case C_TOPRIGHT:					
					{
						if (keepAspect) {
							if ((double)(actualWidth + diffX)/(actualHeight - diffY) <= contentAspectRatio) {
								int widthChange = (int)Math.round(((actualHeight - diffY) * contentAspectRatio)) - actualWidth;
								resizeControls[C_BOTTOMRIGHT].translate(widthChange, 0);
								resizeControls[C_TOPLEFT].translate(0, diffY);
							} else {
								int heightChange = (int)Math.round(((actualWidth + diffX) / contentAspectRatio)) - actualHeight;
								resizeControls[C_BOTTOMRIGHT].translate(diffX, 0);
								resizeControls[C_TOPLEFT].translate(0, -heightChange);
							}
						}
						resizeControls[C_TOPLEFT].translate(0, diffY);
						resizeControls[C_BOTTOMRIGHT].translate(diffX, 0);
					}
					break;
				case C_BOTTOMRIGHT:
					{
						if (keepAspect) {
							if ((double)(actualWidth + diffX)/(actualHeight + diffY) <= contentAspectRatio) {
								int widthChange = (int)Math.round(((actualHeight + diffY) * contentAspectRatio)) - actualWidth;
								resizeControls[C_BOTTOMRIGHT].translate(widthChange, diffY);
							} else {
								int heightChange = (int)Math.round(((actualWidth + diffX) / contentAspectRatio)) - actualHeight;
								resizeControls[C_BOTTOMRIGHT].translate(diffX, heightChange);
							}
						}
						resizeControls[C_BOTTOMRIGHT].translate(diffX, diffY);
					}				
					break;
				case C_BOTTOMLEFT:					
					{
						if (keepAspect) {
							if ((double)(actualWidth - diffX)/(actualHeight + diffY) <= contentAspectRatio) {
								int widthChange = (int)Math.round(((actualHeight + diffY) * contentAspectRatio)) - actualWidth;
								resizeControls[C_BOTTOMRIGHT].translate(0, diffY);
								resizeControls[C_TOPLEFT].translate(-widthChange, 0);
							} else {
								int heightChange = (int)Math.round(((actualWidth - diffX) / contentAspectRatio)) - actualHeight;
								resizeControls[C_BOTTOMRIGHT].translate(0, heightChange);
								resizeControls[C_TOPLEFT].translate(diffX, 0);
							}
						}
						resizeControls[C_TOPLEFT].translate(diffX, 0);
						resizeControls[C_BOTTOMRIGHT].translate(0, diffY);
					}
					break;
				case C_TOPLEFT:		
					{
						if (keepAspect) {
							if ((double)(actualWidth - diffX)/(actualHeight - diffY) <= contentAspectRatio) {
								int widthChange = (int)Math.round(((actualHeight - diffY) * contentAspectRatio)) - actualWidth;
								resizeControls[C_TOPLEFT].translate(-widthChange, diffY);
							} else {
								int heightChange = (int)Math.round(((actualWidth - diffX) / contentAspectRatio)) - actualHeight;
								resizeControls[C_TOPLEFT].translate(diffX, -heightChange);
							}
					}
						resizeControls[C_TOPLEFT].translate(diffX, diffY);	
					}
					break;	
			}
			int newX = resizeControls[C_TOPLEFT].getBounds().x + controlWidth / 2;			
			int newY = resizeControls[C_TOPLEFT].getBounds().y + controlHeight / 2;			
			int newWidth = resizeControls[C_BOTTOMRIGHT].getBounds().x + controlWidth / 2 - newX ;
			int newHeight = resizeControls[C_BOTTOMRIGHT].getBounds().y + controlHeight / 2 - newY;				
			
			// halte die Mindestgröße ein
			if (newWidth < 25 || newHeight < 25 || newWidth + newX - controlWidth / 2 >= figureBounds.width || newHeight + newY - controlHeight / 2 >= figureBounds.height) {
				setRectangleControls();
				return;
			}
			contentFigure.setBounds(new Rectangle(newX, newY, newWidth,
					newHeight));
			setRectangleControls();
		}

		// setze den Bezugspunkt neu
		clickPoint = new Point(x, y);

		// berechne und setze die Region neu
		calculateRegion();
	}
	
	/**
	 * Tries to set the bounds of the content figure to the given values.
	 * @param x new x position of the figure
	 * @param y new y position of the figure
	 * @param width new width of the figure
	 * @param height new height of the figure
	 */
	public void setContentBounds(int x, int y, int width, int height) {
		/* The maximum bounds may not be exceeded. */
		if (x < figureBounds.x || y < figureBounds.y 
						|| x+width > figureBounds.width || y+height > figureBounds.height) {
					return;
		}
		/* PointListShapes cannot be altered meaningfully by setting the bounds
		   of the whole figure. */
		if (pointListShape) {
			return;
		}
		/* Button width and heigth is dependent on button text and must not be
		   altered. */
		if (isButton) {
			width = contentFigure.getBounds().width;
			height = contentFigure.getBounds().height;
		}
		
		contentFigure.setBounds(new Rectangle(x, y, width,
				height));
		
		if (contentFigure instanceof RectangleFigure ||
				contentFigure instanceof Ellipse) {
			setRectangleControls();
		}
		calculateRegion();
	}
	
	public void setAspectRatio(double aspectRatio) {
		this.contentAspectRatio = aspectRatio;
	}
	
	/**
	 * berechnet die Bildgröße neu
	 */
	public void adjustAspect() {
		if (this.pointListShape || this.isButton) {
			return;
		}
		
		int newX = this.contentFigure.getBounds().x;
		int newY = this.contentFigure.getBounds().y;
		
		int newWidth = this.contentFigure.getBounds().width;
		int newHeight = this.contentFigure.getBounds().height;
		
		if (newWidth > newHeight) {
			newWidth = (int) Math.ceil(newHeight * contentAspectRatio);			
		} else {
			newHeight = (int) Math.ceil(newWidth / contentAspectRatio);
		}
				
		if (newX + newWidth > figureBounds.width) {
			newWidth = figureBounds.getBottom().y-4 - newY;
		}
		
		if (newY + newHeight > figureBounds.height) {
			newHeight = figureBounds.getRight().x-4 - newX;
		}
		
		setContentBounds(newX, newY, newWidth, newHeight);
	}

	/**
	 * versetzt die Position der Figure und berechnet die Region entsprechend
	 * neu
	 * 
	 * @param x
	 * @param y
	 */
	public void translateOF(int x, int y) {
		// entferne zunächst die alte Region (vermeidet Verdoppelung und
		// schlieren)
		final int divX = clickPoint.x - x;
		final int divY = clickPoint.y - y;
		
		int correctionX = 0;
		int correctionY = 0;
		
		PointList points = new PointList();
		
		// versetzt die Controls
		for (ResizeControl control : resizeControls) {
			control.translate(-divX, -divY);
			int cx = control.getBounds().x;
			int cy = control.getBounds().y;
			int cw = control.getBounds().width;
			int ch = control.getBounds().height;
			
			// prüfe ob das Control noch innerhalb des erlaubten Breic
			if(cx < -controlWidth / 2){
				correctionX = cx + controlWidth / 2;
			}
			if(cy < -controlWidth / 2){
				correctionY = cy + controlWidth / 2;
			}
			if(cx + cw > figureBounds.width + controlWidth / 2){
				correctionX = cx + cw - figureBounds.width - controlWidth / 2;
			}
			if(cy + ch > figureBounds.height + controlWidth / 2){
				correctionY = cy + ch - figureBounds.height - controlWidth / 2;
			}
		}

		// falls die Grenzen überschritten werden, setze das entsprechende
		// Control zurück
		for (ResizeControl control : resizeControls) {
			control.translate(-correctionX, -correctionY);
			if (pointListShape) {
				points.addPoint(control.getBounds().x + controlWidth / 2,
						control.getBounds().y + controlHeight / 2);
			}
		}
		
		// translate funktioniert nicht auf einem Polygon ... die Methode in
		// der Polygon Klasse wird überschrieben
		// macht aber nichts...
		if (pointListShape) {
			((AbstractPointListShape) contentFigure).setPoints(points);
		} else {
			contentFigure.translate(-divX - correctionX, -divY - correctionY);
		}

		// setze den Bezugspunkt neu
		clickPoint = new Point(x, y);
		region.translate(-divX - correctionX, -divY - correctionY);
		this.regionComp.setRegion(region);
		canvas.redraw();
	}

	/**
	 * liefert die Content Figure
	 * 
	 * @return
	 */
	public Figure getContentFigure() {
		return this.contentFigure;
	}

	/**
	 * liefert die Resize Controls
	 * 
	 * @return
	 */
	public ResizeControl[] getResizeControls() {
		return this.resizeControls;
	}

	/**
	 * Klasse kapselt ein Resize Control
	 * 
	 * @author juhoffma
	 */
	protected class ResizeControl extends RectangleFigure {

		private Region rFRegion;
		private int index;
		private Cursor cursor;

		public ResizeControl(int index) {
			this.index = index;
			OverlayFigure.this.add(this);
			this.setBackgroundColor(Display.getCurrent().getSystemColor(
					SWT.COLOR_WHITE));

			addMouseListener(new MouseListener() {
				@Override
				public void mouseDoubleClicked(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
					// der Klick Punkt in den Resize Controls wird auf die Mitte
					// des Controls gesetzt
					// damit ist es egal wo im Resize Control hingeklickt wurde
					clickPoint = new Point(getLocation().x + controlWidth / 2,
							getLocation().y + controlWidth / 2);
					currentRC = ResizeControl.this.index;
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					currentRC = C_NO_CONTROL;
				}
			});

			addMouseMotionListener(new MouseMotionListener() {
				@Override
				public void mouseDragged(MouseEvent e) {
					if (currentRC != C_NO_CONTROL) {
						resizeOF(e.x, e.y);
					}
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					setCursor(ResizeControl.this.cursor);
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}

				@Override
				public void mouseHover(MouseEvent e) {
				}

				@Override
				public void mouseMoved(MouseEvent e) {
				}
			});
		}

		public void setRCCursor(Cursor cursor) {
			this.cursor = cursor;
		}

		public Region getRegion() {
			if (rFRegion != null) {
				rFRegion.dispose();
			}
			rFRegion = new Region();
			rFRegion.add(getBounds().x + poffX, getBounds().y + poffY,
					getBounds().width, getBounds().height);
			return rFRegion;
		}
	}
}