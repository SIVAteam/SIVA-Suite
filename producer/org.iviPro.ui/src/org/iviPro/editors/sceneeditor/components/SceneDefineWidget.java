package org.iviPro.editors.sceneeditor.components;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.iviPro.application.Application;
import org.iviPro.editors.PreviewComponent;
import org.iviPro.editors.TimeSelector;
import org.iviPro.editors.annotationeditor.AnnotationEditor;
import org.iviPro.editors.common.BeanNameGenerator;
import org.iviPro.editors.common.EditTime;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.editors.sceneeditor.SceneEditor;
import org.iviPro.mediaaccess.player.MediaPlayer;
import org.iviPro.mediaaccess.player.controls.SivaScale;
import org.iviPro.model.resources.Scene;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.video.SceneChangeOperation;
import org.iviPro.theme.Icons;
import org.iviPro.utils.ImageHelper;
import org.iviPro.utils.SivaTime;

public class SceneDefineWidget extends Composite implements SivaEventConsumerI, PropertyChangeListener {

	private static Logger logger = Logger.getLogger(SceneDefineWidget.class);
	
	private static final int MAX_PREVIEW_WIDTH = 640;
	private static final int MAX_PREVIEW_HEIGHT = 360;

	// der MediaPlayer
	private MediaPlayer mp;

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
	
	/**
	 * Absolute time of the video frame which is used as thumbnail for the currently 
	 * edited scene.
	 */
	private long tmpThumbTime;

	// der Szeneneditor (wird für das neu anlegen einer Szene benötigt
	private final SceneEditor editor;
	
	private PreviewComponent thumbPreview;

	public SceneDefineWidget(Composite parent, int style, CTabItem item,
			final Scene scene, final MediaPlayer mp, SceneEditor editor) {

		super(parent, style);

		// setze den Movieplayer
		this.mp = mp;
		this.item = item;
		this.scene = scene;
		this.scene.addPropertyChangeListener(this);
		this.editor = editor;

		tmpStart = new SivaTime(scene.getStart() - mp.getStartTime().getNano());
		tmpStart.setFrame(mp.getFrameForNanos(scene.getStart() - mp.getStartTime().getNano()));
		tmpEnd = new SivaTime(scene.getEnd() - mp.getStartTime().getNano());
		tmpEnd.setFrame(mp.getFrameForTime(tmpEnd));
		tmpThumbTime = scene.getThumbnail().getTime();

		// setze das Layout und den Inhalt
		// das sdWidget soll so weit oben wie möglich und Centered sein
		GridData scwGrid = new GridData(SWT.CENTER, SWT.TOP, true, true);
		scwGrid.widthHint = 620;
		setLayoutData(scwGrid);

		createContent();
	}

	/**
	 * erstellt den Content
	 */
	private void createContent() {
		this.setLayout(new GridLayout(2, false));
		
		Composite scaleComp = new Composite(this, SWT.CENTER);
		GridData scaleCompGD = new GridData(SWT.FILL, SWT.FILL, true, false);
		scaleCompGD.horizontalSpan = 2;
		scaleComp.setLayoutData(scaleCompGD);

		GridLayout scaleCompGL = new GridLayout(2, false);
		scaleCompGL.marginWidth = 0;
		scaleComp.setLayout(scaleCompGL);

		// Skala erstellen, jede Szene besitzt eine eigene Skala
		final SivaScale scale = new SivaScale(scaleComp, mp.getDuration()
				.getNano(), 520, 40, true, true, true);
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

		// Component for scene settings (title, keywords..)
		Composite defineComp = new Composite(this, SWT.BORDER);
		GridLayout layoutdefineComp = new GridLayout(1, false);
		defineComp.setLayout(layoutdefineComp);
		defineComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

		Group groupName = new Group(defineComp, SWT.NONE);
		groupName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		groupName.setLayout(new GridLayout(1, false));
		groupName.setText(Messages.SceneDefineWidget_Label_SceneName);
		
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
		editTimeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		editTimeGroup.setText(Messages.SceneDefineWidget_PositionInput_inTime);
		
		final EditTime editTime = new EditTime(editTimeGroup, SWT.CENTER,
				tmpStart, tmpEnd, mp.getDuration().getNano());		
		GridData editTimeGD = new GridData(225,70);
		editTime.setLayoutData(editTimeGD);
		
		Group editFrameGroup = new Group(defineComp, SWT.NONE);
		editFrameGroup.setLayout(new GridLayout(1, false));
		editFrameGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		editFrameGroup.setText(Messages.SceneDefineWidget_PositionInput_InFrames);
		final FrameCutWidget frameCut = new FrameCutWidget(editFrameGroup,
				SWT.None, tmpStart, tmpEnd, mp);
		GridData editFrameGd = new GridData(225,55);
		frameCut.setLayoutData(editFrameGd);
		
		// Eingabefeld für die Keywords
		Group kwGroup = new Group(defineComp, SWT.NONE);
		kwGroup.setLayout(new GridLayout(1, false));
		kwGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		kwGroup.setText(Messages.SceneDefineWidget_Label_Keywords);
		kwGroup.setToolTipText(Messages.SceneDefineWidget_Label_KeywordsTooltip);
		tmpKeywords = new Text(kwGroup, SWT.V_SCROLL | SWT.MULTI | SWT.WRAP
				| SWT.BORDER);
		GridData tmpKeywordsGD = new GridData(SWT.FILL, SWT.FILL, true, false);
		tmpKeywordsGD.heightHint = 50;
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
		
		// Thumbnail editor
		Composite thumbEditor = new Composite(this, SWT.NONE);
		thumbEditor.setLayout(new GridLayout(1, false));
		GridData editorGD = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		thumbEditor.setLayoutData(editorGD);
						
		thumbPreview = new PreviewComponent(thumbEditor, SWT.NONE,
				MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT);
		thumbPreview.setLabel(Messages.SceneDefineWidget_Label_Preview);
		thumbPreview.setPreview(scene, tmpThumbTime);		
		
		thumbPreview.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				TimeSelector selector = new TimeSelector(scene.getVideo(), 
						MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT,
						tmpStart.getNano(), tmpEnd.getNano());
				selector.addSivaEventConsumer(SceneDefineWidget.this);
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		
					
		// Add component listeners
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
					if (tmpStart.getNano() > tmpThumbTime) {
						tmpThumbTime = tmpStart.getNano();
						thumbPreview.setPreview(scene, tmpThumbTime);
					}
					tmpStart.setFrame(mp.getFrameForTime(tmpStart));
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
					if (tmpEnd.getNano() < tmpThumbTime) {
						tmpThumbTime = tmpEnd.getNano();
						thumbPreview.setPreview(scene, tmpThumbTime);
					}
					SivaEvent event = new SivaEvent(null,
							SivaEventType.ENDTIME_CHANGED, mp.getMediaTime());
					scale.setSashes(event);
					editTime.setValue(event);
					frameCut.setValue(event);
					SivaEvent forwardEvent = new SivaEvent(null,
							SivaEventType.MARK_POINT_END, mp.getMediaTime());
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
					if (tmpStart.getNano() > tmpThumbTime) {
						tmpThumbTime = tmpStart.getNano();
						thumbPreview.setPreview(scene, tmpThumbTime);
					}
					tmpStart.setFrame(mp.getFrameForTime(tmpStart));
					mp.setMediaTime(event);
					scale.setSashes(event);
					frameCut.setValue(event);
				} else if (event.getEventType().equals(
						SivaEventType.ENDTIME_CHANGED)) {
					tmpEnd = event.getTime();
					if (tmpEnd.getNano() < tmpThumbTime) {
						tmpThumbTime = tmpEnd.getNano();
						thumbPreview.setPreview(scene, tmpThumbTime);
					}
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
					if (tmpStart.getNano() > tmpThumbTime) {
						tmpThumbTime = tmpStart.getNano();
						thumbPreview.setPreview(scene, tmpThumbTime);
					}
					tmpStart.setFrame(mp.getFrameForTime(tmpStart));
					mp.setMediaTime(event);
					scale.setSashes(event);
					editTime.setValue(event);
				} else if (event.getEventType().equals(
						SivaEventType.ENDTIME_CHANGED)) {
					tmpEnd = event.getTime();
					if (tmpEnd.getNano() < tmpThumbTime) {
						tmpThumbTime = tmpEnd.getNano();
						thumbPreview.setPreview(scene, tmpThumbTime);
					}
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
					if (tmpStart.getNano() > tmpThumbTime) {
						tmpThumbTime = tmpStart.getNano();
						thumbPreview.setPreview(scene, tmpThumbTime);
					}
					tmpStart.setFrame(mp.getFrameForTime(tmpStart));
					forwardEvent = new SivaEvent(null,
							SivaEventType.MARK_POINT_START, event.getTime());
					editTime.setValue(event);
					frameCut.setValue(event);
				} else if (event.getEventType().equals(
						SivaEventType.ENDTIME_CHANGED)) {
					tmpEnd = event.getTime();
					if (tmpEnd.getNano() < tmpThumbTime) {
						tmpThumbTime = tmpEnd.getNano();
						thumbPreview.setPreview(scene, tmpThumbTime);
					}
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
					tmpThumbTime = scene.getThumbnail().getTime();
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
		SivaEvent endEvent = new SivaEvent(null, SivaEventType.ENDTIME_CHANGED,
				tmpEnd);
		// Need to set end sash first. Otherwise start time would be later than
		// end time which would be ignored by setSash().
		scale.setSashes(endEvent);
		scale.setSashes(startEvent);
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
	 * prüft ob die in der Szene gespeicherten Werte von den eingegebenen
	 * abweichen, falls ja => true
	 * 
	 * @return
	 */
	public boolean isDirty() {
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
		if (scene.getThumbnail().getTime() != tmpThumbTime) {
			return true;
		}
		return false;
	}

	/**
	 * speichert die Szene ab
	 */
	public void saveScene() {
		
		/* If timings of the scene changed, close any related open annotation 
		 * editors first to avoid updating all of the annotation editor gui 
		 * elements. This is a workaround which may be substituted by proper 
		 * gui updating someday. */
		if (!scene.getStart().equals(tmpStart.addTime(mp.getStartTime()))
				|| !scene.getEnd().equals(tmpEnd.addTime(mp.getStartTime()))) {
			List<IEditorPart> openEditors = new ArrayList<IEditorPart>();
			for (IEditorReference editorRef : PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().getEditorReferences()) {
				IEditorPart editor = editorRef.getEditor(false);
				if (editor instanceof AnnotationEditor) {
					if (scene == ((AnnotationEditor)editor).getScene()) {
						openEditors.add(editor);
					}
				}
			}	
			if (!openEditors.isEmpty()) {
				MessageBox messageBox = new MessageBox(Display.getCurrent()
						.getActiveShell(), SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
				messageBox.setMessage(Messages.SceneDefineWidget_MessageBox_CloseAnnoEditors);
				if (messageBox.open() == SWT.OK) {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (window != null) {
						for (IEditorPart editor : openEditors) {
							window.getActivePage().activate(editor);
							if (!window.getActivePage().closeEditor(editor, true)) {
								return;
							}
						}
					}
				} else {
					return;
				}
			}
		}
		
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
								tmpKeywords.getText(), tmpThumbTime);
				try {
					OperationHistory.execute(op);
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				tmpTitle.setText(scene.getTitle());
				item.setText(scene.getTitle());
			}
		}
	}
	
	@Override
	public void handleEvent(SivaEvent event) {
		// React on choosing a new thumbnail time in ThumbnailSelector
		if (!isDisposed()) {
			if (event.getEventType() == SivaEventType.TIME_SELECTION) {
				tmpThumbTime = (Long)event.getValue();
				thumbPreview.setPreview(scene, tmpThumbTime);
				markDirty();
				this.layout();
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// React on changes to the scene thumbnail time
		// (Undo/Redo, changes triggered by other editors)
		if (!isDisposed()) {
			if (evt.getPropertyName().equals(Scene.PROP_THUMB)) {
				tmpThumbTime = (Long) evt.getNewValue();
				thumbPreview.setPreview(scene, tmpThumbTime);
				this.layout();
			}
		}
	}
}
