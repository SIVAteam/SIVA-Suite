package org.iviPro.newExport.descriptor.xml.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.TocItem;
import org.iviPro.model.annotation.OverlayPathItem;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.ScreenArea;
import org.iviPro.model.resources.Scene;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.iviPro.newExport.descriptor.xml.IdManager.LabelType;
import org.iviPro.newExport.util.SivaTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class IXMLExporter implements SivaDefinition {
	
	private static Logger logger = Logger.getLogger(IXMLExporter.class);
	
	private IAbstractBean exportObj = null;

	IXMLExporter(IAbstractBean exportObj) {
		this.exportObj = exportObj;
	}

	abstract Class<? extends IAbstractBean> getExportObjectType();

	/**
	 * Processes the given model data object and exports it into the given
	 * document.
	 * 
	 * @param exportObj
	 *            The model object to be exported. This object must be of the
	 *            same type as the getExportObjectType() method returns.
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
	 * @throws ExportException
	 *             If an error occurs while exporting this object.
	 */
	public void exportObject(Document doc, IdManager idManager,
			Project project, Set<Object> alreadyExported)
			throws ExportException {
		Class<? extends IAbstractBean> procClass = getExportObjectType();
		if (exportObj == null) {
			throw new ExportException("The exported Object in XML-Processor '" //$NON-NLS-1$
					+ this.getClass().getSimpleName() + "' was null!"); //$NON-NLS-1$
		}
		// XXX This check is probably needless. If the XML processor can't
		// handle the submitted object it will throw an exception! So why
		// making the code unnecessarily complicated with this
		// getExportObjectType-thing...
		if (!procClass.isAssignableFrom(exportObj.getClass())) {
			throw new ExportException("The XML-Processor '" //$NON-NLS-1$
					+ this.getClass().getSimpleName()
					+ "' can not process objects of type '" //$NON-NLS-1$
					+ exportObj.getClass().getSimpleName() + "'."); //$NON-NLS-1$
		} else {
			// Falls Objekt nicht bereits exportiert wurde, wird es jetzt
			// exportiert.
			if (!alreadyExported.contains(exportObj)) {
				alreadyExported.add(exportObj);
				logger.debug("Starting XML export of " 
						+ exportObj.getClass().getSimpleName()); //$NON-NLS-1$
				exportObjectImpl(exportObj, doc, idManager, project,
						alreadyExported);
			}
		}
	}

	/**
	 * 
	 * @param exportObj
	 *            The model object to be exported. You can be sure that this
	 *            object is castable to the type that you specified in the
	 *            getExportObjectType() methode.
	 * @param doc
	 *            The document that should be updated with the information of
	 *            the exported object.
	 * @param idManager
	 *            The ID-Manager that should be used to generate IDs of the
	 *            elements
	 * @param project
	 *            The project that the exported object belongs to.
	 * @param alreadyExported
	 *            Set of already exported objects. These must not be checked
	 *            necessarily, because the export method is only called, if the
	 *            object is not yet exported.
	 * @throws ExportException
	 *             If an error occurs while exporting this object.
	 */
	protected abstract void exportObjectImpl(IAbstractBean exportObj,
			Document doc, IdManager idManager, Project project,
			Set<Object> alreadyExported) throws ExportException;

	/**
	 * Sucht das XML-Element &lt;sceneList&gt; der Liste der Szenen in dem
	 * angegebenen Dokument.
	 * 
	 * @param doc
	 * @return
	 * @throws ExportException
	 */
	protected Element getSceneList(Document doc) throws ExportException {
		NodeList nodeList = doc.getDocumentElement().getElementsByTagName(
				XMLExporterProject.TAG_SCENELIST);
		if (nodeList.getLength() == 1) {
			return (Element) nodeList.item(0);
		} else {
			throw new ExportException(
					"Error: sceneList XML-Element could not be found."); //$NON-NLS-1$
		}
	}

	/**
	 * Sucht das XML-Element &lt;index&gt; der Liste der Szenen in dem
	 * angegebenen Dokument.
	 * 
	 * @param doc
	 * @return
	 * @throws ExportException
	 */
	protected Element getIndex(Document doc) throws ExportException {
		NodeList nodeList = doc.getDocumentElement().getElementsByTagName(
				XMLExporterProject.TAG_INDEX);
		if (nodeList.getLength() == 1) {
			return (Element) nodeList.item(0);
		} else {
			Error e = new Error();
			e.printStackTrace();
			throw new ExportException(
					"Error: index XML-Element could not be found."); //$NON-NLS-1$
		}
	}

	/**
	 * Sucht das XML-Element &lt;actions&gt; der Liste mit allen definierten
	 * Actions in dem angegebenen Dokument.
	 * 
	 * @param doc
	 * @return
	 * @throws ExportException
	 */
	protected Element getActions(Document doc) throws ExportException {
		NodeList nodeList = doc.getDocumentElement().getElementsByTagName(
				XMLExporterProject.TAG_ACTIONS);
		if (nodeList.getLength() == 1) {
			return (Element) nodeList.item(0);
		} else {
			throw new ExportException("Error: " //$NON-NLS-1$
					+ XMLExporterProject.TAG_ACTIONS
					+ " XML-Element could not be found."); //$NON-NLS-1$
		}
	}

	/**
	 * Sucht das XML-Element &lt;tableOfContents&gt; in dem angegebenen
	 * Dokument, welches die einzelnen Punkte des Inhaltsverzeichnis enthaelt.
	 * 
	 * @param doc
	 *            Das Dokument
	 * @return
	 * @throws ExportException
	 */
	protected Element getTableOfContents(Document doc) throws ExportException {
		NodeList nodeList = doc.getDocumentElement().getElementsByTagName(
				XMLExporterProject.TAG_TOC_ROOT);
		if (nodeList.getLength() == 1) {
			return (Element) nodeList.item(0);
		} else {
			throw new ExportException("Error: " //$NON-NLS-1$
					+ XMLExporterProject.TAG_TOC_ROOT
					+ " XML-Element could not be found."); //$NON-NLS-1$
		}
	}

	/**
	 * Sucht das XML-Element &lt;ressources&gt; der Liste der Projekt-Ressourcen
	 * in dem angegebenen Dokument.
	 * 
	 * @param doc
	 * @return
	 * @throws ExportException
	 */
	protected Element getRessources(Document doc) throws ExportException {
		NodeList nodeList = doc.getDocumentElement().getElementsByTagName(
				TAG_RESOURCES);
		if (nodeList.getLength() == 1) {
			return (Element) nodeList.item(0);
		} else {
			throw new ExportException(
					"Error: ressources XML-Element could not be found."); //$NON-NLS-1$
		}
	}

	/**
	 * Sucht das Storyboard-Element fuer eine bestimmte Szene im XML-Dokument.
	 * 
	 * @param doc
	 * @param nodeScene
	 * @param idManager
	 * @return
	 * @throws ExportException
	 *             Falls das Storyboard-Element nicht gefunden werden konnte.
	 */
	protected Element getStoryboard(Document doc, NodeScene nodeScene,
			IdManager idManager) throws ExportException {
		Element sceneElement = doc.getElementById(idManager.getID(nodeScene));
		if (sceneElement == null) {
			throw new ExportException(
					"Could not get storyboard: The <scene> Element for scene " //$NON-NLS-1$
							+ nodeScene + " could not be found."); //$NON-NLS-1$
		}
		Element storyBoard = (Element) sceneElement.getElementsByTagName(
				TAG_STORYBOARD).item(0);
		if (storyBoard == null) {
			throw new ExportException(
					"Could not get storyboard: The <scene> Element for scene " //$NON-NLS-1$
							+ nodeScene + " has no <storyBoard> element."); //$NON-NLS-1$
		}
		return storyBoard;
	}
	
	/**
	 * Generates label XML node containing the localized contents
	 * contained in the <code>stringContents</code> parameter. A string
	 * identifying the created label is returned. 
	 * @param obj object referring to the label
	 * @param doc document in which the label is created
	 * @param idManager idManager used to create XML ids
	 * @param stringContents localized content of the label
	 * @param type type of the label
	 * @return identifier of the label 
	 * @throws ExportException if an error occurs during creation of the label
	 */
	protected String createLabel(IAbstractBean obj, Document doc,
			IdManager idManager, Collection<LocalizedString> stringContents,
			LabelType type) throws ExportException {
		// <label resID="label-button1">
		// . . . <content langCode="de-de">Button 01</content>
		// </label>
		Element resources = getRessources(doc);
		Element label = doc.createElement(TAG_LABEL);
		String labelID = idManager.getLabelId(type, obj);
		label.setAttribute(ATTR_RES_ID, labelID);
		
		if (stringContents.isEmpty()) {
			return null;
		}
		for (LocalizedString string : stringContents) {
			Element content = doc.createElement(TAG_CONTENT);
			String langCode = string.getSivaLangcode();
			content.setAttribute(ATTR_LANGCODE, langCode);
			content.setTextContent(string.getValue());
			label.appendChild(content);
		}
		resources.appendChild(label);
		return labelID;
	}

	/**
	 * Erstellt die Positions-Info fuer eine Annotation oder das
	 * Inhaltsverzeichnis (Overlay oder Pfad).
	 * 
	 * @param annotation
	 * @param doc
	 * @return
	 */
	protected Element createPositionInfo(IAbstractBean annotation,
			Document doc, IdManager idManager, Project project) {
		// <area screenArea="left|top|right|bottom">
		// <overlay>
		// . <point xPos="0" yPos="0" xSize="1" ySize="1" time="00:00:00.000" />
		// . ...
		// </overlay>

		ScreenArea screenArea = null;
		if (annotation instanceof INodeAnnotation) {
			screenArea = ((INodeAnnotation) annotation).getScreenArea();
		} else if (annotation instanceof TocItem) {
			screenArea = ((TocItem) annotation).getScreenArea();
		}

		// if no screen Area is set at export set it to overlay
		if (screenArea == null) {
			screenArea = ScreenArea.OVERLAY;
		}

		if (screenArea.equals(ScreenArea.OVERLAY)) {
			Element overlay;
			overlay = doc.createElement(TAG_PATH);
			
			Scene scene = null;
			NodeScene nodeScene = ((INodeAnnotation) annotation).getParentScene();
			if (nodeScene != null) {
				scene = nodeScene.getScene();
			}

			List<OverlayPathItem> overlayPath = null;
			if (annotation instanceof INodeAnnotation) {
				overlayPath = new ArrayList<OverlayPathItem>(
						((INodeAnnotation) annotation).getOverlayPath());
			} else if (annotation instanceof TocItem) {
				overlayPath = new ArrayList<OverlayPathItem>(
						((TocItem) annotation).getOverlayPath());
			}
			boolean centerFlag = false;
			if (overlayPath.isEmpty()) {
				// Overlay-Pfad darf nicht leer sein - Falls doch, dann
				// erstellen wir ein Dummy-Item, weil das Schema das so verlangt
				// Das Item sieht wie folgt aus, siehe Ticket #327
				// <point xSize="-1" xPos="-1" time="00:00:00" ySize="-1"
				// yPos="-1" /> wobei xPos und yPos nicht ins XML geschrieben
				// werden
				overlayPath
						.add(new OverlayPathItem(-1, -1, -1, -1, 0, project));
				centerFlag = true;
			}
			for (OverlayPathItem pathItem : overlayPath) {
				Element point = doc.createElement(TAG_POINT);
				String time = ""; //$NON-NLS-1$
				if (scene != null) {
					time = SivaTime.getSivaXMLTime(pathItem
							.getTimeRelativeTo(scene));
				} else {
					time = SivaTime.getSivaXMLTime(pathItem.getTime());
				}

				if (!centerFlag) {
					point.setAttribute(ATTR_POINT_XPOS,
							String.valueOf(pathItem.getX()));
					point.setAttribute(ATTR_POINT_YPOS,
							String.valueOf(pathItem.getY()));
				}
				point.setAttribute(ATTR_POINT_XSIZE,
						String.valueOf(pathItem.getWidth()));
				point.setAttribute(ATTR_POINT_YSIZE,
						String.valueOf(pathItem.getHeight()));
				if (centerFlag) {
					time = VAL_TIME_NULL;
				}
				point.setAttribute(ATTR_POINT_TIME, time);
				overlay.appendChild(point);
			}
			return overlay;
		} else {
			Element area = doc.createElement(TAG_AREA);
			String sivaScreenArea = screenArea.toString().toLowerCase();
			area.setAttribute(ATTR_AREA_SCREENAREA, sivaScreenArea);
			return area;
		}
	}
}
