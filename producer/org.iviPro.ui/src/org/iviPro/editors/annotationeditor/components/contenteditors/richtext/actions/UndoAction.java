package org.iviPro.editors.annotationeditor.components.contenteditors.richtext.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.htmltext.commands.Command;
import org.eclipse.swt.graphics.Image;

/**
 * 
 * @author juhoffma
 * 
 */
public class UndoAction extends AbstractCommandWrapper {
    
	public UndoAction(String text, int style,
			org.eclipse.mylyn.htmltext.HtmlComposer composer) {
		super(text, style, composer);
		setImageDescriptor(ImageDescriptor.createFromImage((Image)images.get(ActionImages.ActionImage_UndoAction)));
	}

	@Override
	protected Command getWrappedCommand() {
		// TODO Auto-generated method stub
		return null;
	}

}
