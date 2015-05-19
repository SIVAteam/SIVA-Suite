package org.iviPro.actions.undoable;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.OperationHistory;

/**
 * Basisklasse fuer Actions die den Undo/Redo-Mechanismus von Eclipse mittels
 * Operationen unterstuetzt. Wenn die Action ausfuehrbar sein soll, muss mittels
 * {@link #setOperation()} eine Operation gesetzt werden. Bei der Ausfuehrung
 * der Action wird dann automatisch diese Operation ausgefuehrt. Soll die Action
 * nicht ausfuehbar sein, setzt man die Operation einfach auf null.<br>
 * <br>
 * Außerdem bietet diese Klasse die Funktion, auf Aenderungen der Auswahl in der
 * Workbench zu listenen. Bei Änderungen wird dann die implementierende Klasse
 * ueber die {@link #onSelectionChange(IStructuredSelection, IWorkbenchPart)}
 * Methode informiert.
 * 
 * @author dellwo
 * 
 */
public abstract class AbstractUndoableAction extends Action implements
		IWorkbenchAction {

	private static final Logger logger = Logger
			.getLogger(AbstractUndoableAction.class);

	/** Die Operation der Action -> {@link #setOperation(IAbstractOperation)} */
	private IAbstractOperation operation;

	/** Der Listener der auf Auswahl-Aenderungen in der Workbench horcht. */
	private final ISelectionListener selectionListener;

	/** Die Workbench-Window auf dem auf Auswahl-Aenderungen gehorcht wird. */
	private final IWorkbenchWindow selectionListenerTarget;

	/**
	 * Erstellt eine neue rueckgaengig-machbare Action.
	 * 
	 * @param listenOnSelections
	 *            Das Workbench-Window auf dem auf Auswahl-Aenderungen gehorcht
	 *            werden soll. Wenn auf null gesetzt, dann wird nicht auf
	 *            Auswahl-Aenderungen gehorcht.<br>
	 * <br>
	 *            Wenn gesetzt, dann horcht die Action selbststaendig auf
	 *            Aenderungen der Auswahl in der Workbench und informiert die
	 *            implementierende Klasse ueber die Methode
	 *            {@link #onSelectionChange(IStructuredSelection, IWorkbenchPart)}
	 *            wenn sich die Auswahl geaendert hat. Dies ist insbesondere
	 *            fuer all die Actions nuetzlich, die von der aktuellen Auswahl
	 *            abhaengen.
	 */
	public AbstractUndoableAction(IWorkbenchWindow selectionListenerTarget) {
		this.selectionListenerTarget = selectionListenerTarget;
		if (selectionListenerTarget != null) {
			this.selectionListener = new ISelectionListener() {

				@Override
				public void selectionChanged(IWorkbenchPart workbenchPart,
						ISelection selection) {
					onSelectionChange((IStructuredSelection) selection,
							workbenchPart);
				}

			};
			selectionListenerTarget.getSelectionService().addSelectionListener(
					selectionListener);
		} else {
			this.selectionListener = null;
		}
	}

	/**
	 * Wird aufgerufen, wenn sich die Selektion in der Workbench aendert. Dies
	 * erfolgt jedoch nur, wenn die Action mit dem Konstruktor-Wert
	 * "listenOnSelections = true" erstellt wurde.
	 * 
	 * @see AbstractUndoableAction#AbstractUndoableAction(boolean)
	 * 
	 * @param selection
	 *            Die aktuelle Selektion.
	 * @param workbenchPart
	 *            Der Workbench-Part aus dem die Selektion stammt.
	 */
	protected abstract void onSelectionChange(IStructuredSelection selection,
			IWorkbenchPart workbenchPart);

	/**
	 * Fuehrt diese Action aus, d.h. es wird die Operation dieser Action
	 * ausgefuehrt.
	 * 
	 * @see #setOperation(IAbstractOperation)
	 * @see #getOperation()
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public final void run() {
		run(null, null);
	}

	/**
	 * Fuehrt diese Action aus, d.h. es wird die Operation dieser Action
	 * ausgefuehrt.
	 * 
	 * @param monitor
	 *            The progress monitor to be used (or null) during the
	 *            operation.
	 * 
	 * @param info
	 *            The IAdaptable (or null) provided by the caller in order to
	 *            supply UI information for prompting the user if necessary.
	 *            When this parameter is not null, it should minimally contain
	 *            an adapter for the org.eclipse.swt.widgets.Shell.class.
	 * 
	 * @see #setOperation(IAbstractOperation)
	 * @see #getOperation()
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public final void run(IProgressMonitor monitor, IAdaptable info) {
		// Wenn das Kommando ausgefuehrt werden soll, fuehren wir einfach die
		// Operation ueber den Operation-Manager aus.
		try {
			OperationHistory.execute(operation, monitor, info);
		} catch (ExecutionException e) {
			// Im Fehlerfall Benutzer eine Meldung anzeigen.
			IWorkbench workbench = PlatformUI.getWorkbench();
			Shell shell = workbench.getDisplay().getActiveShell();
			MessageDialog.openError(shell,
					Messages.AbstractUndoableAction_ErrorMsgBox_Title,
					operation.getErrorMessage(e));
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public final void runWithEvent(Event event) {
		super.runWithEvent(event);
	}

	/**
	 * Stellt fest ob die Action ausfuehrbar ist. Dies ist der Fall wenn eine
	 * Operation gesetzt wurde und diese ausfuehrbar ist.
	 * 
	 * @return True, wenn Action ausfuehrbar, sonst false.
	 * 
	 * @see #setOperation(IAbstractOperation)
	 * @see IAbstractOperation#canExecute()
	 * 
	 */
	@Override
	public boolean isEnabled() {
		if (operation == null) {
			return false;
		} else {
			return operation.canExecute();
		}
	}

	/**
	 * Gibt die Operation fuer diese Action zurueck oder null, wenn keine
	 * Operation gesetzt wurde und die Action generell derzeit nicht ausfuehrbar
	 * ist.
	 * 
	 * @return Operation der Action oder null.
	 */
	protected IAbstractOperation getOperation() {
		return operation;
	}

	/**
	 * Setzt die Operation fuer diese Action. Die Action ist ausfuehrbar, wenn
	 * mittels dieser Methode eine Operation gesetzt wurde und auch die
	 * Operation ausfuehrbar ist (ermittelt über
	 * {@link IAbstractOperation#canExecute()}). <br>
	 * <br>
	 * Die Operation kann auch auf null gesetzt werden. In diesem Fall ist die
	 * Action dann derzeit generell nicht ausfuehrbar.
	 * 
	 * @param operation
	 *            Die Operation oder null, falls Action derzeit generell nicht
	 *            ausfuehrbar ist.
	 */
	protected void setOperation(IAbstractOperation operation) {
		this.operation = operation;
		super.setEnabled(isEnabled());
	}

	/**
	 * Diese Methode sollte nicht benutzt werden, da die Ausfuehrbarkeit der
	 * Action dadurch bestimmt wird, dass eine Operation gesetzt wurde und diese
	 * ausfuehrbar ist.Diese Methode macht deshalb nichts und ein Aufruf hat
	 * keine Wirkung.
	 * 
	 * @see #setOperation(IAbstractOperation)
	 * @see #getOperation()
	 */
	@Deprecated
	@Override
	public void setEnabled(boolean enabled) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionFactory.IWorkbenchAction#dispose()
	 */
	@Override
	public final void dispose() {
		if (selectionListener != null && selectionListenerTarget != null) {
			selectionListenerTarget.getSelectionService()
					.removeSelectionListener(selectionListener);
		}
		onDispose();
	}

	/**
	 * Wird aufgerufen, wenn die Action disposed wird. In diesem Fall sollte sie
	 * ihre Ressourcen freigeben und ihre Listener beenden.
	 */
	protected abstract void onDispose();

}
