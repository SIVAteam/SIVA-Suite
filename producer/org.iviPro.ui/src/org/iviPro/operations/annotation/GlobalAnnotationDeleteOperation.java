package org.iviPro.operations.annotation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.model.Project;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Löschen einer globalen Annotation
 * 
 * @author juhoffma
 * 
 */
public class GlobalAnnotationDeleteOperation extends IAbstractOperation {

	// Die Variablen zum Speichern der Operations-Daten
	private INodeAnnotationLeaf annotation;
			
	/**
	 * @param annotation
	 */
	public GlobalAnnotationDeleteOperation(INodeAnnotationLeaf annotation) {
		
		super(Messages.AnnotationSaveOperation_UndoLabel);
		Project project = Application.getCurrentProject();
		if (project == null || annotation == null) {
			throw new IllegalArgumentException(
					"None of the parameters may be null."); //$NON-NLS-1$
		}				
		this.annotation = annotation;
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
		Application.getCurrentProject().getGlobalAnnotations().remove(annotation);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		Application.getCurrentProject().getGlobalAnnotations().add(annotation);
		return Status.OK_STATUS;
	}

}