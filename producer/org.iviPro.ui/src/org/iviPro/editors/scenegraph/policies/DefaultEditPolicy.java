package org.iviPro.editors.scenegraph.policies;

import org.apache.log4j.Logger;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.iviPro.editors.scenegraph.commands.CommandWrapper;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.graph.NodeDeleteOperation;

/**
 * Diese Edit-Policy definiert welche Knoten wie geloescht werden koennen.
 */
public class DefaultEditPolicy extends ComponentEditPolicy {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DefaultEditPolicy.class);

	private boolean canBeDeleted;

	public DefaultEditPolicy(boolean canBeDeleted) {
		this.canBeDeleted = canBeDeleted;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.editpolicies.ComponentEditPolicy#createDeleteCommand(
	 * org.eclipse.gef.requests.GroupRequest)
	 */
	protected Command createDeleteCommand(GroupRequest deleteRequest) {
		if (canBeDeleted) {
			Object graph = getHost().getParent().getModel();
			Object node = getHost().getModel();
			if (graph instanceof Graph) {
				if (node instanceof IGraphNode) {
					IAbstractOperation operation = new NodeDeleteOperation(
							(IGraphNode) node);
					return new CommandWrapper(operation);
				}
			}
		}
		// Knoten kann nicht geloescht werden -> Default Command zurueck geben
		return super.createDeleteCommand(deleteRequest);
	}
}