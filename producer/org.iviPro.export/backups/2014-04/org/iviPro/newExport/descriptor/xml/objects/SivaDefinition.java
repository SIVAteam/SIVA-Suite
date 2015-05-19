package org.iviPro.newExport.descriptor.xml.objects;

public interface SivaDefinition {

	// ----- <siva> ------------------------------------

	public static final String TAG_SIVA = "siva"; //$NON-NLS-1$
	public static final String ATTR_SIVA_NAMESPACE = "xmlns:xsi"; //$NON-NLS-1$
	public static final String VAL_SIVA_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance"; //$NON-NLS-1$
	public static final String ATTR_SIVA_SCHEMA = "xsi:noNamespaceSchemaLocation"; //$NON-NLS-1$
	public static final String VAL_SIVA_SCHEMA = "sivaPlayer.xsd"; //$NON-NLS-1$

	// ----- <projectInformation> ----------------------

	public static final String TAG_PROJECTINFO = "projectInformation"; //$NON-NLS-1$
	public static final String ATTR_VIDEONAMEREF = "REFresIDvideoName"; //$NON-NLS-1$
	public static final String TAG_LANGUAGES = "languages"; //$NON-NLS-1$
	public static final String TAG_LANGUAGE = "language"; //$NON-NLS-1$
	public static final String ATTR_DEFAULTLANGCODE = "defaultLangCode"; //$NON-NLS-1$
	public static final String ATTR_LANGCODE = "langCode"; //$NON-NLS-1$
	public static final String ATTR_NAME = "name"; //$NON-NLS-1$
	public static final String ATTR_VALUE = "value"; //$NON-NLS-1$
	public static final String TAG_SETTINGS = "settings"; //$NON-NLS-1$
	public static final String VAL_FULL = "full"; //$NON-NLS-1$
	public static final String VAL_HEIGHT = "size_height"; //$NON-NLS-1$
	public static final String VAL_STARTMODE = "startmode"; //$NON-NLS-1$
	public static final String VAL_AUTOPLAY ="autoplay"; //$NON-NLS-1$
	public static final String VAL_PROJECTNAME = "project_name"; //$NON-NLS-1$
	public static final String VAL_WIDTH = "size_width"; //$NON-NLS-1$
	public static final String VAL_WINDOWED = "window"; //$NON-NLS-1$
	public static final String VAL_SIZELEFT = "area_left_width"; //$NON-NLS-1$
	public static final String VAL_SIZERIGHT = "area_right_width"; //$NON-NLS-1$
	public static final String VAL_SIZETOP = "area_top_height"; //$NON-NLS-1$
	public static final String VAL_SIZEBOTTOM = "area_bottom_height"; //$NON-NLS-1$
	public static final String VAL_SKIN = "skin"; //$NON-NLS-1$
	
	public static final String VAL_DESIGNNAME = "design_name";
	public static final String VAL_DESIGNSCHEMA = "design_schema"; 
	public static final String VAL_COLORSCHEMA = "design_colorschema"; 
	public static final String VAL_BACKGROUNDCOLOR = "design_backgroundcolor"; 
	public static final String VAL_BORDERCOLOR = "design_bordercolor"; 
	public static final String VAL_FONTCOLOR = "design_fontcolor"; 
	public static final String VAL_FONT = "design_font"; 
	public static final String VAL_FONTSIZE = "design_fontsize"; 
	public static final String VAL_PRIMARYCOLOR = "design_primarycolor"; 
	
	public static final String VAL_AUTORELOAD = "autoreload"; //$NON-NLS-1$
	public static final String TAG_PROJECTRESSOURCES = "projectRessources"; //$NON-NLS-1$
	public static final String TAG_PROJECTSTATES = "projectStates"; //$NON-NLS-1$

	// ----- <resourceSetting> -------------------------------
	public static final String ATTR_RESSOURCESETTING = "resourceSettings"; //$NON-NLS-1$
	public static final String VAL_VIDEO = "video"; //$NON-NLS-1$
	public static final String VAL_AUDIO = "audio"; //$NON-NLS-1$
	public static final String ATTR_CONTENTTYPE = "contentType"; //$NON-NLS-1$
	public static final String ATTR_FILEFORMAT = "fileFormat"; //$NON-NLS-1$
	public static final String ATTR_TYPE = "type"; //$NON-NLS-1$

	// ----- <sceneList> -------------------------------
	public static final String TAG_SCENELIST = "sceneList"; //$NON-NLS-1$
	public static final String ATTR_REF_STARTSCENE = "REFsceneIDstart"; //$NON-NLS-1$

	public static final String TAG_SCENE = "scene"; //$NON-NLS-1$
	public static final String ATTR_SCENE_ID = "sceneID"; //$NON-NLS-1$
	public static final String ATTR_SCENE_NAME = "name"; //$NON-NLS-1$
	public static final String ATTR_REFresIDname = "REFresIDname"; //$NON-NLS-1$
	public static final String ATTR_SCENE_XPOS = "xPos"; //$NON-NLS-1$
	public static final String ATTR_SCENE_YPOS = "yPos"; //$NON-NLS-1$
	public static final String ATTR_REF_SCENEID = "REFsceneID"; //$NON-NLS-1$

	public static final String TAG_STORYBOARD = "storyBoard"; //$NON-NLS-1$
	public static final String ATTR_REF_ACTION_ID_END = "REFactionIDend"; //$NON-NLS-1$

	public static final String TAG_TRIGGER = "trigger"; //$NON-NLS-1$
	public static final String ATTR_TRIGGER_ID = "triggerID"; //$NON-NLS-1$
	public static final String ATTR_TRIGGER_STARTTIME = "startTime"; //$NON-NLS-1$
	public static final String ATTR_TRIGGER_ENDTIME = "endTime"; //$NON-NLS-1$

	// ----- <ressources> -------------------------------
	public static final String TAG_RESSOURCES = "ressources"; //$NON-NLS-1$
	public static final String ATTR_RES_ID = "resID"; //$NON-NLS-1$
	public static final String ATTR_RES_CONTAINERFORMAT = "containerFormat"; //$NON-NLS-1$
	public static final String ATTR_RES_VIDEOCODEC = "videoCodec"; //$NON-NLS-1$
	public static final String ATTR_RES_AUDIOCODEC = "audioCodec"; //$NON-NLS-1$
	// Ressourcen-Typen
	public static final String TAG_AUDIOSTREAM = "audioStream"; //$NON-NLS-1$
	public static final String TAG_IMAGE = "image"; //$NON-NLS-1$
	public static final String TAG_LABEL = "label"; //$NON-NLS-1$
	public static final String TAG_PLAINTEXT = "plainText"; //$NON-NLS-1$
	public static final String TAG_RICHPAGE = "richPage"; //$NON-NLS-1$
	public static final String TAG_SUBTITLE = "subTitle"; //$NON-NLS-1$
	public static final String TAG_VIDEOSTREAM = "videoStream"; //$NON-NLS-1$
	// Content
	public static final String TAG_CONTENT = "content"; //$NON-NLS-1$
	public static final String ATTR_HREF = "href"; //$NON-NLS-1$

	// ----- <actions> ----------------------------------

	public static final String TAG_ACTIONS = "actions"; //$NON-NLS-1$
	public static final String ATTR_ACTIONID = "actionID"; //$NON-NLS-1$
	public static final String ATTR_PAUSEVIDEO = "pauseVideo"; //$NON-NLS-1$

	public static final String TAG_LOADVIDEOSCENE = "loadVideoScene"; //$NON-NLS-1$

	public static final String TAG_SHOWMARKCONTROL = "showMarkControl"; //$NON-NLS-1$
	public static final String TAG_BUTTON = "button"; //$NON-NLS-1$
	public static final String TAG_BUTTON_PATH = "buttonPath"; //$NON-NLS-1$
	public static final String TAG_POLYGON = "polygon"; //$NON-NLS-1$
	public static final String TAG_POLYGON_CHAIN = "polygonalChain"; //$NON-NLS-1$
	public static final String TAG_POLYGON_CHAIN_VERTICES = "vertices"; //$NON-NLS-1$
	public static final String TAG_ELLIPSE = "ellipse"; //$NON-NLS-1$
	public static final String TAG_ELLIPSE_PATH = "ellipsePath"; //$NON-NLS-1$

	public static final String TAG_SHOWSELECTIONCONTROL = "showSelectionControl"; //$NON-NLS-1$
	public static final String ATTR_SHOWSELECTIONCONTROL_TYPE = "type"; //$NON-NLS-1$
	public static final String ATTR_SHOWSELECTIONCONTROL_DEFAULTCONTROL = "REFcontrolIDdefault"; //$NON-NLS-1$
	public static final String ATTR_SHOWSELECTIONCONTROL_TIMEOUT = "timeout"; //$NON-NLS-1$
	public static final String TAG_CONTROL = "controls"; //$NON-NLS-1$
	public static final String ATTR_CONTROL_ID = "controlID"; //$NON-NLS-1$
	public static final String ATTR_STYLE = "style"; //$NON-NLS-1$
	public static final String ATTR_DURATION = "duration"; //$NON-NLS-1$

	public static final String TAG_PLAYAUDIO = "playAudio"; //$NON-NLS-1$
	public static final String ATTR_PLAYAUDIO_MUTE = "muteVideo"; //$NON-NLS-1$
	public static final String TAG_SHOWPLAINTEXT = "showPlainText"; //$NON-NLS-1$
	public static final String TAG_SHOWSUBTITLE = "showSubTitle"; //$NON-NLS-1$
	public static final String TAG_SHOWVIDEO = "showVideo"; //$NON-NLS-1$
	public static final String TAG_SHOWRICHPAGE = "showRichPage"; //$NON-NLS-1$
	public static final String TAG_SHOWIMAGE = "showImage"; //$NON-NLS-1$
	public static final String TAG_SHOWIMAGE_GAL = "showImages"; //$NON-NLS-1$
	public static final String TAG_SHOWIMAGE_GALRES = "galleryRessources"; //$NON-NLS-1$
	public static final String TAG_SHOWIMAGE_GALSRES = "galleryRessource"; //$NON-NLS-1$
	public static final String ATTR_PICGAL_COLUMNS = "columnCount"; //$NON-NLS-1$

	public static final String TAG_AREA = "area"; //$NON-NLS-1$
	public static final String ATTR_AREA_SCREENAREA = "screenArea"; //$NON-NLS-1$

	public static final String TAG_PATH = "path"; //$NON-NLS-1$
	public static final String TAG_OVERLAY = "overlay"; //$NON-NLS-1$
	public static final String TAG_POINT = "point"; //$NON-NLS-1$
	public static final String ATTR_POINT_XPOS = "xPos"; //$NON-NLS-1$
	public static final String ATTR_POINT_YPOS = "yPos"; //$NON-NLS-1$
	public static final String ATTR_POINT_XSIZE = "xSize"; //$NON-NLS-1$
	public static final String ATTR_POINT_YSIZE = "ySize"; //$NON-NLS-1$
	public static final String ATTR_POINT_TIME = "time"; //$NON-NLS-1$
	public static final String ATTR_ELL_LENGTHA = "lengthA"; //$NON-NLS-1$
	public static final String ATTR_ELL_LENGTHB = "lengthB"; //$NON-NLS-1$

	public static final String TAG_ENDSIVA = "endSiva"; //$NON-NLS-1$

	public static final String TAG_QUIZ_LINEAR = "showQuizLinear"; //$NON-NLS-1$
	public static final String TAG_QUIZ_TESTPROPERTIES = "testProperties"; //$NON-NLS-1$
	public static final String TAG_QUIZ_TASKLIST = "taskList"; //$NON-NLS-1$
	public static final String TAG_QUIZ_POINTRANGE = "pointRange"; //$NON-NLS-1$
	public static final String TAG_QUIZ_RANGE = "range"; //$NON-NLS-1$
	public static final String TAG_QUIZ_TASK = "task"; //$NON-NLS-1$
	public static final String TAG_QUIZ_ANSWER = "answer"; //$NON-NLS-1$
	public static final String TAG_QUIZ_PATH = "path"; //$NON-NLS-1$
	public static final String TAG_QUIZ_POINT = "point"; //$NON-NLS-1$
	public static final String TAG_QUIZ_YPOS = "yPos"; //$NON-NLS-1$
	public static final String TAG_QUIZ_XPOS = "xPos"; //$NON-NLS-1$
	public static final String TAG_QUIZ_YSIZE = "ySize"; //$NON-NLS-1$
	public static final String TAG_QUIZ_XSIZE = "xSize"; //$NON-NLS-1$
	public static final String TAG_QUIZ_TIME = "time"; //$NON-NLS-1$
	public static final String TAG_QUIZ_TIMEOFFEEDBACK = "timeOfFeedback"; //$NON-NLS-1$
	public static final String TAG_QUIZ_MAXPOINTS = "maxPoints"; //$NON-NLS-1$
	public static final String VAL_QUIZ_QUESTION = "_Question_"; //$NON-NLS-1$
	public static final String ATTR_QUIZ_ANSWER_ID = "answerID"; //$NON-NLS-1$
	public static final String VAL_QUIZ_ANSWER_ID = "AID"; //$NON-NLS-1$
	public static final String VAL_QUIZ_ANSWER = "_Answer_"; //$NON-NLS-1$
	public static final String ATTR_QUIZ_IS_CORRECT = "isCorrect"; //$NON-NLS-1$
	public static final String ATTR_QUIZ_TASK_ID = "taskId"; //$NON-NLS-1$
	public static final String VAL_QUIZ_TASK_ID = "TID_"; //$NON-NLS-1$
	public static final String ATTR_QUIZ_MIN_VALUE = "minValue"; //$NON-NLS-1$
	public static final String ATTR_QUIZ_MAX_VALUE = "maxValue"; //$NON-NLS-1$
	public static final String ATTR_QUIZ_RANGE_ID = "rangeID"; //$NON-NLS-1$
	public static final String VAL_QUIZ_RANGE_ID = "RangeID_"; //$NON-NLS-1$
	public static final String ATTR_QUIZ_POSITION = "position"; //$NON-NLS-1$
	public static final String ATTR_QUIZ_RANDOM = "random"; //$NON-NLS-1$
	public static final String ATTR_QUIZ_POINTS = "points"; //$NON-NLS-1$

	// ----- <tableOfContent> ---------------------------

	public static final String TAG_TOC_ROOT = "tableOfContents"; //$NON-NLS-1$
	public static final String TAG_TOC_CONTENTS = "contents"; //$NON-NLS-1$
	public static final String TAG_TOC_ADJACENCY_LIST = "adjacencyRefListNode"; //$NON-NLS-1$
	public static final String ATTR_TOC_CONTENTSNODEID = "contentsNodeID"; //$NON-NLS-1$
	public static final String ATTR_TOC_REF_CONTENTSNODEID = "REFcontentsNodeID"; //$NON-NLS-1$

	// ----- <index> ----------------------------------

	public static final String TAG_INDEX = "index"; //$NON-NLS-1$
	public static final String TAG_INDEX_KEYWORD = "keyword"; //$NON-NLS-1$
	public static final String ATTR_KEYWORD_WORD = "word"; //$NON-NLS-1$
	public static final String ATTR_REF_TRIGGER_ID = "REFtriggerID"; //$NON-NLS-1$
	public static final String ATTR_RESSOURCE_TYPE = "ressourceType"; //$NON-NLS-1$

	// ------ GLOBAL: REFERENCES -------------------------------------------

	public static final String ATTR_REF_RES_ID = "REFresID"; //$NON-NLS-1$
	public static final String ATTR_REF_RES_ID_SECONDARY = "REFresIDsec"; //$NON-NLS-1$
	public static final String ATTR_REF_ACTION_ID = "REFactionID"; //$NON-NLS-1$
	public static final String ATTR_REF_LANGCODE = "langCode"; //$NON-NLS-1$
	public static final String VAL_TIME_NULL = "00:00:00.000"; //$NON-NLS-1$
	public static final String VAL_POINT_NULL = "-1.0"; //$NON-NLS-1$
	public static final String VAL_NO_PROJECT_TITLE = "NoProjectTitle"; //$NON-NLS-1$

	// ========================================================================
}
