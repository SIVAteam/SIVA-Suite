package org.iviPro.editors;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.EditorPart;
import org.iviPro.operations.OperationHistory;

/**
 * Abstrakte Basisklasse fuer alle Editoren. Diese registriert z.B. automatisch
 * die Undo/Redo Action-Handler im globalen Undo-Context und erledigt andere
 * Standard-Aufgaben.
 * 
 * @author dellwo
 * 
 */
public abstract class IAbstractEditor extends EditorPart {

	/**
	 * Action-Handler registrieren z.B. fuer Undo/Redo.
	 */
	protected void createGlobalActionHandlers() {
		IUndoContext undoContext = OperationHistory.getContext();
		IEditorSite site = getEditorSite();
		// set up action handlers that operate on the current context
		UndoActionHandler undoAction = new UndoActionHandler(site, undoContext);
		RedoActionHandler redoAction = new RedoActionHandler(site, undoContext);
		IActionBars actionBars = site.getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
				undoAction);
		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
				redoAction);
	}

	@Override
	public final void createPartControl(Composite parent) {
		createGlobalActionHandlers();
		createPartControlImpl(parent);
	}

	/**
	 * Erstellt die SWT-Controls fuer diesen Editor.
	 * 
	 * @param parent
	 *            Das Parent-Composite.
	 */
	protected abstract void createPartControlImpl(Composite parent);
}
