package org.iviPro.editors.scenegraph.figures;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.editors.scenegraph.figures.messages"; //$NON-NLS-1$
	public static String FigureNodeSemanticAdditionAnnoDetails_NoAnnotations;
	public static String FigureNodeSemanticAdditionSceneInfo_Duration;
	public static String FigureNodeSemanticAdditionSceneInfo_Source;
	public static String FigureNodeStart_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
