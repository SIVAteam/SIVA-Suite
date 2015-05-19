/**
 * 
 */
package org.iviPro.model.graph;

import java.util.List;

import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.resources.IResource;

/**
 * Simple selection control element used to define an alternative path for
 * a user selection. In contrary to <code>NodeCondSelectionControls</code> it
 * can be connected as child to <code>NodeSelections</code> as well as 
 * <code>NodeCondSelections</code>. 
 * @author John
 *
 */
public class NodeSelectionControl extends AbstractNodeSelectionControl {

	public NodeSelectionControl(LocalizedString title, Project project) {
		super(title, project);
		setType(SelectionControlType.DEFAULT);
	}

	public NodeSelectionControl(String title, Project project) {
		super(new LocalizedString(title, project), project);
		setType(SelectionControlType.DEFAULT);
	}
}
