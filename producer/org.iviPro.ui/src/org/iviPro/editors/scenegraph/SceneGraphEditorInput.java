package org.iviPro.editors.scenegraph;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.iviPro.model.graph.Graph;

public class SceneGraphEditorInput implements IEditorInput {

	private Graph graph;

	public SceneGraphEditorInput(Graph graph) {
		this.graph = graph;
	}

	public Graph getGraph() {
		return graph;
	}

	@Override
	public boolean equals(Object obj) {
		// Wenn zwei Editor-Inputs gleich sind, wird kein neuer Editor
		// aufgemacht, sondern der bereits offene Editor in den Vordergrund
		// geschaltet.
		if (obj instanceof SceneGraphEditorInput) {
			Graph otherGraph = ((SceneGraphEditorInput) obj).getGraph();
			if (graph == null && otherGraph != null) {
				return false;
			}
			return graph.equals(otherGraph);
		}
		return false;
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return graph.getTitle();
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return graph.getTitle();
	}

	@Override
	public Object getAdapter(Class arg0) {
		return null;
	}

}
