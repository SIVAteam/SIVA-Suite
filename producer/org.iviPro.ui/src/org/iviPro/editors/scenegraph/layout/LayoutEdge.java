package org.iviPro.editors.scenegraph.layout;

/**
 * Klasse f�r Kanten im Layoutgraphen
 * 
 * @author grillc
 *
 */
public class LayoutEdge {
	LayoutNode start;
	LayoutNode end;
	LayoutEdge(LayoutNode start, LayoutNode end) {
		this.start = start;
		this.end = end;
	}
}