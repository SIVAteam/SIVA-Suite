package org.iviPro.editors.scenegraph.dnd;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.iviPro.application.Application;
import org.iviPro.dnd.TransferMedia;
import org.iviPro.dnd.TransferScene;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.resources.Scene;

public class TransferSceneDropTargetListener extends
		AbstractTransferDropTargetListener {

	private static Logger logger = Logger
			.getLogger(TransferSceneDropTargetListener.class);

	private NodeSceneFactory factory = new NodeSceneFactory();

	public TransferSceneDropTargetListener(EditPartViewer viewer) {
		super(viewer, TransferScene.getInstance());
		logger.trace("Created TransferSceneDropTargetListener."); //$NON-NLS-1$
	}

	@Override
	public Transfer getTransfer() {
		Transfer transfer = super.getTransfer();
		logger.trace("getTransfer(): " + transfer); //$NON-NLS-1$
		return transfer;
	}

	@Override
	protected void updateTargetRequest() {
		// Setze Drop-Location, damit der Szenen-Knoten auch dort angezeigt
		// werden kann wo er gedropt wurde.
		((CreateRequest) getTargetRequest()).setLocation(getDropLocation());
	}

	@Override
	protected void handleDrop() {
		logger.debug("handleDrop()"); //$NON-NLS-1$
		String dropKey = (String) getCurrentEvent().data;
		factory.setDropKey(dropKey);
		super.handleDrop();
	}

	protected void handleDragOver() {
		super.handleDragOver();
		// Zunaechst wird DND auf ablehnend gesetzt
		// Spaeter akzpetieren wir, falls das transferierte Objekt passt.
		getCurrentEvent().detail = DND.DROP_NONE;

		// Wir akzeptieren Szenen-Objekte generell und Media-Objekte, wenn sie
		// auf einen NodeScene-EditPart gedropt werden.
		Transfer transfer = getTransfer();
		if (transfer instanceof TransferScene) {
			getCurrentEvent().detail = DND.DROP_COPY;
		} else if (transfer instanceof TransferMedia) {
			Object targetModelObj = getTargetEditPart().getModel();
			if (targetModelObj instanceof NodeScene) {
				getCurrentEvent().detail = DND.DROP_COPY;
			}
		}

	}

	@Override
	protected Request createTargetRequest() {
		logger.debug("createTargetRequest()"); //$NON-NLS-1$
		CreateRequest request = new CreateRequest();
		request.setFactory(factory);
		return request;
	}

	class NodeSceneFactory implements CreationFactory {

		private String key = ""; //$NON-NLS-1$

		@Override
		public Object getNewObject() {
			logger.debug("getNewObject() for key=" + key); //$NON-NLS-1$
			Object[] objects = Application.getDragDropManager()
					.endTransfer(key);
			if (objects.length > 0 && objects[0] instanceof Scene) {
				Scene scene = (Scene) objects[0];
				Graph graph = Application.getCurrentProject().getSceneGraph();
				List<IGraphNode> sceneNodes = graph.searchNodes(
						NodeScene.class, true);
				String newNodeName = scene.getTitle();
				char baseChar = 'A';
				boolean nameAlreadyUsed = true;
				while (nameAlreadyUsed) {
					nameAlreadyUsed = false;
					for (IGraphNode node : sceneNodes) {
						if (newNodeName.equals(node.getTitle())) {
							nameAlreadyUsed = true;
							break;
						}
					}
					if (nameAlreadyUsed) {
						newNodeName = scene.getTitle() + " " + baseChar; //$NON-NLS-1$
						baseChar++;
					}
				}
				NodeScene sceneNode = new NodeScene(newNodeName,
						getDropLocation(), scene, Application
								.getCurrentProject());
				return sceneNode;
			} else {
				return null;
			}
		}

		@Override
		public Object getObjectType() {
			return NodeScene.class;
		}

		public void setDropKey(String key) {
			logger.debug("setDropKey(): " + key); //$NON-NLS-1$
			this.key = key;
		}
	}
}
