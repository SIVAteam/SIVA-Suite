package org.iviPro.editors.scenegraph.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.iviPro.application.Application;
import org.iviPro.editors.scenegraph.commands.CommandWrapper;
import org.iviPro.editors.scenegraph.policies.DefaultResizePolicy;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.ProjectSettings;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeEnd;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.graph.NodeQuizControl;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.NodeSelection;
import org.iviPro.model.graph.NodeSelectionControl;
import org.iviPro.model.graph.NodeStart;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.graph.NodeCreateOperation;
import org.iviPro.operations.graph.NodeMoveOperation;

/**
 * EditPart fuer den Graphen selbst.<br>
 * <br>
 * Dieser EditPart dient daher als Container, und wird durch die weisse Flaeche
 * repraesentiert, auf der alle weiteren Graph-Elemente platziert werden. Er ist
 * auch zustaendig fuer das Layout der Elemente und die Container-EditPolicies,
 * sowie festszustellen, dass neue Knoten im Model hinzugefuegt wurden und der
 * View daher geupdated werden muss.
 * 
 * @author dellwo
 */
public class EditPartGraph extends AbstractGraphicalEditPart implements
		PropertyChangeListener {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EditPartGraph.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
	 */
	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			// Upon activation, attach to the model element as a property change
			// listener.
			((IAbstractBean) getModel()).addPropertyChangeListener(this);
			
			//Listen to changes in the projectSettings(e.g. semantic zoom)
			ProjectSettings ps = Application.getCurrentProject().getSettings();
			ps.addPropertyChangeListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {

		// disallows the removal of this edit part from its parent
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new RootComponentEditPolicy());
		// handles constraint changes (e.g. moving and/or resizing) of model
		// elements
		// and creation of new model elements
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new NodeXYLayoutEditPolicy());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		Figure f = new FreeformLayer();
		f.setBorder(new MarginBorder(3));
		f.setLayoutManager(new FreeformLayout());

		// Create the static router for the connection layer
		ConnectionLayer connLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
		connLayer.setConnectionRouter(new ShortestPathConnectionRouter(f));

		return f;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
	 */
	@Override
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			// Upon deactivation, detach from the model element as a property
			// change listener.
			((IAbstractBean) getModel()).removePropertyChangeListener(this);
			
			//Stop listening to ProjectSettings
			ProjectSettings ps = Application.getCurrentProject().getSettings();
			ps.removePropertyChangeListener(this);
		}
	}

	/**
	 * Private Hilfsfunktion, die das Model-Element gleich richtig castet, damit
	 * wir uns das anderswo sparen koennen.
	 * 
	 * @return
	 */
	private Graph getCastedModel() {
		return (Graph) getModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	@Override
	public List getModelChildren() {
		List<IGraphNode> originalChildren = getCastedModel().getNodes();
		List<IGraphNode> filteredChildren = new ArrayList<IGraphNode>();
		for (IGraphNode child : originalChildren) {
			if (isSupported(child)) {
				filteredChildren.add(child);
			}
		}
		return filteredChildren;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.beans.PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		// these properties are fired when nodes are added into or removed from
		// the Graph instance and must cause a call of refreshChildren()
		// to update the diagram's contents.
		Object obj = null;
		if (Graph.PROP_NODE_ADDED.equals(prop)) {
			obj = evt.getNewValue();
		} else if (Graph.PROP_NODE_REMOVED.equals(prop)) {
			obj = evt.getOldValue();
		}
		if (obj instanceof IGraphNode) {
			if (isSupported((IGraphNode) obj)) {
				refreshChildren();
			}
		}
		
		// Wenn nur volle semantische Zoomstufen Zoomstufen erlaubt -> Setze auf Level 1
		//(benötigt zum Speichern der Positionswerte in den einzelnen Stufen)
		Project project = Application.getCurrentProject();
		if (project != null) {
			ProjectSettings ps = Application.getCurrentProject().getSettings();
			if(ProjectSettings.PROP_FULLSEMANTICZOOMLEVELS.equals(prop) && ps.isFullSemanticZoomLevels()) {
				List modelChildren = getModelChildren();
				for (Object child : modelChildren) {
					((IGraphNode)child).setSemZoomlevel(1);
				}
			}
		}
	}

	/**
	 * EditPolicy for the Figure used by this edit part. Children of
	 * XYLayoutEditPolicy can be used in Figures with XYLayout.
	 * 
	 */
	private static class NodeXYLayoutEditPolicy extends XYLayoutEditPolicy {

		@Override
		protected EditPolicy createChildEditPolicy(EditPart child) {
			return new DefaultResizePolicy();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @seeConstrainedLayoutEditPolicy#createChangeConstraintCommand(
		 * ChangeBoundsRequest, EditPart, Object)
		 */
		@Override
		protected Command createChangeConstraintCommand(
				ChangeBoundsRequest request, EditPart child, Object constraint) {
			if (child.getModel() instanceof IGraphNode
					&& constraint instanceof Rectangle) {
				// return a command that can move and/or resize a node
				NodeMoveOperation operation = new NodeMoveOperation(
						(IGraphNode) child.getModel(), request,
						(Rectangle) constraint);
				return new CommandWrapper(operation);
			}
			return super.createChangeConstraintCommand(request, child,
					constraint);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * ConstrainedLayoutEditPolicy#createChangeConstraintCommand(EditPart,
		 * Object)
		 */
		@Override
		protected Command createChangeConstraintCommand(EditPart child,
				Object constraint) {
			// not used in this example
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see LayoutEditPolicy#getCreateCommand(CreateRequest)
		 */
		@Override
		protected Command getCreateCommand(CreateRequest request) {
			Object newObject = request.getNewObject();
			if (newObject instanceof IGraphNode) {
				IGraphNode newNode = (IGraphNode) newObject;
				IAbstractOperation operation = new NodeCreateOperation(newNode,
						(Graph) getHost().getModel(),
						(Rectangle) getConstraintFor(request));
				return new CommandWrapper(operation);
			}
			return null;
		}
	}

	private boolean isSupported(IGraphNode node) {
		if (node instanceof NodeStart // 
				|| node instanceof NodeEnd //
				|| node instanceof NodeScene // 
				|| node instanceof NodeSelection //
				|| node instanceof NodeSelectionControl //
				|| node instanceof NodeQuiz //
				|| node instanceof NodeQuizControl) {
			return true;
		}
		return false;
	}

	//setze einheitliches sem. Zoomlevel wenn ProjectSettings.completeZoomLevels == true
	public void setConsistentSemZoomLevels() {
		ProjectSettings ps = Application.getCurrentProject().getSettings();
		if(ps.isFullSemanticZoomLevels()) {
			List modelChildren = getModelChildren();
			for (int i=1; i < modelChildren.size(); i++) {
				int pre = ((IGraphNode)(modelChildren.get(i-1))).getSemZoomlevel();
				int cur = ((IGraphNode)(modelChildren.get(i))).getSemZoomlevel();
				
				// wenn Knoten nicht alle das gleiche sem. Zoomlevel haben
				if(pre != cur) {
					for (Object child : modelChildren) {
						((IGraphNode)child).setSemZoomlevel(1);
					}
					break;
				}
			}
		}
	}
}