package org.iviPro.actions.undoable;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.operations.global.ChangeTitleOperation;
import org.iviPro.theme.Icons;

/**
 * Action zum Aendern des Titel eines beliebigen Beans, also z.B. zum Umbenennen
 * einer Szene, Medienobjekt, Graph-Knoten, etc...
 * 
 * @author dellwo
 * 
 */
public class ChangeTitleAction extends AbstractUndoableAction {

	/** Action-ID */
	public final static String ID = ChangeTitleAction.class.getName();

	/**
	 * Erstellt eine neue Action zum Aendern eines Titels.
	 * 
	 * @param target
	 *            Das Bean dessen Titel geaendert werden soll.
	 * @param newTitle
	 *            Der neue Titel.
	 */
	public ChangeTitleAction(IAbstractBean target, LocalizedString newTitle) {
		this(target, newTitle, Messages.ChangeTitleAction_Label,
				Messages.ChangeTitleAction_Tooltip);
	}

	/**
	 * Erstellt eine neue Action zum Aendern eines Titels mit der Möglichkeit,
	 * das Label und den Tooltip der Action zu setzen.
	 * 
	 * @param target
	 *            Das Bean dessen Titel geaendert werden soll.
	 * @param newTitle
	 *            Der neue Titel.
	 * @param actionLabel
	 *            Das Label der Action, wie es z.B. im Menü auftaucht.
	 * @param actionTooltip
	 *            Der Tooltip der Action.
	 */
	public ChangeTitleAction(IAbstractBean target, LocalizedString newTitle,
			String actionLabel, String actionTooltip) {
		super(null);
		setId(ID);
		setText(actionLabel);
		setToolTipText(actionTooltip);
		setImageDescriptor(Icons.ACTION_RENAME.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_RENAME
				.getDisabledImageDescriptor());
		setOperation(new ChangeTitleOperation(target, newTitle));
	}

	@Override
	protected void onDispose() {
		// Nix zu tun, da keine Ressourcen oder Listener zum Freigeben vorhanden
	}

	@Override
	protected void onSelectionChange(IStructuredSelection selection,
			IWorkbenchPart workbenchPart) {
	}

}