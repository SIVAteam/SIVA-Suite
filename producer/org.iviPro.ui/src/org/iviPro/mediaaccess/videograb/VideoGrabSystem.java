package org.iviPro.mediaaccess.videograb;

import java.awt.Dimension;
import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.iviPro.model.resources.Video;
import org.iviPro.utils.PathHelper;

public class VideoGrabSystem {
	
	/**
	 * Cache zum Laden von bereits auf der Platte gecachten Videobildern
	 */
	private static Cache cache = null;		
	private static VideoGrabSystem instance;	
	private static ThreadPoolExecutor exe;	
	private static LinkedBlockingQueue<Runnable> jobsQueue;
	
	// Anzahl der Thumbnails die gleichzeitig gecached werden sollen (Achtung bei Performance Problemen!
	private int numberThreads = 10;
	
	private VideoGrabSystem() {
		// Create cache
		cache = new Cache(PathHelper.getPathToImageCache());
		jobsQueue =  new LinkedBlockingQueue<Runnable>();		
		exe = new ThreadPoolExecutor(numberThreads, numberThreads, 0, TimeUnit.SECONDS, jobsQueue);	
		exe.prestartCoreThread();
	}
	
	public static VideoGrabSystem instance() {
		if (instance == null) {
			instance = new VideoGrabSystem();
		}
		return instance;
	}
		
	// Grabe Frame mit Angabe eines Index falls mehrere Bilder benötigt werden zum Sortieren
	// Instant sollte nur verwendet werden, wenn die Frames sofort benötigt werden (bei zu vielen sinkt die Performance, da der Thread sofort ausgeführt wird!)
	public void grabFrame(Video video, long timestamp, Dimension imgBoundingBox, String tag, VideoFrameConsumer consumer, int index, boolean instant) {
		FrameGrabingJob job = new FrameGrabingJob(timestamp, imgBoundingBox, tag, video);
		job.setIndex(index);
		VideoGrabThread vgj = new VideoGrabThread(job, cache, consumer);
		if (instant) {
			new Thread(vgj).start();	
		} else {
			jobsQueue.offer(vgj);
		}
	}
	
	// Grabe Frame
	// Instant sollte nur verwendet werden, wenn die Frames sofort benötigt werden (bei zu vielen sinkt die Performance, da der Thread sofort ausgeführt wird!)
	public void grabFrame(Video video, long timestamp, Dimension imgBoundingBox, String tag, VideoFrameConsumer consumer, boolean instant) {
		FrameGrabingJob job = new FrameGrabingJob(timestamp, imgBoundingBox, tag, video);
		VideoGrabThread vgj = new VideoGrabThread(job, cache, consumer);
		if (instant) {
			new Thread(vgj).start();
		} else {
			jobsQueue.offer(vgj);
		}		
	}		
}
