package org.iviPro.operations.graph;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IConnection;
import org.iviPro.operations.IAbstractOperation;

/**
 * Eine Operation zum Loeschen einer Verbindung zwischen zwei Knoten.
 * 
 * @author dellwo
 * 
 */
public class ConnectionDeleteOperation extends IAbstractOperation {

	/** Zu loeschende Verbindung */
	private final IConnection connection;

	/** Zugehoeriger Graph */
	private final Graph graph;

	/**
	 * Erstellt eine Operation die eine Verbindung loescht.
	 * 
	 * @param connection
	 *            Die zu loeschende Verbindung
	 * @param graph
	 *            Der Graph zu dem die Verbindug gehoert.
	 * @throws IllegalArgumentException
	 *             Falls einer der Parameter null ist.
	 */
	public ConnectionDeleteOperation(IConnection connection, Graph graph) {
		super(Messages.ConnectionDeleteOperation_UndoRedoLabel);
		if (connection == null || graph == null) {
			throw new IllegalArgumentException();
		}
		this.graph = graph;
		this.connection = connection;
	}

	@Override
	public boolean canExecute() {
		return connection != null && graph != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ConnectionDeleteOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		graph.removeConnection(connection);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		graph.addConnection(connection);
		return Status.OK_STATUS;
	}
}
