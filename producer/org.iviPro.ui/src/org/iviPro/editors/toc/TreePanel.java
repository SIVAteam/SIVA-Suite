package org.iviPro.editors.toc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.iviPro.application.Application;
import org.iviPro.dnd.TransferMedia;
import org.iviPro.dnd.TransferScene;
import org.iviPro.editors.common.SivaComposite;
import org.iviPro.model.BeanList;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.TocItem;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.global.ChangeTitleOperation;
import org.iviPro.operations.toc.ChangeSceneOperation;
import org.iviPro.operations.toc.ChangeTocItemOperation;
import org.iviPro.operations.toc.MoveTocItemOperation;
/**
 * Panel zur Verwaltung des Inhaltsverzeichnisses und der Szenenliste
 * @author langa
 *
 */
public class TreePanel extends SivaComposite {

	// Baum des Inhaltsverzeichnisses
	private Tree tocItems;
	private TocItem currSelection;
	private TreeItem toSelect;
	private TocItem root;
	private TreeEditor editor;
	
	// Liste der Szenen
	private Tree sceneList;
	// Interne Liste mit allen Graphknoten
	private List<IGraphNode> nodes;

	public TreePanel(Composite parent, int style) {
		super(parent, style);
		root = Application.getCurrentProject().getTableOfContents();
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		GridLayout layout = new GridLayout(2, false);
		setLayout(layout);

		Label tocItemsLabel = new Label(this, SWT.CENTER);
		tocItemsLabel.setText(Messages.TreePanel_LEFT_HEADER);
		Label tocItemsLabel2 = new Label(this, SWT.CENTER);
		tocItemsLabel2.setText(Messages.TreePanel_RIGHT_HEADER);
		tocItemsLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		tocItemsLabel2.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		
		tocItems = new Tree(this, SWT.MULTI | SWT.BORDER);

		GridData treeLayout = new GridData();
		treeLayout.widthHint = 400;
		treeLayout.heightHint = 500;
		tocItems.setLayoutData(treeLayout);

		// Deselektieren aller Elemente bei Klick außerhalb aller Einträge
		tocItems.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (tocItems.getItem(new Point(event.x, event.y)) == null) {
					tocItems.deselectAll();
				}

			}
		});

		tocItems.setMenu(initMenu());
		tocItems.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode==SWT.DEL){
					deleteItem();
				}
			}			
		});
		
		editor = new TreeEditor(tocItems);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;

		drawTree();

		initDropTarget();
		
		sceneList = new Tree(this, SWT.BORDER);
		GridData treeD = new GridData();
		treeD.widthHint = 300;
		treeD.heightHint = 500;
		treeD.grabExcessVerticalSpace = true;
		sceneList.setLayoutData(treeD);
		drawSceneList();			
		initDragSupport();
		initDragSupportforTOC();
	}
	
	protected TocItem getRoot() {
		return root;
	}

	private Menu initMenu() {
		// Popupmenü des Baumes initialisieren
		Menu popup = new Menu(tocItems);
		popup.addMenuListener(new MenuAdapter() {			
			@Override
			public void menuShown(MenuEvent e) {
				if (tocItems.getSelection()[0] != null) {
					final TreeItem item = tocItems.getSelection()[0];
					NodeScene scene = ((TocItem) item.getData()).getScene();
					if (scene == null) {
						tocItems.getMenu().getItem(2).setEnabled(false);
					} else {
						tocItems.getMenu().getItem(2).setEnabled(true);
					}
				}
			}
		});
			
		MenuItem rename = new MenuItem(popup, SWT.PUSH);
		rename.setText(Messages.TreePanel_MenuRename);
		rename.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				renameItem();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		MenuItem delete = new MenuItem(popup, SWT.PUSH);
		delete.setText(Messages.TreePanel_MenuDelete);
		
		delete.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteItem();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		MenuItem removeScene = new MenuItem(popup, SWT.PUSH);
		removeScene.setText(Messages.TreePanel_MenuRemoveScene);
		removeScene.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tocItems.getSelectionCount() > 0) {

					TocItem item = (TocItem) tocItems.getSelection()[0]
							.getData();
					currSelection = item;
					ChangeSceneOperation operation = new ChangeSceneOperation(
							item, null);
					try {
						OperationHistory.execute(operation);
					} catch (ExecutionException e1) {
						operation.getErrorMessage(e1);
					}
					drawTree();
					tocItems.showSelection();
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		return popup;
	}

	protected void addItem(String text) {
		if (text == null || text.equals("")) { //$NON-NLS-1$
			text = Messages.TreePanel_DefaultNewNodeTitle;
		}

		TocItem item;
		List<TocItem> newChildren = new LinkedList<TocItem>();
		
		if (tocItems.getSelection().length > 0) {			
			item = ((TocItem) tocItems.getSelection()[0].getData());
		} else {
			item = root;
		}
		currSelection = item;
		List<TocItem> children = item.getChildren();
		for (TocItem t : children) {
			newChildren.add(t);
		}
		TocItem newItem = new TocItem(text, Application.getCurrentProject());
		newChildren.add(newItem);
		ChangeTocItemOperation operation = new ChangeTocItemOperation(item,
				newChildren);
		try {
			OperationHistory.execute(operation);
		} catch (ExecutionException e) {
			operation.getErrorMessage(e);
		}
		tocItems.showSelection();
	}
	
	private void deleteItem() {
		if (tocItems.getSelection().length > 0) {
			TreeItem selection = tocItems.getSelection()[0];

			boolean delete = true;
			if (moreThanTwoSubItems(selection, 0)) {
				delete = MessageDialog
						.openQuestion(getShell(), Messages.TreePanel_Delete_Confirmation_Title,
								Messages.TreePanel_Delete_Confirmation_Text);
			}

			if (delete) {
				TreeItem parent = selection.getParentItem();
				TocItem item = (TocItem) selection.getData();
				TocItem newSelection = null;
				TocItem parentToc;
				List<TocItem> pChildren = null;
				if (parent != null) {
					parentToc = ((TocItem) parent.getData());
					pChildren = parentToc.getChildren();
				} else {
					pChildren = root.getChildren();
					parentToc = root;
				}

				List<TocItem> newChildren = new LinkedList<TocItem>();
				for (TocItem t : pChildren) {
					newChildren.add(t);
				}
				if(newChildren.indexOf(item) - 1 >= 0){
					newSelection = newChildren.get(newChildren.indexOf(item) - 1);
				} else if(newChildren.indexOf(item) + 1 < newChildren.size()) {
					newSelection = newChildren.get(newChildren.indexOf(item) + 1);
				} else {
					newSelection = parentToc;
				}
				
				newChildren.remove(item);
				
				ChangeTocItemOperation operation = new ChangeTocItemOperation(
						parentToc, newChildren);
				try {
					OperationHistory.execute(operation);
				} catch (ExecutionException e) {
					operation.getErrorMessage(e);
				}

				tocItems.deselectAll();
				selectItem(tocItems.getItems(), newSelection);
				tocItems.showSelection();
			}

		}
	}
	
	private boolean selectItem(TreeItem[] tree, TocItem item){
		for(TreeItem i : tree){
			if((TocItem)i.getData() == item){
				tocItems.select(i);
				return true;
			}
			if(selectItem(i.getItems(), item)){
				return true;
			}
		}
		return false;
	}
	
	private boolean moreThanTwoSubItems(TreeItem item, int total) {
		TreeItem[] subItems = item.getItems();
		total += subItems.length;
		boolean warn = false;
		if (total >= 2) {
			warn = true;
		} else {
			for (TreeItem t : subItems) {
				warn = warn ? warn : moreThanTwoSubItems(t, total);
			}
		}
		return warn;
	}
	
	protected void moveDown() {
		if (tocItems.getSelection().length > 0) {
			TreeItem selection = tocItems.getSelection()[0];
			TreeItem parent = selection.getParentItem();
			TocItem pToc;
			if (parent != null) {
				pToc = (TocItem) parent.getData();
			} else {
				pToc = root;
			}
			
			TocItem item = (TocItem) selection.getData();
			currSelection = item;
			List<TocItem> pChildren = null;
			if (parent != null) {
				pChildren = pToc.getChildren();

			} else {
				pChildren = root.getChildren();
			}

			if (pChildren.size() > 1) {
				List<TocItem> newChildren = new LinkedList<TocItem>();
				for (TocItem t : pChildren) {
					newChildren.add(t);
				}
				int index = newChildren.indexOf(item);
				if (index != -1 && index < newChildren.size() - 1) {
					newChildren.remove(index);
					newChildren.add(index + 1, item);
					ChangeTocItemOperation operation = new ChangeTocItemOperation(
							pToc, newChildren);
					try {
						OperationHistory.execute(operation);
					} catch (ExecutionException e) {
						operation.getErrorMessage(e);
					}
				}
			}

		}
		tocItems.showSelection();
	}
	
	protected void moveUp() {
		if (tocItems.getSelection().length > 0) {
			TreeItem selection = tocItems.getSelection()[0];
			// Vater im Baum suchen == Vater im Model
			TreeItem parent = selection.getParentItem();
			TocItem pToc;
			if (parent != null) {
				pToc = (TocItem) parent.getData();
			} else {
				pToc = root;
			}
			TocItem item = (TocItem) selection.getData();
			currSelection = item;
			List<TocItem> pChildren = null;
			if (parent != null) {
				pChildren = pToc.getChildren();

			} else {
				pChildren = root.getChildren();
			}

			if (pChildren.size() > 1) {
				List<TocItem> newChildren = new LinkedList<TocItem>();
				for (TocItem t : pChildren) {
					newChildren.add(t);
				}
				int index = newChildren.indexOf(item);
				if (index != -1 && index > 0) {
					newChildren.remove(index);
					newChildren.add(index - 1, item);
					ChangeTocItemOperation operation = new ChangeTocItemOperation(
							pToc, newChildren);
					try {
						OperationHistory.execute(operation);
					} catch (ExecutionException e) {
						operation.getErrorMessage(e);
					}
				}
			}
		}
		tocItems.showSelection();
	}

	@SuppressWarnings("unchecked")
	protected void drawTree() {
		BeanList<TocItem> items = (BeanList) root.getChildren();
		// Erst alle Elemente aus dem Baum löschen
		tocItems.removeAll();
		for (TocItem t : items) {
			TreeItem i = new TreeItem(tocItems, SWT.None);
			NodeScene s = t.getScene();
			// Listeneintrag aus TocItemtitel und Szenentitel zusammensetzen
			String title = t.getTitle();

			if (s != null) {
				title = title.concat(Messages.TreePanel_EntryTitle_1 + s.getTitle() 
						+ Messages.TreePanel_EntryTitle_2 
						+ s.getNodeID() + Messages.TreePanel_EntryTitle_3);
			}
			i.setText(title);
			i.setData(t);
			if (currSelection == t) {
				toSelect = i;
			}
			// Kinder der Wurzel zeichnen
			List<TocItem> children = t.getChildren();
			if (children.size() > 0) {
				drawChildren(children, i);
			}
			i.setExpanded(true);

		}

		// Zuletzt selektiertes Element wieder auswählen
		if (toSelect != null) {
			tocItems.select(toSelect);
			toSelect = null;
		}

	}

	private void drawChildren(List<TocItem> children, TreeItem i) {
		// Kindknoten auslesen
		for (TocItem c : children) {
			TreeItem ci = new TreeItem(i, SWT.None);
			NodeScene s = c.getScene();
			String title = c.getTitle();
			if (s != null) {
				title = title.concat(Messages.TreePanel_EntryTitle_1 + s.getTitle() 
						+ Messages.TreePanel_EntryTitle_2 
						+ s.getNodeID() + Messages.TreePanel_EntryTitle_3); //$NON-NLS-1$ //$NON-NLS-2$
			}
			ci.setText(title);
			ci.setData(c);
			if (currSelection == c) {
				toSelect = ci;
			}
			// Rekursiv alle Kindknoten abarbeiten
			List<TocItem> cChildren = c.getChildren();
			if (cChildren.size() > 0) {
				drawChildren(cChildren, ci);
			}
			ci.setExpanded(true);
		}
	}

	private void renameItem() {
		if (tocItems.getSelectionCount() == 0) {
			return;
		}
		final TreeItem item = tocItems.getSelection()[0];

		currSelection = (TocItem) item.getData();
		// Create a text field to do the editing
		final Text text = new Text(tocItems, SWT.BORDER);
		String title = item.getText();
		String[] parts = title.split("\\["); //$NON-NLS-1$
		String myTitle = parts[0].trim();
		text.setText(myTitle);
		text.selectAll();
		text.setFocus();

		// If the text field loses focus, set its text into the tree
		// and end the editing session
		text.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent event) {
				// ((TocItem)item.getData()).setTitle(text.getText());
				try {
					ChangeTitleOperation operation = new ChangeTitleOperation(
							(TocItem) item.getData(), text.getText());
					OperationHistory.execute(operation);
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				text.dispose();
				drawTree();
			}
		});

		// If they hit Enter, set the text into the tree and end the editing
		// session. If they hit Escape, ignore the text and end the editing
		// session
		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				switch (event.keyCode) {
				case SWT.CR:
					// Enter hit--set the text into the model and redraw the
					// tree
					try {
						ChangeTitleOperation operation = new ChangeTitleOperation(
								(TocItem) item.getData(), text.getText());
						OperationHistory.execute(operation);
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					drawTree();
				case SWT.ESC:
					// End editing session
					text.dispose();
					break;
				}
			}
		});

		// Set the text field into the editor
		editor.setEditor(text, item);
	}

	private void initDropTarget() {
		final DropTarget target = new DropTarget(tocItems, SWT.None);
		Transfer[] supportedTransferTypes = new Transfer[] {
				TransferScene.getInstance(), TransferMedia.getInstance() };
		target.setTransfer(supportedTransferTypes);
		
		target.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) {
				if (event.widget instanceof DropTarget) {
					String transferKey = event.data.toString();
					Object[] objects = Application.getDragDropManager()
							.endTransfer(transferKey);
					if (objects.length > 0) {
						if (objects[0] instanceof NodeScene) {													
							TreeItem i = (TreeItem)event.item;
							
							if (i != null) {
								TocItem item = (TocItem) i.getData();
								currSelection = item;
								ChangeSceneOperation operation = new ChangeSceneOperation(
										item, (NodeScene) objects[0]);
								try {
									OperationHistory.execute(operation);
								} catch (ExecutionException e) {
									operation.getErrorMessage(e);
								}
								drawTree();
								selectItem(tocItems.getItems(), item);
								tocItems.showSelection();
							}
						} else if (objects[0] instanceof TreeItem) {													
							TreeItem target = (TreeItem)event.item;
							TreeItem source = (TreeItem) objects[0];
							
							
							if (target != source) {
								TocItem targetItem ;
								if(target != null){
									targetItem = (TocItem) target.getData();
								}else{
									targetItem = root;
								}
								List<TocItem> targetChilds = targetItem.getChildren();	
								targetChilds = new ArrayList<TocItem>(targetChilds);
								TocItem sourceItem = (TocItem) source.getData();
								targetChilds.add(sourceItem);
								

								TreeItem parent = source.getParentItem(); 
								TocItem parentToc;
								if(parent != null){
									parentToc = (TocItem)source.getParentItem().getData();
								}else{
									parentToc = root;
								}
									
								List<TocItem> sourceChilds = parentToc.getChildren();
								sourceChilds = new ArrayList<TocItem>(sourceChilds);
								sourceChilds.remove(sourceItem);
							
								/*ChangeTocItemOperation operationDelete 
										= new ChangeTocItemOperation(parentToc, sourceChilds);
											
								ChangeTocItemOperation operation= new ChangeTocItemOperation(
										targetItem,targetChilds );
								try {
									OperationHistory.execute(operation);
									OperationHistory.execute(operationDelete);
								} catch (ExecutionException e) {
									operation.getErrorMessage(e);
									operationDelete.getErrorMessage(e);
								}*/
								MoveTocItemOperation operation = 
										new MoveTocItemOperation(parentToc, sourceChilds, targetItem, targetChilds);
								try {
									OperationHistory.execute(operation);
								} catch (ExecutionException e) {
									operation.getErrorMessage(e);
								}
								
								drawTree();
							}
						}
					}
				}
			};
		});
	}
	
	protected void drawSceneList() {
		sceneList.removeAll();
		nodes = Application.getCurrentProject().getSceneGraph().getNodes();
		List<NodeScene> scenes = new ArrayList<NodeScene>();
		for (IGraphNode n : nodes) {
			if (n instanceof NodeScene) {
				scenes.add((NodeScene)n);
			}
		}
		// sort lexicographically
		Collections.sort(scenes, new Comparator<NodeScene>() {
			@Override
			public int compare(NodeScene o1, NodeScene o2) {
				return o1.getTitle().compareTo(o2.getTitle());
			}
			
		});
		for (NodeScene scene : scenes) {
			TreeItem item = new TreeItem(sceneList, SWT.None);
			item.setText(scene.getTitle() + Messages.TreePanel_SceneId_1 + scene.getNodeID() + Messages.TreePanel_SceneId_2); // Szenentitel
			item.setData(scene);
		}
	}
	
	private void initDragSupport() {
		Transfer[] transferTypes = new Transfer[]{TransferScene.getInstance(), TransferMedia.getInstance()};

		// TreeViewer als Drag-Quelle definieren
		final DragSource source = new DragSource(sceneList,
				DND.DROP_COPY | DND.DROP_MOVE);

		// Transfer-Typ fuer DND setzen
		source.setTransfer(transferTypes);

		// Den Listener fuer Drag-Events einrichten:
		// Dieser Stellt die Daten fuer das Drag-and-Drop zur Verfuegung
		source.addDragListener(new DragSourceAdapter() {

			@Override
			public void dragStart(DragSourceEvent event) {
				super.dragStart(event);			
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				TreeItem[] selItems = sceneList.getSelection();
				IAbstractBean[] transferObjects = new IAbstractBean[]{((NodeScene)selItems[0].getData())};
				String transferKey = Application.getDragDropManager()
						.startTransfer(transferObjects);
				event.data = transferKey;
			}
		});
	}
	
	private void initDragSupportforTOC() {
		Transfer[] transferTypes = new Transfer[]{TransferMedia.getInstance()};
				
		// TreeViewer als Drag-Quelle definieren
		final DragSource source = new DragSource(tocItems, DND.DROP_MOVE);

		// Transfer-Typ fuer DND setzen
		source.setTransfer(transferTypes);

		// Den Listener fuer Drag-Events einrichten:
		// Dieser Stellt die Daten fuer das Drag-and-Drop zur Verfuegung
		source.addDragListener(new DragSourceAdapter() {

			@Override
			public void dragStart(DragSourceEvent event) {
				super.dragStart(event);			
			}

			@Override
			public void dragSetData(DragSourceEvent event) {//TODO
				TreeItem[] selItems = tocItems.getSelection(); 
				Object[] transferObjects = new Object[]{selItems[0]};
				String transferKey = Application.getDragDropManager()
						.startTransfer(transferObjects);
				event.data = transferKey;
			}
		});
	}
}
