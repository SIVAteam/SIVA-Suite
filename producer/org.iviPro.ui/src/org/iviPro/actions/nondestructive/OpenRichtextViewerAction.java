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
import org.iviPro.editors.richtextviewer.RichtextViewer;
import org.iviPro.editors.richtextviewer.RichtextViewerInput;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.resources.RichText;
import org.iviPro.theme.Icons;
import org.iviPro.views.mediarepository.MediaTreeLeaf;

public class OpenRichtextViewerAction extends Action {

	private static Logger logger = Logger
			.getLogger(OpenRichtextViewerAction.class);
	public final static String ID = OpenRichtextViewerAction.class.getName();

	private final IWorkbenchWindow window;

	private RichText richtext;

	/**
	 * Konstruktor...
	 * 
	 * @param window
	 */
	public OpenRichtextViewerAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setEnabled(false);
		setText(Messages.OpenRichtextViewerAction_Text);
		setToolTipText(Messages.OpenRichtextViewerAction_Tooltip);
		setImageDescriptor(Icons.ACTION_EDITOR_RICHTEXT.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_EDITOR_RICHTEXT
				.getDisabledImageDescriptor());			
				
		ISelectionListener selectionListener = new ISelectionListener() {
			@Override
			public void selectionChanged(IWorkbenchPart paramIWorkbenchPart, ISelection selection) {
				IStructuredSelection incoming = (IStructuredSelection) selection;
				if (incoming.getFirstElement() instanceof MediaTreeLeaf) {
					IAbstractBean mo = ((MediaTreeLeaf) incoming.getFirstElement())
							.getMediaObject();
					if (mo instanceof RichText) {
						richtext = (RichText) mo;
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
	 * wird verwendet, wenn nur der Editor geöffnet werden soll z.B. in der MediaViewAction
	 * 
	 * @param window
	 * @param picture
	 */
	public OpenRichtextViewerAction(IWorkbenchWindow window, RichText richtext) {
		this.window = window;
		this.richtext = richtext;
	}

	@Override
	public void run() {
		logger.debug("Running richtext editor..."); //$NON-NLS-1$
		if (richtext != null) {
			RichtextViewerInput input = new RichtextViewerInput(richtext);
			try {
				IWorkbenchPage page = window.getActivePage();				
				page.openEditor(input, RichtextViewer.ID, true);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}
}
