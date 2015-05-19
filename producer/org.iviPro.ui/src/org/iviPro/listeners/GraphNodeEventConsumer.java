package org.iviPro.listeners;

import org.iviPro.model.graph.IGraphNode;

public interface GraphNodeEventConsumer {

	public void onGraphChildAdded(IGraphNode node, IGraphNode newChild);

	public void onGraphChildRemoved(IGraphNode node, IGraphNode oldChild);

	public void onGraphParentAdded(IGraphNode node, IGraphNode newParent);

	public void onGraphParentRemoved(IGraphNode node, IGraphNode oldParent);

	public void onGraphNodePropertyChanged(IGraphNode node, String property,
			Object oldValue, Object newValue);
}
