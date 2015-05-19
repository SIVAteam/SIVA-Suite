/**
 * 
 */
package org.iviPro.model.graph;

import java.util.ArrayList;
import java.util.List;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.resources.IResource;
import org.iviPro.model.resources.Picture;

public class NodeQuizControl extends INodeAnnotationAction {

	/**
	 * Der Name des "time"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_AMOUNT_QUIZ_POINTS = "amountPoints"; //$NON-NLS-1$	
	// private LocalizedString amountPoints = null;

	private int minValue = 0;
	private int maxValue = 0;

	private Picture buttonImage = null;

	/** Definition der moeglichen und erforderlichen Verbindungen */
	private static final ConnectionConstraints CONNECTION_CONSTRAINTS = //
	new ConnectionConstraints(1, 1, new ConnectionTargetDefinition[] {
			new ConnectionTargetDefinition(NodeScene.class, 0, 1),
			new ConnectionTargetDefinition(NodeSelection.class, 0, 1),
			new ConnectionTargetDefinition(NodeCondSelection.class, 0, 1),
			new ConnectionTargetDefinition(NodeQuiz.class, 0, 1),
			new ConnectionTargetDefinition(NodeRandomSelection.class, 0, 1),
			new ConnectionTargetDefinition(NodeEnd.class, 0, 1) });

	public NodeQuizControl(LocalizedString title, Project project) {
		super(title, project, CONNECTION_CONSTRAINTS);
	}

	public NodeQuizControl(String title, Project project) {
		super(new LocalizedString(title, project), project,
				CONNECTION_CONSTRAINTS);
	}
	
	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}
	
	@Override
	public boolean isDependentOn(IAbstractBean object) {
		return false;
	}
}
