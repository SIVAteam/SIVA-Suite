package org.iviPro.mediaaccess.player;

import java.awt.image.BufferedImage;

import org.eclipse.swt.widgets.Composite;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventProvider;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.model.IMediaObject;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.Video;
import org.iviPro.utils.SivaTime;

/**
 * Ein MediaPlayer kapselt genau einen Player für ein Video, sobald ein Video z.B. im Szeneneditor geöffnet wird
 * wird ein Player erstellt. Den sichtbaren Teil erstellt man mit createVisualPart()
 * @author juhoffma
 */
public abstract class AMediaPlayer extends SivaEventProvider implements I_MediaPlayer {
		
	// Zeitsprünge bei den Vor/Zurück-Buttons
	private final long SEEKSTEP = 2000000000L;

	// in Nanosekunden, die reale Zeit im Medium
	// Bei einer Szene ist die Startzeit zum Beispiel 30 Sekunden
	// in diesem Bereich operiert der Player
	// der Player arbeitet intern auf der realen Zeit
	// alles was von außen angefordert oder gesetzt wird entspricht der
	// Zeit bezogen auf die Szene/Annotation
	protected SivaTime startTime;
	protected SivaTime endTime;
	
	// das zum Player gehörende Medienobjekt
	protected IMediaObject media;

	// gibt an ob der Player gerade das Video abspielt == true
	private boolean playActive = false;

	// gibt an ob von einer Startzeit zu einer Endzeit abgespielt werden soll
	// playFromTo
	private boolean limitedPlay;
	/**
	 * time limit as absolute time in nanoseconds
	 */
	private long limitEnd;

	/**
	 * instanziiert einen MediaPlayer über das gesamte MediaObject
	 * 
	 * @param mediaObject
	 */
	public AMediaPlayer(IMediaObject mediaObject) {
		this.media = mediaObject;
		this.startTime = new SivaTime(0L);
		if (media instanceof Video) {
			Video vid = (Video) media;
			if (vid.getDuration() != null) {
				this.endTime = new SivaTime(vid.getDuration());
			}
		}
		if (media instanceof Audio) {
			Audio aud = (Audio) media;
			if (aud.getDuration() != null) {
				this.endTime = new SivaTime(aud.getDuration());
			}
		}
		this.initPlayer();
	}

	public AMediaPlayer(IMediaObject mediaObject, long startTime, long endTime) {
		this.media = mediaObject;
		this.startTime = new SivaTime(startTime);
		this.endTime = new SivaTime(endTime);
		this.initPlayer();
	}
			
	public void backward() {		
		this.setNewTime(getTimeNano() - SEEKSTEP);
		this.limitedPlay = false;
	}
	
	public void forward() {
		this.setNewTime(getTimeNano() + SEEKSTEP);		
		this.limitedPlay = false;
	}

	public void frameBackward() {
		this.frameForwardImpl();
		this.limitedPlay = false;
	}	

	public void frameForward() {
		this.frameBackwardImpl();
		this.limitedPlay = false;
	}
	
	private void setNewTime(long newMediaTime) {
		long adjustedTime = adjustTime(newMediaTime);		
		this.setTimeImpl(adjustedTime);
	}
	
	// Zeit muss zwischen start und endzeit liegen
	protected long adjustTime(long newMediaTime) {
		if (newMediaTime < startTime.getNano()) {
			return startTime.getNano();
		} else
		if (newMediaTime > endTime.getNano()) {
			return endTime.getNano();
		}
		return newMediaTime;
	}

	public SivaTime getStartTime() {
		return this.startTime;
	}

	public SivaTime getEndTime() {
		return this.endTime;
	}

	public IMediaObject getMediaObject() {
		return this.media;
	}

	public SivaTime getDuration() {
		return new SivaTime(endTime.getNano() - startTime.getNano());
	}

	/*
	 * startet den MoviePlayer == Film abspielen
	 */
	public void play() {	
		this.playActive = true;
		this.startImpl();		
	}

	public void playFromTo(SivaTime st, SivaTime et) {
		long limitStart = startTime.addTime(st);
		this.limitEnd = startTime.addTime(et);
		this.setNewTime(limitStart);
		this.limitedPlay = true;
		this.play();
	}

	/*
	 * stopt den MoviePlayer == Film stop
	 */
	public void pause() {
		this.pauseImpl();
		this.limitedPlay = false;
		this.playActive = false;		
	}

	/**
	 * Stopping playback and returning to start time.
	 */
	public void stop() {
		this.stopImpl();
		this.limitedPlay = false;
		this.playActive = false;
		notifySivaEventConsumers(new SivaEvent(this,
				SivaEventType.VIDEO_STOPPED, null));
		
	}
	
	/**
	 * Resets the playback, similar to the stop() method (stopping playback
	 * and returning to start time), in cases where the stop() method can not
	 * be used due to special states of the underlying native player.  
	 */
	public void reset() {
		this.resetImpl();
		this.limitedPlay = false;
		this.playActive = false;
		notifySivaEventConsumers(new SivaEvent(this,
				SivaEventType.VIDEO_STOPPED, null));		
	}
	
	
	/**
	 * Get the actual time in the media <b>without</b> considering start or
	 * end times passed to the media player.
	 * @return actual time in the media
	 */
	@Override
	public SivaTime getMediaTime() {
		return new SivaTime(this.getTimeNano()); 
	}
	
	
	/**
	 * Get the actual time relative to the start time used to instantiate the player.
	 * @return actual time relative to start time
	 */
	@Override
	public SivaTime getRelativeTime() {
		return new SivaTime(this.getTimeNano() - this.startTime.getNano());
	}

	/**
	 * Updates the media player time to the time of the given *TIME_CHANGED
	 * event. The event time is considered to be a relative time. Therefore,
	 * the start time of the current media is added.
	 * @param *TIME_CHANGED event 
	 */
	public void setMediaTime(SivaEvent event) {

		SivaEventType type = event.getEventType();
		if (!(type.equals(SivaEventType.MEDIATIME_CHANGED)
				|| type.equals(SivaEventType.STARTTIME_CHANGED) || type
				.equals(SivaEventType.ENDTIME_CHANGED))) {
			return;
		}

		SivaTime time = new SivaTime(event.getTime().getNano());
		long nano = adjustTime(time.addTime(startTime));
		setTimeImpl(nano);	
	}
	
	/**
	 * Notifies event consumers of the player about a change of the actual
	 * playback time.
	 * @param realMediaTime actual playback time of player as absolute time
	 * in nanoseconds
	 */
	protected void notifyAboutTimeChange(long realMediaTime) {		
		long relativeTime = realMediaTime - startTime.getNano();
			
		if (this.limitedPlay) {
			if (realMediaTime > this.limitEnd) {
				this.pause();		
				relativeTime = this.limitEnd - startTime.getNano() ;
			}
		}
		SivaEvent event = new SivaEvent(this, SivaEventType.MEDIATIME_CHANGED, new SivaTime(relativeTime));
		notifySivaEventConsumers(event);
	}

	@Override
	public boolean isActive() {
		return this.playActive;
	}
	
	@Override
	public void finish() {
		this.finishImpl();
	}
	
	/**
	 * forwarded das Event
	 */
	@Override
	public void forwardEvent(final SivaEvent event) {

		// der Player reagiert nur auf MediaTimeChanged, Start und
		// Endzeit Events
		SivaEventType type = event.getEventType();
		if (!(type.equals(SivaEventType.MARK_POINT_START) || type.equals(SivaEventType.MARK_POINT_END))) {
			return;
		}
		notifySivaEventConsumers(event);
	}

	/**
	 * Wird aufgerufen wenn der Player pausiert um 2 Bilder zu extrahieren!
	 * 
	 * @return
	 */
	public BufferedImage[] extractImage(long time) {
		//!FIXME
		return null;
	}
	
	public int getFrameForNanos(long time) {
		if (this.media instanceof Video) {
			Video video = (Video) this.media;
			double fps = video.getFrameRate();
			double seconds = time / 1000000000d;
			return (int) (seconds * fps);			
		}
		return -1;
	}
	
	public int getFrameForTime(SivaTime time) {
		return getFrameForNanos(time.getNano());
	}
	
	public SivaTime getTimeForFrame(int frame) {
		if (this.media instanceof Video && frame > 0) {
			Video video = (Video) this.media;
			double fps = video.getFrameRate();
			double seconds = frame / fps;
			return new SivaTime((long) (seconds * 1000000000L));			
		} 
		return new SivaTime(0);
	}
	
	public abstract void createVisualPart(Composite parent,int x, int y, int width, int height);
	protected abstract void frameForwardImpl();
	protected abstract void frameBackwardImpl();
	protected abstract void setTimeImpl(long nano);
	protected abstract long getTimeNano();
	protected abstract void setMedia(String mediaFile);	
	protected abstract void initPlayer();	
	protected abstract void startImpl();
	protected abstract void stopImpl();
	protected abstract void resetImpl();
	protected abstract void pauseImpl();
	protected abstract void finishImpl();
}
