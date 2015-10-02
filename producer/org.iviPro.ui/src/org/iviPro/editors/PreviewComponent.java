package org.iviPro.editors;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.iviPro.mediaaccess.videograb.FrameGrabingJob;
import org.iviPro.mediaaccess.videograb.framegraber.FrameGraberFactory;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;
import org.iviPro.theme.Colors;
import org.iviPro.utils.ImageHelper;

/**
 * Preview element offering the possibility to create and display thumbnail images
 * for different kinds of resources. Only style bits used by {@link Canvas} are allowed.
 * @author John
 *
 */
public class PreviewComponent extends Canvas {
	
	private static final int LABEL_X = 20;
	private static final int LABEL_Y = 10;
	private static final int MIN_PREVIEW_WIDTH = 224;
	private static final int MIN_PREVIEW_HEIGHT = 126;
		
	private int maxWidth;
	private int maxHeight;
	private double aspect; 
	
	/**
	 * Image drawn onto preview. Using BufferedImage instead of SWT's Image since
	 * image dimension information gets lost on conversion with ImageHelper. 
	 */
	private BufferedImage img = null;
	
	/**
	 * Label text shown on preview.
	 */
	private String label;
	
	/**
	 * Constructs a preview widget using the given parent and style information as 
	 * well as information about the maximum allowed size of the preview. 
	 * Only style bits used by {@link Canvas} are allowed.
	 * @param parent parent control
	 * @param style style to use
	 * @param maxWidth maximum allowed width of the preview
	 * @param maxHeight maximum allowed height of the preview
	 */
	public PreviewComponent(Composite parent, int style, int maxWidth, int maxHeight) {
		super(parent, style);
		this.addPaintListener(new PreviewPainter());
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		this.aspect = ((double)maxWidth/maxHeight);
	}	
	
	/**
	 * Returns the label text currently displayed by this component.	
	 * @return label text
	 */
	public String getLabel() {
		return label;
	}


	/**
	 * Set the label text displayed by this component to the given string. 
	 * @param label new label text
	 */
	public void setLabel(String label) {
		this.label = label;
		this.redraw();
	}

	/**
	 * Creates a preview for the given scene by extracting a frame.
	 * @param scene scene for which a preview is created
	 * @param time time in nanoseconds at which a frame is extracted
	 */
	public void setPreview(Scene scene, long time) {
		setPreview(scene.getVideo(), time);
	}
	
	/**
	 * Creates a preview for the given video by extracting a frame.
	 * @param video video for which a preview is created
	 * @param time time in nanoseconds at which a frame is extracted
	 */
	public void setPreview(Video video, long time) {
		drawPreview(getVideoFrame(video, time));
	}
	
	/**
	 * Resets the preview to its initial state showing an empty preview frame.
	 */
	public void resetPreview() {
		drawPreview(null);
	}
	
	/**
	 * Returns an image representing a frame of the given video at the given time.
	 * The image is scaled to the allowed maximum size of the preview component. 
	 * @param video video for which a thumbnail will be extracted
	 * @param time time in nanoseconds at which a frame should be extracted
	 * @return thumbnail image scaled to the maximum size of the preview
	 */
	private BufferedImage getVideoFrame(Video video, long time) {
		double vidAspect = video.getDimension().getWidth()/video.getDimension().getHeight();
		FrameGrabingJob job = new FrameGrabingJob(time, calcMaxDimension(vidAspect), "Standard preview", video); //$NON-NLS-1$
		FrameGraberFactory.getFrameGrabber().grabFrame(job);
		return job.getImage();
	}
	
	/**
	 * Returns the largest dimension which respects the given aspect ratio and
	 * maximum size of this preview component.
	 * @param ratio desired aspect ratio
	 * @return maximum dimension respecting aspect ratio and component size
	 */
	private Dimension calcMaxDimension(double ratio) {
		if (ratio > aspect) {
			return new Dimension(maxWidth,(int)(maxWidth/ratio));
		} else {
			return new Dimension((int)(maxHeight*ratio),maxHeight);
		}
	}

	/**
	 * Returns the preferred size of the receiver.
	 * <p /> 
	 * The preferred size of a control is the size that it would best be displayed at.
	 * <p />
	 * <b>Note:</b> Hint values other than SWT.DEFAULT will directly constrain the
	 * returned size, no matter whether the preview can be displayed  correctly with
	 * that constraint. Additionally, the returned size is restricted according to
	 * the maximum allowed size of the preview.
	 */
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point size = new Point(MIN_PREVIEW_WIDTH, MIN_PREVIEW_HEIGHT);
		if (img != null) {
			size.x = img.getWidth();
			size.y = img.getHeight();
		}
		if (wHint != SWT.DEFAULT) size.x = wHint;
		if (hHint != SWT.DEFAULT) size.y = hHint;
		if (size.x > maxWidth)	size.x = maxWidth;
		if (size.y > maxHeight) size.y = maxHeight;
		return size;
	};
	
	/**
	 * Draws the provided image to the component. The caller is responsible of
	 * resizing this preview component accordingly.
	 * @param img image to be drawn onto the component
	 */
	private void drawPreview(BufferedImage img) {
		this.img = img;
		this.redraw();
	}	
	
	/**
	 * Listener implementation used for drawing in this component.
	 * @author John
	 *
	 */
	private class PreviewPainter implements PaintListener {

		@Override
		public void paintControl(PaintEvent e) {
			// Create bold label font
			FontDescriptor boldDescriptor =
					FontDescriptor.createFrom(PreviewComponent.this.getFont()).setStyle(SWT.BOLD);
			Font boldFont = boldDescriptor.createFont(PreviewComponent.this.getDisplay());
			e.gc.setFont(boldFont);
			int previewWidth = MIN_PREVIEW_WIDTH;
			int previewHeight = MIN_PREVIEW_HEIGHT;
					
			if (img != null) {
				previewWidth = img.getWidth();
				previewHeight = img.getHeight();
				Image tmpImg = ImageHelper.getSWTImage(img);
				e.gc.drawImage(tmpImg, 0, 0);
				tmpImg.dispose();
			}
			if (label != null) {
				e.gc.drawText(label, LABEL_X, LABEL_Y, SWT.DRAW_DELIMITER);	
			}
			// Draw rectangle around preview area
			e.gc.setLineWidth(4);
			e.gc.setForeground(PreviewComponent.this.getBackground());
			e.gc.drawRectangle(2, 2, 
					previewWidth-4, previewHeight-4);
			e.gc.setForeground(Colors.VIDEO_OVERVIEW_ITEM_BG.getColor());
			e.gc.drawRoundRectangle(2, 2, 
					previewWidth-4, previewHeight-4, 20, 20);
			boldFont.dispose();
			e.gc.dispose();
		}	
	}
}
