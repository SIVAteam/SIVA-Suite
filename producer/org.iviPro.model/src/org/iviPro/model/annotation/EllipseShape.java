/**
 * 
 */
package org.iviPro.model.annotation;

import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;

/** 
 * @author dellwo
 */
public class EllipseShape extends PositionalShape {

	public EllipseShape(LocalizedString title, Project project) {
		super(title, project);
		// TODO Auto-generated constructor stub
	}
	
	public EllipseShape(String title, Project project) {
		super(title, project);
		// TODO Auto-generated constructor stub
	}	

	/** 
	 * @uml.property name="lengthA"
	 */
	private float lengthA;

	/** 
	 * Getter of the property <tt>lengthA</tt>
	 * @return  Returns the lengthA.
	 * @uml.property  name="lengthA"
	 */
	public float getLengthA() {
		return lengthA;
	}

	/** 
	 * @uml.property name="lengthB"
	 */
	private float lengthB;

	/** 
	 * Getter of the property <tt>lengthB</tt>
	 * @return  Returns the lengthB.
	 * @uml.property  name="lengthB"
	 */
	public float getLengthB() {
		return lengthB;
	}

	/** 
	 * Setter of the property <tt>lengthA</tt>
	 * @param lengthA  The lengthA to set.
	 * @uml.property  name="lengthA"
	 */
	public void setLengthA(float lengthA) {
		this.lengthA = lengthA;
	}

	/** 
	 * Setter of the property <tt>lengthB</tt>
	 * @param lengthB  The lengthB to set.
	 * @uml.property  name="lengthB"
	 */
	public void setLengthB(float lengthB) {
		this.lengthB = lengthB;
	}
	
	@Override
	public EllipseShape clone() {
		EllipseShape clone = new EllipseShape(this.getTitle(), project);
		clone.setTime(this.getTime());
		clone.setLengthA(this.lengthA);
		clone.setLengthB(this.lengthB);
		clone.setX(this.getX());
		clone.setY(this.getY());		
		return clone;
	}
	
	@Override
	public boolean isShapeEqual(IMarkShape compShape) {
		if (compShape instanceof EllipseShape) {
			EllipseShape compEllipse = (EllipseShape) compShape;
			if (this.getLengthA() == compEllipse.getLengthA() &&
				this.getLengthB() == compEllipse.getLengthB() && 
				this.getX() == compEllipse.getX() &&
				this.getY() == compEllipse.getY()) {
				return true;
			}
		} 
		return false;
	}
}
