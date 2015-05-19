package org.iviPro.projectsettings;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.iviPro.model.ProjectSettings;
import org.iviPro.model.ProjectSettings.AnnobarVisibility;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.settings.ProjectSettingsSaveOperation;
import org.iviPro.theme.Colors;
import org.iviPro.utils.NumericInputListener;
import org.iviPro.utils.widgets.IpAddressText;
import org.iviPro.utils.widgets.SizedText;
import org.iviPro.utils.widgets.SizedTextWithUnit;

/**
 * Composite containing the controls used to change the player settings.
 * @author John
 *
 */
public class PlayerSettingsComposite extends Composite implements SettingsComposite {
	
	private static final int GROUP_MARGIN = 20;
	private static final int PREVIEW_MAXWIDTH = 360;
	private static final int PREVIEW_MAXHEIGHT = 180;
	
	private static final int MIN_RES_VALUE = 100;
	private static final int MAX_RES_VALUE = 99999;
	private static final int MIN_SIDEBARSIZE = 0; // percentage
	private static final int MAX_SIDEBARSIZE =100; // percentage
	private static final float MAX_ASPECT = 2;
	private static final float MIN_ASPECT = 0.5f;
	
	private ProjectSettings settings;
	private Canvas previewCanvas;
	private GridData previewLayoutData;
	
	// Components which can be used to directly access the settings
	private Text widthField;
	private Text heightField;
	private Button foldedField;
	private Button onstartField;
	private Button alwaysField;
	private Button neverField;
	private Button overlapField;
	private Button shrinkField;
	private Text navigationBarField;
	private Text annotationBarField;
	private ColorSelector primColorField;
	private ColorSelector secColorField;
	private IpAddressText ipField;
	private Text portField;
	private Text videoTitleField;
	private Button autoStartField;
	private Button userDiaryField;
	
	private Label warning;
	
	Composite behaviorComp;
	GridData behaviorData;
	Group sidebars;
	
	/**
	 * Creates a composite containing the controls used to change the player settings.
	 * @param parent parent composite
	 * @param style style of the composite
	 * @param settings project settings
	 */
	public PlayerSettingsComposite(Composite parent, int style, ProjectSettings settings) {
		super(parent, style);
		this.settings = settings;
		setLayout(new GridLayout(1, false));
		
		compPreview(this);
		compResolution(this);
		compAnnobarVisibility(this);
		compAnnobarBehavior(this);		
		compSidebar(this);
		compColors(this);
		//addServerComp(this);
		compPlayerFunction(this);
		compVideo(this);

		warning = new Label(this, SWT.LEFT);
		warning.setForeground(Colors.WARNING_FONT.getColor());
		warning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		warning.setVisible(false);
		initializeValues();
		repaintPreview();
		addListeners();
	}
	
	/**
	 * Assign a standard layout to the given <code>Group</code>.
	 * @param group <code>Group</code> to set a standard layout for
	 */
	private void setStandardGroupLayout(Group group) {
		GridLayout standard = new GridLayout(1, false);
		standard.marginWidth = GROUP_MARGIN;
		group.setLayout(standard);
	}
	
	/**
	 * Adds the component displaying a preview of a player using the actual
	 * settings to the given parent.
	 * @param parent
	 */
	private void compPreview(Composite parent) {
		// Container
		Composite previewComp = new Composite(this, SWT.NONE);
		GridData previewCompData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		previewCompData.heightHint = PREVIEW_MAXHEIGHT;
		previewComp.setLayoutData(previewCompData);
		GridLayout previewCompLayout = new GridLayout(1, false);
		previewCompLayout.marginHeight = 0;
		previewCompLayout.marginWidth = 0;
		previewComp.setLayout(previewCompLayout);
		
		// Canvas
		previewCanvas = new Canvas(previewComp, SWT.NONE);
		previewLayoutData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		previewCanvas.setLayoutData(previewLayoutData);
		previewCanvas.setBackground(new Color(null, 90, 90, 160));
	}
	
	/**
	 * Adds group for setting the player's resolution to the given parent.
	 * @param parent parent composite
	 */
	private void compResolution(Composite parent) {
		// Group
		Group resolution = new Group(parent, SWT.NONE);
		resolution.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		setStandardGroupLayout(resolution);
		resolution.setText(Messages.PlayerSettingsComposite_Resolution);
		
		// Internal container
		Composite resolutionComp = new Composite(resolution, SWT.NONE);
		GridLayout compLayout = new GridLayout(2, false);
		compLayout.marginWidth = 0;
		compLayout.horizontalSpacing = 10;
		resolutionComp.setLayout(compLayout);
		
		// Controls
		Composite widthComp = new Composite(resolutionComp, SWT.NONE);
		GridLayout widthLayout = new GridLayout(2, false);
		widthLayout.marginWidth = 0;
		widthLayout.marginHeight = 0;
		widthComp.setLayout(widthLayout);
				
		Label widthLabel = new Label(widthComp, SWT.LEFT);
		widthLabel.setText(Messages.PlayerSettingsComposite_ResolutionWidth);
		SizedTextWithUnit widthText = new SizedTextWithUnit(widthComp, 
				SWT.SINGLE | SWT.RIGHT, 5, Messages.PlayerSettingsComposite_UnitPixel);
		widthField = widthText.getTextField();
		
		Composite heightComp = new Composite(resolutionComp, SWT.NONE);
		GridLayout heightLayout = new GridLayout(2, false);
		heightLayout.marginWidth = 0;
		heightLayout.marginHeight = 0;
		heightComp.setLayout(heightLayout);
		
		Label heightLabel = new Label(heightComp, SWT.LEFT);
		heightLabel.setText(Messages.PlayerSettingsComposite_ResolutionHeight);
		SizedTextWithUnit heightText = new SizedTextWithUnit(heightComp,
				SWT.SINGLE | SWT.RIGHT, 5, "px"); //$NON-NLS-1$
		heightField = heightText.getTextField();	
	}
	
	/**
	 * Adds a group for annotation bar visibility to the given component.
	 * @param parent parent composite
	 */
	private void compAnnobarVisibility(Composite parent) {
		// Group
		Group annoVisibility = new Group(parent, SWT.NONE);
		annoVisibility.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		setStandardGroupLayout(annoVisibility);
		annoVisibility.setText(Messages.PlayerSettingsComposite_AnnobarVisibility);
		
		// Controls
		Composite visibilityComp = new Composite(annoVisibility, SWT.NONE);
		GridLayout visibilityLayout = new GridLayout(1, false);
		visibilityLayout.marginHeight = 0;
		visibilityLayout.marginWidth = 0;
		visibilityComp.setLayout(visibilityLayout);
		
		foldedField = new Button(visibilityComp, SWT.RADIO);
		foldedField.setAlignment(SWT.CENTER);
		foldedField.setText(Messages.PlayerSettingsComposite_AnnobarFolded);
		foldedField.setToolTipText(Messages.PlayerSettingsComposite_AnnobarFolded_Tooltip);
		
		onstartField = new Button(visibilityComp, SWT.RADIO);
		onstartField.setAlignment(SWT.CENTER);
		onstartField.setText(Messages.PlayerSettingsComposite_AnnobarStartup);
		onstartField.setToolTipText(Messages.PlayerSettingsComposite_AnnobarStartup_Tooltip);
	
		alwaysField = new Button(visibilityComp, SWT.RADIO);
		alwaysField.setAlignment(SWT.CENTER);
		alwaysField.setText(Messages.PlayerSettingsComposite_AnnobarAlways);
		alwaysField.setToolTipText(Messages.PlayerSettingsComposite_AnnobarAlways_Tooltip);
		
		neverField = new Button(visibilityComp, SWT.RADIO);
		neverField.setAlignment(SWT.CENTER);
		neverField.setText(Messages.PlayerSettingsComposite_AnnobarNever);
		neverField.setToolTipText(Messages.PlayerSettingsComposite_AnnobarNever_Tooltip);
	}
	
	/**
	 * Adds a group for annotation bar behavior to the given component.
	 * @param parent parent composite
	 */
	private void compAnnobarBehavior(Composite parent) {	
		// Group
		Group annoBehavior = new Group(parent, SWT.NONE);
		annoBehavior.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		setStandardGroupLayout(annoBehavior);
		annoBehavior.setText(Messages.PlayerSettingsComposite_AnnobarBehavior);
		
		// Controls
		overlapField = new Button(annoBehavior, SWT.RADIO);
		overlapField.setAlignment(SWT.CENTER);
		overlapField.setText(Messages.PlayerSettingsComposite_AnnobarOverlap);
		overlapField.setToolTipText(Messages.PlayerSettingsComposite_AnnobarOverlap_Tooltip);
		
		shrinkField = new Button(annoBehavior, SWT.RADIO);
		shrinkField.setAlignment(SWT.CENTER);
		shrinkField.setText(Messages.PlayerSettingsComposite_AnnobarShrink);
		shrinkField.setToolTipText(Messages.PlayerSettingsComposite_AnnobarShrink_Tooltip);
	}
	
	/**
	 * Adds group for side bar settings to the given parent.
	 * @param parent parent composite
	 */
	private void compSidebar(Composite parent) {
		// Group
		sidebars = new Group(parent, SWT.NONE);
		sidebars.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		setStandardGroupLayout(sidebars);
		sidebars.setText(Messages.PlayerSettingsComposite_SidebarSize);
		
		// Internal container
		Composite sidebarSizes = new Composite(sidebars, SWT.NONE);
		GridLayout compLayout = new GridLayout(2, false);
		compLayout.marginWidth = 0;
		compLayout.horizontalSpacing = 10;
		sidebarSizes.setLayout(compLayout);

		// Controls
		Composite navigationBarComp = new Composite(sidebarSizes, SWT.NONE);
		GridLayout navigationBarLayout = new GridLayout(2, false);
		navigationBarLayout.marginWidth = 0;
		navigationBarLayout.marginHeight = 0;
		navigationBarComp.setLayout(navigationBarLayout);

		Label navigationBarLabel = new Label(navigationBarComp, SWT.LEFT);
		navigationBarLabel.setText(Messages.PlayerSettingsComposite_NavbarSize);
		SizedTextWithUnit navigationBarText = new SizedTextWithUnit(
				navigationBarComp, SWT.SINGLE | SWT.RIGHT, 3, Messages.PlayerSettingsComposite_UnitPercent);
		navigationBarField = navigationBarText.getTextField();

		Composite annotationBarComp = new Composite(sidebarSizes, SWT.NONE);
		GridLayout annotationBarLayout = new GridLayout(2, false);
		annotationBarLayout.marginWidth = 0;
		annotationBarLayout.marginHeight = 0;
		annotationBarComp.setLayout(annotationBarLayout);

		Label annotationBarLabel = new Label(annotationBarComp, SWT.LEFT);
		annotationBarLabel.setText(Messages.PlayerSettingsComposite_AnnobarSize);
		SizedTextWithUnit annotationBarText = new SizedTextWithUnit(
				annotationBarComp, SWT.SINGLE | SWT.RIGHT, 3, "%"); //$NON-NLS-1$
		annotationBarField = annotationBarText.getTextField();		
	}
	
	/**
	 * Adds group for color selection to the given parent.
	 * @param parent parent composite
	 */
	private void compColors(Composite parent) {
		// Group
		Group colors = new Group(parent, SWT.NONE);
		colors.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		setStandardGroupLayout(colors);
		colors.setText(Messages.PlayerSettingsComposite_PlayerColors);
		
		// Internal container
		Composite colorComp = new Composite(colors, SWT.NONE);	
		GridLayout compLayout = new GridLayout(1, false);
		compLayout.marginWidth = 0;
		colorComp.setLayout(compLayout);
		
		// Controls		
		ColorFieldEditor primColorComp = new ColorFieldEditor("primColor", Messages.PlayerSettingsComposite_PrimaryColor, colorComp); //$NON-NLS-1$
		primColorField = primColorComp.getColorSelector();
		ColorFieldEditor secColorComp = new ColorFieldEditor("secColor", Messages.PlayerSettingsComposite_SecondaryColor, colorComp); //$NON-NLS-1$
		secColorField = secColorComp.getColorSelector();
	
	}
	
	/**
	 * Adds a group for server settings to the given parent.
	 * @param parent parent composite
	 */
	private void compServer(Composite parent) {
		// Group
		Group server = new Group(parent, SWT.NONE);
		server.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		setStandardGroupLayout(server);
		server.setText(Messages.PlayerSettingsComposite_CollaborationServer);
		
		compIp(server);
		compPort(server);			
	}
	
	/**
	 * Add component for setting the server IP to the given parent.
	 * @param parent parent composite
	 */
	private void compIp(Composite parent) {
		// Internal container
		Composite ipComp = new Composite(parent, SWT.NONE);
		GridLayout compLayout = new GridLayout(2, false);
		compLayout.marginWidth = 0;
		compLayout.marginHeight = 0;
		ipComp.setLayout(compLayout);

		// Controls
		Label ipLabel = new Label(ipComp, SWT.LEFT);
		ipLabel.setText(Messages.PlayerSettingsComposite_ServerIP);
		ipField = new IpAddressText(ipComp, SWT.RIGHT | SWT.SINGLE);
	}
	
	/**
	 * Adds component for setting the server port to the given parent.
	 * @param parent parent composite
	 */
	private void compPort(Composite parent) {
		// Internal container
		Composite portComp = new Composite(parent, SWT.NONE);
		GridLayout compLayout = new GridLayout(2, false);
		compLayout.marginWidth = 0;
		compLayout.marginHeight = 0;
		portComp.setLayout(compLayout);

		// Controls
		Label portLabel = new Label(portComp, SWT.LEFT);
		portLabel.setText(Messages.PlayerSettingsComposite_ServerPort);
		SizedText portText = new SizedText(portComp, SWT.SINGLE | SWT.RIGHT, 5);
		portField = portText.getTextField();
	}
	
	/**
	 * Adds a group containing player functions to the given parent.
	 * @param parent parent composite
	 */
	private void compPlayerFunction(Composite parent) {
		// Group
		Group playerFunctions = new Group(parent, SWT.NONE);
		playerFunctions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		setStandardGroupLayout(playerFunctions);
		playerFunctions.setText(Messages.PlayerSettingsComposite_Group_Player_Functions);

		compUserDiary(playerFunctions);
	}
		
	/**
	 * Add a component for enabling the user diary function to the given parent.
	 * @param parent parent composite
	 */
	private void compUserDiary(Composite parent) {
		// Internal container
		Composite userDiaryComp = new Composite(parent, SWT.NONE);
		GridLayout compLayout = new GridLayout(2, false);
		compLayout.marginWidth = 0;
		userDiaryComp.setLayout(compLayout);

		// Controls
		userDiaryField = new Button(userDiaryComp, SWT.CHECK);
		userDiaryField.setAlignment(SWT.CENTER);
		userDiaryField.setText(Messages.PlayerSettingsComposite_UserDiary);
		userDiaryField.setToolTipText(Messages.PlayerSettingsComposite_UserDiary_Tooltip);
	}
	
	/**
	 * Adds a group containing video settings to the given parent.
	 * @param parent parent composite
	 */
	private void compVideo(Composite parent) {
		// Group
		Group videoSettings = new Group(parent, SWT.NONE);
		videoSettings.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		setStandardGroupLayout(videoSettings);
		videoSettings.setText(Messages.PlayerSettingsComposite_VideoBehavior);

		compVideoTitle(videoSettings);
		compAutoStart(videoSettings);
	}
	
	/**
	 * Adds a component for editing the video name to the given parent.
	 * @param parent parent composite
	 */
	private void compVideoTitle(Composite parent) {
		// Internal container
		Composite videoTitleComp = new Composite(parent, SWT.NONE);
		GridLayout compLayout = new GridLayout(2, false);
		compLayout.marginWidth = 0;
		videoTitleComp.setLayout(compLayout);
		
		// Controls
		Label videoTitleLabel = new Label(videoTitleComp, SWT.LEFT);
		videoTitleLabel.setText(Messages.PlayerSettingsComposite_VideoTitle);
		SizedText videoTitle = new SizedText(videoTitleComp, SWT.SINGLE | SWT.LEFT, 20);
		videoTitleField = videoTitle.getTextField();
	}
	
	/**
	 * Add a component for enabling automatic video playback to the given parent.
	 * @param parent parent composite
	 */
	private void compAutoStart(Composite parent) {
		// Internal container
		Composite autoStartComp = new Composite(parent, SWT.NONE);
		GridLayout compLayout = new GridLayout(2, false);
		compLayout.marginWidth = 0;
		autoStartComp.setLayout(compLayout);

		// Controls
		autoStartField = new Button(autoStartComp, SWT.CHECK);
		autoStartField.setAlignment(SWT.CENTER);
		autoStartField.setText(Messages.PlayerSettingsComposite_AutoStart);
		autoStartField.setToolTipText(Messages.PlayerSettingsComposite_AutoStart_Tooltip);	
	}
	
	/**
	 * Add listeners to the controls where needed. For all fields a listener
	 * is needed to inform this <code>PlayerSettingsComposite</code> about 
	 * changes to its subcomponents.
	 */
	private void addListeners() {
		NumericInputListener inputConstraints = new NumericInputListener();
		widthField.addVerifyListener(inputConstraints);
		heightField.addVerifyListener(inputConstraints);
		annotationBarField.addVerifyListener(inputConstraints);
		navigationBarField.addVerifyListener(inputConstraints);
	//	ipField.addVerifyListeners(inputConstraints);
		
		addFieldChangeListeners();
	}
	
	/**
	 * Adds listeners to all fields informing this 
	 * <code>PlayerSettingsComposite</code> about changes to its subcomponents. 
	 */
	private void addFieldChangeListeners() {
		Listener passEvents = new Listener() {				
			@Override
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Modify:
				case SWT.Selection: 
					PlayerSettingsComposite.this.notifyListeners(SWT.Modify, event);
					break;
				}		
			}
		};
		
		IPropertyChangeListener passPropEvents = new IPropertyChangeListener() {			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				Event propEvent = new Event();
				PlayerSettingsComposite.this.notifyListeners(SWT.Modify, propEvent);
			}
		};
		
		widthField.addListener(SWT.Modify, passEvents);
		heightField.addListener(SWT.Modify, passEvents);
		foldedField.addListener(SWT.Selection, passEvents);
		onstartField.addListener(SWT.Selection, passEvents);
		alwaysField.addListener(SWT.Selection, passEvents);
		neverField.addListener(SWT.Selection, passEvents);
		overlapField.addListener(SWT.Selection, passEvents);
		shrinkField.addListener(SWT.Selection, passEvents);
		navigationBarField.addListener(SWT.Modify, passEvents);
		annotationBarField.addListener(SWT.Modify, passEvents);
		primColorField.addListener(passPropEvents);
		secColorField.addListener(passPropEvents);
		//ipField.addListener(SWT.Modify, passEvents);
		//portField.addListener(SWT.Modify, passEvents);
		videoTitleField.addListener(SWT.Modify, passEvents);
		autoStartField.addListener(SWT.Modify, passEvents);
		userDiaryField.addListener(SWT.Modify, passEvents);
	}
	
	/**
	 * Repaints the preview with respect to the actual settings.
	 */
	public void repaintPreview() {
		// Resize preview
		if (widthField.getText().isEmpty() || heightField.getText().isEmpty()) {
			previewCanvas.setVisible(false);
			return;
		}
		previewCanvas.setVisible(true);
		float width = Float.valueOf(widthField.getText());
		float height = Float.valueOf(heightField.getText());
		float factor = PREVIEW_MAXWIDTH/width;
		width = PREVIEW_MAXWIDTH;
		height = height * factor;
		
		if (height > PREVIEW_MAXHEIGHT) {
			factor = PREVIEW_MAXHEIGHT/height;
			height = PREVIEW_MAXHEIGHT;
			width = width * factor; 
		}
		previewLayoutData.widthHint = Math.round(width);
		previewLayoutData.heightHint = Math.round(height);
		this.layout();
	}

	/**
	 * Initializes the various fields and widgets with the values stored in the 
	 * project's settings.
	 */
	private void initializeValues() {
		// Resolution
		widthField.setText(String.valueOf(settings.getResolutionWidth()));
		heightField.setText(String.valueOf(settings.getResolutionHeight()));
		// Annotation bar visibility
		setAnnobarVisibility(settings.getAnnobarVisibility());
		// Annotation bar behavior		 
		overlapField.setSelection(settings.isAnnobarOverlayEnabled());
		shrinkField.setSelection(!settings.isAnnobarOverlayEnabled());
		// Side bar widths
		int widthAsPercent = (int)(settings.getNavigationBarWidth()*100);
		navigationBarField.setText(String.valueOf(widthAsPercent));
		widthAsPercent = (int)(settings.getAnnotationBarWidth()*100);
		annotationBarField.setText(String.valueOf(widthAsPercent));
		// Colors
		primColorField.setColorValue(stringToRGB(settings.getPrimaryColor()));
		secColorField.setColorValue(stringToRGB(settings.getSecondaryColor()));
		// Server
	//	ipField.setIpAddress(settings.getServerUrl());
		// Player functions
		userDiaryField.setSelection(settings.isUserDiaryEnabled());
		// Video settings
		autoStartField.setSelection(settings.isAutostartEnabled());
		videoTitleField.setText(settings.getVideoTitle());
	}
	
	@Override
	public void setToDefault() {
		// Resolution
		widthField.setText(String.valueOf(ProjectSettings.DEFAULT_RESOLUTIONWIDTH));
		heightField.setText(String.valueOf(ProjectSettings.DEFAULT_RESOLUTIONHEIGHT));
		// Annotation bar visibility
		setAnnobarVisibility(ProjectSettings.DEFAULT_ANNOBARVISIBILITY);		
		// Annotation bar behavior
		overlapField.setSelection(ProjectSettings.DEFAULT_ANNOTATIONOVERLAY);
		shrinkField.setSelection(!ProjectSettings.DEFAULT_ANNOTATIONOVERLAY);
		// Side bar widths
		int widthAsPercent = (int)(ProjectSettings.DEFAULT_NAVIGATIONBARWIDTH*100);
		navigationBarField.setText(String.valueOf(widthAsPercent));
		widthAsPercent = (int)(ProjectSettings.DEFAULT_ANNOTATIONBARWIDTH*100);
		annotationBarField.setText(String.valueOf(widthAsPercent));
		// Colors
		primColorField.setColorValue(stringToRGB(ProjectSettings.DEFAULT_PRIMARYCOLOR));
		secColorField.setColorValue(stringToRGB(ProjectSettings.DEFAULT_SECONDARYCOLOR));
		// Server
	//	ipField.setIpAddress(ProjectSettings.DEFAULT_SERVERURL);
		// Player functions
		userDiaryField.setSelection(ProjectSettings.DEFAULT_USERDIARY);
		// Video settings
		videoTitleField.setText(ProjectSettings.DEFAULT_VIDEONAME);
		autoStartField.setSelection(ProjectSettings.DEFAULT_AUTOPLAY);	
	}
	
	/**
	 * Returns the <code>AnnobarVisibility</code> appropriate for the
	 * current annotation bar visibility mode selection.
	 * @return selected annotation bar visibility mode
	 */
	private AnnobarVisibility getAnnobarVisibility() {
		if (foldedField.getSelection()) {
			return AnnobarVisibility.FOLDED;
		} else if(onstartField.getSelection()) {
			return AnnobarVisibility.ONSTART;
		} else if (alwaysField.getSelection()) {
			return AnnobarVisibility.ALWAYS;
		} else {
			return AnnobarVisibility.NEVER;
		}
	}
	/**
	 * Selects the appropriate annotation bar visibility button for the given 
	 * annotation bar visibility mode.
	 * @param vis annotation bar visibility mode which should be selected
	 */
	private void setAnnobarVisibility(AnnobarVisibility vis) {
		switch (vis) {
		case FOLDED:
			foldedField.setSelection(true); break;
		case ONSTART:
			onstartField.setSelection(true); break;
		case ALWAYS:
			alwaysField.setSelection(true); break;
		case NEVER:
			neverField.setSelection(true); break;
		}
	}
	
	/**
	 * Converts the given hexadecimal representation of a RGB color to the
	 * corresponding RGB color. The given string has to be of the format
	 * #FFAA00.
	 * @param hexCode
	 * @return
	 */
	private RGB stringToRGB(String hexCode) {
		int intRep = Integer.decode(hexCode);
		int r = (intRep & 0xFF0000) >> 16;
		int g = (intRep & 0xFF00) >> 8;
		int b = (intRep & 0xFF);
		return new RGB(r,g,b);
	}	
	
	@Override
	public boolean checkSettings() {
		warning.setVisible(false);
		checkRange(widthField, MIN_RES_VALUE, MAX_RES_VALUE, true);
		checkRange(heightField, MIN_RES_VALUE, MAX_RES_VALUE, true);
		checkAspect();
		checkRange(navigationBarField, MIN_SIDEBARSIZE, MAX_SIDEBARSIZE, true);
		checkRange(annotationBarField, MIN_SIDEBARSIZE, MAX_SIDEBARSIZE, true);
		//checkIP();
		return !warning.isVisible();		
	}
	
	/**
	 * Checks whether or not the value stored in the given text field is present
	 * (if required) and stays within a given range and sets the warning message
	 * accordingly.
	 * @param textField text field to check
	 * @param min minimum of the range
	 * @param max maximum of the range
	 * @param required whether or not a value has to be entered
	 */
	private void checkRange(Text textField, int min, int max, boolean required) {
		if (warning.isVisible()) {
			textField.setBackground(Display.getCurrent()
					.getSystemColor(SWT.COLOR_WHITE));
			return;
		}
		boolean warn = false;
		if (!textField.getText().isEmpty()) {
			int value = Integer.valueOf(textField.getText());
			if (value < min || value > max) {
				warn = true;
			}
		} else if (required) {
			warn = true;
		} 
		
		if (warn) {
			textField.setBackground(Colors.WARNING_BG.getColor());
			warning.setText(Messages.PlayerSettingsComposite_WarningRange 
							+ min + Messages.PlayerSettingsComposite_WarningRangeTo + max);
			warning.setVisible(true);
		} else {
			textField.setBackground(Display.getCurrent()
					.getSystemColor(SWT.COLOR_WHITE));
		}
	}
	
	private void checkAspect() {
		if (warning.isVisible()) {
			return;
		}
		float width = Float.valueOf(widthField.getText());
		float height = Float.valueOf(heightField.getText());
		float actualAspect = width/height;
		if (actualAspect > MAX_ASPECT || actualAspect < MIN_ASPECT) {
			warning.setText(Messages.PlayerSettingsComposite_WarningAspectRange
					+ MIN_ASPECT + Messages.PlayerSettingsComposite_WarningAspectRangeTo + MAX_ASPECT);
			warning.setVisible(true);
			widthField.setBackground(Colors.WARNING_BG.getColor());
			heightField.setBackground(Colors.WARNING_BG.getColor());
		} else {
			widthField.setBackground(Display.getCurrent()
					.getSystemColor(SWT.COLOR_WHITE));
			heightField.setBackground(Display.getCurrent()
					.getSystemColor(SWT.COLOR_WHITE));
		}
	}
	
	/**
	 * Checks whether or not the IP address field contains a valid entry
	 * and sets the warning message accordingly.
	 * 
	 */
	private void checkIP() {
		if (warning.isVisible()) {
			ipField.setBackground(Display.getCurrent()
					.getSystemColor(SWT.COLOR_WHITE));
			return;
		}
		if (!ipField.isValidAddress() 
				&& !ipField.isEmpty()) {
			ipField.setBackground(Colors.WARNING_BG.getColor());
			warning.setText(Messages.PlayerSettingsComposite_WarningIP);
			warning.setVisible(true);
		} else {
			ipField.setBackground(Display.getCurrent()
					.getSystemColor(SWT.COLOR_WHITE));
		}
	}
	
	@Override
	public ProjectSettings getSettings() {
		ProjectSettings newSettings = new ProjectSettings(settings);
		// Resolution
		newSettings.setResolutionWidth(Integer.valueOf(widthField.getText()));
		newSettings.setResolutionHeight(Integer.valueOf(heightField.getText()));
		// Annotation bar
		newSettings.setAnnobarVisibility(getAnnobarVisibility());
		newSettings.setAnnobarOverlay(overlapField.getSelection());
		// Sidebar widths
		float widthAsFraction = Float.valueOf(navigationBarField.getText())/100;
		newSettings.setNavigationBarWidth(widthAsFraction);
		widthAsFraction = Float.valueOf(annotationBarField.getText())/100;
		newSettings.setAnnotationBarWidth(widthAsFraction);
		// Colors
		newSettings.setPrimaryColor(rgbToString(primColorField.getColorValue()));
		newSettings.setSecondaryColor(rgbToString(secColorField.getColorValue()));
		// Server
		//settings.setServerUrl(ipField.getIpAddress());
		// Player functions
		newSettings.setUserDiary(userDiaryField.getSelection());
		// Video settings
		newSettings.setVideoTitle(videoTitleField.getText());
		newSettings.setAutoStart(autoStartField.getSelection());
		return newSettings;
	}
	
	@Override
	public void updateProjectSettings() {
		if(this.isDirty()){
			ProjectSettings oldSettings = new ProjectSettings(settings);	
			ProjectSettings newSettings = getSettings();								
			IAbstractOperation op = new ProjectSettingsSaveOperation(newSettings, oldSettings);
			try {
				OperationHistory.execute(op);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Checks whether the user has made any changes. 
	 * @return true if changes have been made, false otherwise.
	 */
	private boolean isDirty(){
		
		// Resolution
		if(settings.getResolutionWidth() != Integer.valueOf(widthField.getText())){
			return true;
		}
		if(settings.getResolutionHeight() != Integer.valueOf(heightField.getText())){
			return true;
		}
		
		// Annotation bar visibility
		if(settings.getAnnobarVisibility() != getAnnobarVisibility()){
			return true;
		}
		
		// Annotation bar behaviour
		if(settings.isAnnobarOverlayEnabled() != overlapField.getSelection()){
			return true;
		}

		// Sidebar widths
		if(settings.getNavigationBarWidth() != Float.valueOf(navigationBarField.getText())/100){
			return true;
		}
		if(settings.getAnnotationBarWidth() != Float.valueOf(annotationBarField.getText())/100){
			return true;
		}

		// Colors
		if(!settings.getPrimaryColor().equals(rgbToString(primColorField.getColorValue()))){
			return true;
		}
		if(!settings.getSecondaryColor().equals(rgbToString(secColorField.getColorValue()))){
			return true;
		}

		// Server
		//if(!settings.getServerUrl().equals(ipField.getIpAddress())){
		//	return true;
		//}
		
		// Player functions
		if(settings.isUserDiaryEnabled() != userDiaryField.getSelection()){
			return true;
		}
		
		// Video settings
		if(!settings.getVideoTitle().equals(videoTitleField.getText())){
			return true;
		}
		if(settings.isAutostartEnabled() != autoStartField.getSelection()){
			return true;
		}
		
		return false;
	}
	
	/**
	 * Converts an RGB color to a hexadecimal string representation of the form
	 * #FFAA00.
	 * @param rgb color as RGB value
	 * @return hexadecimal string representation of the color
	 */
	private String rgbToString(RGB rgb) {
		String[] colors = new String[3];
		colors[0] = Integer.toHexString(rgb.red);
		colors[1] = Integer.toHexString(rgb.green);
		colors[2] =	Integer.toHexString(rgb.blue);
		String hexColor = "#"; //$NON-NLS-1$
		for (String c : colors) {
			hexColor += (c.length() < 2 ? "0"+c : c); //$NON-NLS-1$
		}
		return hexColor;
	}
}
