/**
 * 
 */
package org.iviPro.model.annotation;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;

/** 
 * @author dellwo
 */
public class PolygonShape extends IMarkShape {

	public PolygonShape(LocalizedString title, Project project) {
		super(title, project);
		this.vertices = new LinkedList<Position>();
	}
	
	public PolygonShape(String title, Project project) {
		super(title, project);
		this.vertices = new LinkedList<Position>();
	}

	/** 
	 * @author dellwo
	 */
	public class Position implements Serializable{

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
		
		public boolean isEqual(Position compPos) {
			if (compPos.getX() == x && compPos.getY() == y) {
				return true;
			}	
			return false;
		}
	}

	/**
	 * @uml.property  name="vertices"
	 */
	private List<Position> vertices;

	/**
	 * Getter of the property <tt>vertices</tt>
	 * @return  Returns the vertices.
	 * @uml.property  name="vertices"
	 */
	public List<Position> getVertices() {
		return vertices;
	}

	/**
	 * Setter of the property <tt>vertices</tt>
	 * @param vertices  The vertices to set.
	 * @uml.property  name="vertices"
	 */
	public void setVertices(List<Position> vertices) {
		this.vertices = vertices;
	}
	
	@Override
	public PolygonShape clone() {
		PolygonShape clone = new PolygonShape(this.getTitle(), project);
		clone.setTime(this.getTime());
		LinkedList<Position> newVertices = new LinkedList<Position>();
		for (Position vertice : vertices) {
			Position newVert = new Position();
			newVert.setX(vertice.getX());
			newVert.setY(vertice.getY());
			newVertices.add(newVert);
		}
		clone.setVertices(newVertices);
		return clone;
	}

	@Override
	public boolean isShapeEqual(IMarkShape compShape) {
		if (compShape instanceof PolygonShape) {
			PolygonShape comPol = (PolygonShape) compShape;
			if (comPol.getVertices().size() != this.getVertices().size()) {
				return false;
			} else {
				for (int i = 0; i < comPol.getVertices().size(); i++) {
					if (!comPol.getVertices().get(i).isEqual(getVertices().get(i))) {
						return false;
					}
				}
			}
		} else {
			return false;
		}
		return true;
	}
}
