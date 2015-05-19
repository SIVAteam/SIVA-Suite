package org.iviPro.model.graph;

import java.io.Serializable;

public class NodeIDGenerator implements Serializable {
	
	private static final long serialVersionUID = 4185464213072522062L;
	
	int nodeCounter = 1;
	
	public NodeIDGenerator() {
	}
		
	public int getNextID() {
		return nodeCounter++;
	}
}
