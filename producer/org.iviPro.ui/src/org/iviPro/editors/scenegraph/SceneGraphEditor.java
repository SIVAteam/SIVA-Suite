package org.iviPro.editors.scenegraph;

import java.util.EventObject;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.AlignmentAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.iviPro.editors.scenegraph.actions.NodeEditAction;
import org.iviPro.editors.scenegraph.actions.SemanticFisheyeAction;
import org.iviPro.editors.scenegraph.actions.SemanticZoomInAction;
import org.iviPro.editors.scenegraph.actions.SemanticZoomOutAction;
import org.iviPro.editors.scenegraph.dnd.TransferMediaDropTargetListener;
import org.iviPro.editors.scenegraph.dnd.TransferSceneDropTargetListener;
import org.iviPro.editors.scenegraph.editparts.PartFactory;
import org.iviPro.model.graph.Graph;
import org.iviPro.operations.OperationHistory;
import org.iviPro.theme.Icons;

/**
 * Der Szenen-Graph Editor:<br>
 * <br>
 * Um ein neues Model-Objekt im Graph darzustellen ist folgendes noetig:<br>
 * <br>
 * <ol>
 * <li>Figure fuer das Objekt erstellen, welche die Darstellung uebernimmt.</li>
 * <li>Edit-Part fuer das Objekt erstellen.</li>
 * <li>Im Edit-Part festlegen: NodeConnectionPolicy -> Erlaubte Knoten fuer
 * eingehende Verbindungen</li>
 * <li>Im Edit-Part festlegen: NodeDeletePolicy -> Ist loeschen des Knotens
 * erlaubt?</li>
 * <li>Erweitern der PartFactory um einen Eintrag fuer den Edit-Part</li>
 * <li>Erweitern der ModelObjectFactory um einen Eintrag fuer das Model-Objekt</li>
 * <li>Erweitern von EditPartGraph.isSupported() um Eintrag fuer Model-Objekt</li>
 * <li>Erweitern von EditPartGraph.getCreateCommand() um Eintrag guer
 * Model-Objekt</li>
 * </ol>
 * 
 */
public class SceneGraphEditor extends GraphicalEditorWithFlyoutPalette {

	private static Logger logger = Logger.getLogger(SceneGraphEditor.class);
	public static final String ID = SceneGraphEditor.class.getName();

	/** This is the root of the editor's model. */
	private Graph modelRoot;

	/** Palette component, holding the tools and elements that can be used. */
	private PaletteRoot palette;

	/** Der Root-EditPart auf dem alle weiteren Edit-Parts liegen. */
	private ScalableFreeformRootEditPart rootEditPart;

	/** Zoom Action **/
	IAction zoomInAction;
	IAction zoomOutAction;
	IAction semanticZoomInAction;
	IAction semanticZoomOutAction;
	IAction semanticFisheyeAction;
	ToolItem fisheyeButton;


	/**
	 * Erstellt einen neuen Szenengraph-Editor. Dieser Konstruktor wird von der
	 * Eclipse workbench aufgerufen.
	 */
	public SceneGraphEditor() {
		// Setze die Edit-Domain fuer den Editor und den CommandStack mit
		// Operation Support, damit unsere Operationen fuer das globale
		// Undo/Redo richtig verwendet werden.
		DefaultEditDomain editDomain = new DefaultEditDomain(this);
		editDomain.setCommandStack(new CommandStackWithOperationSupport());
		setEditDomain(editDomain);
		rootEditPart = new ScalableFreeformRootEditPart();
		
		//Einstellungen der Flyout Palette mit seinen Werkzeugen
		// Default: Anbringung rechts und ausgeklappt
		FlyoutPreferences fp = this.getPalettePreferences();
		fp.setDockLocation(PositionConstants.EAST);
		fp.setPaletteState(FlyoutPaletteComposite.STATE_PINNED_OPEN);
	}

	/**
	 * Erstellt die Actions die im SceneGraphEditor verwendet werden. Diese
	 * koennen dann z.B. im ContextMenuProvider ueber die Action-Registry
	 * abgerufen werden.<br>
	 * <br>
	 * Bei Actions die von SelectionAction abgeleitet sind, muessen diese zu
	 * getSelectionActions() hinzugefuegt werden, damit diese ueber die aktuelle
	 * Auswahl informiert werden.
	 * 
	 */
	@SuppressWarnings( { "unchecked" })
	@Override
	protected void createActions() {
		super.createActions();
		IEditorSite site = getEditorSite();
		ActionRegistry registry = getActionRegistry();
		IAction a;

		// Undo-Action -> Registriere als globalen Action-Handler fuer Undo
		a = new UndoActionHandler(site, OperationHistory.getContext());
		a.setId(ActionFactory.UNDO.getId());
		site.getActionBars().setGlobalActionHandler(a.getId(), a);
		registry.registerAction(a);

		// Redo-Action -> Registriere als globalen Action-Handler fuer Redo
		a = new RedoActionHandler(site, OperationHistory.getContext());
		a.setId(ActionFactory.REDO.getId());
		site.getActionBars().setGlobalActionHandler(a.getId(), a);
		registry.registerAction(a);

		// Rename action
		a = new NodeEditAction(getSite().getPart());
		registry.registerAction(a);
		getSelectionActions().add(a.getId());

		// Align left action
		a = new AlignmentAction(getSite().getPart(), PositionConstants.LEFT);
		registry.registerAction(a);
		getSelectionActions().add(a.getId());

		// Align right action
		a = new AlignmentAction(getSite().getPart(), PositionConstants.RIGHT);
		registry.registerAction(a);
		getSelectionActions().add(a.getId());

		// Align top action
		a = new AlignmentAction(getSite().getPart(), PositionConstants.TOP);
		registry.registerAction(a);
		getSelectionActions().add(a.getId());

		// Align bottom action
		a = new AlignmentAction(getSite().getPart(), PositionConstants.BOTTOM);
		registry.registerAction(a);
		getSelectionActions().add(a.getId());

		// Align middle action
		a = new AlignmentAction(getSite().getPart(), PositionConstants.MIDDLE);
		registry.registerAction(a);
		getSelectionActions().add(a.getId());

		// Align middle action
		a = new AlignmentAction(getSite().getPart(), PositionConstants.CENTER);
		registry.registerAction(a);
		getSelectionActions().add(a.getId());

		// Zoom in action
		zoomInAction = new ZoomInAction(rootEditPart.getZoomManager());
		registry.registerAction(zoomInAction);

		// Zoom out action
		zoomOutAction = new ZoomOutAction(rootEditPart.getZoomManager());
		registry.registerAction(zoomOutAction);		

		//Semantic Zoom In Action
		semanticZoomInAction = new SemanticZoomInAction(getSite().getPart());
		registry.registerAction(semanticZoomInAction);
		getSelectionActions().add(semanticZoomInAction.getId());

		//Semantic Zoom Out Action
		semanticZoomOutAction = new SemanticZoomOutAction(getSite().getPart());
		registry.registerAction(semanticZoomOutAction);
		getSelectionActions().add(semanticZoomOutAction.getId());

		//Semantic Fisheye Action
		semanticFisheyeAction = new SemanticFisheyeAction(getSite().getPart());
		registry.registerAction(semanticFisheyeAction);
		getSelectionActions().add(semanticFisheyeAction.getId());

		KeyHandler keyHandler = new KeyHandler();
		keyHandler.put(
				KeyStroke.getPressed(SWT.DEL, 127, 0),
				registry.getAction(ActionFactory.DELETE.getId())) ;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
	 */
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		if (input instanceof SceneGraphEditorInput) {
			modelRoot = ((SceneGraphEditorInput) input).getGraph();			
			setPartName("Szenengraph"); //$NON-NLS-1$
			
			
			//setPartName(input.getName());
		}
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		return false;
	}

	/**
	 * Configure the graphical viewer before it receives contents.
	 * <p>
	 * This is the place to choose an appropriate RootEditPart and
	 * EditPartFactory for your editor. The RootEditPart determines the behavior
	 * of the editor's "work-area". For example, GEF includes zoomable and
	 * scrollable root edit parts. The EditPartFactory maps model elements to
	 * edit parts (controllers).
	 * </p>
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
	 */
	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();

		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setEditPartFactory(new PartFactory());

		viewer.setRootEditPart(rootEditPart);

		KeyHandler keyHandler = new GraphicalViewerKeyHandler(viewer);
		keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
				getActionRegistry().getAction(ActionFactory.DELETE.getId()));
		viewer.setKeyHandler(keyHandler);

		// configure the context menu provider
		ContextMenuProvider cmProvider = new SceneGraphContextMenuProvider(
				viewer, getActionRegistry());
		viewer.setContextMenu(cmProvider);
		getSite().registerContextMenu(cmProvider, viewer);
	}

	public ScalableFreeformRootEditPart getRootEditPart() {
		return rootEditPart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.ui.parts.GraphicalEditor#commandStackChanged(java.util
	 * .EventObject)
	 */
	public void commandStackChanged(EventObject event) {
		firePropertyChange(IEditorPart.PROP_DIRTY);
		super.commandStackChanged(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#
	 * createPaletteViewerProvider()
	 */
	protected PaletteViewerProvider createPaletteViewerProvider() {
		return new PaletteViewerProvider(getEditDomain()) {
			protected void configurePaletteViewer(PaletteViewer viewer) {
				super.configurePaletteViewer(viewer);
				// create a drag source listener for this palette viewer
				// together with an appropriate transfer drop target listener,
				// this will enable
				// model element creation by dragging a
				// CombinatedTemplateCreationEntries
				// from the palette into the editor
				// @see SceneGraphEditor#createTransferDropTargetListener()
				viewer.addDragSourceListener( //
						new TemplateTransferDragSourceListener(viewer));
			}
		};
	}

	/**
	 * Create a transfer drop target listener. When using a
	 * CombinedTemplateCreationEntry tool in the palette, this will enable model
	 * element creation by dragging from the palette.
	 * 
	 * @see #createPaletteViewerProvider()
	 */
	private TransferDropTargetListener createTransferDropTargetListener() {
		return new TemplateTransferDropTargetListener(getGraphicalViewer()) {
			protected CreationFactory getFactory(Object template) {
				return new ModelObjectFactory((Class) template);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		getCommandStack().markSaveLocation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getAdapter(
	 * java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class type) {
		return super.getAdapter(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getPaletteRoot
	 * ()
	 */
	protected PaletteRoot getPaletteRoot() {
		if (palette == null)
			palette = SceneGraphEditorPaletteFactory.createPalette();
		return palette;
	}

	/**
	 * Set up the editor's inital content (after creation).	 	
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#initializeGraphicalViewer()
	 */
	@Override
	protected void initializeGraphicalViewer() {
		super.initializeGraphicalViewer();	
		GraphicalViewer viewer = getGraphicalViewer();		
		viewer.setContents(modelRoot); // set the contents of this editor
		// listen for dropped parts
		viewer.addDropTargetListener(createTransferDropTargetListener());
		viewer.addDropTargetListener( //
				new TransferSceneDropTargetListener(viewer));
		viewer.addDropTargetListener( //
				new TransferMediaDropTargetListener(viewer));
		logger.debug("Initialized graphical viewer."); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public Image getDefaultImage() {
		return Icons.EDITOR_SCENEGRAPH.getImage();
	}


	@Override
	public void createPartControl(Composite parent)
	{
		//Container Composite
		Composite c = new Composite(parent, SWT.None);
		c.setLayout(new GridLayout(2, false));
		
		//Toolbar + Items
		ToolBar tb = new ToolBar(c, SWT.VERTICAL);
		tb.setLayoutData(new org.eclipse.swt.layout.GridData(
				org.eclipse.swt.layout.GridData.FILL_VERTICAL));

		//ZoomIn Icon
		ToolItem zoomInButton = new ToolItem(tb, SWT.PUSH);
		zoomInButton.setImage(Icons.GRAPH_ZOOMIN.getImage());
		zoomInButton.setToolTipText(Messages.SceneGraphEditor_geometricZoomIn);
		zoomInButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				zoomInAction.run();	
			} 	
		});
		
		//ZoomOut Icon
		ToolItem zoomOutButton = new ToolItem(tb, SWT.PUSH);
		zoomOutButton.setImage(Icons.GRAPH_ZOOMOUT.getImage());
		zoomOutButton.setToolTipText(Messages.SceneGraphEditor_geometricZoomOut);
		zoomOutButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				zoomOutAction.run();
			} 	
		});

		//Separators
		ToolItem sep1 = new ToolItem(tb, SWT.SEPARATOR);
		ToolItem sep2 = new ToolItem(tb, SWT.SEPARATOR);
		ToolItem sep3 = new ToolItem(tb, SWT.SEPARATOR);

		//Semantic ZoomIn Icon
		ToolItem semanticZoomInButton = new ToolItem(tb, SWT.PUSH);
		semanticZoomInButton.setImage(Icons.GRAPH_SEMANTICZOOMIN.getImage());
		semanticZoomInButton.setToolTipText(semanticZoomInAction.getToolTipText());
		semanticZoomInButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				semanticZoomInAction.run();	
			} 	
		});
		
		//Semantic ZoomOut Icon
		ToolItem semanticZoomOutButton = new ToolItem(tb, SWT.PUSH);
		semanticZoomOutButton.setImage(Icons.GRAPH_SEMANTICZOOMOUT.getImage());
		semanticZoomOutButton.setToolTipText(semanticZoomOutAction.getToolTipText());
		semanticZoomOutButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				semanticZoomOutAction.run();
			} 	
		});

		//Separators
		ToolItem sep4 = new ToolItem(tb, SWT.SEPARATOR);
		ToolItem sep5 = new ToolItem(tb, SWT.SEPARATOR);
		ToolItem sep6 = new ToolItem(tb, SWT.SEPARATOR);

		//Semantic Fisheye Icon
		fisheyeButton = new ToolItem(tb, SWT.CHECK);
		fisheyeButton.setImage(Icons.GRAPH_FISHEYE.getImage());
		fisheyeButton.setToolTipText(semanticFisheyeAction.getToolTipText());
		fisheyeButton.setSelection(false);
		fisheyeButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				semanticFisheyeAction.run();
				fisheyeButton.setToolTipText(semanticFisheyeAction.getToolTipText());
			} 	
		});
		
		//Neues composite für den Graphen (Kind von Container c)
		Composite graphicalViewerContainer = new Composite(c, SWT.None);
		graphicalViewerContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		graphicalViewerContainer.setLayout(new FillLayout());

		super.createPartControl(graphicalViewerContainer);
	}
}