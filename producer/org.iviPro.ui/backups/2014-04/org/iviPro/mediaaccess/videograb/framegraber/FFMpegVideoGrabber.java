package org.iviPro.mediaaccess.videograb.framegraber;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageIO;
import org.iviPro.export.ExportException;
import org.iviPro.export.ffmpeg.StreamHandler;
import org.iviPro.mediaaccess.videograb.FrameGrabingJob;
import org.iviPro.mediaaccess.videograb.interfaces.I_FrameGrabber;
import org.iviPro.model.Video;
import org.iviPro.utils.PathHelper;

public class FFMpegVideoGrabber implements I_FrameGrabber {

	@Override
	public void grabFrame(FrameGrabingJob job) {
		job.setImage(grabImage(job));
	}
	
	private BufferedImage grabImage(FrameGrabingJob job) {
		String ffmpeg = PathHelper.getPathToFFMpegExportExe().getAbsolutePath();
		Video video = job.getVideo();
		String inputFilename = video.getFile().getAbsolutePath();
		
		String outputFilename = "tmpCap" + job.hashCode() + "" + job.getTimestampAsNanos() + ".jpg";
		outputFilename = System.getProperty("java.io.tmpdir") + outputFilename;
		
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
		String timeString = String.format(Locale.ENGLISH, "%.3f",  timeInSeconds);		
				
		//String dimString = (int) video.getDimension().getWidth() + "x" + (int) video.getDimension().getHeight();
		String cmd = ffmpeg + " -ss " + timeString + " -i \"" + inputFilename + "\"" + " -vframes 1 -f mjpeg " + outputFilename;
		
		// holen und Ausführen der Runtime
		Runtime rt = Runtime.getRuntime();
		Process p;
		
		try {
			p = rt.exec(cmd);			
			StreamHandler errorReader = new StreamHandler(p.getErrorStream(), "stderr");
			StreamHandler outputReader = new StreamHandler(p.getInputStream(), "stdout"); 
			errorReader.start();
			outputReader.start();
			
			// warten auf Beendigung
			int exitVal = p.waitFor();
			
			// Falls ein Fehler auftrat liefert ffmpeg einen exit-Wert != 0
			if (exitVal != 0) {
				String errorMsg = "FFmpeg failed with error code: " + exitVal; //$NON-NLS-1$				
				throw new ExportException(errorMsg);
			}
			File filew = new File(outputFilename);
			BufferedImage img = ImageIO.read(new File(outputFilename));

			if (filew.exists()) {
				filew.delete(); 
			}
			return img;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ExportException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return null;
	}

	
	
}
