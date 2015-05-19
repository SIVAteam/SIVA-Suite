package org.iviPro.editors.annotationeditor.components.contenteditors;


import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.iviPro.mediaaccess.videograb.FrameGrabingJob;
import org.iviPro.mediaaccess.videograb.framegraber.FrameGraberFactory;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;
import org.iviPro.utils.ImageHelper;

public class VideoEditor extends DragAndDropEditor {

	private final static Class[] CONTENTCLASSES = new Class[] {Video.class, Scene.class};
	
	public VideoEditor(Composite parent, int style,
			IAbstractBean editorContent) {
		super(parent, style, editorContent, CONTENTCLASSES);
	}
	
	@Override
	protected void updatePreviewDimension() {
		Video video;
		if (content instanceof Video) {
			video = (Video)content;
		} else {
			video = ((Scene)content).getVideo(); 
		}		
		double vidAspect = video.getDimension().getWidth()/video.getDimension().getHeight();
		if (vidAspect > PREVIEW_ASPECT) {
			previewDimension.setSize(PREVIEW_MAXWIDTH,(int)(PREVIEW_MAXWIDTH/vidAspect));
		} else {
			previewDimension.setSize((int)(PREVIEW_MAXHEIGHT*vidAspect),PREVIEW_MAXHEIGHT);
		}
	}
	
	@Override
	protected void updatePreviewText() {
		if (content instanceof Video) {
			previewText = Messages.VideoEditor_Label_Video + content.getTitle();
		} else {
			previewText = Messages.VideoEditor_Label_Scene + content.getTitle();
		}
		
	}
	
	@Override
	protected void paintContentPreview(GC gc) {
		Video video;
		if (content instanceof Video) {
			video = (Video)content;
		} else {
			video = ((Scene)content).getVideo(); 
		}		
		FrameGrabingJob job = new FrameGrabingJob(video.getDuration()/3, previewDimension, "Standard preview", video); //$NON-NLS-1$
		FrameGraberFactory.getFrameGrabber().grabFrame(job);
		gc.drawImage(ImageHelper.getSWTImage(job.getImage()), 0, 0);
	}
}
