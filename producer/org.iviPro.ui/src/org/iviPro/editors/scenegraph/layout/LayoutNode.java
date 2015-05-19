package org.iviPro.editors.scenegraph.layout;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.iviPro.editors.scenegraph.editparts.IEditPartNode;

/**
 * Klasse für Knoten im Layoutgraphen
 * (Inkl. Speichern des referenzierenden EditParts)
 * 
 * @author grillc
 *
 */
public class LayoutNode {
	Rectangle rectangle;
	String id;
	ArrayList<LayoutEdge> edges;
	IEditPartNode editPartNode;
	LayoutNode(String id,int x,int y,int w,int h, IEditPartNode editPartNode) {
		rectangle = new Rectangle(x,y,w,h);
		this.id = id;
		this.editPartNode = editPartNode;
		edges = new ArrayList<LayoutEdge>();
	}
	public IEditPartNode getEditPartNode() {
		return editPartNode;
	}
	
	public int getX() {
		return rectangle.x;
	}
	
	public int getY() {
		return rectangle.y;
	}
	
	public void setX(int x) {
		rectangle.x = x;
	}
	
	public void setY(int y) {
		rectangle.y = y;
	}
	
	public int getWidth() {
		return rectangle.width;
	}
	
	public int getHeight() {
		return rectangle.height;
	}
	
	public void setWidth(int width) {
		rectangle.width = width;
	}
	
	public void setHeight(int height) {
		rectangle.height = height;
	}
	
	void addEdge(LayoutEdge e) {
		edges.add(e);
	}
	public Point2D getPos() {
		Point2D p = rectangle.getLocation();
		double x = rectangle.width/2.0;
		double y = rectangle.height/2.0;
		return new Point2D.Double(p.getX()+x,p.getY()+y);
	}
}
