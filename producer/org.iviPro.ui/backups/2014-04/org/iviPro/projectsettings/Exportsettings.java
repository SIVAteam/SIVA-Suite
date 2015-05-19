package org.iviPro.projectsettings;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.iviPro.application.Application;
import org.iviPro.export.ExportType;
import org.iviPro.model.ProjectSettings;

public class Exportsettings extends Composite {

	Combo videoCodec;
	Combo videoContainerFormat;
	Combo videoFileExtension;
	Combo audioCodec;
	Combo audioContainerFormat;
	Combo audioFileExtension;
	Text videoBitrateText;
	Text videoFramerateText;
	Text audioBitrateText;
	Text audioSampleFrequency;
	Text videoSizeWidth;
	Text videoSizeHeight;
//	Text videoAspectRatioWidth;
//	Text videoAspectRatioHeight;

	private boolean settingFields = false;
	private VerifyListener inputVerify = verifyInputs();
	private FocusListener focusLost = lostFocus();

	public Exportsettings(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(3, false));

		GridData twoColumnText = new GridData();
		twoColumnText.widthHint = 140;
//		twoColumnText.heightHint = 16;
		twoColumnText.horizontalSpan = 2;

		GridData singleColumnText = new GridData();
		singleColumnText.widthHint = 140;
//		singleColumnText.heightHint = 16;
		singleColumnText.verticalSpan = 1;

		GridData threeColumn = new GridData();
		threeColumn.horizontalSpan = 3;
		
		Group presets = new Group(this, SWT.CENTER);
		presets.setText(Messages.Exportsettings_PresetsBox);
		presets.setLayout(new GridLayout(3, false));
		presets.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Label dropDownLabel = new Label(presets, SWT.None);
		dropDownLabel.setText(Messages.Exportsettings_DropdownLabel);
		final Combo dropdown = new Combo(presets, SWT.DROP_DOWN | SWT.READ_ONLY);
		dropdown.add(ProjectSettings.FLASH);
		dropdown.add(ProjectSettings.H246);
		dropdown.add(ProjectSettings.OGG);
		dropdown.add(ProjectSettings.SILVERLIGHT);
		dropdown.setLayoutData(singleColumnText);

//		dropdown.addListener(SWT.Selection, new Listener() {
//
//			@Override
//			public void handleEvent(Event event) {
//				setValues(dropdown.getText());
//			}
//		});
		
		GridData buttonSize = new GridData();
		buttonSize.verticalSpan = 1;
		buttonSize.widthHint = 120;
		buttonSize.heightHint = 22;
		Button loadPresets = new Button(presets, SWT.PUSH);
		loadPresets.setText(Messages.Exportsettings_SetValuesFromPreset);
		loadPresets.setToolTipText(Messages.Exportsettings_SetValues_Tooltip);
		loadPresets.setLayoutData(buttonSize);
		loadPresets.addListener(SWT.MouseDown, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				setValues(dropdown.getText());	
			}
		});
		
		Group custom = new Group(this, SWT.CENTER);
		custom.setText(Messages.Exportsettings_CustomBox);
		custom.setLayout(new GridLayout(3, false));
		custom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		// Settings for Videocodec and file
		Label videoCodecLabel = new Label(custom, SWT.None);
		videoCodecLabel.setText(Messages.Exportsettings_Video_Codec_Label);
		videoCodecLabel.setLayoutData(singleColumnText);
		
		videoCodec = new Combo(custom, SWT.DROP_DOWN | SWT.READ_ONLY);
		videoCodec.setLayoutData(twoColumnText);
		String[] videoCodecs = ExportType.getVideoCodecs();
		fillCombo(videoCodec, videoCodecs);
		
		Label videoContainerFormatLabel = new Label(custom, SWT.None);
		videoContainerFormatLabel.setText(Messages.Exportsettings_Video_Containerformat_Label);
		videoContainerFormatLabel.setLayoutData(singleColumnText);
		
		videoContainerFormat = new Combo(custom, SWT.DROP_DOWN | SWT.READ_ONLY);
		videoContainerFormat.setLayoutData(twoColumnText);
		String[] videoContainers = ExportType.getVideoContainerFormats();
		fillCombo(videoContainerFormat, videoContainers);

		Label videoFileExtensionLabel = new Label(custom, SWT.None);
		videoFileExtensionLabel.setText(Messages.Exportsettings_Video_Fileextension_Label);
		videoFileExtensionLabel.setLayoutData(singleColumnText);
		
		videoFileExtension = new Combo(custom, SWT.DROP_DOWN | SWT.READ_ONLY);
		videoFileExtension.setLayoutData(twoColumnText);
		String[] videoExtension = ExportType.getVideoFileExtensions();
		fillCombo(videoFileExtension, videoExtension);
		
		// Settings for Audiocodec and file

		Label audioCodecLabel = new Label(custom, SWT.None);
		audioCodecLabel.setText(Messages.Exportsettings_Audio_Codec_Label);
		audioCodecLabel.setLayoutData(singleColumnText);
		
		audioCodec = new Combo(custom, SWT.DROP_DOWN | SWT.READ_ONLY);
		audioCodec.setLayoutData(twoColumnText);
		String[] audioCodecs = ExportType.getAudioCodecs();
		fillCombo(audioCodec, audioCodecs);

		Label audioContainerFormatLabel = new Label(custom, SWT.None);
		audioContainerFormatLabel.setText(Messages.Exportsettings_Audio_Containerformat_Label);
		audioContainerFormatLabel.setLayoutData(singleColumnText);
		
		audioContainerFormat = new Combo(custom, SWT.DROP_DOWN | SWT.READ_ONLY);
		audioContainerFormat.setLayoutData(twoColumnText);
		String[] audioContainerFormats = ExportType.getAudioContainerFormats();
		fillCombo(audioContainerFormat, audioContainerFormats);

		Label audioFileExtensionLabel = new Label(custom, SWT.None);
		audioFileExtensionLabel.setText(Messages.Exportsettings_Audio_Fileextension_Label);
		audioFileExtensionLabel.setLayoutData(singleColumnText);
		
		audioFileExtension = new Combo(custom, SWT.DROP_DOWN | SWT.READ_ONLY);
		audioFileExtension.setLayoutData(twoColumnText);
		String [] audioExtensions = ExportType.getAudioFileExtensions();
		fillCombo(audioFileExtension, audioExtensions);
		
		Label videoBitrateLabel = new Label(custom, SWT.None);
		videoBitrateLabel.setText(Messages.Exportsettings_BitrateLabel);
		videoBitrateLabel.setLayoutData(singleColumnText);

		videoBitrateText = new Text(custom, SWT.BORDER);
		videoBitrateText.setLayoutData(twoColumnText);
		addListenersToText(videoBitrateText);
		
		Label videoFramerateLabel = new Label(custom, SWT.None);
		videoFramerateLabel.setText(Messages.Exportsettings_FramerateLabel);
		videoFramerateLabel.setLayoutData(singleColumnText);

		videoFramerateText = new Text(custom, SWT.BORDER);
		videoFramerateText.setLayoutData(twoColumnText);
		addListenersToText(videoFramerateText);

		Label audioBitrateLabel = new Label(custom, SWT.None);
		audioBitrateLabel.setLayoutData(singleColumnText);
		audioBitrateLabel.setText(Messages.Exportsettings_Audio_Bitrate_Label);
		
		audioBitrateText = new Text(custom, SWT.BORDER);
		audioBitrateText.setLayoutData(twoColumnText);
		addListenersToText(audioBitrateText);
		
		Label audioSampleFrequencyLabel = new Label(custom, SWT.None);
		audioSampleFrequencyLabel.setLayoutData(singleColumnText);
		audioSampleFrequencyLabel.setText(Messages.Exportsettings_Audio_Sample_Frequency_Label);
		
		audioSampleFrequency = new Text(custom, SWT.BORDER);
		audioSampleFrequency.setLayoutData(twoColumnText);
		addListenersToText(audioSampleFrequency);
		
		Label videoSizeLabel = new Label(custom, SWT.None);
		videoSizeLabel.setText(Messages.Exportsettings_SizeLabel);
		videoSizeLabel.setLayoutData(singleColumnText);

		videoSizeWidth = new Text(custom, SWT.BORDER);
		videoSizeWidth.setLayoutData(singleColumnText);
		addListenersToText(videoSizeWidth);
		
		videoSizeHeight = new Text(custom, SWT.BORDER);
		videoSizeHeight.setLayoutData(singleColumnText);
		
		addListenersToText(videoSizeHeight);

//		Label videoAspectRatioLabel = new Label(custom, SWT.None);
//		videoAspectRatioLabel.setText(Messages.Exportsettings_AspectLabel);
//		videoAspectRatioLabel.setLayoutData(singleColumnText);
//
//		videoAspectRatioWidth = new Text(custom, SWT.BORDER | SWT.READ_ONLY);
//		videoAspectRatioWidth.setLayoutData(singleColumnText);
//		addListenersToText(videoAspectRatioWidth);
//		videoAspectRatioHeight = new Text(custom, SWT.BORDER | SWT.READ_ONLY);
//		videoAspectRatioHeight.setLayoutData(singleColumnText);
//		addListenersToText(videoAspectRatioHeight);

		setValues(null);
		
		pack();
		

	}
	
	private void addListenersToText(Text text) {
		text.addVerifyListener(inputVerify);
		text.addFocusListener(focusLost);
	}
	
	private void fillCombo(Combo combo, String[] items) {
		for (String s: items) {
			combo.add(s);
		}
	}
	
	protected void setDefaults() {
		setValues(ProjectSettings.FLASH);
	}

	protected HashMap<String, String> getExportSettings() {
		HashMap<String, String> export = new HashMap<String, String>();
		ProjectSettings settings = Application.getCurrentProject()
				.getSettings();
		String bitrate = getValidString(videoBitrateText.getText());
		String framerate = getValidString(videoFramerateText.getText());
		String size = getValidString(videoSizeWidth.getText())
				+ "x" + getValidString(videoSizeHeight.getText()); //$NON-NLS-1$
//		String aspect = getValidString(videoAspectRatioWidth.getText())
//				+ ":" + getValidString(videoAspectRatioHeight.getText()); //$NON-NLS-1$
		String videoCodec = this.videoCodec.getText();
		String videoContainerFormat = this.videoContainerFormat.getText();
		String videoFileExtension = this.videoFileExtension.getText();
		String audioCodec = this.audioCodec.getText();
		String audioContainerFormat = this.audioContainerFormat.getText();
		String audioFileExtension = this.audioFileExtension.getText();
		String audioBitrate = getValidString(audioBitrateText.getText());
		String audioSampleFrequency = getValidString(this.audioSampleFrequency.getText());
		export.put(settings.VIDEOBITRATE, bitrate);
		export.put(settings.VIDEOFRAMERATE, framerate);
		export.put(settings.VIDEOSIZE, size);
//		export.put(settings.ASPECTRATIO, aspect);
		export.put(ProjectSettings.VIDEOCODEC, videoCodec);
		export.put(ProjectSettings.VIDEOCONTAINER, videoContainerFormat);
		export.put(ProjectSettings.VIDEOEXTENSION, videoFileExtension);
		export.put(ProjectSettings.AUDIOCODEC, audioCodec);
		export.put(ProjectSettings.AUDIOCONTAINER, audioContainerFormat);
		export.put(ProjectSettings.AUDIOEXTENSION, audioFileExtension);
		export.put(settings.AUDIOBITRATE, audioBitrate);
		export.put(settings.AUDIOSAMPLEFREQUENCY, audioSampleFrequency);
		return export;
	}
	
	private String getValidString(String input) {
		if (input.equals("")) { //$NON-NLS-1$
			return "0"; //$NON-NLS-1$
		} else {
			return input;
		}
	}

	private void setValues(String type) {
		settingFields = true;
		ProjectSettings settings = Application.getCurrentProject()
				.getSettings();
		HashMap<String, String> exportSettings = settings
				.getExportSettings(type);

		String videoCodecText = (exportSettings.get(ProjectSettings.VIDEOCODEC));
		setComboToString(videoCodec, videoCodecText);
		String videoContainerFormatText = (exportSettings.get(ProjectSettings.VIDEOCONTAINER));
		setComboToString(videoContainerFormat, videoContainerFormatText);
		String videoFileExtensionText = exportSettings.get(ProjectSettings.VIDEOEXTENSION);
		setComboToString(videoFileExtension, videoFileExtensionText);
		String audioCodecText = (exportSettings.get(ProjectSettings.AUDIOCODEC));
		setComboToString(audioCodec, audioCodecText);
		String audioContainerFormatText = (exportSettings.get(ProjectSettings.AUDIOCONTAINER));
		setComboToString(audioContainerFormat, audioContainerFormatText);
		String audioFileExtensionText = exportSettings.get(ProjectSettings.AUDIOEXTENSION);
		setComboToString(audioFileExtension, audioFileExtensionText);
		videoBitrateText.setText(exportSettings.get(settings.VIDEOBITRATE));
		videoFramerateText.setText(exportSettings.get(settings.VIDEOFRAMERATE));
		audioBitrateText.setText(exportSettings.get(settings.AUDIOBITRATE));
		audioSampleFrequency.setText(exportSettings.get(settings.AUDIOSAMPLEFREQUENCY));
		String[] vidSize = exportSettings.get(settings.VIDEOSIZE).split("x"); //$NON-NLS-1$
		if (vidSize.length == 1) {
			vidSize = exportSettings.get(settings.VIDEOSIZE).split("\\*"); //$NON-NLS-1$
		}
		videoSizeWidth.setText(vidSize[0]);
		videoSizeHeight.setText(vidSize[1]);

//		String[] aspect = exportSettings.get(settings.ASPECTRATIO).split(":"); //$NON-NLS-1$
//		videoAspectRatioWidth.setText(aspect[0]);
//		videoAspectRatioHeight.setText(aspect[1]);
		
		settingFields = false;
	}
	
	private void setComboToString(Combo combo, String text) {
		String[] allItems = combo.getItems();
		for (int i = 0; i<allItems.length;i++) {
			if (allItems[i].equals(text)) {
				combo.select(i);
				return;
			}
		}
	}

	private VerifyListener verifyInputs() {
		return new VerifyListener() {

			@Override
			public void verifyText(VerifyEvent e) {
				if (settingFields) {
					e.doit = true;
					return;
				}
				e.doit = false;
				char myChar = e.character;
				// Allow 0-9
				if (Character.isLetter(myChar))
					e.doit = false;
				if (Character.isDigit(myChar))
					e.doit = true;

				// Allow backspace
				if (myChar == '\b' || myChar == SWT.DEL)
					e.doit = true;

			}
		};
	}
	
	
	/**
	 * Setzt leere Felder beim verlieren des Fokus auf "0" um korrekte 
	 * Werte in den Feldern zu haben
	 * @return
	 */
	private FocusListener lostFocus() {
		return new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				if (e.widget instanceof Text) {
					Text text = (Text) e.widget;
					if (text.getText().equals("")) { //$NON-NLS-1$
						settingFields = true;
						text.setText("0"); //$NON-NLS-1$
						settingFields = false;
					}
				}
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
		};
	}

}
