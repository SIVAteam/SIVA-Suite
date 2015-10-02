package org.iviPro.editors.annotationeditor.components.contenteditors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.iviPro.application.Application;
import org.iviPro.dnd.TransferMedia;
import org.iviPro.dnd.TransferScene;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.resources.Subtitle;

/**
 * wird für Subtitle Annotationen verwendet
 * @author juhoffma
 */
public class SubtitleEditor extends ContentEditor {

	private Text editor;
	
	public SubtitleEditor(Composite parent, int style, final Subtitle subtitle) {
		super(parent, style);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setLayout(new GridLayout(1, false));
		
		editor = new Text(this, SWT.SINGLE);
		editor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		createDropTarget(this);
		editor.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				Subtitle newSubtitle = new Subtitle(subtitle.getTitle(), Application.getCurrentProject());
				newSubtitle.setDescription(editor.getText());				
				SivaEvent edEvent = new SivaEvent(SubtitleEditor.this, SivaEventType.CONTENT_CHANGED, newSubtitle);
				notifySivaEventConsumers(edEvent);
			}			
		});
		
		setContent(subtitle);					
	}
	
	public void setContent(IAbstractBean newContent) {
		if (editor != null) {
			if (newContent != null) {
				String title = "";
				if (newContent.getDescription() != null) {
					title = newContent.getDescription();
				}
				editor.setText(title);
				editor.setTopIndex(title.length());
			} else {
				editor.setText(""); //$NON-NLS-1$
			}
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
					if (objects.length > 0) {
						if (objects[0] instanceof Subtitle) {
							Subtitle newSub = (Subtitle) objects[0];
							String sub = newSub.getDescription();
							if (!sub.equals(editor.getText())) {
								editor.setText(sub);
								Subtitle newSubtitle = new Subtitle(newSub.getTitle(), Application.getCurrentProject());
								newSubtitle.setDescription(sub);
								SivaEvent edEvent = new SivaEvent(SubtitleEditor.this, SivaEventType.CONTENT_CHANGED, newSubtitle);
								notifySivaEventConsumers(edEvent);
							}
						}
					}
				}				
			};
		});
	}
}