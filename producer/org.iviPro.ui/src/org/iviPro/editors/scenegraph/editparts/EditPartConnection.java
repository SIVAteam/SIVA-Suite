package org.iviPro.editors.scenegraph.editparts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.iviPro.editors.scenegraph.commands.CommandWrapper;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IConnection;
import org.iviPro.operations.graph.ConnectionDeleteOperation;

/**
 * EditPart fuer Connection-Elemente im Model.
 * 
 * @author dellwo
 */
class EditPartConnection extends AbstractConnectionEditPart
// implements PropertyChangeListener
{

	public void activate() {
		if (!isActive()) {
			super.activate();
			// Der PropertyChangeListener war nur fuer Aenderungen des
			// LineStyles
			// da. TODO: Line-Style bei Connections in SIVA ueberhaupt relevant?
			// ((ModelElement) getModel()).addPropertyChangeListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	protected void createEditPolicies() {
		// Selection handle edit policy.
		// Makes the connection show a feedback, when selected by the user.
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE,
				new ConnectionEndpointEditPolicy());
		// Allows the removal of the connection model element
		// TODO: DELLWO: EditPolicy zum Loeschen von Connections definieren!
		installEditPolicy(EditPolicy.CONNECTION_ROLE,
				new ConnectionEditPolicy() {
					protected Command getDeleteCommand(GroupRequest request) {
						IConnection conn = getCastedModel();
						Graph graph = conn.getSource().getGraph();
						return new CommandWrapper(
								new ConnectionDeleteOperation(getCastedModel(),
										graph));
					}
				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		PolylineConnection connection = (PolylineConnection) super
				.createFigure();
		// arrow at target endpoint
		connection.setTargetDecoration(new PolygonDecoration());
		// line drawing style
		// connection.setLineStyle(getCastedModel().getLineStyle());
		return connection;
	}

	/**
	 * Upon deactivation, detach from the model element as a property change
	 * listener.
	 */
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			// Der PropertyChangeListener war nur fuer Aenderungen des
			// LineStyles
			// da. TODO: Line-Style bei Connections in SIVA ueberhaupt relevant?
			// ((ModelElement) getModel()).removePropertyChangeListener(this);
		}
	}

	private IConnection getCastedModel() {
		return (IConnection) getModel();
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
	// * PropertyChangeEvent)
	// */
	// public void propertyChange(PropertyChangeEvent event) {
	// String property = event.getPropertyName();
	// if (Connection.LINESTYLE_PROP.equals(property)) {
	// ((PolylineConnection) getFigure()).setLineStyle(getCastedModel()
	// .getLineStyle());
	// }
	// }

}