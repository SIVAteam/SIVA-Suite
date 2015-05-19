/**
 * 
 */
package org.iviPro.model.annotation;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;

/**
 * Abstract bean class for shape objects. Shapes are used by mark 
 * annotations.
 * @author dellwo
 * <p><b>Note:</b> Thank the original author for not leaving any comments in the first place.<p>
 */

public abstract class IMarkShape extends IAbstractBean implements Cloneable {

	public IMarkShape(LocalizedString title, Project project) {
		super(title, project);
	}
	
	public IMarkShape(String title, Project project) {
		super(title, project);
	}

	/**
	 * @uml.property name="time"
	 */
	private long time;

	/**
	 * Getter of the property <tt>time</tt>
	 * 
	 * @return Returns the time.
	 * @uml.property name="time"
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Setter of the property <tt>time</tt>
	 * 
	 * @param time
	 *            The time to set.
	 * @uml.property name="time"
	 */
	public void setTime(long time) {
		this.time = time;
	}
	
	/**
	 * Clones this shape. 
	 */
	public abstract IMarkShape clone();
	
	/**
	 * Compares this shape to the given shape.
	 * @param compShape shape to compare with
	 * @return true if both shapes are equal, false otherwise
	 */
	public abstract boolean isShapeEqual(IMarkShape compShape);
}
