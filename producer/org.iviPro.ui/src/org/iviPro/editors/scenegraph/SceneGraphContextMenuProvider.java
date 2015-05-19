package org.iviPro.editors.scenegraph;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.ActionFactory;
import org.iviPro.editors.scenegraph.actions.NodeEditAction;
import org.iviPro.editors.scenegraph.actions.SemanticZoomInAction;
import org.iviPro.editors.scenegraph.actions.SemanticZoomOutAction;

/**
 * Stellt das Kontext-Menue fuer den Szenen-Graphen zur Verfuegung.
 * 
 * @author dellwo
 */
class SceneGraphContextMenuProvider extends ContextMenuProvider {

	/** Die Action-Registry des Editors. */
	private ActionRegistry actionRegistry;

	/**
	 * Instantiiert einen neuen ContextMenu Provider fuer den Szenen-Graph.
	 * 
	 * @param viewer
	 *            Der GraphicalViewer von GEF des Editors.
	 * @param actionRegistry
	 *            Die Action-Registry des Editors.
	 * @throws IllegalArgumentException
	 *             Falls einer der Parameter null sein sollte.
	 */
	public SceneGraphContextMenuProvider(EditPartViewer viewer,
			ActionRegistry actionRegistry) {
		super(viewer);
		if (actionRegistry == null) {
			throw new IllegalArgumentException();
		}
		this.actionRegistry = actionRegistry;
	}

	/**
	 * Wird aufgerufen, wenn das Kontextmenue angezeigt werden soll. Diese
	 * Methode stellt dann das Menue zusammen.
	 * 
	 * @see org.eclipse.gef.ContextMenuProvider#buildContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void buildContextMenu(IMenuManager menu) {

		String group;

		menu.add(new Separator(GEFActionConstants.GROUP_UNDO));
		menu.add(new Separator(GEFActionConstants.GROUP_COPY));
		menu.add(new Separator(GEFActionConstants.GROUP_EDIT));
		menu.add(new Separator(GEFActionConstants.GROUP_VIEW));
		menu.add(new Separator(GEFActionConstants.GROUP_REORGANIZE));
		menu.add(new Separator(GEFActionConstants.GROUP_PRINT));

		// GROUP UNDO
		group = GEFActionConstants.GROUP_UNDO;
		addActionToGroup(menu, group, ActionFactory.UNDO.getId(), false);
		addActionToGroup(menu, group, ActionFactory.REDO.getId(), false);

		// GROUP EDIT
		group = GEFActionConstants.GROUP_EDIT;
		addActionToGroup(menu, group, ActionFactory.DELETE.getId());
		addActionToGroup(menu, group, NodeEditAction.ID);

		// GROUP VIEW
		group = GEFActionConstants.GROUP_VIEW;
		addActionToGroup(menu, group, GEFActionConstants.ZOOM_IN);
		addActionToGroup(menu, group, GEFActionConstants.ZOOM_OUT);
		addActionToGroup(menu, group, SemanticZoomInAction.ID);
		addActionToGroup(menu, group, SemanticZoomOutAction.ID);

		// GROUP REORGANIZE
		group = GEFActionConstants.GROUP_REORGANIZE;
		addActionToGroup(menu, group, GEFActionConstants.ALIGN_LEFT);
		addActionToGroup(menu, group, GEFActionConstants.ALIGN_RIGHT);
		addActionToGroup(menu, group, GEFActionConstants.ALIGN_TOP);
		addActionToGroup(menu, group, GEFActionConstants.ALIGN_BOTTOM);
		addActionToGroup(menu, group, GEFActionConstants.ALIGN_CENTER);
		addActionToGroup(menu, group, GEFActionConstants.ALIGN_MIDDLE);

		// GROUP PRINT
		group = GEFActionConstants.GROUP_PRINT;
		addActionToGroup(menu, group, ActionFactory.PRINT.getId());

	}

	/**
	 * Fuegt eine Action in eine Menue-Gruppe ein, falls die Action enabled ist.
	 * 
	 * @param menu
	 *            Das Menu.
	 * @param group
	 *            Die ID der Menue-Gruppe.
	 * @param actionID
	 *            Die ID der Action in der Action-Registry.
	 */
	private void addActionToGroup(IMenuManager menu, String group,
			String actionID) {
		IAction action = actionRegistry.getAction(actionID);
		if (action.isEnabled()) {
			menu.appendToGroup(group, action);
		}
	}

	/**
	 * Fuegt eine Action in eine Menue-Gruppe ein.
	 * 
	 * @param menu
	 *            Das Menu.
	 * @param group
	 *            Die ID der Menue-Gruppe.
	 * @param actionID
	 *            Die ID der Action in der Action-Registry.
	 * @param showOnlyWhenEnabled
	 *            Wenn true, wird die Action im Menue nur angezeigt, wenn sie
	 *            auch enabled ist.
	 */
	private void addActionToGroup(IMenuManager menu, String group,
			String actionID, boolean showOnlyWhenEnabled) {
		IAction action = actionRegistry.getAction(actionID);
		if (!showOnlyWhenEnabled || action.isEnabled()) {
			menu.appendToGroup(group, action);
		}
	}

}
