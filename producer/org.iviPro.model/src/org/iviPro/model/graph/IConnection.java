package org.iviPro.model.graph;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;

/**
 * 
 * @author dellwo
 * 
 */
public abstract class IConnection extends IAbstractBean {

	private IGraphNode source;
	private IGraphNode target;

	protected IConnection(IGraphNode source, IGraphNode target, Project project) {
		super(source.getTitle() + " -> " + target.getTitle(), project); //$NON-NLS-1$
		this.source = source;
		this.target = target;
	}

	public static IConnection createConnection(IGraphNode source,
			IGraphNode target, Project project) {
		if (target instanceof INodeAnnotationLeaf || target instanceof NodeMark) {
			return new DependentConnection(source, target, project);
		} else {
			return new DefaultConnection(source, target, project);
		}
	}

	public IGraphNode getSource() {
		return source;
	}

	public IGraphNode getTarget() {
		return target;
	}

	public void setSource(IGraphNode source) {
		this.source = source;
	}

	public void setTarget(IGraphNode target) {
		this.target = target;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IConnection) {
			IConnection other = (IConnection) obj;
			return (source == other.source && target == other.target);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return source.hashCode() * target.hashCode();
	}

}
