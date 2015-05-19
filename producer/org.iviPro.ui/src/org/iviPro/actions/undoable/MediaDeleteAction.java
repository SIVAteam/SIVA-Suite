package org.iviPro.actions.undoable;

import java.util.LinkedList;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.model.IAbstractBean;
import org.iviPro.operations.CompoundOperation;
import org.iviPro.operations.IAbstractDeleteOperation;
import org.iviPro.operations.media.MediaDeleteOperation;
import org.iviPro.theme.Icons;
import org.iviPro.views.mediarepository.MediaTreeLeaf;

public class MediaDeleteAction extends AbstractUndoableAction implements
		IWorkbenchAction {

	public final static String ID = MediaDeleteAction.class.getName();

	/**
	 * Konstruktor...
	 */
	public MediaDeleteAction(IWorkbenchWindow window) {
		super(window);
		setId(ID);
		setText(Messages.MediaDeleteAction_Label);
		setToolTipText(Messages.MediaDeleteAction_Tooltip);
		setImageDescriptor(Icons.ACTION_MEDIA_DELETE.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_MEDIA_DELETE
				.getDisabledImageDescriptor());
		setAccelerator(SWT.DEL);
	}
	
	/**
	 * Konstruktor zum loeschen eines einzelnen MediaObject.
	 * 
	 * @param sceneToDelete
	 *            Die zu loeschende Szene.
	 */
	public MediaDeleteAction(IAbstractBean beanToDelete) {
		this((IWorkbenchWindow) null);
		setOperation(new MediaDeleteOperation(beanToDelete));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.iviPro.actions.undoable.AbstractUndoableAction#onDispose()
	 */
	@Override
	protected void onDispose() {
		// Nix zu tun, da keine Ressourcen oder Listener zum Freigeben vorhanden
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.iviPro.actions.undoable.AbstractUndoableAction#onSelectionChange(
	 * org.eclipse.jface.viewers.IStructuredSelection,
	 * org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	protected void onSelectionChange(IStructuredSelection selection,
			IWorkbenchPart workbenchPart) {

		// Alle Medien-Objekte aus der Selektion holen
		LinkedList<IAbstractBean> selectedMediaObjs = new LinkedList<IAbstractBean>();
		for (Object selectedObj : selection.toArray()) {
			if (selectedObj instanceof MediaTreeLeaf) {
				IAbstractBean mediaToDelete = ((MediaTreeLeaf) selectedObj)
						.getMediaObject();
				selectedMediaObjs.add(mediaToDelete);
			}
		}

		if (selectedMediaObjs.size() > 0) {
			// Nur wenn Medien-Objekte selektiert wurden, dann ist auch die
			// Medien-Loesch-Aktion ausfuehrbar. In diesem Fall wird eine
			// Operation
			// gesetzt, die die selektierten Objekte loeschen wuerde.
			CompoundOperation<IAbstractDeleteOperation> deleteAll = new CompoundOperation<IAbstractDeleteOperation>(
					Messages.MediaDeleteAction_0);
			for (IAbstractBean mediaObj : selectedMediaObjs) {
				deleteAll.addOperation(new MediaDeleteOperation(mediaObj));
			}			
			setOperation(deleteAll);
		} else {
			// Keine Medien-Objekte selektiert
			// => Aktion nicht ausfuehrbar, Operation wird auf null gesetzt.
			setOperation(null);
		}
	}
}
