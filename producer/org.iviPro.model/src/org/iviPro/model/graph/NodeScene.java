/**
 * 
 */
package org.iviPro.model.graph;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.resources.IResource;
import org.iviPro.model.resources.Scene;

/**
 * @author dellwo
 * @uml.dependency supplier="org.iviPro.model.Scene"
 * @uml.dependency supplier="org.iviPro.model.graph.INodeAnnotation"
 */
public class NodeScene extends IGraphNode {

	/** Definition der moeglichen und erforderlichen Verbindungen */
	private static final ConnectionConstraints CONNECTION_CONSTRAINTS = //
	new ConnectionConstraints(1, 1, new ConnectionTargetDefinition[] {
			new ConnectionTargetDefinition(NodeScene.class, 0, 1),
			new ConnectionTargetDefinition(NodeEnd.class, 0, 1),
			new ConnectionTargetDefinition(NodeSelection.class, 0, 1),
			new ConnectionTargetDefinition(NodeCondSelection.class, 0, 1),
			new ConnectionTargetDefinition(NodeQuiz.class, 0, 1),
			new ConnectionTargetDefinition(NodeRandomSelection.class, 0, 1),
			new ConnectionTargetDefinition(NodeResume.class, 0, 1),
			new ConnectionTargetDefinition(NodeMark.class),
			new ConnectionTargetDefinition(INodeAnnotationLeaf.class)
			});

	/**
	 * @uml.property name="scene"
	 */
	private Scene scene;

	/**
	 * 
	 * @param title
	 * @param position
	 * @param scene
	 * @param project
	 */
	public NodeScene(LocalizedString title, Point position, Scene scene,
			Project project) {
		super(title, position, project, CONNECTION_CONSTRAINTS);
		this.scene = scene;
	}

	/**
	 * 
	 * @param title
	 * @param position
	 * @param scene
	 * @param project
	 */
	public NodeScene(String title, Point position, Scene scene, Project project) {
		super(title, position, project, CONNECTION_CONSTRAINTS);
		this.scene = scene;
	}
	
	/**
	 * Gibt eine Liste mit allen Annotationen dieses Szenen-Knotens zurueck.
	 * 
	 * @return Liste aller Annotationen dieses Szenen-Knotens.
	 */
	@SuppressWarnings("unchecked")
	public List<INodeAnnotation> getAnnotations() {
		return (List) getChildren(INodeAnnotation.class);
	}

	/**
	 * Getter of the property <tt>scene</tt>
	 * 
	 * @return Returns the scene.
	 * @uml.property name="scene"
	 */
	public Scene getScene() {
		return scene;
	}

	/**
	 * Setter of the property <tt>scene</tt>
	 * 
	 * @param scene
	 *            The scene to set.
	 * @uml.property name="scene"
	 */
	public void setScene(Scene scene) {
		this.scene = scene;
	}	

	@Override
	public boolean isDependentOn(IAbstractBean object) {
		// Szenen-Knoten ist abhaengig von seiner Szene
		return object != null
				&& (object == scene || object == scene.getVideo());
	}
}
