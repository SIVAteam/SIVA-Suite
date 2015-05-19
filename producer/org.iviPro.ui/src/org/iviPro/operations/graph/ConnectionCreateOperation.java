package org.iviPro.operations.graph;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.model.Project;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IConnection;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Erstellen von einer Verbindung zwischen zwei Graph-Knoten.
 * 
 * @author dellwo
 * 
 */
public class ConnectionCreateOperation extends IAbstractOperation {

	private static Logger logger = Logger
			.getLogger(ConnectionCreateOperation.class);

	/** Die zu erstellende Verbindung. */
	private IConnection connection;

	// /** The desired line style for the connection (dashed or solid). */
	// private final int lineStyle;

	/** Start-Knoten der Verbindung. */
	private final IGraphNode source;

	/** End-Knoten der Verbindung. */
	private IGraphNode target;

	/** Zugehoeriger Graph */
	private final Graph graph;

	/**
	 * Erstellt eine Operation zum Erstellen einer Verbindung von einem Knoten
	 * weg. Der Ziel-Knoten wird erst spaeter mit setTarget von einer anderen
	 * Operation aus befuellt.
	 * 
	 * @param source
	 *            Der Start-Knoten der Verbindung.
	 * 
	 * @throws IllegalArgumentException
	 *             Falls der Start-Knoten null ist.
	 */
	public ConnectionCreateOperation(IGraphNode source) {
		super(Messages.ConnectionCreateOperation_UndoRedoLabel);
		if (source == null) {
			throw new IllegalArgumentException();
		}
		this.source = source;
		this.graph = source.getGraph();
		logger.trace("Created ConnectionCreateCommand for: " + source); //$NON-NLS-1$
		// Der Line-Style fuer die Linie. Siehe
		// Connection#setLineStyle(int) fuer mehr Details.
		// this.lineStyle = lineStyle;
	}

	/**
	 * Set the target endpoint for the connection.
	 * 
	 * @param target
	 *            The endpoint of the connection.
	 * @throws IllegalArgumentException
	 *             If target is null.
	 */
	public void setTarget(IGraphNode target) {
		if (target == null) {
			throw new IllegalArgumentException();
		}
		this.target = target;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ConnectionCreateOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public boolean canExecute() {
		if (source == null || target == null) {
			return false;
		}
		boolean canExecute = true;
		// disallow source -> source connections
		if (source.equals(target)) {
			canExecute = false;
		}
		// return false, if the source -> target connection exists already
		if (source.getGraph().containsConnection(source, target)) {
			canExecute = false;
		}
		// Everything seems ok
		logger.trace("canExecute: " + canExecute); //$NON-NLS-1$
		return canExecute;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		Project project = Application.getCurrentProject();
		connection = IConnection.createConnection(source, target, project);
		// connection.setLineStyle(lineStyle);
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		graph.addConnection(connection);
		logger.debug("Created connection: " + connection); //$NON-NLS-1$		
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		graph.removeConnection(connection);
		logger.debug("Removed connection: " + connection); //$NON-NLS-1$
		return Status.OK_STATUS;
	}
}
