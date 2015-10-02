package org.iviPro.editors.annotationeditor.components.contenteditors;


import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.iviPro.editors.TimeSelector;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;

public class VideoEditor extends DragAndDropEditor implements SivaEventConsumerI {

	private final static Class[] CONTENTCLASSES = new Class[] {Video.class, Scene.class};
	
	public VideoEditor(Composite parent, int style,	IAbstractBean editorContent) {
		super(parent, style, editorContent, CONTENTCLASSES);
		previewComponent.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				openTimeSelector();
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	protected void displayContent(IAbstractBean content) {
		if (content instanceof Video) {
			previewComponent.setLabel(Messages.VideoEditor_Label_Video 
					+ content.getTitle());
			previewComponent.setPreview((Video)content, 
					((Video)content).getThumbnail().getTime());
		} else if (content instanceof Scene) {
			previewComponent.setLabel(Messages.VideoEditor_Label_Scene 
					+ content.getTitle());
			previewComponent.setPreview((Scene)content, 
					((Scene)content).getThumbnail().getTime());
		}
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	protected void setInfoLabel() {
		previewComponent.setLabel(Messages.VideoEditor_Label_DragMedia);
	}
	
	/**
	 * Opens a TimeSelector for the current content of the editor.
	 */
	private void openTimeSelector() {
		Video video;
		long start;
		long end;
		if (content instanceof Video) {
			video = (Video)content;
			start = 0;
			end = video.getDuration();
		} else if (content instanceof Scene) {
			video = ((Scene)content).getVideo();
			start = ((Scene)content).getStart();
			end = ((Scene)content).getEnd();
		} else {
			return;
		}		
		TimeSelector selector;
		selector = new TimeSelector(video, 
				PREVIEW_MAXWIDTH, PREVIEW_MAXHEIGHT,
				start, end);
		selector.addSivaEventConsumer(this);
	}
	
	@Override
	public void handleEvent(SivaEvent event) {
		// React on choosing a new thumbnail time in ThumbnailSelector
		if (event.getEventType() == SivaEventType.TIME_SELECTION) {
			if (content instanceof Video) {
				previewComponent.setPreview((Video)content, 
						(Long)event.getValue());
			} if (content instanceof Scene) {
				previewComponent.setPreview((Scene)content, 
						(Long)event.getValue());
			}
			// Pass on event 
			notifySivaEventConsumers(event);
		}
	}
}
