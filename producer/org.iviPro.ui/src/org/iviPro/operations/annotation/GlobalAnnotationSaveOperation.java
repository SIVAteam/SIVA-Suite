package org.iviPro.operations.annotation;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationContentType;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationType;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.annotation.OverlayPathItem;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.ScreenArea;
import org.iviPro.operations.CompoundOperation;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.global.ChangeKeywordsOperation;
import org.iviPro.operations.global.ChangeTitleOperation;

/**
 * Operation zum Ändern einer Szene.
 * @author juhoffma
 */
public class GlobalAnnotationSaveOperation extends IAbstractOperation {

	// Die Variablen zum Speichern der Operations-Daten
	private INodeAnnotation annotation;
	
	// hält Operationen zum Ändern von Titel, Zeit ...
	CompoundOperation<IAbstractOperation> changeAnnotation;
	
	private boolean isNew;
	
	/**
	 * @param annotation
	 * @param title
	 * @param text
	 * @param selection
	 * @param areaSelector
	 * @param editorContent
	 */
	public GlobalAnnotationSaveOperation(INodeAnnotation annotation,
			AnnotationType annotationType, String title, String description,
			String keywords, boolean mute, IAbstractBean editorContent, 
			ScreenArea screenArea, List<OverlayPathItem> opItems) {
		
		super(Messages.AnnotationSaveOperation_UndoLabel);
		Project project = Application.getCurrentProject();
		if (project == null || title == null || description == null || keywords == null
				|| opItems == null) {
			throw new IllegalArgumentException(
					"None of the parameters may be null."); //$NON-NLS-1$
		}				
		this.annotation = annotation;
		
		changeAnnotation = new CompoundOperation<IAbstractOperation>(Messages.AnnotationSaveOperation_UndoLabel);
		changeAnnotation.addOperation(new ChangeTitleOperation(annotation, title));
		changeAnnotation.addOperation(new ChangeDescriptionOperation(annotation, description));
		changeAnnotation.addOperation(new ChangeKeywordsOperation(annotation, keywords));
		
		// speichere die Anzeigeposition, nicht bei Audio und Subtitle
		if (!(annotationType.getContentType().equals(AnnotationContentType.SUBTITLE))) {
			changeAnnotation.addOperation(new ChangeScreenPositionOperation(annotation, screenArea, opItems));			
		}

		changeAnnotation.addOperation(new ChangeMuteOperation(annotation, mute));
		
		changeAnnotation.addOperation(new ChangeContentOperation(annotation, editorContent));

		List<INodeAnnotationLeaf> globalAnnotations = Application.getCurrentProject().getGlobalAnnotations();
		if (!globalAnnotations.contains(annotation)) {
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
		if (isNew) {
			Application.getCurrentProject().getGlobalAnnotations().add((INodeAnnotationLeaf) annotation);
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		changeAnnotation.undo(monitor, info);
		if (isNew) {
			Application.getCurrentProject().getGlobalAnnotations().remove((INodeAnnotationLeaf) annotation);		
		}
		return Status.OK_STATUS;
	}

}