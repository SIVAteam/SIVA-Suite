/**
 * 
 */
package org.iviPro.model.annotation;

import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;


/** 
 * @author dellwo
 */
public class PositionalShape extends IMarkShape {

	public PositionalShape(LocalizedString title, Project project) {
		super(title, project);
		// TODO Auto-generated constructor stub
	}

	public PositionalShape(String title, Project project) {
		super(title, project);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @uml.property  name="x"
	 */
	private float x;

	/**
	 * Getter of the property <tt>x</tt>
	 * @return  Returns the x.
	 * @uml.property  name="x"
	 */
	public float getX() {
		return x;
	}

	/**
	 * Setter of the property <tt>x</tt>
	 * @param x  The x to set.
	 * @uml.property  name="x"
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * @uml.property  name="y"
	 */
	private float y;

	/**
	 * Getter of the property <tt>y</tt>
	 * @return  Returns the y.
	 * @uml.property  name="y"
	 */
	public float getY() {
		return y;
	}

	/**
	 * Setter of the property <tt>y</tt>
	 * @param y  The y to set.
	 * @uml.property  name="y"
	 */
	public void setY(float y) {
		this.y = y;
	}
	
	@Override
	public PositionalShape clone() {
		PositionalShape clone = new PositionalShape(this.getTitle(), project);
		clone.setTime(this.getTime());
		clone.setX(this.getX());
		clone.setY(this.getY());		
		return clone;
	}

	@Override
	public boolean isShapeEqual(IMarkShape compShape) {
		if (compShape instanceof PositionalShape) {
			PositionalShape compPos = (PositionalShape) compShape;
			if (compPos.getX() == this.getX() &&
				compPos.getY() == this.getY()) {
				return true;
			}
		}
		return false;
	}

}
