package org.iviPro.newExport.descriptor.xml.objects;

import java.util.Set;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.resources.IResource;
import org.iviPro.model.resources.Scene;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.iviPro.newExport.descriptor.xml.IdManager.LabelType;
import org.iviPro.newExport.descriptor.xml.resources.IXMLResourceExporter;
import org.iviPro.newExport.descriptor.xml.resources.ResourceExporterFactory;
import org.iviPro.newExport.util.SivaTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class IXMLExporterNodeAnnotationLeaf extends IXMLExporter {
	
	private INodeAnnotationLeaf annotation;
	protected boolean isGlobalAnnotation;
	
	IXMLExporterNodeAnnotationLeaf(INodeAnnotationLeaf exportObj) {
		super(exportObj);
		annotation = exportObj;
		isGlobalAnnotation = annotation.getProject().getGlobalAnnotations()
				.contains(annotation);		
	}

	/**
	 * Used to export content annotations from within the NodeMark exporter.
	 * Exports the stored content annotation in the usual way and afterwards
	 * creates the keywords with the triggerID of the NodeMark.  
	 * 
	 * @param doc
	 *            The document that should be updated with the information of
	 *            the exported object.
	 * @param idManager
	 *            The ID-Manager that should be used to generate IDs of the
	 *            elements
	 * @param project
	 *            The project that the exported object belongs to.
	 * @param alreadyExported
	 *            Set of already exported objects. These should not be exported
	 *            again.
	 * @param nodeMark The NodeMark which is the parent of the annotation
	 * @param contentAnnotation The annotation itself (since the field of the
	 * superclass containing it cannot be accessed)
	 * @throws ExportException
	 *             If an error occurs while exporting this object.
	 */
	protected void exportContentAnno(Document doc, IdManager idManager,
			Project project, Set<Object> alreadyExported,
			NodeMark nodeMark, INodeAnnotationLeaf contentAnnotation)
					throws ExportException {
		exportObject(doc, idManager, project, alreadyExported);
		NodeScene scene = nodeMark.getParentScene();
		String triggerID = idManager.getTriggerID(nodeMark);
		createIndexKeywords(contentAnnotation, doc, idManager, scene, triggerID);
	}
	
	@Override
	protected final void exportObjectImpl(IAbstractBean exportObj,
			Document doc, IdManager idManager, Project project,
			Set<Object> alreadyExported) throws ExportException {
		
		// Export resources
		for (IResource resource : annotation.getResources()) {
			IXMLResourceExporter exporter = ResourceExporterFactory.createExporter(resource);
			exporter.exportResource(doc, idManager, alreadyExported);
		}

		// Create action
		Element actionList = getActions(doc);
		Element action = createAction(doc, annotation, idManager, project);
		actionList.appendChild(action);
		
		// Create trigger in storyboard and keywords for standard annotations.
		// For NodeMarks trigger and keywords are created in XMLExporterNodeMark.
		if (!isGlobalAnnotation && !annotation.isTriggerAnnotation()) {
			NodeScene parentScene = annotation.getParentScene();
			Element storyboard = getStoryboard(doc, parentScene, idManager);
			Element trigger = createTrigger(doc, annotation, idManager);
			storyboard.appendChild(trigger);
		
			createIndexKeywords(annotation, doc, idManager, parentScene, 
					idManager.getTriggerID(annotation));	
		}
	}

	/**
	 * Gibt den Tag-Namen des Ressourcen-Elements fuer diese Annotation zurueck.
	 * 
	 * @return Tag-Name des Ressourcen-Elements.
	 */
	protected abstract String getTagNameResource();

	// /**
	// * Gibt den Unterordner im Export-Verzeichnis zurueck, in dem die
	// Ressource
	// * dieser Annotation liegt.
	// *
	// * @return Unterordner der Ressource dieser Annotation.
	// */
	// protected abstract String getResourceSubdirectory();

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
	 * Creates a trigger for the annotation.
	 * <p/><b>Note:</b> This method may only be used for annotations which are neither global
	 * nor trigger annotations.
	 * 
	 * @param doc
	 * @param annotation
	 * @param idManager
	 * @return
	 * @throws ExportException
	 */
	private Element createTrigger(Document doc, INodeAnnotation annotation,
			IdManager idManager) throws ExportException {
		// <trigger triggerID="trigger-Anno1" REFactionID="action-Anno1"
		// . . . . . startTime="00:02:00.3" endTime="00:04:00.5"/>

		// Vater-Szene holen um die Start-Zeiten relativ zum Szenen-Beginn
		// berechnen zu koennen
		Scene scene = annotation.getParentScene().getScene();
		long sceneStartTime = 0L;
		if (scene != null) {
			sceneStartTime = scene.getStart();
		}

		// IDs des Triggers und der getriggerten Action holen
		String actionID = idManager.getActionID(annotation);
		String triggerID = idManager.getTriggerID(annotation);

		// Trigger-Element anlegen
		Element trigger = doc.createElement(TAG_TRIGGER);
		trigger.setAttribute(ATTR_TRIGGER_ID, triggerID);
		trigger.setAttribute(ATTR_REF_ACTION_ID, actionID);
		// Falls der Trigger Start- und Endzeiten besitzt, ergaenzen wir diese
		if (annotation.getStart() != null) {
			String startTime = SivaTime.getSivaXMLTime(annotation.getStart() 
					- sceneStartTime);
			trigger.setAttribute(ATTR_TRIGGER_STARTTIME, startTime);
		}
		if (annotation.getEnd() != null) {
			String endTime = SivaTime.getSivaXMLTime(annotation.getEnd() 
					- sceneStartTime);
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
			IdManager idManager, Project project) throws ExportException {

		// IDs der Action und der verknuepften Ressource holen
		String actionID = idManager.getActionID(annotation);
				
		// Action-Element anlegen
		String tagNameAction = getTagNameAction();
		Element action = doc.createElement(tagNameAction);
		
		// Add description
		String labelID = createLabel(annotation, doc, idManager, 
				annotation.getDescriptions(), LabelType.DESCRIPTION);
		action.setAttribute(ATTR_REFresIDtitle, labelID);
		
		action.setAttribute(ATTR_ACTIONID, actionID);

		// Set RefResID
		if (getReferencedResource() != null) {
			action.setAttribute(ATTR_REF_RES_ID, idManager.getID(getReferencedResource()));
		}
		
		if (!isGlobalAnnotation) {
			action.setAttribute(ATTR_DISABLEABLE,Boolean.toString(annotation.isDisableable()));
			action.setAttribute(ATTR_PAUSEVIDEO, Boolean.toString(annotation.isPauseVideo()));
		}
		// Falls die Action der Annotation eine Area oder einen Pfad benoetigt
		// ergaenzen wir die Action entsprechend
		if (requiresPositionInfo()) {
			Element posInfo = createPositionInfo(annotation, doc, idManager,
					project);
			action.appendChild(posInfo);
		}
		setAdditionalActionElements(action, idManager, doc);

		// Fertig
		return action;
	}
	
	private void createIndexKeywords(INodeAnnotationLeaf annotation,
			Document doc, IdManager idManager, NodeScene nodeScene,
			String triggerID)
			throws ExportException {

		Element index = getIndex(doc);
		NodeList keywordsIndex = index.getElementsByTagName(TAG_INDEX_KEYWORD);
		String sceneID = idManager.getID(nodeScene);

		// description in keywords not desired at the moment
		// String description = (annotation.getDescription() != null ? annotation.getDescription() : "");
		String keywords = annotation.getKeywords(); // + " " + description;
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


	/**
	 * Returns the main resource referenced by the respective annotation. If the annotation
	 * does not reference a resource (e.g. Picturegallery) <code>null</code> is returned.
	 * @return resource referenced by the respective annotation or <code>null</code> if no resource is referenced
	 */
	protected abstract IResource getReferencedResource();
	
	protected abstract void setAdditionalActionElements(Element action,
			IdManager idManager, Document doc) throws ExportException;
}
