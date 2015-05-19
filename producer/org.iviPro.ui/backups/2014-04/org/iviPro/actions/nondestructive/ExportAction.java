package org.iviPro.actions.nondestructive;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.editors.scenegraph.SceneGraphValidator;
import org.iviPro.model.Project;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.job.ExportJob;
import org.iviPro.newExport.profile.ExportProfile;
import org.iviPro.newExport.profile.ExportProfileProvider;
import org.iviPro.newExport.view.ExportWizard;
import org.iviPro.theme.Icons;

/**
 * Creates a wizard for exporting the current project.
 * 
 * @author Codebold
 * 
 */
public class ExportAction extends Action implements IWorkbenchAction,
		ApplicationListener {
	public final static String ID = ExportAction.class.getName();
	public final static String ACTIONDEFINITIONID = "org.iviPro.ui.ProjectExportAction"; //$NON-NLS-1$

	private static final Logger logger = Logger.getLogger(ExportAction.class);

	private final IWorkbenchWindow window;
	
	/**
	 * Constructs an action that creates an wizard for exporting the current
	 * project.
	 * 
	 * @param window
	 *            The current workbench window.
	 */
	public ExportAction(IWorkbenchWindow window) {
		this.window = window;
		setEnabled(false);
		setId(ID);
		setText("Export");
		setToolTipText("Export");
		setImageDescriptor(Icons.ACTION_PROJECT_EXPORT.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_PROJECT_EXPORT
				.getDisabledImageDescriptor());
		setActionDefinitionId(ACTIONDEFINITIONID);
		Application.getDefault().addApplicationListener(this);
	}

	@Override
	public void run() {
		SceneGraphValidator validator = new SceneGraphValidator();
		if (validator.validateSceneGraph()) {
			ExportProfileProvider provider = new ExportProfileProvider(
					Application.getCurrentProject().getSettings()
							.getSizeWidth(), Application.getCurrentProject()
							.getSettings().getSizeHeight());
			List<ExportProfile> availableExportProfiles = null;
			try {
				availableExportProfiles = provider.getAvailableProfiles(window
						.getShell());
			} catch (ExportException e) {
				logger.error(String.format("Can't open the export wizard! %s",
						e.getMessage()));
				return;
			}
			if (availableExportProfiles == null) {
				logger.warn("Can't open the export wizard, because getting the export profiles failed!");
				return;
			}

			ExportWizard wizard = new ExportWizard(provider,
					availableExportProfiles, Application.getCurrentProject()
							.getFile().getValue());
			WizardDialog dialog = new WizardDialog(window.getShell(), wizard);

			if (dialog.open() == Window.OK) {
				final File outputFolder = wizard.getOutputFolder();
				final List<ExportProfile> exportProfiles = wizard
						.getExportProfiles();
				int currentExport = 0;
				final int exportCount = getExportCount(exportProfiles);
				ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(window.getShell());    
				for (ExportProfile exportProfile : exportProfiles) {
					if (exportProfile.isChecked()) {
						currentExport++;
						final ExportJob exportJob = new ExportJob(Application.getCurrentProject()
								.getTitle(), Application.getCurrentProject()
								.getFile().getValue(), window.getWorkbench()
								.getDisplay(), exportProfile, outputFolder,
								currentExport, exportCount);
						try {
							progressDialog.run(true, true, new IRunnableWithProgress() {
								
								@Override
								public void run(IProgressMonitor monitor) throws InvocationTargetException,
										InterruptedException {
							        exportJob.runInBackground(monitor);
								}
							});
						} catch (InvocationTargetException e) {
							// TODO
						} catch (InterruptedException e) {
							// TODO
						}
						exportJob.schedule();
					}
				}
			}
		} else {
			// Shows error message.
			Shell parentShell = window.getShell();
			MessageDialog messageDialog = new MessageDialog(
					parentShell,
					Messages.ProjectExportAction_InvalidSceneGraphErrorDialogTitle,
					null,
					Messages.ProjectExportAction_InvalidSceneGraphErrorDialogMessage,
					MessageDialog.ERROR,
					new String[] { Messages.ProjectExportAction_InvalidSceneGraphErrorDialogOk },
					0);
			messageDialog.open();
		}
	}

	private int getExportCount(List<ExportProfile> exportProfiles) {
		int count = 0;
		for (ExportProfile exportProfile : exportProfiles) {
			if (exportProfile.isChecked()) {
				count++;
			}
		}
		return count;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void onProjectClosed(Project project) {
		setEnabled(false);
	}

	@Override
	public void onProjectOpened(final Project project) {
		// Activates the export menu item if there are more than two nodes
		// (start
		// and end node are always available).
		if (project.getSceneGraph().searchNodes(NodeScene.class, true).size() > 0) {
			setEnabled(true);
		}
		// Activates the action, if there are more than two nodes in the scene
		// graph.
		Application.getCurrentProject().getSceneGraph()
				.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent arg0) {
						if (project.getSceneGraph()
								.searchNodes(NodeScene.class, true).size() > 0) {
							setEnabled(true);
						} else {
							setEnabled(false);
						}
					}
				});
	}
}
