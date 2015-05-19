package org.iviPro.editors.scenegraph.dnd;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
import org.eclipse.swt.dnd.DND;
import org.iviPro.application.Application;
import org.iviPro.dnd.TransferMedia;
import org.iviPro.editors.scenegraph.requests.MediaDropRequest;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.graph.NodeScene;

/**
 * Listener, der fuer das Drag-and-Drop von Medien-Objekten auf den Graphen
 * zustaendig ist.
 * 
 * @author dellwo
 * 
 */
public class TransferMediaDropTargetListener extends
		AbstractTransferDropTargetListener {

	private static Logger logger = Logger
			.getLogger(TransferMediaDropTargetListener.class);

	/**
	 * Erstellt einen neuen Media-Drop-Listener auf dem angegebenen
	 * EditPartViewer.
	 * 
	 * @param viewer
	 *            Der EditPartViewer auf dem der Listener erstellt werden soll.
	 */
	public TransferMediaDropTargetListener(EditPartViewer viewer) {
		super(viewer, TransferMedia.getInstance());
		logger.trace("Created TransferMediaDropTargetListener."); //$NON-NLS-1$
	}

	/**
	 * Liest den DropKey aus dem MediaDrop-Request aus, holt sich die
	 * zugehoerigen Medien-Objekte und den Szenen-Knoten und befuellt damit den
	 * MediaDropRequest, damit dieser spaeter abgearbeitet werden kann.
	 */
	@Override
	protected void handleDrop() {
		// Hole die per DND uebertragenen Medien-Objekte und den Szenen-Knoten
		// auf den sie gedropt wurden, und befuelle damit den Request.
		Request request = getTargetRequest();
		if (request instanceof MediaDropRequest) {
			Object dropKey = getCurrentEvent().data;
			logger.debug("handleDrop() for key=" + dropKey); //$NON-NLS-1$
			if (dropKey == null) {
				logger.error("No DropKey was transfered during DND!"); //$NON-NLS-1$
				return;
			}
			updateTargetEditPart();
			NodeScene dropTarget = (NodeScene) getTargetEditPart().getModel();
			Object[] transferedObjects = Application.getDragDropManager()
					.endTransfer(dropKey.toString());
			List<IAbstractBean> mediaObjects = new ArrayList<IAbstractBean>();

			for (Object obj : transferedObjects) {
				if (obj instanceof IAbstractBean) {
					mediaObjects.add((IAbstractBean) obj);
				}
			}
			MediaDropRequest dropRequest = ((MediaDropRequest) request);
			dropRequest.setDroppedMediaObjects(mediaObjects);
			dropRequest.setSceneNode(dropTarget);

		}
		// Dann lassen wir den Request abarbeiten von der Super-Klasse
		super.handleDrop();

	}

	/**
	 * Entscheidet, ob das Medien-Objekt auf den Ziel-EditPart gedroppt werden
	 * kann. Je nachdem wird im Event das Feld "detail" auf DND.DROP_MOVE oder
	 * DND.DROP_NONE gesetzt.
	 */
	@Override
	protected void handleDragOver() {
		super.handleDragOver();
		logger.trace("handleDragOver(): " + getTargetEditPart() + " at (" //$NON-NLS-1$ //$NON-NLS-2$
				+ getDropLocation() + ")"); //$NON-NLS-1$
		// Wir akzeptieren Media-Objekte nur, wenn sie
		// auf einen NodeScene-EditPart gedropt werden.
		if (getTargetEditPart() instanceof EditPart) {
			EditPart targetEditPart = (EditPart) getTargetEditPart();
			Object targetModelObj = targetEditPart.getModel();
			if (targetModelObj instanceof NodeScene) {
				getCurrentEvent().detail = DND.DROP_MOVE;
			} else {
				getCurrentEvent().detail = DND.DROP_NONE;
			}
		} else {
			getCurrentEvent().detail = DND.DROP_NONE;
		}
	}

	/**
	 * Erstellt den MediaDrop-Request fuer den EditPart auf den die Medien-Datei
	 * gedropped werden soll.
	 */
	@Override
	protected Request createTargetRequest() {
		return new MediaDropRequest();
	}

	@Override
	protected void updateTargetRequest() {
	}

}
