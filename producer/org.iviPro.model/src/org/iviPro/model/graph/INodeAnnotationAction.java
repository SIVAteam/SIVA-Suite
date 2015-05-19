/**
 * 
 */
package org.iviPro.model.graph;

import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;

/**
 * @author dellwo
 */
public abstract class INodeAnnotationAction extends INodeAnnotation {

	public INodeAnnotationAction(LocalizedString title, Project project,
			ConnectionConstraints connectionConstraints) {
		super(title, project, connectionConstraints);
	}
	
	public INodeAnnotationAction(String title, Project project,
			ConnectionConstraints connectionConstraints) {
		super(title, project, connectionConstraints);
	}
}
