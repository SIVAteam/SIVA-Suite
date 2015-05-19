package org.iviPro.preview;

import org.iviPro.model.graph.INodeAnnotation;

public class NodeTimeContainer {
	
	protected long time;
	protected INodeAnnotation annotation;
	
	public NodeTimeContainer(long start, INodeAnnotation annotation) {
		this.time=start;
		this.annotation=annotation;
	}
}
