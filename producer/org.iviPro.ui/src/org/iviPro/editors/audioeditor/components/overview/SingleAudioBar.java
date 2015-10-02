package org.iviPro.editors.audioeditor.components.overview;

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
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.actions.undoable.MediaDeleteAction;
import org.iviPro.application.Application;
import org.iviPro.mediaaccess.player.MediaPlayer;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.theme.Colors;
import org.iviPro.utils.SivaTime;

/*
 * zeichnet einen einzelnen Audio-Part
 */
public class SingleAudioBar extends Composite implements PaintListener {

	private int[] points = new int[2];

	private int width = 0;

	// die Höhe eines Audio-Part Balken
	private int height = 17;
	private MediaPlayer mp = null;

	// die Start- und Endzeit eines Audio-Part
	private SivaTime startTime = new SivaTime(0);
	private SivaTime endTime = new SivaTime(0);

	// das zugehörige Audio-Part Objekt
	private AudioPart audioPart = null;

	// der aktuell selektierte Audiopart
	private AudioPart selectedAudioPart = null;

	// der Audio-Part Name
	private String audioPartName = ""; //$NON-NLS-1$

	// gibt an ob die Maus aktuell über der SingleAudioBar ist
	private boolean hover = false;

	// Mindestlänge der SingleAudioBar
	private int minWidth = 15;

	/**
	 * Konstruktor wird zum Zeichnen der bereits vorhandenen Audio-Parts verwendet ==
	 * Audioübersicht, ein Audio-Part kann angeklickt werden. Single-Click ->
	 * Springe zur Stelle im AudioFile, Doppelklick -> spiele den Audio-Part ab.
	 */
	public SingleAudioBar(Composite parent, int style, final AudioPartOverview overview, 
			final AudioPart audioPart, final MediaPlayer mp, int width) {
		super(parent, style);
		
		this.mp = mp;
		this.width = width;
		this.audioPart = audioPart;
		
		if (overview.getSelection() instanceof StructuredSelection) {
			if (((StructuredSelection) overview.getSelection()).getFirstElement() instanceof AudioPart) {
				this.selectedAudioPart = (AudioPart) ((StructuredSelection) overview
						.getSelection()).getFirstElement();	
			}
		}
		
		addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {		
				if (arg0.keyCode == SWT.DEL) {
					new MediaDeleteAction(audioPart).run();
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}			
		});

		// Listener der auf einen Klick, ein SelectionChangedEvent wirft
		// meldet dem AudioOverview, dass dieser Audio-Part selektiert wurde
		addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {				
				StructuredSelection selection = new StructuredSelection(audioPart);
				overview.setSelection(selection);
				
				// rechte Maustaste => öffne Menü, mit Löschmöglichkeit für die
				// Annotation
				MenuManager menuManager = new MenuManager();
				menuManager.add(new MediaDeleteAction(audioPart));
				SingleAudioBar.this.setMenu(menuManager
						.createContextMenu(SingleAudioBar.this));
				SingleAudioBar.this.setFocus();
			}
		});

		// setze die Start und Endzeit des Audio-Parts
		this.startTime = new SivaTime(audioPart.getStart() - mp.getStartTime().getNano());
		this.endTime = new SivaTime(audioPart.getEnd() - mp.getStartTime().getNano());

		this.audioPartName = audioPart.getTitle(Application.getCurrentLanguage());
		calcPaintStartEnd();

		GridData ssBGrid = new GridData();
		ssBGrid.widthHint = this.width;
		ssBGrid.heightHint = this.height;
		setLayoutData(ssBGrid);

		GridLayout gl = new GridLayout(1, false);
		setLayout(gl);

		// setzt die Listener, Actions etc
		setEvents();

		// hört darauf ob sich im Audio-Part etwas geändert hat,
		// das passiert nur wenn der Audio-Part gespeichert wurde
		// dann wird die Bar neu gezeichnet
		audioPart.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (isDisposed()) {
					return;
				}
				// Datei wurde gespeichert
				audioPartName = audioPart.getTitle();
				startTime = new SivaTime(audioPart.getStart() - mp.getStartTime().getNano());
				endTime = new SivaTime(audioPart.getEnd() - mp.getStartTime().getNano());
				calcPaintStartEnd();
				pack(true);
				
				// selektiere den Audio-Part, der geändert wurde
				StructuredSelection selection = new StructuredSelection(audioPart);
				overview.setSelection(selection);
			}
			
		});
		addPaintListener(this);

		addListener(SWT.Dispose, new Listener() {
			@Override
			public void handleEvent(Event event) {
				SingleAudioBar.this.removePaintListener(SingleAudioBar.this);
			}
		});
		
		// die SingleAudioBar hört auf den Overview ob ein anderer Audio-Part
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
						if (curSelection.getFirstElement() instanceof AudioPart) {
							selectedAudioPart = (AudioPart) ((StructuredSelection) event
								.getSelection()).getFirstElement();
							redraw();
							update();
						}
					} else {
						selectedAudioPart = null;
						redraw();
						update();
					}
				}
			}			
		});
	}

	/**
	 * setzt die Actions/Listener/Events die auf den Audio-Part Balken gebraucht
	 * werden
	 * 
	 * @param startTime
	 * @param endTime
	 */
	private void setEvents() {
		
		this.addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseEnter(MouseEvent e) {
				hover = true;
				redraw();
				update();
			}

			@Override
			public void mouseExit(MouseEvent e) {
				hover = false;
				redraw();
				update();	
			}

			@Override
			public void mouseHover(MouseEvent e) {
			}			
		});

		addMouseListener(new MouseListener() {

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
	 * die Funktion berechnet den Startpunkt und die Länge des Audio-Part, der
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

		int textOffsetX = 0;

		// setze die Texte die gezeichnet werden sollen
		String desc = audioPartName;

		// zeichne die SingleAudioBar Fläche entsprechend dem RoundRectangle
		int y = this.getBounds().y;
		setBounds(points[0], y, points[1], height);

		e.gc.setBackground(Colors.VIDEO_OVERVIEW_ITEM_BG.getColor());

		// falls diese Annotation gerade gewählt ist setzte sie auf aktiv
		if ((selectedAudioPart != null
				&& selectedAudioPart.equals(audioPart)) || hover) {
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
		textOffsetX = 5;
		setToolTipText(""); //$NON-NLS-1$

		e.gc.setForeground(Colors.VIDEO_OVERVIEW_ITEM_FONT.getColor());
		String adesc = adjustDescriptionText(desc, textOffsetX, 5, e.gc, points[1], minWidth);
		e.gc.drawText(adesc, textOffsetX, posDescY);		
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
