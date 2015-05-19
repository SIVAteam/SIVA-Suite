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
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.editors.imageviewer.ImageViewer;
import org.iviPro.editors.imageviewer.ImageWidgetInput;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.PictureGallery;
import org.iviPro.model.resources.Picture;
import org.iviPro.theme.Icons;
import org.iviPro.views.mediarepository.MediaTreeLeaf;

public class OpenImageViewerAction extends Action implements
		ISelectionListener, IWorkbenchAction {

	private static Logger logger = Logger
			.getLogger(OpenImageViewerAction.class);
	public final static String ID = OpenImageViewerAction.class.getName();

	private final IWorkbenchWindow window;

	private IAbstractBean selectedPicture;

	/**
	 * Konstruktor...
	 * 
	 * @param window
	 */
	public OpenImageViewerAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setEnabled(false);
		setText(Messages.ViewImage_ViewImage);
		setToolTipText(Messages.ViewImage_ViewImageToolTip);
		setImageDescriptor(Icons.ACTION_EDITOR_IMAGE.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_EDITOR_IMAGE
				.getDisabledImageDescriptor());
		window.getSelectionService().addSelectionListener(this);
	}

	/**
	 * wird verwendet, wenn die Action direkt aufgerufen wird z.B. über Doppelklick
	 * 
	 * @param window
	 * @param picture
	 */
	public OpenImageViewerAction(IWorkbenchWindow window, IAbstractBean picture) {
		this.window = window;
		this.selectedPicture = picture;
	}

	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

	/**
	 * Die eigentliche Ausführungslogik der Action.
	 */
	@Override
	public void run() {		
		logger.debug("Running image viewer..."); //$NON-NLS-1$
		if (selectedPicture != null) {
			ImageWidgetInput input = new ImageWidgetInput(selectedPicture);
			try {
				IWorkbenchPage page = window.getActivePage();
				page.openEditor(input, ImageViewer.ID, true);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}		
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {

		IStructuredSelection incoming = (IStructuredSelection) selection;
		if (incoming.getFirstElement() instanceof MediaTreeLeaf) {
			IAbstractBean mo = ((MediaTreeLeaf) incoming.getFirstElement())
					.getMediaObject();
			if (mo instanceof Picture || mo instanceof PictureGallery) {
				selectedPicture = mo;
				setEnabled(true);
			} else {
				setEnabled(false);
			}
		} else {
			setEnabled(false);
		}
	}
}
