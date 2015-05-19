package org.iviPro.views.scenerepository;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.iviPro.theme.Icons;

/**
 * Diese Klasse wird benötigt, um die Anzeige des Treeviewers für die View
 * org.iviPro.views.SceneRepository zu ermöglichen.
 * 
 * @author Florian Stegmaier
 */
public class SceneAdapterFactory implements IAdapterFactory {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SceneAdapterFactory.class);
	// TODO noch auslagern...
	// Ausgabe für die View
	private static final String IN_TREE_COMMENT = Messages.SceneAdapterFactory_ScenesUsed;
	private static final String NOT_IN_TREE_COMMENT = Messages.SceneAdapterFactory_ScenesNotUsed;

	public SceneAdapterFactory() {
	}

	/**
	 * Definiert die Anzeige der Elemente vom Typ
	 * org.iviPro.internObjects.SceneGroup
	 */
	private IWorkbenchAdapter sceneGroupAdapter = new IWorkbenchAdapter() {

		@Override
		public Object getParent(Object o) {
			return ((SceneTreeGroup) o).getParent();
		}

		@Override
		public String getLabel(Object o) {
			if (((SceneTreeGroup) o).getName()
					.contentEquals(SceneManager.IN_TREE)) {
				return IN_TREE_COMMENT;
			} else {
				return NOT_IN_TREE_COMMENT;
			}
		}

		@Override
		public ImageDescriptor getImageDescriptor(Object o) {
			if (((SceneTreeGroup) o).getName()
					.contentEquals(SceneManager.IN_TREE)) {
				return Icons.VIEW_SCENEREPOSITORY_GROUP_USED
						.getImageDescriptor();
			} else {
				return Icons.VIEW_SCENEREPOSITORY_GROUP_UNUSED
						.getImageDescriptor();
			}
		}

		@Override
		public Object[] getChildren(Object o) {
			return ((SceneTreeGroup) o).getEntries().toArray();
		}
	};

	/**
	 * Definiert die Anzeige der Elemente vom Typ
	 * org.iviPro.internObjects.SceneLeaf
	 */
	private IWorkbenchAdapter sceneAdapter = new IWorkbenchAdapter() {

		@Override
		public Object[] getChildren(Object o) {
			return new Object[0];
		}

		@Override
		public ImageDescriptor getImageDescriptor(Object object) {
			return Icons.VIEW_SCENEREPOSITORY_ITEM_SCENE.getImageDescriptor();
		}

		@Override
		public String getLabel(Object o) {
			return ((SceneTreeLeaf) o).getName();
		}

		@Override
		public Object getParent(Object o) {
			return ((SceneTreeLeaf) o).getParent();
		}

	};

	/**
	 * Holt den entsprechenden Adapter.
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IWorkbenchAdapter.class
				&& adaptableObject instanceof SceneTreeGroup) {
			return sceneGroupAdapter;
		}

		if (adapterType == IWorkbenchAdapter.class
				&& adaptableObject instanceof SceneTreeLeaf) {
			return sceneAdapter;
		}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class };
	}

}
