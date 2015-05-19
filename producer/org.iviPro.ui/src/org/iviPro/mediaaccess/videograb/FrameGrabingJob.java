package org.iviPro.mediaaccess.videograb;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import org.iviPro.model.resources.Video;

/**
 * Bean-Klasse die einen Job zum Grabben eines Frames kapselt.
 * 
 * @author juhoffma
 * 
 */
public class FrameGrabingJob {

	// die Zeit für das Bild (ab jetzt in Nanosekunden)
	private long timestamp;

	// das Video aus dem ein Bild gegrabed werden soll
	private Video video;

	// die Größe des Screenshots
	private Dimension imgBoundingBox;

	// Tag zum wiedererkennen des Jobs
	private String tag;
	
	// falls mehrere jobs angefordert werden kann ein Index für die Sortierung angegeben werden
	private int index;
	
	// das gegrabte Bild (wird natürlich erst nach dem graben gesetzt)
	private BufferedImage image;

	/**
	 * Constructs a job for extracting a frame of the given video at the given 
	 * timestamp and creating an image with the desired size and tag from this frame.
	 * @param timestamp	the desired frame's time in the video in nanoseconds
	 * @param imgBoundingBox size of the resulting image
	 * @param tag tag associated with the image
	 * @param video video to extract the image from
	 */
	public FrameGrabingJob(long timestamp, Dimension imgBoundingBox, String tag, Video video) {		
		init(timestamp, imgBoundingBox, tag, video);
	}

	public void init(long timestamp, Dimension imgBoundingBox, String tag, Video video) {
		this.timestamp = timestamp;
		this.imgBoundingBox = imgBoundingBox;
		this.tag = tag;
		this.video  = video;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return this.index;
	}

	@Override
	public String toString() {
		return "Job[tag=" + tag + ", time=" + (timestamp / 1000000000f) + "s]";
	}

	/**
	 * Gibt den Timestamp als Nano-Sekunden zurueck
	 * 
	 * @return Wert von timestamp in Nano-Sekunden
	 */
	public long getTimestampAsNanos() {
		// Rechne Hunderstel-Sekunden in Nano-Sekunden um <--- veraltet, ab jetzt wird timestamp in nanos gespeichert
		return timestamp; // * 10000000L;
	}

// Nicht benötigt
//	/**
//	 * Setzt den Timestamp in Nano-Sekunden. Der Wert wird jedoch auf
//	 * Hunderstel-Sekunden abgeschnitten.
//	 * 
//	 * @param timestamp
//	 *            Neuer Timestamp in Nano-Sekunden
//	 */
//	public void setTimestampAsNanos(long timestamp) {
//		// Nano-Sekunden in Hunderstel umrechnen
//		this.timestamp = (int) (timestamp / 10000000L);
//	}

	/**
	 * Gibt den Wert von imgBoundingBox zurueck
	 * 
	 * @return Wert von imgBoundingBox
	 */
	public Dimension getImgBoundingBox() {
		return imgBoundingBox;
	}

	/**
	 * Setzt den Wert von imgBoundingBox.
	 * 
	 * @param imgBoundingBox
	 *            Neuer Wert von imgBoundingBox
	 */
	public void setImgBoundingBox(Dimension imgBoundingBox) {
		this.imgBoundingBox = imgBoundingBox;
	}

	/**
	 * Gibt den Wert von tag zurueck
	 * 
	 * @return Wert von tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * Setzt den Wert von tag.
	 * 
	 * @param tag
	 *            Neuer Wert von tag
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}

	/**
	 * Gibt den Wert von mediaObject zurueck
	 * 
	 * @return Wert von mediaObject
	 */
	public Video getVideo() {
		return video;
	}
	
	public void setImage(BufferedImage img) {
		this.image = img;
	}
	
	public BufferedImage getImage() {
		return this.image;
	}
}
