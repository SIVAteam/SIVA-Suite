package org.iviPro.mediaaccess.videograb.framegraber;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.iviPro.mediaaccess.MediaAccessException;
import org.iviPro.mediaaccess.StreamHandler;
import org.iviPro.mediaaccess.videograb.FrameGrabingJob;
import org.iviPro.mediaaccess.videograb.interfaces.I_FrameGrabber;
import org.iviPro.model.resources.Video;
import org.iviPro.utils.ImageHelper;
import org.iviPro.utils.PathHelper;

public class FFMpegVideoGrabber implements I_FrameGrabber {
	
	private static final Logger logger = Logger.getLogger(FFMpegVideoGrabber.class);

	@Override
	public void grabFrame(FrameGrabingJob job) {
		try {
			BufferedImage img = createImageFromFrame(job);
			// The captured image needs to be scaled again to fit the size defined 
			// in the job
			img = ImageHelper.getScaledImage(img, job.getImgBoundingBox(), true);
			job.setImage(img);
		} catch (MediaAccessException e) {
			logger.error(e);
			// Create black placeholder img
			BufferedImage img = new BufferedImage(job.getImgBoundingBox().width,
					job.getImgBoundingBox().height, BufferedImage.TYPE_BYTE_BINARY);
			job.setImage(img);
		}
	}
	
	private BufferedImage createImageFromFrame(FrameGrabingJob job) throws MediaAccessException {
		String ffmpeg = PathHelper.getPathToFFMpegExe().getAbsolutePath();
		Video video = job.getVideo();
		String inputFilename = video.getFile().getAbsolutePath();
		
		String outputFilename = "tmpCap" + job.hashCode() + "" + job.getTimestampAsNanos() + ".jpg"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		outputFilename = System.getProperty("java.io.tmpdir") + outputFilename; //$NON-NLS-1$
		
		File file = new File(outputFilename);
		if (file.exists()) {
			file.delete();
		}
		
		long time = job.getTimestampAsNanos();
		/*
		 * ffmpeg kann derzeit den letzten Frame nur eingeschränkt extrahieren. 
		 * Zur Sicherheit 1 Sekunde Puffer.
		 */
		long oneSec = 1000000000L;
		if (time > job.getVideo().getDuration()-oneSec && time > oneSec) {
			time -= oneSec;
		}
		float timeInSeconds = time / 1000000000.0f;
		String timeString = String.format(Locale.ENGLISH, "%.3f",  timeInSeconds);		 //$NON-NLS-1$
				
		//String dimString = (int) video.getDimension().getWidth() + "x" + (int) video.getDimension().getHeight();
		String cmd = ffmpeg + " -ss " + timeString + " -i \"" + inputFilename + "\"" + " -vframes 1 -f mjpeg " + outputFilename; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
		// holen und Ausführen der Runtime
		Runtime rt = Runtime.getRuntime();
		Process p;		
		File tmpFile = null;		
		try {
			p = rt.exec(cmd);			
			StreamHandler errorReader = new StreamHandler(p.getErrorStream(), "stderr"); //$NON-NLS-1$
			StreamHandler outputReader = new StreamHandler(p.getInputStream(), "stdout"); //$NON-NLS-1$
			errorReader.start();
			outputReader.start();
			
			// warten auf Beendigung
			int exitVal = p.waitFor();
			
			// Falls ein Fehler auftrat liefert ffmpeg einen exit-Wert != 0
			if (exitVal != 0) {
				String errorMsg = "Could not extract image from frame.\n" //$NON-NLS-1$
						+ "FFmpeg failed with error code: " + exitVal //$NON-NLS-1$	
						+ "\n" + errorReader.getString(); //$NON-NLS-1$
				throw new MediaAccessException(errorMsg);
			}
			
			// Process file written by ffmpeg
			tmpFile = new File(outputFilename);
			BufferedImage img = ImageIO.read(tmpFile);
			// In case of videos with a pixel aspect ratio other than 1:1 the captured frame
			// needs to be scaled to aspect ratio of the video.
			img = ImageHelper.getScaledImage(img, video.getDimension(), false);
			return img;
						
		} catch(IOException e) {
			throw new MediaAccessException("Could not extract image from frame.", e); //$NON-NLS-1$
		} catch (InterruptedException e) {
			throw new MediaAccessException("Could not extract image from frame.", e); //$NON-NLS-1$
		} finally {
			if (tmpFile != null) {
				tmpFile.delete();
			}
		}
	}
}
