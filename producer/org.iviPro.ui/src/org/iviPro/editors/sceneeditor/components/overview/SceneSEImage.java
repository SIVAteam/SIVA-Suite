package org.iviPro.editors.sceneeditor.components.overview;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.iviPro.mediaaccess.videograb.FrameGrabingJob;
import org.iviPro.mediaaccess.videograb.VideoFrameConsumer;
import org.iviPro.mediaaccess.videograb.VideoGrabSystem;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;
import org.iviPro.theme.Colors;
import org.iviPro.utils.ImageHelper;

/**
 * Composite, das das Start und Endbild einer Szene anzeigt Die Klasse kann
 * sowohl auf Property Changes als gespeicherte Änderungen als auch temporäre
 * (TimeChangedListener) reagieren.
 * 
 * @author juhoffma
 * 
 */
public class SceneSEImage extends Composite {

	private static Logger logger = Logger.getLogger(SceneSEImage.class);

	/**
	 * Tag zum Markieren des Grabbing-Jobs der zu einer Szene gehören
	 */
	private static final String JOB_TAG = "SCENESEIMAGE_JOB,"; //$NON-NLS-1$

	// die beiden Bilder
	private Image imgStart = null;
	private Image imgEnd = null;

	// Start und Endzeit
	private long startTime = 0;
	private long endTime = 0;

	// Dimension der zu holenden Bilder
	private Dimension dim;

	// das Video von dem die Bilder geholt werden
	private Video vid;
	
	// Die Composites in denen die Bilder angezeigt werden
	Composite imgCompStart;
	Composite imgCompEnd;
	
	// der MediaInfoConsumer für das grabben der Frames
	VideoFrameConsumer miConsumer;

	/**
	 * Die Zeiten müssen der Gesamtzeit im Video entsprechen
	 * 
	 * @param parent
	 * @param style
	 * @param start
	 *            Startzeit bezogen auf das Video
	 * @param end
	 *            Endzeit bezogen auf das Video
	 * @param dimX
	 *            Dimension für das Bild Breite
	 * @param dimY
	 *            Dimension für das Bild Höhe
	 */
	public SceneSEImage(Composite parent, int style, long start, long end,
			Video vid, final int dimX, final int dimY, final Scene scene) {
		super(parent, style);
		this.startTime = start;
		this.endTime = end;
		this.dim = new Dimension(dimX, dimY);
		this.vid = vid;

		GridData gridData = new GridData();
		gridData.widthHint = 2 * dimX;
		gridData.heightHint = dimY;		
		setLayoutData(gridData);
		setLayout(new GridLayout(2, true));
				
		setBackground(Colors.DEFAULT_VIDEOFRAME_BACKGROUND.getColor());
				
		imgCompStart = new Composite(this, SWT.CENTER);
		imgCompStart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		imgCompEnd = new Composite(this, SWT.CENTER);
		imgCompEnd.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		scene.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (isDisposed()) {
					return;
				}

				// Datei wurde gespeichert
				if (evt.getSource() instanceof Scene) {
					Scene chScene = (Scene) evt.getSource();
					if (scene.getTitle().equals(chScene.getTitle())) {
						startTime = chScene.getStart();			
						endTime = chScene.getEnd();
						grabImages();	
					}					
				}
			}			
		});
		
		// der Media Info Conumser zum Grabben der Images
		miConsumer = new VideoFrameConsumer() {

			@Override
			public void consumeGrabingJob(final FrameGrabingJob job) {
				if (isDisposed()) {
					return;
				}

				logger.debug("frame"); //$NON-NLS-1$
				String tag = job.getTag();

				// prüfe ob das Bild überhaupt für eine SingleSceneBar bestimmt ist
				if (!tag.startsWith(JOB_TAG)) {
					return;
				}

				// Parse die Frame-Nummer aus dem Tag, um das richtige Thumbnail zu
				// bekommen
				final long time = Long.parseLong(tag.split(",")[1]); //$NON-NLS-1$

				// das Startbild wurde geliefert
				if (time == startTime) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							// falls die Zeit des Bildes mit der Startzeit übereinstimmt
							// wurde das richtige Bild gegrabbed
							imgStart = ImageHelper.getSWTImage(job.getImage());
							Image scaled = new Image(Display.getCurrent(), imgStart.getImageData().scaledTo(dimX, dimY));
							if (!imgCompStart.isDisposed()) {
								imgCompStart.setBackgroundImage(scaled);
							}
							imgStart.dispose();
						}
					});
				}

				// das Endbild wurde geliefert
				if (time == endTime) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							// falls die Zeit des Bildes mit der Endzeit übereinstimmt
							// wurde das richtige Bild gegrabbed
							imgEnd = ImageHelper.getSWTImage(job.getImage());
							Image scaled = new Image(Display.getCurrent(), imgEnd.getImageData().scaledTo(dimX, dimY));
							if (!imgCompStart.isDisposed()) {
								imgCompEnd.setBackgroundImage(scaled);
							}
							imgEnd.dispose();
						}
					});
				}
			}	
		};
		grabImages();
	}
	
	private void grabImages() {
		VideoGrabSystem.instance().grabFrame(vid, startTime, dim, JOB_TAG + startTime, miConsumer, true);
		VideoGrabSystem.instance().grabFrame(vid, endTime, dim, JOB_TAG + endTime, miConsumer, true);
		layout();
	}
}
