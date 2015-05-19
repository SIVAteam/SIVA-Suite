package org.iviPro.operations.media;

import java.awt.Canvas;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import org.iviPro.model.resources.Video;

import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

/**
 * wird dazu verwendet um die wirkliche Aufläsung des Videos herauszufinden.
 * Anhand eines Snapshots durch vlcj da sonst bei manchen Videos die falsche
 * Auflösung ausgelesen wird, die allerdings in manchen Teilen des Programms
 * benötigt wird.
 */
public class VideoDimensionSetter implements Runnable{
	String videoFilePath;
	Video vid;
	boolean taken = false;
	EmbeddedMediaPlayer player;
	Timer timer;
	
	public VideoDimensionSetter(Video video){
		this.videoFilePath = video.getFile().getAbsolutePath();
		this.vid = video;
	}
	
	@Override
	public void run(){
		final Frame f = new Frame();			
		Canvas c = new Canvas();
		f.add(c);
		f.pack();
		
		MediaPlayerFactory fac = new MediaPlayerFactory();
		player = fac.newEmbeddedMediaPlayer();
		player.setVideoSurface(fac.newVideoSurface(c));
		
		player.addMediaPlayerEventListener(new MediaPlayerEventAdapter(){
					
			@Override
			public void snapshotTaken(MediaPlayer mediaPlayer, String filename){
				if(!taken){	
					taken = true;
					BufferedImage img = null;
					try {
						img = ImageIO.read(new File(filename));
					} catch (IOException e) {
						e.printStackTrace();
					}
					vid.setDimension(img.getWidth(), img.getHeight());
					player.release();
					f.dispose();					
				}
			}
		});
		
		player.playMedia(videoFilePath);
		player.mute();
		timer = new Timer();
		timer.schedule(new SnapTask(), 400, 200);		
		
	}
	class SnapTask extends TimerTask{
		@Override
		public void run() {
			if(taken){
				timer.cancel();
				timer.purge();				
			}else{
				if(player != null){
					player.saveSnapshot();
				}
			}
			
		}				
	}
}
