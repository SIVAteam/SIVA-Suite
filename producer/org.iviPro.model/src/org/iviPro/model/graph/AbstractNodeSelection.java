/**
 * 
 */
package org.iviPro.model.graph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.List;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;

/**
 * @author dellwo
 * @uml.dependency supplier="org.iviPro.model.graph.NodeSelectionControl"
 * @uml.dependency supplier="org.iviPro.model.graph.IGraphNode"
 */
public abstract class AbstractNodeSelection extends INodeAnnotationAction {

	private static final String PROP_TIMEOUT = "timeout";
	private static final String PROP_DEFAULT_CONTROL = "defaultControl";

	/**
	 * @uml.property name="buttonType"
	 */
	private ButtonType buttonType;

	/**
	 * Timeout in sek. (nur bedeutsam wenn default-control gesetzt ist).
	 */
	private int timeout;
	
	/**
	 * Standardpfad 
	 */
	private boolean useStandardPath;

	/**
	 * @uml.property name="defaultControl"
	 */
	private AbstractNodeSelectionControl defaultControl;

	/**
	 * Listener for the disconnection of <code>NodeSelectionControls</code>
	 * actually connected to this <code>NodeSelection</code>.
	 */
	class GraphChangeListener implements PropertyChangeListener, Serializable {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String property = evt.getPropertyName();
			
			if (property.equals(Graph.PROP_CONNECTION_REMOVED)) {
				IConnection conn = (IConnection) evt.getOldValue();
				if (conn.getSource().equals(AbstractNodeSelection.this)) {
					AbstractNodeSelectionControl control = 
							(AbstractNodeSelectionControl) conn.getTarget();
					onControlRemoved(control);
				}				
			}
		}
	}

	private PropertyChangeListener graphChangeListener = new GraphChangeListener();

	public AbstractNodeSelection(LocalizedString title, Project project, 
			ConnectionConstraints constraints) {
		super(title, project, constraints);
		this.useStandardPath = false;
		init();
	}

	public AbstractNodeSelection(String title, Project project,
			ConnectionConstraints constraints) {
		super(new LocalizedString(title, project), project,
				constraints);
		this.useStandardPath = false;
		init();
	}

	/**
	 * Setzt den Default Button-Type und registriert den Knoten als Listener auf
	 * seinen Kindern, damit er beim hinzufueugen/entfernen von Kindern das
	 * DefaultControl automatisch anpassen kann
	 */
	private void init() {
		buttonType = ButtonType.DEFAULT;
		timeout = 0;
	}
	
	@SuppressWarnings("unchecked")
	public List<AbstractNodeSelectionControl> getControls() {
		List controls = getChildren(AbstractNodeSelectionControl.class);
		return controls;
	}

	/**
	 * Called when a <code>NodeSelectionControl</code> is disconnected from
	 * this <code>NodeSelection</code>.
	 * Resets the default control for this <code>NodeSelection</code> to
	 * <code>null</code> if the given <code>NodeSelectionControl</code> is 
	 * the actual default.
	 * 
	 * @param control control which has been disconnected
	 */
	private void onControlRemoved(AbstractNodeSelectionControl control) {
		if ((defaultControl != null && defaultControl.equals(control)) 
				|| getChildren().isEmpty()) {
			setDefaultControl(null);
		}
	}
	
	public void setUseStandardPath(boolean useStandardPath) {
		this.useStandardPath = useStandardPath;
	}
	
	public boolean getUseStandardPath() {
		return this.useStandardPath;
	}

	/**
	 * Getter of the property <tt>buttonType</tt>
	 * 
	 * @return Returns the buttonType.
	 * @uml.property name="buttonType"
	 */
	public ButtonType getButtonType() {
		return buttonType;
	}

	/**
	 * Setter of the property <tt>buttonType</tt>
	 * 
	 * @param buttonType
	 *            The buttonType to set.
	 * @uml.property name="buttonType"
	 */
	public void setButtonType(ButtonType buttonType) {
		this.buttonType = buttonType;
	}

	/**
	 * Getter of the property <tt>defaultControl</tt>
	 * 
	 * @return Returns the defaultControl.
	 * @uml.property name="defaultControl"
	 */
	public AbstractNodeSelectionControl getDefaultControl() {
		return defaultControl;
	}

	/**
	 * Setter of the property <tt>defaultControl</tt>
	 * 
	 * @param defaultControl
	 *            The defaultControl to set.
	 * @uml.property name="defaultControl"
	 */
	public void setDefaultControl(AbstractNodeSelectionControl defaultControl) {
		AbstractNodeSelectionControl oldValue = this.defaultControl;
		this.defaultControl = defaultControl;
		firePropertyChange(PROP_DEFAULT_CONTROL, oldValue, defaultControl);
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		int oldValue = this.timeout;
		this.timeout = timeout;
		firePropertyChange(PROP_TIMEOUT, oldValue, timeout);
	}	

	@Override
	public boolean isDependentOn(IAbstractBean object) {
		return false;
	}

	@Override
	void setGraph(Graph graph) {
		if (graph == null) {
			Graph oldGraph = super.getGraph();
			oldGraph.removePropertyChangeListener(graphChangeListener);
		} else {
			graph.addPropertyChangeListener(graphChangeListener);
		}
		super.setGraph(graph);
	}
}
