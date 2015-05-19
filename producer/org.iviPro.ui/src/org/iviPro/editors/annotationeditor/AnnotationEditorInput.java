package org.iviPro.editors.annotationeditor;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.iviPro.application.Application;
import org.iviPro.model.graph.NodeScene;

/**
 * Klasse die die Input-Parameter fuer den Annotations-Editor kapselt.
 */
public class AnnotationEditorInput implements IEditorInput {
	private static Logger logger = Logger
			.getLogger(AnnotationEditorInput.class);

	/**
	 * Das Szenenknoten-Objekt fuer das die Annotation gemacht werden soll.
	 */
	private NodeScene sceneNode;

	/**
	 * Erstellt ein Input-Objekt fuer den Szenen-Editor
	 * 
	 * @param scene
	 *            Der Knoten, dessen Annotationen bearbeitet werden sollen.
	 * 
	 */
	public AnnotationEditorInput(NodeScene sceneNode) {
		this.sceneNode = sceneNode;
		logger.debug("Created new AnnotateSceneEditorInput for " + sceneNode); //$NON-NLS-1$
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
		return sceneNode.getTitle(Application.getCurrentLanguage());
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return getName();
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	/**
	 * Gibt die Szene die annotiert werden soll zurueck
	 * 
	 * @return Zu annotierende Szene
	 */
	public NodeScene getSceneNode() {
		return sceneNode;
	}

	@Override
	public boolean equals(Object obj) {
		// Wenn zwei Editor-Inputs gleich sind, wird kein neuer Editor
		// aufgemacht, sondern der bereits offene Editor in den Vordergrund
		// geschaltet.
		if (obj instanceof AnnotationEditorInput) {
			NodeScene otherSceneNode = ((AnnotationEditorInput) obj)
					.getSceneNode();
			if (sceneNode == null && otherSceneNode != null) {
				return false;
			}
			return sceneNode.equals(otherSceneNode);
		}
		return false;
	}

}
