package org.iviPro.editors.scenegraph.layout;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.iviPro.model.graph.IGraphNode;

public class LayoutManager implements PropertyChangeListener {
	
	//Wähle den zu verwendenden Layout-Algorithmus 
	Algorithm algorithm = FnorAlgorithm.getInstance();
	
	public static String PROP_NODE_CREATED = "nodeCreated";
	public static String PROP_NODE_MOVED = "nodeMoved";
	public static String PROP_ADDED_ANNOTATION = "addedAnnotation";
	public static String PROP_DELETED_ANNOTATION = "deletedAnnotation";
	public static String PROP_SEMANTIC_ZOOM_IN = "semanticZoomIn";
	public static String PROP_SEMANTIC_ZOOM_OUT = "semanticZoomOut";

	/**
	 * Fängt PropertyChangeEvents bei Veränderungen im Graphen und
	 * leitet diese an den Algorithmus weiter 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String property = evt.getPropertyName();
		Object o = evt.getNewValue();
		if(property.equals(PROP_NODE_MOVED)) {
			algorithm.nodeMoved((IGraphNode) o);
		}
		if(property.equals(PROP_NODE_CREATED)) {
			algorithm.newNode((IGraphNode) o);
		}
		if(property.equals(PROP_ADDED_ANNOTATION)) {
			algorithm.nodeResized((List<ElementChangeReport>) o);
		}
		if(property.equals(PROP_DELETED_ANNOTATION)) {
			algorithm.nodeResized((List<ElementChangeReport>) o);
		}
		if(property.equals(PROP_SEMANTIC_ZOOM_IN)) {
			algorithm.expandNodes((List<ElementChangeReport>) o);
		}
		if(property.equals(PROP_SEMANTIC_ZOOM_OUT)) {
			algorithm.minimizeNodes((List<ElementChangeReport>) o);
		}
	}



}