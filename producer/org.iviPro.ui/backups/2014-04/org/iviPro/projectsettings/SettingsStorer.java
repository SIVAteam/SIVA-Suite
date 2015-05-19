package org.iviPro.projectsettings;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.PreferencePage;
import org.iviPro.application.Application;
import org.iviPro.model.ProjectSettings;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.project.ChangeSettingsOperation;

public class SettingsStorer {

	private List<PreferencePage> pages;

	public SettingsStorer() {
		pages = new LinkedList<PreferencePage>();
	}

	public void addPage(PreferencePage page) {
		pages.add(page);
	}

	public void storeSettings() {
		ProjectSettings settings = Application.getCurrentProject()
				.getSettings();

		boolean newStartmode = false;
		int newSkin = -1;
		int newSizeWidth = -1;
		int newSizeHeight = -1;
		float newAreaLeftWidth = -1.0f;
		float newAreaRightWidth = -1.0f;
		float newAreaTopHeight = -1.0f;
		float newAreaBottomHeight = -1.0f;
		HashMap<String, String> export = null;
		boolean autoReload = false;
		int autoReloadTime = 0;
		int projectCollaborationID = -1;
		boolean fullSemanticZoomLevels = false;
		String name = ""; //$NON-NLS-1$
		String newDesignName = ""; //$NON-NLS-1$
		String newDesignSchema = "";
		String newColorSchema = "";
		String newBackgroundColor = "";
		String newBorderColor = "";
		String newFontColor = "";
		String newFont = "";
		String newFontSize = "";
		String newPrimaryColor = "";
		boolean newAutoPlay = false;
		boolean primaryColor = false;

		for (PreferencePage page : pages) {
			if (page instanceof PageStartmode) {
				PageStartmode pageStartmode = (PageStartmode) page;
				if (pageStartmode.isInit()) {
					newStartmode = pageStartmode.getStartmode();
				} else {
					newStartmode = settings.isFullscreen();
				}
			} else if (page instanceof PagePlayerSettings) {
				PagePlayerSettings pagePlayerSettings = (PagePlayerSettings) page;
				if (pagePlayerSettings.isInit()) {
					newAutoPlay = pagePlayerSettings.autoPlay();
					newDesignName = pagePlayerSettings.getDesignName();
					newDesignSchema = pagePlayerSettings.getDesignSchema();
					newColorSchema = pagePlayerSettings.getColorSchema();
					newBackgroundColor = pagePlayerSettings.getBackgroundColor();
					newBorderColor = pagePlayerSettings.getBorderColor();
					newFontColor = pagePlayerSettings.getFontColor();
					newFont = pagePlayerSettings.getTextFont();
					newFontSize = pagePlayerSettings.getFontSize();
					newPrimaryColor = pagePlayerSettings.getPrimaryColor();
					primaryColor = !pagePlayerSettings.isOwnDesign();

				} else {
					newDesignName = settings.getDesignName();
					newDesignSchema = settings.getDesignSchema();
					newColorSchema = settings.getColorSchema();
					newBackgroundColor = settings.getBackgroundColor();
					newBorderColor = settings.getBorderColor();
					newFontColor = settings.getTextColor();
					newFont = settings.getFont();
					newFontSize = settings.getFontSize();
					newAutoPlay = settings.autoPlay();
					newPrimaryColor = settings.getPrimaryColor();
					primaryColor = settings.isPrimaryColor();
				}
			} else if (page instanceof PageLayout) {
				PageLayout pageLayout = (PageLayout) page;
				if (pageLayout.isInit()) {
					HashMap<String, Float> layout = pageLayout.getLayout();
					newSizeWidth = (layout.get("sizeWidth")).intValue(); //$NON-NLS-1$
					newSizeHeight = ((layout.get("sizeHeight"))).intValue(); //$NON-NLS-1$
					newAreaLeftWidth = layout.get("areaLeftWidth"); //$NON-NLS-1$
					newAreaRightWidth = layout.get("areaRightWidth"); //$NON-NLS-1$
					newAreaTopHeight = layout.get("areaTopHeight"); //$NON-NLS-1$
					newAreaBottomHeight = layout.get("areaBottomHeight"); //$NON-NLS-1$
				} else {
					newSizeWidth = settings.getSizeWidth();
					newSizeHeight = settings.getSizeHeight();
					newAreaLeftWidth = settings.getAreaLeftWidth();
					newAreaRightWidth = settings.getAreaRightWidth();
					newAreaTopHeight = settings.getAreaTopHeight();
					newAreaBottomHeight = settings.getAreaBottomHeight();
				}
			} else if (page instanceof PageExport) {
				PageExport pageExport = (PageExport) page;
				if (pageExport.isInit()) {
					export = pageExport.getExportSettings();
				} else {
					export = settings.getExportSettings(null);
				}
			} else if (page instanceof PageExtended) {
				PageExtended pageExtended = (PageExtended) page;
				if (pageExtended.isInit()) {
					autoReload = pageExtended.getAutoReload();
					autoReloadTime = pageExtended.getAutoReloadTime();
					name = pageExtended.getProjectName();
					projectCollaborationID = pageExtended.getProjectCollaborationID();
				} else {
					autoReloadTime = settings.getAutoreloadTime();
					autoReload = settings.isAutoreload();
					name = settings.getProjectName();
					projectCollaborationID = settings.getProjectCollaborationID();
				}
			} else if (page instanceof PageSemanticZoom) {
				PageSemanticZoom pageSemanticZoom = (PageSemanticZoom) page;
				if (pageSemanticZoom.isInit()) {
					fullSemanticZoomLevels = pageSemanticZoom
							.getFullZoomLevels();
				} else {
					fullSemanticZoomLevels = settings
							.isFullSemanticZoomLevels();
				}
			}
		}

		ChangeSettingsOperation operation;

		operation = new ChangeSettingsOperation(newStartmode, newSkin,
				newSizeWidth, newSizeHeight, newAreaLeftWidth,
				newAreaRightWidth, newAreaTopHeight, newAreaBottomHeight,
				export, autoReload, autoReloadTime, projectCollaborationID, name, newDesignName,
				newDesignSchema, newColorSchema, newBackgroundColor,
				newBorderColor, newFontColor, newFont, newFontSize,
				fullSemanticZoomLevels, newAutoPlay, newPrimaryColor, primaryColor, settings);

		try {
			OperationHistory.execute(operation);
		} catch (ExecutionException e) {
			operation.getErrorMessage(e);
		}

	}
}
