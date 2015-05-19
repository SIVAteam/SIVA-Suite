package org.iviPro.editors.scenegraph;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.editors.scenegraph.messages"; //$NON-NLS-1$
	public static String ModelObjectFactory_NodeQuiz_DefaultTitle;
	public static String ModelObjectFactory_NodeQuizControl_DefaultTitle;
	public static String ModelObjectFactory_NodeSelection_DefaultTitle;
	public static String ModelObjectFactory_NodeSelectionControl_DefaultTitle;
	public static String SceneGraphEditor_geometricZoomIn;
	public static String SceneGraphEditor_geometricZoomOut;
	public static String SceneGraphEditorPaletteFactory_Entry_Fork_Description;
	public static String SceneGraphEditorPaletteFactory_Entry_Fork_Title;
	public static String SceneGraphEditorPaletteFactory_Entry_ForkAlternative_Description;
	public static String SceneGraphEditorPaletteFactory_Entry_ForkAlternative_Title;
	public static String SceneGraphEditorPaletteFactory_Entry_Quiz_Description;
	public static String SceneGraphEditorPaletteFactory_Entry_Quiz_Title;
	public static String SceneGraphEditorPaletteFactory_Entry_QuizAlternative_Description;
	public static String SceneGraphEditorPaletteFactory_Entry_QuizAlternative_Title;
	public static String SceneGraphEditorPaletteFactory_PaletteName;
	public static String SceneGraphEditorPaletteFactory_Section_Items;
	public static String SceneGraphEditorPaletteFactory_Section_Tools;
	public static String SceneGraphEditorPaletteFactory_Tool_Connection_Description;
	public static String SceneGraphEditorPaletteFactory_Tool_Connection_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
