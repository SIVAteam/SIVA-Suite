package org.iviPro.export.xml.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.log4j.Logger;
import org.iviPro.export.ExportException;
import org.iviPro.export.ExportType;
import org.iviPro.export.Exporter;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedFile;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.Scene;
import org.iviPro.model.Video;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.INodeAnnotationAction;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeScene;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Exporter-Klasse die einen Szenen-Knoten in ein Siva-Player XML-Dokument
 * exportiert.
 * 
 * @author dellwo
 * 
 */
public class XMLExporterNodeScene extends IXMLExporter {

	XMLExporterNodeScene(IAbstractBean exportObj) {
		super(exportObj);
	}

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(XMLExporterNodeScene.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.iviPro.export.xml.objects.IAbstractXMLExporter#exportObjectImpl(org
	 * .iviPro.model.IAbstractBean, org.w3c.dom.Document,
	 * org.iviPro.export.xml.IDManager, org.iviPro.model.Project)
	 */
	@Override
	protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
			IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported) throws ExportException {

		// IDs der verschiedenen Elemente im XML-Dokument generieren
		NodeScene nodeScene = (NodeScene) exportObj;
		String sceneID = idManager.getID(nodeScene);
		String videoID = idManager.getID(nodeScene.getScene());

		if (isAlreadyExported(doc, nodeScene, idManager)) {
			return;
		}

		// Erstelle Eintrag in <sceneList>
		createSceneListEntry(nodeScene, doc, sceneID, videoID, idManager);

		// Erstelle Eintrag in <actions>
		createActionsEntry(nodeScene, doc, idManager);

		// Erstelle Eintrag in <ressources>
		createVideoRessourceEntry(nodeScene, doc, videoID, idManager, project);
		
		// Erstelle Eintrag in <index>
		createIndexKeywords(nodeScene, sceneID, doc, idManager);

		// Verarbeite alle normalen Annotationen
		exportAnnotations(nodeScene, doc, idManager, project, alreadyExported);

		// Verarbeitet alle Nachfolge-Knoten
		exportSuccessors(nodeScene, doc, idManager, project, alreadyExported);
	}

	private boolean isAlreadyExported(Document doc, NodeScene nodeScene,
			IDManager idManager) throws ExportException {
		Element sceneList = getSceneList(doc);
		NodeList existingScenes = sceneList.getChildNodes();
		for (int i = 0; i < existingScenes.getLength(); i++) {
			Node item = existingScenes.item(i);
			if (item instanceof Element && item.getNodeName().equals(TAG_SCENE)) {
				Element elem = (Element) item;
				if (idManager.getID(nodeScene).equals(
						elem.getAttribute(ATTR_SCENE_ID))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isRessourceExported(Document doc, String ressourceTag,
			String ressourceID) throws ExportException {
		Element ressourceList = getRessources(doc);
		NodeList existingRessources = ressourceList.getChildNodes();
		for (int i = 0; i < existingRessources.getLength(); i++) {
			Node item = existingRessources.item(i);
			if (item instanceof Element
					&& item.getNodeName().equals(ressourceTag)) {
				Element elem = (Element) item;
				if (ressourceID.equals(elem.getAttribute(ATTR_RES_ID))) {
					return true;
				}
			}
		}
		return false;

	}

	/**
	 * Erstellt einen Eintrag in die Szenen-Liste fuer diesen Szenen-Knoten<br>
	 * <br>
	 * Beispiel:<br>
	 * <code><br>
	 * &lt;scene sceneID="NodeScene_1" REFresID="res-video-Scene_1"&gt;<br>
	 * . &lt;storyBoard REFactionIDend="end-siva"&gt;<br>
	 * . &lt;/storyBoard&gt;<br>
	 * &lt;/scene&gt;><br>
	 * </code>
	 * 
	 * 
	 * @param nodeScene
	 *            Szenen-Knoten fuer den der Eintrag erstellt wird
	 * @param doc
	 *            XML-Dokument in dem der Eintrag gemacht werden soll
	 * @param sceneID
	 *            Die Szenen-ID im XML-Dokument
	 * @param videoID
	 *            Die Video-ID im XML-Dokument
	 * @param idManager
	 *            Der ID-Manager zum Generieren der IDs
	 * 
	 * @throws ExportException
	 *             Falls beim Export in das XML-Dokument ein Fehler auftritt.
	 */
	private void createSceneListEntry(NodeScene nodeScene, Document doc,
			String sceneID, String videoID, IDManager idManager)
			throws ExportException {

		// <scene> Tag fuer Szenen-Liste
		Element scene = doc.createElement(TAG_SCENE);
		scene.setAttribute(ATTR_SCENE_ID, sceneID);
		scene.setIdAttribute(ATTR_SCENE_ID, true);
		scene.setAttribute(ATTR_REF_RES_ID, videoID);
		scene.setAttribute(ATTR_SCENE_NAME, nodeScene.getTitle());
		
		scene.setAttribute(ATTR_REFresIDname, idManager.getTitleLabelID(nodeScene));
		createTitleLabels(nodeScene, doc, idManager);		

		// Position der Szene errechnen
		Graph graph = nodeScene.getGraph();
		List<IGraphNode> allSceneNodes = graph.searchNodes(NodeScene.class,
				false);
		double maxX = Integer.MIN_VALUE;
		double maxY = Integer.MIN_VALUE;
		for (IGraphNode node : allSceneNodes) {
			maxX = Math.max(node.getPosition().x, maxX);
			maxY = Math.max(node.getPosition().y, maxY);
		}
		double xPos = nodeScene.getPosition().x / maxX;
		double yPos = nodeScene.getPosition().y / maxY;
		scene.setAttribute(ATTR_SCENE_XPOS, "" + xPos); //$NON-NLS-1$
		scene.setAttribute(ATTR_SCENE_YPOS, "" + yPos); //$NON-NLS-1$

		// <storyBoard> mit Actions
		Element storyboard = doc.createElement(TAG_STORYBOARD);
		String endActionID = getEndActionID(nodeScene, idManager);
		storyboard.setAttribute(ATTR_REF_ACTION_ID_END, endActionID);
		scene.appendChild(storyboard);

		// Eintrag in Szenen-Liste vornehmen
		Element sceneList = getSceneList(doc);
		sceneList.appendChild(scene);

	}

	/**
	 * Erstellt den Eintrag in der actions-Liste fuer diesen Szenen-Knoten
	 * 
	 * @param nodeScene
	 * @param doc
	 * @param idManager
	 * @throws ExportException
	 */
	private void createActionsEntry(NodeScene nodeScene, Document doc,
			IDManager idManager) throws ExportException {
		// Entsprechenden <loadVideoScene> Eintrag in <actions>-Liste erstellen
		Element loadVideoAction = doc.createElement(TAG_LOADVIDEOSCENE);
		loadVideoAction.setAttribute(ATTR_ACTIONID, idManager
				.getActionID(nodeScene));
		loadVideoAction.setAttribute(ATTR_REF_SCENEID, idManager
				.getID(nodeScene));
		Element actions = getActions(doc);
		actions.appendChild(loadVideoAction);

	}

	/**
	 * Gibt die ID der end-Action des gegebenen Szenen-Knotens an
	 * 
	 * @param nodeScene
	 * @param idManager
	 * @return
	 * @throws ExportException
	 */
	private String getEndActionID(NodeScene nodeScene, IDManager idManager)
			throws ExportException {
		List<IGraphNode> children = nodeScene.getChildren();
		for (IGraphNode child : children) {
			if (child instanceof NodeScene
					|| child instanceof INodeAnnotationAction) {
				return idManager.getActionID(child);
			}
		}
		throw new ExportException("Scene node '" + nodeScene //$NON-NLS-1$
				+ "' has no end action."); //$NON-NLS-1$
	}

	/**
	 * Erstellt einen VideoStream-Eintrag in der Ressourcen-Liste fuer diesen
	 * Szenen-Knoten.<br>
	 * <br>
	 * Beispiel:<br>
	 * <code><br>
	 * &lt;videoStream resID="res-video-Scene_1"&gt;<br>
	 * . &lt;content href="Scene_1.flv" langCode="de-de"/&gt;<br>
	 * &lt;/videoStream&gt;<br>
	 * </code>
	 * 
	 * 
	 * @param nodeScene
	 *            Szenen-Knoten fuer den der Eintrag erstellt wird
	 * @param doc
	 *            XML-Dokument in dem der Eintrag gemacht werden soll
	 * @param videoID
	 *            Die Video-ID im XML-Dokument
	 * @param videoFilename
	 *            Der Dateiname der Video-Ressource
	 * @throws ExportException
	 *             Falls beim Export in das XML-Dokument ein Fehler auftritt.
	 */
	private void createVideoRessourceEntry(NodeScene nodeScene, Document doc,
			String videoID, IDManager idManager, Project project)
			throws ExportException {

		if (isRessourceExported(doc, TAG_VIDEOSTREAM, videoID)) {
			return;
		}

		Element videoStream = doc.createElement(TAG_VIDEOSTREAM);
		videoStream.setAttribute(ATTR_RES_ID, videoID);
		videoStream.setIdAttribute(ATTR_RES_ID, true);
		
		ExportType type = idManager.getExportType();
		if (type != ExportType.HTML5) {
			videoStream.setAttribute(ATTR_RES_CONTAINERFORMAT, idManager
					.getExportType().getFFmpegVideoContainerFormat());
			videoStream.setAttribute(ATTR_RES_VIDEOCODEC, idManager.getExportType()
					.getFFmpegVideoCodec());
			videoStream.setAttribute(ATTR_RES_AUDIOCODEC, idManager.getExportType()
					.getFFmpegAudioCodec());
		}
		

		Scene scene = nodeScene.getScene();
		Video video = scene.getVideo();
		for (Locale language : project.getLanguages()) {
			String curLangCode = LocalizedString.getSivaLangcode(language);
			LocalizedFile file = video.getFile(language);
			
			String filename = Exporter.EXPORT_SUBDIR_VIDEOS + "/" //$NON-NLS-1$
					+ idManager.getFilename(scene, file.getLanguage());
			System.out.println("Original filename: " + filename);
			if (type == ExportType.HTML5) {
				filename = filename.substring(0, filename.length() - 1);
				System.out.println("XML Filename: " + filename);
			}
			Element content = doc.createElement(TAG_CONTENT);
			content.setAttribute(ATTR_HREF, filename);
			content.setAttribute(ATTR_REF_LANGCODE, curLangCode);
			videoStream.appendChild(content);
		}

		Element ressources = getRessources(doc);
		ressources.appendChild(videoStream);
	}

	/**
	 * Exportiert die Nachfolge-Knoten eines Szenen-Knotens. Nachfolge-Knoten
	 * sind andere Knoten vom Typ NodeScene, NodeSceneSequence oder vom Typ
	 * INodeAnnotationAction.
	 * 
	 * @param nodeScene
	 *            Der Szenen-Knoten dessen Nachfolge-Knoten exportiert werden
	 *            sollen.
	 * @param doc
	 *            Das Dokument in das exportiert weden soll.
	 * @param idManager
	 *            Der ID-Manager zur Erzeugung der XML-Element IDs
	 * @param project
	 *            Das Projekt aus dem der Szenen-Knoten stammt.
	 * @throws ExportException
	 *             Falls beim Export in das XML-Dokument ein Fehler auftritt.
	 */
	private void exportSuccessors(NodeScene nodeScene, Document doc,
			IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported) throws ExportException {

		// Liste mit allen potentiellen Nachfolge-Knoten erstellen.
		List<IGraphNode> children = new ArrayList<IGraphNode>();
		children.addAll(nodeScene.getChildren(INodeAnnotationAction.class));
		children.addAll(nodeScene.getChildren(NodeScene.class));

		for (IGraphNode child : children) {
			IXMLExporter exporter = ExporterFactory.createExporter(child);
			exporter.exportObject(doc, idManager, project, alreadyExported);

		}
	}

	/**
	 * Exportiert alle "normalen" Annotation (Text, Audio, Video, ...) eines
	 * Szenen-Knotens. Dies sind im speziellen alle Annotationen die von
	 * INodeAnnotationLeaf abgeleitet sind.
	 * 
	 * @param nodeScene
	 *            Der Szenen-Knoten dessen "normale" Annotationen exportiert
	 *            werden sollen.
	 * @param doc
	 *            Das Dokument in das exportiert weden soll.
	 * @param idManager
	 *            Der ID-Manager zur Erzeugung der XML-Element IDs
	 * @param project
	 *            Das Projekt aus dem der Szenen-Knoten stammt.
	 * @throws ExportException
	 *             Falls beim Export in das XML-Dokument ein Fehler auftritt.
	 */
	private void exportAnnotations(NodeScene nodeScene, Document doc,
			IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported) throws ExportException {
		Collection<IGraphNode> children = nodeScene
				.getChildren(INodeAnnotationLeaf.class);
		for (IGraphNode child : children) {

			IXMLExporter exporter = ExporterFactory.createExporter(child);
			exporter.exportObject(doc, idManager, project, alreadyExported);
		}
	}
	
	private void createIndexKeywords(NodeScene nodeScene, String sceneID,
			Document doc, IDManager idManager) throws ExportException {

		Element index = getIndex(doc);
		NodeList keywordsIndex = index.getElementsByTagName(TAG_INDEX_KEYWORD);

		Scene scene = nodeScene.getScene();
		String[] keywords = scene.getKeywords().split(","); //$NON-NLS-1$
		for (String k : keywords) {
			k = k.trim();
			if (!k.equals("")) { //$NON-NLS-1$
				Element keyword = null;
				for (int i = 0; i < keywordsIndex.getLength(); i++) {
					Element kNode = (Element) keywordsIndex.item(i);
					if (kNode.getAttribute(ATTR_KEYWORD_WORD).equals(k)) {
						keyword = kNode;
						break;
					}
				}
				if (keyword == null) {
					keyword = doc.createElement(TAG_INDEX_KEYWORD);
				}
				keyword.setAttribute(ATTR_KEYWORD_WORD, k);

				Element sceneKeyword = doc.createElement(TAG_SCENE);
				sceneKeyword.setAttribute(ATTR_REF_SCENEID, sceneID);
				keyword.appendChild(sceneKeyword);
				index.appendChild(keyword);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.iviPro.export.xml.objects.IAbstractXMLExporter#getExportObjectType()
	 */
	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeScene.class;
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}

}
