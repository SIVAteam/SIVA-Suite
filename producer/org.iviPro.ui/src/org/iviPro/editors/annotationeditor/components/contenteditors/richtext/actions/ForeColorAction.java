package org.iviPro.editors.annotationeditor.components.contenteditors.richtext.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.htmltext.HtmlComposer;
import org.eclipse.mylyn.htmltext.commands.Command;
import org.eclipse.mylyn.htmltext.commands.formatting.SetForegroundColorCommand;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;

/**
 * @author juhoffma
 * 
 */
public class ForeColorAction extends AbstractCommandWrapper {

    public ForeColorAction(HtmlComposer composer) {
        super("Foreground Color", 1, composer); //$NON-NLS-1$
		setImageDescriptor(ImageDescriptor.createFromImage((Image)images.get(ActionImages.ActionImage_ForeColorAction)));
    }
    
    public void run() {
        ColorDialog colorDialog = new ColorDialog(this.composer.getShell());
        RGB rgb = colorDialog.open();
        if (rgb != null) {
        	((SetForegroundColorCommand) this.wrappedCommand).setColor(colorDialog.getRGB());
        } 
        super.run();
    }

	@Override
	protected Command getWrappedCommand() {
		return new SetForegroundColorCommand();
	}
}
