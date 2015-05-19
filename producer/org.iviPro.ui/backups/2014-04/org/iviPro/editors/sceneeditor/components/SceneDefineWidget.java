package org.iviPro.editors.sceneeditor.components;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Label;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.iviPro.actions.nondestructive.ProjectSaveAction;
import org.iviPro.application.Application;
import org.iviPro.editors.common.BeanNameGenerator;
import org.iviPro.editors.common.EditTime;
import org.iviPro.editors.common.EditTimeSingle;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.sceneeditor.DefineScenesEditor;
import org.iviPro.editors.sceneeditor.DefineScenesShotInsertEditorInput;
import org.iviPro.mediaaccess.player.I_MediaPlayer;
import org.iviPro.mediaaccess.player.controls.SivaScale;
import org.iviPro.model.Scene;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.video.SceneChangeOperation;
import org.iviPro.theme.Icons;
import org.iviPro.utils.SivaTime;
import org.iviPro.utils.ImageHelper;
import org.iviPro.model.Video;

import org.iviPro.scenedetection.sd_main.SDTime;
import org.iviPro.scenedetection.shd_algorithm.Cut;

public class SceneDefineWidget extends Composite {

	private static Logger logger = Logger.getLogger(SceneDefineWidget.class);

	// der MediaPlayer
	private I_MediaPlayer mp;

	// die zum SceneDefineWidget gehörende Scene
	private final Scene scene;

	// die Start und Endzeit der Szene, relative Zeit
	private SivaTime tmpStart;
	private SivaTime tmpEnd;

	// der Name der Szene
	private Text tmpTitle;

	// das zur Szene gehörende Tab Item
	private CTabItem item = null;

	// die aktuelle Medienzeit
	private SivaTime currentMediaTime = new SivaTime(0);

	// Eingabefeld für Keywords
	private Text tmpKeywords;

	// der Szeneneditor (wird für das neu anlegen einer Szene benötigt
	private final DefineScenesEditor editor;

	private final Composite parent;

	public SceneDefineWidget(Composite parent, int style, CTabItem item,
			final Scene scene, final I_MediaPlayer mp, DefineScenesEditor editor) {

		super(parent, style);

		this.parent = parent;

		// setze den Movieplayer
		this.mp = mp;
		this.item = item;
		this.scene = scene;
		this.editor = editor;

		tmpStart = new SivaTime(scene.getStart() - mp.getStartTime().getNano());
		tmpStart.setFrame(mp.getFrameForNanos(scene.getStart() - mp.getStartTime().getNano()));
		tmpEnd = new SivaTime(scene.getEnd() - mp.getStartTime().getNano());
		tmpEnd.setFrame(mp.getFrameForTime(tmpEnd));

		// setze das Layout und den Inhalt
		// das sdWidget soll so weit oben wie möglich und Centered sein
		GridData scwGrid = new GridData(SWT.CENTER, SWT.TOP, true, true);
		scwGrid.widthHint = 620;
		setLayoutData(scwGrid);

		// setze 3-spaltiges GridLayout für die SceneDefine Komponenten
		GridLayout sdWLayout = new GridLayout(3, false);
		setLayout(sdWLayout);
		createContent();
	}

	/**
	 * erstellt den Content
	 */
	private void createContent() {
		if (editor.getEditorInput() instanceof DefineScenesShotInsertEditorInput) {
			createContentShotAndSceneInsert();
			return;
		}
		Composite scaleComp = new Composite(this, SWT.CENTER);
		GridData scaleCompGD = new GridData(SWT.FILL, SWT.FILL, true, false);
		scaleCompGD.horizontalSpan = 3;
		scaleComp.setLayoutData(scaleCompGD);

		GridLayout scaleCompGL = new GridLayout(2, false);
		scaleCompGL.marginWidth = 0;
		scaleComp.setLayout(scaleCompGL);

		// Skala erstellen, jede Szene besitzt eine eigene Skala
		final SivaScale scale = new SivaScale(scaleComp, mp.getDuration()
				.getNano(), 538, 40, true, true, true);
		scale.setToolTip(Messages.SceneDefineWidgetScaleTooltip);

		// Container für die Buttons
		Composite defineSceneButtons = new Composite(scaleComp, SWT.TOP
				| SWT.CENTER);

		// Layout für die Buttons
		GridLayout layoutSceneControls = new GridLayout(3, false);
		layoutSceneControls.marginWidth = 0;
		layoutSceneControls.marginHeight = 0;
		layoutSceneControls.verticalSpacing = 0;
		layoutSceneControls.horizontalSpacing = 0;
		defineSceneButtons.setLayout(layoutSceneControls);

		// Button zur Definition des Szenenstarts
		final Button cutStart = new Button(defineSceneButtons, SWT.CENTER);
		// Bild für den Button
		ImageHelper.setButtonImage(cutStart, Icons.ACTION_SCENE_CUT_START);
		cutStart.setToolTipText(Messages.SceneDefineWidget_StartPosition);

		// Button zur Definition des Szenenendes
		final Button cutEnd = new Button(defineSceneButtons, SWT.CENTER);
		// Bild für den Button
		ImageHelper.setButtonImage(cutEnd, Icons.ACTION_SCENE_CUT_END);
		cutEnd.setToolTipText(Messages.SceneDefineWidget_StopPosition);

		// Button zum Speichern der Szene
		final Button saveButton = new Button(defineSceneButtons, SWT.CENTER);
		saveButton.setText(Messages.SceneDefineWidget_Text_SaveButton);
		saveButton
				.setToolTipText(Messages.SceneDefineWidget_Tooltip_SaveButton);
		GridData saveButtonGD = new GridData();
		saveButtonGD.horizontalIndent = 30;
		saveButton.setLayoutData(saveButtonGD);
		// Bild für den Button
		ImageHelper.setButtonImage(saveButton, Icons.ACTION_SCENE_SAVE);
		saveButton.setToolTipText(Messages.SceneDefineWidget_SaveButton);
		saveButton.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				saveScene();
			}
		});

		// Button zum Speichern der Szene und dem anlegen einer neuen
		final Button saveCreateButton = new Button(defineSceneButtons,
				SWT.CENTER);
		saveCreateButton
				.setText(Messages.SceneDefineWidget_Text_SaveCreateButton);
		saveCreateButton
				.setToolTipText(Messages.SceneDefineWidget_Tooltip_SaveCreateButton);
		GridData savecreButtonGD = new GridData();
		savecreButtonGD.horizontalIndent = 86;
		savecreButtonGD.horizontalSpan = 3;
		saveCreateButton.setLayoutData(savecreButtonGD);
		// Bild für den Button
		ImageHelper.setButtonImage(saveCreateButton,
				Icons.ACTION_SCENE_CREATE_SAVE);
		saveCreateButton
				.setToolTipText(Messages.SceneDefineWidget_SaveCreateButton);
		saveCreateButton.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				saveScene();
				editor.createNewScene();
			}
		});

		Composite defineComp = new Composite(this, SWT.LEFT | SWT.BORDER);
		GridLayout layoutdefineComp = new GridLayout(2, true);
		defineComp.setLayout(layoutdefineComp);

		Composite nameComposite = new Composite(defineComp, SWT.CENTER);
		GridLayout nameCompositeGL = new GridLayout(1, false);
		nameCompositeGL.marginWidth = 0;
		nameComposite.setLayout(nameCompositeGL);

		Group groupName = new Group(nameComposite, SWT.CENTER);
		groupName.setLayout(new GridLayout(1, false));
		groupName.setText(Messages.SceneDefineWidget_Label_SceneName);
		GridData groupNameGD = new GridData();
		groupNameGD.horizontalSpan = 1;
		groupName.setLayoutData(groupNameGD);
		tmpTitle = new Text(groupName, SWT.SINGLE | SWT.BORDER);
		tmpTitle.setText(scene.getTitle());
		GridData tmpTitleGD = new GridData();
		tmpTitleGD.widthHint = 197;
		tmpTitle.setLayoutData(tmpTitleGD);

		tmpTitle.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				// setze die Scene auf dirty
				markDirty();
			}
		});

	

		// erstelle das Composite zum Editieren der Zeit
		// der EditTime Editor arbeitet mit der relativen Zeit zur Szene
		// => die Startzeit der Szene muss noch abgezogen werden
		Group editTimeGroup = new Group(defineComp, SWT.NONE);
		editTimeGroup.setLayout(new GridLayout(1, false));
		editTimeGroup.setText(Messages.SceneDefineWidget_PositionInput_inTime);
		
		final EditTime editTime = new EditTime(editTimeGroup, SWT.CENTER,
				tmpStart, tmpEnd, mp.getDuration().getNano());		
		GridData editTimeGD = new GridData(225,70);
		editTime.setLayoutData(editTimeGD);

		
		
		// Eingabefeld für die Keywords
		Composite keywordsComposite = new Composite(defineComp, SWT.CENTER);
		GridLayout keywordsCompositeGL = new GridLayout(1, false);
		keywordsCompositeGL.marginWidth = 0;
		keywordsComposite.setLayout(keywordsCompositeGL);
		GridData keywordsCompositeGD = new GridData();
		keywordsCompositeGD.verticalAlignment = SWT.TOP;
		keywordsComposite.setLayoutData(keywordsCompositeGD);
		Group kwGroup = new Group(keywordsComposite, SWT.CENTER);
		kwGroup.setLayout(new GridLayout(1, false));
		kwGroup.setText(Messages.SceneDefineWidget_Label_Keywords);
		kwGroup.setToolTipText(Messages.SceneDefineWidget_Label_KeywordsTooltip);
		tmpKeywords = new Text(kwGroup, SWT.V_SCROLL | SWT.MULTI | SWT.WRAP
				| SWT.BORDER);
		GridData tmpKeywordsGD = new GridData(180, 50);
		tmpKeywords.setLayoutData(tmpKeywordsGD);
		tmpKeywords.setText(scene.getKeywords());
		tmpKeywords.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				// setze die Scene auf dirty
				markDirty();
			}
		});
		
		Group editFrameGroup = new Group(defineComp, SWT.NONE);
		editFrameGroup.setLayout(new GridLayout(1, false));
		editFrameGroup.setText(Messages.SceneDefineWidget_PositionInput_InFrames);
		final FrameCutWidget frameCut = new FrameCutWidget(editFrameGroup,
				SWT.None, tmpStart, tmpEnd, mp);
		GridData editFrameGd = new GridData(225,55);
		frameCut.setLayoutData(editFrameGd);

		cutStart.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				currentMediaTime = mp.getMediaTime();
				if (currentMediaTime.compareTo(tmpEnd) >= 0) {
					Shell shell = new Shell(Display.getDefault());
					MessageDialog
							.openInformation(
									shell,
									org.iviPro.editors.sceneeditor.components.Messages.SceneDefineWidget_Define,
									org.iviPro.editors.sceneeditor.components.Messages.SceneDefineWidget_Start);
				} else {
					tmpStart = mp.getMediaTime();
					SivaEvent event = new SivaEvent(null,
							SivaEventType.STARTTIME_CHANGED, mp.getMediaTime());
					scale.setSashes(event);
					editTime.setValue(event);
					frameCut.setValue(event);
					SivaEvent forwardEvent = new SivaEvent(null,
							SivaEventType.MARK_POINT_START, mp.getMediaTime());
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
									org.iviPro.editors.sceneeditor.components.Messages.SceneDefineWidget_Define,
									org.iviPro.editors.sceneeditor.components.Messages.SceneDefineWidget_End);
				} else {
					tmpEnd = mp.getMediaTime();
					SivaEvent event = new SivaEvent(null,
							SivaEventType.ENDTIME_CHANGED, mp.getMediaTime());
					scale.setSashes(event);
					editTime.setValue(event);
					frameCut.setValue(event);
					SivaEvent forwardEvent = new SivaEvent(null,
							SivaEventType.MARK_POINT_START, mp.getMediaTime());
					mp.forwardEvent(forwardEvent);
				}
			}
		});

		// Listener auf Edit Time
		editTime.addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {
				if (event.getEventType()
						.equals(SivaEventType.STARTTIME_CHANGED)) {
					tmpStart = event.getTime();
					tmpStart.setFrame(mp.getFrameForTime(tmpStart));
					mp.setMediaTime(event);
					scale.setSashes(event);
					frameCut.setValue(event);
				} else if (event.getEventType().equals(
						SivaEventType.ENDTIME_CHANGED)) {
					tmpEnd = event.getTime();
					mp.setMediaTime(event);
					scale.setSashes(event);
					frameCut.setValue(event);
				}
				scale.addMarkPoint(event.getTime().getNano(), ""); //$NON-NLS-1$
				markDirty();
			}
		});

		frameCut.addSivaEventConsumer(new SivaEventConsumerI() {

			@Override
			public void handleEvent(SivaEvent event) {
				if (event.getEventType()
						.equals(SivaEventType.STARTTIME_CHANGED)) {
					tmpStart = event.getTime();
					tmpStart.setFrame(mp.getFrameForTime(tmpStart));
					mp.setMediaTime(event);
					scale.setSashes(event);
					editTime.setValue(event);
				} else if (event.getEventType().equals(
						SivaEventType.ENDTIME_CHANGED)) {
					tmpEnd = event.getTime();
					mp.setMediaTime(event);
					scale.setSashes(event);
					editTime.setValue(event);
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
				if (event.getEventType()
						.equals(SivaEventType.STARTTIME_CHANGED)) {
					tmpStart = event.getTime();
					tmpStart.setFrame(mp.getFrameForTime(tmpStart));
					forwardEvent = new SivaEvent(null,
							SivaEventType.MARK_POINT_START, event.getTime());
					editTime.setValue(event);
					frameCut.setValue(event);
				} else if (event.getEventType().equals(
						SivaEventType.ENDTIME_CHANGED)) {
					tmpEnd = event.getTime();
					forwardEvent = new SivaEvent(null,
							SivaEventType.MARK_POINT_END, event.getTime());
					editTime.setValue(event);
					frameCut.setValue(event);
				}
				mp.setMediaTime(event);
				mp.forwardEvent(forwardEvent);
				markDirty();
			}
		});

		mp.addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {
				// ignoriere die Events des MediaPlayers die von der Skala
				// ausgelöst wurden
				if (event.getSource() != null
						&& event.getSource().equals(scale)) {
					return;
				}
				if (event.getEventType()
						.equals(SivaEventType.MEDIATIME_CHANGED)) {
					scale.addMarkPoint(event.getTime().getNano(), ""); //$NON-NLS-1$
				}
			}
		});

		// Property Change Listener auf die Scene (wird z.B. beim Speichern
		// aufgerufen)
		scene.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (isDisposed()) {
					return;
				}
				if (scene != null) {
					tmpTitle.setText(scene.getTitle());
					tmpStart = new SivaTime(scene.getStart()
							- mp.getStartTime().getNano());
					tmpStart.setFrame(mp.getFrameForTime(tmpStart));
					tmpEnd = new SivaTime(scene.getEnd()
							- mp.getStartTime().getNano());
					tmpKeywords.setText(scene.getKeywords());
					SivaEvent eventStart = new SivaEvent(null,
							SivaEventType.STARTTIME_CHANGED, tmpStart);
					SivaEvent eventEnd = new SivaEvent(null,
							SivaEventType.ENDTIME_CHANGED, tmpEnd);
					scale.setSashes(eventStart);
					scale.setSashes(eventEnd);
					editTime.setValue(eventStart);
					editTime.setValue(eventEnd);
					frameCut.setValue(eventStart);
					frameCut.setValue(eventEnd);
				}
			}
		});

		SivaEvent startEvent = new SivaEvent(null,
				SivaEventType.STARTTIME_CHANGED, tmpStart);
		scale.setSashes(startEvent);
		SivaEvent endEvent = new SivaEvent(null, SivaEventType.ENDTIME_CHANGED,
				tmpEnd);
		scale.setSashes(endEvent);
		scale.addMarkPoint(tmpStart.getNano(), ""); //$NON-NLS-1$

		markDirty();
	}

	/**
	 * gibt die Szene des SceneDefineWidgets zurück
	 * 
	 * @return Scene
	 */
	public Scene getScene() {
		return this.scene;
	}

	/*
	 * aktualisiert den Tabnamen wird bei jeder Änderungsaktion im Editor
	 * aufgerufen
	 */
	private void markDirty() {
		if (!(editor.getEditorInput() instanceof DefineScenesShotInsertEditorInput)) {
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
	}

	/**
	 * prüft ob die in der Szene gespeicherten Werte von den eingegebenen
	 * abweichen, falls ja => true
	 * 
	 * @return
	 */
	public boolean isDirty() {
		if ((editor.getEditorInput() instanceof DefineScenesShotInsertEditorInput)) {
			return false;
		}
		// prüft ob schon eine Scene mit diesem Namen existiert, z.B. bei einer
		// neuen Scene
		if (scene.getVideo().getScene(scene.getLocalizedTitle().getValue(),
				Application.getCurrentLanguage()) == null) {
			return true;
		}
		if (!scene.getTitle().equals(tmpTitle.getText())) {
			return true;
		}
		if (!scene.getStart().equals(tmpStart.addTime(mp.getStartTime()))) {
			return true;
		}
		if (!scene.getEnd().equals(tmpEnd.addTime(mp.getStartTime()))) {
			return true;
		}
		if (!scene.getKeywords().equals(tmpKeywords.getText())) {
			return true;
		}
		return false;
	}

	/**
	 * speichert die Szene ab
	 */
	public void saveScene() {
		logger.debug("Creating/Changing scene: " + tmpTitle.getText()); //$NON-NLS-1$

		BeanNameGenerator nameGen = new BeanNameGenerator(tmpTitle.getText(),
				scene, scene.getVideo().getScenes(), ""); //$NON-NLS-1$
		String newTitle = nameGen.generate();
		// falls die Namensgenerierung abgebrochen wurde, wird nicht gespeichert
		if (!nameGen.getCancelState()) {
			tmpTitle.setText(newTitle);
			if (isDirty()) {
				IAbstractOperation op = new SceneChangeOperation(scene,
						newTitle, new SivaTime(tmpStart.addTime(mp
								.getStartTime())), new SivaTime(
								tmpEnd.addTime(mp.getStartTime())),
						tmpKeywords.getText());
				try {
					OperationHistory.execute(op);
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				tmpTitle.setText(scene.getTitle());
				item.setText(scene.getTitle());
			}
		}
		// speicher gleich das komplette Projekt
		ProjectSaveAction s = new ProjectSaveAction(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow(), false);
		s.run();
	}

	private void createContentShotAndSceneInsert() {
		Composite scaleComp = new Composite(this, SWT.CENTER);
		GridData scaleCompGD = new GridData(SWT.FILL, SWT.FILL, true, false);
		scaleCompGD.horizontalSpan = 3;
		scaleComp.setLayoutData(scaleCompGD);

		GridLayout scaleCompGL = new GridLayout(2, false);
		scaleCompGL.marginWidth = 0;
		scaleComp.setLayout(scaleCompGL);

		// Skala erstellen, jede Szene besitzt eine eigene Skala
		final SivaScale scale = new SivaScale(scaleComp, mp.getDuration()
				.getNano(), 538, 40, false, true, true);
		scale.setToolTip(Messages.SceneDefineWidgetScaleTooltip);

		// Container für die Buttons
		Composite defineSceneButtons = new Composite(scaleComp, SWT.TOP
				| SWT.CENTER);

		// Layout für die Buttons
		GridLayout layoutSceneControls = new GridLayout(3, false);
		layoutSceneControls.marginWidth = 0;
		layoutSceneControls.marginHeight = 0;
		layoutSceneControls.verticalSpacing = 0;
		layoutSceneControls.horizontalSpacing = 0;
		defineSceneButtons.setLayout(layoutSceneControls);

		// Button zum Speichern des Shots
		final Button saveShot = new Button(defineSceneButtons, SWT.CENTER);
		saveShot.setText("Shot speichern");
		saveShot.setToolTipText(Messages.SceneDefineWidget_Tooltip_SaveCreateButton);
		GridData savecreButtonGD = new GridData();
		savecreButtonGD.horizontalIndent = 86;
		savecreButtonGD.horizontalSpan = 3;
		saveShot.setLayoutData(savecreButtonGD);
		// Bild für den Button
		ImageHelper.setButtonImage(saveShot, Icons.ACTION_SCENE_CREATE_SAVE);
		saveShot.setToolTipText(Messages.SceneDefineWidget_SaveCreateButton);

		Composite defineComp = new Composite(this, SWT.LEFT | SWT.BORDER);
		GridLayout layoutdefineComp = new GridLayout(2, true);
		defineComp.setLayout(layoutdefineComp);

		Composite nameComposite = new Composite(defineComp, SWT.CENTER);
		GridLayout nameCompositeGL = new GridLayout(2, false);
		nameCompositeGL.marginWidth = 0;
		nameComposite.setLayout(nameCompositeGL);

		// erstelle das Composite zum Editieren der Zeit
		// der EditTime Editor arbeitet mit der relativen Zeit zur Szene
		// => die Startzeit der Szene muss noch abgezogen werden^
		// final EditTimeSingle editTime = new EditTimeSingle(nameComposite,
		// SWT.CENTER,
		// tmpStart);
		final EditTimeSingle editTime = new EditTimeSingle(nameComposite,
				SWT.CENTER, tmpStart, mp.getDuration(), mp.getDuration()
						.getNano());

		GridData editTimeGD = new GridData();
		editTimeGD.horizontalSpan = 1;
		editTime.setLayoutData(editTimeGD);

		final FrameCutWidgetSingle frameCut = new FrameCutWidgetSingle(
				nameComposite, SWT.None, tmpStart, tmpEnd, mp);

		final Composite cutChoosingComp = new Composite(defineComp, SWT.NONE);
		GridLayout gridLay = new GridLayout(2, false);
		gridLay.marginLeft = 30;
		gridLay.marginTop = 19;
		cutChoosingComp.setLayout(gridLay);
		cutChoosingComp.setSize(100, 50);
		final Label cutTypeLabel = new Label(cutChoosingComp, SWT.NONE);
		cutTypeLabel.setText("Typ: ");
		GridData gridData = new GridData();

		cutTypeLabel.setLayoutData(gridData);

		final Combo cutChoosing = new Combo(cutChoosingComp, SWT.DROP_DOWN
				| SWT.BORDER | SWT.READ_ONLY);
		cutChoosing.add("HardCut");
		cutChoosing.add("Fade");
		cutChoosing.add("Dissolve");
		cutChoosing.setText("HardCut");

		gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.LEFT;
		gridData.minimumWidth = 100;
		gridData.minimumHeight = 30;
		gridData.verticalIndent = 10;
		cutChoosing.setLayoutData(gridData);

		saveShot.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				DefineScenesShotInsertEditorInput input = (DefineScenesShotInsertEditorInput) editor
						.getEditorInput();
				SivaTime endTime = new SivaTime(input.getEnd());
				if (frameCut.getFrame() != mp.getFrameForTime(endTime)) {
					long nanos = mp.getTimeForFrame(
							(frameCut.getFrame() + 1 + (int) input
									.getFramePosToAdd())).getNano();
					BufferedImage[] imgs = mp.extractImage(nanos);
					SDTime time1 = new SDTime(nanos);
					Video vid = (Video) mp.getMediaObject();
					double seconds = (100 / (vid.getFrameRate() * 100));
					long nanoTime = (long) (seconds * 1000000000L);
					SDTime time2 = new SDTime(nanos - nanoTime);
					SDTime[] time = { time2, time1 };
					int category = 0;
					if (cutChoosing.getSelectionIndex() == 1) {
						category = 1;
					} else if (cutChoosing.getSelectionIndex() == 2) {
						category = 2;
					}
					Cut cut = new Cut(imgs, category, (frameCut.getFrame()
							+ input.getFramePosToAdd() + 1), time, 0, 0);
					input.getOverviewEditor().setCut(cut);
					IWorkbenchWindow window = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow();
					IWorkbenchPage page = window.getActivePage();
					input.getOverviewEditor().closeWindow();
					if (editor != null) {
						page.activate(editor);
						page.closeEditor(editor, false);
					}
				} else {
					MessageDialog.openInformation(parent.getShell(),
							"Information", "Bei Frame " + frameCut.getFrame()
									+ "kann kein Cut eingefügt werden!");
				}
			}
		});

		// Listener auf Edit Time
		editTime.addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {
				if (event.getEventType()
						.equals(SivaEventType.STARTTIME_CHANGED)) {
					tmpStart = event.getTime();
					tmpStart.setFrame(mp.getFrameForTime(tmpStart));
					mp.setMediaTime(event);
					scale.setSashes(event);
					frameCut.setValue(event);
				} else if (event.getEventType().equals(
						SivaEventType.ENDTIME_CHANGED)) {
					tmpEnd = event.getTime();
					mp.setMediaTime(event);
					scale.setSashes(event);
					frameCut.setValue(event);
				}
				scale.addMarkPoint(event.getTime().getNano(), ""); //$NON-NLS-1$
				markDirty();
			}
		});

		frameCut.addSivaEventConsumer(new SivaEventConsumerI() {

			@Override
			public void handleEvent(SivaEvent event) {
				System.out.println("EVENT_" + event.getEventType());
				if (event.getEventType()
						.equals(SivaEventType.STARTTIME_CHANGED)) {
					tmpStart = event.getTime();
					tmpStart.setFrame(mp.getFrameForTime(tmpStart));
					mp.setMediaTime(event);
					scale.setSashes(event);
					editTime.setValue(event);
				} else if (event.getEventType().equals(
						SivaEventType.ENDTIME_CHANGED)) {
					tmpEnd = event.getTime();
					mp.setMediaTime(event);
					scale.setSashes(event);
					editTime.setValue(event);
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
				// if (event.getEventType()
				// .equals(SivaEventType.STARTTIME_CHANGED)) {
				tmpStart = event.getTime();
				tmpStart.setFrame(mp.getFrameForTime(tmpStart));
				forwardEvent = new SivaEvent(null,
						SivaEventType.MARK_POINT_START, event.getTime());
				editTime.setValue(event);
				frameCut.setValue(event);
				mp.setMediaTime(event);
				mp.forwardEvent(forwardEvent);
				markDirty();
			}
		});

		mp.addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {
				// ignoriere die Events des MediaPlayers die von der Skala
				// ausgelöst wurden
				if (event.getSource() != null
						&& event.getSource().equals(scale)) {
					return;
				}
				if (event.getEventType()
						.equals(SivaEventType.MEDIATIME_CHANGED)) {
					scale.addMarkPoint(event.getTime().getNano(), ""); //$NON-NLS-1$
				}
			}
		});

		// Property Change Listener auf die Scene (wird z.B. beim Speichern
		// aufgerufen)
		scene.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (isDisposed()) {
					return;
				}
				if (scene != null) {
					tmpTitle.setText(scene.getTitle());
					tmpStart = new SivaTime(scene.getStart()
							- mp.getStartTime().getNano());
					tmpStart.setFrame(mp.getFrameForTime(tmpStart));
					tmpEnd = new SivaTime(scene.getEnd()
							- mp.getStartTime().getNano());
					tmpKeywords.setText(scene.getKeywords());
					SivaEvent eventStart = new SivaEvent(null,
							SivaEventType.STARTTIME_CHANGED, tmpStart);
					SivaEvent eventEnd = new SivaEvent(null,
							SivaEventType.ENDTIME_CHANGED, tmpEnd);
					scale.setSashes(eventStart);
					scale.setSashes(eventEnd);
					editTime.setValue(eventStart);
					editTime.setValue(eventEnd);
					frameCut.setValue(eventStart);
					frameCut.setValue(eventEnd);
				}
			}
		});

		frameCut.setStartFrame();

		SivaEvent startEvent = new SivaEvent(null,
				SivaEventType.STARTTIME_CHANGED, tmpStart);
		scale.setSashes(startEvent);
		SivaEvent endEvent = new SivaEvent(null, SivaEventType.ENDTIME_CHANGED,
				tmpEnd);
		scale.setSashes(endEvent);
		scale.addMarkPoint(tmpStart.getNano(), ""); //$NON-NLS-1$
	}
}
