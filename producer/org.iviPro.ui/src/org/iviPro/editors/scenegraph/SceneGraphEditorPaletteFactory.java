package org.iviPro.editors.scenegraph;

import org.eclipse.draw2d.Graphics;
import org.eclipse.gef.internal.GEFMessages;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteToolbar;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.iviPro.model.graph.NodeCondSelection;
import org.iviPro.model.graph.NodeCondSelectionControl;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.graph.NodeQuizControl;
import org.iviPro.model.graph.NodeRandomSelection;
import org.iviPro.model.graph.NodeResume;
import org.iviPro.model.graph.NodeSelection;
import org.iviPro.model.graph.NodeSelectionControl;
import org.iviPro.theme.Icons;

/**
 * Diese Klasse ist fuer die Erstellung der GEF-Palette mit den Tools fuer den
 * Graphen zustaendig.
 * 
 * @see #createPalette()
 * @author dellwo
 */
final class SceneGraphEditorPaletteFactory {

	// /** Preference ID used to persist the palette location. */
	// private static final String PALETTE_DOCK_LOCATION =
	// "SceneGraphEditorPaletteFactory.Location";
	// /** Preference ID used to persist the palette size. */
	// private static final String PALETTE_SIZE =
	// "SceneGraphEditorPaletteFactory.Size";
	// /** Preference ID used to persist the flyout palette's state. */
	// private static final String PALETTE_STATE =
	// "SceneGraphEditorPaletteFactory.State";

	/** Create the "items" drawer. */
	private static PaletteContainer createItemsDrawer() {
		PaletteDrawer componentsDrawer = new PaletteDrawer(
				Messages.SceneGraphEditorPaletteFactory_Section_Items);
		CombinedTemplateCreationEntry component;

		// NodeSelection
		component = new CombinedTemplateCreationEntry(
				Messages.SceneGraphEditorPaletteFactory_Entry_Fork_Title,
				Messages.SceneGraphEditorPaletteFactory_Entry_Fork_Description,
				NodeSelection.class,
				new ModelObjectFactory(NodeSelection.class),
				Icons.GRAPH_TOOL_SELECTION.getImageDescriptor(),
				Icons.GRAPH_TOOL_SELECTION.getImageDescriptor());
		componentsDrawer.add(component);

		// NodeSelectionControl
		component = new CombinedTemplateCreationEntry(
				Messages.SceneGraphEditorPaletteFactory_Entry_ForkAlternative_Title,
				Messages.SceneGraphEditorPaletteFactory_Entry_ForkAlternative_Description,
				NodeSelectionControl.class, new ModelObjectFactory(
						NodeSelectionControl.class),
						Icons.GRAPH_TOOL_SELECTION_ALTERNATIVE.getImageDescriptor(),
						Icons.GRAPH_TOOL_SELECTION_ALTERNATIVE.getImageDescriptor());
		componentsDrawer.add(component);
		
		// NodeCondSelection
		component = new CombinedTemplateCreationEntry(
				Messages.SceneGraphEditorPaletteFactory_Entry_Conditional_Title,
				Messages.SceneGraphEditorPaletteFactory_Entry_Conditional_Description,
						NodeCondSelection.class,
						new ModelObjectFactory(NodeCondSelection.class),
						Icons.GRAPH_TOOL_COND_SELECTION.getImageDescriptor(),
						Icons.GRAPH_TOOL_COND_SELECTION.getImageDescriptor());
		componentsDrawer.add(component);

		// NodeCondSelectionControl
		component = new CombinedTemplateCreationEntry(
				Messages.SceneGraphEditorPaletteFactory_Entry_ConditionalAlternative_Title,
				Messages.SceneGraphEditorPaletteFactory_Entry_ConditionalAlternative_Description,
				NodeCondSelectionControl.class,
				new ModelObjectFactory(NodeCondSelectionControl.class),
				Icons.GRAPH_TOOL_COND_SELECTION_ALTERNATIVE.getImageDescriptor(),
				Icons.GRAPH_TOOL_COND_SELECTION_ALTERNATIVE.getImageDescriptor());
		componentsDrawer.add(component);

		
// Quiz utilities are disabled for now until the Quiz works and export is fixed
		
//		// NodeQuiz
//		component = new CombinedTemplateCreationEntry(
//				Messages.SceneGraphEditorPaletteFactory_Entry_Quiz_Title,
//				Messages.SceneGraphEditorPaletteFactory_Entry_Quiz_Description,
//				NodeQuiz.class,
//				new ModelObjectFactory(NodeQuiz.class),
//				Icons.GRAPH_TOOL_QUIZ.getImageDescriptor(),
//				Icons.GRAPH_TOOL_QUIZ.getImageDescriptor());
//		componentsDrawer.add(component);

//		// NodeQuizControl
//		component = new CombinedTemplateCreationEntry(
//				Messages.SceneGraphEditorPaletteFactory_Entry_QuizAlternative_Title,
//				Messages.SceneGraphEditorPaletteFactory_Entry_QuizAlternative_Description,
//				NodeQuizControl.class,
//				new ModelObjectFactory(NodeQuizControl.class),
//				Icons.GRAPH_TOOL_QUIZ_ALTERNATIVE.getImageDescriptor(),
//				Icons.GRAPH_TOOL_QUIZ_ALTERNATIVE.getImageDescriptor());
//		componentsDrawer.add(component);
				
		// NodeRandomSelection
		component = new CombinedTemplateCreationEntry(
				Messages.SceneGraphEditorPaletteFactory_Entry_Random_Title,
				Messages.SceneGraphEditorPaletteFactory_Entry_Random_Description,
				NodeRandomSelection.class,
				new ModelObjectFactory(NodeRandomSelection.class),
				Icons.GRAPH_TOOL_RANDOMSELECTION.getImageDescriptor(),
				Icons.GRAPH_TOOL_RANDOMSELECTION.getImageDescriptor());
		componentsDrawer.add(component);
		
		// NodeResume		
		component = new CombinedTemplateCreationEntry(
				Messages.SceneGraphEditorPaletteFactory_Entry_Resume_Title,
				Messages.SceneGraphEditorPaletteFactory_Entry_Resume_Description,
				NodeResume.class,
				new ModelObjectFactory(NodeResume.class),
				Icons.GRAPH_TOOL_RESUME.getImageDescriptor(),
				Icons.GRAPH_TOOL_RESUME.getImageDescriptor());
		componentsDrawer.add(component);

		return componentsDrawer;
	}

	/**
	 * Creates the PaletteRoot and adds all palette elements. Use this factory
	 * method to create a new palette for your graphical editor.
	 * 
	 * @return a new PaletteRoot
	 */
	static PaletteRoot createPalette() {
		GEFMessages.Palette_Label = Messages.SceneGraphEditorPaletteFactory_PaletteName; //$NON-NLS-1$
		PaletteRoot palette = new PaletteRoot();
		palette.add(createToolsGroupEdit(palette));
		palette.add(createItemsDrawer());
		return palette;
	}

	/** Create the "Tools" group. */
	private static PaletteContainer createToolsGroupEdit(PaletteRoot palette) {
		PaletteToolbar toolbar = new PaletteToolbar(
				Messages.SceneGraphEditorPaletteFactory_Section_Tools);

		// Add a selection tool to the group
		ToolEntry tool = new PanningSelectionToolEntry();
		toolbar.add(tool);
		palette.setDefaultEntry(tool);

		// Add a marquee tool to the group
		// toolbar.add(new MarqueeToolEntry());

		// Add (solid-line) connection tool
		tool = new ConnectionCreationToolEntry(
				Messages.SceneGraphEditorPaletteFactory_Tool_Connection_Title,
				Messages.SceneGraphEditorPaletteFactory_Tool_Connection_Description,
				new CreationFactory() {
					public Object getNewObject() {
						return null;
					}

					// see NodeEditPart#createEditPolicies()
					// this is abused to transmit the desired line style
					public Object getObjectType() {
						// return Connection.SOLID_CONNECTION;
						return new Integer(Graphics.LINE_SOLID);
					}
				}, Icons.GRAPH_TOOL_ADDCONNECTION.getImageDescriptor(),
				Icons.GRAPH_TOOL_ADDCONNECTION.getImageDescriptor());
		toolbar.add(tool);

		return toolbar;
	}

	/** Utility class. */
	private SceneGraphEditorPaletteFactory() {
		// Utility class
	}

}