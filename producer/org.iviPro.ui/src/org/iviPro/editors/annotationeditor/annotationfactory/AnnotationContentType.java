package org.iviPro.editors.annotationeditor.annotationfactory;

/**
 * die verschiedenen Content Typen der Annotationen
 * Verschiedene Annotationstypen können den gleichen Inhalt haben
 * z.B. globaler Richtext oder getriggerter Richtext
 * @author juhoffma
 */
public enum AnnotationContentType {
	TEXT, 
	RICHTEXT, 
	SUBTITLE, 
	VIDEO, 
	PICTURE, 
	AUDIO,
	PDF;
}
