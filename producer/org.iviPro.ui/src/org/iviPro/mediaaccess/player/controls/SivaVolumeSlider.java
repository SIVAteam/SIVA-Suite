package org.iviPro.mediaaccess.player.controls;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;

public class SivaVolumeSlider extends AbstractSivaSlider {

	public SivaVolumeSlider(Composite parent, int sliderWidth, int sliderHeight) {
		
		super(parent, sliderWidth, sliderHeight, sliderHeight, 100, false, true, false);
		
		// setze den Slider aufs Ende
		adjustSashPosition(sliderWidth, false, ID_RIGHT_SASH);
	}

	@Override
	protected PaintListener getProgressPaintListener() {
		PaintListener progPaintListener = new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
			}			
		};
		return progPaintListener;
	}

	@Override
	protected PaintListener getSliderPaintListener() {
		PaintListener sliderPaintListener = new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				e.gc.setBackground(SASH_COLOR_R);
				e.gc.fillRoundRectangle(0, sliderHeight/2-5, sliderWidth + SASH_WIDTH, 10, 5, 5);
			}
		};
		return sliderPaintListener;
	}

	@Override
	public void setSashes(SivaEvent event) {
		// TODO Auto-generated method stub		
	}

	@Override
	protected void createNotification(long value, int sashID) {
		SivaEvent event = new SivaEvent(null, SivaEventType.VOLUME_CHANGED, value);
		notifySivaEventConsumers(event);
	}

	@Override
	public void addMarkPoint(long value, String id) {
		// TODO Auto-generated method stub
		
	}
}
