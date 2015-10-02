/**
 * 
 */
package org.iviPro.model.graph;

import java.util.List;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.resources.IResource;

/**
 * @author dellwo
 */
public class NodeAnnotationText extends INodeAnnotationLeaf {

	public NodeAnnotationText(LocalizedString title, Project project) {
		super(title, project);
	}

	public NodeAnnotationText(String title, Project project) {
		super(title, project);

	}

	@Override
	public boolean isDependentOn(IAbstractBean object) {
		return false;
	}

	@Override
	public List<IResource> getResources() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBeanTag() {
		return "Text annotation";
	}

}
