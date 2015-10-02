package org.iviPro.views.scenerepository;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.iviPro.actions.nondestructive.OpenEditorAction;
import org.iviPro.actions.nondestructive.OpenSceneGraphAction;
import org.iviPro.actions.undoable.SceneDeleteAction;
import org.iviPro.application.Application;
import org.iviPro.dnd.TransferScene;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;
import org.iviPro.theme.Icons;
import org.iviPro.views.IAbstractRepositoryFilter;
import org.iviPro.views.IAbstractRepositoryView;

public class SceneRepository extends IAbstractRepositoryView implements
		SceneManagerListener {
	private static Logger logger = Logger.getLogger(SceneRepository.class);
	public static final String ID = SceneRepository.class.getName();

	/** Unterstuetzte Transfer-Typen fuer ausgehende Drag-Events */
	public static final Transfer[] TRANSFER_DRAG_TYPE = new Transfer[] { TransferScene
			.getInstance() };
	/** Unterstuetzte Transfer-Typen fuer eingehende Drop-Events */
	public static final Transfer[] TRANSFER_DROP_TYPE = null;

	private SceneManager sceneManager;

	public SceneRepository() {
		// Listener initalisieren
		sceneManager = new SceneManager();
		sceneManager.setSceneManagerListener(this);
	}

	/**
	 * Selects a scene in the scene repository.
	 * 
	 * @param scene
	 * The scene that should be selected or null if no scene should
	 * be selected.
	 */
	public void selectScene(Scene scene) {
		if (scene == null) {
			logger.debug("Unselecting all scenes."); //$NON-NLS-1$
			getTreeViewer().setSelection(TreeSelection.EMPTY);
		} else {
			SceneTreeLeaf leaf = sceneManager.findLeaf(scene);
			if (leaf != null) {
				logger.debug("Selecting scene: " //$NON-NLS-1$
						+ scene.getTitle(Application.getCurrentLanguage()));
				getTreeViewer().setSelection(new StructuredSelection(leaf));				
			}
		}
	}
	
	public void addSelectionListener(ISelectionChangedListener listener) {
		getTreeViewer().addSelectionChangedListener(listener);
	}

	@Override
	public void notifySceneContainerChanged() {
		logger.debug("Scene-Container has changed."); //$NON-NLS-1$	
		updateTreeviewer();		
	}
	
	@Override
	public void updateTreeviewer() {
		if (treeViewer == null || treeViewer.getTree().isDisposed()) {
			return;
		}	
	
		treeViewer.setInput(getTreeRoot());						
		treeViewer.expandAll();
	}
	
	public Scene getCurrentSelection() {
		TreeSelection selection = (TreeSelection) (getTreeViewer().getSelection());
		if (selection.isEmpty()) {
			return null;
		} else {
			if (selection.getFirstElement() instanceof SceneTreeLeaf) {
				return ((SceneTreeLeaf) selection.getFirstElement()).getScene();
			}
		}
		return null;
	}

	@Override
	protected MenuManager createContextMenu(TreeViewer treeViewer) {
		IWorkbenchWindow window = this.getSite().getWorkbenchWindow();
		MenuManager menuManager = new MenuManager();
		menuManager.add(new OpenEditorAction(window));
		final SceneDeleteAction delScene = new SceneDeleteAction(window);
		menuManager.add(delScene);
		menuManager.add(new Separator());
		OpenSceneGraphAction osga = new OpenSceneGraphAction(window);
		osga.setImageDescriptor(Icons.ACTION_EDITOR_SCENEGRAPH.getImageDescriptor());		
		osga.setDisabledImageDescriptor(Icons.ACTION_EDITOR_SCENEGRAPH
				.getDisabledImageDescriptor());
		menuManager.add(osga);
		
		treeViewer.getTree().addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					delScene.run();
				}
			}
		});

		// Actions in die Toolbar einfügen
		toolBarManager.add(new OpenEditorAction(window));
		toolBarManager.add(new SceneDeleteAction(window));
		toolBarManager.add(new Separator());
		toolBarManager.add(osga);
		toolBarManager.update(true);
		return menuManager;
	}

	@Override
	protected IAbstractRepositoryFilter createRepositoryFilter() {
		return new SceneRepositoryFilter();
	}

	@Override
	protected Image getDefaultImage() {
		return Icons.VIEW_SCENEREPOSITORY.getImage();
	}

	@Override
	protected Transfer[] getDragTransferTypes() {
		return TRANSFER_DRAG_TYPE;
	}

	@Override
	protected Transfer[] getDropTransferTypes() {
		return TRANSFER_DROP_TYPE;
	}

	@Override
	protected IAbstractBean[] getObjectsToTransfer(TreeItem[] selItems) {
		List<Scene> selScenes = new ArrayList<Scene>(selItems.length);
		for (int i = 0; i < selItems.length; i++) {
			Object selItem = selItems[i].getData();
			if (selItem instanceof SceneTreeLeaf) {
				Scene scene = ((SceneTreeLeaf) selItem).getScene();
				selScenes.add(scene);
			}
		}
		Scene[] transferObjects = new Scene[selScenes.size()];
		transferObjects = selScenes.toArray(transferObjects);
		return transferObjects;
	}

	@Override
	protected Object initTreeRoot() {
		return sceneManager.getRoot();
	}

	@Override
	protected boolean isDragAllowed(Object selItem) {
		return selItem instanceof SceneTreeLeaf;
	}

	@Override
	protected void onDoubleClick(Object selectedElement) {
		if (selectedElement instanceof SceneTreeLeaf) {
			SceneTreeLeaf leaf = (SceneTreeLeaf) selectedElement;
			Video video = leaf.getScene().getVideo();
			new OpenEditorAction(SceneRepository.this.getSite()
					.getWorkbenchWindow(), video).run();
			selectScene(leaf.getScene());
		}
	}

	@Override
	protected void onDrop(DropTargetEvent event) {
	}

	@Override
	protected void onProjectClosed(Project project) {
	}

	@Override
	protected void onProjectOpened(Project project) {
	}

	@Override
	protected void registerTreeItemsAdapter() {
		Platform.getAdapterManager().registerAdapters(
				new SceneAdapterFactory(), SceneTreeNode.class);
	}

	@Override
	protected void handleMouseMove(Event arg0) {
			
	}
	
	@Override
	protected void handleMouseExit(Event arg0) {
	
	}	
}
