package org.iviPro.actions.undoable;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.theme.Icons;
import org.iviPro.views.IAbstractRepositoryView;
import org.iviPro.views.mediarepository.MediaTreeGroup;

public class FolderRenameAction extends Action implements ISelectionListener,
		IWorkbenchAction, ICellModifier {

	public final static String ID = MediaRenameAction.class.getName();

	private final IWorkbenchWindow window;
	private TreeViewer treeViewer;
	private IAbstractRepositoryView rep; //for updating the treeview
	
	private boolean editActive = false;
	

	/**
	 * Konstruktor...
	 * @param window
	 */
	public FolderRenameAction(IWorkbenchWindow window, TreeViewer treeViewer, IAbstractRepositoryView rep) {
		this.window = window;
		setEnabled(false);
		setId(ID);
		this.treeViewer = treeViewer;
		treeViewer.setCellModifier(this);
		setText(Messages.FolderRenameAction_Title);
		setToolTipText(Messages.FolderRenameAction_Title);
		setImageDescriptor(Icons.ACTION_RENAME.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_RENAME
				.getDisabledImageDescriptor());
		window.getSelectionService().addSelectionListener(this);
		this.rep = rep;
	}

	/**
	 * Die eiegentliche Ausführungslogik der Action.
	 */
	@Override
	public void run() {
		treeViewer.setCellModifier(this);
		// falls das selektierte Objekt ein MediaLeaf ist, prüfe ob das
		// MediaObject
		// Objekt mit dem selektierten übereinstimmt
		if (((IStructuredSelection) treeViewer.getSelection())
				.getFirstElement() instanceof MediaTreeGroup) {
			editActive = true;
			MediaTreeGroup leaf = (MediaTreeGroup) ((IStructuredSelection) treeViewer
					.getSelection()).getFirstElement();

			treeViewer.editElement(((IStructuredSelection) treeViewer
					.getSelection()).getFirstElement(), 0);

		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IStructuredSelection incoming = (IStructuredSelection) selection;

		// Das eintreffende Objekt muss vom Typ MediaLeaf sein
		if (incoming.getFirstElement() instanceof MediaTreeGroup) {
		
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}

	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

	@Override
	public boolean canModify(Object element, String property) {
		if (editActive && element != null && element instanceof MediaTreeGroup) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Object getValue(Object element, String property) {
		if (element instanceof MediaTreeGroup) {
			MediaTreeGroup leaf = (MediaTreeGroup) element;
			if (property.equals("name")) { //$NON-NLS-1$
				return leaf.getName();
			}
		}
		return null;
	}

	@Override
	public void modify(final Object element, final String property, Object value) {
		String name = "";
		if (value instanceof String) {
			name = (String) value;
		}
		MediaTreeGroup group = (MediaTreeGroup)((TreeItem) element).getData();
		group.setName(name);
		String[] prop = {property};
		treeViewer.update((TreeItem) element, prop);
		rep.updateTreeviewer();
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				String[] prop = {property};
				treeViewer.update(element, prop);
			}
		});

		editActive = false;
	}
}