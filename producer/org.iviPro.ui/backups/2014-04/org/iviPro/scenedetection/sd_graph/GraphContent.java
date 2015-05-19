package org.iviPro.scenedetection.sd_graph;

public class GraphContent<T> {

	private T content;

	private boolean ambiguous; 
	
	GraphContent(T content, boolean ambiguous) {
		this.content = content;
		this.ambiguous = ambiguous;
	}
	
	public T getContent() {
		return content;
	}
	
	public boolean isAmbiguous() {
		return ambiguous;
	}
}
