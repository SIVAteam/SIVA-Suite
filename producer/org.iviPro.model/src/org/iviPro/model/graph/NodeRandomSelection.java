package org.iviPro.model.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.resources.IResource;

/**
 * Model of random selection nodes. This selection node represents random 
 * choices in the hypervideo. To each of its children a selection probability
 * can be assigned. When the node is reached in the video, a successor is
 * chosen according to those probabilities.
 * 
 * @author John
 *
 */
public class NodeRandomSelection extends INodeAnnotationAction {
	
	/**
	 * Map assigning selection probabilities to the children. Should be 
	 * <code>null</code> when equal probabilities should be used.
	 */
	private HashMap<IGraphNode, Integer> probabilityMap = null;
	
	/**
	 * Value determining if equal selection probabilities for all children
	 * should be used.
	 */
	private boolean equalProbability = true;

	private static final ConnectionConstraints CONNECTION_CONSTRAINTS =
			new ConnectionConstraints(1, Integer.MAX_VALUE,
					new ConnectionTargetDefinition[] { // 
						new ConnectionTargetDefinition(NodeScene.class, 0,
								Integer.MAX_VALUE), 
						new ConnectionTargetDefinition(NodeSelection.class, 0,
								Integer.MAX_VALUE),
						new ConnectionTargetDefinition(NodeCondSelection.class, 0,
								Integer.MAX_VALUE),
						new ConnectionTargetDefinition(NodeQuiz.class, 0,
								Integer.MAX_VALUE),
						new ConnectionTargetDefinition(NodeRandomSelection.class, 0,
								Integer.MAX_VALUE),
						new ConnectionTargetDefinition(NodeEnd.class, 0, 1) });

	public NodeRandomSelection(LocalizedString title, Project project) {
		super(title, project, CONNECTION_CONSTRAINTS);
	}

	public NodeRandomSelection(String title, Project project) {
		super(title, project, CONNECTION_CONSTRAINTS);
	}
		
	/**
	 * Returns a map assigning probabilities to the children of this 
	 * <code>NodeRandomSelection</code>.
	 * @return map assigning probabilities - should be null if equal 
	 * probabilities should be used
	 */
	public HashMap<IGraphNode, Integer> getProbabilityMap() {
		return probabilityMap;		
	}
	
	/**
	 * Sets the map assigning probabilities to the children of this 
	 * <code>NodeRandomSelection>/code>.
	 * @param map new map to be used - should be null if equal 
	 * probabilities should be used
	 */
	public void setProbabilityMap(HashMap<IGraphNode, Integer> map) {
		probabilityMap = map;
	}
	
	/**
	 * Returns whether or not equal probabilities should be used for randomly
	 * choosing a child node.
	 * @return true if equal probability should be assigned to children - false
	 * otherwise
	 */
	public boolean useEqualProbability() {
		return equalProbability;
	}
	
	/**
	 * Sets whether or not equal probabilities should be used for randomly
	 * choosing a child node.
	 * @param equalProbability value determining whether or not equal 
	 * probability should be used 
	 */
	public void setEqualProbability(boolean equalProbability) {
		this.equalProbability = equalProbability;
	}	

	@Override
	public boolean isDependentOn(IAbstractBean object) {
		return false;
	}
}
