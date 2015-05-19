package org.iviPro.theme;

import java.net.URL;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

public enum Icons {

	// ################################################################
	// # . . . ICONS FUER DIE HAUPT-TOOLBAR . . . . . . . . . . . . . #
	// ################################################################
	TOOLBAR_PROJECT_NEW("toolbar-project-new.png"), //$NON-NLS-1$
	TOOLBAR_PROJECT_EXPORT("toolbar-project-export.png"), //$NON-NLS-1$
	TOOLBAR_PROJECT_OPEN("toolbar-project-open.png"), //$NON-NLS-1$
	TOOLBAR_PROJECT_SAVE("toolbar-project-save.png"), //$NON-NLS-1$
	TOOLBAR_PROJECT_SAVE_AS("toolbar-project-save-as.png"), TOOLBAR_UNDO( //$NON-NLS-1$
			"toolbar-undo.png"), TOOLBAR_REDO("toolbar-redo.png"), TOOLBAR_EDITOR_SCENE_OPEN( //$NON-NLS-1$ //$NON-NLS-2$
			"toolbar-editor-scene.png"), //$NON-NLS-1$
	TOOLBAR_EDITOR_SCENEGRAPH_OPEN("toolbar-editor-scenegraph.png"), //$NON-NLS-1$
	TOOLBAR_EDITOR_SCENEDETECTION_OPEN("toolbar-editor-scenedetection"), //$NON-NLS-1$
	TOOLBAR_MEDIA_LOAD("toolbar-media-load.png"), //$NON-NLS-1$
	TOOLBAR_MEDIA_DELETE("toolbar-media-delete.png"), //$NON-NLS-1$
	TOOLBAR_TOC("toolbar-table-of-content.png"), //$NON-NLS-1$
	TOOLBAR_SETTINGS("toolbar-settings.png"), //$NON-NLS-1$
	TOOLBAR_GRAPHVALIDATION("toolbar-graphvalidation.png"), //$NON-NLS-1$
	TOOLBAR_GLOBAL_ANNOS("toolbar-global-annos.png"), //$NON-NLS-1$

	// ################################################################
	// # . . . MENU ACTIONS . . . . . . . . . . . . . . . . . . . . . #
	// ################################################################
	ACTION_APPLICATION_QUIT("action-application-quit.png"), //$NON-NLS-1$
	ACTION_PROJECT_NEW("action-project-new.png"), //$NON-NLS-1$
	ACTION_PROJECT_OPEN("action-project-open.png"), //$NON-NLS-1$
	ACTION_PROJECT_SAVE("action-project-save.png"), ACTION_PROJECT_SAVE_AS( //$NON-NLS-1$
			"action-project-save-as.png"), ACTION_PROJECT_EXPORT( //$NON-NLS-1$
			"action-project-export.png"), //$NON-NLS-1$
	ACTION_PROJECT_HANDOVER("action-project-handover.png"), //$NON-NLS-1$
	ACTION_PROJECT_CLOSE("action-project-close.png"), //$NON-NLS-1$
	ACTION_MEDIA_LOAD("action-media-load.png"), //$NON-NLS-1$
	ACTION_MEDIA_DELETE("trashcan.png"), //$NON-NLS-1$
	ACTION_TABLE_OF_CONTENTS("action-table-of-contents.png"), //$NON-NLS-1$
	ACTION_PROJECTSETTINGS("action-settings.png"), //$NON-NLS-1$
	ACTION_GRAPHVALIDATION("graph-validation.png"), //$NON-NLS-1$

	// ################################################################
	// # . . . ICONS FUER ACTIONS IM ANNOTATIONS-EDITOR . . . . . . . #
	// ################################################################
	ACTION_ANNOTATION_DELETE("trashcan.png"), //$NON-NLS-1$
	ACTION_ANNOTATION_SAVE("action-annotation-save.png"), ACTION_ANNOTATION_CUT_START( //$NON-NLS-1$
			"action-annotation-set-start.png"), //$NON-NLS-1$	
	ACTION_ANNOTATION_CUT_END("action-annotation-set-end.png"), //$NON-NLS-1$		
	ACTION_ANNOTATION_CREATE("action-media-load.png"), ACTION_GLOBAL_ANNOTATION_DELETE( //$NON-NLS-1$
			"trashcan.png"), //$NON-NLS-1$

	// ################################################################
	// # . . . ICONS FUER ACTIONS IM SZENEN-EDITOR . . . . . . . . . .#
	// ################################################################
	ACTION_SCENE_NEW("action-media-load.png"), //$NON-NLS-1$
	ACTION_SCENE_DELETE("trashcan.png"), //$NON-NLS-1$
	ACTION_SCENE_MERGE("action-scene-merge.png"), //$NON-NLS-1$
	ACTION_SCENE_CUT_START("action-scene-set-start.png"), //$NON-NLS-1$	
	ACTION_SCENE_CUT_END("action-scene-set-end.png"), //$NON-NLS-1$	
	ACTION_SCENE_SAVE("action-annotation-save.png"), //$NON-NLS-1$	
	ACTION_SCENE_CREATE_SAVE("action-annotation-save.png"), //$NON-NLS-1$

	// ################################################################
	// # . . . ICONS FUER ACTIONS ZUM ÖFFNEN VON EDITOREN/VIEWER ETC .#
	// # . . . HAUPTSÄCHLICH FÜR POPUP-MENÜ IN DEN REPOSITORIEN . . . #
	// ################################################################
	ACTION_EDITOR_IMAGE("action-editor-image.png"), //$NON-NLS-1$
	ACTION_EDITOR_ANNOTATION("action-editor-annotation.png"), //$NON-NLS-1$
	ACTION_EDITOR_RICHTEXT("action-editor-richtext.png"), //$NON-NLS-1$	
	ACTION_EDITOR_SUBTITLE("action-editor-subtitle.png"), //$NON-NLS-1$		
	ACTION_EDITOR_SCENE("action-editor-scene.png"), //$NON-NLS-1$	
	ACTION_EDITOR_SCENEGRAPH("action-editor-scenegraph.png"), //$NON-NLS-1$		
	ACTION_EDITOR_SCENEDETECTION("action-editor-scenedetection.png"), //$NON-NLS-1$	
	ACTION_EDITOR_MEDIAPLAYER("action-editor-mediaplayer.png"), //$NON-NLS-1$	
	ACTION_MEDIA_VIEW("action-media-view.png"), //$NON-NLS-1$
	ACTION_RENAME("action-rename.png"), //$NON-NLS-1$

	// ################################################################
	// # . . . ICONS ALS SINNBILDER FUER OBJEKT-TYPEN . . . . . . . . #
	// ################################################################
	OBJECT_ANNOTATION("object-annotation.png"), //$NON-NLS-1$
	OBJECT_MEDIA_AUDIO("object-media-audio.png"), //$NON-NLS-1$
	OBJECT_MEDIA_PICTURE("object-media-picture.png"), //$NON-NLS-1$
	OBJECT_MEDIA_VIDEO("object-media-video.png"), //$NON-NLS-1$
	OBJECT_MEDIA_TEXT_PLAIN("object-media-text-plain.png"), //$NON-NLS-1$
	OBJECT_MEDIA_TEXT_RICH("object-media-text-rich.png"), //$NON-NLS-1$
	OBJECT_MEDIA_TEXT_SUBTITLE("object-media-text-subtitle.png"), //$NON-NLS-1$
	OBJECT_MEDIA_PDF("object-media-pdf.png"), //$NON-NLS-1$	

	// ################################################################
	// # . . . TAB ICONS FÜR EDITOREN . . . . . . . . . . . . . . . . #
	// ################################################################
	EDITOR_SCENE("editor-scene.png"), //$NON-NLS-1$
	EDITOR_SCENEGRAPH("editor-scenegraph.png"), //$NON-NLS-1$
	EDITOR_RICHTEXT("editor-richtext.png"), //$NON-NLS-1$
	EDITOR_SUBTITLE("editor-richtext.png"), //$NON-NLS-1$
	EDITOR_IMAGE("editor-image.png"), //$NON-NLS-1$
	EDITOR_MEDIAPLAYER("editor-mediaplayer.png"), //$NON-NLS-1$
	EDITOR_ANNOTATION("editor-annotation.png"), //$NON-NLS-1$
	EDITOR_TOC("editor-table-of-content.png"), //$NON-NLS-1$

	// ################################################################
	// # . . . TAB ICONS FÜR VIEWS . . . . . . . . . . . . . . . . . #
	// ################################################################
	VIEW_MINIATUREGRAPH("view-miniaturegraph.png"), //$NON-NLS-1$
	VIEW_SCENEREPOSITORY("view-scenerepository.png"), //$NON-NLS-1$
	VIEW_SCENEREPOSITORY_ITEM_SCENE("view-scenerepository-item-scene.png"), //$NON-NLS-1$
	VIEW_MEDIAREPOSITORY("view-mediarepository.png"), //$NON-NLS-1$
	VIEW_INFORMATION("view-information.png"), //$NON-NLS-1$
	VIEW_ANNOTATIONREPOSITORY("view-annotationrepository.png"), //$NON-NLS-1$
	VIEW_FOLDER("Folder.png"), //$NON-NLS-1$

	// ################################################################
	// # . . . GRUPPEN ICONS FÜR REPOSITORIEN. . . . . . . . . . . . #
	// ################################################################
	VIEW_SCENEREPOSITORY_GROUP_USED("view-scenerepository-group-used.png"), //$NON-NLS-1$
	VIEW_SCENEREPOSITORY_GROUP_UNUSED("view-scenerepository-group-unused.png"), //$NON-NLS-1$
	VIEW_MEDIAREPOSITORY_GROUP_AUDIO("view-mediarepository-group-audio.png"), //$NON-NLS-1$
	VIEW_MEDIAREPOSITORY_GROUP_PICTURE("view-mediarepository-group-picture.png"), //$NON-NLS-1$
	VIEW_MEDIAREPOSITORY_GROUP_VIDEO("view-mediarepository-group-video.png"), //$NON-NLS-1$
	VIEW_MEDIAREPOSITORY_GROUP_TEXT("view-mediarepository-group-text.png"), //$NON-NLS-1$
	VIEW_MEDIAREPOSITORY_GROUP_SUBTITLE("view-mediarepository-group-subtitle.png"), //$NON-NLS-1$
	VIEW_MEDIAREPOSITORY_GROUP_PDF("view-mediarepository-group-pdf.png"), //$NON-NLS-1

	// ################################################################
	// # . . . MEDIENTYP ICONS FÜR REPOSITORIEN. . . . . . . . . . . #
	// ################################################################
	VIEW_MEDIAREPOSITORY_ITEM_AUDIO("view-mediarepository-item-audio.png"), //$NON-NLS-1$
	VIEW_MEDIAREPOSITORY_ITEM_AUDIOPART(
			"view-mediarepository-item-audio-cut.png"), VIEW_MEDIAREPOSITORY_ITEM_PICTURE( //$NON-NLS-1$
			"view-mediarepository-item-picture.png"), //$NON-NLS-1$
	VIEW_MEDIAREPOSITORY_ITEM_VIDEO("view-mediarepository-item-video.png"), //$NON-NLS-1$
	VIEW_MEDIAREPOSITORY_ITEM_TEXT("view-mediarepository-item-text.png"), //$NON-NLS-1$
	VIEW_MEDIAREPOSITORY_ITEM_SUBTITLE("view-mediarepository-item-subtitle.png"), //$NON-NLS-1$
	VIEW_MEDIAREPOSITORY_ITEM_PDF("view-mediarepository-item-pdf.png"), //$NON-NLS-1$	

	// ################################################################
	// # . . . ICONS DES MEDIAPLAYER . . . . . . . . . . . . . . . . #
	// ################################################################
	MEDIAPLAYER_PLAY("action-mediaplayer-play.png"), //$NON-NLS-1$
	MEDIAPLAYER_PAUSE("action-mediaplayer-pause.png"), //$NON-NLS-1$
	MEDIAPLAYER_STOP("action-mediaplayer-stop.png"), //$NON-NLS-1$
	MEDIAPLAYER_MUTE("action-mediaplayer-mute.png"), //$NON-NLS-1$
	MEDIAPLAYER_UNMUTE("action-mediaplayer-unmute.png"), //$NON-NLS-1$	
	MEDIAPLAYER_VOLUME_UP("action-mediaplayer-volume-up.png"), //$NON-NLS-1$	
	MEDIAPLAYER_VOLUME_DOWN("action-mediaplayer-volume-down.png"), //$NON-NLS-1$
	MEDIAPLAYER_FORWARD_FRAME("action-mediaplayer-forward-frame.png"), //$NON-NLS-1$
	MEDIAPLAYER_FORWARD_LITTLE("action-mediaplayer-forward-little.png"), //$NON-NLS-1$
	MEDIAPLAYER_FORWARD_MUCH("action-mediaplayer-forward-much.png"), //$NON-NLS-1$
	MEDIAPLAYER_FORWARD_END("action-mediaplayer-forward-end.png"), //$NON-NLS-1$
	MEDIAPLAYER_REWIND_FRAME("action-mediaplayer-rewind-frame.png"), //$NON-NLS-1$
	MEDIAPLAYER_REWIND_LITTLE("action-mediaplayer-rewind-little.png"), //$NON-NLS-1$
	MEDIAPLAYER_REWIND_MUCH("action-mediaplayer-rewind-much.png"), //$NON-NLS-1$
	MEDIAPLAYER_REWIND_START("action-mediaplayer-rewind-start.png"), //$NON-NLS-1$		

	//
	// ################################################################
	// # . . . ICONS FUER DEN SZENEN-GRAPH. . . . . . . . . . . . . . #
	// ################################################################
	GRAPH_ANNOTATIONS_EXISTENT("graph-annotations-existent.png"), //$NON-NLS-1$
	GRAPH_ANNOTATIONS_NONEXISTENT("graph-annotations-nonexistent.png"), //$NON-NLS-1$
	GRAPH_ANNOTATION_POSITION_LEFT("graph-annotation-position-left.png"), //$NON-NLS-1$
	GRAPH_ANNOTATION_POSITION_RIGHT("graph-annotation-position-right.png"), //$NON-NLS-1$
	GRAPH_ANNOTATION_POSITION_TOP("graph-annotation-position-top.png"), //$NON-NLS-1$
	GRAPH_ANNOTATION_POSITION_BOTTOM("graph-annotation-position-bottom.png"), //$NON-NLS-1$
	GRAPH_ANNOTATION_POSITION_OVERLAY("graph-annotation-position-overlay.png"), //$NON-NLS-1$
	GRAPH_CONTEXTMENU_SEMANTICZOOMIN("graph-contextmenu-semanticzoomin.png"), //$NON-NLS-1$
	GRAPH_CONTEXTMENU_SEMANTICZOOMOUT("graph-contextmenu-semanticzoomout.png"), //$NON-NLS-1$
	GRAPH_ZOOMIN("graph-zoomin.png"), //$NON-NLS-1$
	GRAPH_ZOOMOUT("graph-zoomout.png"), //$NON-NLS-1$
	GRAPH_FISHEYE("graph-fisheye.png"), //$NON-NLS-1$
	GRAPH_SEMANTICZOOMIN("graph-semanticzoomin.png"), //$NON-NLS-1$
	GRAPH_SEMANTICZOOMOUT("graph-semanticzoomout.png"), //$NON-NLS-1$	
	GRAPH_ZOOM_BLACK_ONE("graph-zoom-black-one.png"), //$NON-NLS-1$
	GRAPH_ZOOM_BLACK_TWO("graph-zoom-black-two.png"), //$NON-NLS-1$
	GRAPH_ZOOM_BLACK_THREE("graph-zoom-black-three.png"), //$NON-NLS-1$
	GRAPH_ZOOM_WHITE_ONE("graph-zoom-white-one.png"), //$NON-NLS-1$
	GRAPH_ZOOM_WHITE_TWO("graph-zoom-white-two.png"), //$NON-NLS-1$
	GRAPH_ZOOM_WHITE_THREE("graph-zoom-white-three.png"), //$NON-NLS-1$
	GRAPH_ZOOM_NOFRAMEPREVIEW("graph-zoom-noframepreview.png"), //$NON-NLS-1$
	GRAPH_FISHEYE_NOFRAMEPREVIEW("graph-fisheye-noframepreview.png"), //$NON-NLS-1$
	GRAPH_CURSOR_ADDCONNECTION("cursor-graph-addconnection.png"), //$NON-NLS-1$
	GRAPH_CURSOR_DELETE("cursor-graph-delete.png"), //$NON-NLS-1$
	GRAPH_TOOL_SELECT("graph-tool-select.png"), //$NON-NLS-1$	
	GRAPH_TOOL_ADDCONNECTION("graph-tool-addconnection.png"), //$NON-NLS-1$	
	GRAPH_TOOL_FISHEYE("graph-tool-fisheye.png"), //$NON-NLS-1$	
	GRAPH_TOOL_DELETE("graph-tool-delete.png"), //$NON-NLS-1$
	GRAPH_TOOL_SELECTION("graph-tool-selection.png"), //$NON-NLS-1$
	GRAPH_TOOL_SELECTION_ALTERNATIVE("graph-tool-selection-alternative.png"), //$NON-NLS-1$
	GRAPH_TOOL_COND_SELECTION("graph-tool-cond-selection.png"), //$NON-NLS-1$
	GRAPH_TOOL_COND_SELECTION_ALTERNATIVE("graph-tool-cond-selection-alternative.png"), //$NON-NLS-1$
	GRAPH_TOOL_QUIZ("graph-tool-quiz.png"), //$NON-NLS-1$
	GRAPH_TOOL_QUIZ_ALTERNATIVE("graph-tool-quiz-alternative.png"), //$NON-NLS-1$
	GRAPH_TOOL_RANDOMSELECTION("graph-tool-random.png"), //$NON-NLS-1$
	GRAPH_TOOL_RESUME("graph-tool-resume.png"), //$NON-NLS-1$
	GRAPH_TOOLBAR("graph-toolbar.png"), //$NON-NLS-1$			

	// ################################################################
	// # . . . BILDER FUER DIALOGE. . . . . . . . . . . . . . . . . . #
	// ################################################################
	DIALOG_PROJECTCREATEWIZARD_TITLE("dialog-projectcreatewizard-title.png"), //$NON-NLS-1$

	//
	// ################################################################
	// # . . . ICONS FUER den Image Editor. . . . . . . . . . . . . . #
	// ################################################################
	IMAGE_EDITOR_SELECTION("imageeditor_pfeil.png"), IMAGE_EDITOR_ELLIPSE( //$NON-NLS-1$
			"imageeditor_ellipse.png"), IMAGE_EDITOR_FORBACK( //$NON-NLS-1$
			"imageeditor_ForBackGround.png"), IMAGE_EDITOR_TEXT( //$NON-NLS-1$
			"imageeditor_text.png"), IMAGE_EDITOR_RECTANGLE( //$NON-NLS-1$
			"imageeditor_rechteck.png"), IMAGE_EDTIOR_DELETE("trashcan.png"), IMAGE_EDITOR_SAVE( //$NON-NLS-1$ //$NON-NLS-2$
			"imageeditor_save.png"), //$NON-NLS-1$

	// ################################################################
	// # . . . Sortierrichtung. . . . . . . . . . . . . . . . . . . . #
	// ################################################################
	SORT_DIRECTION_ASC("sort_asc.png"), SORT_DIRECTION_DESC("sort_desc.png"), //$NON-NLS-1$ //$NON-NLS-2$

	// ################################################################
	// # . . . Szenenerkennung. . . . . . . . . . . . . . . . . . . . #
	// ################################################################
	MERGE_SHOT("shot_connect.png"), INSERT_SHOT("shot_cut.png"), SHOT_EDITOR( //$NON-NLS-1$ //$NON-NLS-2$
			"shot-view-information.png"), //$NON-NLS-1$
			
	// ################################################################
	// # . . . SINNBILD FUER FEHLENDE ICON-DATEIEN. . . . . . . . . . #
	// ################################################################
	RICHEDITOR_COLOR("richeditor-color.png"), //$NON-NLS-1$	
	RICHEDITOR_BOLD("richeditor-bold.png"), //$NON-NLS-1$
	RICHEDITOR_ITALIC("richeditor-italic.png"), //$NON-NLS-1$
	RICHEDITOR_UNDERLINE("richeditor-underline.png"), //$NON-NLS-1$
	RICHEDITOR_CUT("richeditor-cut.png"), //$NON-NLS-1$
	RICHEDITOR_COPY("richeditor-copy.png"), //$NON-NLS-1$
	RICHEDITOR_PASTE("richeditor-paste.png"), //$NON-NLS-1$
	RICHEDITOR_ALIGN_LEFT("richeditor-left.png"), //$NON-NLS-1$
	RICHEDITOR_ALIGN_RIGHT("richeditor-right.png"), //$NON-NLS-1$
	RICHEDITOR_ALIGN_CENTER("richeditor-center.png"), //$NON-NLS-1$
	RICHEDITOR_ALIGN_JUSTIFY("richeditor-justify.png"), //$NON-NLS-1$

	// ################################################################
	// # . . . SINNBILD FUER FEHLENDE ICON-DATEIEN. . . . . . . . . . #
	// ################################################################
	DEFAULT("default.png"); //$NON-NLS-1$	

	/**
	 * Verzeichnis im Projekt in dem die Icons liegen.
	 */
	private static final String ICON_RESOURCE_DIR = "org/iviPro/theme/icons/"; //$NON-NLS-1$

	/**
	 * Logger
	 */
	private static Logger logger = Logger.getLogger(Icons.class);

	/**
	 * Dateiname des Icons
	 */
	private String filename = ""; //$NON-NLS-1$

	/**
	 * Image-Descriptor fuer dieses Icon.
	 */
	private ImageDescriptor imageDescriptor = null;

	/**
	 * ImageDescriptor fuer das ausgegraute Icon
	 */
	private ImageDescriptor disabledImageDescriptor = null;

	/**
	 * Ausgegrautes Icon
	 */
	private Image disabledImage = null;

	/**
	 * Erstellt ein neues Icon mit einem bestimmten Dateinamen
	 * 
	 * @param filename
	 */
	Icons(String filename) {
		this.filename = filename;
	}

	/**
	 * Liefert einen SWT-Image-Descriptor fuer dieses Icon.
	 * 
	 * @return Der SWT ImageDescriptor fuer dieses Icon.
	 */
	public ImageDescriptor getImageDescriptor() {

		// Wenn noch kein ImageDescriptor gesetzt wurde, dann
		// erstellen wir erst einen.
		if (imageDescriptor == null) {
			// URL fuer Ressource ueber ClassLoader suchen
			ClassLoader loader = Icons.class.getClassLoader();

			URL url = loader.getResource(ICON_RESOURCE_DIR + filename);

			// ImageDescriptor anhand der URL setzen
			if (url == null && this != DEFAULT) {
				// Die Resource konnte nicht gefunden werden, wir nehmen dann
				// einfach das Default-Icon
				logger.error("Icon not found: " + filename); //$NON-NLS-1$
				imageDescriptor = DEFAULT.getImageDescriptor();
			} else {
				// Die Resource wurde gefunden und wir koennen einen
				// ImageDescriptor fuer das Icon erstellen.
				imageDescriptor = ImageDescriptor.createFromURL(url);
			}
		}
		// Jetzt sollte ein ImageDescriptor verfuegbar sein, den wir nun zurueck
		// geben koennen
		return imageDescriptor;
	}

	/**
	 * Liefert einn Swing-ImageIcon fuer dieses Icon.
	 * 
	 * @return Das Swing ImageIcon fuer dieses Icon.
	 */
	public ImageIcon getImageIcon() {
		// URL fuer Ressource ueber ClassLoader suchen
		ClassLoader loader = Icons.class.getClassLoader();
		URL url = loader.getResource(ICON_RESOURCE_DIR + filename);
		// ImageDescriptor anhand der URL setzen
		if (url == null && this != DEFAULT) {
			// Die Resource konnte nicht gefunden werden, wir nehmen dann
			// einfach das Default-Icon
			logger.error("Icon not found: " + filename); //$NON-NLS-1$
			return DEFAULT.getImageIcon();
		} else {
			// Erstellt ein neuen ImageIcon aus dem angegebenen Pfad
			return new ImageIcon(url);
		}
	}

	/**
	 * Lieft ein SWT-Image fuer dieses Icon.
	 * 
	 * @return SWT-Image fuer dieses Icon.
	 */
	public Image getImage() {
		return getImageDescriptor().createImage();
	}

	/**
	 * Gibt einen ImageDescriptor fuer eine ausgegraute Version dieses Icons
	 * zurueck.
	 * 
	 * @return ImageDescriptor fuer eine ausgegraute Version dieses Icons.
	 */
	public ImageDescriptor getDisabledImageDescriptor() {
		if (disabledImageDescriptor == null) {
			disabledImageDescriptor = new ImageDescriptor() {

				@Override
				public ImageData getImageData() {
					return getDisabledImage().getImageData();
				}

			};
		}
		return disabledImageDescriptor;
	}

	/**
	 * Gibt eine ausgegraute Version des Icons zurueck.
	 * 
	 * @return Ausgegraute Version des Icons.
	 */
	private Image getDisabledImage() {
		if (disabledImage == null) {
			Display display = Display.getDefault();
			Image tmpImage = new Image(display, getImage(), SWT.IMAGE_GRAY);
			ImageData data = tmpImage.getImageData();
			int pos = 0;
			for (int x = 0; x < data.width; x++) {
				for (int y = 0; y < data.height; y++) {
					int pixel = data.getPixel(x, y);
					if (pixel != 0) {
						data.alphaData[pos] = (byte) 128;
					} else {
						data.setPixel(x, y, data.transparentPixel);
					}
					pos++;
				}
			}
			disabledImage = new Image(display, data);

		}
		return disabledImage;
	}
}
