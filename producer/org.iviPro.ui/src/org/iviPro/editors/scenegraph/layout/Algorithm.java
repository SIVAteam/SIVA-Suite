package org.iviPro.editors.scenegraph.layout;

import java.util.List;

import org.iviPro.model.graph.IGraphNode;

/**
 * Interface für den Aufbau von Graph-Layouting-Algorithmen
 * @author grillc
 *
 */
public interface Algorithm {

	public void newNode(IGraphNode node);
	
	public void nodeMoved(IGraphNode node);
	
	public void nodeResized(List<ElementChangeReport> list);
	
	public void expandNodes(List<ElementChangeReport> list);
	
	public void minimizeNodes(List<ElementChangeReport> list);

}
