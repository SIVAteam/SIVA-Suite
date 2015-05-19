package org.iviPro.editors.annotationeditor.annotationfactory;
import org.iviPro.editors.annotationeditor.Messages;

/**
 * die Gruppen zu den Annotationen gehören können (Standard und Action)
 * Standard Annotationen können auch als globale Annotation verwendet werden
 * Diese entsprechen den normalen Annotationen: 
 * NodeAnnotationAudio, NodeAnnotationVideo, NodeAnnotationRichtext, NodeAnnotationText
 * NodeAnnotationSubtitle, NodeAnnotationPicture
 * somit den Subclasses von INodeAnnotationLeaf
 * 
 * Mark umfasst die Markierungsannotationen und ist somit ein MarkNode (INodeAnnotationAction)
 * welche eine Standard-Annotation triggern kann
 * @author juhoffma
 */
public enum AnnotationGroup {
	STANDARD(Messages.AnnotationType_GROUP1_NAME, true), 
	MARK(Messages.AnnotationType_GROUP2_NAME, true);
	
	private String name;
	
	private boolean inUse;
	
	public boolean inUse() {
		return inUse;
	}
	
	private AnnotationGroup(String name, boolean inUse) {
		this.name = name;
		this.inUse = inUse;
	}
	
	public String getName() {
		return this.name;
	}
}
