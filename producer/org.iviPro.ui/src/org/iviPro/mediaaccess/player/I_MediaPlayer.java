package org.iviPro.mediaaccess.player;

import java.awt.image.BufferedImage;

import org.eclipse.swt.widgets.Composite;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventProviderI;
import org.iviPro.model.IMediaObject;
import org.iviPro.utils.SivaTime;

public interface I_MediaPlayer extends SivaEventProviderI {

	public IMediaObject getMediaObject();
	public SivaTime getStartTime();
	public SivaTime getEndTime();
	public SivaTime getDuration();

	public void setMediaTime(SivaEvent event);
	public SivaTime getMediaTime();
	public SivaTime getRelativeTime();
	public void play();
	public void playFromTo(SivaTime st, SivaTime et);
	public void pause();
	public void stop();
	
	public boolean isMute();
	public void unMute();
	public void mute();
	public void setVolume(SivaEvent event);
	
	public boolean isActive();
		
	public void finish();
	
	public void backward();	
	public void forward();
	public void frameForward();
	public void frameBackward();
	
	public int getFrameForNanos(long time);
	public int getFrameForTime(SivaTime time);
	public SivaTime getTimeForFrame(int frame);
	
	public void createVisualPart(Composite parent,int x, int y, int width, int height);
	public BufferedImage[] extractImage(long nanos);
}
