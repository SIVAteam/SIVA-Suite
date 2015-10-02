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
	
	private boolean triggerAnnotation;
	private NodeMark parentMarkAnno;

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
		return triggerAnnotation;
	}
	
	/**
	 * Declares this annotation to be a trigger annotation of the given mark
	 * annotation.
	 * @param markAnno mark annotation for which this is the trigger annotation
	 * @throws IllegalArgumentException if the parameter is <code>null</code>
	 */
	public void setAsTriggerAnnotation(NodeMark markAnno) 
			throws IllegalArgumentException {
		if (markAnno == null) {
			throw new IllegalArgumentException("Argument may not be null");
		}
		triggerAnnotation = true;
		parentMarkAnno = markAnno;
	}
	
	/**
	 * Returns the parent <code>NodeMark</code> if this annotation is a trigger 
	 * annotation, otherwise <code>null</code> is returned.
	 * @return parent <code>NodeMark</code> or <code>null</code> if this is not
	 * a trigger annotation
	 */
	public NodeMark getParentMarkAnno() {
		return parentMarkAnno;
	}

}
