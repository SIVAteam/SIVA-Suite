package org.iviPro.scenedetection.sd_graph;

/**
 * Enumeration to flag Nodes (for example if it is visited or not).
 * Used for Dijkstra algorithm and scene extraction algorithm
 */
public enum NodeTypes {

	BLACK,

	GREY,

	WHITE,
	
	RED;
}