package org.iviPro.editors.scenegraph.layout;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Hashtable;

import org.iviPro.editors.scenegraph.editparts.IEditPartNode;

/**
 * Klasse zum Aufbau eines Layoutgraphen
 * (Wird für die Algorithmen als Input benötigt)
 * 
 * @author grillc
 *
 */
public class LayoutGraph {
	Hashtable<String,LayoutNode> nodes = new Hashtable<String,LayoutNode>();
	ArrayList<LayoutEdge> edges = new ArrayList<LayoutEdge>();
	public ArrayList<Rectangle2D> getRectangles() {
		ArrayList<Rectangle2D> rectangles = new ArrayList<Rectangle2D>();
		for(LayoutNode n:nodes.values()) {
			rectangles.add(n.rectangle);
		}
		return rectangles;
	}
	
	/**
	 * Hole die Kanten
	 * @return
	 */
	public ArrayList<Line2D> getLines() {
		ArrayList<Line2D> lines = new ArrayList<Line2D>();
		for(LayoutEdge e:edges) {
			lines.add(new Line2D.Double(e.start.getPos(),e.end.getPos()));
		}
		return lines;
	}
	
	/**
	 * Füge neue Knoten hinzu
	 * @param id Identifier
	 * @param x X-Position
	 * @param y Y-Position
	 * @param w Breite
	 * @param h Höhe
	 * @param editPartNode referenzierender EditPart
	 */
	public void addNode(String id,int x,int y,int w, int h, IEditPartNode editPartNode) {
		nodes.put(id,new LayoutNode(id,x,y,w,h,editPartNode));
	}
	public void addEdge(String id1, String id2) {
		edges.add(new LayoutEdge(nodes.get(id1),nodes.get(id2)));
	}
	
	public Hashtable<String,LayoutNode> getNodes() {
		return nodes;
	}
}
