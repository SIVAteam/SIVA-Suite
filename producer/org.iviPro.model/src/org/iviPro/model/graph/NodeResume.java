package org.iviPro.model.graph;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;

/**
 * Node representing a spot in the hypervideo where playback should be paused.
 * To carry on either a user interaction is needed or a timeout has to be 
 * specified after which playback will automatically continue.  
 * @author John
 *
 */
public class NodeResume extends INodeAnnotationAction {
	
	private int timeout = 0;
	private boolean useTimeout;

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
	
	public NodeResume(LocalizedString title, Project project) {
		super(title, project, CONNECTION_CONSTRAINTS);
	}
	
	public NodeResume(String title, Project project) {
		super(title, project, CONNECTION_CONSTRAINTS);
	}
	
	/**
	 * Set the timeout after which playback should continue.
	 * @param timeout time to wait before continuing
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	/**
	 * Get the timeout after which playback should continue.
	 * @return time to wait before continuing
	 */
	public int getTimeout() {
		return timeout;
	}
	
	/**
	 * Return whether or not the use of a timeout is configured for this 
	 * resume node.
	 * @return true if timeout should be used - false otherwise
	 */
	public boolean useTimeout() {
		return useTimeout;
	}
	
	/**
	 * Set whether or not a timeout should be used for this resume node.
	 * @param useTimeout value determining the use of a timeout
	 */
	public void setUseTimeout(boolean useTimeout) {
		this.useTimeout = useTimeout;
	}

	@Override
	public boolean isDependentOn(IAbstractBean object) {
		return false;
	}

	@Override
	public String getBeanTag() {
		return "Resume";
	}
}