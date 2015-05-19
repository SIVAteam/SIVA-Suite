package org.iviPro.export.xml.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.iviPro.export.ExportException;
import org.iviPro.export.ExportType;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.LocalizedFile;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.Scene;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.utils.SivaTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class IXMLExporterNodeAnnotationLeaf extends IXMLExporter {

	IXMLExporterNodeAnnotationLeaf(IAbstractBean exportObj) {
		super(exportObj);
	}

	@Override
	protected final void exportObjectImpl(IAbstractBean exportObj,
			Document doc, IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported) throws ExportException {

		// Zugehoerigen Szenen-Knoten holen
		INodeAnnotationLeaf annotation = (INodeAnnotationLeaf) exportObj;

		// Trigger zu Storyboard fuer die Annotation hinzufuegen.
		NodeScene parentScene = null;
		try {
			parentScene = annotation.getParentScene();
			// Szenen-Annotation => Storyboard-Eintrag erstellen
			Element storyboard = getStoryboard(doc, parentScene, idManager);
			Element trigger = createTrigger(doc, annotation, idManager);
			storyboard.appendChild(trigger);
		} catch (IllegalStateException e) {
			// Globale Annotation => Kein Parent, daher kein Storyboard-Eintrag
			// Project-Ressources Eintrag wird von XMLExporterProject erstellt
		}

		// Vom Trigger referenzierte Action anlegen
		Element actionList = getActions(doc);
		Element action = createAction(doc, annotation, idManager, project);
		actionList.appendChild(action);

		// Ressource fuer die Annotation hinzufuegen
		Element ressources = getRessources(doc);
		List<Element> resourceElements = createRessources(annotation, doc, idManager);
		
		for (Element resElement : resourceElements) {
			// wenn keine Ressource angegeben ist, exportiere nicht 
			if (resElement != null) {
				NodeList nl = ressources.getElementsByTagName(resElement.getTagName());
				boolean export = true;
				String resID = resElement.getAttribute(ATTR_RES_ID);
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);		
					if (n.getAttributes().getNamedItem(ATTR_RES_ID).getNodeValue().equals(resID)) {
						export = false;
						break;
					}
				}
				if (export) {
					ressources.appendChild(resElement);
				}
			}
		}
		
		// Keywords nur wenn parentScene vorhanden, also nicht für globale Annotation
		if (parentScene != null) {
			createIndexKeywords(annotation, doc, idManager, parentScene);
		}
	}	

	/**
	 * Gibt den Tag-Namen des Ressourcen-Elements fuer diese Annotation zurueck.
	 * 
	 * @return Tag-Name des Ressourcen-Elements.
	 */
	protected abstract String getTagNameResource();

	/**
	 * Gibt den Unterordner im Export-Verzeichnis zurueck, in dem die Ressource
	 * dieser Annotation liegt.
	 * 
	 * @return Unterordner der Ressource dieser Annotation.
	 */
	protected abstract String getResourceSubdirectory();

	/**
	 * Gibt den Tag-Namen des Action-Elements fuer diese Annotation zurueck.
	 * 
	 * @return Tag-Name des Action-Elements.
	 */
	protected abstract String getTagNameAction();

	/**
	 * Gibt an, ob die Action dieser Annotation einen Anzeige-Bereich oder einen
	 * Anzeige-Pfad definiert. Falls true, wird dieser in das Action-Element
	 * uebernommen.
	 * 
	 * @return True, falls Area- oder Path-Element in der Action benoetigt wird,
	 *         ansonsten false.
	 */
	protected abstract boolean requiresPositionInfo();

	/**
	 * Gibt das dateibasierte Objekt zurueck, dass dieser Annotation zugrunde
	 * liegt. Falls die Annotation kein dateibasiertes Objekt besitzt, muss null
	 * zurueck gegeben werden.
	 * 
	 * @return Dateibasiertes Objekt der Annotation oder null, falls diese kein
	 *         solches besitzt.
	 */
	protected abstract List<IFileBasedObject> getFileBasedObjects();

	/**
	 * In dieser Methode koennen zusaetzliche Attribute in das Ressource-Element
	 * eingefuegt werden.
	 * 
	 * @param element
	 *            Das Ressource-Element der Annotation.
	 */
	protected abstract void setAdditionalRessourceAttributes(Element element,
			IDManager idManager);

	/**
	 * Erstellt die Ressourcen-Elemente für die Annotationen, verwendet eine Annotation mehrere Ressourcen
	 * sind in der Liste entsprechend mehr Elemente
	 * @param annotation
	 * @param doc
	 * @param idManager
	 * @return
	 * @throws ExportException
	 */
	private List<Element> createRessources(INodeAnnotationLeaf annotation,
			Document doc, IDManager idManager) throws ExportException {
		// <plainText resID="plainText-Annotation1">
		// . . . <content langCode="de-de">Das ist der Anno-Text</content>
		// </plainText>
		
		List<Element> elements = new ArrayList<Element>();

		// ID der Ressource holen
		List<String> ressourceIDS = idManager.getRessourceID(annotation);

		List<IFileBasedObject> fileObjects = getFileBasedObjects();
		
		if (fileObjects != null && ressourceIDS.size() != fileObjects.size()) {
			throw new ExportException("Number of Ressource IDs must match Number of FileObjects");
		}
		System.out.println(annotation + " ------------ " + ressourceIDS.size() + " " + fileObjects);
		for (int i = 0; i < ressourceIDS.size(); i++) {
			String resID = ressourceIDS.get(i);

			// Ressource-Element anlegen.
			String tagNameResource = getTagNameResource();
			Element resource = doc.createElement(tagNameResource);
			resource.setAttribute(ATTR_RES_ID, resID);
	
			// Das Ressource-Element um Unterklassen-spezifische Attribute erweitern
			setAdditionalRessourceAttributes(resource, idManager);
	
			IFileBasedObject fileObject = null;
			if (fileObjects != null) {
				fileObject = fileObjects.get(i);
			}
			if (fileObject == null) {
				// Text-basierte Annotation
				// => Beschreibungen als Text-Content der Ressource hinzufuegen
				Collection<LocalizedString> texts = null;
				if (annotation instanceof NodeAnnotationSubtitle) {
					texts = ((NodeAnnotationSubtitle) annotation).getSubtitle().getDescriptions();
				} else {
					texts = annotation.getDescriptions();
				}
				if (texts.isEmpty())
					return null; // Mind. ein Text muss existieren!
				for (LocalizedString text : texts) {
					Element content = doc.createElement(TAG_CONTENT);
					content.setAttribute(ATTR_LANGCODE, text.getSivaLangcode());
					content.setTextContent(text.getValue());
					resource.appendChild(content);
				}
			} else {
				// Dateibasierte Annotation
				Collection<LocalizedFile> files = fileObject.getFiles();
				if (files.isEmpty())
					return null; // Mind. ein File muss existieren!
				for (LocalizedFile file : files) {
					Locale language = file.getLanguage();
					String filename = getResourceSubdirectory() + "/" //$NON-NLS-1$
							+ idManager.getFilename(fileObject, language, getExportParameters());
					if ((this instanceof XMLExporterNodeAnnotationVideo || this instanceof XMLExporterNodeAnnotationAudio) && idManager.getExportType() == ExportType.HTML5) {
						filename = filename.substring(0, filename.length() - 1); // Cut of the entailing '.'
					}					
					Element content = doc.createElement(TAG_CONTENT);
					content.setAttribute(ATTR_HREF, filename);
					content.setAttribute(ATTR_LANGCODE, file.getSivaLangcode());
					resource.appendChild(content);
				}
			}
			elements.add(resource);
		}
		return elements;
	}

	/**
	 * Erstellt ein Trigger-Element fuer eine Annotation
	 * 
	 * @param doc
	 * @param annotation
	 * @param idManager
	 * @return
	 * @throws ExportException
	 */
	private Element createTrigger(Document doc, INodeAnnotation annotation,
			IDManager idManager) throws ExportException {
		// <trigger triggerID="trigger-Anno1" REFactionID="action-Anno1"
		// . . . . . startTime="00:02:00.3" endTime="00:04:00.5"/>

		// Vater-Szene holen um die Start-Zeiten relativ zum Szenen-Beginn
		// berechnen zu koennen
		Scene scene = annotation.getParentScene().getScene();

		// IDs des Triggers und der getriggerten Action holen
		String actionID = idManager.getActionID(annotation);
		String triggerID = idManager.getTriggerID(annotation);

		// Trigger-Element anlegen
		Element trigger = doc.createElement(TAG_TRIGGER);
		trigger.setAttribute(ATTR_TRIGGER_ID, triggerID);
		trigger.setAttribute(ATTR_REF_ACTION_ID, actionID);
		// Falls der Trigger Start- und Endzeiten besitzt, ergaenzen wir diese
		if (annotation.getStart() != null) {
			long convertedStartTime = annotation.getStart() - scene.getStart();
			String startTime = SivaTime.getSivaXMLTime(convertedStartTime);
			trigger.setAttribute(ATTR_TRIGGER_STARTTIME, startTime);
		}
		if (annotation.getEnd() != null) {
			long convertedEndTime = annotation.getEnd() - scene.getStart();
			String endTime = SivaTime.getSivaXMLTime(convertedEndTime);
			trigger.setAttribute(ATTR_TRIGGER_ENDTIME, endTime);
		}
		return trigger;
	}

	/**
	 * Erstellt ein Action-Element zum Anzeigen fuer diese Annotation
	 * 
	 * @param doc
	 * @param annotation
	 * @param idManager
	 * @return
	 * @throws ExportException
	 */
	private Element createAction(Document doc, INodeAnnotationLeaf annotation,
			IDManager idManager, Project project) throws ExportException {

		// IDs der Action und der verknuepften Ressource holen
		String actionID = idManager.getActionID(annotation);
		List<String> resIDS = idManager.getRessourceID(annotation);

		// Action-Element anlegen
		String tagNameAction = getTagNameAction();
		String pauseVideo = "" + annotation.isPauseVideo(); //$NON-NLS-1$

		Element action = doc.createElement(tagNameAction);
		action.setAttribute(ATTR_ACTIONID, actionID);
		
		if (tagNameAction == TAG_SHOWIMAGE_GAL) {
			Element galRessources = doc.createElement(TAG_SHOWIMAGE_GALRES);
			for (String resID : resIDS) {
				Element galRessource = doc.createElement(TAG_SHOWIMAGE_GALSRES);
				galRessource.setAttribute(ATTR_REF_RES_ID, resID);
				galRessources.appendChild(galRessource);
			}
			action.appendChild(galRessources);
		} else {
			String resID = resIDS.get(0);
			action.setAttribute(ATTR_REF_RES_ID, resID);
		}
				
		action.setAttribute(ATTR_PAUSEVIDEO, pauseVideo);
		// Falls die Action der Annotation eine Area oder einen Pfad benoetigt
		// ergaenzen wir die Action entsprechend
		if (requiresPositionInfo()) {
			Element posInfo = createPositionInfo(annotation, doc, idManager,
					project);
			action.appendChild(posInfo);
		}
		setAdditionActionAttributes(action, idManager);

		// Fertig
		return action;
	}
	
	private void createIndexKeywords(INodeAnnotationLeaf annotation, 
			Document doc, IDManager idManager, NodeScene nodeScene) throws ExportException {

		Element index = getIndex(doc);
		NodeList keywordsIndex = index.getElementsByTagName(TAG_INDEX_KEYWORD);
		String triggerID = idManager.getTriggerID(annotation);
		String sceneID = idManager.getID(nodeScene);

		String[] keywords = annotation.getKeywords().split(","); //$NON-NLS-1$
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

				Element annotationKeyword = doc.createElement(TAG_SCENE);
				annotationKeyword.setAttribute(ATTR_REF_SCENEID, sceneID);
				annotationKeyword.setAttribute(ATTR_REF_TRIGGER_ID, triggerID);
				annotationKeyword.setAttribute(ATTR_RESSOURCE_TYPE,
						getTagNameResource());
				keyword.appendChild(annotationKeyword);
				index.appendChild(keyword);
			}
		}

	}

	protected abstract void setAdditionActionAttributes(Element action,
			IDManager idManager);
}
