/**
 * 
 */
package org.iviPro.model.annotation;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.resources.Scene;

/**
 * @author dellwo
 */
public class OverlayPathItem extends IAbstractBean implements Cloneable {

	/**
	 * Erstellt ein neues Pfad-Item
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param time
	 * @param project
	 */
	public OverlayPathItem(float x, float y, float width, float height,
			long time, Project project) {
		super(OverlayPathItem.class.getSimpleName(), project);
		init(x, y, width, height, time);
	}

	/**
	 * Initialisiert die Werte des Pfad-Items. Wird vom Konstruktor aufgerufen.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param time
	 */
	private void init(float x, float y, float width, float height, long time) {
		setX(x);
		setY(y);
		setWidth(width);
		setHeight(height);
		setTime(time);
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
	 * 
	 * @param scene
	 * @return
	 */
	public long getTimeRelativeTo(Scene scene) {
		if (scene.getStart() == null) {
			return 0;
		}
		return getTime() - scene.getStart();
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
	 * @uml.property name="x"
	 */
	private float x;

	/**
	 * Getter of the property <tt>x</tt>
	 * 
	 * @return Returns the x.
	 * @uml.property name="x"
	 */
	public float getX() {
		return x;
	}

	/**
	 * Setter of the property <tt>x</tt>
	 * 
	 * @param x
	 *            The x to set.
	 * @uml.property name="x"
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * @uml.property name="y"
	 */
	private float y;

	/**
	 * Getter of the property <tt>y</tt>
	 * 
	 * @return Returns the y.
	 * @uml.property name="y"
	 */
	public float getY() {
		return y;
	}

	/**
	 * Setter of the property <tt>y</tt>
	 * 
	 * @param y
	 *            The y to set.
	 * @uml.property name="y"
	 */
	public void setY(float y) {
		this.y = y;
	}

	/**
	 * @uml.property name="width"
	 */
	private float width;

	/**
	 * Getter of the property <tt>width</tt>
	 * 
	 * @return Returns the width.
	 * @uml.property name="width"
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * Setter of the property <tt>width</tt>
	 * 
	 * @param width
	 *            The width to set.
	 * @uml.property name="width"
	 */
	public void setWidth(float width) {
		this.width = width;
	}

	/**
	 * @uml.property name="height"
	 */
	private float height;

	/**
	 * Getter of the property <tt>height</tt>
	 * 
	 * @return Returns the height.
	 * @uml.property name="height"
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * Setter of the property <tt>height</tt>
	 * 
	 * @param height
	 *            The height to set.
	 * @uml.property name="height"
	 */
	public void setHeight(float height) {
		this.height = height;
	}
	
	@Override
	public OverlayPathItem clone() {
		OverlayPathItem item = new OverlayPathItem(this.x, this.y, this.width, this.height, this.time, project);
		item.setKeywords(this.getKeywords());
		for (LocalizedString descr : this.getDescriptions()) {
			item.setDescription(descr);
		}
		for (LocalizedString title : this.getTitles()) {
			item.setTitle(title);
		}
		return item;
	}

}
