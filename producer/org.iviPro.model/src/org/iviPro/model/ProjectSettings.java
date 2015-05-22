package org.iviPro.model;


public class ProjectSettings extends IAbstractBean {
	
	/**
	 * Enum for the available modes of the annotation bar. To keep the player
	 * backwards compatible, a name field is used to get the same notions
	 * used in the player before this feature has been implemented to the producer.
	 */
	public enum AnnobarVisibility {
		FOLDED(""), ONSTART("onStart"),	ALWAYS("always"), NEVER("never");
		
		private String name;
		
		private AnnobarVisibility(String name) {
			this.name = name;
		}		
		
		@Override
		public String toString() {
			return name;
		}
	}

	private static final long serialVersionUID = 1L;
	
	// Default values
	//Producer settings
	public static final String DEFAULT_DEFAULTMEDIAFOLDER = "";
	public static final boolean DEFAULT_FULLSEMANTICZOOMLEVELS = true;
	//Player settings
	public static final int DEFAULT_RESOLUTIONWIDTH = 1280;
	public static final int DEFAULT_RESOLUTIONHEIGHT = 720;
	
	public static final AnnobarVisibility DEFAULT_ANNOBARVISIBILITY = 
			AnnobarVisibility.FOLDED; 
	public static final boolean DEFAULT_ANNOTATIONOVERLAY = true;
	public static final float DEFAULT_ANNOTATIONBARWIDTH = 0.2f;
	public static final float DEFAULT_NAVIGATIONBARWIDTH = 0.2f;
	public static final String DEFAULT_PRIMARYCOLOR = "#363636";
	public static final String DEFAULT_SECONDARYCOLOR = "#FFFFFF";
	public static final boolean DEFAULT_USERDIARY = true;
	public static final String DEFAULT_VIDEONAME = "";
	public static final boolean DEFAULT_AUTOPLAY = true;
	public static final boolean DEFAULT_LOGGING = true;
	//Collaboration
	public static final boolean DEFAULT_COLLABORATION = false;
	public static final String DEFAULT_SERVERURL = "";
	

//	// Just a few properties really need to be monitored 
//	public static final String PROP_FULL = "fullscreen";
//	public static final String PROP_SKIN = "skin";
//	public static final String PROP_AUTORELOAD = "autoreload";
//	public static final String PROP_PROJECTCOLLABORATIONID = "projectCollaborationID";
	public static final String PROP_FULLSEMANTICZOOMLEVELS = "fullsemanticzoomlevels"; // needed
//	public static final String PROP_PROJECTNAME = "projectName";
//	public static final String PROP_DESIGNNAME = "designName";
//	public static final String PROP_DESIGNSCHEMA = "desingSchema";
//	public static final String PROP_COLORSCHEMA = "colorSchema";
//	public static final String PROP_BORDERCOLOR = "borderColor";
//	public static final String PROP_BACKGROUNDCOLOR = "backgroundColor";
//	public static final String PROP_TEXTCOLOR = "textColor";
//	public static final String PROP_FONT = "font";
//	public static final String PROP_FONTSIZE = "fontSize";
//	public static final String PROP_AUTOPLAY = "autoPlay";
//	public static final String PROP_PRIMARYCOLOR = "primaryColorValue";
//	public static final String PROP_PRIMARYCOLOR_BOOL = "primaryColor";
//	
//	// Ids f�r die Skins
//	public static final int SKIN_SIMPLE = 1;
//	public static final int SKIN_DARK = 2;
//	public static final int SKIN_WHITE = 3;
//
	public static final String PROP_DIMENSION = "dimension"; // needed


		
//	private float areaBottomHeight;
//	private float areaTopHeight;
//	private int skin;
//	private int autoreloadTime;
//	private boolean autoreload;
	
//	private int projectCollaborationID;
//	
	
	//Producer settings
	private String defaultMediaFolder;
	private boolean fullSemanticZoomLevels;
	
	//Player settings
	private int resolutionWidth;
	private int resolutionHeight;
	
	private AnnobarVisibility annobarVisibility;
	private boolean annobarOverlay;
	
	private float annotationBarWidth;
	private float navigationBarWidth;
	
	private String primaryColor;
	private String secondaryColor;
	
	private boolean userDiary;
	
	private String videoTitle;
	private boolean autoStart;
	private boolean logging;
	
	// Collaboration settings
	private boolean collaboration;
	private String serverUrl;	
	
	/**
	 * Constructs default <code>ProjectSettings</code>. Settings do not have a title.
	 */
	public ProjectSettings(Project project) {
		super((String)null, project);
		initializeValues();
	}
	
	/**
	 * Copy constructor. Return a copy of the given settings.
	 * @param settingsToCopy settings which should be copied
	 */
	public ProjectSettings(ProjectSettings settingsToCopy) {
		super((String)null, settingsToCopy.project);
	
		// Producer settings
		defaultMediaFolder = settingsToCopy.defaultMediaFolder;
		fullSemanticZoomLevels = settingsToCopy.fullSemanticZoomLevels;

		// Player settings
		resolutionWidth = settingsToCopy.resolutionWidth;
		resolutionHeight = settingsToCopy.resolutionHeight;
		
		annobarVisibility = settingsToCopy.annobarVisibility;
		annobarOverlay = settingsToCopy.annobarOverlay;
		annotationBarWidth = settingsToCopy.annotationBarWidth;
		navigationBarWidth = settingsToCopy.navigationBarWidth;
		
		primaryColor = settingsToCopy.primaryColor;
		secondaryColor = settingsToCopy.secondaryColor;

		userDiary = settingsToCopy.userDiary;

		videoTitle = settingsToCopy.videoTitle;
		autoStart = settingsToCopy.autoStart;
		logging = settingsToCopy.logging;

		// Collaboration
		collaboration = settingsToCopy.collaboration;
		serverUrl = settingsToCopy.serverUrl;
	}
	
	/**
	 * Sets the project this settings object belongs to.
	 * Since the ProjectSettings have to be created before the creation of the
	 * Project itself took place, this method (in contrast to the otherwise 
	 * protected method of all other beans) has to be declared public.
	 * 
	 * @param project project to which these settings belong
	 */
	@Override
	public void setProject(Project project) {
		this.project = project;
	}	
	
	/**
	 * Initializes the settings with default values.
	 */
	private void initializeValues() {		
		// Producer settings
		defaultMediaFolder = DEFAULT_DEFAULTMEDIAFOLDER;
		fullSemanticZoomLevels = DEFAULT_FULLSEMANTICZOOMLEVELS;
				
		// Player settings
		resolutionWidth = DEFAULT_RESOLUTIONWIDTH;
		resolutionHeight = DEFAULT_RESOLUTIONHEIGHT;
		
		annobarVisibility = DEFAULT_ANNOBARVISIBILITY;
		annobarOverlay = DEFAULT_ANNOTATIONOVERLAY;
		annotationBarWidth = DEFAULT_ANNOTATIONBARWIDTH;
		navigationBarWidth = DEFAULT_NAVIGATIONBARWIDTH;
		
		primaryColor = DEFAULT_PRIMARYCOLOR;
		secondaryColor = DEFAULT_SECONDARYCOLOR;
		
		userDiary = DEFAULT_USERDIARY;
		
		videoTitle = DEFAULT_VIDEONAME;
		autoStart = DEFAULT_AUTOPLAY;
		logging = DEFAULT_LOGGING;
		
		// Collaboration
		collaboration = DEFAULT_COLLABORATION;
		serverUrl = DEFAULT_SERVERURL;
	}
	
	// Producer settings
	public void setFullSemanticZoom(boolean fullSemanticZoomLevels) {
		this.fullSemanticZoomLevels = fullSemanticZoomLevels;
		firePropertyChange(PROP_FULLSEMANTICZOOMLEVELS, 1, -1);
	}

	public boolean isFullSemanticZoomEnabled() {
		return fullSemanticZoomLevels;
	}

	// Player settings
	public int getResolutionWidth() {
		return resolutionWidth;
	}
	
	public void setResolutionWidth(int width) {
		resolutionWidth = width;
		firePropertyChange(PROP_DIMENSION, 1, -1);
	}
	
	public int getResolutionHeight() {
		return resolutionHeight;
	}
	
	public void setResolutionHeight(int height) {
		resolutionHeight = height;
		firePropertyChange(PROP_DIMENSION, 1, -1);
	}
	
	public AnnobarVisibility getAnnobarVisibility() {
		return annobarVisibility;
	}
	
	public void setAnnobarVisibility(AnnobarVisibility vis) {
		annobarVisibility = vis;
	}
	
	public boolean isAnnobarOverlayEnabled() {
		return annobarOverlay;
	}
	
	public void setAnnobarOverlay(boolean overlap) {
		this.annobarOverlay = overlap;
	}
	
	public float getAnnotationBarWidth() {
		return annotationBarWidth;
	}

	public void setAnnotationBarWidth(float width) {
		annotationBarWidth = width;
		firePropertyChange(PROP_DIMENSION, 1, -1);
	}

	public float getNavigationBarWidth() {
		return navigationBarWidth;
	}
	
	public void setNavigationBarWidth(float width) {
		navigationBarWidth = width;
		firePropertyChange(PROP_DIMENSION, 1, -1);
	}
	
	public String getPrimaryColor() {
		return primaryColor;
	}
	
	public void setPrimaryColor(String primaryColor) {
		this.primaryColor = primaryColor;
		//firePropertyChange(PROP_PRIMARYCOLOR, 1, -1);
	}
	
	public String getSecondaryColor() {
		return secondaryColor;
	}
	
	public void setSecondaryColor(String secondaryColor) {
		this.secondaryColor = secondaryColor;
		//firePropertyChange(PROP_PRIMARYCOLOR, 1, -1);
	}
	
	public boolean isUserDiaryEnabled(){
		return userDiary;
	}
	
	public void setUserDiary(boolean userDiary) {
		this.userDiary = userDiary;
//		firePropertyChange(PROP_AUTOPLAY, 1, -1);
	}
	
	public String getVideoTitle() {
		return videoTitle;
	}

	public void setVideoTitle(String videoTitle) {
		this.videoTitle = videoTitle;
		//firePropertyChange(PROP_PROJECTNAME, 1, -1);
	}
	
	public boolean isAutostartEnabled(){
		return autoStart;
	}
	
	public void setAutoStart(boolean autoStart) {
		this.autoStart = autoStart;
//		firePropertyChange(PROP_AUTOPLAY, 1, -1);
	}
	
	public boolean isLoggingEnabled() {
		return logging;
	}
	
	public void setLogging(boolean logging) {
		this.logging = logging;
	}
	
	// Collaboration
	public boolean isCollaborationEnabled() {
		return collaboration;
	}
	
	public void setCollaboration(boolean collaboration) {
		this.collaboration = collaboration;
	}
	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String url) {
		serverUrl = url;
	}
}