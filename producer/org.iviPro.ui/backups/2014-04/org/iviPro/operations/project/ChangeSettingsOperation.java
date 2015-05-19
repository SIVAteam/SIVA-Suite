package org.iviPro.operations.project;

import java.util.HashMap;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.model.ProjectSettings;
import org.iviPro.operations.IAbstractOperation;

public class ChangeSettingsOperation extends IAbstractOperation {

	private boolean newStartmode;
	private boolean oldStartmode;
	private int newSkin;
	private int oldSkin;
	private int newSizeWidth;
	private int oldSizeWidth;
	private int newSizeHeight;
	private int oldSizeHeight;
	private float newAreaLeftWidth;
	private float oldAreaLeftWidth;
	private float newAreaRightWidth;
	private float oldAreaRightWidth;
	private float newAreaTopHeight;
	private float oldAreaTopHeight;
	private float newAreaBottomHeight;
	private float oldAreaBottomHeight;
	private HashMap<String, String> newExportSettings;
	private HashMap<String, String> oldExportSettings;
	private boolean newAutoReload;
	private boolean oldAutoReload;
	private int newAutoReloadTime;
	private int oldAutoReloadTime;
	private int oldProjectCollaborationID;
	private int newProjectCollaborationID;
	private boolean newFullSemanticZoomLevels;
	private boolean oldFullSemanticZoomLevels;
	private String oldProjectName;
	private String newProjectName;
	private String oldDesignName;
	private String newDesignName;
	private String oldDesignSchema;
	private String newDesignSchema;
	private String oldColorSchema;
	private String newColorSchema;
	private String oldBackgroundColor;
	private String newBackgroundColor;
	private String oldBorderColor;
	private String newBorderColor;
	private String oldFontColor;
	private String newFontColor;
	private String oldFont;
	private String newFont;
	private String oldFontSize;
	private String newFontSize;
	private String newPrimaryColorValue;
	private String oldPrimaryColorValue;
	private boolean newAutoPlay;
	private boolean oldAutoPlay;
	private boolean newPrimaryColor;
	private boolean oldPrimaryColor;

	private ProjectSettings settings;

	public ChangeSettingsOperation(boolean newStartmode, int newSkin,
			int newSizeWidth, int newSizeHeight, float newAreaLeftWidth,
			float newAreaRightWidth, float newAreaTopHeight,
			float newAreaBottomHeight,
			HashMap<String, String> newExportSettings, boolean newAutoReload,
			int newAutoReloadTime, int newProjectCollaborationID, String newProjectName, String newDesName,
			String newDesignSchema, String newColorSchema,
			String newBackgroundColor, String newBorderColor,
			String newFontColor, String newFont, String newFontSize,
			boolean newFullZoomLevels, boolean newAutoPlay, String newPrimaryColor,
			boolean primaryColor, ProjectSettings settings) {
		super(Messages.ChangeSettingsOperation_ChangeSettingsOperationTitle);
		
		setNewValues(newStartmode, newSkin, newSizeWidth, newSizeHeight,
				newAreaLeftWidth, newAreaRightWidth, newAreaTopHeight,
				newAreaBottomHeight, newExportSettings, newAutoReload,
				newAutoReloadTime, newProjectCollaborationID, newProjectName, newDesName, newDesignSchema,
				newColorSchema, newBackgroundColor, newBorderColor,
				newFontColor, newFont, newFontSize, newFullZoomLevels, newAutoPlay,
				newPrimaryColor, primaryColor, settings);

		this.settings = settings;
		setOldValues(settings);

	}

	private void setOldValues(ProjectSettings settings) {
		this.oldStartmode = settings.isFullscreen();
		this.oldSkin = settings.getSkin();
		this.oldSizeWidth = settings.getSizeWidth();
		this.oldSizeHeight = settings.getSizeHeight();
		this.oldAreaLeftWidth = settings.getAreaLeftWidth();
		this.oldAreaRightWidth = settings.getAreaRightWidth();
		this.oldAreaTopHeight = settings.getAreaTopHeight();
		this.oldAreaBottomHeight = settings.getAreaBottomHeight();
		this.oldExportSettings = settings.getExportSettings(null);
		this.oldAutoReload = settings.isAutoreload();
		this.oldAutoReloadTime = settings.getAutoreloadTime();
		this.oldProjectCollaborationID = settings.getProjectCollaborationID();
		this.oldFullSemanticZoomLevels = settings.isFullSemanticZoomLevels();
		this.oldProjectName = settings.getProjectName();
		this.oldDesignName = settings.getDesignName();
		this.oldDesignSchema = settings.getDesignSchema();
		this.oldColorSchema = settings.getColorSchema();
		this.oldBackgroundColor = settings.getBackgroundColor();
		this.oldBorderColor = settings.getBorderColor();
		this.oldFontColor = settings.getTextColor();
		this.oldFont = settings.getFont();
		this.oldFontSize = settings.getFontSize();
		this.oldPrimaryColorValue = settings.getPrimaryColor();
		this.oldAutoPlay = settings.autoPlay();
		this.oldPrimaryColor = settings.isPrimaryColor();
	}

	private void setNewValues(boolean newStartmode, int newSkin,
			int newSizeWidth, int newSizeHeight, float newAreaLeftWidth,
			float newAreaRightWidth, float newAreaTopHeight,
			float newAreaBottomHeight,
			HashMap<String, String> newExportSettings, boolean newAutoReload,
			int newAutoReloadTime, int newProjectCollaborationID, String newProjectName, String newDesName,
			String newDesignSchema, String newColorSchema,
			String newBackgroundColor, String newBorderColor,
			String newFontColor, String newFont, String newFontSize,
			boolean newFullZoomLevels, boolean newAutoPlay, String newPrimaryColor,
			boolean primaryColor, ProjectSettings settings) {
		this.newStartmode = newStartmode;
		this.newSkin = newSkin;
		this.newSizeWidth = newSizeWidth;
		this.newSizeHeight = newSizeHeight;
		this.newAreaLeftWidth = newAreaLeftWidth;
		this.newAreaRightWidth = newAreaRightWidth;
		this.newAreaTopHeight = newAreaTopHeight;
		this.newAreaBottomHeight = newAreaBottomHeight;
		this.newExportSettings = newExportSettings;
		this.newAutoReload = newAutoReload;
		this.newAutoReloadTime = newAutoReloadTime;
		this.newProjectCollaborationID = newProjectCollaborationID;
		this.newFullSemanticZoomLevels = newFullZoomLevels;
		this.newProjectName = newProjectName;
		this.settings = settings;
		this.newDesignName = newDesName;
		this.newDesignSchema = newDesignSchema;
		this.newColorSchema = newColorSchema;
		this.newBorderColor = newBorderColor;
		this.newBackgroundColor = newBackgroundColor;
		this.newFontColor = newFontColor;
		this.newFont = newFont;
		this.newFontSize = newFontSize;
		this.newAutoPlay = newAutoPlay;
		this.newPrimaryColorValue = newPrimaryColor;
		this.newPrimaryColor = primaryColor;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return e.getMessage();
	}

	@Override
	public boolean canExecute() {
		return settings != null;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		settings.setFullscreen(newStartmode);
		settings.setSkin(newSkin);
		settings.setDimensions(newSizeWidth, newSizeHeight, newAreaLeftWidth,
				newAreaTopHeight, newAreaBottomHeight, newAreaRightWidth);
		settings.setExportSettings(newExportSettings);
		settings.setAutoreload(newAutoReload, newAutoReloadTime);
		settings.setProjectCollaborationID(newProjectCollaborationID);
		settings.setFullSemanticZoomLevels(newFullSemanticZoomLevels);
		settings.setProjectName(newProjectName);
		settings.setDesignName(newDesignName);

		settings.setDesignSchema(newDesignSchema);
		settings.setColorSchema(newColorSchema);
		settings.setBackgroundColor(newBackgroundColor);
		settings.setBorderColor(newBorderColor);
		settings.setTextColor(newFontColor);
		settings.setFont(newFont);
		settings.setFontSize(newFontSize);
		settings.setAutoPlay(newAutoPlay);
		settings.setPrimaryColor(newPrimaryColorValue);
		settings.setPrimaryColorBool(newPrimaryColor);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		settings.setFullscreen(oldStartmode);
		settings.setSkin(oldSkin);
		settings.setDimensions(oldSizeWidth, oldSizeHeight, oldAreaLeftWidth,
				oldAreaTopHeight, oldAreaBottomHeight, oldAreaRightWidth);
		settings.setExportSettings(oldExportSettings);
		settings.setAutoreload(oldAutoReload, oldAutoReloadTime);
		settings.setProjectCollaborationID(oldProjectCollaborationID);
		settings.setFullSemanticZoomLevels(oldFullSemanticZoomLevels);
		settings.setProjectName(oldProjectName);
		settings.setDesignName(oldDesignName);

		settings.setDesignSchema(oldDesignSchema);
		settings.setColorSchema(oldColorSchema);
		settings.setBackgroundColor(oldBackgroundColor);
		settings.setBorderColor(oldBorderColor);
		settings.setTextColor(oldFontColor);
		settings.setFont(oldFont);
		settings.setFontSize(oldFontSize);
		settings.setAutoPlay(oldAutoPlay);
		settings.setPrimaryColor(oldPrimaryColorValue);
		settings.setPrimaryColorBool(oldPrimaryColor);
		return Status.OK_STATUS;
	}

}
