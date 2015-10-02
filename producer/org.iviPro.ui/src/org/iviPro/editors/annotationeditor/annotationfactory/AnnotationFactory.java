package org.iviPro.editors.annotationeditor.annotationfactory;

import org.iviPro.application.Application;
import org.iviPro.editors.common.ScreenAreaSelector;
import org.iviPro.model.Project;
import org.iviPro.model.graph.IConnection;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeAnnotationAudio;
import org.iviPro.model.graph.NodeAnnotationPdf;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.NodeAnnotationRichtext;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.graph.NodeAnnotationText;
import org.iviPro.model.graph.NodeAnnotationVideo;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.graph.NodeMarkType;
import org.iviPro.model.resources.RichText;

/**
 * Klasse zum Erstellen von Annotationsobjekten etc.
 * Die AnnotationFactory und Unterscheidung zwischen den Mark und Standard Annotationen wäre
 * einfacher bzw. könnte man sich auf diese Art und Weise sparen, wenn man direkt in der
 * INodeAnnotation Klasse einen entsprechenden Type/Content Flag setzen könnte für Mark, Standard und Global
 * @author juhoffma
 */
public class AnnotationFactory {

	/**
	 * liefert ein AnnotationsObject für den AnnotationType
	 * @param type
	 * @return
	 */
	public static INodeAnnotation getAnnotationForType(AnnotationType type) {
		INodeAnnotation annotation = null;
		Project project = Application.getCurrentProject();
		String title = "";
		
		switch (type.getAnnotationGroup()) {
			case STANDARD:
				annotation = getAnnotationForContent(type.getContentType());
				break;
			case MARK:
				annotation = new NodeMark(title, project);
				((NodeMark) annotation).setTriggerAnnotation(getAnnotationForContent(type.getContentType()));
				((NodeMark) annotation).setType(NodeMarkType.BUTTON);
				break;
		}

		annotation.setScreenArea(ScreenAreaSelector.getAnnotationScreenArea());
		return annotation;
	}
	
	/**
	 * Returns an annotation (<code>INodeAnnotationLeaf</code>) for the given
	 * content type.
	 * @param content type of the content of the desired annotation
	 * @return annotation of type <code>content</code>
	 */
	private static INodeAnnotationLeaf getAnnotationForContent(AnnotationContentType content) {
		INodeAnnotationLeaf annotation = null;
		Project project = Application.getCurrentProject();
		String title = "";
		switch (content) {
			case AUDIO:
				annotation = new NodeAnnotationAudio(title, project);
				break;
			case PICTURE:
				annotation = new NodeAnnotationPicture(title, project);
				break;
			case RICHTEXT: 
				annotation = new NodeAnnotationRichtext(title, project);
				int hashCode = annotation.hashCode();
				String temporaryTitle = "Unnamed_Richtext_" + hashCode; //$NON-NLS-1$
				RichText richtext = new RichText(temporaryTitle, project);
				((NodeAnnotationRichtext) annotation).setRichtext(richtext);
				break;
			case SUBTITLE:
				annotation = new NodeAnnotationSubtitle(title, project);				
				break;
			case VIDEO:
				annotation = new NodeAnnotationVideo(title, project);
				break;
			case PDF:
				annotation = new NodeAnnotationPdf(title, project);
				break;
		}
		return annotation;		
	}
	
	/**
	 * liefert für eine best. Annotation den Annotationstyp
	 * @param annotation
	 * @return
	 */
	public static AnnotationType getAnnotationTypeForAnnotation(INodeAnnotation annotation) {
		if (annotation instanceof NodeAnnotationPicture) {
			return AnnotationType.PICTURE;
		} else if (annotation instanceof NodeAnnotationAudio) {
			return AnnotationType.AUDIO;
		} else if (annotation instanceof NodeAnnotationRichtext) {
			return AnnotationType.RICHTEXT;
		} else if (annotation instanceof NodeAnnotationVideo) {
			return AnnotationType.VIDEO;
		} else if (annotation instanceof NodeAnnotationSubtitle) {
			return AnnotationType.SUBTITLE;
		} else if (annotation instanceof NodeAnnotationPdf) {
			return AnnotationType.PDF;
		} else if (annotation instanceof NodeMark) {
			INodeAnnotation contentAnnotation = getContentAnnotation(annotation);
			if (contentAnnotation instanceof NodeAnnotationText) {
				return AnnotationType.MARK_TEXT;
			} else if (contentAnnotation instanceof NodeAnnotationPicture) {
				return AnnotationType.MARK_PICTURE;
			} else if (contentAnnotation instanceof NodeAnnotationAudio) {
				return AnnotationType.MARK_AUDIO;
			} else if (contentAnnotation instanceof NodeAnnotationRichtext) {
				return AnnotationType.MARK_RICHTEXT;
			} else if (contentAnnotation instanceof NodeAnnotationVideo) {
				return AnnotationType.MARK_VIDEO;
			} else if (contentAnnotation instanceof NodeAnnotationPdf) {
				return AnnotationType.MARK_PDF;
			}
		}
		return null;
	}	
	
	// liefert die Annotation in der der eigentliche Content (Picutre, Audio etc)
	// gespeichert ist, bei Mark Annotationen wird die INodeAnnotation direkt in NodeMark gespeichert
	// bei "normalen" die Annotation (z.B. NodeAnnotationAudio) selbst
	// Das ist erst verfügbar nachdem, die Annotation gespeichert wurde
	public static INodeAnnotation getContentAnnotation(INodeAnnotation annotation) {
		AnnotationGroup annotationGroup = AnnotationGroup.STANDARD;
		
		if (annotation instanceof NodeMark) {
			annotationGroup = AnnotationGroup.MARK;
		}
		
		switch (annotationGroup) {
			case STANDARD: return annotation;
			case MARK: return ((NodeMark) annotation).getTriggerAnnotation();
		}
		return null;
	}
}
