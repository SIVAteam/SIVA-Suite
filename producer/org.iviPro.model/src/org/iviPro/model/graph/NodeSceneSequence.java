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

/**
 * @author dellwo
 */
public class NodeSceneSequence extends IGraphNode {

	/** Definition der moeglichen und erforderlichen Verbindungen */
	private static final ConnectionConstraints CONNECTION_CONSTRAINTS = //
	new ConnectionConstraints(1, 1, new ConnectionTargetDefinition[] {
			new ConnectionTargetDefinition(NodeScene.class, 0, 1),
			new ConnectionTargetDefinition(NodeSceneSequence.class, 0, 1),
			new ConnectionTargetDefinition(NodeEnd.class, 0, 1),
			new ConnectionTargetDefinition(NodeSelection.class, 0, 1),
			new ConnectionTargetDefinition(NodeCondSelection.class, 0, 1),
			new ConnectionTargetDefinition(NodeQuiz.class, 0, 1),
			new ConnectionTargetDefinition(NodeRandomSelection.class, 0, 1),
			new ConnectionTargetDefinition(NodeResume.class, 0, 1),});
	

	/**
	 * @uml.property name="sceneSequence"
	 */
	private Graph sceneSequence;

	public NodeSceneSequence(LocalizedString title, Point position,
			Project project) {
		super(title, position, project, CONNECTION_CONSTRAINTS);
		sceneSequence = new Graph(title, project);
	}

	/**
	 * Getter of the property <tt>sceneSequence</tt>
	 * 
	 * @return Returns the sceneSequence.
	 * @uml.property name="sceneSequence"
	 */
	public Graph getSceneSequence() {
		return sceneSequence;
	}

	/**
	 * Setter of the property <tt>sceneSequence</tt>
	 * 
	 * @param sceneSequence
	 *            The sceneSequence to set.
	 * @uml.property name="sceneSequence"
	 */
	public void setSceneSequence(Graph sceneSequence) {
		this.sceneSequence = sceneSequence;
	}

	@Override
	public boolean isDependentOn(IAbstractBean object) {
		return object != null && object == sceneSequence;
	}
}
