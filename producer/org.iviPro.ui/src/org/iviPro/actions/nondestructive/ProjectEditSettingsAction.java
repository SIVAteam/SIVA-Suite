package org.iviPro.actions.nondestructive;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.model.Project;
import org.iviPro.projectsettings.PlayerSettingsPage;
import org.iviPro.projectsettings.ProjectSettingsDialog;
import org.iviPro.theme.Icons;

public class ProjectEditSettingsAction extends Action implements IWorkbenchAction,
		ApplicationListener {
		
	public ProjectEditSettingsAction() {
		setToolTipText(Messages.ProjectEditSettingsAction_SettingsTooltip);
		setText(Messages.ProjectEditSettingsAction_SettingsText);
		setImageDescriptor(Icons.ACTION_PROJECTSETTINGS.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_PROJECTSETTINGS
				.getDisabledImageDescriptor());
		Application.getDefault().addApplicationListener(this);
		setEnabled(Application.getCurrentProject() != null);
	}
	public void run() {
		PreferenceManager mgr = new PreferenceManager();
		PreferencePage playerSettings = new PlayerSettingsPage();
		PreferenceNode playerSettingsNode = new PreferenceNode("playersettings", playerSettings); //$NON-NLS-1$
		mgr.addToRoot(playerSettingsNode);
		
		// Init the Settingsstorer and the Pages, needed for Operations-Pattern
//		SettingsStorer storer = new SettingsStorer();
//		PageStartmode pageStartmode = new PageStartmode(storer);
//		PagePlayerSettings pagePlayerLayout = new PagePlayerSettings(storer);
//		PageLayout pageLayout = new PageLayout(storer);
//		PageExport pageExport = new PageExport(storer);
//		PageExtended pageExtended = new PageExtended(storer);
//		PageSemanticZoom pageSemanticZoom = new PageSemanticZoom(storer);
//		
//		storer.addPage(pageStartmode);	
//		storer.addPage(pagePlayerDesign);
//		storer.addPage(pageLayout);
//		storer.addPage(pageExport);
//		storer.addPage(pageExtended);
//		storer.addPage(pageSemanticZoom);
//	
//		PreferenceNode startmodeNode = new PreferenceNode("startmode", pageStartmode); //$NON-NLS-1$
//		PreferenceNode playerDesignNode = new PreferenceNode("playerdesign", pagePlayerDesign);//$NON-NLS-1$
//		PreferenceNode layoutNode = new PreferenceNode("layoutsetting", pageLayout); //$NON-NLS-1$
//		PreferenceNode exportNode =  new PreferenceNode("exportsettings", pageExport);//$NON-NLS-1$
//		PreferenceNode extendedNode = new PreferenceNode("extendedsettings", pageExtended);//$NON-NLS-1$
//		PreferenceNode semanticZoomNode = new PreferenceNode("semanticzoomsettings", pageSemanticZoom);//$NON-NLS-1$
//		
//		
//		mgr.addToRoot(startmodeNode);
//		mgr.addToRoot(playerDesignNode);
//		mgr.addToRoot(layoutNode);
//		mgr.addToRoot(exportNode);
//		mgr.addToRoot(extendedNode);
//		mgr.addToRoot(semanticZoomNode);
		
		
		
//		PreferenceDialog dlg = new PreferenceDialog(new Shell(display), mgr);
		ProjectSettingsDialog dlg = new ProjectSettingsDialog(null, mgr);		
		dlg.open();
	}
	
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProjectClosed(Project project) {
		setEnabled(false);

	}

	@Override
	public void onProjectOpened(Project project) {
		setEnabled(true);

	}

}