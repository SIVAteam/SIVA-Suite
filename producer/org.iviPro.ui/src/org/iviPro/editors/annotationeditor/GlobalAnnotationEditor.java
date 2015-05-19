package org.iviPro.editors.annotationeditor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.iviPro.actions.undoable.GlobalAnnotationDeleteAction;
import org.iviPro.application.Application;
import org.iviPro.editors.IAbstractEditor;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationFactory;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationType;
import org.iviPro.editors.annotationeditor.components.GlobalAnnotationDefineWidget;
import org.iviPro.model.BeanList;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeAnnotationAudio;
import org.iviPro.model.graph.NodeAnnotationPdf;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.NodeAnnotationRichtext;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.graph.NodeAnnotationText;
import org.iviPro.model.graph.NodeAnnotationVideo;
import org.iviPro.theme.Icons;
import org.iviPro.views.IAbstractRepositoryFilter;

/**
 * @author juhoffma
 * 
 */
public class GlobalAnnotationEditor extends IAbstractEditor {

	private static Logger logger = Logger
			.getLogger(GlobalAnnotationEditor.class);
	public static final String ID = GlobalAnnotationEditor.class.getName();

	// hält für jede globale Annotation einen Tab
	private CTabFolder tabFolder = null;

	// die zuletzt hinzugefügeten Annotationen
	private HashMap<AnnotationType, INodeAnnotation> lastAddedAnnotations = new HashMap<AnnotationType, INodeAnnotation>();
	
	// der zuletzt hinzugefügte GlobalAnnotationType
	private AnnotationType lastAddedGlobalAnnotationType;

	private TreeViewer treeViewer;

	private Label infoLabel;

	// TreeNodes für die Annotationsübersicht
	private TreeNode video;
	private TreeNode audio;
	private TreeNode picture;
	private TreeNode text;
	private TreeNode subtitle;
	private TreeNode pdf;
	
	// das Suchfeld
	private Text searchField;
	
	// für die Suche wird der Abstrakte Filter aus den Views verwendet
	private IAbstractRepositoryFilter treeViewerFilter;
	
	private BeanList<INodeAnnotationLeaf> annotations;

	public GlobalAnnotationEditor() {
		super();
		logger.debug("Creating annotation editor."); //$NON-NLS-1$
	}

	@Override
	public Image getDefaultImage() {
		return Icons.EDITOR_ANNOTATION.getImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		CTabItem[] tabs = tabFolder.getItems();
		for (int i = 0; i < tabs.length; i++) {
			GlobalAnnotationDefineWidget defineWidget = (GlobalAnnotationDefineWidget) tabs[i]
					.getControl();
			if (defineWidget.isDirty()) {
				defineWidget.executeSaveOperation();
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
		setSite(site);
		setInput(input);
		setPartName(Messages.GlobalAnnotationEditorName);
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
			GlobalAnnotationDefineWidget defineWidget = (GlobalAnnotationDefineWidget) tabs[i]
					.getControl();
			if (!defineWidget.isDisposed()) {
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
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControlImpl(final Composite parent) {
		
		GridLayout griLayout = new GridLayout(1, false);
		parent.setLayout(griLayout);
		
		// Scrolling
		final ScrolledComposite scrollComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		scrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrollComposite.setLayout(new GridLayout(1, true));			
		final Composite scrollContent = new Composite(scrollComposite, SWT.NONE);
		scrollContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrollContent.setLayout(new GridLayout(2, false));
		scrollComposite.setContent(scrollContent);		
		scrollComposite.setMinSize(860, 600);
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.setExpandVertical(true);
		
		scrollComposite.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				if (video != null) {
					video.icon.dispose();
				}
				if (audio != null) {
					audio.icon.dispose();
				}
				if (picture != null) {
					picture.icon.dispose();
				}
				if (text != null) {
					text.icon.dispose();
				}
				if (subtitle != null) {
					subtitle.icon.dispose();
				}
				if (pdf != null) {
					pdf.icon.dispose();
				}
			}			
		});
		
		scrollComposite.addListener(SWT.Activate, new Listener() {
	        public void handleEvent(Event e) {
	        	scrollComposite.setFocus();
	        }
	    });
		scrollComposite.getVerticalBar().setIncrement(10);
				
				
		// Container für die Liste und zum Anlegen der Annotationen
		Composite container = new Composite(scrollContent, SWT.TOP | SWT.BORDER);
		container.setLayout(new GridLayout(2, false));
		GridData contGD = new GridData(SWT.CENTER, SWT.FILL, false, true);
		contGD.widthHint = 180;
		container.setLayoutData(contGD);
		
		annotations = Application.getCurrentProject().getGlobalAnnotations();
		
		// für den Anlegenbutton + Löschbutton
		Composite buttonComposite = new Composite(container, SWT.CENTER);
		GridData buttonCompositeGD = new GridData();
		buttonCompositeGD.horizontalSpan = 2;
		buttonComposite.setLayoutData(buttonCompositeGD);
		buttonComposite.setLayout(new GridLayout(2, false));
		
		final Menu menu = new Menu(Display.getCurrent().getActiveShell(), SWT.POP_UP);	
		final Button createButton = new Button(buttonComposite, SWT.CENTER);
		createButton.setToolTipText(Messages.AnnotationType_CreateButtonText1 + " " + Messages.GlobalAnnotationEditor_CreateGlobalAnnotation2_Tooltip); //$NON-NLS-1$
		createButton.setImage(Icons.ACTION_ANNOTATION_CREATE.getImage());
		createButton.setMenu(menu);
		createButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Rectangle rect = createButton.getBounds();
				Point pt = new Point(rect.x + rect.width/2, rect.y + rect.height/2);
				pt = createButton.toDisplay(pt);
				menu.setLocation(pt.x, pt.y);
				menu.setVisible(true);
			}
		});		

		final Button itemRemove = new Button(buttonComposite, SWT.PUSH);
		itemRemove.setToolTipText(Messages.GlobalAnnotationEditor_Remove);
		itemRemove.setImage(Icons.ACTION_GLOBAL_ANNOTATION_DELETE.getImage());
		itemRemove.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) treeViewer
						.getSelection();
				List<IStructuredSelection> list = selection.toList();
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i) instanceof TreeNode) {
						TreeNode curNode = (TreeNode) list.get(i);
						List<INodeAnnotationLeaf> leafs = new LinkedList<INodeAnnotationLeaf>();
						if (curNode.isLeaf()) {
							leafs.add(curNode.getElement());
							new GlobalAnnotationDeleteAction(leafs).run();
						}
					}
				}
			}
		});
		
		infoLabel = new Label(container, SWT.BORDER | SWT.CENTER);
		infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		infoLabel.setText(Messages.GlobalAnnotationEditor_Label + " " //$NON-NLS-1$
				+ annotations.size());
		
		// Control fuer Suchfeld erstellen
		Composite searchCtrl = createSearchControl(container);
		GridData searchCtrlGD = new GridData(SWT.FILL, SWT.TOP, true, false);
		searchCtrlGD.horizontalSpan = 2;
		searchCtrl.setLayoutData(searchCtrlGD);

		// Menüeinträge, entsprechend Enumeration der AnnotationsTypen
		// (AnnotationType)
		for (final AnnotationType at : AnnotationType.values()) {
			if (at.inUse() && at.allowGlobal()) {
				final MenuItem mitem = new MenuItem(menu, SWT.PUSH);
				mitem.setText(getNewGlobalAnnotationMenuTitle(at.getMenuName()));
				mitem.setData(at);
				mitem.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event event) {
						createNewGlobalAnnotation(at);
					}
				});
			}
		}			

		// Liste globaler Annotationen
		treeViewer = new TreeViewer(container, SWT.V_SCROLL | SWT.MULTI);
		treeViewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewerFilter = new SearchFilter();
		treeViewer.addFilter(treeViewerFilter);		
		annotations.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if (treeViewer.getTree().isDisposed()) {
					return;
				}
				INodeAnnotationLeaf changedAnno = (INodeAnnotationLeaf) e
						.getNewValue();
				updateTreeViewer();
				if (e.getPropertyName().equals(BeanList.PROP_ITEM_REMOVED)) {
					closeTab(changedAnno);
				} else if (e.getPropertyName().equals(BeanList.PROP_ITEM_ADDED)) {
					updateSelection(changedAnno);
				}
				infoLabel.setText(Messages.GlobalAnnotationEditor_Label
						+ " " //$NON-NLS-1$
						+ Application.getCurrentProject()
								.getGlobalAnnotations().size());
			}
		});

		treeViewer.setContentProvider(new ITreeContentProvider() {

			@Override
			public Object[] getChildren(Object arg0) {
				if (arg0 instanceof TreeNode) {
					TreeNode node = (TreeNode) arg0;
					if (!node.isLeaf()) {
						return node.getChildren().toArray();
					}
				}
				return null;
			}

			@Override
			public Object getParent(Object arg0) {
				if (arg0 instanceof TreeNode) {
					return ((TreeNode) arg0).getParent();
				}
				return null;
			}

			@Override
			public boolean hasChildren(Object arg0) {
				if (arg0 instanceof TreeNode) {
					TreeNode node = (TreeNode) arg0;
					if (!node.isLeaf()) {
						return true;
					}
				}
				return false;
			}

			@Override
			public Object[] getElements(Object arg0) {
				video = new TreeNode(Messages.GlobalAnnotationEditor_TreeNodeVideo, 
						Icons.OBJECT_MEDIA_VIDEO.getImage());
				audio = new TreeNode(Messages.GlobalAnnotationEditor_TreeNodeAudio, 
						Icons.OBJECT_MEDIA_AUDIO.getImage());
				picture = new TreeNode(Messages.GlobalAnnotationEditor_TreeNodePicture, 
						Icons.OBJECT_MEDIA_PICTURE.getImage());
				text = new TreeNode(Messages.GlobalAnnotationEditor_TreeNodeText, 
						Icons.OBJECT_MEDIA_TEXT_PLAIN.getImage());
				subtitle = new TreeNode(Messages.GlobalAnnotationEditor_TreeNodeSubtitle,
						Icons.OBJECT_MEDIA_TEXT_SUBTITLE.getImage());
				pdf = new TreeNode(Messages.GlobalAnnotationEditor_TreeNodePdf, Icons.OBJECT_MEDIA_PDF.getImage());	

				for (INodeAnnotationLeaf leaf : annotations) {
					TreeNode newTreeNode = new TreeNode();
					newTreeNode.setElement(leaf);
					if (leaf instanceof NodeAnnotationAudio) {
						audio.addChild(newTreeNode);
					} else if (leaf instanceof NodeAnnotationVideo) {
						video.addChild(newTreeNode);
					} else if (leaf instanceof NodeAnnotationPicture) {
						picture.addChild(newTreeNode);
					} else if (leaf instanceof NodeAnnotationText || leaf instanceof NodeAnnotationRichtext) {
						text.addChild(newTreeNode);
					} else if (leaf instanceof NodeAnnotationSubtitle) {
						subtitle.addChild(newTreeNode);
					} else if (leaf instanceof NodeAnnotationPdf) {
						pdf.addChild(newTreeNode);
					}
				}
				LinkedList<Object> list = new LinkedList<Object>();
				list.add(video);
				list.add(audio);
				list.add(text);
				list.add(picture);
				list.add(subtitle);
				list.add(pdf);
				return list.toArray();
			}

			@Override
			public void dispose() {

			}

			@Override
			public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
			}

		});

		treeViewer.setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				if (element instanceof TreeNode) {
					return ((TreeNode) element).getIcon();
				}
				return null;
			}

			public String getText(Object element) {
				if (element instanceof TreeNode) {
					return ((TreeNode) element).getTitle();
				}
				return null;
			}
		});

		treeViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof TreeNode && e2 instanceof TreeNode) {
					TreeNode n1 = (TreeNode) e1;
					TreeNode n2 = (TreeNode) e2;
					if (n1.isLeaf() && n2.isLeaf()) {
						return n1.getTitle().compareTo(n2.getTitle());
					}
					if (!n1.isLeaf() && !n2.isLeaf()) {
						return n1.getTitle().compareTo(n2.getTitle());
					}
				}
				return 0;
			}
		});

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(SelectionChangedEvent e) {
				List<INodeAnnotationLeaf> leafs = new LinkedList<INodeAnnotationLeaf>();
				IStructuredSelection selection = (IStructuredSelection) treeViewer
						.getSelection();
				List<IStructuredSelection> list = selection.toList();
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i) instanceof TreeNode) {
						TreeNode curNode = (TreeNode) list.get(i);
						if (curNode.isLeaf()) {
							leafs.add(curNode.getElement());
						}
					}
				}
				if (selection.getFirstElement() instanceof TreeNode) {
					TreeNode treeNode = (TreeNode) selection.getFirstElement();
					if (treeNode.isLeaf()) {
						INodeAnnotationLeaf selectedAnnotation = treeNode
								.getElement();
						if (selectedAnnotation != null) {
							openTab(selectedAnnotation);
							MenuManager menuManager = new MenuManager();
							menuManager.add(new GlobalAnnotationDeleteAction(
									leafs));
							treeViewer.getTree().setMenu(
									menuManager.createContextMenu(treeViewer
											.getControl()));
						}

					}
				}
			}
		});
		treeViewer.setInput(annotations);
		treeViewer.expandAll();
		
		// Tabs: jedes Tab enthält den Editor für genau eine Annotation
		tabFolder = new CTabFolder(scrollContent, SWT.TOP);
		tabFolder.setBorderVisible(true);
		GridData tabFolderGD = new GridData(SWT.CENTER, SWT.FILL, false, true);
		tabFolderGD.widthHint = 654;
		tabFolder.setLayoutData(tabFolderGD);
		tabFolder.setSimple(false);
		tabFolder.setUnselectedCloseVisible(false);

		// Listener auf den TabFolder, wird verwendet, wenn ein Tab
		// angeklickt wird
		tabFolder.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				CTabItem curTab = tabFolder.getSelection();
				if (curTab != null) {
					GlobalAnnotationDefineWidget adw = (GlobalAnnotationDefineWidget) curTab
							.getControl();
					updateSelection((INodeAnnotationLeaf) adw.getAnnotation());
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		tabFolder.setMinimizeVisible(false);
		tabFolder.setMaximizeVisible(false);
		// Set up a gradient background for the selected tab
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_TITLE_BACKGROUND));
		tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				if (event.item instanceof CTabItem) {
					CTabItem closedTab = (CTabItem) event.item;
					if (closedTab.getControl() instanceof GlobalAnnotationDefineWidget) {
						GlobalAnnotationDefineWidget tabWid = (GlobalAnnotationDefineWidget) closedTab
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
						treeViewer.setSelection(StructuredSelection.EMPTY);
					}
				}
			}
		});
	}

	private void updateSelection(INodeAnnotationLeaf leaf) {
		TreeNode selectionTreeNode = new TreeNode();
		selectionTreeNode.setElement(leaf);
		StructuredSelection selection = new StructuredSelection(
				selectionTreeNode);
		// Kategorie aufklappen, der veränderten Annotation
		// setselection macht das nicht automatisch?!?
		if (leaf instanceof NodeAnnotationAudio) {
			treeViewer.expandToLevel(audio, 1);
		} else if (leaf instanceof NodeAnnotationVideo) {
			treeViewer.expandToLevel(video, 1);
		} else if (leaf instanceof NodeAnnotationPicture) {
			treeViewer.expandToLevel(picture, 1);
		} else if (leaf instanceof NodeAnnotationText) {
			treeViewer.expandToLevel(text, 1);
		} else if (leaf instanceof NodeAnnotationSubtitle) {
			treeViewer.expandToLevel(subtitle, 1);
		} else if (leaf instanceof NodeAnnotationPdf) {
			treeViewer.expandToLevel(pdf, 1);
		}
		treeViewer.setSelection(selection, true);
	}

	private void updateTreeViewer() {
		// öffne die Elemente wieder die vorher auch geöffnet waren
		Object[] objects = treeViewer.getExpandedElements();
		treeViewer.refresh();
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] instanceof TreeNode) {
				TreeNode node = (TreeNode) objects[i];
				treeViewer.setExpandedState(node, true);
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
			GlobalAnnotationDefineWidget defineWidget = (GlobalAnnotationDefineWidget) tabs[i]
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
			GlobalAnnotationDefineWidget defineWidget = (GlobalAnnotationDefineWidget) tabs[i]
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

			AnnotationType annotationType = AnnotationFactory.getAnnotationTypeForAnnotation(selectedAnnotation);
			
			// Das Widget zum Editieren/Erstellen einer
			// Annotation
			final GlobalAnnotationDefineWidget newAn = new GlobalAnnotationDefineWidget(
					tabFolder, SWT.CENTER, selectedAnnotation, annotationType, tabItem, this);
			tabItem.setControl(newAn);
			selectedAnnotation
					.addPropertyChangeListener(new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if (treeViewer.getTree().isDisposed()) {
								return;
							}
							updateTreeViewer();
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
	}

	/**
	 * Pops up a save dialog if the annotation has changed since the last save
	 * operation.
	 * 
	 * @return false if a save operation has been initiated but could not be
	 * executed - true otherwise  
	 * @param wid
	 */
	private boolean saveAnnotation(GlobalAnnotationDefineWidget wid) {
		if (wid.isDirty()) {
			MessageBox messageBox = new MessageBox(Display.getCurrent()
					.getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			messageBox
					.setMessage(Messages.AnnotationEditor_MsgBox_SaveAnnotation_Text);
			messageBox
					.setText(Messages.AnnotationEditor_MsgBox_SaveAnnotation_Title);

			int option = messageBox.open();
			if (option == SWT.YES) {
				return wid.executeSaveOperation();
			}
		}
		return true;
	}
	
	/**
	 * String für den Menüeintrag zur Auswahl einer neuen globalen Annotation
	 * @param title
	 * @return
	 */
	private String getNewGlobalAnnotationMenuTitle(String title) {
		return Messages.AnnotationType_CreateButtonText1 + title + "-" + Messages.AnnotationType_CreateButtonText2;  //$NON-NLS-1$
	}	

	// erstellt eine neue temporäre Annotation, explizit gespeichert wird
	// sie erst später
	private void createNewGlobalAnnotation(AnnotationType annotationType) {
		final INodeAnnotation newAnnotation = AnnotationFactory.getAnnotationForAnnotationType(annotationType);
		lastAddedGlobalAnnotationType = annotationType;

		logger.debug("Creating new annotation..."); //$NON-NLS-1$

		// die zuletzt von diesem Typ erstellte Annotation
		INodeAnnotation lastAdded = lastAddedAnnotations.get(lastAddedGlobalAnnotationType);

		if (lastAdded != null) {
			newAnnotation.setScreenArea(lastAdded.getScreenArea());
		}

		CTabItem tabItem = new CTabItem(tabFolder, SWT.CLOSE);
		tabItem.setText(Messages.AnnotationEditor_TabTitle_UnnamedAnnotation1
				+ annotationType.getName()
				+ Messages.AnnotationEditor_TabTitle_UnnamedAnnotation2);
		tabFolder.setSelection(tabItem);

		// Das Widget zum Editieren/Erstellen einer Annotation
		final GlobalAnnotationDefineWidget newAn = new GlobalAnnotationDefineWidget(
				tabFolder, SWT.CENTER, newAnnotation, annotationType, tabItem, this);
		newAnnotation.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (treeViewer.getTree().isDisposed()) {
					return;
				}
				updateTreeViewer();
			}
		});
		lastAddedAnnotations.put(annotationType, newAnnotation);
		tabItem.setControl(newAn);
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
	
	// erstellt das Suchfeld
	private Composite createSearchControl(Composite parent) {
		// Create the search panel with the search controls
		final Composite searchPane = new Composite(parent, SWT.NONE);
		searchPane.setLayout(new GridLayout(2, false));
		// Paint only a bottom border - As SWT has no support for custom borders
		// we must paint it ourselves.
		searchPane.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				Color borderColor = e.gc.getDevice().getSystemColor(
						SWT.COLOR_WIDGET_NORMAL_SHADOW);
				e.gc.setForeground(borderColor);
				e.gc.drawLine(0, e.height - 1, e.width, e.height - 1);
				e.gc.dispose();
			}
		});

		// Add label
		Label label = new Label(searchPane, SWT.NONE);
		label.setText(Messages.LabelSearch);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		// Add text field for search term			
		this.searchField = new Text(searchPane, SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		this.searchField.setLayoutData(gridData);
		this.searchField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				onSearchTextChanged(searchField.getText());
			}
		});
		return searchPane;
	}
	
	/**
	 * Wird aufgerufen wenn sich der Text im Suchfeld aendert.
	 * 
	 * @param newSearchText
	 *            Der neue Text im Suchfeld.
	 */
	private void onSearchTextChanged(String newSearchText) {
		logger.debug("Search text changed: " + newSearchText); //$NON-NLS-1$
		treeViewerFilter.setFilterText(newSearchText);		
		treeViewer.setInput(annotations);
		treeViewer.setSelection(treeViewer.getSelection());
		treeViewer.expandAll();
	}
	
	private class SearchFilter extends IAbstractRepositoryFilter {

		@Override
		protected boolean filter(String filterText, Object element,
				Object parentElement, Viewer viewer) {
			if (element instanceof TreeNode) {	
				
				TreeNode curNode = (TreeNode) element;
				INodeAnnotationLeaf curLeaf = curNode.getElement();
				
				if (curLeaf != null) {				
					String name = curLeaf.getTitle().toLowerCase();
					if (name.contains(filterText.toLowerCase())) {
						return true;
					}
					return false;
				}
			}
			return true;
		}		
	}

	private class TreeNode {
		private String title;
		private Image icon;
		private List<TreeNode> children;
		private TreeNode parent;
		private INodeAnnotationLeaf element = null;
		private boolean isLeaf = false;

		public TreeNode() {
		}

		public TreeNode(String name, Image icon) {
			this.title = name;
			this.icon = icon;
			this.children = new LinkedList<TreeNode>();
		}

		public void setParent(TreeNode parent) {
			this.parent = parent;
		}

		public TreeNode getParent() {
			return this.parent;
		}

		public String getTitle() {
			if (isLeaf) {
				return element.getLocalizedTitle().getValue();
			}
			return this.title;
		}

		public Image getIcon() {
			if (isLeaf) {
				return parent.getIcon();
			}
			return this.icon;
		}

		public boolean isLeaf() {
			return isLeaf;
		}

		public List<TreeNode> getChildren() {
			return this.children;
		}

		public void addChild(TreeNode child) {
			child.setParent(this);
			this.children.add(child);
		}

		public void setElement(INodeAnnotationLeaf element) {
			this.isLeaf = true;
			this.element = element;
		}

		public INodeAnnotationLeaf getElement() {
			return this.element;
		}

		@Override
		public boolean equals(Object e) {
			if (e instanceof TreeNode) {
				TreeNode compTreeNode = (TreeNode) e;
				if (isLeaf() && compTreeNode.isLeaf()) {
					if (compTreeNode.getTitle().equals(getTitle())) {
						return true;
					}
				}
				if (!isLeaf() && !compTreeNode.isLeaf()) {
					if (compTreeNode.getTitle().equals(getTitle())) {
						return true;
					}
				}
			}
			return false;
		}	
	}
}
