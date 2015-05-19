package org.iviPro.editors.annotationeditor.components.contenteditors;

import java.awt.Dimension;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.iviPro.application.Application;
import org.iviPro.dnd.TransferMedia;
import org.iviPro.dnd.TransferScene;
import org.iviPro.editors.common.SivaComposite;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.model.IAbstractBean;
import org.iviPro.theme.Colors;


/**
 * A generic editor for changing the content of annotations using drag and drop.
 * 
 * @author John
 *
 */
public class DragAndDropEditor extends SivaComposite {
	
	private static final int DROPZONE_WIDTH = 224;
	private static final int DROPZONE_HEIGHT = 126;
	
	protected static final int PREVIEW_MAXWIDTH = 640;
	protected static final int PREVIEW_MAXHEIGHT = 360;
	protected static final double PREVIEW_ASPECT = 
			((double)PREVIEW_MAXWIDTH)/PREVIEW_MAXHEIGHT;
	
	private final Class[] contentClasses;
	private Composite dropComposite;
	private GridData dropGD;
	protected IAbstractBean content;
	protected Dimension previewDimension = new Dimension(DROPZONE_WIDTH, DROPZONE_HEIGHT);
	protected String previewText = Messages.DragAndDropEditor_Label_DragMedia;

	private int textX = 20;
	private int textY = 10;
	
	/**
	 * 
	 * @param parent parent composite
	 * @param style SWT style used
	 * @param editorContent initial content of the editor
	 * @param contentClasses array of classes determining the object types which can
	 * be handled as content types by this editor
	 */
	public DragAndDropEditor (Composite parent, int style, final IAbstractBean editorContent,
			Class[] contentClasses) {
		super(parent, style);
		this.contentClasses = contentClasses;
				
		setLayout(new GridLayout(1, false));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		dropComposite = new Composite(this, SWT.NONE);			
		createDropTarget(dropComposite);
		
		dropGD = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		dropGD.widthHint = DROPZONE_WIDTH;
		dropGD.heightHint = DROPZONE_HEIGHT;
		dropComposite.setLayoutData(dropGD);
				
		dropComposite.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				FontDescriptor boldDescriptor =
						FontDescriptor.createFrom(dropComposite.getFont()).setStyle(SWT.BOLD);
				Font boldFont = boldDescriptor.createFont(dropComposite.getDisplay());
				e.gc.setFont(boldFont);
				
				if (content != null) {
					paintContentPreview(e.gc);
					e.gc.drawText(previewText, textX, textY, 
							SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER);
				} else {
					e.gc.drawText(previewText, textX, textY, SWT.DRAW_DELIMITER);	
				}
				e.gc.setLineWidth(4);
				e.gc.setForeground(dropComposite.getBackground());
				e.gc.drawRectangle(2, 2, 
						previewDimension.width-4, previewDimension.height-4);
				e.gc.setForeground(Colors.VIDEO_OVERVIEW_ITEM_BG.getColor());
				e.gc.drawRoundRectangle(2, 2, 
						previewDimension.width-4, previewDimension.height-4, 20, 20);
				boldFont.dispose();
				e.gc.dispose();
			}	
		});
		
		if (editorContent != null) {
			setContent(editorContent);
		}	
	}
	
	
	private void createDropTarget(final Composite dropTarget) {
		DropTarget target = new DropTarget(dropTarget, DND.DROP_MOVE);
		Transfer[] supportedTransferTypes = new Transfer[] {
				TransferScene.getInstance(), TransferMedia.getInstance() };
		target.setTransfer(supportedTransferTypes);
		target.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) {
				if (event.widget instanceof DropTarget) {
					String transferKey = event.data.toString();
					Object[] objects = Application.getDragDropManager()
							.endTransfer(transferKey);
					if (objects.length == 1) {
						if (hasContentType(objects[0])) {
							IAbstractBean newContent = (IAbstractBean) objects[0];
							if (!newContent.equals(content)) {
								setContent(newContent);
								SivaEvent edEvent = new SivaEvent(DragAndDropEditor.this, 
										SivaEventType.EDITOR_CHANGED, content);
								notifySivaEventConsumers(edEvent);
							}
						}
					}
				}				
			};
		});
	}
	
	/**
	 * Checks if the parameter object is of a type which may be used as content for this editor.
	 * @param object object for which type is checked
	 */
	private boolean hasContentType(Object object) {
		for (Class cl : contentClasses) {
			if (cl.isInstance(object)) {
				return true;
			}
		}
		return false;
	}
	
	public void setContent(IAbstractBean newContent) {
		content = newContent;
		updatePreviewDimension();
		updatePreviewText();
		dropGD.widthHint = previewDimension.width;
		dropGD.heightHint = previewDimension.height;
		dropComposite.getParent().layout(true, true);
		dropComposite.redraw();
		dropComposite.update();
	}
	
	protected void updatePreviewDimension() {
		// In the standard implementation the drop zone is not resized
	};
	
	protected void updatePreviewText() {
		previewText = Messages.DragAndDropEditor_Label_Title + content.getTitle();
	}
	
	protected void paintContentPreview(GC gc) {
		// In the standard implementation no preview is added
	};
}
