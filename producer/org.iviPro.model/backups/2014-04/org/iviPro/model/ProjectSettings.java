package org.iviPro.model;

import java.util.HashMap;

public class ProjectSettings extends IAbstractBean {

	private static final long serialVersionUID = 1L;

	public static final String PROP_FULL = "fullscreen";
	public static final String PROP_SKIN = "skin";
	public static final String PROP_AUTORELOAD = "autoreload";
	public static final String PROP_PROJECTCOLLABORATIONID = "projectCollaborationID";
	public static final String PROP_FULLSEMANTICZOOMLEVELS = "fullsemanticzoomlevels";
	public static final String PROP_PROJECTNAME = "projectName";
	public static final String PROP_DESIGNNAME = "designName";
	public static final String PROP_DESIGNSCHEMA = "desingSchema";
	public static final String PROP_COLORSCHEMA = "colorSchema";
	public static final String PROP_BORDERCOLOR = "borderColor";
	public static final String PROP_BACKGROUNDCOLOR = "backgroundColor";
	public static final String PROP_TEXTCOLOR = "textColor";
	public static final String PROP_FONT = "font";
	public static final String PROP_FONTSIZE = "fontSize";
	public static final String PROP_AUTOPLAY = "autoPlay";
	public static final String PROP_PRIMARYCOLOR = "primaryColorValue";
	public static final String PROP_PRIMARYCOLOR_BOOL = "primaryColor";
	
	// Ids für die Skins
	public static final int SKIN_SIMPLE = 1;
	public static final int SKIN_DARK = 2;
	public static final int SKIN_WHITE = 3;

	public static final String PROP_DIMENSION = "dimension";

	private boolean fullscreen;
	private float areaBottomHeight;
	private float areaTopHeight;
	private float areaRightWidth;
	private float areaLeftWidth;
	private int skin;
	private int sizeHeight;
	private int sizeWidth;
	private int autoreloadTime;
	private boolean autoreload;
	private boolean fullSemanticZoomLevels;

	private String projectName;
	private int projectCollaborationID;
	
	//Player settings
	private boolean autoPlay;
	private String primaryColorValue;
	private boolean primaryColor;
	private String designName;
	private String designSchema;
	private String colorSchema;
	private String backgroundColor;
	private String borderColor;
	private String textColor;
	private String font;
	private String fontSize;	

	public final String VIDEOBITRATE = "videoBitrate";
	public final String VIDEOFRAMERATE = "videoFramerate";
	public final String VIDEOSIZE = "videoSize"; // "600*800"
	public final String ASPECTRATIO = "aspectRatio"; // "16:9"
	public static final String VIDEOCODEC = "videoCodec";
	public static final String VIDEOCONTAINER = "videoContainereFormat";
	public static final String VIDEOEXTENSION = "videoFileExtension";
	public static final String AUDIOCODEC = "audioCodec";
	public static final String AUDIOCONTAINER = "audioContainerFormat";
	public static final String AUDIOEXTENSION = "audioFileExtension";
	public final String AUDIOBITRATE = "audioBitrate";
	public final String AUDIOSAMPLEFREQUENCY = "audioSampleFrequency";

	public static final String FLASH = "Flash";
	public static final String H246 = "H.264";
	public static final String OGG = "OGG/THEORA";
	public static final String SILVERLIGHT = "Silverlight";
	public static final String PROJECT = "project";
	private HashMap<String, String> projectExportSettings;

	public ProjectSettings(String title, Project project) {
		super(title, project);
	}

	public ProjectSettings(LocalizedString title, Project project) {
		super(title, project);
	}

	public HashMap<String, String> getExportSettings(String type) {
		if (type == null) {
			type = "FLASH";
		}
		// if (projectExportSettings == null) {
		// type = FLASH;
		// }
		boolean init = false;
		HashMap<String, String> exp = new HashMap<String, String>();
		String videoCodec = "";
		String videoContainerFormat = "";
		String videoFileExtension = "";
		String audioCodec = "";
		String audioContainerFormat = "";
		String audioFileExtension = "";
		if (type.equals(FLASH)) {
			videoCodec = "flv";
			videoContainerFormat = "flv";
			videoFileExtension = "flv";
			audioCodec = "libmp3lame";
			audioContainerFormat = "mp3";
			audioFileExtension = "mp3";
			init = true;
		} else if (type.equals(H246)) {
			videoCodec = "libx264";
			videoContainerFormat = "mp4";
			videoFileExtension = "mp4";
			audioCodec = "libmp3lame";
			audioContainerFormat = "mp3";
			audioFileExtension = "mp3";
			init = true;
		} else if (type.equals(OGG)) {
			videoCodec = "libtheora";
			videoContainerFormat = "ogg";
			videoFileExtension = "ogg";
			audioCodec = "libvorbis";
			audioContainerFormat = "ogg";
			audioFileExtension = "ogg";
			init = true;
		} else if (type.equals(SILVERLIGHT)) {
			videoCodec = "wmv2";
			videoContainerFormat = "asf";
			videoFileExtension = "wmv";
			audioCodec = "wmav2";
			audioContainerFormat = "asf";
			audioFileExtension = "wma";
			init = true;
		} else {
			if (projectExportSettings == null) {
				projectExportSettings = getExportSettings(FLASH);
			}
			exp = projectExportSettings;
		}
		if (init) {
			setDefaults(exp);
			exp.put(VIDEOCODEC, videoCodec);
			exp.put(VIDEOCONTAINER, videoContainerFormat);
			exp.put(VIDEOEXTENSION, videoFileExtension);
			exp.put(AUDIOCODEC, audioCodec);
			exp.put(AUDIOCONTAINER, audioContainerFormat);
			exp.put(AUDIOEXTENSION, audioFileExtension);
		}
		return exp;
	}

	private void setDefaults(HashMap<String, String> fill) {
		fill.put(VIDEOBITRATE, "2048000");
		fill.put(VIDEOFRAMERATE, "25");
		fill.put(VIDEOSIZE, "640*480");
		fill.put(ASPECTRATIO, "4:3");
		fill.put(AUDIOBITRATE, "32768");
		fill.put(AUDIOSAMPLEFREQUENCY, "22050");
	}

	public void setExportSettings(HashMap<String, String> export) {
		projectExportSettings = export;
	}

	public boolean isFullscreen() {
		return fullscreen;
	}

	public void setFullscreen(boolean fullscreen) {
		this.fullscreen = fullscreen;
		firePropertyChange(PROP_FULL, this.fullscreen, fullscreen);
	}

	public float getAreaBottomHeight() {
		return areaBottomHeight;
	}

	public float getAreaTopHeight() {
		return areaTopHeight;
	}

	public float getAreaRightWidth() {
		return areaRightWidth;
	}

	public float getAreaLeftWidth() {
		return areaLeftWidth;
	}

	public int getSizeHeight() {
		return sizeHeight;
	}

	public int getSizeWidth() {
		return sizeWidth;
	}

	public int getAreaCenterWidth() {
		return this.sizeWidth - (int) areaRightWidth * sizeWidth
				- (int) areaLeftWidth * sizeWidth;
	}

	public int getAreaCenterHeight() {
		return this.sizeHeight - (int) areaTopHeight * sizeHeight
				- (int) areaBottomHeight * sizeHeight;
	}

	public void setDimensions(int width, int height, float leftWidth,
			float topHeight, float bottomHeight, float rightWidth) {
		this.sizeWidth = width;
		this.sizeHeight = height;
		this.areaLeftWidth = leftWidth;
		this.areaTopHeight = topHeight;
		this.areaBottomHeight = bottomHeight;
		this.areaRightWidth = rightWidth;
		firePropertyChange(PROP_DIMENSION, 1, -1);
	}

	public int getAutoreloadTime() {
		return autoreloadTime;
	}

	public void setAutoreload(boolean autoreload, int autoreloadtime) {
		this.autoreload = autoreload;
		this.autoreloadTime = autoreloadtime;
		firePropertyChange(PROP_AUTORELOAD, 1, -1);
	}

	public boolean isAutoreload() {
		return autoreload;
	}

	public void setFullSemanticZoomLevels(boolean fullSemanticZoomLevels) {
		this.fullSemanticZoomLevels = fullSemanticZoomLevels;
		firePropertyChange(PROP_FULLSEMANTICZOOMLEVELS, 1, -1);
	}

	public boolean isFullSemanticZoomLevels() {
		return fullSemanticZoomLevels;
	}

	public String getProjectName() {
		if (projectName == null) {
			projectName = "";
		}
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
		firePropertyChange(PROP_PROJECTNAME, 1, -1);
	}

	public int getProjectCollaborationID() {
		return projectCollaborationID;
	}
	
	public void setProjectCollaborationID(int projectCollaborationID) {
		this.projectCollaborationID = projectCollaborationID;
	}
	
	public int getSkin() {
		return skin;
	}
	
	public void setSkin(int skin) {
		int old = this.skin;
		this.skin = skin;
		firePropertyChange(PROP_SKIN, old, skin);
	}
	
	//Player settings
	public boolean autoPlay(){
		return autoPlay;
	}
	
	public void setAutoPlay(boolean autoPlay) {
		this.autoPlay = autoPlay;
		firePropertyChange(PROP_AUTOPLAY, 1, -1);
	}
	
	public boolean isPrimaryColor() {
		return primaryColor;
	}
	
	public void setPrimaryColorBool(boolean primaryColor) {
		this.primaryColor = primaryColor;
		firePropertyChange(PROP_PRIMARYCOLOR_BOOL, 1, -1);
	}
	
	public String getPrimaryColor() {
		return primaryColorValue;
	}
	
	public void setPrimaryColor(String primaryColor) {
		this.primaryColorValue = primaryColor;
		firePropertyChange(PROP_PRIMARYCOLOR, 1, -1);
	}
	

	public void setDesignSchema(String designSchema) {
		this.designSchema = designSchema;
		firePropertyChange(PROP_DESIGNSCHEMA, 1, -1);
	}

	public String getDesignSchema() {
		return designSchema;
	}
	
	public String getDesignName() {
		if(designName == null) {
			designName = "";
		}
		return designName;
	}
	
	public void setDesignName(String newdesignName) {
		this.designName = newdesignName;
		firePropertyChange(PROP_DESIGNNAME, 1, -1);
	}

	public void setColorSchema(String colorSchema) {
		this.colorSchema = colorSchema;
		firePropertyChange(PROP_COLORSCHEMA, 1, -1);
	}

	public String getColorSchema() {
		return colorSchema;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
		firePropertyChange(PROP_BACKGROUNDCOLOR, 1, -1);
	}
	
	public String getBackgroundColor() {
		return backgroundColor;
	}
	
	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
		firePropertyChange(PROP_BORDERCOLOR, 1, -1);
	}

	public String getBorderColor() {
		return borderColor;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
		firePropertyChange(PROP_TEXTCOLOR, 1, -1);
	}

	public String getTextColor() {
		return textColor;
	}

	public void setFont(String font) {
		this.font = font;
		firePropertyChange(PROP_FONT, 1, -1);
	}

	public String getFont() {
		return font;
	}

	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
		firePropertyChange(PROP_FONTSIZE, 1, -1);
	}

	public String getFontSize() {
		return fontSize;
	}

}
