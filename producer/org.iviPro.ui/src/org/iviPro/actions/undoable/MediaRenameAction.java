package org.iviPro.actions.undoable;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.application.Application;
import org.iviPro.model.BeanList;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.theme.Icons;
import org.iviPro.views.mediarepository.MediaTreeLeaf;

public class MediaRenameAction extends Action implements ISelectionListener,
		IWorkbenchAction, ICellModifier {

	public final static String ID = MediaRenameAction.class.getName();

	private final IWorkbenchWindow window;
	private IAbstractBean mediaToRename;
	private TreeViewer treeViewer;

	private boolean editActive = false;

	/**
	 * Konstruktor...
	 * @param window
	 */
	public MediaRenameAction(IWorkbenchWindow window, TreeViewer treeViewer) {
		this.window = window;
		setEnabled(false);
		setId(ID);
		this.treeViewer = treeViewer;
		treeViewer.setCellModifier(this);
		setText(Messages.ChangeTitleAction_Label);
		setToolTipText(Messages.ChangeTitleAction_Tooltip);
		setImageDescriptor(Icons.ACTION_RENAME.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_RENAME
				.getDisabledImageDescriptor());
		window.getSelectionService().addSelectionListener(this);
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
				.getFirstElement() instanceof MediaTreeLeaf) {
			editActive = true;
			MediaTreeLeaf leaf = (MediaTreeLeaf) ((IStructuredSelection) treeViewer
					.getSelection()).getFirstElement();
			IAbstractBean leafObject = leaf.getMediaObject();
			// wir ändern das richtige Element
			if (leafObject.getTitle().equals(mediaToRename.getTitle())) {
				treeViewer.editElement(((IStructuredSelection) treeViewer
						.getSelection()).getFirstElement(), 0);
			}
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IStructuredSelection incoming = (IStructuredSelection) selection;

		// Das eintreffende Objekt muss vom Typ MediaLeaf sein
		if (incoming.getFirstElement() instanceof MediaTreeLeaf) {
			mediaToRename = ((MediaTreeLeaf) incoming.getFirstElement())
					.getMediaObject();
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
		if (editActive && element != null && element instanceof MediaTreeLeaf) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Object getValue(Object element, String property) {
		if (element instanceof MediaTreeLeaf) {
			MediaTreeLeaf leaf = (MediaTreeLeaf) element;
			if (property.equals("name")) { //$NON-NLS-1$
				return leaf.getName();
			}
		}
		return null;
	}

	@Override
	public void modify(final Object element, String property, Object value) {
		LocalizedString newName = null;
		if (value instanceof String) {
			if (((String) value).length() > 0) {
				newName = new LocalizedString((String) value, Application
						.getCurrentLanguage());
			}
		}
		// prüfe ob der Name bereits vergeben ist
		boolean nameInUse = false;
		BeanList<IAbstractBean> bl = Application.getCurrentProject().getMediaObjects();
		for (IAbstractBean mobj : bl) {			
			if (mobj.getClass().equals(mediaToRename.getClass())) {
				if (!mobj.getTitle().equals(mediaToRename.getTitle())) {
					if (mobj.getTitle().equals(newName.getValue())) {
						nameInUse = true;
						break;
					}
				}
			}
		}
		if (newName != null && !nameInUse) {
			new ChangeTitleAction(mediaToRename, newName).run();
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					treeViewer.update(element, null);
				}
			});						
			editActive = false;
		} else {
			MessageBox messageBox = new MessageBox(Display.getCurrent()
					.getActiveShell(), SWT.ICON_ERROR);
			messageBox.setText(Messages.MediaRenameAction_MsgBox_Title);
			messageBox.setMessage(Messages.MediaRenameAction_MsgBox_Text);
			messageBox.open();
		}
	}
}