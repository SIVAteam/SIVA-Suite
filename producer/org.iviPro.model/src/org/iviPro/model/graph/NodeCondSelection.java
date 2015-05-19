/**
 * 
 */
package org.iviPro.model.graph;

import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;

/**
 * @author dellwo
 * @uml.dependency supplier="org.iviPro.model.graph.NodeSelectionControl"
 * @uml.dependency supplier="org.iviPro.model.graph.IGraphNode"
 */
public class NodeCondSelection extends AbstractNodeSelection {

	/** Definition der moeglichen und erforderlichen Verbindungen */
	private static final ConnectionConstraints CONNECTION_CONSTRAINTS = //
	new ConnectionConstraints(1, Integer.MAX_VALUE,
			new ConnectionTargetDefinition[] { // 
			new ConnectionTargetDefinition(NodeSelectionControl.class, 1,
					Integer.MAX_VALUE),
			new ConnectionTargetDefinition(NodeCondSelectionControl.class, 1,
					Integer.MAX_VALUE) });

	public NodeCondSelection(LocalizedString title, Project project) {
		super(title, project, CONNECTION_CONSTRAINTS);
	}

	public NodeCondSelection(String title, Project project) {
		super(new LocalizedString(title, project), project,
				CONNECTION_CONSTRAINTS);
	}
}
