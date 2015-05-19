package org.iviPro.application;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.iviPro.actions.nondestructive.GraphValidationAction;
import org.iviPro.actions.nondestructive.MediaViewAction;
import org.iviPro.actions.nondestructive.OpenEditorAction;
import org.iviPro.actions.nondestructive.OpenGlobalAnnotationEditorAction;
import org.iviPro.actions.nondestructive.OpenSceneGraphAction;
import org.iviPro.actions.nondestructive.OpenTocEditorAction;
import org.iviPro.actions.nondestructive.ProjectCloseAction;
import org.iviPro.actions.nondestructive.ProjectCreateNewAction;
import org.iviPro.actions.nondestructive.ProjectEditSettingsAction;
import org.iviPro.actions.nondestructive.ProjectExportAction;
import org.iviPro.actions.nondestructive.ExportAction;
import org.iviPro.actions.nondestructive.ProjectHandOverAction;
import org.iviPro.actions.nondestructive.ProjectOpenAction;
import org.iviPro.actions.nondestructive.ProjectSaveAction;
import org.iviPro.actions.nondestructive.UndoRedoWrapperAction;
import org.iviPro.actions.undoable.MediaDeleteAction;
import org.iviPro.actions.undoable.MediaLoadAction;
import org.iviPro.actions.undoable.SceneDetectionAction;
import org.iviPro.theme.Icons;
import org.iviPro.utils.PathHelper;

@SuppressWarnings("restriction")
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	private static Logger logger = Logger
			.getLogger(ApplicationActionBarAdvisor.class);

	private ProjectCreateNewAction projectNewToolbar;
	private ProjectCreateNewAction projectNew;

	private ProjectOpenAction projectOpenToolbar;
	private ProjectOpenAction projectOpen;

	private ProjectSaveAction projectSaveToolbar;
	private ProjectSaveAction projectSave;
	private ProjectSaveAction projectSaveAs;
	private ProjectSaveAction projectSaveAsToolbar;

	private ProjectHandOverAction projectHandOver;

	private ProjectEditSettingsAction projectSettings;
	private ProjectEditSettingsAction projectSettingsToolbar;

	private OpenTocEditorAction openToc;
	private OpenTocEditorAction openTocToolbar;

	private MediaLoadAction mediaLoadToolbar;
	private MediaLoadAction mediaLoad;

	private MediaDeleteAction mediaDeleteToolbar;
	private MediaDeleteAction mediaDelete;

	private ProjectExportAction projectExport;
	private ProjectExportAction projectExportToolbar;

	private ExportAction projectExportWizard;
	private ExportAction projectExportWizardToolbar;

	private OpenEditorAction editorScene;
	private OpenEditorAction editorSceneToolbar;

	private OpenSceneGraphAction openSceneGraph;

	private UndoRedoWrapperAction undoToolbar;
	private UndoRedoWrapperAction redoToolbar;

	private ProjectCloseAction projectClose;
	private MediaViewAction mediaView;
	private SceneDetectionAction editorSceneDetection;

	private OpenGlobalAnnotationEditorAction openGlobalAnnotationEditorAction;
	private OpenGlobalAnnotationEditorAction openGlobalAnnoToolbar;

	private GraphValidationAction graphValidator;

	private GraphValidationAction graphValidatorToolbar;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
		PropertyConfigurator.configure(PathHelper.getPathToLoggerIni());
	}

	protected void makeActions(IWorkbenchWindow window) {

		logger.debug("Registering actions for key binding..."); //$NON-NLS-1$

		// vorhandene nutzbare Aktionen
		IWorkbenchAction quitAction = ActionFactory.QUIT.create(window);
		quitAction.setText(Messages.ApplicationActionBarAdvisor_Menu_File_Exit);
		quitAction.setImageDescriptor(Icons.ACTION_APPLICATION_QUIT
				.getImageDescriptor());
		register(ActionFactory.ABOUT.create(window));
		register(quitAction);
		register(ActionFactory.UNDO.create(window));
		register(ActionFactory.REDO.create(window));
		register(ActionFactory.HELP_CONTENTS.create(window));
		register(ActionFactory.HELP_SEARCH.create(window));
		register(ActionFactory.RESET_PERSPECTIVE.create(window));

		// Build project
		projectNew = new ProjectCreateNewAction(window);
		projectNewToolbar = new ProjectCreateNewAction(window);
		projectNewToolbar.setImageDescriptor(Icons.TOOLBAR_PROJECT_NEW
				.getImageDescriptor());
		projectNewToolbar.setDisabledImageDescriptor(Icons.TOOLBAR_PROJECT_NEW
				.getDisabledImageDescriptor());
		register(projectNew);
		register(projectNewToolbar);

		// Open project
		projectOpen = new ProjectOpenAction(window);
		projectOpenToolbar = new ProjectOpenAction(window);
		projectOpenToolbar.setImageDescriptor(Icons.TOOLBAR_PROJECT_OPEN
				.getImageDescriptor());
		projectOpenToolbar
				.setDisabledImageDescriptor(Icons.TOOLBAR_PROJECT_OPEN
						.getDisabledImageDescriptor());
		register(projectOpen);
		register(projectOpenToolbar);

		// Save project
		projectSave = new ProjectSaveAction(window, false);
		projectSaveToolbar = new ProjectSaveAction(window, false);
		projectSaveToolbar.setImageDescriptor(Icons.TOOLBAR_PROJECT_SAVE
				.getImageDescriptor());
		projectSaveToolbar
				.setDisabledImageDescriptor(Icons.TOOLBAR_PROJECT_SAVE
						.getDisabledImageDescriptor());
		projectSaveAs = new ProjectSaveAction(window, true);
		projectSaveAsToolbar = new ProjectSaveAction(window, true);
		projectSaveAsToolbar.setImageDescriptor(Icons.TOOLBAR_PROJECT_SAVE_AS
				.getImageDescriptor());
		projectSaveAsToolbar
				.setDisabledImageDescriptor(Icons.TOOLBAR_PROJECT_SAVE_AS
						.getDisabledImageDescriptor());
		register(projectSave);
		register(projectSaveToolbar);
		register(projectSaveAs);
		register(projectSaveAsToolbar);

		// Load media
		mediaLoad = new MediaLoadAction(window);
		mediaLoadToolbar = new MediaLoadAction(window);
		mediaLoadToolbar.setImageDescriptor(Icons.TOOLBAR_MEDIA_LOAD
				.getImageDescriptor());
		mediaLoadToolbar.setDisabledImageDescriptor(Icons.TOOLBAR_MEDIA_LOAD
				.getDisabledImageDescriptor());
		register(mediaLoad);
		register(mediaLoadToolbar);

		// Delete media
		mediaDelete = new MediaDeleteAction(window);
		mediaDeleteToolbar = new MediaDeleteAction(window);
		mediaDeleteToolbar.setImageDescriptor(Icons.TOOLBAR_MEDIA_DELETE
				.getImageDescriptor());
		mediaDeleteToolbar
				.setDisabledImageDescriptor(Icons.TOOLBAR_MEDIA_DELETE
						.getDisabledImageDescriptor());
		register(mediaDelete);
		register(mediaDeleteToolbar);

		// Export project
		projectExport = new ProjectExportAction(window);
		projectExportToolbar = new ProjectExportAction(window);
		projectExportToolbar.setImageDescriptor(Icons.TOOLBAR_PROJECT_EXPORT
				.getImageDescriptor());
		projectExportToolbar
				.setDisabledImageDescriptor(Icons.TOOLBAR_PROJECT_EXPORT
						.getDisabledImageDescriptor());
		projectExportToolbar.setText(org.iviPro.actions.nondestructive.Messages.ExportProject_ExportProject_Toolbar);
		register(projectExportToolbar);
		register(projectExport);

		// NEW Export project
		projectExportWizard = new ExportAction(window);
		projectExportWizardToolbar = new ExportAction(window);
		projectExportWizardToolbar
				.setImageDescriptor(Icons.TOOLBAR_PROJECT_EXPORT
						.getImageDescriptor());
		projectExportWizardToolbar
				.setDisabledImageDescriptor(Icons.TOOLBAR_PROJECT_EXPORT
						.getDisabledImageDescriptor());
		register(projectExportWizardToolbar);
		register(projectExportWizard);

		// Open Scene Editor
		editorScene = new OpenEditorAction(window);
		editorSceneToolbar = new OpenEditorAction(window);
		editorSceneToolbar.setImageDescriptor(Icons.TOOLBAR_EDITOR_SCENE_OPEN
				.getImageDescriptor());
		editorSceneToolbar
				.setDisabledImageDescriptor(Icons.TOOLBAR_EDITOR_SCENE_OPEN
						.getDisabledImageDescriptor());
		register(editorSceneToolbar);
		register(editorScene);

		// Open Scenegraph
		openSceneGraph = new OpenSceneGraphAction(window);
		openSceneGraph.setImageDescriptor(Icons.TOOLBAR_EDITOR_SCENEGRAPH_OPEN
				.getImageDescriptor());
		openSceneGraph
				.setDisabledImageDescriptor(Icons.TOOLBAR_EDITOR_SCENEGRAPH_OPEN
						.getDisabledImageDescriptor());
		openSceneGraph.setText(org.iviPro.actions.nondestructive.Messages.OpenSceneGraphAction_Text_Toolbar);
		register(openSceneGraph);

		openTocToolbar = new OpenTocEditorAction(window);
		openTocToolbar.setImageDescriptor(Icons.TOOLBAR_TOC
				.getImageDescriptor());
		openTocToolbar.setDisabledImageDescriptor(Icons.TOOLBAR_TOC
				.getDisabledImageDescriptor());
		openTocToolbar.setText(org.iviPro.actions.nondestructive.Messages.OpenTocEditorAction_Title_Toolbar);
		register(openTocToolbar);

		projectClose = new ProjectCloseAction(window);
		register(projectClose);

		mediaView = new MediaViewAction(window);
		register(mediaView);

		editorSceneDetection = new SceneDetectionAction(window);
		register(editorSceneDetection);

		projectHandOver = new ProjectHandOverAction(window);
		register(projectHandOver);

		// Graph Validator
		graphValidator = new GraphValidationAction(window);

		graphValidatorToolbar = new GraphValidationAction(window);
		graphValidatorToolbar.setImageDescriptor(Icons.TOOLBAR_SETTINGS
				.getImageDescriptor());
		graphValidatorToolbar
				.setDisabledImageDescriptor(Icons.TOOLBAR_SETTINGS
						.getDisabledImageDescriptor());
		
		
		
		// Projectsettings
		projectSettings = new ProjectEditSettingsAction();

		projectSettingsToolbar = new ProjectEditSettingsAction();
		projectSettingsToolbar.setImageDescriptor(Icons.TOOLBAR_SETTINGS
				.getImageDescriptor());
		projectSettingsToolbar
				.setDisabledImageDescriptor(Icons.TOOLBAR_SETTINGS
						.getDisabledImageDescriptor());
		// register(projectSettingsToolbar);

		openToc = new OpenTocEditorAction(window);

		openGlobalAnnotationEditorAction = new OpenGlobalAnnotationEditorAction(
				window);

		openGlobalAnnoToolbar = new OpenGlobalAnnotationEditorAction(window);
		openGlobalAnnoToolbar.setImageDescriptor(Icons.TOOLBAR_GLOBAL_ANNOS
				.getImageDescriptor());
		openGlobalAnnoToolbar
				.setDisabledImageDescriptor(Icons.TOOLBAR_GLOBAL_ANNOS
						.getDisabledImageDescriptor());

		// annotateScene = new AnnotateSceneAction(window);
		// register(annotateScene);

		undoToolbar = new UndoRedoWrapperAction(window,
				getAction(ActionFactory.UNDO.getId()));
		undoToolbar.setImageDescriptor(Icons.TOOLBAR_UNDO.getImageDescriptor());
		undoToolbar.setDisabledImageDescriptor(Icons.TOOLBAR_UNDO
				.getDisabledImageDescriptor());
		redoToolbar = new UndoRedoWrapperAction(window,
				getAction(ActionFactory.REDO.getId()));
		redoToolbar.setImageDescriptor(Icons.TOOLBAR_REDO.getImageDescriptor());
		redoToolbar.setDisabledImageDescriptor(Icons.TOOLBAR_REDO
				.getDisabledImageDescriptor());
		// Hack um OpenFile aus dem Datei-Menu zu bekommen
		String noOpenFile = "org.eclipse.ui.actionSet.openFiles"; //$NON-NLS-1$

		ActionSetRegistry reg = WorkbenchPlugin.getDefault()
				.getActionSetRegistry();
		IActionSetDescriptor[] actionSets = reg.getActionSets();

		for (int i = 0; i < actionSets.length; i++) {
			if (noOpenFile.equals(actionSets[i].getId())) {

				IExtension ext = actionSets[i].getConfigurationElement()
						.getDeclaringExtension();
				// removes the unwanted action
				reg.removeExtension(ext, new Object[] { actionSets[i] });
			}
		}
	}

	protected void fillMenuBar(IMenuManager menuBar) {

		logger.debug("Creating main menu..."); //$NON-NLS-1$

		// das Menü "File"
		MenuManager fileMenu = new MenuManager(
				Messages.ApplicationActionBarAdvisor_File,
				IWorkbenchActionConstants.M_FILE);
		menuBar.add(fileMenu);
		fileMenu.add(new Separator(IWorkbenchActionConstants.FILE_START));
		fileMenu.add(projectNew);
		fileMenu.add(projectOpen);
		fileMenu.add(projectSave);
		fileMenu.add(projectSaveAs);
		fileMenu.add(mediaLoad);
		fileMenu.add(new Separator());
		fileMenu.add(projectExport);
		fileMenu.add(projectHandOver);
//		fileMenu.add(new Separator());
		fileMenu.add(projectExportWizard);
		fileMenu.add(new Separator());
		fileMenu.add(projectClose);
		fileMenu.add(getAction(ActionFactory.QUIT.getId()));
		fileMenu.add(new Separator(IWorkbenchActionConstants.FILE_END));

		// das Menü "Edit"
		MenuManager editMenu = new MenuManager(
				Messages.ApplicationActionBarAdvisor_Edit,
				IWorkbenchActionConstants.M_EDIT);
		menuBar.add(editMenu);
		editMenu.add(new Separator(IWorkbenchActionConstants.EDIT_START));
		editMenu.add(getAction(ActionFactory.UNDO.getId()));
		editMenu.add(getAction(ActionFactory.REDO.getId()));
		editMenu.add(new Separator());
		editMenu.add(openSceneGraph);
		editMenu.add(getAction(ActionFactory.RESET_PERSPECTIVE.getId()));
		editMenu.add(new Separator(IWorkbenchActionConstants.EDIT_END));

		// Das Projekt-Menü
		MenuManager projectMenu = new MenuManager(
				Messages.ApplicationActionBarAdvisor_Menu_Project);
		menuBar.add(projectMenu);
		projectMenu.add(openGlobalAnnotationEditorAction);
		projectMenu.add(openToc);
		projectMenu.add(projectSettings);

		/*
		 * MenuManager videoMenu = new MenuManager(
		 * Messages.ApplicationActionBarAdvisor_Menu_Media);
		 * menuBar.add(videoMenu);
		 * 
		 * videoMenu.add(mediaDelete); videoMenu.add(mediaView);
		 * videoMenu.add(new Separator()); videoMenu.add(editorScene);
		 * videoMenu.add(editorSceneDetection);
		 */

		// das Menü "Help"
		MenuManager helpMenu = new MenuManager(
				Messages.ApplicationActionBarAdvisor_Help,
				IWorkbenchActionConstants.M_HELP);
		menuBar.add(helpMenu);
		helpMenu.add(new Separator(IWorkbenchActionConstants.HELP_START));
		helpMenu.add(getAction(ActionFactory.HELP_CONTENTS.getId()));
		helpMenu.add(getAction(ActionFactory.HELP_SEARCH.getId()));
		helpMenu.add(getAction(ActionFactory.ABOUT.getId()));
		helpMenu.add(new Separator(IWorkbenchActionConstants.HELP_END));

		// /////////////////////////////////////////////////////////////////////

		MenuManager stefansTestMenu = new MenuManager("Testzone");
		menuBar.add(stefansTestMenu);
	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {

		logger.debug("Creating toolbar..."); //$NON-NLS-1$
		IToolBarManager toolbar = new ToolBarManager(coolBar.getStyle() | SWT.RIGHT);//RIGHT=Texposition
		
		coolBar.add(toolbar);
				
		toolbar.add(projectNewToolbar);
		toolbar.add(projectOpenToolbar);
		toolbar.add(new Separator());
		toolbar.add(undoToolbar);
		toolbar.add(redoToolbar);
		toolbar.add(new Separator());
		toolbar.add(projectSaveToolbar);
		toolbar.add(projectSaveAsToolbar);
		
		
//		ActionContributionItem projectExportToolbarI = new ActionContributionItem(projectExportToolbar);
//		projectExportToolbarI.setMode(ActionContributionItem.MODE_FORCE_TEXT);
//		toolbar.add(projectExportToolbarI);
		
		ActionContributionItem projectExportWizardToolbarI = new ActionContributionItem(projectExportWizardToolbar);
		projectExportWizardToolbarI.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		toolbar.add(projectExportWizardToolbarI);
		
		toolbar.add(new Separator());
		
		ActionContributionItem mediaLoadToolbarI = new ActionContributionItem(mediaLoadToolbar);
		mediaLoadToolbarI.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		toolbar.add(mediaLoadToolbarI);
		
		ActionContributionItem mediaDeleteToolbarI = new ActionContributionItem(mediaDeleteToolbar);
		mediaDeleteToolbarI.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		toolbar.add(mediaDeleteToolbarI);
				
		toolbar.add(new Separator());
		
		ActionContributionItem openSceneGraphI = new ActionContributionItem(openSceneGraph);
		openSceneGraphI.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		toolbar.add(openSceneGraphI);
		
		ActionContributionItem openTocToolbarI = new ActionContributionItem(openTocToolbar);
		openTocToolbarI.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		toolbar.add(openTocToolbarI);
		
		toolbar.add(new Separator());
		
		ActionContributionItem openGlobalAnnoToolbarI = new ActionContributionItem(openGlobalAnnoToolbar);
		openGlobalAnnoToolbarI.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		toolbar.add(openGlobalAnnoToolbarI);
		
		ActionContributionItem projectSettingsToolbarI = new ActionContributionItem(projectSettingsToolbar);
		projectSettingsToolbarI.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		toolbar.add(projectSettingsToolbarI);
		
		ActionContributionItem projectGraphValidatorToolbarI = new ActionContributionItem(graphValidatorToolbar);
		projectGraphValidatorToolbarI.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		toolbar.add(projectGraphValidatorToolbarI);
	}
	

	/*public class LabelControlContribution extends ControlContribution {
		String text;
		
		protected LabelControlContribution(String id, String text) {
			super(id);
			this.text = text;
		}

		@Override
		protected Control createControl(Composite parent) {
			final Label b = new Label(parent, SWT.LEFT);
				b.setText(text);		b.setAlignment(SWT.CENTER);	
				Font font = new Font(Display.getCurrent(), "", 8, SWT.None);
				b.setFont(font);
			return b;
		}
	}*/

}


