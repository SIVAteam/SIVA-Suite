package org.iviPro.theme.colorprovider;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.iviPro.theme.Colors;

/**
 * Default Color-Provider der immer die System-Ressourcen benutzt.
 * @author dellwo
 */
public class SystemColorProvider implements IColorProvider {

	private static final Logger logger = Logger
			.getLogger(DefaultColorProvider.class);

	private static Display display = Display.getDefault();

	private static final Color COLOR_RED = display
			.getSystemColor(SWT.COLOR_RED);
	protected static final Color COLOR_BLACK = display
			.getSystemColor(SWT.COLOR_BLACK);
	protected static final Color COLOR_WHITE = display
			.getSystemColor(SWT.COLOR_WHITE);
	private static final Color COLOR_WIDGET_FOREGROUND = display
			.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
	private static final Color COLOR_WIDGET_BACKGROUND = display
			.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	private static final Color COLOR_LIST_BACKGROUND = display
			.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	private static final Color COLOR_LIST_SELECTION = display
			.getSystemColor(SWT.COLOR_LIST_SELECTION);
	private static final Color COLOR_LIST_FOREGROUND = display
			.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
	private static final Color COLOR_TITLE_INACTIVE_BACKGROUND = display
			.getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
	private static final Color COLOR_TITLE_BACKGROUND = display
			.getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
	private static final Color COLOR_TITLE_INACTIVE_FOREGROUND = display
			.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
	private static final Color COLOR_TITLE_FOREGROUND = display
			.getSystemColor(SWT.COLOR_TITLE_FOREGROUND);
	private static final Color COLOR_WIDGET_LIGHT_SHADOW = display
			.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
	private static final Color COLOR_WIDGET_DARK_SHADOW = display
			.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
	private static final Color COLOR_WIDGET_BORDER = display
			.getSystemColor(SWT.COLOR_WIDGET_BORDER);
	private static final Color DEVELOPER_TEST_BG = display
			.getSystemColor(SWT.COLOR_GREEN);
	private static final Color UNKNOWN_COLOR = display
			.getSystemColor(SWT.COLOR_RED);

	@Override
	public Color getColor(Colors colorType) {
		switch (colorType) {

		// Generelle Default-Farben
		case DEFAULT_FONT_COLOR:
			return COLOR_WIDGET_FOREGROUND;
		case DEFAULT_VIDEOFRAME_BACKGROUND:
			return COLOR_BLACK;

			// Editoren-Standard-Farben
		case EDITOR_BG:
			return COLOR_WIDGET_BACKGROUND;
		case EDITOR_TABSINGLE_HEADER_BG:
			return COLOR_TITLE_BACKGROUND;
		case EDITOR_TABSINGLE_HEADER_FG:
			return COLOR_TITLE_FOREGROUND;
		case EDITOR_TABSINGLE_CONTENT_BG:
			return COLOR_WIDGET_BACKGROUND;

			// Annotations-Editor
		case ANNOTATIONEDITOR_SETOVERLAYWINDOW_BG:
			return COLOR_WIDGET_BACKGROUND;
		case ANNOTATIONEDITOR_SETOVERLAYWINDOW_FG:
			return COLOR_RED;
		case ANNOTATIONEDITOR_POSSELECTOR_BG_SELECTED:
			return COLOR_LIST_SELECTION;
		case ANNOTATIONEDITOR_POSSELECTOR_FG:
			return COLOR_LIST_FOREGROUND;

			// Video-Timeline
		case VIDEO_TIMELINE_BG:
			return COLOR_WIDGET_DARK_SHADOW;
		case VIDEO_TIMELINE_MARKER:
			return COLOR_LIST_SELECTION;
		case VIDEO_TIMELINE_THUMB_BG:
			return COLOR_WIDGET_LIGHT_SHADOW;
		case VIDEO_TIMELINE_THUMB_BORDER:
			return COLOR_WIDGET_BACKGROUND;

			// Video-Slider
		case SLIDER_BG:
			return COLOR_LIST_BACKGROUND;
		case SLIDER_BORDER:
			return COLOR_WIDGET_BORDER;
		case SLIDER_PROGRESSBAR_CURRENT:
			return COLOR_LIST_SELECTION;
		case SLIDER_PROGRESSBAR_PLAYED:
			return COLOR_WIDGET_LIGHT_SHADOW;

			// Video-Skala
		case VIDEO_SCALE_MARKED:
			return COLOR_LIST_SELECTION;
		case VIDEO_SCALE_TICKS:
			return COLOR_WIDGET_FOREGROUND;
		case VIDEO_SCALE_FONT:
			return COLOR_WIDGET_FOREGROUND;

			// Video-Overview (=Szenen/Annotaions-Uebersicht)
		case VIDEO_OVERVIEW_BG:
			return COLOR_LIST_BACKGROUND;
		case VIDEO_OVERVIEW_ITEM_BG:
			return COLOR_WIDGET_DARK_SHADOW;
		case VIDEO_OVERVIEW_ITEM_BG_SELECTED:
			return COLOR_LIST_SELECTION;
		case VIDEO_OVERVIEW_ITEM_BORDER:
			return COLOR_WIDGET_BORDER;
		case VIDEO_OVERVIEW_ITEM_FONT:
			return COLOR_WIDGET_FOREGROUND;
		case VIDEO_OVERVIEW_ITEM_BG_HOVER:
			return COLOR_WIDGET_LIGHT_SHADOW;

			// Volume-Slider
		case VOLUME_SLIDER_BG_SELECTED:
			return COLOR_WIDGET_LIGHT_SHADOW;
		case VOLUME_SLIDER_BORDER:
			return COLOR_WIDGET_BORDER;
		case VOLUME_SLIDER_MARKER:
			return COLOR_LIST_SELECTION;

			// Szenen-Graph
		case GRAPH_SCENENODE_BG:
			return COLOR_WIDGET_BACKGROUND;
		case GRAPH_SCENENODE_BG_SELECTED:
			return COLOR_WIDGET_BACKGROUND;
		case GRAPH_SCENENODE_HEADER_BG:
			return COLOR_TITLE_INACTIVE_BACKGROUND;
		case GRAPH_SCENENODE_HEADER_BG_SELECTED:
			return COLOR_TITLE_BACKGROUND;
		case GRAPH_SCENENODE_HEADER_FG:
			return COLOR_TITLE_FOREGROUND;
		case GRAPH_SCENENODE_HEADER_FG_SELECTED:
			return COLOR_TITLE_INACTIVE_FOREGROUND;
		case GRAPH_SCENENODE_TOOLBAR_BG:
			return COLOR_WIDGET_LIGHT_SHADOW;
		case GRAPH_SCENENODE_TOOLBAR_BG_SELECTED:
			return COLOR_WIDGET_LIGHT_SHADOW;
		case GRAPH_SCENENODE_BORDER:
			return COLOR_WIDGET_BORDER;
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
			return DEVELOPER_TEST_BG;
		default:
			logger.error("Color unknown: " + colorType.toString()); //$NON-NLS-1$
			return UNKNOWN_COLOR;

		}
	}
}
