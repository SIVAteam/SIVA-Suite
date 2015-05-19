package org.iviPro.actions.undoable;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.model.IAbstractBean;
import org.iviPro.operations.global.NewFolderInMediaRepOperation;
import org.iviPro.theme.Icons;
import org.iviPro.views.mediarepository.MediaRepository;
import org.iviPro.views.mediarepository.MediaTreeGroup;

public class NewFolderAction extends AbstractUndoableAction implements ISelectionListener,
		IWorkbenchAction {

	public final static String ID = NewFolderAction.class.getName();

	private final IWorkbenchWindow window;
	private IAbstractBean mediaToRename;
	private TreeViewer treeViewer;
	private MediaRepository rep;

	private boolean editActive = false;

	/**
	 * Konstruktor...
	 * @param window
	 */
	public NewFolderAction(IWorkbenchWindow window, TreeViewer treeViewer, MediaRepository rep) {
		super(window);
		this.window = window;
		setEnabled(false);
		setId(ID);
		this.treeViewer = treeViewer;
		this.rep = rep;
		
		setText(Messages.NewFolderAction_Title);
		setToolTipText(Messages.NewFolderAction_Title);
		setImageDescriptor(Icons.VIEW_FOLDER.getImageDescriptor());
		setDisabledImageDescriptor(Icons.VIEW_FOLDER
				.getDisabledImageDescriptor());
		window.getSelectionService().addSelectionListener(this);
		NewFolderInMediaRepOperation operation = new NewFolderInMediaRepOperation(treeViewer,rep);
		setOperation(operation);
	}

	/**
	 * Die eiegentliche Ausführungslogik der Action.
	 */
	/*@Override
	public void run() {

		// falls das selektierte Objekt ein MediaLeaf ist, prüfe ob das
		// MediaObject
		// Objekt mit dem selektierten übereinstimmt
		if (((IStructuredSelection) treeViewer.getSelection()).getFirstElement() instanceof MediaTreeGroup) {
			MediaTreeGroup leaf = (MediaTreeGroup) ((IStructuredSelection) treeViewer
					.getSelection()).getFirstElement();
			MediaTreeGroup folder = new MediaTreeGroup(leaf, "New Folder", Icons.VIEW_FOLDER);
			leaf.addElement(folder);
			
			rep.updateTreeviewer();
		
		}
	}*/
	

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IStructuredSelection incoming = (IStructuredSelection) selection;

		// Das eintreffende Objekt muss vom Typ MediaTreeGroup sein
		if (incoming.getFirstElement() instanceof MediaTreeGroup) {
		
			setEnabled(true);//TODO
		} else {
			setEnabled(false);
		}
	}

	@Override
	public void onDispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

	@Override
	protected void onSelectionChange(IStructuredSelection selection,
			IWorkbenchPart workbenchPart) {
		// TODO Auto-generated method stub
		
	}
	
}