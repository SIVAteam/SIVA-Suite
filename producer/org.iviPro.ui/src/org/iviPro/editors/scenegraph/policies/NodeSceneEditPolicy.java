package org.iviPro.editors.scenegraph.policies;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.iviPro.editors.scenegraph.commands.CommandWrapper;
import org.iviPro.editors.scenegraph.requests.MediaDropRequest;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.operations.CompoundOperation;
import org.iviPro.operations.graph.CreateMediaAnnotationOperation;
import org.iviPro.operations.graph.Messages;

public class NodeSceneEditPolicy extends DefaultEditPolicy {

	private static Logger logger = Logger.getLogger(NodeSceneEditPolicy.class);

	public NodeSceneEditPolicy(boolean canBeDeleted) {
		super(canBeDeleted);
	}

	@Override
	public EditPart getTargetEditPart(Request request) {
		EditPart editPart = null;
		if (request instanceof MediaDropRequest) {
			// Das ist notwendig, da sonst immer der Graph als EditPart
			// zurueck gegeben wird, der Knoten auf den man droppt aber
			// in getHost() zu finden ist. Warum das so ist, weiss ich
			// selber nicht, aber sonst gehts nicht ;)
			editPart = getHost();
			logger.trace("request[" + request + "]  ==> editPart: " + editPart); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			editPart = super.getTargetEditPart(request);
		}
		return editPart;
	}

	@Override
	public Command getCommand(Request request) {
		if (request instanceof MediaDropRequest) {
			MediaDropRequest dropRequest = (MediaDropRequest) request;
			NodeScene sceneNode = dropRequest.getSceneNode();
			List<IAbstractBean> mediaObjs = dropRequest.getDroppedMediaObjects();

			CompoundOperation<CreateMediaAnnotationOperation> compoundOperation = new CompoundOperation<CreateMediaAnnotationOperation>(
					Messages.CreateMediaAnnotationOperation_UndoRedoLabel);
			for (IAbstractBean mediaObject : mediaObjs) {
				compoundOperation
						.addOperation(new CreateMediaAnnotationOperation(
								sceneNode, mediaObject));
			}
			return new CommandWrapper(compoundOperation);
		}
		// TODO Auto-generated method stub
		return super.getCommand(request);
	}
}
