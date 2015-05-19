package org.iviPro.mediaaccess.videograb;

import java.awt.image.BufferedImage;

import org.iviPro.mediaaccess.videograb.framegraber.FrameGraberFactory;
import org.iviPro.mediaaccess.videograb.interfaces.I_FrameGrabber;

public class VideoGrabThread implements Runnable {
	
	private FrameGrabingJob job;
	
	private I_FrameGrabber frameGraber;
		
	private Cache cache;
	private VideoFrameConsumer consumer;
		
	public VideoGrabThread(FrameGrabingJob job, Cache cache, VideoFrameConsumer consumer) {		
		this.job = job;
		this.frameGraber = FrameGraberFactory.getFrameGrabber();
		this.cache = cache;
		this.consumer = consumer;
	}
	
	@Override
	public void run() {
		
		BufferedImage cachedImage = cache.getImage(this.job);
		if (cachedImage != null) {
			this.job.setImage(cachedImage);
		} else {
			this.frameGraber.grabFrame(this.job);
			cache.writeImage(job, job.getImage());
		}				
		
		// sortiere Grabed Images aufsteigend nach der Zeit		
		consumer.consumeGrabingJob(job);	
	}
}
