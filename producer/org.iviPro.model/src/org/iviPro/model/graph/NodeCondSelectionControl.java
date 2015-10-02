/**
 * 
 */
package org.iviPro.model.graph;

import java.util.ArrayList;
import java.util.List;

import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;

/**
 * Selection control element for which conditions can be defined. In contrary 
 * to the standard <code>NodeSelectionControls</code> these can only be
 * connected as child nodes to <code>NodeCondSelection</code> elements. 
 * @author John
 *
 */
public class NodeCondSelectionControl extends AbstractNodeSelectionControl {
		
	private List<NodeScene> prerequisiteScenes = new ArrayList<NodeScene>();
		
	public NodeCondSelectionControl(LocalizedString title, Project project) {
		super(title, project);
		setType(SelectionControlType.CONDITIONAL);
	}

	public NodeCondSelectionControl(String title, Project project) {
		super(new LocalizedString(title, project), project);
		setType(SelectionControlType.CONDITIONAL);
	}
	
	/**
	 * Returns a list of scenes which need to be visited before the path 
	 * represented by this <code>NodeCondSelectionControl</code> can be
	 * entered.
	 * @return list of prerequisite scenes
	 */
	public List<NodeScene> getPrerequisiteScenes() {
		return prerequisiteScenes;
	}
	
	/**
	 * Sets the list of scenes which need to be visited before the path
	 * represented by this <code>NodeCondSelectionControl</code> can be
	 * entered to the given list.
	 * @param prerequisiteScenes new list of prerequisite scenes
	 */
	public void setPrequisiteScenes(List<NodeScene> prerequisiteScenes) {
		this.prerequisiteScenes = prerequisiteScenes;
	}

	@Override
	public String getBeanTag() {
		return "Conditional control";
	}
	
	@Override
	public boolean validateNode() {
		if (getPrerequisiteScenes().isEmpty()) {
			setValidationError(getBeanTag() + ": " + getTitle() + " (id: " + getNodeID() + ")\n\n" +
					"No condition has been defined!" );
			return false;
		}
		return super.validateNode();
	}
	
}