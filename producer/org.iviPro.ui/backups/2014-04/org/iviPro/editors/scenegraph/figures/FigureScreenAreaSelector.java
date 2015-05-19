package org.iviPro.editors.scenegraph.figures;

import org.eclipse.draw2d.geometry.Dimension;
import org.apache.log4j.Logger;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.iviPro.application.Application;
import org.iviPro.editors.scenegraph.SceneGraphEditor;
import org.iviPro.model.ProjectSettings;
import org.iviPro.model.graph.ScreenArea;
import org.iviPro.theme.Colors;

public class FigureScreenAreaSelector extends IFigureNode {

	private static Logger logger = Logger.getLogger(SceneGraphEditor.class);

	private static final int DEFAULT_WIDTH = 21;
	private static final int DEFAULT_HEIGHT = 16;
	
	
	private static final Color BLACK = new Color(Display.getDefault(),
			0, 0, 0);
	public static Color classColor = new Color(null,240,240,240);
	public static Color BORDERCOLOR = new Color(null,180,180,180);
	public Font classFont = new Font(null, "Arial", 10, SWT.BOLD);

	/** Das aktuelle Screen-Area Objekt das im Selektor angezeigt werden soll */
	private ScreenArea screenArea;

	/*
	 * Die Rechtecke kapseln die Position und Groesse der zu zeichnenden
	 * Rechtecke die den einzelnen Areas in der Miniatur-Ansicht entsprechen.
	 */
	private Rectangle aLeft;
	private Rectangle aTop;
	private Rectangle aRight;
	private Rectangle aBottom;
	private Rectangle aCenter;

	/**
	 * Erstellt ein neues Control zum Anzeigen und Editieren einer ScreenArea.
	 * 
	 * @param parent
	 *            Parent-Control
	 * @param style
	 *            SWT-Style
	 * @param screenArea
	 *            Die Screen-Area die angezeigt oder editiert werden soll.
	 */
	public FigureScreenAreaSelector(Point position, final ScreenArea screenArea) {
		//super();
		this.screenArea = screenArea;
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
		setSize(new Dimension(21,16));
		setBorder(new LineBorder(1));
		setBackgroundColor(classColor);
		setOpaque(true);
		setLocation(position);
	}
	
	
	@Override
	public void paint(Graphics g) {

		//Berechne Gitterlayout
		calcRectangles();
		
		g.setForegroundColor(BLACK);
		AdvancedPath path = new AdvancedPath();
		path.addRectangle(aLeft.x, aLeft.y, aLeft.width, aLeft.height);
		path.addRectangle(aRight.x, aRight.y, aRight.width, aRight.height);
		path.addRectangle(aTop.x, aTop.y, aTop.width, aTop.height);
		path.addRectangle(aBottom.x, aBottom.y, aBottom.width, aBottom.height);
		path.addRectangle(aCenter.x, aCenter.y, aCenter.width, aCenter.height);
		g.setClip(path);
		
		//speichere Rechtecke für das Füllen
		Rectangle aLeftFill = new Rectangle(aLeft.x, aLeft.y, aLeft.width, aLeft.height);
		Rectangle aRightFill = new Rectangle(aRight.x-1, aRight.y, aRight.width, aRight.height);
		Rectangle aTopFill = new Rectangle(aTop.x-1, aTop.y, aTop.width+1, aTop.height+1);
		Rectangle aBottomFill = new Rectangle(aBottom.x-1, aBottom.y, aBottom.width+1, aBottom.height);
		Rectangle aCenterFill = new Rectangle(aCenter.x-1, aCenter.y, aCenter.width+1, aCenter.height+1);
		
		
		//Anpassung aufgrund des "Border-Problems"
		//(immer -1 bei width und height sonst wird der Rand nicht gezeichnet)
		aLeft.height = aLeft.height - 1;
		aLeft.width = aLeft.width - 1;
		aBottom.height = aBottom.height - 1;
		aRight.width = aRight.width - 1;
		aRight.height = aRight.height - 1;
		
		aTop.x = aTop.x - 1;
		aCenter.x = aCenter.x - 1;
		aBottom.x = aBottom.x - 1;
		aRight.x = aRight.x - 1;
		
		//Zeichne Rahmen der Rechtecke
		g.drawRectangle(aLeft);
		g.drawRectangle(aRight);
		g.drawRectangle(aTop);
		g.drawRectangle(aBottom);
		g.drawRectangle(aCenter);
		
		g.setAlpha(80);
		g.setBackgroundColor(Colors.ANNOTATIONEDITOR_POSSELECTOR_BG_SELECTED
				.getColor());
		switch (screenArea) {
		case LEFT:
			g.fillRectangle(aLeftFill);
			break;
		case TOP:
			g.fillRectangle(aTopFill);
			break;
		case RIGHT:
			g.fillRectangle(aRightFill);
			break;
		case BOTTOM:
			g.fillRectangle(aBottomFill);
			break;
		case OVERLAY:
			g.fillRectangle(aCenterFill);
			break;
		}
	}

	/**
	 * Berrechnet die Groesse der zu zeichnenden Rechtecke fuer die einzelnen
	 * Screenareas entsprechend der aktuellen Control-Groesse und der
	 * Einstellungen in den Project-Settings.
	 */
	private void calcRectangles() {

		// Settings holen
		ProjectSettings settings = Application.getCurrentProject()
				.getSettings();

		// Control-Size: Groesse dieses SWT-Controls
		// = maximale Zeichenflaeche (1px kleiner, weil sonst hinaus gezeichnet
		// wuerde.
		Dimension controlSize = new Dimension(DEFAULT_WIDTH - 1, DEFAULT_HEIGHT - 1);
		float controlAspect = controlSize.width * 1f / controlSize.height;

		// Player-Size: Groesse des Flash-Players wie in den Settings angegeben
		// => Im selben Seitenverhaeltnis soll der ScreenAreaSelector
		// . .gezeichnet werden
		Dimension playerSize = new Dimension(settings.getSizeWidth(), settings
				.getSizeHeight());
		float playerAspect = playerSize.width * 1f / playerSize.height;

		// =====================================================================
		// In der Regel werden das Seitenverhaeltnis des SWT-Controls und des
		// Flash-Players nicht ueberein stimmen.
		// 
		// SWT-Control Size. . . . . . Flash-Player Size
		// z.B. 60x60px. . . . . . . . z.B. 800x450px
		// => Aspect 1:1 . . . . . . . => Aspect 16:9
		// ._____________. . . . . . .___________________
		// | . . . . . . | . . . . . | . . . . . . . . . |
		// | . . . . . . | . . . . . | . . . . . . . . . |
		// | . . . . . . | . . . . . | . . . . . . . . . |
		// | . . . . . . | . . . . . | . . . . . . . . . |
		// | . . . . . . | . . . . . | . . . . . . . . . |
		// |_____________| . . . . . |___________________|
		//
		// Der Miniatur-Selektor muss also in die Control-Size so eingepasst
		// werden, dass er entweder Breite oder Hoehe komplett ausfuellt und
		// die gleiche Aspect-Ratio wie der Player hat. Oben oder links waere
		// dann je nachdem Abstand zum Rand des SWT-Controls.
		//
		// Beispiel (wenn PlayerAspect groesser als ControlAspect)
		// ._____________
		// |_____________| <- Offset-Y
		// | . . . . . . |
		// | . . . . . . | <- Selector fuer die ScreenArea (-> SelectorSize s.u)
		// | . . . . . . | . (hat die gleiche Aspect-Ratio wie Player)
		// |_____________|
		// |_____________| <- Offset-Y
		//
		// Wenn PlayerAspect dagegen kleiner als ControlAspect, dann muss
		// entsprechend links und rechts ein Offset gezeichnet werden und
		// der Minitatur-Selektor fuellt die gesamte Hoehe aus.
		// =====================================================================

		// Offset.X und Offset.Y beschreiben den Abstand der Selektor-Position
		// von der linken oberen Ecke des Controls (siehe Zeichnung oben).
		// Das Offset ist 0, wenn Aspect-Ratio von Control und Player gleich
		// sind, ansonsten ist einer von ihnen ungleich 0. Dieser Wert wird
		// unten dann anhand eines Vergleichs der Aspect-Ratios ermittelt
		Point offset = new Point(0, 0);

		// Selector-Size gibt die Groesse des Miniatur-Selektors an.
		// Wir initialisieren mit der Control-Groesse und verkleinern je nach
		// AspectRatio die Hoehe oder Breite
		Dimension selectorSize = new Dimension(controlSize);
		if (playerAspect > controlAspect) {
			// Player hat eine laenglichere Form als Control
			// => Selector ist niedriger als Control und oben/unten ist Abstand
			selectorSize.height = Math.round(controlSize.width / playerAspect);
			offset.y = (controlSize.height - selectorSize.height) / 2;

		} else if (playerAspect < controlAspect) {
			// Control hat eine laenglichere Form als Player
			// => Selector ist schmaeler als Control und links/rechts ist Absta.
			selectorSize.width = Math.round(controlSize.height * playerAspect);
			offset.x = (controlSize.width - selectorSize.width) / 2;
		}

		// Jetzt wissen wir also wie groß wir den Selektor zeichnen müssen und
		// wieviel Offset wir vom Rand haben.
		// 
		// => Wir berechnen nun die Groesse und Positions der einzelnen
		// Rechtecke fuer die ScreenAreas im Selektor.

		// Der Selektor mit seinen einzelnen Rechtecken:
		// . . ._____________________
		// . . | . | . TOP . . . | . |
		// . . | L |_____________| R |
		// . . | E | . . . . . . | I |
		// . . | F | . CENTER. . | G |
		// . . | T |_____________| H |
		// . . | . | . BOTTOM. . | T |
		// . . |___|_____________|___|
		//
		// Zuerst initialisieren wir erstmal alle Rechtecke mit den Offsets
		// dann brauchen wir diese spaeter schon mal nicht mehr beruecksichtigen
		aLeft = new Rectangle(bounds.x, bounds.y, 0, 0);
		aTop = new Rectangle(bounds.x, bounds.y, 0, 0);
		aRight = new Rectangle(bounds.x, bounds.y, 0, 0);
		aBottom = new Rectangle(bounds.x, bounds.y, 0, 0);
		aCenter = new Rectangle(bounds.x, bounds.y, 0, 0);

		// Dann berechnen wir die Breiten/Hoehen der auesseren Rechtecke
		// und leiten daraus dann die verbleibenden Groessen ab.
		// So verhindern wir, dass durch Rundungsfehler z.B. die Rechtecke
		// Top/Center/bottom z.B. hoeher sind, als Left/Right.
		aLeft.width = Math.round(selectorSize.width
				* settings.getAreaLeftWidth());
		aRight.width = Math.round(selectorSize.width
				* settings.getAreaRightWidth());
		aTop.height = Math.round(selectorSize.height
				* settings.getAreaTopHeight());
		aBottom.height = Math.round(selectorSize.width
				* settings.getAreaBottomHeight());

		// Diese Groessen koennen wir nun aus den bereits bekannten ableiten
		// und somit ein konsistentes Gesamtbild erhalten
		aCenter.height = selectorSize.height - aTop.height - aBottom.height;
		aLeft.height = selectorSize.height;
		aRight.height = aLeft.height;
		aCenter.width = selectorSize.width - aLeft.width - aRight.width;
		aTop.width = aCenter.width;
		aBottom.width = aCenter.width;

		// Nachdem wir die Groessen der Rechtecke errechnet haben, sind nun
		// die Positionen dran.
		aTop.x = aLeft.x + aLeft.width;
		aRight.x = aTop.x + aTop.width;
		aCenter.x = aTop.x;
		aCenter.y = aTop.y + aTop.height;
		aBottom.x = aTop.x;
		aBottom.y = aCenter.y + aCenter.height;
	}
	
	/**
	 * erlaubt das externe setzen der aktuellen Anzeigeposition
	 * @param screenArea
	 */
	public void setScreenArea(ScreenArea screenArea) {
		this.screenArea = screenArea;
		repaint();
	}

	/**
	 * Gibt die Screen-Area zurueck, die von diesem ScreenArea-Selector
	 * dargestellt wird.
	 * 
	 * @return
	 */
	public ScreenArea getScreenArea() {
		return this.screenArea;
	}
}
