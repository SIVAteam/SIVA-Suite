package org.iviPro.actions.nondestructive;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.iviPro.editors.subtitleviewer.SubtitleViewer;
import org.iviPro.editors.subtitleviewer.SubtitleViewerInput;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.resources.Subtitle;
import org.iviPro.theme.Icons;
import org.iviPro.views.mediarepository.MediaTreeLeaf;

public class OpenSubtitleViewerAction extends Action {

	private static Logger logger = Logger.getLogger(OpenSubtitleViewerAction.class);
	public final static String ID = OpenSubtitleViewerAction.class.getName();

	private final IWorkbenchWindow window;

	private Subtitle subtitle;

	/**
	 * Konstruktor...
	 * 
	 * @param window
	 */
	public OpenSubtitleViewerAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setEnabled(false);
		setText(Messages.OpenSubtitleViewerAction_Text);
		setToolTipText(Messages.OpenSubtitleViewerAction_Tooltip);
		setImageDescriptor(Icons.ACTION_EDITOR_SUBTITLE.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_EDITOR_SUBTITLE.getDisabledImageDescriptor());			
				
		ISelectionListener selectionListener = new ISelectionListener() {
			@Override
			public void selectionChanged(IWorkbenchPart paramIWorkbenchPart, ISelection selection) {
				IStructuredSelection incoming = (IStructuredSelection) selection;
				if (incoming.getFirstElement() instanceof MediaTreeLeaf) {
					IAbstractBean mo = ((MediaTreeLeaf) incoming.getFirstElement())
							.getMediaObject();
					if (mo instanceof Subtitle) {
						subtitle = (Subtitle) mo;
						setEnabled(true);
					} else {
						setEnabled(false);
					}
				} else {
					setEnabled(false);
				}				
			}
		};
		window.getSelectionService().addSelectionListener(selectionListener);		
	}

	/**
	 * wird verwendet, wenn nur der Editor geöffnet werden soll
	 * 
	 * @param window
	 * @param picture
	 */
	public OpenSubtitleViewerAction(IWorkbenchWindow window, Subtitle subtitle) {
		this.window = window;
		this.subtitle = subtitle;
	}

	@Override
	public void run() {
		logger.debug("Running subtitle viewer..."); //$NON-NLS-1$
		if (subtitle != null) {
			SubtitleViewerInput input = new SubtitleViewerInput(subtitle);
			try {
				IWorkbenchPage page = window.getActivePage();				
				page.openEditor(input, SubtitleViewer.ID, true);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}
}
