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

/**
 * Selection control element for which conditions can be defined. In contrary 
 * to the standard <code>NodeSelectionControls</code> these can only be
 * connected as child nodes to <code>NodeCondSelection</code> elements. 
 * 
 * @author John
 *
 */
public abstract class AbstractNodeSelectionControl extends INodeAnnotationAction {
	
	public enum SelectionControlType {
		DEFAULT,
		CONDITIONAL
	}
	
	/**
	 * Der Name des "time"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_BUTTON_IMAGE = "buttonImage"; //$NON-NLS-1$
	public static final String PROP_RANK = "rank"; //$NON-NLS-1$	
	
	private Picture buttonImage = null;
	private boolean visible = true;
	
	/**
	 * Used to define an order on the list of controls of the parent NodeSelection.
	 */
	private int rank;
	private SelectionControlType type;

	/** Definition der moeglichen und erforderlichen Verbindungen */
	private static final ConnectionConstraints CONNECTION_CONSTRAINTS = //
	new ConnectionConstraints(1, 1, new ConnectionTargetDefinition[] {
			new ConnectionTargetDefinition(NodeScene.class, 0, 1),
			new ConnectionTargetDefinition(NodeSelection.class, 0, 1),
			new ConnectionTargetDefinition(NodeCondSelection.class, 0, 1),
			new ConnectionTargetDefinition(NodeQuiz.class, 0, 1),
			new ConnectionTargetDefinition(NodeRandomSelection.class, 0, 1),
			new ConnectionTargetDefinition(NodeEnd.class, 0, 1) });

	public AbstractNodeSelectionControl(LocalizedString title, Project project) {
		super(title, project, CONNECTION_CONSTRAINTS);
	}

	public AbstractNodeSelectionControl(String title, Project project) {
		super(new LocalizedString(title, project), project,
				CONNECTION_CONSTRAINTS);
	}
	
	/**
	 * Returns the <code>Picture</code> representing the control
	 * @return <code>Picture</code> representing the control
	 */
	public Picture getButtonImage() {
		return buttonImage;
	}

	/**
	 * Sets the <code>Picture</code> which should be associated with the control.
	 * @param buttonImage <code>Picture</code> representing the control
	 */
	public void setButtonImage(Picture buttonImage) {
		Picture oldValue = this.buttonImage;
		this.buttonImage = buttonImage;
		firePropertyChange(PROP_BUTTON_IMAGE, oldValue, buttonImage);
	}
	
	/**
	 * Returns the rank among the other controls of the parent NodeSelection.
	 * @return rank in the list of controls of the parent NodeSelection
	 */
	public int getRank() {
		return rank;
	}
	
	/**
	 * Sets the rank of this control among the other controls of the parent
	 * NodeSelection.
	 * @param rank rank in the list of controls of the parent NodeSelection
	 */
	public void setRank(int rank) {
		int oldRank = this.rank;
		this.rank = rank;
		firePropertyChange(PROP_RANK, oldRank, rank);
	}
	
	/**
	 * Returns the type of the control.
	 * @return type of control
	 */
	public SelectionControlType getType() {
		return type;
	}
	
	/**
	 * Sets the type of the control.
	 * @param type type of control
	 */
	protected void setType(SelectionControlType type) {
		this.type = type;
	}
	
	/**
	 * Returns whether or not the path represented by this 
	 * <code>AbstractNodeSelectionControl</code> should be visible.
	 * @return true if the path should be visible - false otherwise 
	 */
	public boolean isVisible() {
		return visible;
	}
	
	/**
	 * Sets the visibility of the path represented by this 
	 * <code>AbstractNodeSelectionControl</code>. Setting it to true will 
	 * show the path.
	 * @param visible visibility value which should be used
	 */
	public void setVisibile(boolean visible) {
		this.visible = visible;
	}

	@Override
	public boolean isDependentOn(IAbstractBean object) {
		return false;
	}
}