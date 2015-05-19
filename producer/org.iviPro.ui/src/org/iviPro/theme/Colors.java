package org.iviPro.theme;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.iviPro.theme.colorprovider.IColorProvider;

/**
 * Klasse ueber die saemtliche in SIVA verwendeten Farben zugaenglich sind.
 * 
 * @author dellwo
 * 
 */
public enum Colors {

	//
	// ################################################################
	// # . . . GENERELLE FARBEN . . . . . . . . . . . . . . ......... #
	// ################################################################
	DEFAULT_FONT_COLOR,
	DEFAULT_VIDEOFRAME_BACKGROUND,
	WARNING_BG,
	WARNING_FONT,
	

	// ################################################################
	// # . . . ANNOTATIONS-EDITOR . . . . . . . . . . . . . . . . . . #
	// ################################################################
	ANNOTATIONEDITOR_SETOVERLAYWINDOW_BG,
	ANNOTATIONEDITOR_SETOVERLAYWINDOW_FG,
	ANNOTATIONEDITOR_POSSELECTOR_BG_SELECTED,
	ANNOTATIONEDITOR_POSSELECTOR_FG,

	// ################################################################
	// # . . . EDITOREN DEFAULT-FARBEN. . . . . . . . . . . . . . . . #
	// ################################################################
	EDITOR_BG,
	EDITOR_TABSINGLE_CONTENT_BG,
	EDITOR_TABSINGLE_HEADER_FG,
	EDITOR_TABSINGLE_HEADER_BG,

	// ################################################################
	// # . . . SZENEN-GRAPH . . . . . . . . . . . . . . . . . . . . . #
	// ################################################################
	GRAPH_SCENENODE_BG,
	GRAPH_SCENENODE_BG_SELECTED,
	GRAPH_SCENENODE_BORDER,
	GRAPH_SCENENODE_HEADER_FG,
	GRAPH_SCENENODE_HEADER_FG_SELECTED,
	GRAPH_SCENENODE_HEADER_BG,
	GRAPH_SCENENODE_HEADER_BG_SELECTED,
	GRAPH_SCENENODE_TOOLBAR_BG,
	GRAPH_SCENENODE_TOOLBAR_BG_SELECTED,

	// ################################################################
	// # . . . SZENEN- UND ANNOTATIONSUEBERSICHT. . . . . . . . . . . #
	// ################################################################
	VIDEO_OVERVIEW_BG,
	VIDEO_OVERVIEW_ITEM_BG,
	VIDEO_OVERVIEW_ITEM_BG_SELECTED,
	VIDEO_OVERVIEW_ITEM_BG_HOVER,
	VIDEO_OVERVIEW_ITEM_FONT,
	VIDEO_OVERVIEW_ITEM_BORDER,

	// ################################################################
	// # . . . Video/Audio-SLIDER + Skala . . . . . . . . . . . . . . #
	// ################################################################
	SLIDER_BG,
	SLIDER_BORDER,
	SLIDER_PROGRESSBAR_CURRENT,
	SLIDER_PROGRESSBAR_PLAYED,
	VIDEO_SCALE_TICKS,
	VIDEO_SCALE_FONT,
	VIDEO_SCALE_MARKED,
	VOLUME_SLIDER_MARKER,
	VOLUME_SLIDER_BG_SELECTED,
	VOLUME_SLIDER_BORDER,

	// ################################################################
	// # . . . VIDEO-TIMELINE . . . . . . . . . . . . . . . . . . . . #
	// ################################################################
	VIDEO_TIMELINE_BG,
	VIDEO_TIMELINE_THUMB_BORDER,
	VIDEO_TIMELINE_THUMB_BG,
	VIDEO_TIMELINE_MARKER,

	// ################################################################
	// # . . . FARBEN NUR FUER ENTWICKLUNGSZWECKE . . . . . . . . . . #
	// ################################################################
	DEVELOPER_TEST_BG,

	// ################################################################
	// # . . . Skin Hintergrundfarben für den RichtextEditor. . . . . #
	// # . . . Fordergrundfarben entsprechen der Textfarbe  . . . . . #
	// ################################################################
	SKIN_SIMPLE_BG,
	SKIN_DARK_BG,
	SKIN_SIMPLE_FG,
	SKIN_DARK_FG;

	private static final Logger logger = Logger.getLogger(Colors.class);

	private static IColorProvider provider;

	/**
	 * Setzt den zu verwendenden Color-Provider der die Farben zur Verfuegung
	 * stellt. Ein Color-Provider muss gesetzt sein, damit Farb-Informationen
	 * abgerufen werden koennen.
	 * 
	 * @param provider
	 */
	public static void setColorProvider(IColorProvider provider) {
		logger.info("Setting color provider: " + provider); //$NON-NLS-1$
		Colors.provider = provider;
	}

	/**
	 * Gibt die SWT-Farbe dieses Colors-Objekts zurueck.
	 * 
	 * @return
	 */
	public Color getColor() {
		if (provider == null) {
			throw new NullPointerException(
					"ColorProvider is null. Set a color provider before calling this method"); //$NON-NLS-1$
		}
		return provider.getColor(this);
	}

	/**
	 * 
	 * @param widget
	 */
	public static void styleWidget(Control widget) {

	}

	/**
	 * 
	 * @param tabFolder
	 */
	public static void styleWidget(CTabFolder tabFolder) {


		tabFolder.setSelectionBackground(EDITOR_TABSINGLE_HEADER_BG.getColor());
		
		// Systemfarbe ist bei Windows weiß => kein Style => da schwarzer
		// Text gewollt
		//tabFolder.setSelectionForeground(EDITOR_TABSINGLE_HEADER_FG.getColor());
		
		tabFolder.setBackground(EDITOR_TABSINGLE_CONTENT_BG.getColor());
		
		tabFolder.setBackgroundMode(SWT.INHERIT_DEFAULT);
	}

}
