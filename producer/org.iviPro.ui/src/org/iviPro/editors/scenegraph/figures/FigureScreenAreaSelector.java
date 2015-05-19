package org.iviPro.editors.scenegraph.figures;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
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
	public Font classFont = new Font(null, "Arial", 10, SWT.BOLD); //$NON-NLS-1$

	/** Das aktuelle Screen-Area Objekt das im Selektor angezeigt werden soll */
	private ScreenArea screenArea;

	/*
	 * Die Rechtecke kapseln die Position und Groesse der zu zeichnenden
	 * Rechtecke die den einzelnen Areas in der Miniatur-Ansicht entsprechen.
	 */
	private Rectangle aRight;
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
		path.addRectangle(aRight.x, aRight.y, aRight.width, aRight.height);
		path.addRectangle(aCenter.x, aCenter.y, aCenter.width, aCenter.height);
		g.setClip(path);
		
		//speichere Rechtecke für das Füllen
		Rectangle aRightFill = new Rectangle(aRight.x-1, aRight.y, aRight.width, aRight.height);
		Rectangle aCenterFill = new Rectangle(aCenter.x-1, aCenter.y, aCenter.width+1, aCenter.height+1);
		
		
		//Anpassung aufgrund des "Border-Problems"
		//(immer -1 bei width und height sonst wird der Rand nicht gezeichnet)
		aRight.width = aRight.width - 1;
		aRight.height = aRight.height - 1;
		
		aCenter.x = aCenter.x - 1;
		aRight.x = aRight.x - 1;
		
		//Zeichne Rahmen der Rechtecke
		g.drawRectangle(aRight);
		g.drawRectangle(aCenter);
		
		g.setAlpha(80);
		g.setBackgroundColor(Colors.ANNOTATIONEDITOR_POSSELECTOR_BG_SELECTED
				.getColor());
		switch (screenArea) {
		case RIGHT:
			g.fillRectangle(aRightFill);
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
		Dimension playerSize = new Dimension(settings.getResolutionWidth(), settings
				.getResolutionHeight());
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
		aRight = new Rectangle(bounds.x, bounds.y, 0, 0);
		aCenter = new Rectangle(bounds.x, bounds.y, 0, 0);

		// Dann berechnen wir die Breiten/Hoehen der auesseren Rechtecke
		// und leiten daraus dann die verbleibenden Groessen ab.
		// So verhindern wir, dass durch Rundungsfehler z.B. die Rechtecke
		// Top/Center/bottom z.B. hoeher sind, als Left/Right.
		aRight.width = Math.round(selectorSize.width
				* settings.getAnnotationBarWidth());

		// Diese Groessen koennen wir nun aus den bereits bekannten ableiten
		// und somit ein konsistentes Gesamtbild erhalten
		aCenter.height = selectorSize.height;
		aRight.height = selectorSize.height;
		aCenter.width = selectorSize.width - aRight.width;

		// Nachdem wir die Groessen der Rechtecke errechnet haben, sind nun
		// die Positionen dran.
		aRight.x = aCenter.x + aCenter.width;
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
