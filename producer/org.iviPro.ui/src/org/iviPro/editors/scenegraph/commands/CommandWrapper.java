package org.iviPro.editors.scenegraph.commands;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.OperationHistory;

/**
 * Wrapper, der eine Operation in ein GEF-Kommando verwandelt. Dadurch lassen
 * sich normale Eclipse-Operationen, die z.B. auch den Standard-Undo-Redo
 * Mechanismus von Eclipse unterstuetzen, in GEF verwenden.
 * 
 * @author dellwo
 * 
 */
public class CommandWrapper extends Command {

	private static final Logger logger = Logger.getLogger(CommandWrapper.class);

	/** Die Operation die gewrappt wird. */
	private final IAbstractOperation operation;

	/**
	 * Erstellt einen neuen Wrapper fuer eine Operation.
	 * 
	 * @param operation
	 *            Die Operation.
	 */
	public CommandWrapper(IAbstractOperation operation) {
		this.operation = operation;
	}

	@Override
	public final boolean canUndo() {
		// Liefert false, da hier ja der Eclipse-Undo-Redo Mechanismus ueber die
		// OperationHistory genutzt wird, und nicht der GEF-Mechanismus.
		return false;
	}

	@Override
	public final void execute() {
		// Wenn das Kommando ausgefuehrt werden soll, fuehren wir einfach die
		// Operation ueber den Operation-Manager aus.
		try {
			OperationHistory.execute(operation);
		} catch (ExecutionException e) {
			// Im Fehlerfall Benutzer eine Meldung anzeigen.
			IWorkbench workbench = PlatformUI.getWorkbench();
			Shell shell = workbench.getDisplay().getActiveShell();
			MessageDialog.openError(shell, "Error", operation //$NON-NLS-1$
					.getErrorMessage(e));
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public final boolean canExecute() {
		// Das GEF Kommando ist ausfuehrbar, wenn die Operation ausfuehrbar ist.
		return operation.canExecute();
	}

	public IAbstractOperation getOperation() {
		return operation;
	}

}
