package org.iviPro.mediaaccess.player;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.mediaaccess.videograb.FrameGrabingJob;
import org.iviPro.mediaaccess.videograb.framegraber.FrameGraberFactory;
import org.iviPro.mediaaccess.videograb.interfaces.FrameGrabber;
import org.iviPro.model.IMediaObject;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.Video;

import uk.co.caprica.vlcj.component.AudioMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.windows.WindowsVideoSurfaceAdapter;

public class VLCBasedMediaPlayer extends AbstractMediaPlayer {
	
	public VLCBasedMediaPlayer(IMediaObject mediaObject) {
		super(mediaObject);
		
	}
	
	public VLCBasedMediaPlayer(IMediaObject mediaObject, long startTime, long endTime) {
		super(mediaObject, startTime, endTime);
	}
	
	// der VLC Player
	private MediaPlayer p;
	private CanvasVideoSurface c;
	
	private BufferedImage img; //Startbild des Videos über ffmpeg

	//Klasse die es ermöglicht ein Bild auf die VIdeosurface zu zeichnen
	//um ein Startbild anzuzeigen anstatt nur Schwarz
	private class VideoCanvas extends Canvas{
		int x;
		int y;
		int height;
		int width;
		
		public VideoCanvas(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.height = height;
			this.width = width;
		}
		
		public void paint(Graphics g) {
			if (width > (img.getWidth() + 1)) {
				x = (width-img.getWidth())/2;
			} 
			if( height > (img.getHeight() +1)){
				y = (height - img.getHeight())/2;
			}
			g.drawImage(img,x ,y, new ImageObserver() {

				@Override
				public boolean imageUpdate(java.awt.Image img, int infoflags,
						int x, int y, int width, int height) {

					return false;
				}
			});
			
			
		}
	}
	public void createVisualPart(Composite parent,int x, int y, int width, int height) {
				
		createFirstFrame(width,height);
				
		VideoCanvas videoSurface = new VideoCanvas(x,y,width, height);
		videoSurface.setBackground(java.awt.Color.black);
		
		
		Composite videoComposite = new Composite(parent, SWT.CENTER);
		videoComposite.setBounds(x, y, width, height);
		
		// das Composite für das Video
		Composite videoShow = new Composite(videoComposite, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		videoShow.setBounds(x, y, width, height);
		videoShow.setVisible(true);
		
		Frame vidFrame = SWT_AWT.new_Frame(videoShow);
		vidFrame.add(videoSurface);
		vidFrame.setVisible(true);
				
		VideoSurfaceAdapter a = new WindowsVideoSurfaceAdapter();
		c = new CanvasVideoSurface(videoSurface, a);

		if (this.p instanceof EmbeddedMediaPlayer) {
			((EmbeddedMediaPlayer) this.p).setVideoSurface(c);
		}
		
		initStartTime();
	}
	
	//erstellt das Startbild da vlc am anfang nur Schwarz anzeigt
	private void createFirstFrame(int surfaceWidth, int surfaceHeight){
		Video vid = (Video) media;
		
		FrameGrabber frameGrabber = FrameGraberFactory.getFrameGrabber();
		FrameGrabingJob frameGrabberJob = new FrameGrabingJob(getStartTime().getNano(),
					new Dimension(surfaceWidth,surfaceHeight), "Startbild", vid);
		frameGrabber.grabFrame(frameGrabberJob);
		img = frameGrabberJob.getImage();
	}

	@Override
	protected void frameForwardImpl() {
		p.nextFrame();
	}

	@Override
	protected void frameBackwardImpl() {
		// !FIXME
		boolean restart = false;
		if (this.isActive()) {
			restart = true;
		}
		this.p.pause();
		int newFrame = this.getFrameForNanos(p.getTime()) - 1;
		long newTime = this.getTimeForFrame(newFrame).getNano();
		this.p.setTime(newTime);
		
		if (restart) {
			this.p.start();
		}
		
	}
	
	/**
	 * Returns the actual time in the VLC player. Due to possibly inaccurate 
	 * time jumps the returned value is normalized to always stay between start
	 * and end time.
	 * @throws MediaPlayerException if the time returned by the player is -1
	 * and therefore indicates the player is not playing a video right now.
	 */
	@Override
	protected long getTimeNano() {
		long actualTime = this.p.getTime();
		if (actualTime < 0) {
			throw new MediaPlayerException("Invalid status call." +
					"VLC player can only return status values during playback.");
		} 
		actualTime = actualTime*1000000L;
		if (actualTime < startTime.getNano()) {
			return startTime.getNano();
		} else if (actualTime > endTime.getNano()) {
			return endTime.getNano();
		}
		return actualTime;
	}

	@Override
	protected void setMedia(String mediaFile) {
		this.p.playMedia(mediaFile, "");
		this.p.stop();
	}

	/**
	 * Creates and initializes the VLC media player and adds listeners for its 
	 * events. 
	 */
	@Override
	protected void initPlayer() {
		MediaPlayerFactory fac = new MediaPlayerFactory();
		String path = this.media.getFile().getAbsolutePath();
		
		if (this.media instanceof Audio) {
			AudioMediaPlayerComponent audioPlayer = new AudioMediaPlayerComponent() {
				@Override
				public void finished(MediaPlayer mediaPlayer) {
				}
				@Override
				public void error(MediaPlayer mediaPlayer) {
				}
			};
			this.p = audioPlayer.getMediaPlayer();
			// Start, end time option not used due to problems with video playback
			//String startTimeInSec = String.format(Locale.ENGLISH, "%.3f",  startTime.getMillis() / 1000.0f);
			//String endTimeInSec =  String.format(Locale.ENGLISH, "%.3f", endTime.getMillis() / 1000.0f);
			this.p.prepareMedia(path,""); //":start-time=" + String.valueOf(startTimeInSec), ":stop-time=" + String.valueOf(endTimeInSec));
			// For audio files start time can be initialized here
			initStartTime();
		} else {
			this.p = fac.newEmbeddedMediaPlayer();
			/*
			 * Unfortunately vlclib cannot assign start and end times for all kinds 
			 * of videos. Therefore, staying in the proper time range needs to be
			 * checked manually.
			 */
			this.p.prepareMedia(path,"");
			// Initialization of start time for videos is done in createVisualPart()
			// since the video canvas is needed first.
		}
		p.setPlaySubItems(false);
		p.setRepeat(false);
		
		this.p.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

			/**
			 * Notify components which have to react to time changes in the 
			 * player. Due to accuracy issues the media player might play back
			 * slightly outside the given time frame. Time is adjusted in 
			 * this case.
			 */
			@Override
			public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
				newTime = newTime*1000000L;
				if (newTime < startTime.getNano() 
						|| newTime >= endTime.getNano()) {
					newTime =  adjustTime(newTime);
				}
					notifyAboutTimeChange(newTime);
			}
			

			/**
			 * Called when the media player reaches the end of media.
			 * <p>
			 * <b>Note:</b>
			 * Resets the whole media player and creates a new one. Other 
			 * attempts to get out of VLC's finished state failed. Just
			 * preparing the media and restarting it, lost the event listeners.
			 * Setting setRepeat(true) didn't work, since the player didn't
			 * react to pause commands properly when reaching finished state.
			 */
			@Override
			public void finished(MediaPlayer mediaPlayer) {
				reset();
			}
			
			/**
			 * Called when the playback is paused.
			 * <p>
			 * <b>Note:</b>
			 * Since multiple pause commands in a row are sometimes not
			 * executed correctly, we need a workaround for continuous playback
			 * after {@link VLCBasedMediaPlayer#setTimeImpl(long nano) 
			 * setTimeImpl()} is called during playback.
			 */
			@Override
			public void paused(MediaPlayer mediaPlayer) {
				if (isActive()) {
					p.setPause(false);
				}
			}
			
			@Override
			public void playing(MediaPlayer mediaPlayer) {
			}		
			
			@Override
			public void opening(MediaPlayer mediaPlayer) {
			}
		});
	}
	
	/**
	 * This is a dirty workaround for vlclib not being able to use the
	 * :start-time option correctly for all video formats. See {@link 
	 * #setTimeImpl(long)} for further information on that problem. When trying
	 * to play an embedded video, this method has to be called <b>after</b> the
	 * players video surface has been set, since starting the video will
	 * otherwise create a new window.
	 */
	public void initStartTime() {
		p.start();
		setTimeImpl(startTime.getNano());
	}

	@Override
	protected void startImpl() {
		this.p.start();	
	}

	/**
	 * Vlc player implementation of stop method. Stops the playback 
	 * by reseting the media player time to the start time which also includes
	 * pausing the video.
	 */
	@Override
	protected void stopImpl() {
		setTimeImpl(startTime.getNano());
	}
	
	/**
	 * Releases the whole media player and initializes a new one. 
	 */
	@Override
	protected void resetImpl() {
		p.release();
		initPlayer();
		if (media instanceof Video) {
			((EmbeddedMediaPlayer) p).setVideoSurface(c);
			initStartTime();
		}
		
	}

	@Override
	protected void pauseImpl() {
		p.setPause(true);
		
	}

	@Override
	protected void finishImpl() {
		this.p.release();
	}


	@Override
	public boolean isMute() {
		return this.p.isMute();
	}

	@Override
	public void unMute() {
		this.p.mute(false);
	}

	@Override
	public void mute() {
		this.p.mute(true);
	}

	@Override
	public void setVolume(SivaEvent event) {
		if (event.getEventType() == SivaEventType.VOLUME_CHANGED) {						
				Long integer =  (Long) event.getValue();
				this.p.setVolume(integer.intValue());						
		}
	}
	
	/**
	 * Sets the actual time of the VLC player. The given value needs to be an
	 * absolute time in nanoseconds. It will also stop the playback if 
	 * isActive() returns false!
	 * <p>
	 * <b>Note:</b>
	 * It seems that for some video formats jumps to a certain time in the 
	 * video can not be done directly since accuracy is dependent on the timing
	 * information available in the format.
	 * In our test cases setting the time twice and pausing the playback in 
	 * between seems to work for some reason.
	 * <p>
	 * Unfortunately continuing playback right after pausing doesn't work,
	 * probably due to concurrency issues with the native call. Therefore, in
	 * case the playback should continue after setting the time, we have to
	 * handle the media players pause event to continue (see {@link 
	 * #initPlayer()}).   
	 * <p>
	 * This workaround has just be tested with a small set of different 
	 * formats. So it might result in unexpected behavior for some others. 
	 * Solutions might be to change the media framework or hope for a fix in a
	 * later version of vlclib.
	 * <p>
	 * As for now (11.06.2014), version of vlclib is: 2.0.7
	 * 
	 * @param nano absolute time in nanoseconds
	 */
	@Override
	public void setTimeImpl(long nano) {
		this.p.setTime(nano / 1000000);
		p.setPause(true);
		this.p.setTime(nano / 1000000);
	}
}

