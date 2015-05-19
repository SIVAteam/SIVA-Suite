package org.iviPro.operations;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.iviPro.actions.nondestructive.ProjectSaveAction;
import org.iviPro.application.Application;
import org.iviPro.model.Project;

/**
 * Die Operation-History ist als Zugriffsklasse auf die Eclipse
 * Undo/Redo-History vorgesehen und soll den Umgang mit dieser vereinfachen.
 * Ausserdem markiert er das Fenster automatisch mit einem Sternchen, falls
 * ungespeicherte Aenderungen vorhanden sind.
 * 
 * @author dellwo
 * 
 */
public class OperationHistory extends Observable {

	/** Logger */
	private static Logger logger = Logger.getLogger(OperationHistory.class);

	/** Private Instanz-Klasse fuer Singleton-Pattern. */
	private static OperationHistory instance = null;
	/** Kontext in dem alle Operationen global abgelegt werden. */
	private final IUndoContext globalContext;
	/** Die History die benutzt wird */
	private final IOperationHistory history;
	/** Der letzte Save-Point oder null, falls noch nicht gespeichert wurde. */
	private IUndoableOperation savePoint;
	/** Die letzte Operation die ausgefuehrt/redone/undone wurde wurde. */
	private IUndoableOperation lastOperation;

	/**
	 * Privater Konstruktor. Nur die oeffentlichen statischen Methoden sind fuer
	 * den Ausruf von aussen bestimmt.
	 */
	private OperationHistory() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		history = workbench.getOperationSupport().getOperationHistory();
		history.addOperationHistoryListener(new IOperationHistoryListener() {

			@Override
			public void historyNotification(OperationHistoryEvent event) {
				onHistoryEvent(event);
			}
		});
		globalContext = IOperationHistory.GLOBAL_UNDO_CONTEXT;
		logger.info("OperationHistory created."); //$NON-NLS-1$
	}

	/**
	 * Gibt die Instanz der Operation-History zurueck. Nur intern zugaenglich:
	 * Fuer den Aufruf von aussen sind nur die oeffentlichen statischen Methoden
	 * vorgesehen.
	 * 
	 * @return Instanz der Operation-History.
	 */
	private static synchronized OperationHistory getInstance() {
		if (instance == null) {
			instance = new OperationHistory();
		}
		return instance;
	}

	/**
	 * Wird aufgerufen wenn ein History-Event auftritt.
	 * 
	 * @param event
	 *            Der History-Event.
	 */
	private void onHistoryEvent(OperationHistoryEvent event) {
		switch (event.getEventType()) {
		case OperationHistoryEvent.OPERATION_ADDED:
			setLastOperation(event.getOperation());
			break;
		case OperationHistoryEvent.REDONE:
			setLastOperation(event.getOperation());
			break;
		case OperationHistoryEvent.UNDONE:
			setLastOperation(history.getUndoOperation(globalContext));
			break;
		}
		logger.trace("Detected history change: current = " //$NON-NLS-1$
				+ getLastOperation());
		updateWindowTitle();
		notifyObservers();
	}

	/**
	 * Markiert den Fenster-Titel mit einem *-Sternchen, falls ungespeicherte
	 * Aenderungen vorhanden sind.
	 */
	private void updateWindowTitle() {
		if (PlatformUI.getWorkbench() == null) {
			return;
		}
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
			return;
		}
		// Den Fenster-Titel mit einem *-Sternchen markieren, falls
		// ungespeicherte Aenderungen gemacht wurden.		
		Shell window = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell();
		
		String windowTitle = window.getText();
		if (hasUnsavedChanges()) {
			if (!windowTitle.endsWith("*")) { //$NON-NLS-1$
				window.setText(windowTitle + "*"); //$NON-NLS-1$
			}
		} else {
			if (windowTitle.endsWith("*")) { //$NON-NLS-1$
				window.setText(windowTitle.substring(0,
						windowTitle.length() - 1));
			}
		}
	}

	/**
	 * Fuehrt die angegebene Operation aus.
	 * 
	 * @param operation
	 *            Die Operation die ausgefuehrt werden soll.
	 * @param monitor
	 *            Der Progress-Monitor der benutzt werden soll während der
	 *            Operation oder null, falls kein Progress-Monitor verwendet
	 *            werden soll.
	 * @param info
	 *            The IAdaptable (or null) provided by the caller in order to
	 *            supply UI information for prompting the user if necessary.
	 *            When this parameter is not null, it should minimally contain
	 *            an adapter for the org.eclipse.swt.widgets.Shell.class.
	 * @return Der IStatus der Operation, der angibt, ob die Operation
	 *         erfolgreich ausgefuehrt wurde oder nicht.
	 * @throws ExecutionException
	 *             Falls beim Ausfuehren der Operation ein Fehler auftrat.
	 */
	public static IStatus execute(IUndoableOperation operation,
			IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		logger.debug("Executing operation: " + operation); //$NON-NLS-1$
		OperationHistory instance = getInstance();
		IOperationHistory history = instance.history;
		IUndoContext globalContext = instance.globalContext;
		// Operation im globalen Kontext ausfueheren
		if (!operation.hasContext(globalContext)) {
			operation.addContext(globalContext);
		}
		IStatus status = history.execute(operation, monitor, info);
		return status;
	}

	/**
	 * Fuehrt die angegebene Operation aus und gibt dabei eine moeglicherweise
	 * auftretende ExecutionException nicht weiter an den Aufrufer. Stattdessen
	 * wird dem Benutzer in diesem Fall eine Fehlermeldung angezeigt.
	 * 
	 * @param operation
	 *            Die Operation die ausgefuehrt werden soll.
	 * @param errorMsg
	 *            Die Fehlermeldung, falls bei der Ausfuehrung ein Fehler
	 *            auftrat.
	 * @param shell
	 *            Die Shell die zur Anzeige der Fehlermeldung verwendet werden
	 *            soll.
	 * @return Der IStatus der Operation, der angibt, ob die Operation
	 *         erfolgreich ausgefuehrt wurde oder nicht.
	 */
	public static IStatus execute(IUndoableOperation operation,
			String errorMsg, Shell shell) {
		IStatus status = Status.CANCEL_STATUS;
		try {
			status = execute(operation, (IProgressMonitor) null, null);
		} catch (ExecutionException e) {
			MessageDialog.openError(shell, Messages.OperationHistory_ErrorMsgBox_Title, errorMsg);
		}
		return status;
	}

	/**
	 * Fuehrt die angegebene Operation aus.
	 * 
	 * @param operation
	 *            Die Operation die ausgefuehrt werden soll.
	 * @return Der IStatus der Operation, der angibt, ob die Operation
	 *         erfolgreich ausgefuehrt wurde oder nicht.
	 * @throws ExecutionException
	 *             Falls beim Ausfuehren der Operation ein Fehler auftrat.
	 */
	public static IStatus execute(IUndoableOperation operation)
			throws ExecutionException {
		return execute(operation, (IProgressMonitor) null, null);
	}

	/**
	 * Gibt an, ob es nicht gespeicherte Aenderungen seit dem letzten Save-Point
	 * gibt. Dies ist der Fall, wenn Operationen seither durchgefuehrt wurden.
	 * 
	 * @return True, falls Aenderungen vorhanden sind. False sonst.
	 */
	public static boolean hasUnsavedChanges() {
		OperationHistory instance = getInstance();
		IUndoableOperation lastOperation = instance.getLastOperation();
		IUndoableOperation savePoint = instance.getSavePoint();
		boolean unsaved = lastOperation != savePoint;
		return unsaved;
	}

	/**
	 * Gibt den Undo-Context der Operation-History zurueck.
	 * 
	 * @return Undo-Context der Operation-History.
	 */
	public static IUndoContext getContext() {
		return getInstance().globalContext;
	}

	/**
	 * Loescht die Undo/Redo-History.
	 */
	public static void clearHistory() {
		logger.info("Clearing history..."); //$NON-NLS-1$
		OperationHistory instance = getInstance();
		IOperationHistory history = instance.history;
		history.dispose(instance.globalContext, true, true, true);
		instance.setSavePoint(null);
		instance.setLastOperation(null);
		instance.notifyObservers();
	}

	/**
	 * Setzt den Save-Point, d.h. dass der jetztige Zustand als gespeichert
	 * markiert.
	 */
	public static void setSavePoint() {
		logger.info("Setting savepoint..."); //$NON-NLS-1$
		OperationHistory instance = getInstance();
		instance.setSavePoint(instance.getLastOperation());
		instance.updateWindowTitle();
		instance.notifyObservers();
	}

	/**
	 * Fuegt einen Operation-Observer hinzu. Diese werden informiert, sobald
	 * sich der Zustand der Operation-History geaendert hat, z.B. wenn eine neue
	 * Operation hinzu gekommen ist.
	 * 
	 * @param o
	 *            Der Observer.
	 */
	public static void addOperationObserver(Observer o) {
		logger.debug("Adding observer: " + o); //$NON-NLS-1$
		getInstance().addObserver(o);
	}

	/**
	 * Entfernt einen Operation-Observer wieder.
	 * 
	 * @param o
	 *            Der zu entfernende Observer.
	 */
	public static void removeOperationObserver(Observer o) {
		logger.debug("Removing observer: " + o); //$NON-NLS-1$
		getInstance().deleteObserver(o);
	}

	/**
	 * Setzt den Save-Point.
	 * 
	 * @param savePoint
	 *            Der Save-Point.
	 */
	private void setSavePoint(IUndoableOperation savePoint) {
		this.savePoint = savePoint;
		instance.setChanged();
	}

	/**
	 * Setzt die zuletzt durchgefuehrte Operation.
	 * 
	 * @param lastOperation
	 *            Letzte Operation.
	 */
	private void setLastOperation(IUndoableOperation lastOperation) {
		this.lastOperation = lastOperation;
		Project project = Application.getCurrentProject();
		if(project != null){
			try {
				ProjectSaveAction.doSaveBackup(project, project.getBackupFile());
			} catch (Throwable t) {
				Logger logger = Logger.getLogger(OperationHistory.class);
				logger.error(t.getMessage(), t);
			}
		}
		instance.setChanged();
	}

	/**
	 * Gibt den Save-Point zurueck.
	 * 
	 * @return Der Save-Point.
	 */
	private IUndoableOperation getSavePoint() {
		return savePoint;
	}

	/**
	 * Gibt die zuletzt durchgefuehrte Operation zurueck.
	 * 
	 * @return Die zuletzt durchgefuehrte Operation.
	 */
	private IUndoableOperation getLastOperation() {
		return lastOperation;
	}

}
