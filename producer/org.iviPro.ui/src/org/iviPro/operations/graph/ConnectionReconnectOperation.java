package org.iviPro.operations.graph;

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
 * Ein Kommando, um eine Verbindung zu einem anderen Start- oder Endpunkt zu
 * verbinden. Dieses Kommando unterstuetzt Undo/Redo.
 */
public class ConnectionReconnectOperation extends IAbstractOperation {

	/** Die alte Verbindung vor dem Reconnect. */
	private final IConnection oldConnection;
	/** Die neue Verbindung nach dem Reconnect */
	private IConnection newConnection;
	/** The new source endpoint. */
	private IGraphNode newSource;
	/** The new target endpoint. */
	private IGraphNode newTarget;
	/** The original source endpoint. */
	private final IGraphNode oldSource;
	/** The original target endpoint. */
	private final IGraphNode oldTarget;

	/**
	 * Instantiate a command that can reconnect a Connection instance to a
	 * different source or target endpoint.
	 * 
	 * @param oldConnection
	 *            the connection instance to reconnect (non-null)
	 * @throws IllegalArgumentException
	 *             if conn is null
	 */
	public ConnectionReconnectOperation(IConnection oldConnection) {
		super(Messages.ConnectionReconnectOperation_UndoRedoLabel_Default);
		if (oldConnection == null) {
			throw new IllegalArgumentException();
		}
		this.oldConnection = oldConnection;
		this.oldSource = oldConnection.getSource();
		this.oldTarget = oldConnection.getTarget();
	}

	/**
	 * Return true, if reconnecting the connection-instance to newSource is
	 * allowed.
	 */
	private boolean checkSourceReconnection() {
		return newSource.canCompleteOutgoingConnection(oldTarget);
	}

	/**
	 * Return true, if reconnecting the connection-instance to newTarget is
	 * allowed.
	 */
	private boolean checkTargetReconnection() {
		return oldSource.canReconnectOutgoingConnection(oldTarget, newTarget);
	}

	/**
	 * Set a new source endpoint for this connection. When execute() is invoked,
	 * the source endpoint of the connection will be attached to the supplied
	 * IGraphNode instance.
	 * <p>
	 * Note: Calling this method, deactivates reconnection of the <i>target</i>
	 * endpoint. A single instance of this command can only reconnect either the
	 * source or the target endpoint.
	 * </p>
	 * 
	 * @param newSource
	 *            a non-null IGraphNode instance, to be used as a new source
	 *            endpoint
	 * @throws IllegalArgumentException
	 *             if connectionSource is null
	 */
	public void setNewSource(IGraphNode newSource) {
		if (newSource == null) {
			throw new IllegalArgumentException();
		}
		this.newSource = newSource;
		newTarget = null;
		updateLabel();
	}

	/**
	 * Set a new target endpoint for this connection When execute() is invoked,
	 * the target endpoint of the connection will be attached to the supplied
	 * IGraphNode instance.
	 * <p>
	 * Note: Calling this method, deactivates reconnection of the <i>source</i>
	 * endpoint. A single instance of this command can only reconnect either the
	 * source or the target endpoint.
	 * </p>
	 * 
	 * @param newTarget
	 *            a non-null IGraphNode instance, to be used as a new target
	 *            endpoint
	 * @throws IllegalArgumentException
	 *             if connectionTarget is null
	 */
	public void setNewTarget(IGraphNode newTarget) {
		if (newTarget == null) {
			throw new IllegalArgumentException();
		}
		newSource = null;
		this.newTarget = newTarget;
		updateLabel();
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ConnectionReconnectOperation_ErrorMsg + e.getMessage();
	}

	/**
	 * Aktualisiert das Label, je nach dem ob der Start- oder Endpunkt
	 * reconnectet werden soll.
	 */
	public void updateLabel() {
		if (newSource != null) {
			setLabel(Messages.ConnectionReconnectOperation_UndoRedoLabel_MoveStartpoint);
		} else {
			setLabel(Messages.ConnectionReconnectOperation_UndoRedoLabel_MoveEndpoint);
		}
	}

	@Override
	public boolean canExecute() {
		return true;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		Project project = Application.getCurrentProject();
		if (newSource != null) {
			newConnection = IConnection.createConnection(newSource, oldTarget,
					project);
		} else if (newTarget != null) {
			newConnection = IConnection.createConnection(oldSource, newTarget,
					project);
		}
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		Graph graph = oldSource.getGraph();
		graph.removeConnection(oldConnection);
		graph.addConnection(newConnection);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		Graph graph = oldSource.getGraph();
		graph.removeConnection(newConnection);
		graph.addConnection(oldConnection);
		return Status.OK_STATUS;
	}

}
