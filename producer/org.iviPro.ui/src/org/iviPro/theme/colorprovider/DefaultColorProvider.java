package org.iviPro.theme.colorprovider;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.iviPro.theme.Colors;

public class DefaultColorProvider extends SystemColorProvider {

	private static Display display = Display.getDefault();

	private static final Color LIGHT_RED = c(255, 100, 100);
	private static final Color ORANGE_BG_STRONG = c(231, 134, 20);
	private static final Color ORANGE_BG_NORMAL = c(239, 211, 178);
	private static final Color ORANGE_BG_LIGHT = c(244, 235, 224);
	private static final Color GREEN_BG_STRONG = c(114, 176, 41);
	private static final Color GREEN_BG_NORMAL = c(203, 239, 178);
	private static final Color GREEN_BG_LIGHT = c(100, 120, 80);
	private static final Color BLACK = c(SWT.COLOR_BLACK);
	private static final Color WHITE = c(SWT.COLOR_WHITE);
	private static final Color RED = c(SWT.COLOR_RED);
	private static final Color GREEN = c(SWT.COLOR_GREEN);
	private static final Color BLUE_BG_STRONG = c(153, 180, 209);
	private static final Color GRAY_BG_LIGHT = c(240, 240, 240);
	private static final Color GRAY_BG_STRONG = c(100, 100, 100);
	private static final Color GRAY_BG_DARK = c(60, 60, 60);

	private static final Color c(int swtColorId) {
		return display.getSystemColor(swtColorId);
	}

	private static final Color c(int red, int green, int blue) {
		return new Color(display, red, green, blue);
	}

	private static Logger logger = Logger
			.getLogger(DefaultColorProvider.class);

	public DefaultColorProvider() {
		display = Display.getDefault();
	}

	@Override
	public Color getColor(Colors colorType) {
		switch (colorType) {

		// Generelle Default-Farben
		case DEFAULT_FONT_COLOR:
			return BLACK;
		case DEFAULT_VIDEOFRAME_BACKGROUND:
			return BLACK;
		case WARNING_BG:
			return LIGHT_RED;
		case WARNING_FONT:
			return RED;

			// Editoren-Standard-Farben
		case EDITOR_BG:
			return WHITE;
		case EDITOR_TABSINGLE_HEADER_BG:
			return BLUE_BG_STRONG;
		case EDITOR_TABSINGLE_HEADER_FG:
			return WHITE;
		case EDITOR_TABSINGLE_CONTENT_BG:
			return GRAY_BG_LIGHT;

			// Annotations-Editor
		case ANNOTATIONEDITOR_SETOVERLAYWINDOW_BG:
			return WHITE;
		case ANNOTATIONEDITOR_SETOVERLAYWINDOW_FG:
			return RED;
		case ANNOTATIONEDITOR_POSSELECTOR_BG_SELECTED:
			return ORANGE_BG_STRONG;
		case ANNOTATIONEDITOR_POSSELECTOR_FG:
			return BLACK;

			// Video-Timeline
		case VIDEO_TIMELINE_BG:
			return GRAY_BG_DARK;
		case VIDEO_TIMELINE_MARKER:
			return ORANGE_BG_NORMAL;
		case VIDEO_TIMELINE_THUMB_BG:
			return BLACK;
		case VIDEO_TIMELINE_THUMB_BORDER:
			return GRAY_BG_STRONG;

			// Video-Slider
		case SLIDER_BG:
			return WHITE;
		case SLIDER_BORDER:
			return GRAY_BG_DARK;
		case SLIDER_PROGRESSBAR_CURRENT:
			return ORANGE_BG_STRONG;
		case SLIDER_PROGRESSBAR_PLAYED:
			return ORANGE_BG_NORMAL;

			// Video-Skala
		case VIDEO_SCALE_MARKED:
			return ORANGE_BG_NORMAL;
		case VIDEO_SCALE_TICKS:
			return BLACK;
		case VIDEO_SCALE_FONT:
			return BLACK;

			// Video-Overview (=Szenen/Annotaions-Uebersicht)
		case VIDEO_OVERVIEW_BG:
			return WHITE;
		case VIDEO_OVERVIEW_ITEM_BG:
			return BLUE_BG_STRONG;
		case VIDEO_OVERVIEW_ITEM_BG_SELECTED:
			return ORANGE_BG_NORMAL;
		case VIDEO_OVERVIEW_ITEM_BORDER:
			return GRAY_BG_DARK;
		case VIDEO_OVERVIEW_ITEM_FONT:
			return BLACK;
		case VIDEO_OVERVIEW_ITEM_BG_HOVER:
			return ORANGE_BG_NORMAL;

			// Volume-Slider
		case VOLUME_SLIDER_BG_SELECTED:
			return ORANGE_BG_NORMAL;
		case VOLUME_SLIDER_BORDER:
			return ORANGE_BG_STRONG;
		case VOLUME_SLIDER_MARKER:
			return ORANGE_BG_STRONG;

			// Szenen-Graph
		case GRAPH_SCENENODE_BG:
			return ORANGE_BG_LIGHT;
		case GRAPH_SCENENODE_BG_SELECTED:
			return GREEN_BG_LIGHT;
		case GRAPH_SCENENODE_HEADER_BG:
			return ORANGE_BG_STRONG;
		case GRAPH_SCENENODE_HEADER_BG_SELECTED:
			return GREEN_BG_STRONG;
		case GRAPH_SCENENODE_HEADER_FG:
			return WHITE;
		case GRAPH_SCENENODE_HEADER_FG_SELECTED:
			return WHITE;
		case GRAPH_SCENENODE_TOOLBAR_BG:
			return ORANGE_BG_NORMAL;
		case GRAPH_SCENENODE_TOOLBAR_BG_SELECTED:
			return GREEN_BG_NORMAL;
		case GRAPH_SCENENODE_BORDER:
			return BLACK;
		case SKIN_SIMPLE_BG:
			return COLOR_WHITE;
		case SKIN_DARK_BG:
			return COLOR_BLACK;
		case SKIN_SIMPLE_FG:
			return COLOR_BLACK;
		case SKIN_DARK_FG:
			return COLOR_WHITE;
			
			// Ansonsten...
		case DEVELOPER_TEST_BG:
			return GREEN;

		}
		logger.error("Missing color: " + colorType); //$NON-NLS-1$
		return super.getColor(colorType);
	}
}
