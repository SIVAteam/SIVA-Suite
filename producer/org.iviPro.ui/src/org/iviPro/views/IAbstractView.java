package org.iviPro.views;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.ViewPart;
import org.iviPro.operations.OperationHistory;

public abstract class IAbstractView extends ViewPart {

	protected void createGlobalActionHandlers() {
		IUndoContext undoContext = OperationHistory.getContext();
		IViewSite site = getViewSite();
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

	public abstract void createPartControlImpl(Composite parent);
}
