package org.iviPro.views.miniaturegraph;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.iviPro.application.Application;
import org.iviPro.editors.scenegraph.SceneGraphEditor;
import org.iviPro.editors.scenegraph.SceneGraphEditorInput;
import org.iviPro.model.Project;
import org.iviPro.model.graph.Graph;
import org.iviPro.theme.Icons;
import org.iviPro.views.IAbstractView;

/**
 * View fuer die Miniatur-Ansicht des Szenen-Graphen.
 * 
 * @author dellwo
 * 
 */
public class MiniatureGraph extends IAbstractView {

	/** Logger */
	private static Logger logger = Logger.getLogger(MiniatureGraph.class);
	/** ID des Miniatur-Ansicht Views */
	public static final String ID = MiniatureGraph.class.getName();
	/** Verweis auf die scrollbare Miniatur-Ansicht */
	private ScrollableThumbnail thumbnail;
	/** Das Panel auf dem der gesamte Inhalt des Editors sitzt. */
	private Composite editorPanel;
	/** Das Panel auf dem die Miniatur-Ansicht sitzt. */
	private Composite thumbPanel;

	/**
	 * Erstellung den View und zeigt die Miniatur-Ansicht an, falls der
	 * Szenen-Graph bereits geoeffnet ist.
	 */
	@Override
	public void createPartControlImpl(Composite parent) {
		logger.debug("Creating part control..."); //$NON-NLS-1$
		this.editorPanel = parent;
		// Wenn Szenen-Graph geoeffnet -> Update thumbnail view
		Project project = Application.getCurrentProject();
		if (project != null) {
			Graph graph = project.getSceneGraph();
			IEditorPart editor = Application
					.getEditor(new SceneGraphEditorInput(graph));
			if (editor instanceof SceneGraphEditor) {
				updateThumbnailView(((SceneGraphEditor) editor)
						.getRootEditPart());
			}
		}

	}

	/**
	 * Wird aufgerufen wenn der Szenen-Editor geoeffnet wird. In diesem Fall
	 * soll der View die Miniaturansicht dieses Szenen-Editors darstellen.
	 * 
	 * @param editor
	 *            Der Szenen-Editor.
	 */
	private void onSceneGraphEditorOpened(SceneGraphEditor editor) {
		// Zeige Minitaturansicht an
		ScalableFreeformRootEditPart rootEditPart = editor.getRootEditPart();
		updateThumbnailView(rootEditPart);
	}

	/**
	 * Wird aufgerufen wenn der Szenen-Editor geschlossen wird. In diesem Fall
	 * soll der View nichts mehr anzeigen und ebenfalls geschlossen werden.
	 * 
	 * @param editor
	 *            Der Szenen-Editor
	 */
	private void onSceneGraphEditorClosed(SceneGraphEditor editor) {
		// Zeige nichts mehr an
		updateThumbnailView(null);
		// Blende Thumbnailview selbst aus
		IWorkbenchPage page = getSite().getWorkbenchWindow().getActivePage();
		if (page != null) {
			if (page.isPartVisible(this)) {
				page.hideView(this);
			}
		}
	}

	/**
	 * Updated den Thumbnail-View und zeigt den gegebenen Graphen an. Falls noch
	 * kein Thumbnail-View erstellt wurde, wird er automatisch erstellt.
	 * 
	 * @param rootEditPart
	 *            Der Root-EditPart des anzuzeigenden Graphen. Wenn null
	 *            uebergeben wird, wird kein Thumbnail-View angezeigt.
	 */
	private void updateThumbnailView(
			final ScalableFreeformRootEditPart rootEditPart) {
		// Das Editor-Panel darf nicht disposed sein
		if (editorPanel.isDisposed()) {
			logger.debug("Editor pane is disposed!!"); //$NON-NLS-1$
			return;
		}

		if (thumbnail == null && rootEditPart != null) {
			// Wenn es noch keine Thumbnail-Anzeige gibt, dann erstellen wir
			// sie hier
			logger.debug("Creating thumbnail view..."); //$NON-NLS-1$
			createThumbnailView(editorPanel, rootEditPart);
		} else if (rootEditPart == null) {
			// Wenn kein Root-EditPart gegeben wurde, soll kein Thumbnail-View
			// angezeigt werden -> Alten Thumbnail-Pane entfernen
			logger.debug("Disposing thumbnail view..."); //$NON-NLS-1$
			thumbPanel.dispose();
			thumbnail = null;
			editorPanel.layout(true);
		}

	}

	/**
	 * Erstellt die Thumbnail-View fuer einen gegebenen Graphen.
	 * 
	 * @param parent
	 *            Das Composite, auf das der Thumbnail-View eingefuegt werden
	 *            soll.
	 * @param rootEditPart
	 *            Der Root-EditPart des Graphen fuer den der Thumbnail-View
	 *            angezeigt werden soll.
	 */
	private void createThumbnailView(final Composite parent,
			final ScalableFreeformRootEditPart rootEditPart) {
		// Erstelle Panel auf den das Thumbnail kommt
		thumbPanel = new Composite(parent, SWT.NONE);
		thumbPanel.setLayout(new FillLayout(SWT.HORIZONTAL | SWT.VERTICAL));

		// Erstelle Thumbnail-View
		Canvas canvas = new Canvas(thumbPanel, SWT.BORDER);
		canvas.setBackground(parent.getDisplay()
				.getSystemColor(SWT.COLOR_WHITE));
		LightweightSystem lws = new LightweightSystem(canvas);
		thumbnail = new ScrollableThumbnail((Viewport) rootEditPart.getFigure());
		thumbnail.setSource(rootEditPart
				.getLayer(LayerConstants.PRINTABLE_LAYERS));
		lws.setContents(thumbnail);

		// Layoute die Komponenten
		parent.layout();

	}

	/**
	 * Ueberschrieben, um den Thumbnail-View als Part-Listener zu registrieren,
	 * damit er mitbekommt, wenn der Szenen-Graph geöffnet oder geschlossen
	 * wird.
	 * 
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		// Fuege Part-Listener hinzu, der mitkriegt, wenn der Szenen-Graph
		// geoeffnet oder geschlossen wird.
		getSite().getPage().addPartListener(new IPartListener2() {

			@Override
			public void partOpened(IWorkbenchPartReference partRef) {
				IWorkbenchPart editor = partRef.getPart(false);
				if (editor instanceof SceneGraphEditor) {
					onSceneGraphEditorOpened((SceneGraphEditor) editor);
				}
				logger.debug("part opened: " + partRef.getId()); //$NON-NLS-1$
			}

			@Override
			public void partClosed(IWorkbenchPartReference partRef) {
				IWorkbenchPart editor = partRef.getPart(false);
				if (editor instanceof SceneGraphEditor) {
					onSceneGraphEditorClosed((SceneGraphEditor) editor);
				}
				logger.debug("part closed: " + partRef.getId()); //$NON-NLS-1$
			}

			@Override
			public void partVisible(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partInputChanged(IWorkbenchPartReference partRef) {
				logger.trace("part input changed: " + partRef.getId()); //$NON-NLS-1$
			}

			@Override
			public void partHidden(IWorkbenchPartReference partRef) {
				logger.trace("part hidden: " + partRef.getId()); //$NON-NLS-1$
			}

			@Override
			public void partDeactivated(IWorkbenchPartReference partRef) {
				logger.trace("part deactivated: " + partRef.getId()); //$NON-NLS-1$
			}

			@Override
			public void partBroughtToTop(IWorkbenchPartReference partRef) {
				logger.trace("part brought to top: " + partRef.getId()); //$NON-NLS-1$
			}

			@Override
			public void partActivated(IWorkbenchPartReference partRef) {
				logger.trace("part activated: " + partRef.getId()); //$NON-NLS-1$
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#getDefaultImage()
	 */
	@Override
	protected Image getDefaultImage() {
		return Icons.VIEW_MINIATUREGRAPH.getImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
	}

}
