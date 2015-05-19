package org.iviPro.editors.annotationeditor.components.contenteditors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.iviPro.application.Application;
import org.iviPro.dnd.TransferMedia;
import org.iviPro.dnd.TransferScene;
import org.iviPro.editors.common.SivaComposite;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.theme.Colors;

public class AudioEditor extends SivaComposite {
	
	private Audio audio;
	private AudioPart audioPart;
	private Composite dropComposite;
		
	// Breite und Höhe der Dropzone
	private final int widthDrop = 200;
	private final int heightDrop = 150;
	
	private String insertText = Messages.AudioEditor_0;
	private int textX = 20;
	private int textY = 10;
	
	private String title = Messages.AudioEditor_1;

	public AudioEditor(Composite parent, int style, final Object newAudio) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		if (newAudio instanceof Audio) {
			this.audio = (Audio) newAudio;
		} else
		if (newAudio instanceof AudioPart) {
			this.audioPart = (AudioPart) newAudio;
		}
		dropComposite = new Composite(this, SWT.CENTER);			
		createDropTarget(dropComposite);
		
		GridData dropGD = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		dropGD.widthHint = this.widthDrop;
		dropGD.heightHint = this.heightDrop;
		dropComposite.setLayoutData(dropGD);

		final Color parentBackgroundColor = parent.getBackground();
		dropComposite.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {				
				if (audio != null) {
					e.gc.drawText(title + audio.getTitle(), textX, textY);
				} else
				if (audioPart != null) {
					e.gc.drawText(title + audioPart.getTitle(), textX, textY);
				} else {
					e.gc.drawText(insertText, textX, textY,SWT.DRAW_DELIMITER);	
				}
				e.gc.setLineWidth(4);
				e.gc.setForeground(parentBackgroundColor);
				e.gc.drawRectangle(2, 2, widthDrop -4 ,heightDrop - 4);
				
				e.gc.setForeground(Colors.VIDEO_OVERVIEW_ITEM_BG.getColor());
				e.gc.drawRoundRectangle(2, 2, widthDrop-4, heightDrop-4, 20, 20);
				e.gc.dispose();
			}	
		});
	}
	
	public void setAudio(Object newAudio) {
		if (newAudio instanceof Audio) {
			this.audio = (Audio) newAudio;
			this.audioPart = null;
		} else
		if (newAudio instanceof AudioPart) {
			this.audioPart = (AudioPart) newAudio;
			this.audio = null;
		}
		dropComposite.redraw();
		dropComposite.update();
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
						if (objects[0] instanceof Audio) {
							Audio newAudio = (Audio) objects[0];
							if (!newAudio.equals(audio)) {
								audio = (Audio) objects[0];
								if (audio != null) {
									setAudio(audio);
								}
								SivaEvent edEvent = new SivaEvent(AudioEditor.this, SivaEventType.EDITOR_CHANGED, audio);
								notifySivaEventConsumers(edEvent);
							}
						} else 
						if (objects[0] instanceof AudioPart) {
							AudioPart newAudio = (AudioPart) objects[0];
							if (!newAudio.equals(audioPart)) {
								audioPart = (AudioPart) objects[0];
								if (audioPart != null) {
									setAudio(audioPart);
								}
								SivaEvent edEvent = new SivaEvent(AudioEditor.this, SivaEventType.EDITOR_CHANGED, audioPart);
								notifySivaEventConsumers(edEvent);
							}
						}
					}
				}				
			};
		});
	}
}
