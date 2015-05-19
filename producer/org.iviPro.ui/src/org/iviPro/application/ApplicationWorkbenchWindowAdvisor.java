package org.iviPro.application;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.part.EditorInputTransfer;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
	private static Logger logger = Logger.getLogger(ApplicationWorkbenchWindowAdvisor.class);
    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }
    
    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        logger.debug("Setting Workbench-Windows properties (size,title,etc)."); //$NON-NLS-1$
        configurer.setInitialSize(new Point(1200, 800));
        configurer.setShowPerspectiveBar(false);
        configurer.setShowCoolBar(true);
        configurer.setShowStatusLine(true);
        configurer.setShowProgressIndicator(true);
        configurer.setShowFastViewBars(true);
        configurer.setTitle(Messages.ApplicationWorkbenchWindowAdvisor_Title);        
       
        configurer.addEditorAreaTransfer(EditorInputTransfer.getInstance());
        configurer.configureEditorAreaDropListener(new EditorAreaDropAdapter(getWindowConfigurer().getWindow()));
       
    }
    @Override
    //FIX damit setAccelerator() im Menü funktioniert
    public void postWindowCreate() {
        getWindowConfigurer().getActionBarConfigurer().getMenuManager().updateAll(true);
    }
}
