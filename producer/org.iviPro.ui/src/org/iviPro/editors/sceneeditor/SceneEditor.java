package org.iviPro.editors.sceneeditor;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.iviPro.application.Application;
import org.iviPro.editors.IAbstractEditor;
import org.iviPro.editors.common.BComparatorComposite;
import org.iviPro.editors.common.BeanComparator;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.editors.sceneeditor.components.SceneDefineWidget;
import org.iviPro.editors.sceneeditor.components.VideoTimelineWidget;
import org.iviPro.editors.sceneeditor.components.overview.ScenesOverview;
import org.iviPro.mediaaccess.player.MediaPlayer;
import org.iviPro.mediaaccess.player.MediaPlayerWidget;
import org.iviPro.mediaaccess.player.PlayerFactory;
import org.iviPro.model.BeanList;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;
import org.iviPro.theme.Colors;
import org.iviPro.theme.Icons;
import org.iviPro.utils.SivaTime;

/**
 * 
 * @author juhoffma
 * 
 */
public class SceneEditor extends IAbstractEditor {

	private static Logger logger = Logger.getLogger(SceneEditor.class);
	public static final String ID = SceneEditor.class.getName();

	private static final String PREFIX_DEFINE_SCENES = Messages.DefineScenesEditor_EditorName_Prefix;
	
	/**
	 * Component allowing the scrolling of the main component
	 */
	private ScrolledComposite scrollComposite;
	/**
	 * The main component encapsulating the editor widgets
	 */
	private Composite scrollContent;

	// hält für jede Szene einen Tab
	private CTabFolder tabFolder = null;

	// das Video des Editors
	private Video video = null;

	// der Szenenüberlbick
	ScenesOverview scenesOverview;

	// Endzeit der zuletzt hinzugefügten Szene
	// wird beim Speichern einer Szene gesetzt
	private long lastEndTime = -1;

	// das Item des Topfolders
	CTabItem item;

	/**
	 * der Movie Player des aktuellen Video-Files
	 */
	private MediaPlayer mp;

	public SceneEditor() {
	}

	@Override
	public Image getDefaultImage() {
		return Icons.EDITOR_SCENE.getImage();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		CTabItem[] tabs = tabFolder.getItems();
		for (int i = 0; i < tabs.length; i++) {
			SceneDefineWidget defineWidget = (SceneDefineWidget) tabs[i].getControl();
			if (defineWidget.isDirty()) {
				defineWidget.saveScene();
			}
		}
		logger.info("Scene Editor closed."); //$NON-NLS-1$
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
	
		video = ((DefineScenesEditorInput) input).getVideo();
		video.addPropertyChangeListener(this);
		setPartName(PREFIX_DEFINE_SCENES
				+ video.getTitle(Application.getCurrentLanguage()));
		mp = PlayerFactory.getPlayer(video);
	}

	// laufe alle Tabs durch und schaue ob zugehörige SceneDefineWidgets Dirty
	// sind
	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void dispose() {
		// Wird aufgerufen, wenn der Editor geschlossen wird. Wir stoppen
		// dann alle Player und Threads.
		logger.debug("Disposing scenes editor."); //$NON-NLS-1$
		logger.debug("Stopping player"); //$NON-NLS-1$
		mp.stop();
		logger.debug("Closing player"); //$NON-NLS-1$
		mp.finish();
		logger.info("Define Scenes Editor closed."); //$NON-NLS-1$
		video.removePropertyChangeListener(this);
		logger.debug("Dispoing super-type"); //$NON-NLS-1$
		super.dispose();
	}

	@Override
	protected void createPartControlImpl(final Composite parent) {

		GridLayout griLayout = new GridLayout(1, false);
		parent.setLayout(griLayout);
		parent.setBackground(Colors.EDITOR_BG.getColor());

		// Scrolling
		scrollComposite = new ScrolledComposite(parent,
				SWT.V_SCROLL | SWT.H_SCROLL);
		scrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrollComposite.setLayout(new GridLayout(1, true));
		
		scrollContent = new Composite(scrollComposite, SWT.NONE);
		scrollContent
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrollContent.setLayout(new GridLayout(1, false));
		scrollComposite.setContent(scrollContent);
		
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.setExpandVertical(true);
		scrollComposite.getVerticalBar().setIncrement(10);

		scrollComposite.addListener(SWT.Activate, new Listener() {
			public void handleEvent(Event e) {
				scrollComposite.setFocus();
			}
		});
		

		// TabFolder hält die Szenenübersicht und das Video+Controls
		final CTabFolder tabFolderTop = new CTabFolder(scrollContent, SWT.TOP
				| SWT.LEFT);
		tabFolderTop
				.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		tabFolderTop.setBorderVisible(true);
		tabFolderTop.setSimple(false);
		tabFolderTop.setSingle(true);
		tabFolderTop.setMinimizeVisible(true);
		tabFolderTop.setMaximizeVisible(false);
		Colors.styleWidget(tabFolderTop);

		tabFolderTop
				.setSelectionBackground(Colors.VIDEO_OVERVIEW_ITEM_BG
						.getColor());

		tabFolderTop.setBackground(Colors.EDITOR_TABSINGLE_CONTENT_BG
				.getColor());

		tabFolderTop.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void minimize(CTabFolderEvent event) {
				tabFolderTop.setMinimized(true);
				tabFolderTop.setMinimizeVisible(false);
				tabFolderTop.setMaximizeVisible(true);
				scrollComposite.setMinSize(920, 370);
				scrollContent.layout(true);
			}

			public void maximize(CTabFolderEvent event) {
				tabFolderTop.setMinimized(false);
				tabFolderTop.setMinimizeVisible(true);
				tabFolderTop.setMaximizeVisible(false);
				scrollComposite.setMinSize(920, 740);
				scrollContent.layout(true);
			}
		});

		item = new CTabItem(tabFolderTop, SWT.NONE);
		item.setText(video.getTitle());
		tabFolderTop.setSelection(0);

		// der Inhalt des oberen Tab Folder
		Composite itemContent = new Composite(tabFolderTop, SWT.CENTER);
		GridData topGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		GridLayout topLayout = new GridLayout(1, false);
		itemContent.setLayout(topLayout);
		itemContent.setLayoutData(topGridData);
		item.setControl(itemContent);

		// hält den Button zum Erstellen der Scene und die Suchauswahl
		Composite itemContentTop = new Composite(itemContent, SWT.CENTER);
		itemContentTop.setLayout(new GridLayout(2, false));

		// Button zum Erstellen einer neuen Szene
		Button newSceneButton = new Button(itemContentTop, SWT.CENTER);
		newSceneButton.setImage(Icons.ACTION_SCENE_NEW.getImage());
		newSceneButton
				.setToolTipText(Messages.DefineScenesEditor_Button_NewScene);
		newSceneButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				createNewScene();
			}
		});

		// Composite zur Auswahl der Sortierung für den Szenenüberblick
		ArrayList<BeanComparator> comparators = new ArrayList<BeanComparator>();
		for (BeanComparator bc : BeanComparator.values()) {
			if (!bc.equals(BeanComparator.SORT_BY_TYPE)) {
				comparators.add(bc);
			}
		}
		BComparatorComposite bcc = new BComparatorComposite(itemContentTop,
				SWT.CENTER, Messages.DefineScenesEditor_SortScenes_Tooltip,
				comparators);

		// hält die Szenenübersicht und das MediaPlayerWidget
		Composite itemContentBottom = new Composite(itemContent, SWT.BORDER);
		GridLayout contentBottomGL = new GridLayout(2, false);
		contentBottomGL.horizontalSpacing=70;
		itemContentBottom.setLayout(new GridLayout(2, false));

		Composite itemContentBottomLeft = new Composite(itemContentBottom,
				SWT.TOP);
		itemContentBottomLeft.setLayout(new GridLayout(1, false));
		GridData icblGD = new GridData(SWT.FILL, SWT.FILL, true, false);
		itemContentBottomLeft.setLayoutData(icblGD);

		// erstellt den Szenenüberblick
		scenesOverview = new ScenesOverview(itemContentBottomLeft, SWT.LEFT, getSite(), mp, 400);
		bcc.addSivaEventConsumer(scenesOverview);
		scenesOverview
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						if (event.getSelection() instanceof StructuredSelection) {
							Scene selectedScene = (Scene) ((StructuredSelection) event
									.getSelection()).getFirstElement();
							if (selectedScene != null) {

								openTab(selectedScene);

								// setze die Markierungspunkte
								mp.forwardEvent(new SivaEvent(null,
										SivaEventType.MARK_POINT_START,
										new SivaTime(selectedScene.getStart())));
								mp.forwardEvent(new SivaEvent(null,
										SivaEventType.MARK_POINT_END,
										new SivaTime(selectedScene.getEnd())));
							}
						}
					}
				});
		
		// höre auf den Player
		mp.addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {
				scenesOverview.refreshView();
			}			
		});
		
		

		// füge die Timeline hinzu
		new VideoTimelineWidget(itemContentBottomLeft, SWT.LEFT, mp);

		// füge die Komponente hinzu die das Video steuert
		MediaPlayerWidget mpw = new MediaPlayerWidget(itemContentBottom, SWT.CENTER, mp, true, true);
		mpw.getSlider().addSivaEventConsumer(new SivaEventConsumerI() {
			@Override
			public void handleEvent(SivaEvent event) {
				scenesOverview.refreshView();
			}			
		});
		
		// Tabs: jedes Tab enthält den Editor für genau eine Szene
		tabFolder = new CTabFolder(scrollContent, SWT.TOP);
		tabFolder.setBorderVisible(true);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tabFolder.setSimple(false);
		tabFolder.setUnselectedCloseVisible(false);

		tabFolder.setSelectionBackground(Colors.VIDEO_OVERVIEW_ITEM_BG
				.getColor());

		//tabFolder.setBackground(Colors.SKIN_DARK_BG.getColor());
		tabFolder.setBackground(Colors.EDITOR_TABSINGLE_CONTENT_BG.getColor());

		// Listener auf den TabFolder, wird verwendet, wenn ein Tab
		// angeklickt wird
		tabFolder.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				CTabItem curTab = tabFolder.getSelection();
				if (curTab != null) {
					SceneDefineWidget adw = (SceneDefineWidget) curTab
							.getControl();
					StructuredSelection selection = new StructuredSelection(adw
							.getScene());
					scenesOverview.setSelection(selection);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				if (event.item instanceof CTabItem) {
					CTabItem closedTab = (CTabItem) event.item;
					if (closedTab.getControl() instanceof SceneDefineWidget) {
						SceneDefineWidget tabWid = (SceneDefineWidget) closedTab
								.getControl();
						saveScene(tabWid);
					}
					// falls das letzte Tab geschlossen wird, entferne die
					// Selektion im Overview
					if (tabFolder.getSelectionIndex() == 0) {
						scenesOverview.setSelection(StructuredSelection.EMPTY);
					}
				}
			}
		});
		video.getScenes().addPropertyChangeListener(this);
		
		scrollComposite.setMinSize(scrollContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	/**
	 * erstelle ein Tab für eine neue Szene
	 */
	public void createNewScene() {
		// erstelle ein Tab für die neue Szene
		CTabItem tabItem = new CTabItem(tabFolder, SWT.CLOSE);
		tabItem.setText(Messages.DefineScenesEditor_TabTitle_UnnamedScene);
		tabFolder.setSelection(tabItem);

		// erstelle eine neue temporäre Szene
		Scene newScene = new Scene("", video, Application.getCurrentProject()); //$NON-NLS-1$
		newScene.setStart(0L);
		if (lastEndTime != -1) {
			if (lastEndTime == mp.getDuration().getNano()) {
				this.lastEndTime = 0L;
			}
			newScene.setStart(lastEndTime);
		}

		newScene.setEnd(mp.getDuration().getNano());
		newScene.addPropertyChangeListener(this);

		final SceneDefineWidget sdw = new SceneDefineWidget(tabFolder, SWT.CENTER,
				tabItem, newScene, mp, this);
		tabItem.setControl(sdw);
		tabItem.addDisposeListener(new DisposeListener() {				
			@Override
			public void widgetDisposed(DisposeEvent e) {
				sdw.dispose();					
			}
		});
		
		scrollComposite.setMinSize(scrollContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	/**
	 * prüft ob eine Szene bereits ein geöffnetes Tab besitzt
	 * 
	 * @param Szene
	 * @return true, Tab existiert
	 */
	private boolean isOpen(Scene scene) {
		// alle Tabs
		CTabItem[] tabs = tabFolder.getItems();

		// prüfe ob die übergebene Szene, zu einem Tab gehört
		for (int i = 0; i < tabs.length; i++) {
			if (!tabs[i].isDisposed()) {
				SceneDefineWidget defineWidget = (SceneDefineWidget) tabs[i]
						.getControl();
				if (!defineWidget.isDisposed()) {
					if (scene.getTitle().equals(
							defineWidget.getScene().getTitle())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// öffnet/erstellt das zur aktuellen Selektion gehörende Tab
	private void openTab(Scene scene) {
		// falls noch kein Tab geöffnet ist
		if (!isOpen(scene)) {
			if (tabFolder != null) {
				CTabItem tabItem = new CTabItem(tabFolder, SWT.CLOSE);
				tabItem.setText(scene.getTitle());
				tabFolder.setSelection(tabItem);

				// Das Widget zum Editieren/Erstellen einer
				// Szene
				scene.addPropertyChangeListener(this);
				final SceneDefineWidget sdw = new SceneDefineWidget(tabFolder,
						SWT.CENTER, tabItem, scene, mp, this);
				tabItem.setControl(sdw);
				tabItem.addDisposeListener(new DisposeListener() {				
					@Override
					public void widgetDisposed(DisposeEvent e) {
						sdw.dispose();					
					}
				});
				
			}
		} else {
			// vergleiche die Tabnamen mit dem Namen der
			// Szene
			CTabItem[] tabs = tabFolder.getItems();
			for (int i = 0; i < tabs.length; i++) {
				SceneDefineWidget sdw = (SceneDefineWidget) tabs[i]
						.getControl();
				String compName = sdw.getScene().getTitle();
				String name = scene.getTitle();
				// auf Tab der angeklickten Szene wechseln
				if (compName.equals(name)) {
					tabFolder.setSelection(tabs[i]);
				}
			}
		}
		this.scenesOverview.forceFocus();
		scrollComposite.setMinSize(scrollContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	/**
	 * schließt das Tab der übergebenen Szene
	 */
	private void closeTab(Scene scene) {
		// alle Tabs
		CTabItem[] tabs = tabFolder.getItems();

		// prüfe ob die übergebene Szene, zu einem Tab gehört
		for (int i = 0; i < tabs.length; i++) {
			SceneDefineWidget defineWidget = (SceneDefineWidget) tabs[i]
					.getControl();
			if (scene == defineWidget.getScene()) {
				tabs[i].dispose();
			}
		}
	}

	/**
	 * prüft ob die Szene im übergebenen SceneDefineWidget gespeichert ist,
	 * falls nicht, wird nachgefragt ob es gemacht werden soll
	 * 
	 * @param wid
	 */
	private void saveScene(SceneDefineWidget wid) {
		if (wid.isDirty()) {
			MessageBox messageBox = new MessageBox(Display.getCurrent()
					.getActiveShell(), SWT.ICON_QUESTION | SWT.CLOSE | SWT.YES
					| SWT.NO);
			messageBox
					.setMessage(Messages.DefineScenesEditor_MsgBox_SaveScene_Text);
			messageBox
					.setText(Messages.DefineScenesEditor_MsgBox_SaveScene_Title);

			int option = messageBox.open();
			if (option == SWT.YES) {
				wid.saveScene();
			}
		}
	}

	@Override
	public void setFocus() {
		if (tabFolder != null) {
			tabFolder.setFocus();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {		
		super.propertyChange(event);
		
		Scene changedScene = null;
		if (event.getNewValue() instanceof Scene) {
			changedScene = (Scene) event.getNewValue();
		}

		if (event.getPropertyName().equals(BeanList.PROP_ITEM_REMOVED)) {
			if (changedScene != null) {
				// wurde eine Szene gelöscht, schließe auch das zugehörige Tab falls es
				// offen ist
				if (isOpen(changedScene)) {
					closeTab(changedScene);
				}
			}
		}
		
		if (event.getPropertyName().equals(BeanList.PROP_ITEM_ADDED)) {
			if (changedScene != null) {
				lastEndTime = changedScene.getEnd();
			}
		}
		
		// falls sich die Endzeit einer Szene ändert
		// setze die neue Szene auf die zuletzt editierte Endzeit
		if (event.getPropertyName().equals(Scene.PROP_END)) {
			lastEndTime = (Long) event.getNewValue();
		}

		// Name des Medionobjekts hat sich geändert
		if (event.getSource() == video) {
			if (event.getPropertyName().equals(IAbstractBean.PROP_TITLE)) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						item.setText(video.getTitle());
						setPartName(PREFIX_DEFINE_SCENES
								+ video.getTitle(Application
										.getCurrentLanguage()));
					}
				});
			}
		}		
	}

	@Override
	protected IAbstractBean getKeyObject() {
		return video;
	}
}
