package org.iviPro.editors.sceneeditor.components.overview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.actions.undoable.SceneDeleteAction;
import org.iviPro.application.Application;
import org.iviPro.mediaaccess.player.I_MediaPlayer;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;
import org.iviPro.theme.Colors;
import org.iviPro.utils.SivaTime;

/*
 * zeichnet eine einzelne Szene
 */
public class SingleSceneBar extends Composite implements PaintListener {

	private int[] points = new int[2];

	private int width = 0;

	// die Höhe eines Szenebalkens
	private int height = 17;
	private I_MediaPlayer mp = null;

	// Breite und Höhe des Start und Endbildes
	private int widthImg = 50;
	private int heightImg = 40;

	// die Start- und Endzeit einer Szene
	private SivaTime startTime = new SivaTime(0);
	private SivaTime endTime = new SivaTime(0);

	// das zugehörige Szenenobjekt
	private Scene scene = null;

	// die aktuell selektierte Szene
	private Scene selectedScene = null;

	// der Szenenname
	private String sceneName = ""; //$NON-NLS-1$

	private SingleSceneBar ssb = this;

	// gibt an ob das PopUp angezeigt werden soll.
	// es wird im Compact Modus angezeigt und im Detailmodus, falls
	// die Szene zu kurz ist um alle Details anzeigen zu können
	private boolean showPopUp = true;

	// gibt an ob die Maus aktuell über der SingleSceneBar ist
	private boolean marked = false;

	// Mindestlänge der SingleSceneBar
	private int minWidth = 15;

	// Zeigt das Start und Endbild der Szene an
	private SceneSEImage sceneStartEndImg = null;

	/**
	 * Konstruktor wird zum Zeichnen der bereits vorhandenen Szenen verwendet ==
	 * Szenenübersicht, eine Szene kann angeklickt werden. Single-Click ->
	 * Springe zur Stelle im Video, Doppelklick -> spiele die Szene ab.
	 */
	public SingleSceneBar(Composite parent, int style,
			final ScenesOverview overview, final Scene scene, final I_MediaPlayer mp, int width) {
		super(parent, style);
		
		this.mp = mp;
		this.width = width;
		this.scene = scene;
		
		if (overview.getSelection() instanceof StructuredSelection) {
			this.selectedScene = (Scene) ((StructuredSelection) overview
					.getSelection()).getFirstElement();
		}
		
		addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {		
				if (arg0.keyCode == SWT.DEL) {
					new SceneDeleteAction(scene).run();
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}			
		});

		// Listener der auf einen Klick, ein SelectionChangedEvent wirft
		// meldet dem ScenesOverview, dass diese Szene selektiert wurde
		addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {				
				StructuredSelection selection = new StructuredSelection(scene);
				overview.setSelection(selection);
				
				// rechte Maustaste => öffne Menü, mit Löschmöglichkeit für die
				// Annotation
				MenuManager menuManager = new MenuManager();
				menuManager.add(new SceneDeleteAction(scene));
				SingleSceneBar.this.setMenu(menuManager
						.createContextMenu(SingleSceneBar.this));
				SingleSceneBar.this.setFocus();
			}
		});

		// setze die Start und Endzeit der Szene
		this.startTime = new SivaTime(scene.getStart() - mp.getStartTime().getNano());
		this.endTime = new SivaTime(scene.getEnd() - mp.getStartTime().getNano());

		this.sceneName = scene.getTitle(Application.getCurrentLanguage());
		calcPaintStartEnd();

		GridData ssBGrid = new GridData();
		ssBGrid.widthHint = this.width;
		ssBGrid.heightHint = this.height;
		setLayoutData(ssBGrid);

		GridLayout gl = new GridLayout(1, false);
		setLayout(gl);

		// füge die Start und Endbilder ein
		if (mp.getMediaObject() instanceof Video) {
			Video vid = (Video) mp.getMediaObject();
			sceneStartEndImg = new SceneSEImage(this, SWT.CENTER, startTime.addTime(mp.getStartTime()), endTime.addTime(mp.getStartTime()), vid, widthImg, heightImg, scene);
		}

		// setzt die Listener, Actions etc
		setEvents();

		// hört darauf ob sich in der Szene etwas geändert hat,
		// das passiert nur wenn die Szene gespeichert wurde
		// dann wird die Bar neu gezeichnet
		scene.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (isDisposed()) {
					return;
				}
				// Datei wurde gespeichert
				sceneName = scene.getTitle();
				startTime = new SivaTime(scene.getStart() - mp.getStartTime().getNano());
				endTime = new SivaTime(scene.getEnd() - mp.getStartTime().getNano());
				calcPaintStartEnd();
				pack(true);
				
				// selektierte die Szene, die geändert wurde
				StructuredSelection selection = new StructuredSelection(scene);
				overview.setSelection(selection);
			}
			
		});
		addPaintListener(this);

		addListener(SWT.Dispose, new Listener() {
			@Override
			public void handleEvent(Event event) {
				SingleSceneBar.this.removePaintListener(SingleSceneBar.this);
			}
		});
		
		// die SingleSceneBar hört auf den Overview ob eine andere Szene
		// ausgewählt wurde
		overview.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// falls die Bar nicht mehr existiert
				if (isDisposed()) {
					return;
				}
				StructuredSelection curSelection;
				if (event.getSelection() instanceof StructuredSelection) {
					curSelection = (StructuredSelection) event.getSelection();
					if (curSelection.getFirstElement() != null) {
						if (curSelection.getFirstElement() instanceof Scene) {
							selectedScene = (Scene) ((StructuredSelection) event
								.getSelection()).getFirstElement();
							redraw();
							update();
						}
					} else {
						selectedScene = null;
						redraw();
						update();
					}
				}
			}			
		});
	}

	/**
	 * setzt die Actions/Listener/Events die auf dem Szenen Balken gebraucht
	 * werden
	 * 
	 * @param startTime
	 * @param endTime
	 */
	private void setEvents() {

		/*
		 * geht man über die Szene wird ein PopUp angezeigt
		 */
		this.addListener(SWT.MouseEnter, new Listener() {
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				marked = true;
				redraw();
				update();

				if (showPopUp) {
					// die y Position des Events wird mitgeben, damit das Popup
					// unabhängig
					// von der Eintrittsposition der Maus positioniert werden
					// kann.
					CustomPopup.getInstance(scene, event.y
							- SingleSceneBar.this.height);
				} else {
					CustomPopup.disposePop();
				}
			}
		});

		/*
		 * verlässt man die Szene wird das Popup verworfen
		 */
		this.addListener(SWT.MouseExit, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!ssb.isDisposed()) {
					marked = false;
					redraw();
					update();
					if (event.x <= 0 || event.x >= points[1] || event.y <= 0
							|| event.y >= height) {
						if (showPopUp) {
							CustomPopup.disposePop();
						}
					}
				}
			}
		});

		this.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {
				mp.playFromTo(startTime, endTime);
			}

			public void mouseDown(MouseEvent e) {
			}

			public void mouseUp(MouseEvent e) {
			}
		});
	}

	/*
	 * die Funktion berechnet den Startpunkt und die Länge der Szene, die
	 * gezeichnet werden soll.
	 */
	private void calcPaintStartEnd() {

		// prozentualer Anteil einer Nanosekunde
		double percentNano = 100 / (double) mp.getDuration().getNano();

		// prozentualer Anteil der Start bzw. Endzeit
		double percentStart = (percentNano * startTime.getNano());
		double percentEnd = (percentNano * endTime.getNano());

		// 1 Prozent der Breite
		double percentWidth = (double) width / 100;
		points[0] = (int) (percentWidth * percentStart);
		int newwidth = ((int) (percentWidth * percentEnd)) - points[0];
		if (newwidth < this.minWidth) {
			newwidth = this.minWidth;
		}
		points[1] = newwidth;
	}

	@Override
	public void paintControl(PaintEvent e) {

		int textOffsetX = widthImg * 2 + 30;

		// setze die Texte die gezeichnet werden sollen
		String desc = sceneName;

		// zeichne die SingleSceneBar Fläche entsprechend dem RoundRectangle
		int y = this.getBounds().y;
		setBounds(points[0], y, points[1], height);

		e.gc.setBackground(Colors.VIDEO_OVERVIEW_ITEM_BG.getColor());

		// falls diese Annotation gerade gewählt ist setzte sie auf aktiv
		if ((selectedScene != null 
				&& selectedScene.equals(scene)) || marked) {
			e.gc.setBackground(Colors.VIDEO_OVERVIEW_ITEM_BG_SELECTED
					.getColor());
		}
	
		// bei Unterschreitung der Mindestgröße wird ein Kreis gezeichnet
		if (points[1] <= this.minWidth) {
			e.gc.fillOval(0, 0, minWidth, minWidth);
			e.gc.setForeground(Colors.VIDEO_OVERVIEW_ITEM_BORDER.getColor());
			e.gc.drawOval(0, 0, minWidth - 1, minWidth - 1);
		} else {
			e.gc.fillRoundRectangle(0, 0, points[1], height, 10, 10);
			e.gc.setForeground(Colors.VIDEO_OVERVIEW_ITEM_BORDER.getColor());
			e.gc.drawRoundRectangle(0, 0, points[1] - 1, height - 1, 10, 10);
		}
		
		// y-Position des Beschreibungstextes
		int posDescY = 1;
		
		// die Zeit + Bild wird nur im Detailmodus gezeichnet <- not relevant anymore?!
		sceneStartEndImg.setVisible(false);
		textOffsetX = 5;
		setToolTipText(""); //$NON-NLS-1$

		e.gc.setForeground(Colors.VIDEO_OVERVIEW_ITEM_FONT.getColor());
		String adesc = adjustDescriptionText(desc, textOffsetX, 5, e.gc, points[1], minWidth);
		e.gc.drawText(adesc, textOffsetX, posDescY);	
		e.gc.dispose();
	}
	
	private String adjustDescriptionText(String str, int offsetXL, int offsetXR, GC gc, int paintWidth, int minPaintWidth) {
		String description = ""; //$NON-NLS-1$
						
		// der Text wird nur gesetzt wenn die Mindestgröße der Annotation Bar gegeben ist 
		if (paintWidth > minPaintWidth) {
			
			// Länge des Textes wenn er gezeichnet wird + 2 mal das Offset als Platzhalter links und rechts
			int textDrawLength = gc.textExtent(str).x + offsetXL + offsetXR;
			
			// falls der Platz ausreicht, muss nichts gemacht werden
			if (textDrawLength < paintWidth) {
				return str;
			}
			
			// StringBuffer mit jeweils der halben Beschreibung
			StringBuffer strBuf1 = new StringBuffer(str.substring(0, str.length()/2));			
			StringBuffer strBuf2 = new StringBuffer(str.substring(str.length()/2));
			// gibt an ob der String passt
			boolean strFits = false;
			while (!strFits) {
				description = strBuf1.toString() + " ... " + strBuf2.toString(); //$NON-NLS-1$
				if (gc.textExtent(description).x + offsetXL + offsetXR < paintWidth) {
					strFits = true;
				}
				// wenn beide Puffer leer laufen passt der String nicht rein => zeichne "..."
				if (strBuf1.length() == 0 && strBuf2.length() == 0) {
					return "..."; //$NON-NLS-1$
				}
				if (strBuf1.length() > 0) {
					strBuf1.deleteCharAt(strBuf1.length()-1);
				}
				if (strBuf2.length() > 0) {
					strBuf2.deleteCharAt(0);
				}				
			}
		}
		return description;
	}
}
