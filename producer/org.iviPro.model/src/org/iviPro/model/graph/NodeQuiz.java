/**
 * 
 */
package org.iviPro.model.graph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.iviPro.model.BeanList;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.resources.IResource;

/**
 * @author dellwo
 * @uml.dependency supplier="org.iviPro.model.graph.NodeQuizControl"
 * @uml.dependency supplier="org.iviPro.model.graph.IGraphNode"
 */
public class NodeQuiz extends INodeAnnotationAction {

	private static final String PROP_TIMEOUT = "timeout";
	private static final String PROP_DEFAULT_CONTROL = "defaultControl";

	/**
	 * @uml.property name="testCounter"
	 */
	private StaticSaver staticSaver;

	/**
	 * Current TestID
	 */
	private int testID;

	/** Definition der moeglichen und erforderlichen Verbindungen */
	private static final ConnectionConstraints CONNECTION_CONSTRAINTS = //
	new ConnectionConstraints(1, Integer.MAX_VALUE,
			new ConnectionTargetDefinition[] { //
			new ConnectionTargetDefinition(NodeQuizControl.class, 1,
					Integer.MAX_VALUE) });

	/**
	 * @uml.property name="buttonType"
	 */
	private ButtonType buttonType;

	/**
	 * Timeout in sek. (nur bedeutsam wenn default-control gesetzt ist).
	 */
	private int timeout;

	/**
	 * @uml.property name="defaultControl"
	 */
	private List<NodeQuizControl> controlList;

	class GraphChangeListener implements PropertyChangeListener, Serializable {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getNewValue() instanceof NodeQuizControl) {
				NodeQuizControl control = (NodeQuizControl) evt.getNewValue();
				String property = evt.getPropertyName();
				if (property.equals(BeanList.PROP_ITEM_ADDED)) {
					onControlAdded(control);
				} else if (property.equals(BeanList.PROP_ITEM_REMOVED)) {
					onControlRemoved(control);
				}
			}
		}

	}

	private PropertyChangeListener graphChangeListener = new GraphChangeListener();

	public NodeQuiz(LocalizedString title, Project project) {
		super(title, project, CONNECTION_CONSTRAINTS);
		staticSaver = new StaticSaver();
		staticSaver.setTestCouner((staticSaver.getSaver()) + 1);
		testID = -1;
		init();
		controlList = new LinkedList<NodeQuizControl>();
	}

	public NodeQuiz(String title, Project project) {
		super(new LocalizedString(title, project), project,
				CONNECTION_CONSTRAINTS);
		testID = -1;
		init();
		staticSaver = new StaticSaver();
		staticSaver.setTestCouner((staticSaver.getSaver()) + 1);
		controlList = new LinkedList<NodeQuizControl>();
	}
	
	@Override
	public boolean isDependentOn(IAbstractBean object) {
		return false;
	}

	/**
	 * Setzt den Default Button-Type und registriert den Knoten als Listener auf
	 * seinen Kindern, damit er beim hinzufueugen/entfernen von Kindern das
	 * DefaultControl automatisch anpassen kann
	 */
	private void init() {
		buttonType = ButtonType.DEFAULT;
		setScreenArea(ScreenArea.OVERLAY);
		timeout = 0;
	}

	/**
	 * Wird aufgerufen, wenn ein QuizControl hinzugefuegt wurde.
	 * 
	 * @param control
	 */
	private void onControlAdded(NodeQuizControl control) {
		// Wenn ein Control hinzugefuegt wird und kein Control das
		// Default-Control ist, dann wird dieses Control das Default-Control.
		controlList.add(control);
	}

	/**
	 * Wird aufgerufen, wenn ein QuizControl entfernt wurde.
	 * 
	 * @param control
	 */
	private void onControlRemoved(NodeQuizControl control) {
		for (int i = 0; i < controlList.size(); i++) {
			if (controlList.get(i).equals(control)) {
				controlList.remove(i);
			}
		}
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
	public List<NodeQuizControl> getDefaultControl() {
		return controlList;
	}

	/**
	 * Setter of the property <tt>defaultControl</tt>
	 * 
	 * @param defaultControl
	 *            The defaultControl to set.
	 * @uml.property name="defaultControl"
	 */
	public void setDefaultControl(NodeQuizControl defaultControl) {
		if (controlList == null) {
			controlList = new LinkedList<NodeQuizControl>();
		}

		// nur hinzufügen, wenn noch nicht in controlList vohanden
		if (!controlList.contains(defaultControl)) {
			controlList.add(defaultControl);
		}
		firePropertyChange(PROP_DEFAULT_CONTROL, null, defaultControl);
	}
	
	@SuppressWarnings("unchecked")
	public List<NodeQuizControl> getControls() {
		List controls = getChildren(NodeQuizControl.class);
		return controls;
	}
	
	public int getTimeout() {
		return timeout;
	}	
	
	public void setTimeout(int timeout) {
		int oldValue = this.timeout;
		this.timeout = timeout;
		firePropertyChange(PROP_TIMEOUT, oldValue, timeout);
	}

	public int setNewTest() {
		return ++StaticSaver.testCounter;
	}

	public int getLastCreatedTestId() {
		return StaticSaver.testCounter;
	}

	public void setTestId(int id) {
		this.testID = id;
	}

	public int getTestId() {
		return this.testID;
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
