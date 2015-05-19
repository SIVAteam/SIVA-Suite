package org.iviPro.mediaaccess.player;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.iviPro.application.Application;
import org.iviPro.editors.IAbstractEditor;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IMediaObject;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.model.resources.Video;
import org.iviPro.theme.Colors;
import org.iviPro.theme.Icons;

public class MediaPlayerView extends IAbstractEditor {
	private static Logger logger = Logger.getLogger(MediaPlayerView.class);
	public static final String ID = MediaPlayerView.class.getName();
	public static final String PREFIX_MOVIEEDITOR = "Player - "; //$NON-NLS-1$

	private I_MediaPlayer mp = null;
	
	private Composite root;

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
	}

	@Override
	public Image getDefaultImage() {
		return Icons.EDITOR_MEDIAPLAYER.getImage();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);

		IAbstractBean mo = ((MediaPlayerWidgetInput) input).getMediaObject();
		mo.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						setPartName(PREFIX_MOVIEEDITOR
								+ mp.getMediaObject().getTitle(Application.getCurrentLanguage()));
					}
				});				
			}
			
		});	
		if (mo instanceof Audio || mo instanceof Video) {
			mp = PlayerFactory.getPlayer((IMediaObject) mo);
		} else 
		if (mo instanceof AudioPart) {
			AudioPart part = (AudioPart) mo;
			mp = PlayerFactory.getPlayer(part.getAudio(), part.getStart(), part.getEnd());
		}
		setPartName(PREFIX_MOVIEEDITOR + mo.getTitle(Application.getCurrentLanguage()));
		logger.debug("MovieEditor initialized."); //$NON-NLS-1$
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControlImpl(Composite parent) {

		root = new Composite(parent, SWT.EMBEDDED);
		GridLayout layout = new GridLayout(1, false);
		root.setLayout(layout);
		root.setBackground(Colors.EDITOR_BG.getColor());
		root.setBackgroundMode(SWT.INHERIT_DEFAULT);

		if (Application.getDefault().isProjectOpen()) {
			// erstelle das Media Player Widget
			new MediaPlayerWidget(root, SWT.CENTER, mp, 420, true, false);
		}
	}

	@Override
	public void setFocus() {
		if (root != null) {
			root.setFocus();
		}
	}

	@Override
	public void dispose() {
		if (mp != null) {
			mp.stop();
			mp.finish();
		}
		super.dispose();
	}
}
