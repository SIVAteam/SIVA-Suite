package org.iviPro.editors.annotationeditor;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;

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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.iviPro.application.Application;
import org.iviPro.editors.IAbstractEditor;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationFactory;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationGroup;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationType;
import org.iviPro.editors.annotationeditor.components.AnnotationDefineWidget;
import org.iviPro.editors.annotationeditor.components.overview.AnnotationOverview;
import org.iviPro.editors.common.BComparatorComposite;
import org.iviPro.editors.common.BeanComparator;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.mediaaccess.player.MediaPlayer;
import org.iviPro.mediaaccess.player.MediaPlayerWidget;
import org.iviPro.mediaaccess.player.PlayerFactory;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IConnection;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;
import org.iviPro.theme.Colors;
import org.iviPro.theme.Icons;
import org.iviPro.utils.SivaTime;

/**
 * @author juhoffma
 */
public class AnnotationEditor extends IAbstractEditor implements
		ISelectionChangedListener {

	private static Logger logger = Logger.getLogger(AnnotationEditor.class);
	public static final String ID = AnnotationEditor.class.getName();
	private static final String PREFIX_DEFINE_ANNOTATION = "Annotation-Editing - "; //$NON-NLS-1$
	
	/**
	 * Component allowing the scrolling of the main component
	 */
	private ScrolledComposite scrollComposite;
	/**
	 * The main component encapsulating the editor widgets
	 */
	private Composite scrollContent;
	
	// die zum Annotationseditor gehörende Szene
	private NodeScene nodeScene = null;

	// der Annotationsüberblick
	private AnnotationOverview aov = null;

	// hält für jede Annotation einen Tab
	private CTabFolder tabFolder = null;

	// der zur Szene gehörende Movieplayer
	private MediaPlayer mp = null;

	// die zuletzt hinzugefügeten Annotationen
	private HashMap<AnnotationType, INodeAnnotation> lastAddedAnnotations = new HashMap<AnnotationType, INodeAnnotation>();

	// der zuletzt hinzugefügte Annotationstyp
	private AnnotationType lastAddedAnnotationType;
	
	public AnnotationEditor() {
		super();
		logger.debug("Creating annotation editor."); //$NON-NLS-1$		
	}

	@Override
	public Image getDefaultImage() {
		return Icons.EDITOR_ANNOTATION.getImage();
	}

	/*
	 * (non-Javadoc)	
	 * @seeorg.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		CTabItem[] tabs = tabFolder.getItems();
		for (int i = 0; i < tabs.length; i++) {
			AnnotationDefineWidget defineWidget = (AnnotationDefineWidget) tabs[i]
					.getControl();
			if (defineWidget.isDirty()) {
				monitor.setCanceled(!defineWidget.executeSaveOperation());
			}
		}
		logger.info("Annotation Editor closed."); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
	 * org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		firePropertyChange(IEditorPart.PROP_DIRTY);
		
		nodeScene = ((AnnotationEditorInput) input).getSceneNode();
		Graph graph = Application.getCurrentProject().getSceneGraph();
		graph.addPropertyChangeListener(this);
		nodeScene.addPropertyChangeListener(this);
		setPartName(PREFIX_DEFINE_ANNOTATION + nodeScene.getTitle());	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		CTabItem[] tabs = tabFolder.getItems();
		for (int i = 0; i < tabs.length; i++) {
			AnnotationDefineWidget defineWidget = (AnnotationDefineWidget) tabs[i]
					.getControl();
			if (defineWidget != null && !defineWidget.isDisposed()) {
				if (defineWidget.isDirty()) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/*
	 * (non-Javadoc)	 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		logger.debug("Disposing annotation editor."); //$NON-NLS-1$
		logger.debug("Stopping player"); //$NON-NLS-1$
		mp.stop();
		logger.debug("Closing player"); //$NON-NLS-1$
		mp.finish();
		
		if (Application.getCurrentProject() != null) {
			Graph graph = Application.getCurrentProject().getSceneGraph();
			graph.removePropertyChangeListener(this);
		}		
		nodeScene.removePropertyChangeListener(this);
		aov.removeSelectionChangedListener(this);		
		logger.debug("Dispoing super-type"); //$NON-NLS-1$
		super.dispose();
	}

	/*
	 * (non-Javadoc)	 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControlImpl(final Composite parent) {	
				
		// Scroll component
		scrollComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		scrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				
		// Main editor component
		scrollContent = new Composite(scrollComposite, SWT.NONE);
		scrollContent.setLayout(new GridLayout(1, false));
		scrollComposite.setContent(scrollContent);	
		
		scrollComposite.getVerticalBar().setIncrement(10);	
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.setExpandVertical(true);
		
		scrollComposite.addListener(SWT.Activate, new Listener() {
	        public void handleEvent(Event e) {
	        	scrollComposite.setFocus();
	        }
	    });

		// hole das Medienobjekt für die Szene
		Video video = nodeScene.getScene().getVideo();

		// der MoviePlayer der die entsprechende Szene abspielt
		mp = PlayerFactory.getPlayer(video, nodeScene.getScene().getStart(), nodeScene.getScene().getEnd());
		parent.setBackground(Colors.EDITOR_BG.getColor());
		GridLayout griLayout = new GridLayout(1, false);
		parent.setLayout(griLayout);
		
		final CTabFolder tabFolderTop = new CTabFolder(scrollContent, SWT.TOP | SWT.LEFT);
		tabFolderTop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		tabFolderTop.setBorderVisible(true);
		tabFolderTop.setSimple(false);
		tabFolderTop.setSingle(true);
		Colors.styleWidget(tabFolderTop);

		tabFolderTop.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void minimize(CTabFolderEvent event) {
				tabFolderTop.setMinimized(true);
				tabFolderTop.setMinimizeVisible(false);
				tabFolderTop.setMaximizeVisible(true);			
				scrollComposite.setMinSize(scrollContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				
			}
			public void maximize(CTabFolderEvent event) {
				tabFolderTop.setMinimized(false);
				tabFolderTop.setMinimizeVisible(true);
				tabFolderTop.setMaximizeVisible(false);				
				scrollComposite.setMinSize(scrollContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		});		
		tabFolderTop.setSelection(0);
		tabFolderTop.setMinimizeVisible(true);
		tabFolderTop.setMaximizeVisible(false);		
		tabFolderTop.setSelectionBackground(Colors.VIDEO_OVERVIEW_ITEM_BG.getColor());		
		tabFolderTop.setBackground(Colors.EDITOR_TABSINGLE_CONTENT_BG.getColor());		

		// Das einzige Item für das obere Tab
		CTabItem item = new CTabItem(tabFolderTop, SWT.NONE);
		item.setText(nodeScene.getTitle());
		
		// Composite hält den Annotationsüberblick und das Video inkl.
		// Videocontrol
		final Composite itemContent = new Composite(tabFolderTop, SWT.CENTER);
		GridData topGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		GridLayout topLayout = new GridLayout(2, false);
		topLayout.horizontalSpacing = 70;
		itemContent.setLayout(topLayout);
		itemContent.setLayoutData(topGridData);
		item.setControl(itemContent);
		
		// hält die Toolbar zum Erstellen der Annoation und die Suchauswahl
		Composite itemContentLeftTop = new Composite(itemContent, SWT.CENTER);
		itemContentLeftTop.setLayout(new GridLayout(2, false));
		
		// initialisiere das Auswalhmenü
		createAnnotationChooseButton(itemContentLeftTop);
				
		// Composite zur Auswahl der Sortierung für den Annotationsüberblick
		ArrayList<BeanComparator> comparators = new ArrayList<BeanComparator>();
		for (BeanComparator bc : BeanComparator.values()) {
			comparators.add(bc);
		}
		BComparatorComposite bcc = new BComparatorComposite(itemContentLeftTop, SWT.CENTER,
				Messages.AnnotationEditor_Sort_Tooltip, comparators);
		
		// MediaPlayer
		Composite itemContentTopRight = new Composite(itemContent, SWT.CENTER);
		GridLayout itemContentTopRightGL = new GridLayout(1, false);
		itemContentTopRightGL.marginTop=10;
		itemContentTopRight.setLayout(itemContentTopRightGL);
		GridData itemContentTopRightGD = new GridData(SWT.FILL, SWT.FILL, false, false);
		itemContentTopRightGD.verticalSpan = 2;
		itemContentTopRight.setLayoutData(itemContentTopRightGD);

		new MediaPlayerWidget(itemContentTopRight, SWT.RIGHT, mp, true, true);
		
		// hält die Annoationsübersicht und das MediaPlayerWidget
		Composite itemContentLeftBottom = new Composite(itemContent, SWT.CENTER);
		itemContentLeftBottom.setLayout(new GridLayout(2, false));
					
		aov = new AnnotationOverview(itemContentLeftBottom, SWT.LEFT, nodeScene, 360, mp);
		aov.addSelectionChangedListener(this);
		bcc.addSivaEventConsumer(aov);
			
		// Tabs: jedes Tab enthält den Editor für genau eine Annotation
		tabFolder = new CTabFolder(scrollContent, SWT.TOP);
		tabFolder.setBorderVisible(true);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tabFolder.setSimple(false);
		tabFolder.setUnselectedCloseVisible(false);
		// Listener auf den TabFolder, wird verwendet, wenn ein Tab
		// angeklickt wird
		tabFolder.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				CTabItem curTab = tabFolder.getSelection();
				if (curTab != null) {
					AnnotationDefineWidget adw = (AnnotationDefineWidget) curTab
							.getControl();
					StructuredSelection selection = new StructuredSelection(
							adw.getAnnotation());
					aov.setSelection(selection);
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		tabFolder.setMinimizeVisible(false);
		tabFolder.setMaximizeVisible(false);
		// Set up a gradient background for the selected tab
		tabFolder.setSelectionBackground(Colors.EDITOR_BG.getColor());
		
		tabFolder.setBackground(Colors.EDITOR_TABSINGLE_CONTENT_BG
				.getColor());
		tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				if (event.item instanceof CTabItem) {
					CTabItem closedTab = (CTabItem) event.item;
					if (closedTab.getControl() instanceof AnnotationDefineWidget) {
						AnnotationDefineWidget tabWid = (AnnotationDefineWidget) closedTab
								.getControl();
						boolean saved = saveAnnotation(tabWid);
						if (!saved) {
							event.doit = false;
							return;
						}
						tabWid.dispose();
					}
					// falls das letzte Tab geschlossen wird, entferne die
					// Selektion im Overview
					if (tabFolder.getSelectionIndex() == 0) {
						StructuredSelection selection = StructuredSelection.EMPTY;
						aov.setSelection(selection);
					}
				}
			}
		});
		
		scrollComposite.setMinSize(scrollContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	private void createAnnotationChooseButton(Composite parent) {
		// Toolbar zum Erstellen einer Annotation
		final Menu menu = new Menu(Display.getCurrent().getActiveShell(), SWT.POP_UP);
		
		// Button zum Erzeugen der neuen Annos
		final Button createButton = new Button(parent, SWT.PUSH);
		createButton.setImage(Icons.ACTION_ANNOTATION_CREATE.getImage());		
		createButton.setToolTipText(Messages.AnnotationType_CreateButtonText1 + " " + Messages.AnnotationType_CreateButtonText2); //$NON-NLS-1$
		createButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Point pt = new Point(createButton.getBounds().x + createButton.getBounds().width/2, createButton.getBounds().y/2 + createButton.getBounds().height/2);
				pt = createButton.toDisplay(pt);
				menu.setLocation(pt.x, pt.y);
				menu.setVisible(true);				
			}
		});
		
		// füge die Gruppen in das Auswahlmenü ein
		for (AnnotationGroup group : AnnotationGroup.values()) {
			if (group.inUse()) {
				MenuItem groupItem = new MenuItem (menu, SWT.CASCADE);
				groupItem.setText(group.getName());
				
				// das Untermenü mit der eigentlichen Annotationsauswahl
				Menu annoMenu = new Menu (menu);
				groupItem.setMenu(annoMenu);
				
				// füge die zur Gruppe gehörenden Annotationen als Untermenü ein
				for (final AnnotationType at : AnnotationType.values()) {
					// füge die Standard Annotationen ein
					if (at.inUse() && at.getAnnotationGroup().equals(group)) {
						final MenuItem mitem = new MenuItem(annoMenu, SWT.PUSH);
						mitem.setText(getAnnotationMenuTitle(at.getMenuName()));
						mitem.setData(at);
						mitem.addListener(SWT.Selection, new Listener() {
							@Override
							public void handleEvent(Event event) {
								createNewAnnotation(at, mp);
							}
						});
					}
				}
			}
		}
	}

	/**
	 * prüft ob eine Annotation bereits ein geöffnetes Tab besitzt
	 * 
	 * @param annotation
	 * @return true, Tab existiert
	 */
	private boolean isOpen(INodeAnnotation annotation) {
		// alle Tabs
		CTabItem[] tabs = tabFolder.getItems();

		// prüfe ob die übergebene Annotation, zu einem Tab gehört
		for (int i = 0; i < tabs.length; i++) {
			AnnotationDefineWidget defineWidget = (AnnotationDefineWidget) tabs[i]
					.getControl();
			if (annotation.getTitle().equals(
					defineWidget.getAnnotation().getTitle())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * schließe das Tab der Annotation
	 * 
	 * @param annotation
	 * @return true, Tab existiert
	 */
	private void closeTab(INodeAnnotation annotation) {
		// alle Tabs
		CTabItem[] tabs = tabFolder.getItems();

		// prüfe ob die übergebene Annotation, zu einem Tab gehört
		for (int i = 0; i < tabs.length; i++) {
			AnnotationDefineWidget defineWidget = (AnnotationDefineWidget) tabs[i]
					.getControl();
			if (annotation.getTitle().equals(
					defineWidget.getAnnotation().getTitle())) {
				tabs[i].dispose();
			}
		}
	}

	// öffnet/erstellt das zur aktuellen Selektion gehörende Tab
	private void openTab(INodeAnnotation selectedAnnotation) {
		
		// falls noch kein Tab geöffnet ist
		if (!isOpen(selectedAnnotation)) {
			CTabItem tabItem = new CTabItem(tabFolder, SWT.CLOSE);
			tabItem.setText(selectedAnnotation.getTitle());
			tabFolder.setSelection(tabItem);

			// bestimme den Annotationstyp
			AnnotationType annoType = AnnotationFactory.getAnnotationTypeForAnnotation(selectedAnnotation);
			
			// Das Widget zum Editieren/Erstellen einer
			// Annotation
			final AnnotationDefineWidget newAn = new AnnotationDefineWidget(
					tabFolder, SWT.CENTER, selectedAnnotation, annoType, mp, tabItem,
					nodeScene, this);
			
			tabItem.setControl(newAn);
			tabItem.addDisposeListener(new DisposeListener() {				
				@Override
				public void widgetDisposed(DisposeEvent e) {
					newAn.dispose();					
				}
			});
		} else {
			// vergleiche die Tabnamen mit dem Namen der
			// Annotation
			CTabItem[] tabs = tabFolder.getItems();
			for (int i = 0; i < tabs.length; i++) {
				String tabName = tabs[i].getText();
				// falls die Datei zum Speichern ist, muss beim Namensvergleich
				// der * entfernt werden
				int starPos = tabName.lastIndexOf('*');
				if (starPos > 0) {
					if (starPos + 1 == tabName.length()) {
						tabName = tabName.substring(0, starPos);
					}
				}
				String name = selectedAnnotation.getTitle();
				// auf Tab der angeklickten Annotation wechseln
				if (tabName.equals(name)) {
					tabFolder.setSelection(tabs[i]);
				}
			}
		}
		scrollComposite.setMinSize(scrollContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	/**
	 * Pops up a save dialog if the annotation has changed since the last save
	 * operation. 
	 * 
	 * @return false if a save operation has been initiated but could not be
	 * executed - true otherwise
	 * @param wid
	 */
	private boolean saveAnnotation(AnnotationDefineWidget wid) {
		if (wid.isDirty()) {
			MessageBox messageBox = new MessageBox(Display.getCurrent()
					.getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			messageBox.setMessage(Messages.AnnotationEditor_MsgBox_SaveAnnotation_Text);
			messageBox.setText(Messages.AnnotationEditor_MsgBox_SaveAnnotation_Title);
			int option = messageBox.open();
			if (option == SWT.YES) {
				return wid.executeSaveOperation();
			}
		}
		return true;
	}
	
	/**
	 * liefert den Text für den Menüeintrag zum Erstellen einer neuen Annotation
	 * @param title
	 * @return
	 */
	public String getAnnotationMenuTitle(String title) {
		return Messages.AnnotationType_CreateButtonText1 + title + "-" + Messages.AnnotationType_CreateButtonText2;  //$NON-NLS-1$
	}	

	// erstellt eine neue temporäre Annotation, explizit gespeichert wird
	// sie erst später
	public void createNewAnnotation(AnnotationType annoType, MediaPlayer mp) {
		INodeAnnotation newAnnotation = AnnotationFactory.getAnnotationForType(annoType);
		lastAddedAnnotationType = annoType;
		
		newAnnotation.adjustTimeToScene(nodeScene);
		logger.debug("Creating new annotation..."); //$NON-NLS-1$

		// die zuletzt von diesem Typ erstellte Annotation
		INodeAnnotation lastAdded = lastAddedAnnotations.get(annoType);

		if (lastAdded != null) {
			// setze die letzte Endzeit einer Annotation auf den Start der
			// Szene falls die Endzeit dem Ende der Szene entspricht
			long lastEndTime = lastAdded.getEnd();
			if (lastEndTime != -1) {
				if (lastEndTime == nodeScene.getScene().getEnd()) {
					lastEndTime = nodeScene.getScene().getStart();
				}
				newAnnotation.setStart(lastEndTime);
			}
			newAnnotation.setScreenArea(lastAdded.getScreenArea());
		}
		
		// for mark annos set duration to max duration time intially
		if (annoType.getAnnotationGroup().equals(AnnotationGroup.MARK)) {
			long maxDuration = nodeScene.getScene().getEnd() 
					- newAnnotation.getStart();
			((NodeMark)newAnnotation).setDuration(maxDuration);
		}
		
		CTabItem tabItem = new CTabItem(tabFolder, SWT.CLOSE);
		tabItem.setText(Messages.AnnotationEditor_TabTitle_UnnamedAnnotation1
				+ annoType.getName()
				+ Messages.AnnotationEditor_TabTitle_UnnamedAnnotation2);
		tabFolder.setSelection(tabItem);

		// Das Widget zum Editieren/Erstellen einer Annotation
		final AnnotationDefineWidget newAn = new AnnotationDefineWidget(tabFolder,
				SWT.NONE, newAnnotation, annoType, mp, tabItem, nodeScene, this);
		tabItem.setControl(newAn);
		tabItem.addDisposeListener(new DisposeListener() {				
			@Override
			public void widgetDisposed(DisposeEvent e) {
				newAn.dispose();					
			}
		});
		
		scrollComposite.setMinSize(scrollContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		updateDirtyStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (tabFolder != null) {
			tabFolder.setFocus();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {	
		super.propertyChange(event);
		
		// Close the editor if the underlying NodeScene is deleted
		if (event.getPropertyName().equals(Graph.PROP_NODE_REMOVED)) {
			if (event.getOldValue() == nodeScene) {
				 getSite().getPage().closeEditor(this, false);
			}
		}		
		if (event.getPropertyName().equals(Graph.PROP_CONNECTION_ADDED)) {			
			// React on added annotation
			if (event.getNewValue() instanceof IConnection) {
				IConnection conn = (IConnection) event.getNewValue();
				IGraphNode source = conn.getSource();
				if (source == nodeScene) {
					// Ein Kind wurde zu unserem Szenen-Knoten hinzugefuegt
					IGraphNode child = conn.getTarget();
					if (child instanceof INodeAnnotationLeaf 
							|| child instanceof NodeMark ) {
						INodeAnnotation addedAnno = (INodeAnnotation) child;
						if (addedAnno != null) {
							lastAddedAnnotations.remove(lastAddedAnnotationType);
							lastAddedAnnotations.put(lastAddedAnnotationType,
									addedAnno);
							StructuredSelection selection = new StructuredSelection(addedAnno);
							if (!aov.isDisposed()) {
								aov.setSelection(selection);								
							}
						}
					}
				}
			}			
		}
		if (event.getPropertyName().equals(Graph.PROP_CONNECTION_REMOVED)) {
			// React on removed annotation
			if (event.getOldValue() instanceof IConnection) {
				IConnection conn = (IConnection) event.getOldValue();
				IGraphNode source = conn.getSource();
				if (source == nodeScene) {
					// Ein Kind wurde von unserem Szenen-Knoten geloescht
					IGraphNode child = conn.getTarget();
					if (child instanceof INodeAnnotationLeaf 
							|| child instanceof NodeMark ) {
						closeTab((INodeAnnotation) child);
					}
				}
			}
		}		
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		INodeAnnotation selectedAnnotation = 
				(INodeAnnotation) ((StructuredSelection) event
						.getSelection()).getFirstElement();
		if (selectedAnnotation != null) {
			openTab(selectedAnnotation);
			// setze im slider die Markierungspunkte
			mp.forwardEvent(new SivaEvent(null, 
					SivaEventType.MARK_POINT_START, 
					new SivaTime(selectedAnnotation.getStart() 
							- nodeScene.getScene().getStart())));						
			mp.forwardEvent(new SivaEvent(null, 
					SivaEventType.MARK_POINT_END, 
					new SivaTime(selectedAnnotation.getEnd() 
							- nodeScene.getScene().getStart())));
		}	
	}
	
	/**
	 * Returns the <code>Scene</code> for which this editor can create annotations.
	 * @return underlying <code>Scene</code>
	 */
	public Scene getScene() {
		return nodeScene.getScene();
	}

	@Override
	protected IAbstractBean getKeyObject() {
		return nodeScene.getScene();
	}
}
