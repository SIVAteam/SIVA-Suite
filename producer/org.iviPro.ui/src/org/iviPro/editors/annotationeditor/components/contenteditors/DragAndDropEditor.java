package org.iviPro.editors.annotationeditor.components.contenteditors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.iviPro.application.Application;
import org.iviPro.dnd.TransferMedia;
import org.iviPro.dnd.TransferScene;
import org.iviPro.editors.PreviewComponent;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.model.IAbstractBean;


/**
 * A generic editor for changing the content of annotations using drag and drop.
 * 
 * @author John
 *
 */
public class DragAndDropEditor extends ContentEditor {
		
	protected static final int PREVIEW_MAXWIDTH = 640;
	protected static final int PREVIEW_MAXHEIGHT = 360;
		
	protected PreviewComponent previewComponent;	
	protected IAbstractBean content;
		
	private final Class[] contentClasses;
	private DropTarget target;

	
	/**
	 * Constructs a DragAndDropEditor offering a drop zone onto which content of the
	 * defined contentClasses can be dropped. The editor also offers a preview of the
	 * currently assigned content.
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
		createComponents();				
		setContent(editorContent);
	}
	
	/**
	 * Creates the components of the editor.
	 */
	protected void createComponents() {
		GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 10;
		setLayout(layout);
		setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		previewComponent = new PreviewComponent(this, SWT.NONE,
				PREVIEW_MAXWIDTH, PREVIEW_MAXHEIGHT);
		previewComponent.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		setInfoLabel();
		createDropTarget(previewComponent);
	}
	
	/**
	 * Creates the drop zone of the editor.
	 * @param dropTarget
	 */
	private void createDropTarget(final Composite dropTarget) {
		target = new DropTarget(dropTarget, DND.DROP_MOVE);
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
	
	/**
	 * Sets the content of this editor to the given IAbstractBean.
	 * @param newContent new content of this editor
	 */
	public void setContent(IAbstractBean newContent) {
		content = newContent;
		if (content != null) {
			displayContent(content);			
		} else {
			previewComponent.resetPreview();
			setInfoLabel();
		}
		this.getParent().layout();
		// Inform AbstractAnnotationDefineWidget about content change
		SivaEvent edEvent = new SivaEvent(DragAndDropEditor.this, 
				SivaEventType.CONTENT_CHANGED, content);
		notifySivaEventConsumers(edEvent);
	}
	
	/**
	 * Displays the given content in the editor.
	 * @param content content to be displayed
	 */
	protected void displayContent(IAbstractBean content) {
		previewComponent.setLabel(Messages.DragAndDropEditor_Label_Title 
				+ content.getTitle());
	};
	
	/**
	 * Show drag&drop information in the label of the preview component.
	 */
	protected void setInfoLabel() {
		previewComponent.setLabel(Messages.DragAndDropEditor_Label_DragMedia);
	}
	
	@Override
	public void dispose() {
		target.dispose();
		super.dispose();
	}
}
