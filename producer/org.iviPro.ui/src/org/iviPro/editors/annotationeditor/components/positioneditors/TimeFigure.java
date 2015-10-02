package org.iviPro.editors.annotationeditor.components.positioneditors;

import org.eclipse.draw2d.AbstractPointListShape;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Containerklasse zum Binden einer Figure an einen Zeitpunkt
 * Die Klasse speichert eine Kopie der übergebenen Figure (der ContentFigure)
 * Es kann aber auch die OverlayFigure direkt übergeben werden, die copy Methode
 * holt dann automatisch die ContentFigure
 * @author juhoffma
 */
public class TimeFigure implements Comparable {

	// die Figure der aktuellen Position
	private Figure figure;
	
	/**
	 * Absolute time in video where this figure occurs.
	 */
	private long time;
	
	// Rechteck zum Speichern der Koordinaten des Markierungspunkts 
	// in der Skala
	private Rectangle markPosition;
			
	/**
	 * Constructs a <code>TimeFigure</code> containing the given figure and
	 * time. The time has to be an absolute time with regard to the underlying
	 * media object.
	 * @param newFigure figure to store in this object
	 * @param time absolute time value
	 */
	public TimeFigure(final Figure newFigure, final long time) {
		// falls eine Overlayfigur übergeben wird, kopiere die ContentFigure
		this.figure = OverlayFactory.getInstance().getCopy(newFigure);			
		this.time = time;		
	}
	
	public void setMarkPosition(Rectangle markPosition) {
		this.markPosition = markPosition;
	}
	
	public Rectangle getMarkPosition() {
		return this.markPosition;
	}
	
	public Figure getFigure() {
		return this.figure;
	}
	
	/**
	 * Returns the absolute time in the video where this figure occurs.
	 * @return absolute time in video
	 */
	public long getTime() {
		return this.time;
	}
	
	/**
	 * prüft ob die gespeicherte Figure gleich ist
	 * @param figure die Vergleichs TimeFigure
	 * @return
	 */
	public boolean equalFigure(TimeFigure timeFigure) {
		Figure compareFigure = timeFigure.getFigure();
		if (figure instanceof AbstractPointListShape && compareFigure instanceof AbstractPointListShape) {
			AbstractPointListShape fig1 = (AbstractPointListShape) figure;
			AbstractPointListShape fig2 = (AbstractPointListShape) compareFigure;
			// falls die Bounds nicht übereinstimmen können die Figures nicht gleich sein
			// falls ja müssen die einzelnen Punkte überprüft werden
			if (!fig1.getBounds().equals(fig2.getBounds())) {
				return false;
			} else {
				if (fig1.getPoints().size() != fig2.getPoints().size()) {
					return false;
				}
				for (int i=0; i < fig1.getPoints().size(); i++) {
					Point p1 = fig1.getPoints().getPoint(i);
					Point p2 = fig2.getPoints().getPoint(i);
					if (p1.equals(p2)) {						
						return false;
					}					
				}
				return true;
			}	
		} else {
			return figure.getBounds().equals(compareFigure.getBounds());
		}
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof TimeFigure) {
			TimeFigure compFig = (TimeFigure) o;
			if (time == compFig.getTime()) {
				return 0;
			}
			return time > compFig.getTime() ? 1 : -1;
		}
		return 0;
	}
}
