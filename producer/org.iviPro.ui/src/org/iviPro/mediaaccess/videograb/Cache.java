/**
 * 
 */
package org.iviPro.mediaaccess.videograb;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.iviPro.model.resources.Video;

/**
 * @author dellwo
 * 
 */
class Cache {

	/**
	 * Logger
	 */
	private static Logger logger = Logger.getLogger(Cache.class);

	/**
	 * 
	 */
	private File cacheDir = null;

	/**
	 * 
	 * @param cacheDir
	 */
	Cache(File cacheDir) {
		this.cacheDir = cacheDir;
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
	}

	/**
	 * 
	 * @param mo
	 * @param job
	 * @return
	 * @throws IOException
	 */
	BufferedImage getImage(FrameGrabingJob job) {
		File file = getFile(job);
		if (file.exists()) {
			logger.debug("Found in cache: '" + file.getAbsolutePath() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				return ImageIO.read(file);
			} catch (IOException e) {
				// TODO: Was machen bei IO-Fehler im Cache?
				logger.error(Messages.Cache_CacheFileError1
						+ file.getAbsolutePath() + ". " + e.getMessage()); //$NON-NLS-1$
			}
		} else {
			logger.debug("Not in cache: '" + file.getAbsolutePath() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return null;
	}

	/**
	 * Writes the given image to the cache directory. The given job
	 * determines the name of the stored file.
	 * @param job job to determine filename
	 * @param image image to store in cache
	 */
	void writeImage(FrameGrabingJob job, BufferedImage image) {
		File file = getFile(job);
		try {
			ImageIO.write(image, "jpeg", file); //$NON-NLS-1$
		} catch (IOException e) {
			// TODO: Was machen bei IO-Fehler im Cache?
			logger.error(Messages.Cache_CacheFileError2
					+ file.getAbsolutePath() + ". " + e.getMessage()); //$NON-NLS-1$
		}
		logger.debug("Writing file '" + file.getAbsolutePath() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private File getFile(FrameGrabingJob job) {
		String hashCode = getHashCode(job.getVideo());
		String filename = hashCode + "_" + job.getTimestampAsNanos() + ".jpg"; //$NON-NLS-1$ //$NON-NLS-2$
		File file = new File(cacheDir.getAbsolutePath() + File.separator
				+ filename);
		return file;
	}

	/**
	 * 
	 * @param mo
	 * @return
	 */
	private String getHashCode(Video video) {
		String moHashCode = "MO" + video.getFile().getAbsolutePath().hashCode(); //$NON-NLS-1$
		return moHashCode;
	}
}
