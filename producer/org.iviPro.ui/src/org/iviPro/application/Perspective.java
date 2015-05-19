package org.iviPro.application;

import org.apache.log4j.Logger;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.iviPro.views.informationview.InformationView;
import org.iviPro.views.mediarepository.MediaRepository;
import org.iviPro.views.miniaturegraph.MiniatureGraph;
import org.iviPro.views.scenerepository.SceneRepository;

/**
 * Hier werden die einzelnen Views und Editoren angeordnet.
 * 
 * @author Florian Stegmaier
 */
public class Perspective implements IPerspectiveFactory {
	private static Logger logger = Logger.getLogger(Perspective.class);
	public static final String ID = "org.iviPro.perspective"; //$NON-NLS-1$

	public static final String FOLDER_TOPRIGHT_ID = "FOLDER_TOPRIGHT_ID"; //$NON-NLS-1$
	public static final String FOLDER_BOTTOMRIGHT_ID = "FOLDER_BOTTOMRIGHT_ID"; //$NON-NLS-1$
	public static IPageLayout layout;

	public void createInitialLayout(IPageLayout layout) {
		logger.debug("Creating layout of initial perspective."); //$NON-NLS-1$
		Perspective.layout = layout;
		layout.setEditorAreaVisible(true);

		IFolderLayout topRightFolder = layout.createFolder(FOLDER_TOPRIGHT_ID,
				IPageLayout.RIGHT, 0.8f, layout.getEditorArea());
		topRightFolder.addView(MediaRepository.ID);
		topRightFolder.addView(SceneRepository.ID);		
		
		IFolderLayout bottomRightFolder = layout.createFolder(FOLDER_BOTTOMRIGHT_ID,
				IPageLayout.BOTTOM, 0.5f, FOLDER_TOPRIGHT_ID);
		bottomRightFolder.addView(InformationView.ID);
		bottomRightFolder.addView(MiniatureGraph.ID);
	}

	public static IPageLayout getLayout() {
		return layout;
	}
}
