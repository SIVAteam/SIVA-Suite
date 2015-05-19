package org.iviPro.views;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;

/**
 * Abstrakte Basisklasse fuer die verschiedenen Repository-Views. Kapselt
 * gemeinsame Arbeitsschritte.
 * 
 * @author dellwo
 * 
 */
public abstract class IAbstractRepositoryView extends IAbstractView {

	private static Logger logger = Logger
			.getLogger(IAbstractRepositoryView.class);

	protected TreeViewer treeViewer;
	private Text searchField;
	private IAbstractRepositoryFilter treeViewerFilter;
	private Object treeRoot;
	
	// der Manager der Toolbar, zum Hinzufügen der Actions
	protected ToolBarManager toolBarManager;

	/**
	 * Erstellt den Repository-View.
	 */
	public IAbstractRepositoryView() {
		// Listener initalisieren
		Application.getDefault().addApplicationListener(
				new ApplicationListener() {

					@Override
					public void onProjectOpened(Project project) {						
						IAbstractRepositoryView.this.onProjectOpened(project);
						updateTreeviewer();
						treeViewer.expandAll();
					}

					@Override
					public void onProjectClosed(Project project) {
						IAbstractRepositoryView.this.onProjectClosed(project);
						treeRoot = initTreeRoot();						
						updateTreeviewer();
					}
				});
	}

	@Override
	public void createPartControlImpl(Composite parent) {
		// Registriere Adapter fuer Darstellung der Tree-items.
		registerTreeItemsAdapter();

		// Layout bestimmen
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		parent.setLayout(layout);

		// Control fuer Suchfeld erstellen
		Composite searchCtrl = createSearchControl(parent);
		searchCtrl.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		// die Coolbar mit den Actions
		ToolBar toolbar = new ToolBar(parent, SWT.FLAT | SWT.RIGHT);
		toolBarManager = new ToolBarManager(toolbar);

		// Root-Treenode anlegen, falls nicht existent
		logger.debug("Builiding media repository view..."); //$NON-NLS-1$
		if (treeRoot == null) {
			treeRoot = initTreeRoot();
		}

		// Treeviewer anlegen
		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI
				| SWT.V_SCROLL);
		treeViewer.setContentProvider(new BaseWorkbenchContentProvider());
		treeViewer.setLabelProvider(new WorkbenchLabelProvider());
		getSite().setSelectionProvider(treeViewer);
		treeViewerFilter = createRepositoryFilter();
		treeViewer.addFilter(treeViewerFilter);
		treeViewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		treeViewer.getTree().addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event) {
				onDoubleClick(event);
			}
		});
		
		treeViewer.getTree().addListener(SWT.MouseMove, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				handleMouseMove(arg0);
			}			
		});
		
		treeViewer.getTree().addListener(SWT.MouseExit, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				handleMouseExit(arg0);
			}
			
		});

		// CellEditor zum Editieren des Namens
		CellEditor[] cellEditors = new CellEditor[1];
		cellEditors[0] = new TextCellEditor(treeViewer.getTree(), SWT.BORDER);
		treeViewer.setCellEditors(cellEditors);
		treeViewer.setColumnProperties(new String[] { "name" }); //$NON-NLS-1$
	
		// Drag-n-Drop Support initialisieren
		initDropSupport(treeViewer);
		initDragSupport(treeViewer);

		// Kontext-Menu fuer Treeview erstellen
		MenuManager menuManager = createContextMenu(treeViewer);
		treeViewer.getTree().setMenu(
				menuManager.createContextMenu(treeViewer.getTree()));
		
		// Die Daten des aktuell geoeffneten Projekts darstellen
		onProjectOpened(Application.getCurrentProject());
		updateTreeviewer();
	}

	/**
	 * Erstellt das Control mit dem Suchfeld
	 * 
	 * @param parent
	 *            Parent-Control auf dem das Suchfeld eingefuegt werden soll
	 * @return Das Composite auf dem das Suchfeld liegt.
	 */
	protected Composite createSearchControl(Composite parent) {
		// Create the search panel with the search controls
		final Composite searchPane = new Composite(parent, SWT.NONE);
		searchPane.setLayout(new GridLayout(3, false));
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
		label.setText(Messages.IAbstractRepositoryView_LabelSearch);
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

	protected abstract MenuManager createContextMenu(TreeViewer treeViewer);

	@Override
	protected abstract Image getDefaultImage();

	@Override
	public void setFocus() {
		searchField.setFocus();
	}
	
	protected abstract void handleMouseMove(Event arg0);
	protected abstract void handleMouseExit(Event arg0);
	
	/**
	 * Updated den Tree-Viewer. Sollte aufgerufen werden, wenn der
	 * changeListener Aenderungen an den Media-Objekten detektiert hat.
	 */
	public void updateTreeviewer() {
		if (treeViewer == null || treeViewer.getTree().isDisposed()) {
			return;
		}
		Object[] expanded = treeViewer.getExpandedElements();
		treeViewer.setInput(treeRoot);		
		treeViewer.setExpandedElements(expanded);
		
	}

	/**
	 * Registriert den Adapter fuer den Tree-View, der zu gegebenen Objekten
	 * ihre Repraesentation im TreeViewer liefern kann, z.B. Text und Icon fuer
	 * ein Items im Tree.
	 */
	protected abstract void registerTreeItemsAdapter();

	/**
	 * Gibt den Filter zurueck, der fuer das Reposiotry genutzt werden soll.
	 * Dieser Filter bekommt dann den im Suchfeld eingegebenen Text uebergeben
	 * und soll dann die entsprechenden Eintraege herausfiltern, die nicht zum
	 * Suchtext passen.
	 * 
	 * @return
	 */
	protected abstract IAbstractRepositoryFilter createRepositoryFilter();

	/**
	 * Wird beim Doppelklick auf ein Element im Treeview aufgerufen.
	 * 
	 * @param event
	 *            Der Mouse-ClickEvent
	 */
	protected abstract void onDoubleClick(Object selectedElement);

	/**
	 * Wird aufgerufen wenn ein Objekt auf den Treeview gezogen wird, z.b. eine
	 * Datei aus dem Explorer.
	 * 
	 * @param event
	 *            Der Drop-Event.
	 */
	protected abstract void onDrop(DropTargetEvent event);

	/**
	 * Wird aufgerufen, wenn ein Projekt geoeffnet wird. Der Repository-View
	 * sollte dann seine Listener auf dem Projekt installieren und das
	 * Repository mit den Daten des Projekts befuellen. Der Tree wird danach
	 * automatisch aktualisiert.
	 * 
	 * @param project
	 *            Das Projekt das geoeffnet wurde.
	 */
	protected abstract void onProjectOpened(Project project);

	/**
	 * Wird aufgerufen, wenn das aktuelle Projekt geschlossen wird. Der
	 * Repository-View sollte dann seine Listener vom Projekt entfernen. Der
	 * Tree wird danach automatisch zurueck gesetzt.
	 * 
	 * @param project
	 *            Das Projekt das geschlossen wurde.
	 */
	protected abstract void onProjectClosed(Project project);

	/**
	 * Gibt das Root-Objekt des Tree-Viewers zurueck
	 * 
	 * @return Root-Objekt des Tree-Viewers.
	 */
	protected abstract Object initTreeRoot();

	/**
	 * Gibt die Transfer-Typen zurueck, die vom Repository fuer Drag-Aktionen
	 * unterstuetzt werden, d.h. Transfers aus dem Repository heraus werden
	 * diese Typen besitzen.
	 * 
	 * @return Unterstuetzte ausgehende Transfer-Typen fuer Drag-Aktionen oder
	 *         null, falls ausgehenden Drag-Aktionen nicht unterstuetzt werden.
	 */
	protected abstract Transfer[] getDragTransferTypes();

	/**
	 * Gibt die Transfer-Typen zurueck, die vom Repository fuer Drop-Aktionen
	 * unterstuetzt werden, d.h. Transfers dieser Typen koennen auf das
	 * Repository gezogen werden.
	 * 
	 * @return Unterstuetzte eingehende Transfer-Typen fuer Drop-Aktionen oder
	 *         null, falls eingehende Drop-Aktionen nicht unterstuetzt werden.
	 */
	protected abstract Transfer[] getDropTransferTypes();

	/**
	 * Ermittelt, welche Medien-Objekte gerade markiert wurden und die deshalb
	 * per DnD uebertragen werden sollen.
	 * 
	 * @return Liste von Medien-Objekten, die in den DnD-Event uebertragen
	 *         werden sollen.
	 */
	protected abstract IAbstractBean[] getObjectsToTransfer(TreeItem[] selItems);

	/**
	 * Gibt an, ob das angegebene Element per DND gedraggt werden darf, also aus
	 * dem Treeview heraus gezogen werden darf.
	 * 
	 * @param selItem
	 *            Das im Tree selektierte Item.
	 * @return True, wenn Drag des Objektes erlaubt ist, false ansonsten.
	 */
	protected abstract boolean isDragAllowed(Object selItem);

	/**
	 * Gibt den Tree-Viewer zurueck.
	 * 
	 * @return Der Tree-Viewer.
	 */
	protected TreeViewer getTreeViewer() {
		return treeViewer;
	}

	/**
	 * Gibt das aktuelle Wurzel-Element des Trees zurueck.
	 * 
	 * @return Aktuelles Wurzel-Element des Trees.
	 */
	protected Object getTreeRoot() {
		return treeRoot;
	}

	/**
	 * Wird beim Doppelklick auf ein Element im Treeview aufgerufen.
	 * 
	 * @param event
	 *            Der Mouse-ClickEvent
	 */
	private void onDoubleClick(Event event) {
		ISelection selection = treeViewer.getSelection();
		if (selection instanceof ITreeSelection) {
			ITreeSelection treeSelection = (ITreeSelection) selection;			
			Object selectedElement = treeSelection.getFirstElement();
			onDoubleClick(selectedElement);
		}
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
		updateTreeviewer();
	}

	
	/**
	 * Initialisiert den Drag-n-Drop Drag-Support auf dem Treeviewer, d.h. die
	 * Unterstuetzung des Treeviews um z.B. Medien aus dem Media-Repository in
	 * den Szenen-Graph ziehen zu koennen.
	 * 
	 * @param treeViewer
	 */
	private void initDragSupport(final TreeViewer treeViewer) {

		Transfer[] transferTypes = getDragTransferTypes();
		if (transferTypes == null) {
			// Wenn Transfer-Typ null ist, dann ist Drop nicht unterstuetzt und
			// wir beenden deshalb hier.
			return;
		}

		// TreeViewer als Drag-Quelle definieren
		final DragSource source = new DragSource(treeViewer.getTree(),
				DND.DROP_COPY | DND.DROP_MOVE);
		// TODO Passt das, oder doch lieber DND.DROP_COPY | DND.DROP_MOVE?

		// Transfer-Typ fuer DND setzen
		source.setTransfer(transferTypes);

		// Den Listener fuer Drag-Events einrichten:
		// Dieser Stellt die Daten fuer das Drag-and-Drop zur Verfuegung
		source.addDragListener(new DragSourceAdapter() {

			@Override
			public void dragStart(DragSourceEvent event) {
				super.dragStart(event);
				// Hier erlauben wir, ob die markierten Objekte ueberhaupt
				// per DND transferiert werden duerfen. Damit verhindern wir,
				// dass z.B. die Kategorie-Knoten im Baum tranferiert werden
				// koennen, da dies nur mit den nur die MedienObjekt-Knoten
				// moeglich sein sollte.
				TreeItem[] selItems = treeViewer.getTree().getSelection();
				boolean containsUnallowedNodes = false;
				for (int i = 0; i < selItems.length; i++) {
					Object selItem = selItems[i].getData();
					if (!isDragAllowed(selItem)) {
						containsUnallowedNodes = true;
						break;
					}
				}
				if (containsUnallowedNodes) {
					event.doit = false;
				}
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				TreeItem[] selItems = treeViewer.getTree().getSelection();
				IAbstractBean[] transferObjects = getObjectsToTransfer(selItems);
				String transferKey = Application.getDragDropManager()
						.startTransfer(transferObjects);
				event.data = transferKey;
			}
		});
	}

	/**
	 * Initialisiert den Drag-and-Drop Drop-Support fuer den Treeviewer um z.B.
	 * Dateien hinein ziehen zu koennen aus dem Explorer um sie dem
	 * Media-Repository hinzuzufuegen.
	 * 
	 * @param treeViewer
	 *            Der TreeViewer.
	 */
	private void initDropSupport(TreeViewer treeViewer) {
		// Treeviewer als Drop-Target registrien und unterstuetzten
		// Transfer-Type setzen
		Transfer[] transferTypes = getDropTransferTypes();
		if (transferTypes == null) {
			// Wenn Transfer-Typ null ist, dann ist Drop nicht unterstuetzt und
			// wir beenden deshalb hier.
			return;
		}
		DropTarget dt = new DropTarget(treeViewer.getTree(), DND.DROP_MOVE);
		dt.setTransfer(transferTypes);

		// Registriere Drop-Listener fuer das behandeln eingehender Daten per
		// Drag-and-Drop
		dt.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) {
				onDrop(event);
			}
		});

	}

}
