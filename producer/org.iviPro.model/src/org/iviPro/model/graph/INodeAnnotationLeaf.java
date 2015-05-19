/**
 * 
 */
package org.iviPro.model.graph;

import java.util.List;

import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.resources.IResource;

/**
 * @author dellwo
 */
public abstract class INodeAnnotationLeaf extends INodeAnnotation {

	/** Definition der moeglichen und erforderlichen Verbindungen */
	private static final ConnectionConstraints CONNECTION_CONSTRAINTS = //
	new ConnectionConstraints(0, 0, new ConnectionTargetDefinition[] {});

	public INodeAnnotationLeaf(LocalizedString title, Project project) {
		super(title, project, CONNECTION_CONSTRAINTS);
	}

	public INodeAnnotationLeaf(String title, Project project) {
		super(title, project, CONNECTION_CONSTRAINTS);
	}
	
	/**
	 * Returns a list of <code>IResource</code> elements the annotation uses
	 * or depends upon. The returned value should never be <code>null</code>.
	 * @return list of resources
	 */
	public abstract List<IResource> getResources();
	
	/**
	 * Indicates if the annotation is a trigger annotation of
	 * a {@link NodeMark mark annotation}.
	 * @return true if the annotation is a trigger annotation - false otherwise
	 */
	public boolean isTriggerAnnotation() {
		for (IGraphNode node : getParents()) {
			if (node instanceof NodeMark) {
				return true;
			}
		}
		return false;
	}

}
