package org.iviPro.export.smil.objects;

import java.util.Locale;
import java.util.Set;

import org.iviPro.export.ExportException;
import org.iviPro.export.Exporter;
import org.iviPro.export.smil.VideoMovingInformation;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedFile;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.Scene;
import org.iviPro.model.Video;
import org.iviPro.model.graph.NodeScene;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SMILExporterNodeScene extends SMILExporter {

	SMILExporterNodeScene(IAbstractBean exportObj) {
		super(exportObj);
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeScene.class;
	}

	@Override
	protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
			IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported, Element parent) throws ExportException {

		NodeScene nodeScene = (NodeScene) exportObj;
		String sceneID = idManager.getID(nodeScene);
		String videoID = idManager.getID(nodeScene.getScene());

		// Create the "root"-node for the scene, which is a <par> node, meaning
		// that all of its children are played at the same time
		Element sceneElement = doc.createElement(TAG_PAR);
		sceneElement.setAttribute(ATTR_ID, sceneID);

		// Create <switch> element for the different possible languages of the
		// video
		Element languageSwitch = doc.createElement(TAG_SWITCH);
		sceneElement.appendChild(languageSwitch);

		Scene scene = nodeScene.getScene();
		Video video = scene.getVideo();
		
		// Video sizing, if it will be cropped by the SMIL-Player, because of the fit="meet"-attribute
		VideoMovingInformation movingInfo = calculateVideoMoving(SMILExporterProject.mainRegionWidth, SMILExporterProject.mainRegionHeight, (int) video.getDimension().getWidth(), (int) video.getDimension().getHeight());

		// Create a <video> element for every language with its corresponding
		// video
		for (Locale language : project.getLanguages()) {
			Element videoElement = exportLanguageScene(video, videoID, scene,
					idManager, doc, language, false, movingInfo);
			languageSwitch.appendChild(videoElement);
		}
		
		// Append a child with the language of the current project as default
		// video. This is done first, so the default case will be the last one
		// in the switch-element
		Element defVideoElement = exportLanguageScene(video, videoID, scene,
				idManager, doc, project.getDefaultLanguage(), true, movingInfo);
		languageSwitch.appendChild(defVideoElement);

		if(parent == null) {
			// Append the par-node for this exported scene to the SMIL-document.
			getMainSeqElement(doc).appendChild(sceneElement);			
		} else {
			// parent is not null, so the scene is inside of a path following a fork, so it has to be appended at a special node
			parent.appendChild(sceneElement);
		}

		// Export all of the annotations for this scene
		exportAnnotations(nodeScene, doc, idManager, project, alreadyExported, sceneElement);
		
		// Export all markers for this scene
		exportMarkers(nodeScene, doc, idManager, project, alreadyExported, sceneElement);

		if(parent == null) {
			// Export all successors of this scene, only if not inside a fork path
			exportSuccessors(nodeScene, doc, idManager, project, alreadyExported);			
		}
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}

	/**
	 * Method to create a video element according to a given language. There's
	 * also the possibility to create the default video, which will have no
	 * systemLanguage attribute.
	 * 
	 * @param video
	 *            The video corresponding to the scene.
	 * @param videoID
	 *            The id of the video.
	 * @param scene
	 *            The scene that is to be exported.
	 * @param idManager
	 *            The IDManager of the current project.
	 * @param doc
	 *            The file for the SMIL code.
	 * @param language
	 *            The language for the exported scene.
	 * @param defLanguage
	 *            True, if the video element for the default language is to be
	 *            exported.
	 * @return A video element with the corresponding language attribute.
	 * @throws ExportException 
	 */
	private Element exportLanguageScene(Video video, String videoID,
			Scene scene, IDManager idManager, Document doc, Locale language,
			boolean defLanguage, VideoMovingInformation movingInfo) throws ExportException {
		String curLangCode = LocalizedString.getSivaLangcode(language);
		LocalizedFile file = video.getFile(language);
		String filename = "../" + Exporter.EXPORT_SUBDIR_VIDEOS + "/"
				+ idManager.getFilename(scene, file.getLanguage());
		Element videoElement = doc.createElement(TAG_VIDEO);
		String videoLanguageID = videoID;
		if (defLanguage) {
			videoLanguageID += "_def";
		} else {			
			videoElement.setAttribute(ATTR_SYSTEMLANGUAGE, curLangCode);
			videoLanguageID += "_" + curLangCode;
		}
		videoElement.setAttribute(ATTR_ID, videoLanguageID);
		videoElement.setAttribute(ATTR_REGION, VAL_MAIN_REGION_ID);
		videoElement.setAttribute(ATTR_SOURCE, filename);
		videoElement.setAttribute(ATTR_BEGIN, VAL_ZERO + ADDITION_SECONDS);
		
		realizeVideoMoving(videoElement, movingInfo);

		return videoElement;
	}

}
