package org.iviPro.mediaaccess.player.controls;

import java.util.Collections;

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

public class SivaSlider extends AbstractSivaSlider {
	
	private String title;
	
	public static final String MARKER_STARTTIME = "MARKER_STARTTIME";
	public static final String MARKER_ENDTIME = "MARKER_ENDTIME";
	
	// Farbe für die Markierungen
	private Color MARK_COLOR_FIRST = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
	private Color MARK_COLOR_SECOND = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

	public SivaSlider(Composite parent, String title, long duration, int sliderWidth, int sliderHeight, boolean supportMarkPoints) {		
		super(parent, sliderWidth, sliderHeight, sliderHeight-20, duration, false, true, supportMarkPoints);
		if (supportMarkPoints) {
			// 2 initiale Markierungspunkte, diese befinden sich erst mal ausserhalb der Anzeige		
			markPoints.add(-10L);
			markPoints.add((long) sliderWidth + SASH_DISTANCE + 10);
		}
		this.title = title;
	}

	@Override
	protected PaintListener getProgressPaintListener() {
		PaintListener progPaintListener = new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				e.gc.setAlpha(80);
				e.gc.setBackground(Colors.ANNOTATIONEDITOR_POSSELECTOR_BG_SELECTED
						.getColor());
				e.gc.fillRectangle(0, 20, sashPosition , sliderHeight);		

				if (supportMarkPoints) {
					// zeichne die Markierungspunkte aus markPoints
					for (int i=0; i < markPoints.size(); i++) {
						// Farbe des Sash
						if (i == 0) {
							e.gc.setBackground(MARK_COLOR_FIRST);
						} else
						if (i == 1) {
							e.gc.setBackground(MARK_COLOR_SECOND);
						} else {
							e.gc.setBackground(SASH_COLOR_R);
						}
						e.gc.fillRectangle(markPoints.get(i).intValue(), 20, MARKPOINT_WIDTH, sliderHeight);
					}
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
				String infoText = title;		
				e.gc.drawText(infoText , POS_INFO_TEXT_X, POS_INFO_TEXT_Y);						
				e.gc.drawLine(0, 18, sliderWidth + SASH_WIDTH, 18);		
				String sashTime = new SivaTime(convertPositionToValue()).toString();
				String maxTime = new SivaTime(maxValue).toString();
				String timeString = sashTime + " / " + maxTime;						 //$NON-NLS-1$
				int timePos = sliderWidth - e.gc.textExtent(timeString).x - 5;
				e.gc.drawText(timeString, timePos, POS_INFO_TEXT_Y);
				
				if (supportMarkPoints) {
					// zeichne die Markierungspunkte aus markPoints
					for (int i=0; i < markPoints.size(); i++) {
						// Farbe des Sash
						if (i == 0) {
							e.gc.setBackground(MARK_COLOR_FIRST);
						} else
						if (i == 1) {
							e.gc.setBackground(MARK_COLOR_SECOND);
						} else {
							e.gc.setBackground(SASH_COLOR_R);
						}
						e.gc.fillRectangle(markPoints.get(i).intValue(), 20, MARKPOINT_WIDTH, sliderHeight);
					}
				}
			}			
		};
		return sliderPaintListener;
	}

	@Override
	protected void createNotification(long value, int sashID) {
		// der Video Slider hört auf den Videoplayer
		SivaEvent event = new SivaEvent(SivaSlider.this, SivaEventType.MEDIATIME_CHANGED, new SivaTime(value));
		notifySivaEventConsumers(event);
	}

	@Override
	public void setSashes(SivaEvent event) {
		if (event.getTime() != null) {
			// prüfe ob es die Start oder Endzeit ist und setze entsprechend die Marker
			if (event.getEventType().equals(SivaEventType.MEDIATIME_CHANGED) ||
				event.getEventType().equals(SivaEventType.STARTTIME_CHANGED) ||
				event.getEventType().equals(SivaEventType.ENDTIME_CHANGED)) {
				adjustSashPosition(convertValueToPosition(event.getTime().getNano()), false, ID_RIGHT_SASH);
			}						
		}
		
		if (event.getEventType().equals(SivaEventType.VIDEO_STOPPED)) {
			adjustSashPosition(0, false, ID_RIGHT_SASH);
		}
	}

	@Override
	public void addMarkPoint(long value, String id) {
		if (isDisposed()) {
			return;
		}

		long val = convertValueToPosition(value);
		
		// der Slider verwendet einen Marker für die Start und Endzeit
		if (id.equals(MARKER_STARTTIME)) {
			// prüfe ob der Marker der Startzeit vor der Endzeit liegt
			if (markPoints.get(1) > 0 && val >= markPoints.get(1)) {
				val = markPoints.get(1)-3;
			}
			markPoints.remove(0);
			markPoints.add(0, val);
		} else
		if (id.equals(MARKER_ENDTIME)) {
			if (markPoints.get(0) > 0 && val <= markPoints.get(0)) {
				val = markPoints.get(0)+3;
			}
			markPoints.remove(1);
			markPoints.add(1, val+SASH_DISTANCE);
		}	
		// sortiere aufsteigen => Farbe wird beim zeichnen richtig gesetzt
		Collections.sort(markPoints);
		slider.redraw();
		slider.layout();
	}
}
