package org.iviPro.editors.audioeditor;

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
import org.iviPro.editors.audioeditor.components.AudioPartDefineWidget;
import org.iviPro.editors.audioeditor.components.overview.AudioPartOverview;
import org.iviPro.editors.common.BComparatorComposite;
import org.iviPro.editors.common.BeanComparator;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.mediaaccess.player.MediaPlayer;
import org.iviPro.mediaaccess.player.MediaPlayerWidget;
import org.iviPro.mediaaccess.player.PlayerFactory;
import org.iviPro.model.BeanList;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.theme.Colors;
import org.iviPro.theme.Icons;
import org.iviPro.utils.SivaTime;

/**
 * @author juhoffma
 */
public class AudioEditor extends IAbstractEditor {

	private static Logger logger = Logger.getLogger(AudioEditor.class);
	public static final String ID = AudioEditor.class.getName();

	private static final String PREFIX_DEFINE_AUDIOPART = Messages.AudioEditor_EditorName_Prefix;

	// hält für jeden Audio-Part einen Tab
	private CTabFolder tabFolder = null;

	// das Audio-File des Editors
	private Audio audio = null;

	// der Audio-Part Overview
	private AudioPartOverview audioOverview;

	// Endzeit des zuletzt hinzugefügten Audio-Part
	// wird beim Speichern eines Audio-Part gesetzt
	private long lastEndTime = -1;

	// das Item des Topfolders
	CTabItem item;

	/**
	 * der Movie Player des aktuellen Video-Files
	 */
	private MediaPlayer mp;

	public AudioEditor() {
	}

	@Override
	public Image getDefaultImage() {
		return Icons.EDITOR_SCENE.getImage();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		CTabItem[] tabs = tabFolder.getItems();
		for (int i = 0; i < tabs.length; i++) {
			AudioPartDefineWidget defineWidget = (AudioPartDefineWidget) tabs[i]
					.getControl();
			if (defineWidget.isDirty()) {
				defineWidget.saveAudioPart();
			}
		}
		logger.info("Audio Editor closed."); //$NON-NLS-1$
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(final IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		
		audio = ((AudioEditorInput) input).getAudio();
		audio.addPropertyChangeListener(this);
		setPartName(PREFIX_DEFINE_AUDIOPART
				+ audio.getTitle(Application.getCurrentLanguage()));		
		mp = PlayerFactory.getPlayer(audio);
	}

	// laufe alle Tabs durch und schaue ob zugehörige AudioPartDefineWidget Dirty
	// sind
	@Override
	public boolean isDirty() {
		CTabItem[] tabs = tabFolder.getItems();
		for (int i = 0; i < tabs.length; i++) {
			AudioPartDefineWidget defineWidget = (AudioPartDefineWidget) tabs[i]
					.getControl();
			if (defineWidget.isDirty()) {
				return true;
			}
		}
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
		logger.debug("Disposing Audio editor."); //$NON-NLS-1$
		logger.debug("Stopping player"); //$NON-NLS-1$
		mp.stop();
		logger.debug("Closing player"); //$NON-NLS-1$
		mp.finish();
		logger.info("Define Audio Editor closed."); //$NON-NLS-1$
		audio.removePropertyChangeListener(this);
		logger.debug("Dispoing super-type"); //$NON-NLS-1$
		super.dispose();
	}

	@Override
	protected void createPartControlImpl(final Composite parent) {
		
		GridLayout griLayout = new GridLayout(1, false);		
		parent.setLayout(griLayout);
		parent.setBackground(Colors.EDITOR_BG.getColor());	
		
		// Scrolling
		final ScrolledComposite scrollComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		scrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrollComposite.setLayout(new GridLayout(1, true));			
		final Composite scrollContent = new Composite(scrollComposite, SWT.NONE);
		scrollContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrollContent.setLayout(new GridLayout(1, false));
		scrollComposite.setContent(scrollContent);		
		scrollComposite.setMinSize(920, 740);
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.setExpandVertical(true);
		
		scrollComposite.addListener(SWT.Activate, new Listener() {
	        public void handleEvent(Event e) {
	        	scrollComposite.setFocus();
	        }
	    });
		scrollComposite.getVerticalBar().setIncrement(10);
		

		// TabFolder hält die Audio-Part Übersicht und den Mediaplayer+Controls
		final CTabFolder tabFolderTop = new CTabFolder(scrollContent, SWT.TOP
				| SWT.LEFT);
		tabFolderTop.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		tabFolderTop.setBorderVisible(true);
		tabFolderTop.setSimple(false);
		tabFolderTop.setSingle(true);
		tabFolderTop.setMinimizeVisible(true);
		tabFolderTop.setMaximizeVisible(false);
		Colors.styleWidget(tabFolderTop);
		
		tabFolderTop.setSelectionBackground(Colors.VIDEO_OVERVIEW_ITEM_BG
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
		item.setText(mp.getMediaObject().getTitle());
		tabFolderTop.setSelection(0);

		// der Inhalt des oberen Tab Folder
		Composite itemContent = new Composite(tabFolderTop, SWT.CENTER);
		GridData topGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		GridLayout topLayout = new GridLayout(1, false);
		itemContent.setLayout(topLayout);
		itemContent.setLayoutData(topGridData);
		item.setControl(itemContent);
		
		// hält den Button zum Erstellen des Audio-Part und die Suchauswahl
		Composite itemContentTop = new Composite(itemContent, SWT.CENTER);
		itemContentTop.setLayout(new GridLayout(2, false));
		
		// Button zum Erstellen eines neuen Audio-Part
		Button newAudioPartButton = new Button(itemContentTop, SWT.CENTER);
		newAudioPartButton.setImage(Icons.ACTION_SCENE_NEW.getImage());
		newAudioPartButton.setToolTipText(Messages.AudioEditor_Button_NewAudioPart);
		newAudioPartButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				createNewAudioPart();
			}
		});
		
		// Composite zur Auswahl der Sortierung für den Audio-Part Overview
		ArrayList<BeanComparator> comparators = new ArrayList<BeanComparator>();
		for (BeanComparator bc : BeanComparator.values()) {
			if (!bc.equals(BeanComparator.SORT_BY_TYPE)) {
				comparators.add(bc);
			}
		}
		BComparatorComposite bcc = new BComparatorComposite(itemContentTop, SWT.CENTER, Messages.AudioEditor_SortAudioParts_Tooltip,  comparators);
		
		// hält den Audio-Part Overview und das MediaPlayerWidget
		Composite itemContentBottom = new Composite(itemContent, SWT.BORDER);
		itemContentBottom.setLayout(new GridLayout(2, false));
		
		Composite itemContentBottomLeft = new Composite(itemContentBottom, SWT.TOP);
		itemContentBottomLeft.setLayout(new GridLayout(1, false));
		GridData icblGD = new GridData(SWT.FILL, SWT.FILL, true, false);
		itemContentBottomLeft.setLayoutData(icblGD);

		// erstellt den Audio-Part Overview
		audioOverview = new AudioPartOverview(itemContentBottomLeft, SWT.LEFT, getSite(),
				mp, 400);
		bcc.addSivaEventConsumer(audioOverview);
		audioOverview.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof StructuredSelection) {
					Object sel = ((StructuredSelection) event.getSelection()).getFirstElement();
					AudioPart selectedPart = null;
					if (sel instanceof AudioPart) {
						selectedPart = (AudioPart) sel;
					}
					if (selectedPart != null) {
						openTab(selectedPart);						
						// setze die Markierungspunkte
						mp.forwardEvent(new SivaEvent(null, SivaEventType.MARK_POINT_START, new SivaTime(selectedPart.getStart())));						
						mp.forwardEvent(new SivaEvent(null, SivaEventType.MARK_POINT_END, new SivaTime(selectedPart.getEnd())));						
					}					
				}
			}			
		});

		// füge die Komponente hinzu die das Audio-File steuert
		new MediaPlayerWidget(itemContentBottom, SWT.CENTER, mp, true, true);
		
		// Tabs: jedes Tab enthält den Editor für genau einen Audio-Part
		tabFolder = new CTabFolder(scrollContent, SWT.TOP);
		tabFolder.setBorderVisible(true);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tabFolder.setSimple(false);
		tabFolder.setUnselectedCloseVisible(false);

		tabFolder.setSelectionBackground(Colors.VIDEO_OVERVIEW_ITEM_BG
				.getColor());
		
		tabFolder.setBackground(Colors.EDITOR_TABSINGLE_CONTENT_BG
				.getColor());
		
		// Listener auf den TabFolder, wird verwendet, wenn ein Tab
		// angeklickt wird
		tabFolder.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				CTabItem curTab = tabFolder.getSelection();
				if (curTab != null) {
					AudioPartDefineWidget adw = (AudioPartDefineWidget) curTab.getControl();
					StructuredSelection selection = new StructuredSelection(adw.getAudioPart());
					audioOverview.setSelection(selection);
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
					if (closedTab.getControl() instanceof AudioPartDefineWidget) {
						AudioPartDefineWidget tabWid = (AudioPartDefineWidget) closedTab
								.getControl();
						saveAudioPart(tabWid);
					}
					// falls das letzte Tab geschlossen wird, entferne die
					// Selektion im Overview
					if (tabFolder.getSelectionIndex() == 0) {
						audioOverview.setSelection(StructuredSelection.EMPTY);
					}
				}
			}
		});
		audio.getAudioParts().addPropertyChangeListener(this);
	}

	/**
	 * erstelle ein Tab für einen neuen Audio-Part
	 */
	public void createNewAudioPart() {
		// erstelle ein Tab für den neuen Audio-Part
		CTabItem tabItem = new CTabItem(tabFolder, SWT.CLOSE);
		tabItem.setText(Messages.AudioEditor_TabTitle_UnnamedAudioPart);
		tabFolder.setSelection(tabItem);

		// erstelle einen neuen temp. AudioPart
		AudioPart newPart = new AudioPart("", audio, Application.getCurrentProject()); //$NON-NLS-1$
		newPart.setStart(0L);
		if (lastEndTime != -1) {
			if (lastEndTime == mp.getDuration().getNano()) {
				this.lastEndTime = 0L;
			}
			newPart.setStart(lastEndTime);
		}

		newPart.setEnd(mp.getDuration().getNano());
		newPart.addPropertyChangeListener(this);

		final AudioPartDefineWidget sdw = new AudioPartDefineWidget(tabFolder, SWT.CENTER, tabItem, newPart, mp, this);
		tabItem.setControl(sdw);
		tabItem.addDisposeListener(new DisposeListener() {				
			@Override
			public void widgetDisposed(DisposeEvent e) {
				sdw.dispose();					
			}
		});
	}

	/**
	 * prüft ob ein Audio-Part bereits ein geöffnetes Tab besitzt
	 * 
	 * @param Audio Part
	 * @return true, Tab existiert
	 */
	private boolean isOpen(AudioPart part) {
		// alle Tabs
		CTabItem[] tabs = tabFolder.getItems();

		// prüfe ob der übergebene Audio-Part, zu einem Tab gehört
		for (int i = 0; i < tabs.length; i++) {
			if (!tabs[i].isDisposed()) {
				AudioPartDefineWidget defineWidget = (AudioPartDefineWidget) tabs[i]
						.getControl();
				if (!defineWidget.isDisposed()) {
					if (part.getTitle().equals(defineWidget.getAudioPart().getTitle())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// öffnet/erstellt das zur aktuellen Selektion gehörende Tab
	private void openTab(AudioPart part) {
		// falls noch kein Tab geöffnet ist
		if (!isOpen(part)) {
			if (tabFolder != null) {
				CTabItem tabItem = new CTabItem(tabFolder, SWT.CLOSE);
				tabItem.setText(part.getTitle());
				tabFolder.setSelection(tabItem);

				// Das Widget zum Editieren/Erstellen eines Audio-Part				
				part.addPropertyChangeListener(this);
				final AudioPartDefineWidget sdw = new AudioPartDefineWidget(tabFolder, SWT.CENTER, tabItem, part, mp, this);
				tabItem.setControl(sdw);
				tabItem.addDisposeListener(new DisposeListener() {				
					@Override
					public void widgetDisposed(DisposeEvent e) {
						sdw.dispose();					
					}
				});
			}
		} else {
			// vergleiche die Tabnamen mit dem Namen des Audio-Part
			CTabItem[] tabs = tabFolder.getItems();
			for (int i = 0; i < tabs.length; i++) {
				AudioPartDefineWidget sdw = (AudioPartDefineWidget) tabs[i].getControl();
				String compName = sdw.getAudioPart().getTitle();
				String name = part.getTitle();
				// auf Tab des angeklickten Audio-Part wechseln
				if (compName.equals(name)) {
					tabFolder.setSelection(tabs[i]);
				}
			}
		}
		this.audioOverview.forceFocus();
		
	}

	/**
	 * schließt das Tab des übergebenen Audio-Part
	 */
	private void closeTab(AudioPart part) {
		// alle Tabs
		CTabItem[] tabs = tabFolder.getItems();

		// prüfe ob der übergebene Audio-Part, zu einem Tab gehört
		for (int i = 0; i < tabs.length; i++) {
			AudioPartDefineWidget defineWidget = (AudioPartDefineWidget) tabs[i]
					.getControl();
			if (part == defineWidget.getAudioPart()) {
				tabs[i].dispose();
			}
		}
	}

	/**
	 * prüft ob der Audio-Part im übergebenen AudioPartDefineWidget gespeichert ist,
	 * falls nicht, wird nachgefragt ob es gemacht werden soll
	 * 
	 * @param wid
	 */
	private void saveAudioPart(AudioPartDefineWidget wid) {
		if (wid.isDirty()) {
			MessageBox messageBox = new MessageBox(Display.getCurrent()
					.getActiveShell(), SWT.ICON_QUESTION | SWT.CLOSE | SWT.YES
					| SWT.NO);
			messageBox
					.setMessage(Messages.AudioEditor_MsgBox_SaveAudioPart_Text);
			messageBox
					.setText(Messages.AudioEditor_MsgBox_SaveAudioPart_Title);

			int option = messageBox.open();
			if (option == SWT.YES) {
				wid.saveAudioPart();
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
	public void propertyChange(PropertyChangeEvent evt) {		
		super.propertyChange(evt);
		
		AudioPart changedPart = null;
		if (evt.getNewValue() instanceof AudioPart) {
			changedPart = (AudioPart) evt.getNewValue();
		}

		// falls sich die Endzeit eines Audio-Part ändert
		// setze den neuen Audio-Part auf die zuletzt editierte Endzeit
		if (evt.getPropertyName().equals(AudioPart.PROP_END)) {
			lastEndTime = (Long) evt.getNewValue();
		}

		// Name des Medionobjekts hat sich geändert
		if (evt.getSource() instanceof Audio && evt.getPropertyName() != null) {
			if (evt.getPropertyName().equals(BeanList.PROP_TITLE)) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						item.setText(audio.getTitle());
						setPartName(PREFIX_DEFINE_AUDIOPART
								+ audio.getTitle(Application
										.getCurrentLanguage()));
					}
				});
			}
		}

		if (evt.getPropertyName().equals(BeanList.PROP_ITEM_ADDED)) {
			if (changedPart != null) {
				lastEndTime = changedPart.getEnd();
			}
		}

		// wurde ein AudioPart gelöscht, schließe auch das zugehörige Tab falls es
		// offen ist
		if (evt.getPropertyName().equals(BeanList.PROP_ITEM_REMOVED)) {
			if (changedPart != null) {
				if (isOpen(changedPart)) {
					closeTab(changedPart);
				}
			}
		}
	}

	@Override
	protected IAbstractBean getKeyObject() {
		return audio;
	}
}
