package org.iviPro.editors.annotationeditor.components.contenteditors.richtext.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.htmltext.HtmlComposer;
import org.eclipse.mylyn.htmltext.commands.Command;
import org.eclipse.mylyn.htmltext.commands.formatting.SetBackgroundColorCommand;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;

/**
 * @author juhoffma
 * 
 */
public class BackColorAction extends AbstractCommandWrapper {

    public BackColorAction(HtmlComposer composer) {
        super("Background Color", IAction.AS_CHECK_BOX, composer); //$NON-NLS-1$
		setImageDescriptor(ImageDescriptor.createFromImage((Image)images.get(ActionImages.ActionImage_BackColorAction)));
    }
    
    public void run() {
        ColorDialog colorDialog = new ColorDialog(this.composer.getShell());
        RGB rgb = colorDialog.open();
        if (rgb != null) {
        	((SetBackgroundColorCommand) this.wrappedCommand).setColor(colorDialog.getRGB());
            super.run();
        }  
    }

	@Override
	protected Command getWrappedCommand() {
		return new SetBackgroundColorCommand();
	}
}
