package org.iviPro.editors.common;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.application.Application;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.editors.scenegraph.SceneGraphEditor;
import org.iviPro.model.ProjectSettings;
import org.iviPro.model.graph.ScreenArea;
import org.iviPro.theme.Colors;

public class ScreenAreaSelector extends SivaComposite {

	private static Logger logger = Logger.getLogger(SceneGraphEditor.class);

	private static final int DEFAULT_WIDTH = 60;
	private static final int DEFAULT_HEIGHT = 45;

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
	public ScreenAreaSelector(final Composite parent, int style,
			final ScreenArea screenArea) {
		super(parent, style);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		setLayoutData(gd);
		this.screenArea = screenArea;
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

		// Listener installieren, der auf Aenderungen in den Projekt-Settings
		// horcht. Ist dort eine Aenderung aufgetreten, zeichnet sich das
		// Control neu, um eventuellen Aenderungen zu entsprechen.
		Application.getCurrentProject().getSettings()
				.addPropertyChangeListener(new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (!isDisposed()) {
							calcRectangles();
							redraw();
							update();
							layout(true);
						}
					}
				});

		// Paint-Listener zum Zeichnen des Controls.
		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				paint(e);
			}

		});

		// Mouse-Listener, fuer das wechseln der ausgewaehlten ScreenArea
		// per Mausklick auf eine andere ScreenArea.
		addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				int x = e.x;
				int y = e.y;
				setScreenArea(x, y);
			}
		});

	}
	
	/**
	 * berechnet anhand der Settings eine verwendbare ScreenArea Position
	 * @param annotation
	 * @return
	 */
	public static ScreenArea getFreeScreenArea() {

		ProjectSettings settings = Application.getCurrentProject().getSettings();
		ScreenArea usableScreenArea = ScreenArea.TOP;
		
		if (settings.getAreaTopHeight() > 0 && settings.getAreaCenterWidth() > 0) {
			usableScreenArea = ScreenArea.TOP;
		} else
		if (settings.getAreaRightWidth() > 0 && settings.getAreaCenterHeight() > 0) {
			usableScreenArea = ScreenArea.RIGHT;
		} else
		if (settings.getAreaBottomHeight() > 0 && settings.getAreaCenterWidth() > 0) {
			usableScreenArea = ScreenArea.BOTTOM;
		} else 
		if (settings.getAreaLeftWidth() > 0 && settings.getAreaCenterHeight() > 0) {
			usableScreenArea = ScreenArea.LEFT;
		} else {
			usableScreenArea = ScreenArea.OVERLAY;
		}
		return usableScreenArea;
	}
	
	public static boolean checkScreenArea(ScreenArea screenArea) {				
		ProjectSettings settings = Application.getCurrentProject().getSettings();
		
		switch (screenArea) {
			case TOP: return settings.getAreaTopHeight() > 0 && settings.getAreaCenterWidth() > 0;
			case RIGHT: return settings.getAreaRightWidth() > 0 && settings.getAreaCenterHeight() > 0;
			case BOTTOM: return settings.getAreaBottomHeight() > 0 && settings.getAreaCenterWidth() > 0;
			case LEFT: return settings.getAreaLeftWidth() > 0 && settings.getAreaCenterHeight() > 0;
			case OVERLAY: return settings.getAreaCenterHeight() > 0 && settings.getAreaCenterWidth() > 0;			
		}
		return false;
	}

	/**
	 * Zeichnet das Control.
	 * 
	 * @param e
	 *            Der Paint-Event
	 */
	private void paint(PaintEvent e) {
		if (isDisposed()) {
			return;
		}

		// // zeichne die Umrisse
		e.gc.drawRectangle(aLeft);
		e.gc.drawRectangle(aRight);
		e.gc.drawRectangle(aTop);
		e.gc.drawRectangle(aBottom);
		// e.gc.drawRectangle(aCenter);

		// zeichne das markierte Rechteck
		e.gc.setAlpha(80);
		e.gc.setBackground(Colors.ANNOTATIONEDITOR_POSSELECTOR_BG_SELECTED
				.getColor());
		switch (screenArea) {
		case LEFT:
			e.gc.fillRectangle(aLeft);
			break;
		case TOP:
			e.gc.fillRectangle(aTop);
			break;
		case RIGHT:
			e.gc.fillRectangle(aRight);
			break;
		case BOTTOM:
			e.gc.fillRectangle(aBottom);
			break;
		case OVERLAY:
			e.gc.fillRectangle(aCenter);
			break;
		}		
		e.gc.dispose();
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
		Dimension controlSize = new Dimension(getSize().x - 1, getSize().y - 1);
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
		aLeft = new Rectangle(offset.x, offset.y, 0, 0);
		aTop = new Rectangle(offset.x, offset.y, 0, 0);
		aRight = new Rectangle(offset.x, offset.y, 0, 0);
		aBottom = new Rectangle(offset.x, offset.y, 0, 0);
		aCenter = new Rectangle(offset.x, offset.y, 0, 0);

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
	 * Berechnet welche ScreenArea der Benutzer durch einen Mausklick ausgewählt
	 * hat und setzt diese entsprechend.
	 * 
	 * @param x
	 *            Die X-Koordinate des Mausklicks im Control
	 * @param y
	 *            Die Y-Koordinate des Mausklicks im Control
	 */
	private void setScreenArea(int x, int y) {
		boolean set = false;
		if (aLeft.contains(x, y)) {
			screenArea = ScreenArea.LEFT;
			set = true;
		} else

		if (aTop.contains(x, y)) {
			screenArea = ScreenArea.TOP;
			set = true;
		} else

		if (aRight.contains(x, y)) {
			screenArea = ScreenArea.RIGHT;
			set = true;
		} else

		if (aBottom.contains(x, y)) {
			screenArea = ScreenArea.BOTTOM;
			set = true;
		} else

		if (aCenter.contains(x, y)) {
			screenArea = ScreenArea.OVERLAY;
			set = true;
		}
		if (set) {
			if (!isDisposed()) {
				redraw();
				update();
				SivaEvent event = new SivaEvent(null, 
						SivaEventType.SCREEN_AREA_CHANGED, screenArea);
				notifySivaEventConsumers(event);
			}
		}
	}
	
	/**
	 * erlaubt das externe setzen der aktuellen Anzeigeposition
	 * @param screenArea
	 */
	public void setScreenArea(ScreenArea screenArea) {
		this.screenArea = screenArea;
		update();
		redraw();
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

	/*
	 * Ueberschrieben, damit bei Groessen-Aenderungen das Control neu gezeichnet
	 * wird.
	 * 
	 * @see org.eclipse.swt.widgets.Control#setBounds(int, int, int, int)
	 */
	@Override
	public void setBounds(int x, int y, int width, int height) {
		logger.debug("setBounds: " + width + "x" + height); //$NON-NLS-1$ //$NON-NLS-2$
		super.setBounds(x, y, width, height);
		if (!isDisposed()) {
			calcRectangles();
			redraw();
			update();
			layout(true);
		}
	}

}
