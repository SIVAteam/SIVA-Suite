package org.iviPro.editors.scenegraph.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.iviPro.editors.scenegraph.actions.messages"; //$NON-NLS-1$
	public static String NodeEditAction_ContextEntry_Edit;
	public static String NodeEditAction_ContextEntry_Edit_Tooltip;
	public static String SemanticFisheyeAction_DisableFisheye;
	public static String SemanticFisheyeAction_EnableFisheye;
	public static String SemanticZoomInAction_SemanticZoomIn;
	public static String SemanticZoomOutAction_SemanticZoomOut;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
