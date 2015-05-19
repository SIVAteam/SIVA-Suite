package org.iviPro.editors.audioeditor.components;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.iviPro.application.Application;
import org.iviPro.editors.audioeditor.AudioEditor;
import org.iviPro.editors.common.BeanNameGenerator;
import org.iviPro.editors.common.EditTime;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.mediaaccess.player.I_MediaPlayer;
import org.iviPro.mediaaccess.player.controls.SivaScale;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.audio.AudioPartChangeOperation;
import org.iviPro.theme.Icons;
import org.iviPro.utils.ImageHelper;
import org.iviPro.utils.SivaTime;

public class AudioPartDefineWidget extends Composite {

	private static Logger logger = Logger.getLogger(AudioPartDefineWidget.class);

	// der MediaPlayer
	private I_MediaPlayer mp;

	// die zum AudioPartDefineWidget gehörende Audio-Part
	private final AudioPart audioPart;

	// die Start und Endzeit des Audio-Part, relative Zeit
	private SivaTime tmpStart;
	private SivaTime tmpEnd;

	// der Name des Audio-Part
	private Text tmpTitle;

	// das zum Audio-Part gehörende Tab Item
	private CTabItem item = null;

	// die aktuelle Medienzeit
	private SivaTime currentMediaTime = new SivaTime(0);
	
	// Eingabefeld für Keywords
	private Text tmpKeywords;
	
	// der AudioEditor (wird für das neu anlegen eines Audio-Part benötigt
	private AudioEditor editor;

	public AudioPartDefineWidget(Composite parent, int style, CTabItem item, final AudioPart audioPart, final I_MediaPlayer mp, AudioEditor editor) {
		
		super(parent, style);

		// setze den Movieplayer
		this.mp = mp;
		this.item = item;
		this.audioPart = audioPart;
		this.editor = editor;

		tmpStart = new SivaTime(audioPart.getStart() - mp.getStartTime().getNano());
		tmpEnd = new SivaTime(audioPart.getEnd() - mp.getStartTime().getNano());
		
		// setze das Layout und den Inhalt
		// das sdWidget soll so weit oben wie möglich und Centered sein
		GridData scwGrid = new GridData(SWT.CENTER, SWT.TOP, true, true);
		scwGrid.widthHint = 620;
		setLayoutData(scwGrid);
		
		// setze 3-spaltiges GridLayout für die AudioPartDefine Komponenten
		GridLayout sdWLayout = new GridLayout(3, false);
		setLayout(sdWLayout);
		createContent();
	}

	/**
	 * erstellt den Content
	 */
	private void createContent() {
		Composite scaleComp = new Composite(this, SWT.CENTER);
		GridData scaleCompGD = new GridData(SWT.FILL, SWT.FILL, true, false);	
		scaleCompGD.horizontalSpan = 3;
		scaleComp.setLayoutData(scaleCompGD);

		GridLayout scaleCompGL = new GridLayout(2, false);
		scaleCompGL.marginWidth = 0;
		scaleComp.setLayout(scaleCompGL);
		
		// Skala erstellen, jeder Audio-Part besitzt eine eigene Skala
		final SivaScale scale = new SivaScale(scaleComp, mp.getDuration().getNano(), 538, 40, true, true, true);
		scale.setToolTip(Messages.AudioPartDefineWidgetScaleTooltip);
				
		// Container für die Buttons
		Composite defineAudioPartButtons = new Composite(scaleComp, SWT.TOP | SWT.CENTER);

		// Layout für die Buttons
		GridLayout layoutAudioPartControls = new GridLayout(3, false);
		layoutAudioPartControls.marginWidth = 0;
		layoutAudioPartControls.marginHeight = 0;
		layoutAudioPartControls.verticalSpacing = 0;
		layoutAudioPartControls.horizontalSpacing = 0;
		defineAudioPartButtons.setLayout(layoutAudioPartControls);

		// Button zur Definition des Audio-Part Start
		final Button cutStart = new Button(defineAudioPartButtons, SWT.CENTER);
		// Bild für den Button
		ImageHelper.setButtonImage(cutStart, Icons.ACTION_SCENE_CUT_START);
		cutStart.setToolTipText(Messages.AudioPartDefineWidget_StartPosition);		

		// Button zur Definition des Audio-Part Ende
		final Button cutEnd = new Button(defineAudioPartButtons, SWT.CENTER);
		// Bild für den Button
		ImageHelper.setButtonImage(cutEnd, Icons.ACTION_SCENE_CUT_END);
		cutEnd.setToolTipText(Messages.AudioPartDefineWidget_StopPosition);

		// Button zum Speichern des AudioPart
		final Button saveButton = new Button(defineAudioPartButtons, SWT.CENTER);
		saveButton.setText(Messages.AudioPartDefineWidget_Text_SaveButton);
		saveButton.setToolTipText(Messages.AudioPartDefineWidget_Tooltip_SaveButton);
		GridData saveButtonGD = new GridData();
		saveButtonGD.horizontalIndent = 30;
		saveButton.setLayoutData(saveButtonGD);
		// Bild für den Button
		ImageHelper.setButtonImage(saveButton, Icons.ACTION_SCENE_SAVE);
		saveButton.setToolTipText(Messages.AudioPartDefineWidget_SaveButton);
		saveButton.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				saveAudioPart();
			}
		});
		
		// Button zum Speichern des Audio-Part und zum Anlegen eines neuen
		final Button saveCreateButton = new Button(defineAudioPartButtons, SWT.CENTER);
		saveCreateButton.setText(Messages.AudioPartDefineWidget_Text_SaveCreateButton);
		saveCreateButton.setToolTipText(Messages.AudioPartDefineWidget_Tooltip_SaveCreateButton);
		GridData savecreButtonGD = new GridData();
		savecreButtonGD.horizontalIndent = 86;
		savecreButtonGD.horizontalSpan = 3;
		saveCreateButton.setLayoutData(savecreButtonGD);
		// Bild für den Button
		ImageHelper.setButtonImage(saveCreateButton, Icons.ACTION_SCENE_CREATE_SAVE);
		saveCreateButton.setToolTipText(Messages.AudioPartDefineWidget_SaveCreateButton);
		saveCreateButton.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				saveAudioPart();
				editor.createNewAudioPart();
			}
		});

		Composite defineComp = new Composite(this, SWT.LEFT | SWT.BORDER);
		GridLayout layoutdefineComp = new GridLayout(2, true);			
		defineComp.setLayout(layoutdefineComp);
		
		Composite nameComposite = new Composite(defineComp, SWT.CENTER);
		GridLayout nameCompositeGL = new GridLayout(2, false);
		nameCompositeGL.marginWidth = 0;
		nameComposite.setLayout(nameCompositeGL);
		
		Group groupName = new Group(nameComposite, SWT.CENTER);
		groupName.setLayout(new GridLayout(1, false));
		groupName.setText(Messages.AudioPartDefineWidget_Label_AudioPartName);
		GridData groupNameGD = new GridData();
		groupNameGD.horizontalSpan = 2;
		groupName.setLayoutData(groupNameGD);
		tmpTitle = new Text(groupName, SWT.SINGLE | SWT.BORDER);
		tmpTitle.setText(audioPart.getTitle());
		GridData tmpTitleGD = new GridData();
		tmpTitleGD.widthHint = 160;
		tmpTitle.setLayoutData(tmpTitleGD);

		tmpTitle.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				// setze die AudioPart auf dirty
				markDirty();
			}
		});
				
		
		// Eingabefeld für die Keywords
		Composite keywordsComposite = new Composite(defineComp, SWT.CENTER);
		GridLayout keywordsCompositeGL = new GridLayout(1, false);
		keywordsCompositeGL.marginWidth = 0;
		keywordsComposite.setLayout(keywordsCompositeGL);
		GridData keywordsCompositeGD = new GridData();
		keywordsCompositeGD.verticalSpan = 2;
		keywordsCompositeGD.verticalAlignment = SWT.TOP;
		keywordsComposite.setLayoutData(keywordsCompositeGD);
		Group kwGroup = new Group(keywordsComposite, SWT.CENTER);
		kwGroup.setLayout(new GridLayout(1, false));
		kwGroup.setText(Messages.AudioPartDefineWidget_Label_Keywords);
		tmpKeywords = new Text(kwGroup, SWT.V_SCROLL | SWT.MULTI | SWT.WRAP | SWT.BORDER);
		GridData tmpKeywordsGD = new GridData(220, 50);
		tmpKeywords.setLayoutData(tmpKeywordsGD);
		tmpKeywords.setText(audioPart.getKeywords());
		tmpKeywords.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				// setze die AudioPart auf dirty
				markDirty();
			}
		});

		// erstelle das Composite zum Editieren der Zeit
		// der EditTime Editor arbeitet mit der relativen Zeit zum Audio-Part
		// => die Startzeit des Audio-Part muss noch abgezogen werden
		final EditTime editTime = new EditTime(nameComposite, SWT.CENTER, tmpStart, tmpEnd, mp.getDuration().getNano());
				
		GridData editTimeGD = new GridData();
		editTimeGD.horizontalSpan = 1;
		editTime.setLayoutData(editTimeGD);		
						
		cutStart.addListener(SWT.MouseDown, new Listener() {			
			public void handleEvent(Event e) {
				currentMediaTime = mp.getMediaTime();
				if (currentMediaTime.compareTo(tmpEnd) >= 0) {
					Shell shell = new Shell(Display.getDefault());
					MessageDialog
							.openInformation(
									shell,
									org.iviPro.editors.audioeditor.components.Messages.AudioPartDefineWidget_Define,
									org.iviPro.editors.audioeditor.components.Messages.AudioPartDefineWidget_Start);
				} else {
					tmpStart = mp.getMediaTime();	
					SivaEvent event = new SivaEvent(null, SivaEventType.STARTTIME_CHANGED, mp.getMediaTime());
					scale.setSashes(event);
					editTime.setValue(event);
					SivaEvent forwardEvent = new SivaEvent(null, SivaEventType.MARK_POINT_START, mp.getMediaTime());
					mp.forwardEvent(forwardEvent);
				}
			}
		});
		
		cutEnd.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				// Endzeit darf nicht vor der Startzeit liegen
				// Endzeit darf nicht vor der Startzeit liegen
				currentMediaTime = mp.getMediaTime();
				if (currentMediaTime.compareTo(tmpStart) < 0) {
					Shell shell = new Shell(Display.getDefault());
					MessageDialog
							.openInformation(
									shell,
									org.iviPro.editors.audioeditor.components.Messages.AudioPartDefineWidget_Define,
									org.iviPro.editors.audioeditor.components.Messages.AudioPartDefineWidget_End);
				} else {
					tmpEnd = mp.getMediaTime();
					SivaEvent event = new SivaEvent(null, SivaEventType.ENDTIME_CHANGED, mp.getMediaTime());
					scale.setSashes(event);
					editTime.setValue(event);
					SivaEvent forwardEvent = new SivaEvent(null, SivaEventType.MARK_POINT_START, mp.getMediaTime());
					mp.forwardEvent(forwardEvent);
				}
			}
		});
		
		// Listener auf Edit Time
		editTime.addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {
				if (event.getEventType().equals(SivaEventType.STARTTIME_CHANGED)) {
					tmpStart = event.getTime();
					mp.setMediaTime(event);
					scale.setSashes(event);
				} else
				if (event.getEventType().equals(SivaEventType.ENDTIME_CHANGED)) {
					tmpEnd = event.getTime();
					mp.setMediaTime(event);
					scale.setSashes(event);
				}
				scale.addMarkPoint(event.getTime().getNano(), ""); //$NON-NLS-1$
				markDirty();
			}			
		});
		
		// Listener auf die Skala
		scale.addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {

				SivaEvent forwardEvent = null;
				if (event.getEventType().equals(SivaEventType.STARTTIME_CHANGED)) {
					tmpStart = event.getTime();
					forwardEvent = new SivaEvent(null, SivaEventType.MARK_POINT_START, event.getTime());
					editTime.setValue(event);
				} else
				if (event.getEventType().equals(SivaEventType.ENDTIME_CHANGED)) {
					tmpEnd = event.getTime();
					forwardEvent = new SivaEvent(null, SivaEventType.MARK_POINT_END, event.getTime());
					editTime.setValue(event);
				}	
				mp.setMediaTime(event);
				mp.forwardEvent(forwardEvent);
				markDirty();
			}			
		});
		
		mp.addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {
				// ignoriere die Events des MediaPlayers die von der Skala ausgelöst wurden
				if (event.getSource() != null && event.getSource().equals(scale)) {
					return;
				}
				if (event.getEventType().equals(SivaEventType.MEDIATIME_CHANGED)) {
					scale.addMarkPoint(event.getTime().getNano(), ""); //$NON-NLS-1$
				}
			}			
		});
		
		// Property Change Listener auf die AudioPart (wird z.B. beim Speichern aufgerufen)
		audioPart.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (isDisposed()) {
					return;
				}
				if (audioPart != null) {
					tmpTitle.setText(audioPart.getTitle());
					tmpStart = new SivaTime(audioPart.getStart() - mp.getStartTime().getNano());
					tmpEnd = new SivaTime(audioPart.getEnd() - mp.getStartTime().getNano());
					tmpKeywords.setText(audioPart.getKeywords());
					SivaEvent eventStart = new SivaEvent(null, SivaEventType.STARTTIME_CHANGED, tmpStart);
					SivaEvent eventEnd = new SivaEvent(null, SivaEventType.ENDTIME_CHANGED, tmpEnd);
					scale.setSashes(eventStart);
					scale.setSashes(eventEnd);
					editTime.setValue(eventStart);
					editTime.setValue(eventEnd);
				}
			}			
		});
		
		SivaEvent startEvent = new SivaEvent(null, SivaEventType.STARTTIME_CHANGED, tmpStart);
		scale.setSashes(startEvent);
		SivaEvent endEvent = new SivaEvent(null, SivaEventType.ENDTIME_CHANGED, tmpEnd);
		scale.setSashes(endEvent);	
		scale.addMarkPoint(tmpStart.getNano(), ""); //$NON-NLS-1$
		
		markDirty();
	}

	/**
	 * gibt den Audio-Part des AudioPartDefineWidgets zurück
	 * 
	 * @return AudioPart
	 */
	public AudioPart getAudioPart() {
		return this.audioPart;
	}

	/*
	 * aktualisiert den Tabnamen wird bei jeder Änderungsaktion im Editor
	 * aufgerufen
	 */
	private void markDirty() {
		if (isDirty()) {
			if (tmpTitle.getText().length() != 0) {
				item.setText(tmpTitle.getText() + "*"); //$NON-NLS-1$
			} else {
				if (!item.getText().endsWith("*")) { //$NON-NLS-1$
					item.setText(item.getText() + "*"); //$NON-NLS-1$
				}
			}
		} else {
			item.setText(tmpTitle.getText());
		}
	}

	/**
	 * prüft ob die in dem Audio-Part gespeicherten Werte von den eingegebenen
	 * abweichen, falls ja => true
	 * 
	 * @return
	 */
	public boolean isDirty() {

		// prüft ob schon eine AudioPart mit diesem Namen existiert, z.B. bei einer neuen AudioPart
		if (audioPart.getAudio().getAudioPart(audioPart.getLocalizedTitle().getValue(), Application.getCurrentLanguage()) == null)  {
			return true;
		}
		if (!audioPart.getTitle().equals(tmpTitle.getText())) {
			return true;
		}
		if (!audioPart.getStart().equals(tmpStart.addTime(mp.getStartTime()))) {
			return true;
		}
		if (!audioPart.getEnd().equals(tmpEnd.addTime(mp.getStartTime()))) {
			return true;
		}
		if (!audioPart.getKeywords().equals(tmpKeywords.getText())) {
			return true;
		}
		return false;
	}

	/**
	 * speichert den Audio-Part ab
	 */
	public void saveAudioPart() {
		logger.debug("Creating/Changing Audio-Part: " + tmpTitle.getText()); //$NON-NLS-1$
		BeanNameGenerator nameGen = new BeanNameGenerator(tmpTitle.getText(), audioPart, audioPart.getAudio().getAudioParts(), ""); //$NON-NLS-1$
		String newTitle = nameGen.generate();
		// falls die Namensgenerierung abgebrochen wurde, wird nicht gespeichert
		if (!nameGen.getCancelState()) {
			tmpTitle.setText(newTitle);	
			if (isDirty()) {
				IAbstractOperation op = new AudioPartChangeOperation(audioPart, newTitle, new SivaTime(tmpStart.addTime(mp.getStartTime())), new SivaTime(tmpEnd.addTime(mp.getStartTime())), tmpKeywords.getText());
				try {
					OperationHistory.execute(op);
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				tmpTitle.setText(audioPart.getTitle());
				item.setText(audioPart.getTitle());
			}	
		}
	}
}
