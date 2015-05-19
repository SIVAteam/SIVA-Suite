package org.iviPro.editors.scenegraph.policies;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.iviPro.editors.scenegraph.commands.CommandWrapper;
import org.iviPro.model.graph.IConnection;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.operations.graph.ConnectionCreateOperation;
import org.iviPro.operations.graph.ConnectionReconnectOperation;

public class DefaultConnectionPolicy extends GraphicalNodeEditPolicy {

	/**
	 * Feedback should be added to the scaled feedback layer.
	 * 
	 * @see org.eclipse.gef.editpolicies.GraphicalEditPolicy#getFeedbackLayer()
	 */
	protected IFigure getFeedbackLayer() {
		/*
		 * Fix for Bug# 66590 Feedback needs to be added to the scaled feedback
		 * layer
		 */
		return getLayer(LayerConstants.SCALED_FEEDBACK_LAYER);
	}

	@Override
	protected Command getConnectionCompleteCommand(
			CreateConnectionRequest request) {
		IGraphNode source = (IGraphNode) request.getSourceEditPart().getModel();
		IGraphNode target = (IGraphNode) getHost().getModel();
		// Nur wenn das Source-Objekt eine erlaubtes Quelle der
		// eingehenden Verbindung ist, soll die Verbindung zugelassen werden.
		if (source.canCompleteOutgoingConnection(target)
				&& target.canCompleteIncomingConnection(source)) {
			CommandWrapper cmd = (CommandWrapper) request.getStartCommand();
			ConnectionCreateOperation createOperation = (ConnectionCreateOperation) cmd
					.getOperation();
			createOperation.setTarget((IGraphNode) getHost().getModel());
			return cmd;
		} else {
			// Verbindung nicht erlaubt.
			return null;
		}
	}

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		IGraphNode source = (IGraphNode) getHost().getModel();
		if (source.canCreateOutgoingConnection(true)) {
			// int style = ((Integer) request.getNewObjectType()).intValue();
			ConnectionCreateOperation operation = new ConnectionCreateOperation(
					source);
			CommandWrapper cmd = new CommandWrapper(operation);
			request.setStartCommand(cmd);
			return cmd;
		} else {
			return null;
		}
	}

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		IGraphNode newSource = (IGraphNode) getHost().getModel();
		IConnection oldConn = (IConnection) request.getConnectionEditPart()
				.getModel();
		
		if (!newSource.canCompleteOutgoingConnection(oldConn.getTarget())) {
			return null;
		}
		ConnectionReconnectOperation operation = new ConnectionReconnectOperation(
				oldConn);
		operation.setNewSource(newSource);
		return new CommandWrapper(operation);
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		IConnection oldConn = (IConnection) request.getConnectionEditPart()
				.getModel();
		IGraphNode newTarget = (IGraphNode) getHost().getModel();
		if (!oldConn.getSource().canReconnectOutgoingConnection(oldConn.getTarget(), newTarget)) {
			return null;
		}
		ConnectionReconnectOperation operation = new ConnectionReconnectOperation(
				oldConn);
		operation.setNewTarget(newTarget);
		return new CommandWrapper(operation);
	}

}
