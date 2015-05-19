package org.iviPro.operations.global;

import java.awt.Dimension;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.mediaaccess.videograb.FrameGrabingJob;
import org.iviPro.mediaaccess.videograb.VideoFrameConsumer;
import org.iviPro.mediaaccess.videograb.VideoGrabSystem;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.SivaImage;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Aendern der Keywords eines Model-Objekts.
 * 
 * @author hoffmanj
 * 
 */
public class ChangeImagesOperation extends IAbstractOperation {
	
	private static final Logger logger = Logger.getLogger(ChangeImagesOperation.class);

	private final IAbstractBean target;
	private final LinkedList<SivaImage> newImages;
	private final LinkedList<SivaImage> oldImages;
	private SivaImage startImg = null;
	private SivaImage endImg = null;
	
	// das Video für Vorschaubilder
	private Video video = null;

	/**
	 * Erstellt eine neue Operation zum Aendern der Bilder eines Model-Objekts.
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Bilder geaendert werden soll.
	 * @param contentSource
	 *            Der lieferant der Bilder z.B. Scene, Video
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter null ist.
	 */
	public ChangeImagesOperation(IAbstractBean target, IAbstractBean contentSource)
			throws IllegalArgumentException {
		super(Messages.ChangeTitleOperation_Label);
		if (target == null && contentSource == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		newImages = new LinkedList<SivaImage>();
		this.oldImages = target.getImages();
		this.target = target;		
		prepareOperation(contentSource);
	}	
	
	
	/**
	 * Erstellt eine neue Operation zum Aendern der Bilder eines Model-Objekts.
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Bilder geaendert werden soll.
	 * @param images
	 *            Die neuen Bilder
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter null ist.
	 */
	public ChangeImagesOperation(IAbstractBean target)
			throws IllegalArgumentException {
		super(Messages.ChangeTitleOperation_Label);
		if (target == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		newImages = new LinkedList<SivaImage>();
		this.oldImages = target.getImages();
		this.target = target;
		prepareOperation(target);
	}
	
	/**
	 * Receives Images and sets old and new images
	 * @param imageSource the source for the images e.g. scene, video
	 */
	private void prepareOperation(IAbstractBean imageSource) {
		
		if (imageSource == null) {
			return;
		}
		
		if (!(imageSource instanceof Scene) && !(imageSource instanceof Video)) {
			return;
		}
		
		long start = 0;
		long end = 0;
		if (imageSource instanceof Scene) {
			video = ((Scene) imageSource).getVideo();
			start = ((Scene) imageSource).getStart();
			end = ((Scene) imageSource).getEnd();
		}
		if (imageSource instanceof Video) {
			video = (Video) imageSource;
			start = 200;
			end = video.getDuration() - 200;
		}		
		if (video == null) {
			return;
		}
				
		Dimension dim = new Dimension(140, 140);
		
		VideoGrabSystem.instance().grabFrame(video, start, dim, "START_IMG", new VideoFrameConsumer() { //$NON-NLS-1$

			@Override
			public void consumeGrabingJob(FrameGrabingJob job) {
				if (job.getTag().equals("START_IMG")) { //$NON-NLS-1$
					startImg = new SivaImage(job.getImage());
				}	
			}		
		}, true);			
		VideoGrabSystem.instance().grabFrame(video, end, dim, "END_IMG", new VideoFrameConsumer() { //$NON-NLS-1$

			@Override
			public void consumeGrabingJob(FrameGrabingJob job) {
				// TODO Auto-generated method stub
				if (job.getTag().equals("END_IMG")) { //$NON-NLS-1$
					endImg = new SivaImage(job.getImage());
				}
			}			
		}, true);			
		
		// Time limit to receive images in ms
		int limit = 2000;
		long startTime = System.currentTimeMillis();
		while (startImg == null || endImg == null) {
			if (System.currentTimeMillis() - startTime > limit) {
				logger.error("Could not capture start and end image of scene in time."); //$NON-NLS-1$
				break;
			}
		}
		newImages.add(startImg);
		newImages.add(endImg);
	}

	@Override
	public boolean canExecute() {
		return target != null && newImages != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ChangeTitleOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setImages(newImages);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setImages(oldImages);
		return Status.OK_STATUS;
	}

}
