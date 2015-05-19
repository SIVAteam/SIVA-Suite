package org.iviPro.mediaaccess.player.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.theme.Colors;
import org.iviPro.utils.SivaTime;

/**
 * Widget implementing a time line. Used in different editors for direct 
 * manipulation of start or end times, timestamps etc.  
 */
public class SivaScale extends AbstractSivaSlider {
	
	// Abstand der Skalapunkte
	private final int SCALE_POINT_DISTANCE = 20;
	
	// gibt an, jeder wievielte Punkt ein Hauptpunkt ist 
	private int SCALE_MAINPOINT_DISTANCE = 5;
	
	// Breite der Skalenpunkte
	private final int SCALE_POINT_WIDTH = 2;
	
	private boolean paintProgression;
	
	private Color SCALE_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	private Color MARK_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
	
	public SivaScale(Composite parent, long maxValue, int sliderWidth, int sliderHeight, 
												boolean useLeft, boolean paintProgression, boolean supportMarkPoints) {		
		super(parent, sliderWidth, sliderHeight, sliderHeight-20, maxValue, useLeft, paintProgression, supportMarkPoints);
		this.paintProgression = paintProgression;

		// Überschreibe die Standard Farbwerte, wenn Markierpunkte verwendet werden
		if (useLeft && supportMarkPoints) {
			SASH_COLOR_L = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
			SASH_COLOR_R = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		}
	}

	@Override
	protected PaintListener getProgressPaintListener() {
		PaintListener progPaintListener = new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				if (paintProgression) {
					e.gc.setAlpha(80);
					e.gc.setBackground(Colors.ANNOTATIONEDITOR_POSSELECTOR_BG_SELECTED.getColor());
					e.gc.fillRectangle(0, 20, rightSashFormData.left.offset - leftSashFormData.left.offset , sliderHeight);
				}
			}			
		};
		return progPaintListener;
	}

	@Override
	protected PaintListener getSliderPaintListener() {
		PaintListener sliderPaintListener = new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				int posSP = 0;
				int	counter = 5;
				while (posSP < sliderWidth + SASH_WIDTH) {
					e.gc.setBackground(SCALE_COLOR);
					if (counter != SCALE_MAINPOINT_DISTANCE) {
						e.gc.fillRectangle(posSP, 30, SCALE_POINT_WIDTH, sliderHeight);
						counter++;
					} else {
						e.gc.fillRectangle(posSP, 20, SCALE_POINT_WIDTH+5, sliderHeight);
						if(posSP!=0){
							SivaTime pointTime = new SivaTime(maxValue / sliderWidth * (posSP+4));
							e.gc.drawText(pointTime.toString(), posSP - 20, 0, true);
						}
						counter = 1;
					}
					posSP = posSP + SCALE_POINT_DISTANCE;							
				}

				if (supportMarkPoints) {
					// Farbe für die Markierungspunkte
					e.gc.setBackground(MARK_COLOR);				
				
					//	zeichne die Markierungspunkte aus markPoints
					for (int i=0; i < markPoints.size(); i++) {					
						e.gc.fillRectangle(markPoints.get(i).intValue(), 15, MARKPOINT_WIDTH, sliderHeight);
					}
				}
			}			
		};
		return sliderPaintListener;
	}	

	@Override
	protected void createNotification(long value, int sashID) {
		// der Video Slider hört auf den Videoplayer
		SivaEventType type = SivaEventType.STARTTIME_CHANGED;		
		if (sashID == ID_RIGHT_SASH) {
			type = SivaEventType.ENDTIME_CHANGED;
		}		
		SivaEvent event = new SivaEvent(SivaScale.this, type, new SivaTime(value));
		notifySivaEventConsumers(event);
	}

	@Override
	public void setSashes(SivaEvent event) {
		int sash = ID_RIGHT_SASH;
		if (event.getEventType().equals(SivaEventType.STARTTIME_CHANGED)) {
			sash = ID_LEFT_SASH;
		} else
		if (event.getEventType().equals(SivaEventType.ENDTIME_CHANGED)) {
			sash = ID_RIGHT_SASH;			
		}

		if (event.getTime() != null) {
			adjustSashPosition(convertValueToPosition(event.getTime().getNano()), false, sash);
		}
	}

	@Override
	public void addMarkPoint(long value, String id) {
		if (isDisposed()) {
			return;
		}
		long newVal = convertValueToPosition(value) + SASH_DISTANCE/2 + MARKPOINT_WIDTH/2;
		markPoints.clear();
		markPoints.add(newVal);
		slider.redraw();
		slider.layout();
	}

}
