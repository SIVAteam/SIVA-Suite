package org.iviPro.export.xml.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.iviPro.export.ExportException;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.Scene;
import org.iviPro.model.TocItem;
import org.iviPro.model.annotation.OverlayPathItem;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.NodeSelection;
import org.iviPro.model.graph.ScreenArea;
import org.iviPro.utils.SivaTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class IXMLExporter {

	// ----- <siva> ------------------------------------

	protected static final String TAG_SIVA = "siva"; //$NON-NLS-1$

	// ----- <projectInformation> ----------------------

	protected static final String TAG_PROJECTINFO = "projectInformation"; //$NON-NLS-1$
	protected static final String ATTR_VIDEONAMEREF = "REFresIDvideoName"; //$NON-NLS-1$
	protected static final String TAG_LANGUAGES = "languages"; //$NON-NLS-1$
	protected static final String TAG_LANGUAGE = "language"; //$NON-NLS-1$
	protected static final String ATTR_DEFAULTLANGCODE = "defaultLangCode"; //$NON-NLS-1$
	protected static final String ATTR_LANGCODE = "langCode"; //$NON-NLS-1$
	protected static final String ATTR_NAME = "name"; //$NON-NLS-1$
	protected static final String ATTR_VALUE = "value"; //$NON-NLS-1$
	protected static final String TAG_SETTINGS = "settings"; //$NON-NLS-1$
	protected static final String VAL_FULL = "full"; //$NON-NLS-1$
	protected static final String VAL_HEIGHT = "size_height"; //$NON-NLS-1$
	protected static final String VAL_STARTMODE = "startmode"; //$NON-NLS-1$
	protected static final String VAL_PROJECTNAME = "project_name"; //$NON-NLS-1$
	protected static final String VAL_WIDTH = "size_width"; //$NON-NLS-1$
	protected static final String VAL_WINDOWED = "window"; //$NON-NLS-1$
	protected static final String VAL_SIZELEFT = "area_left_width"; //$NON-NLS-1$
	protected static final String VAL_SIZERIGHT = "area_right_width"; //$NON-NLS-1$
	protected static final String VAL_SIZETOP = "area_top_height"; //$NON-NLS-1$
	protected static final String VAL_SIZEBOTTOM = "area_bottom_height"; //$NON-NLS-1$
	protected static final String VAL_SKIN = "skin"; //$NON-NLS-1$
	
	protected static final String VAL_DESIGNNAME = "design_name";
	protected static final String VAL_DESIGNSCHEMA = "design_schema"; 
	protected static final String VAL_COLORSCHEMA = "design_colorschema"; 
	protected static final String VAL_BACKGROUNDCOLOR = "design_backgroundcolor"; 
	protected static final String VAL_BORDERCOLOR = "design_bordercolor"; 
	protected static final String VAL_FONTCOLOR = "design_fontcolor"; 
	protected static final String VAL_FONT = "design_font"; 
	protected static final String VAL_FONTSIZE = "design_fontsize"; 
	protected static final String VAL_PRIMARYCOLOR = "design_primarycolor"; 
	
	protected static final String VAL_AUTORELOAD = "autoreload"; //$NON-NLS-1$
	protected static final String TAG_PROJECTRESSOURCES = "projectRessources"; //$NON-NLS-1$
	protected static final String TAG_PROJECTSTATES = "projectStates"; //$NON-NLS-1$

	// ----- <resourceSetting> -------------------------------
	protected static final String ATTR_RESSOURCESETTING = "resourceSettings";
	protected static final String VAL_VIDEO = "video"; //$NON-NLS-1$
	protected static final String VAL_AUDIO = "audio"; //$NON-NLS-1$
	protected static final String ATTR_CONTENTTYPE = "contentType"; 
	protected static final String ATTR_FILEFORMAT = "fileFormat";
	protected static final String ATTR_TYPE = "type";
	
	
	// ----- <sceneList> -------------------------------
	protected static final String TAG_SCENELIST = "sceneList"; //$NON-NLS-1$
	protected static final String ATTR_REF_STARTSCENE = "REFsceneIDstart"; //$NON-NLS-1$

	protected static final String TAG_SCENE = "scene"; //$NON-NLS-1$
	protected static final String ATTR_SCENE_ID = "sceneID"; //$NON-NLS-1$
	protected static final String ATTR_SCENE_NAME = "name"; //$NON-NLS-1$
	protected static final String ATTR_REFresIDname = "REFresIDname"; //$NON-NLS-1$
	protected static final String ATTR_SCENE_XPOS = "xPos"; //$NON-NLS-1$
	protected static final String ATTR_SCENE_YPOS = "yPos"; //$NON-NLS-1$
	protected static final String ATTR_REF_SCENEID = "REFsceneID"; //$NON-NLS-1$

	protected static final String TAG_STORYBOARD = "storyBoard"; //$NON-NLS-1$
	protected static final String ATTR_REF_ACTION_ID_END = "REFactionIDend"; //$NON-NLS-1$

	protected static final String TAG_TRIGGER = "trigger"; //$NON-NLS-1$
	protected static final String ATTR_TRIGGER_ID = "triggerID"; //$NON-NLS-1$
	protected static final String ATTR_TRIGGER_STARTTIME = "startTime"; //$NON-NLS-1$
	protected static final String ATTR_TRIGGER_ENDTIME = "endTime"; //$NON-NLS-1$

	// ----- <ressources> -------------------------------
	protected static final String TAG_RESSOURCES = "ressources"; //$NON-NLS-1$
	protected static final String ATTR_RES_ID = "resID"; //$NON-NLS-1$
	protected static final String ATTR_RES_CONTAINERFORMAT = "containerFormat"; //$NON-NLS-1$
	protected static final String ATTR_RES_VIDEOCODEC = "videoCodec"; //$NON-NLS-1$
	protected static final String ATTR_RES_AUDIOCODEC = "audioCodec"; //$NON-NLS-1$
	// Ressourcen-Typen
	protected static final String TAG_AUDIOSTREAM = "audioStream"; //$NON-NLS-1$
	protected static final String TAG_IMAGE = "image"; //$NON-NLS-1$
	protected static final String TAG_LABEL = "label"; //$NON-NLS-1$
	protected static final String TAG_PLAINTEXT = "plainText"; //$NON-NLS-1$
	protected static final String TAG_RICHPAGE = "richPage"; //$NON-NLS-1$
	protected static final String TAG_SUBTITLE = "subTitle"; //$NON-NLS-1$
	protected static final String TAG_VIDEOSTREAM = "videoStream"; //$NON-NLS-1$
	// Content
	protected static final String TAG_CONTENT = "content"; //$NON-NLS-1$
	protected static final String ATTR_HREF = "href"; //$NON-NLS-1$

	// ----- <actions> ----------------------------------

	protected static final String TAG_ACTIONS = "actions"; //$NON-NLS-1$
	protected static final String ATTR_ACTIONID = "actionID"; //$NON-NLS-1$
	protected static final String ATTR_PAUSEVIDEO = "pauseVideo"; //$NON-NLS-1$

	protected static final String TAG_LOADVIDEOSCENE = "loadVideoScene"; //$NON-NLS-1$
	
	protected static final String TAG_SHOWMARKCONTROL = "showMarkControl";
	protected static final String TAG_BUTTON = "button";
	protected static final String TAG_BUTTON_PATH = "buttonPath";
	protected static final String TAG_POLYGON = "polygon";
	protected static final String TAG_POLYGON_CHAIN = "polygonalChain";
	protected static final String TAG_POLYGON_CHAIN_VERTICES = "vertices";
	protected static final String TAG_ELLIPSE = "ellipse";
	protected static final String TAG_ELLIPSE_PATH = "ellipsePath";

	protected static final String TAG_SHOWSELECTIONCONTROL = "showSelectionControl"; //$NON-NLS-1$
	protected static final String ATTR_SHOWSELECTIONCONTROL_TYPE = "type"; //$NON-NLS-1$
	protected static final String ATTR_SHOWSELECTIONCONTROL_DEFAULTCONTROL = "REFcontrolIDdefault"; //$NON-NLS-1$
	protected static final String ATTR_SHOWSELECTIONCONTROL_TIMEOUT = "timeout"; //$NON-NLS-1$
	protected static final String TAG_CONTROL = "controls"; //$NON-NLS-1$
	protected static final String ATTR_CONTROL_ID = "controlID"; //$NON-NLS-1$
	protected static final String ATTR_STYLE = "style";
	protected static final String ATTR_DURATION = "duration";

	protected static final String TAG_PLAYAUDIO = "playAudio"; //$NON-NLS-1$
	protected static final String ATTR_PLAYAUDIO_MUTE = "muteVideo"; //$NON-NLS-1$
	protected static final String TAG_SHOWPLAINTEXT = "showPlainText"; //$NON-NLS-1$
	protected static final String TAG_SHOWSUBTITLE = "showSubTitle"; //$NON-NLS-1$
	protected static final String TAG_SHOWVIDEO = "showVideo"; //$NON-NLS-1$
	protected static final String TAG_SHOWRICHPAGE = "showRichPage"; //$NON-NLS-1$
	protected static final String TAG_SHOWIMAGE = "showImage"; //$NON-NLS-1$
	protected static final String TAG_SHOWIMAGE_GAL = "showImages"; //$NON-NLS-1$
	protected static final String TAG_SHOWIMAGE_GALRES = "galleryRessources"; //$NON-NLS-1$
	protected static final String TAG_SHOWIMAGE_GALSRES = "galleryRessource"; //$NON-NLS-1$
	protected static final String ATTR_PICGAL_COLUMNS = "columnCount"; //$NON-NLS-1$

	protected static final String TAG_AREA = "area"; //$NON-NLS-1$
	protected static final String ATTR_AREA_SCREENAREA = "screenArea"; //$NON-NLS-1$

	protected static final String TAG_PATH = "path"; //$NON-NLS-1$
	protected static final String TAG_OVERLAY = "overlay"; //$NON-NLS-1$
	protected static final String TAG_POINT = "point"; //$NON-NLS-1$
	protected static final String ATTR_POINT_XPOS = "xPos"; //$NON-NLS-1$
	protected static final String ATTR_POINT_YPOS = "yPos"; //$NON-NLS-1$
	protected static final String ATTR_POINT_XSIZE = "xSize"; //$NON-NLS-1$
	protected static final String ATTR_POINT_YSIZE = "ySize"; //$NON-NLS-1$
	protected static final String ATTR_POINT_TIME = "time"; //$NON-NLS-1$
	protected static final String ATTR_ELL_LENGTHA = "lengthA";
	protected static final String ATTR_ELL_LENGTHB = "lengthB";

	protected static final String TAG_ENDSIVA = "endSiva"; //$NON-NLS-1$
	
	protected static final String TAG_QUIZ_LINEAR = "showQuizLinear";
	protected static final String TAG_QUIZ_TESTPROPERTIES = "testProperties";
	protected static final String TAG_QUIZ_TASKLIST = "taskList";
	protected static final String TAG_QUIZ_POINTRANGE = "pointRange";
	protected static final String TAG_QUIZ_RANGE = "range";
	protected static final String TAG_QUIZ_TASK = "task";
	protected static final String TAG_QUIZ_ANSWER = "answer";
	protected static final String TAG_QUIZ_PATH = "path";
	protected static final String TAG_QUIZ_POINT = "point";
	protected static final String TAG_QUIZ_YPOS = "yPos";
	protected static final String TAG_QUIZ_XPOS = "xPos";
	protected static final String TAG_QUIZ_YSIZE = "ySize";
	protected static final String TAG_QUIZ_XSIZE = "xSize";
	protected static final String TAG_QUIZ_TIME = "time";
	protected static final String TAG_QUIZ_TIMEOFFEEDBACK = "timeOfFeedback";
	protected static final String TAG_QUIZ_MAXPOINTS = "maxPoints";
	

	// ----- <tableOfContent> ---------------------------

	protected static final String TAG_TOC_ROOT = "tableOfContents"; //$NON-NLS-1$
	protected static final String TAG_TOC_CONTENTS = "contents"; //$NON-NLS-1$
	protected static final String TAG_TOC_ADJACENCY_LIST = "adjacencyRefListNode"; //$NON-NLS-1$
	protected static final String ATTR_TOC_CONTENTSNODEID = "contentsNodeID"; //$NON-NLS-1$
	protected static final String ATTR_TOC_REF_CONTENTSNODEID = "REFcontentsNodeID"; //$NON-NLS-1$

	// ----- <index> ----------------------------------

	protected static final String TAG_INDEX = "index"; //$NON-NLS-1$
	protected static final String TAG_INDEX_KEYWORD = "keyword"; //$NON-NLS-1$
	protected static final String ATTR_KEYWORD_WORD = "word"; //$NON-NLS-1$
	protected static final String ATTR_REF_TRIGGER_ID = "REFtriggerID"; //$NON-NLS-1$
	protected static final String ATTR_RESSOURCE_TYPE = "ressourceType"; //$NON-NLS-1$

	// ------ GLOBAL: REFERENCES -------------------------------------------

	protected static final String ATTR_REF_RES_ID = "REFresID"; //$NON-NLS-1$
	protected static final String ATTR_REF_RES_ID_SECONDARY = "REFresIDsec"; //$NON-NLS-1$
	protected static final String ATTR_REF_ACTION_ID = "REFactionID"; //$NON-NLS-1$
	protected static final String ATTR_REF_LANGCODE = "langCode"; //$NON-NLS-1$
	

	// ========================================================================

	private IAbstractBean exportObj = null;
	protected ExportParameters parameters;

	IXMLExporter(IAbstractBean exportObj) {
		this.exportObj = exportObj;
		this.parameters = new ExportParameters();
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
	public void exportObject(Document doc, IDManager idManager,
			Project project, Set<IAbstractBean> alreadyExported)
			throws ExportException {
		Class<? extends IAbstractBean> procClass = getExportObjectType();
		if (exportObj == null) {
			throw new ExportException("The exported Object in XML-Processor '" //$NON-NLS-1$
					+ this.getClass().getSimpleName() + "' was null!"); //$NON-NLS-1$
		}
		if (!procClass.
				isAssignableFrom(
						exportObj.getClass())) {
			throw new ExportException("The XML-Processor '" //$NON-NLS-1$
					+ this.getClass().getSimpleName()
					+ "' can not process objects of type '" //$NON-NLS-1$
					+ exportObj.getClass().getSimpleName() + "'."); //$NON-NLS-1$
		} else {
			// Falls Objekt nicht bereits exportiert wurde, wird es jetzt
			// exportiert.
			if (!alreadyExported.contains(exportObj)) {
				alreadyExported.add(exportObj);
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
			Document doc, IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported) throws ExportException;

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
	 * Liefert eine Liste von Export Parametern für den gerade verwendeten XMLExporter
	 * z.B. Start und Endzeit für eine Annotation
	 * @return
	 */
	protected abstract ExportParameters getExportParameters();
	
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
				XMLExporterProject.TAG_RESSOURCES);
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
			IDManager idManager) throws ExportException {
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
	 * Erstellt die <label> Elemente fuer die Description eines bestimmten
	 * Model-Objekts im angegebenen XML-Dokument und gibt die ID des generierten
	 * <label> Elements zurueck. Falls das Element keine Descriptions besitzt,
	 * wird null zurueck gegeben und das Dokument nicht veraendert.
	 * 
	 * @param obj
	 * @param doc
	 * @param idManager
	 * @return
	 * @throws ExportException
	 */
	protected String createDescriptionLabels(IAbstractBean obj, Document doc,
			IDManager idManager) throws ExportException {
		// <label resID="label-button1">
		// . . . <content langCode="de-de">Button 01</content>
		// </label>
		Element resources = getRessources(doc);
		Element label = doc.createElement(TAG_LABEL);
		String labelID = idManager.getDescriptionLabelID(obj);
		label.setAttribute(ATTR_RES_ID, labelID);
		Collection<LocalizedString> descriptions = obj.getDescriptions();
		if (descriptions.isEmpty()) {
			return null;
		}
		for (LocalizedString descr : descriptions) {
			Element content = doc.createElement(TAG_CONTENT);
			String langCode = descr.getSivaLangcode();
			content.setAttribute(ATTR_LANGCODE, langCode);
			content.setTextContent(descr.getValue());
			label.appendChild(content);
		}
		resources.appendChild(label);
		return labelID;
	}

	/**
	 * Erstellt die <label> Elemente fuer die Title eines bestimmten
	 * Model-Objekts im angegebenen XML-Dokument und gibt die ID des generierten
	 * <label> Elements zurueck. Falls das Element keine Titles besitzt, wird
	 * null zurueck gegeben und das Dokument nicht veraendert.
	 * 
	 * @param obj
	 * @param doc
	 * @param idManager
	 * @return
	 * @throws ExportException
	 */
	protected String createTitleLabels(IAbstractBean obj, Document doc,
			IDManager idManager) throws ExportException {
		// <label resID="label-button1">
		// . . . <content langCode="de-de">Button 01</content>
		// </label>
		Element resources = getRessources(doc);
		Element label = doc.createElement(TAG_LABEL);
		String labelID = idManager.getTitleLabelID(obj);
		label.setAttribute(ATTR_RES_ID, labelID);
		Collection<LocalizedString> titles = obj.getTitles();
		if (titles.isEmpty()) {
			return null;
		}
		for (LocalizedString title : titles) {
			Element content = doc.createElement(TAG_CONTENT);
			String langCode = title.getSivaLangcode();
			content.setAttribute(ATTR_LANGCODE, langCode);
			content.setTextContent(title.getValue());
			label.appendChild(content);
		}
		resources.appendChild(label);
		return labelID;
	}

	/**
	 * Erstellt die Positions-Info fuer eine Annotation oder das Inhaltsverzeichnis (Overlay oder Pfad).
	 * 
	 * @param annotation
	 * @param doc
	 * @return
	 */
	protected Element createPositionInfo(IAbstractBean annotation,
			Document doc, IDManager idManager, Project project) {
		// <area screenArea="left|top|right|bottom">
		// <overlay>
		// . <point xPos="0" yPos="0" xSize="1" ySize="1" time="00:00:00.000" />
		// . ...
		// </overlay>
		
		ScreenArea screenArea = null;
		if (annotation instanceof INodeAnnotation) {
			screenArea = ((INodeAnnotation)annotation).getScreenArea();
		} else if (annotation instanceof TocItem) {
			screenArea = ((TocItem)annotation).getScreenArea();
		}
		
		// if no screen Area is set at export set it to overlay
		if (screenArea == null) {
			screenArea = ScreenArea.OVERLAY;
		}
				
		if (screenArea.equals(ScreenArea.OVERLAY)) {
			Element overlay;
			if (annotation instanceof INodeAnnotationLeaf
					|| annotation instanceof NodeSelection || annotation instanceof TocItem) {
				overlay = doc.createElement(TAG_PATH);
			} else {
				overlay = doc.createElement(TAG_OVERLAY);
			}
			Scene scene = null; 
			try {
				scene = ((INodeAnnotation)annotation).getParentScene().getScene();
			} catch (Exception e) {
				
			}			

			List<OverlayPathItem> overlayPath = null;
			if (annotation instanceof INodeAnnotation) {
				overlayPath = new ArrayList<OverlayPathItem>(((INodeAnnotation)annotation).getOverlayPath());
			} else if (annotation instanceof TocItem) {
				overlayPath = new ArrayList<OverlayPathItem>(((TocItem)annotation).getOverlayPath());
			}
			boolean centerFlag = false;
			if (overlayPath.isEmpty()) {
				// Overlay-Pfad darf nicht leer sein - Falls doch, dann
				// erstellen wir ein Dummy-Item, weil das Schema das so verlangt
				// Das Item sieht wie folgt aus, siehe Ticket #327
				// <point xSize="-1" xPos="-1" time="00:00:00" ySize="-1"
				// yPos="-1" /> wobei xPos und yPos nicht ins XML geschrieben werden
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
					point.setAttribute(ATTR_POINT_XPOS, "" + pathItem.getX()); //$NON-NLS-1$
					point.setAttribute(ATTR_POINT_YPOS, "" + pathItem.getY()); //$NON-NLS-1$
				}
				point.setAttribute(ATTR_POINT_XSIZE,
						"" + pathItem.getWidth()); //$NON-NLS-1$
				point.setAttribute(ATTR_POINT_YSIZE,
						"" + pathItem.getHeight()); //$NON-NLS-1$
				if (centerFlag) {
					time = "00:00:00.000"; //$NON-NLS-1$
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
