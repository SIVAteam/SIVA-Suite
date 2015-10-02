/**
 * 
 */
package org.iviPro.model.graph;

import java.util.LinkedList;
import java.util.List;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.annotation.IMarkShape;

/**
 * @author dellwo
 */
public class NodeMark extends INodeAnnotationAction {
	
	public static final String PROP_DURATION = "duration"; //$NON-NLS-1$
	

	// die Annotation die von der Mark Annotation getriggered wird
	private INodeAnnotation trigAnnotation;
	
	/**
	 * Duration in ns during which the triggered annotation will be shown.
	 */
	private long duration;
	
	/**
	 * @uml.property name="shapes"
	 */
	private List<IMarkShape> shapes;
	
	/**
	 * @uml.property name="type"
	 */
	private NodeMarkType type;
	
	/**
	 * Style information e.g. CSS for formatting.
	 * 
	 * @uml.property name="style"
	 */
	private String style = ""; //$NON-NLS-1$
	
	private String buttonLabel;
	

	/** Definition der moeglichen und erforderlichen Verbindungen */
	private static final ConnectionConstraints CONNECTION_CONSTRAINTS = //
	new ConnectionConstraints(1, 1, new ConnectionTargetDefinition[] {
			new ConnectionTargetDefinition(INodeAnnotationLeaf.class, 1, 1)});

	public NodeMark(String title, Project project) {
		super(title, project, CONNECTION_CONSTRAINTS);
		this.shapes = new LinkedList<IMarkShape>();
		this.setScreenArea(ScreenArea.OVERLAY);
	}

	public NodeMark(LocalizedString title, Project project) {
		super(title, project, CONNECTION_CONSTRAINTS);
		this.shapes = new LinkedList<IMarkShape>();
		this.setScreenArea(ScreenArea.OVERLAY);
	}

	public void setTriggerAnnotation(INodeAnnotationLeaf annotation) {
		this.trigAnnotation = annotation;
		annotation.setAsTriggerAnnotation(this);
	}

	public INodeAnnotation getTriggerAnnotation() {
		return this.trigAnnotation;
	}
	
	/**
	 * Retrieves the duration the annotation triggered by this mark annotation 
	 * will be shown. The value is stored in nanoseconds.
	 * @return duration of triggered annotation in ns
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Sets the duration the annotation triggered by this mark annotation 
	 * will be shown. The value has to be given in nanoseconds.
	 */
	public void setDuration(long duration) {
		long oldValue = this.duration;
		this.duration = duration;
		firePropertyChange(PROP_DURATION, oldValue, duration);
	}

	/**
	 * Getter of the property <tt>shapes</tt>
	 * 
	 * @return Returns the shapes.
	 * @uml.property name="shapes"
	 */
	public List<IMarkShape> getShapes() {
		return shapes;
	}

	/**
	 * Setter of the property <tt>shapes</tt>
	 * 
	 * @param shapes
	 *            The shapes to set.
	 * @uml.property name="shapes"
	 */
	public void setShapes(List<IMarkShape> shapes) {
		this.shapes = shapes;
	}

	/**
	 * Getter of the property <tt>style</tt>
	 * 
	 * @return Returns the style.
	 * @uml.property name="style"
	 */
	public String getStyle() {
		return style;
	}

	/**
	 * Setter of the property <tt>style</tt>
	 * 
	 * @param style
	 *            The style to set.
	 * @uml.property name="style"
	 */
	public void setStyle(String style) {
		this.style = style;
	}

	/**
	 * Getter of the property <tt>type</tt>
	 * 
	 * @return Returns the type.
	 * @uml.property name="type"
	 */
	public NodeMarkType getType() {
		return type;
	}

	/**
	 * Setter of the property <tt>type</tt>
	 * 
	 * @param type
	 *            The type to set.
	 * @uml.property name="type"
	 */
	public void setType(NodeMarkType type) {
		this.type = type;
	}

	/**
	 * Getter of the <tt>buttonLabel</tt>
	 * 
	 * @returns Text on the button.
	 */
	public String getButtonLabel() {
		return buttonLabel;
	}

	/**
	 * Setter of the <tt>buttonLabel</tt>
	 * 
	 * @param text
	 *            Text on the button.
	 */
	public void setButtonLabel(String text) {
		buttonLabel = text;
	}

	@Override
	public boolean isDependentOn(IAbstractBean object) {
		return object != null && trigAnnotation.isDependentOn(object);
	}

	@Override
	public String getBeanTag() {
		return "Mark annotation";
	}
}
