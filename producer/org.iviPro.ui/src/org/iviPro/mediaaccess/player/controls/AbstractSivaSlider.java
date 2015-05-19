package org.iviPro.mediaaccess.player.controls;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.iviPro.editors.common.SivaComposite;
import org.iviPro.editors.events.SivaEvent;

public abstract class AbstractSivaSlider extends SivaComposite {
	
	// IDs für den rechten und linken Sash
	protected int ID_LEFT_SASH = 0;
	protected int ID_RIGHT_SASH = 1;

	// Breite und Höhe der Slider
	protected int sliderHeight;
	protected int sliderWidth;
		
	// Breite des Slider Sash
	protected final int SASH_WIDTH = 7;
	protected int sashHeight;
	
	// der eigentliche Slider
	protected Composite slider;
	
	// die Progress Bar
	private Composite progress;
	
	// der rechte und linke Sash
	protected Sash rightSash;
	protected FormData rightSashFormData;
	protected Sash leftSash;
	protected FormData leftSashFormData;
		
	// der maximal einstellbare Wert des Volume Sliders
	protected long maxValue;
		
	// Position des Sashes
	protected int sashPosition = 0;
	
	// Position des Informationstextes
	protected final int POS_INFO_TEXT_X = 8;
	protected final int POS_INFO_TEXT_Y = 1;
	
	// minimale SashDistanz
	protected final int SASH_DISTANCE = 5;
	
	protected boolean useLeft;
	protected boolean paintProgression;
	
	// gibt an ob Markeriungspunkte unterstützt und gezeichnet werden
	protected boolean supportMarkPoints;
	
	// Positionen von Markierungspunkten
	protected List<Long> markPoints;
	// Breite eines Markierungspunktes
	protected int MARKPOINT_WIDTH = 3;
	
	// Farbe für die Sashes
	protected Color SASH_COLOR_L = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
	protected Color SASH_COLOR_R = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
		
	/**
	 * Skala, Slider oder Volume Control
	 * @param parent
	 * @param style
	 * @param mediaType Medientyp, wird aktuell nur beim Slider angezeigt
	 * @param mediaName Medienname, wird aktuell nur beim Slider anzeigt
	 * @param maxValue maximaler Wert den der Slider/Skala annehmen kann
	 * @param mode Modus des Controlers
	 * @param useLeft gibt an ob ein zweiter = linker Sash verwendet werden soll z.B. bei der Skala
	 */
	public AbstractSivaSlider(Composite parent, int sliderWidth, int sliderHeight, int sashHeight, 
										long maxValue, boolean useLeft, boolean paintProgression, boolean supportMarkPoints) {
		super(parent, SWT.CENTER);
		this.sliderHeight = sliderHeight;
		this.sliderWidth = sliderWidth;
		this.sashHeight = sashHeight;
		this.maxValue = maxValue;
		this.useLeft = useLeft;
		this.paintProgression = paintProgression;
		this.supportMarkPoints = supportMarkPoints;
		this.markPoints = new LinkedList<Long>();
		init();
	}
	
	/**
	 * initialisiert den Slider
	 */
	protected void init() {	
		// Layout für das gesamte Composite, dieses hält Infos, Slider, Skala
		GridLayout assGL = new GridLayout(1, false);
		assGL.marginWidth = 0;
		setLayout(assGL);
		GridData vcGD;
		vcGD = new GridData();
		vcGD.horizontalAlignment = SWT.CENTER;
		setLayoutData(vcGD);
		
		// Container hält den Slider
		slider = new Composite(this, SWT.CENTER | SWT.BORDER);
		GridData sliderGD = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		sliderGD.widthHint = sliderWidth + SASH_WIDTH;
		sliderGD.heightHint = sliderHeight;		
		
		slider.setLayoutData(sliderGD);		
		slider.setLayout(new FormLayout());
		
		// linker Sash, nur aktiv falls useLeft == true
		leftSash = new Sash(slider, SWT.VERTICAL);
		leftSashFormData = new FormData();
		leftSashFormData.top = new FormAttachment(0, sliderHeight - sashHeight);
		leftSashFormData.left = new FormAttachment(0, 0);
		leftSashFormData.width = SASH_WIDTH;
		leftSashFormData.height = sashHeight;
		leftSash.setLayoutData(leftSashFormData);
		
		if (!useLeft) {
			leftSash.setVisible(false);
		}
								
		// rechter Sash
		rightSash = new Sash(slider, SWT.VERTICAL);
		rightSashFormData = new FormData();
		rightSashFormData.top = new FormAttachment(0, sliderHeight - sashHeight);
		rightSashFormData.left = new FormAttachment(0, 0);
		rightSashFormData.width = SASH_WIDTH;
		rightSashFormData.height = sashHeight;
		rightSash.setLayoutData(rightSashFormData);
				
		// zeichne einen schönen Sash :)
		leftSash.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				e.gc.setBackground(SASH_COLOR_L);
				e.gc.fillRoundRectangle(0, 0, SASH_WIDTH, sliderHeight, 5, 5);				
			}			
		});
		
		rightSash.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				e.gc.setBackground(SASH_COLOR_R);
				e.gc.fillRoundRectangle(0, 0, SASH_WIDTH, sliderHeight, 5, 5);
			}			
		});
		
		leftSash.addListener(SWT.Selection, new Listener () {
			public void handleEvent(Event e) {
				adjustSashPosition(e.x, true, ID_LEFT_SASH);
			}
		});
						
		rightSash.addListener(SWT.Selection, new Listener () {
			public void handleEvent(Event e) {
				adjustSashPosition(e.x, true, ID_RIGHT_SASH);
			}
		});
		
		
		// das Composite befindet sich zwischen den Sash und wird dazu verwendet
		// den Fortschritt darzustellen, das Buffern funktioniert mit zusätzlichem Composite
		// besser als wenn man direkt in den Slider zeichnet. Die Positionierung wird automatisch
		// vom FormLayout übernommen		
		progress = new Composite(slider, SWT.CENTER | SWT.TRANSPARENT);
		FormData progressFormData = new FormData();
		progressFormData.right = new FormAttachment(rightSash, SASH_WIDTH);
		if (leftSash.isVisible()) {
			progressFormData.left = new FormAttachment(leftSash, -SASH_WIDTH);	
		} else {
			progressFormData.left = new FormAttachment(0, 0);
		}
		progressFormData.height = sliderHeight;
		progress.setLayoutData(progressFormData);
		
		// zeichne die Progress Bar (Triangle, Bar)
		progress.addPaintListener(getProgressPaintListener());
		
		if (!paintProgression) {
			progress.setVisible(false);
		}
						
		// zeichne in den Slider (Skalenpunkte, Informationen)
		slider.addPaintListener(getSliderPaintListener());
		
		// Listener auf den Slider/Filler um die Position per Maus Klick zu setzen
		slider.addListener(SWT.MouseDown, new Listener() {			
			@Override
			public void handleEvent(Event e) {
				if (leftSash.isVisible()) {
					// Unterscheide Maus Buttons links = linker Sash, rechts = rechter Sash
					if (e.button == 1) {
						adjustSashPosition(e.x, true, ID_LEFT_SASH);	
					} else {
						adjustSashPosition(e.x, true, ID_RIGHT_SASH);	
					}
				} else {
					adjustSashPosition(e.x, true, ID_RIGHT_SASH);
				}
			}			
		});		
		
		// wird benötigt wenn die Progress Bar gezeichnet wird
		// da die Maus Events nicht weitergeleitet werden
		progress.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event e) {
				if (leftSash.isVisible()) {
					if (e.button == 1) {
						adjustSashPosition(e.x + leftSashFormData.left.offset, true, ID_LEFT_SASH);	
					} else {
						adjustSashPosition(e.x + leftSashFormData.left.offset, true, ID_RIGHT_SASH);	
					}
				} else {
					adjustSashPosition(e.x, true, ID_RIGHT_SASH);					
				}
			};
		});	
	}
	
	/**
	 * setzt die Positionen der Sashes
	 */
	protected void adjustSashPosition(int position, boolean notify, int sashID) {
		if (isDisposed() || (position == sashPosition)) {
			return;
		}
		if (position < 0) {
			position = 0;
		}
		if (position > sliderWidth) {
			position = sliderWidth;
		}
		sashPosition = position;
		
		if (sashID == this.ID_LEFT_SASH) {
			if (sashPosition < rightSashFormData.left.offset - SASH_WIDTH) {
				leftSashFormData.left = new FormAttachment(0, sashPosition);
			} else {
				leftSashFormData.left = new FormAttachment(0, rightSashFormData.left.offset - SASH_WIDTH);	
			}
		} else 
		if (sashID == this.ID_RIGHT_SASH) {
			if (!leftSash.isDisposed() && leftSash.isVisible()) {
				if (sashPosition > leftSashFormData.left.offset + SASH_WIDTH) {
					rightSashFormData.left = new FormAttachment(0, sashPosition);
				} else {
					rightSashFormData.left = new FormAttachment(0, leftSashFormData.left.offset + SASH_WIDTH);
				}
			} else {
				rightSashFormData.left = new FormAttachment(0, sashPosition);
			}
		}
		
		// zeichne den Slider neu
		slider.redraw();
		rightSash.redraw();
		slider.layout();

		if (notify) {
			createNotification(convertPositionToValue(), sashID);
		}
	}
		
	/**
	 * berechnet aus der aktuellen Position den entsprechenden Wert (basierend auf dem Maximalwert)
	 * @param id
	 * @return
	 */
	protected long convertPositionToValue() {
		return (maxValue/sliderWidth) * sashPosition; 
	}
	
	public Composite getSlider() {
		return this.slider;
	}
	
	protected int convertValueToPosition(long val) {
		return (int) ((val*sliderWidth) / maxValue);
	}
	
	/**
	 * füge explizite Markierungspunkte ein, diese können von den PaintListenern 
	 * (z.B. slider.getPaintListener()) eingezeichnet werden, die Unterklassen handlen 
	 * die Methode selbst
	 * es kann eine ID mitgegeben werden z.B. Markierung Endzeit
	 * @param value
	 */
	public abstract void addMarkPoint(long value, String id);
	
	// entfernt die Markierungspunkte
	public void clearMarkPoints() {
		markPoints.clear();
	}
	
	// abstrakte Methoden die von den Unterklassen implementiert werden müssen
	protected abstract PaintListener getProgressPaintListener();
	protected abstract PaintListener getSliderPaintListener();
	
	// wird verwendet um die Benachrichtigung der Consumer vorzubereiten
	// und auszüführen
	// z.B. das Erstellen der Events
	protected abstract void createNotification(long value, int sashID);
	
	// wird zum setzen der Sashes von ausserhalb verwendet
	protected abstract void setSashes(SivaEvent event);
	
	public void setToolTip(String message) {
		if (slider != null) {
			slider.setToolTipText(message);
		}
		
		if (progress != null) {
			progress.setToolTipText(message);
		}
	}
	
	public void addSivaSliderMouseListener(MouseListener listener) {
		slider.addMouseListener(listener);
		progress.addMouseListener(listener);
		rightSash.addMouseListener(listener);
		leftSash.addMouseListener(listener);
	}
}



