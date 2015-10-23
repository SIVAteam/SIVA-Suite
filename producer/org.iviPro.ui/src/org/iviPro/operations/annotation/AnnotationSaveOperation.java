package org.iviPro.operations.annotation;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationContentType;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationFactory;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationType;
import org.iviPro.editors.scenegraph.editparts.IEditPartNode;
import org.iviPro.editors.scenegraph.editparts.PartFactory;
import org.iviPro.editors.scenegraph.layout.ElementChangeReport;
import org.iviPro.editors.scenegraph.layout.LayoutManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.annotation.IMarkShape;
import org.iviPro.model.annotation.OverlayPathItem;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IConnection;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.NodeAnnotationVideo;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.graph.NodeMarkType;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.ScreenArea;
import org.iviPro.model.resources.IVideoResource;
import org.iviPro.model.resources.PdfDocument;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;
import org.iviPro.model.resources.VideoThumbnail;
import org.iviPro.operations.CompoundOperation;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.global.ChangeImagesOperation;
import org.iviPro.operations.global.ChangeKeywordsOperation;
import org.iviPro.operations.global.ChangeTimeOperation;
import org.iviPro.operations.global.ChangeTitleOperation;

/**
 * Operation zum Ändern einer Szene.
 * 
 * @author juhoffma
 * 
 */
public class AnnotationSaveOperation extends IAbstractOperation {
	
	//Identifikator fuer PropertyChangeEvent
	public static final String PROP_ANNO_ADDED = "annotationAdded";
	
	// Die Variablen zum Speichern der Operations-Daten
	private INodeAnnotation annotation;
	private final NodeScene nodeScene;
	
	// hält Operationen zum Ändern von Titel, Zeit ...
	CompoundOperation<IAbstractOperation> changeAnnotation;
	
	private boolean isNew;
		
	/**
	 * @param annotation
	 * @param title
	 * @param start
	 * @param end
	 * @param text
	 * @param selection
	 * @param areaSelector
	 * @param editorContent
	 */
	public AnnotationSaveOperation(INodeAnnotation annotation,
			AnnotationType annotationType, NodeScene nodeScene, String title, 
			String description,	Long start, Long end, String keywords, 
			boolean disableable, boolean pause, boolean mute, NodeMarkType markType,
			List<IMarkShape> markShapes, long markDuration, String buttonLabel,
			IAbstractBean editorContent, IAbstractBean replacementContent,
			String contentDescription, long thumbnailTime, ScreenArea screenArea, 
			List<OverlayPathItem> opItems) {
		
		super(Messages.AnnotationSaveOperation_UndoLabel);
		Project project = Application.getCurrentProject();
		if (project == null || title == null || start == null 
				|| end == null || keywords == null || opItems == null) {
			throw new IllegalArgumentException(
					"None of the parameters may be null."); //$NON-NLS-1$
		}				
		this.annotation = annotation;
		this.nodeScene = nodeScene;
		
		//if (editorContent != null && annotationType.getContentType().equals(AnnotationContentType.PDF)) {
		//	((PdfDocument)editorContent).changeThumbnailTime(thumbnailTime);
		//} 
				
		changeAnnotation = new CompoundOperation<IAbstractOperation>(Messages.AnnotationSaveOperation_UndoLabel);
		changeAnnotation.addOperation(new ChangeTitleOperation(annotation, title));
		changeAnnotation.addOperation(new ChangeTimeOperation(annotation, start, end));
		changeAnnotation.addOperation(new ChangeDisableableOperation(annotation, disableable));	
					
		
		// speichere die Anzeigeposition = links, oben, rechts, unten, Overlay Path
		if (!(annotationType.getContentType().equals(AnnotationContentType.SUBTITLE))) {
			changeAnnotation.addOperation(new ChangeScreenPositionOperation(annotation, screenArea, opItems));			
		}
		
		// falls ein MarkAnnotation erstellt wird, speichere die Markierungen
		if (annotation instanceof NodeMark) {
			if (markType != null && markShapes != null) {
				changeAnnotation.addOperation(new ChangeMarkOperation((NodeMark) annotation, markType, markShapes, markDuration, buttonLabel));
			}
		}	
				
		// sets describing images for the annotation
		changeAnnotation.addOperation(new ChangeImagesOperation(annotation, editorContent));
		
		// Some values have to be stored also/only in the content annotation, which 
		// is the triggered annotation in case of a mark annotation
		INodeAnnotation contentAnnotation = AnnotationFactory.getContentAnnotation(annotation);
		changeAnnotation.addOperation(new ChangeContentOperation(contentAnnotation, editorContent,
				replacementContent,	contentDescription, thumbnailTime));
		changeAnnotation.addOperation(new ChangeDisableableOperation(contentAnnotation, disableable));	
		changeAnnotation.addOperation(new ChangePauseOperation(contentAnnotation, pause));
		changeAnnotation.addOperation(new ChangeMuteOperation(contentAnnotation, mute));
		changeAnnotation.addOperation(new ChangeDescriptionOperation(contentAnnotation, description));
		changeAnnotation.addOperation(new ChangeKeywordsOperation(contentAnnotation, keywords));
		if (!(annotationType.getContentType().equals(AnnotationContentType.SUBTITLE))) {
			changeAnnotation.addOperation(new ChangeScreenPositionOperation(contentAnnotation, screenArea, opItems));			
		}
		
		Graph graph = nodeScene.getGraph();
		if (!graph.containsConnection(nodeScene, annotation)) {
			isNew = true;
		}
	}

	@Override
	public boolean canExecute() {
		return annotation != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.AnnotationSaveOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		
		changeAnnotation.execute(monitor, info);
		
		// add annotation (and possibly connected trigger annotation) to the graph
		if (isNew) {
			Graph graph = nodeScene.getGraph();
			graph.addNode(annotation);
			IConnection annoConn = IConnection.createConnection(nodeScene, 
					annotation, Application.getCurrentProject());
			graph.addConnection(annoConn);
			
			if (annotation instanceof NodeMark) {
				INodeAnnotation trigger = ((NodeMark) annotation).getTriggerAnnotation();
				graph.addNode(trigger);
				IConnection triggerConn = IConnection.createConnection(annotation, trigger, 
						Application.getCurrentProject());
				graph.addConnection(triggerConn);
			}
		}		
		
		IEditPartNode editPartNode = PartFactory.getReferingEditPart(nodeScene);
		//hole aktuelle größe
		int oldWidth = editPartNode.getFigure().getBounds().width;
		int oldHeight = editPartNode.getFigure().getBounds().height;
		//hole neue größe
		int newWidth = editPartNode.getFigure().getBounds().width;
		int newHeight = editPartNode.getFigure().getBounds().height;
		//erzeuge changereport für layoutmanager
		ElementChangeReport ecp = new ElementChangeReport(editPartNode,newWidth-oldWidth,newHeight-oldHeight);
		List<ElementChangeReport> editPartSizeChangedList = new LinkedList<ElementChangeReport>();
		editPartSizeChangedList.add(ecp);
		
		//PropertyChange für den Layoutmanager
		this.firePropChange(LayoutManager.PROP_ADDED_ANNOTATION, null, editPartSizeChangedList);
		
		//PropertyChange zum Refresh der Figure
		//(benötigt wenn Semantic Fisheye aktiv und Annotation gelöscht wurde)
		nodeScene.firePropertyChange(PROP_ANNO_ADDED, null, nodeScene);
		
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (isNew) {
			Graph graph = nodeScene.getGraph();
			
			// Remove added nodes (connections are removed during node deletion)
			graph.removeNode(annotation);
				
			if (annotation instanceof NodeMark) {
				INodeAnnotation trigger = ((NodeMark) annotation).getTriggerAnnotation();
				graph.removeNode(trigger);
			}			
		}
		changeAnnotation.undo(monitor, info);
		return Status.OK_STATUS;
	}

}