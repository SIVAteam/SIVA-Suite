package org.iviPro.editors.annotationeditor;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Klasse die die Input-Parameter fuer den Globale-Annotationen-Editor kapselt.
 */
public class GlobalAnnotationEditorInput implements IEditorInput {
	private static Logger logger = Logger
			.getLogger(GlobalAnnotationEditorInput.class);

	/**
	 * Erstellt ein Input-Objekt fuer den Global-Annotations-Editor
	 * 
	 * @param scene
	 *            Der Knoten, dessen Annotationen bearbeitet werden sollen.
	 * 
	 */
	public GlobalAnnotationEditorInput() {
		logger.debug("Created new AnnotateGlobalEditorInput"); //$NON-NLS-1$
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return Messages.GlobalAnnotationEditorName;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return getName();
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GlobalAnnotationEditorInput) {
			return true;
		}
		return false;
	}

}
