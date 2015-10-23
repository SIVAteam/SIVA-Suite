package org.iviPro.newExport.descriptor.xml.objects;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.INodeAnnotationAction;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.graph.NodeResume;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.resources.Scene;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.iviPro.newExport.descriptor.xml.IdManager.LabelType;
import org.iviPro.newExport.descriptor.xml.resources.IXMLResourceExporter;
import org.iviPro.newExport.descriptor.xml.resources.ResourceExporterFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Exporter-Klasse die einen Szenen-Knoten in ein Siva-Player XML-Dokument
 * exportiert.
 * 
 * @author dellwo
 * 
 */
public class XMLExporterNodeScene extends IXMLExporter {
	
	private NodeScene nodeScene;

	XMLExporterNodeScene(NodeScene exportObj) {
		super(exportObj);
		nodeScene = exportObj;
	}

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(XMLExporterNodeScene.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.iviPro.export.xml.objects.IAbstractXMLExporter#exportObjectImpl(org
	 * .iviPro.model.IAbstractBean, org.w3c.dom.Document,
	 * org.iviPro.export.xml.IdManager, org.iviPro.model.Project)
	 */
	@Override
	protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
			IdManager idManager, Project project,
			Set<Object> alreadyExported) throws ExportException {

		// IDs der verschiedenen Elemente im XML-Dokument generieren
		String sceneID = idManager.getID(nodeScene);
		String videoID = idManager.getID(nodeScene.getScene());

		// Export scene resource
		IXMLResourceExporter exporter = ResourceExporterFactory
				.createExporter(nodeScene.getScene());
		exporter.exportResource(doc, idManager, alreadyExported);
		
		exporter = ResourceExporterFactory
				.createExporter(nodeScene.getScene().getThumbnail());
		exporter.exportResource(doc, idManager, alreadyExported);
		
		// Erstelle Eintrag in <sceneList>
		createSceneListEntry(nodeScene, doc, sceneID, videoID, idManager);

		// Erstelle Eintrag in <actions>
		createActionsEntry(nodeScene, doc, idManager);

		// Erstelle Eintrag in <index>
		createIndexKeywords(nodeScene, sceneID, doc, idManager);
		
		// Verarbeite alle normalen Annotationen
		exportAnnotations(nodeScene, doc, idManager, project, alreadyExported);

		// Verarbeitet alle Nachfolge-Knoten
		exportSuccessors(nodeScene, doc, idManager, project, alreadyExported);
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
			String sceneID, String videoID, IdManager idManager)
			throws ExportException {

		// <scene> Tag fuer Szenen-Liste
		Element scene = doc.createElement(TAG_SCENE);
		scene.setAttribute(ATTR_SCENE_ID, sceneID);
		scene.setIdAttribute(ATTR_SCENE_ID, true);
		scene.setAttribute(ATTR_REF_RES_ID, videoID);
		scene.setAttribute(ATTR_SCENE_NAME, nodeScene.getTitle());
		scene.setAttribute(ATTR_REF_RES_THUMBNAIL, 
				idManager.getID(nodeScene.getScene().getThumbnail()));
		
		String labelID = createLabel(nodeScene, doc, idManager, 
				nodeScene.getTitles(), LabelType.TITLE);
		scene.setAttribute(ATTR_REFresIDname, labelID);
		

		// Position der Szene errechnen
		Graph graph = nodeScene.getGraph();
		List<IGraphNode> allSceneNodes = graph.searchNodes(NodeScene.class);
		double maxX = Integer.MIN_VALUE;
		double maxY = Integer.MIN_VALUE;
		for (IGraphNode node : allSceneNodes) {
			maxX = Math.max(node.getPosition().x, maxX);
			maxY = Math.max(node.getPosition().y, maxY);
		}
		double xPos = nodeScene.getPosition().x / maxX;
		double yPos = nodeScene.getPosition().y / maxY;
		scene.setAttribute(ATTR_SCENE_XPOS, String.valueOf(xPos));
		scene.setAttribute(ATTR_SCENE_YPOS, String.valueOf(yPos));

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
			IdManager idManager) throws ExportException {
		// Entsprechenden <loadVideoScene> Eintrag in <actions>-Liste erstellen
		Element loadVideoAction = doc.createElement(TAG_LOADVIDEOSCENE);
		loadVideoAction.setAttribute(ATTR_ACTIONID,
				idManager.getActionID(nodeScene));
		loadVideoAction.setAttribute(ATTR_REF_SCENEID,
				idManager.getID(nodeScene));
		
		// Add resume button elements if resume button is connected
		List<IGraphNode> resumeList = nodeScene.getChildren(NodeResume.class);
		if (!resumeList.isEmpty()) {
			NodeResume resume = (NodeResume)resumeList.get(0);
			if (resume.useTimeout()) {
				Date timeout = new Date(resume.getTimeout() * 1000);
				SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss"); //$NON-NLS-1$
				loadVideoAction.setAttribute(ATTR_TIMEOUT,
						"00:" + dateFormat.format(timeout));//$NON-NLS-1$
			}
			Element forwardButton = doc.createElement(TAG_FOWARDBUTTON);	
			forwardButton.setAttribute(ATTR_FORWARD_ID, 
					idManager.getID(resume));
			String titleLabelId = createLabel(resume, doc, idManager, 
					resume.getTitles(), LabelType.TITLE);
			forwardButton.setAttribute(ATTR_REF_RES_ID, titleLabelId);
			loadVideoAction.appendChild(forwardButton);
		}
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
	private String getEndActionID(NodeScene nodeScene, IdManager idManager)
			throws ExportException {
		List<IGraphNode> children = nodeScene.getChildren();
		for (IGraphNode child : children) {
			if (child instanceof NodeResume) {
				return idManager.getActionID(child.getChildren().get(0));
			} else if (child instanceof NodeScene
					|| (child instanceof INodeAnnotationAction
							&& !(child instanceof NodeMark))) {
				return idManager.getActionID(child);
			} 
		}
		throw new ExportException(String.format(
				"Scene node '%s' has no end action.", nodeScene.getTitle())); //$NON-NLS-1$
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
			IdManager idManager, Project project,
			Set<Object> alreadyExported) throws ExportException {

		// Liste mit allen potentiellen Nachfolge-Knoten erstellen.
		List<IGraphNode> children = new ArrayList<IGraphNode>();
		children.addAll(nodeScene.getChildren(INodeAnnotationAction.class));
		children.addAll(nodeScene.getChildren(NodeScene.class));
		
		for (IGraphNode child : children) {
			if (child instanceof NodeResume) {
				child = child.getChildren().get(0);
			}
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
			IdManager idManager, Project project,
			Set<Object> alreadyExported) throws ExportException {
		Collection<IGraphNode> children = nodeScene
				.getChildren(INodeAnnotationLeaf.class);
		for (IGraphNode child : children) {

			IXMLExporter exporter = ExporterFactory.createExporter(child);
			exporter.exportObject(doc, idManager, project, alreadyExported);
		}
	}

	private void createIndexKeywords(NodeScene nodeScene, String sceneID,
			Document doc, IdManager idManager) throws ExportException {

		Element index = getIndex(doc);
		NodeList keywordsIndex = index.getElementsByTagName(TAG_INDEX_KEYWORD);

		Scene scene = nodeScene.getScene();
		// description in keyword not desired at the moment
		//String description = (scene.getDescription() != null ? scene.getDescription() : "");
		String keywords = scene.getKeywords() + " ";//+ description;
		String[] keywordList = keywords.split(" "); //$NON-NLS-1$
		for (String k : keywordList) {
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
}
