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
	 * Absolute time in the video where this shape occurs.
	 */
	private long time;

	/**
	 * Returns the absolute time in the video where this shape occurs.
	 * @return absolute time of the shape
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Sets the absolute time in the video where this shape occurs.
	 * 
	 * @param time
	 *            The time to set.
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
