package org.iviPro.views.scenerepository;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.model.BeanList;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;

/**
 * Ein Container, in dem alle Szenenobjekte eingeordnet werden. Dabei wird
 * unterschieden, ob die Szenen im Szenengraph verwendet sind oder nicht.
 */
public class SceneManager implements ApplicationListener,
		PropertyChangeListener {
	private static Logger logger = Logger.getLogger(SceneManager.class);
	private SceneTreeGroup root;

	public static final String IN_TREE = "true"; //$NON-NLS-1$
	public static final String NOT_IN_TREE = "false"; //$NON-NLS-1$	
	private static final String ROOT_NAME = "root"; //$NON-NLS-1$

	private SceneManagerListener listener = null;

	public SceneManagerListener getSceneContainerListenerListener() {
		return listener;
	}

	public void setSceneManagerListener(SceneManagerListener listener) {
		this.listener = listener;
	}

	private void fireChangeEvent() {
		if (listener != null) {
			listener.notifySceneContainerChanged();
		}
	}

	/**
	 * Die Szenen-Gruppe die die Szenen enthaelt, die irgendwo im Graph
	 * verwendet werden.
	 */
	private SceneTreeGroup groupUsed;

	/**
	 * Die Szenen-Gruppe die die Szenen enthaelt, die nirgends im Graph
	 * verwendet werden.
	 */
	private SceneTreeGroup groupUnused;

	/**
	 * Erstellt einen neuen Scene-Container der bereits zwei Szenen-Gruppen
	 * besitzt fuer Szenen die im Graph sind (SceneTypes.IN_TREE) und solche die
	 * nicht im Graph (SceneTypes.NOT_IN_TREE) sind.
	 */
	public SceneManager() {
		Application app = Application.getDefault();
		logger.debug("Registered as ApplicationListener."); //$NON-NLS-1$

		// Szenen-Ordner erstellen
		root = new SceneTreeGroup(null, ROOT_NAME);
		groupUsed = new SceneTreeGroup(root, IN_TREE);
		groupUnused = new SceneTreeGroup(root, NOT_IN_TREE);
		root.addEntries(groupUsed);
		root.addEntries(groupUnused);

		init(Application.getCurrentProject());
		app.addApplicationListener(this);
	}

	/**
	 * Initialisiert den Szenen-Container, d.h. versetzt
	 */
	private void init(Project project) {
		logger.debug("Initialzing container for project: " + project); //$NON-NLS-1$

		// Bestehende Szenen hier eintragen, falls schon ein Projekt offen
		if (project != null) {
			project.getMediaObjects().addPropertyChangeListener(this);
			logger.debug("Registered as listener of: " + project); //$NON-NLS-1$
			project.getSceneGraph().addPropertyChangeListener(
					Graph.PROP_NODE_REMOVED, new PropertyChangeListener() {

						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if (evt.getOldValue() instanceof NodeScene) {
								removeUnused((NodeScene) evt.getOldValue());
							}

						}
					});
			project.getSceneGraph().addPropertyChangeListener(
					Graph.PROP_NODE_ADDED, new PropertyChangeListener() {

						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if (evt.getNewValue() instanceof NodeScene) {
								NodeScene node = (NodeScene) evt.getNewValue();
								updateTreeStatus(node.getScene(), true);
							}

						}

					});
			HashSet<Scene> unused = new HashSet<Scene>();
			Iterator<IAbstractBean> it = project.getMediaObjects().iterator();
			while (it.hasNext()) {
				IAbstractBean mediaObj = it.next();
				if (mediaObj instanceof Video) {
					Video video = (Video) mediaObj;
					video.getScenes().addPropertyChangeListener(this);
					logger.debug("Registered as listener of: " + video); //$NON-NLS-1$
					unused.addAll(video.getScenes());
				}
			}
			HashSet<Scene> used = new HashSet<Scene>();
			Collection<IGraphNode> sceneNodes = project.getSceneGraph()
					.searchNodes(NodeScene.class, true);
			Iterator<IGraphNode> nodeIt = sceneNodes.iterator();
			while (nodeIt.hasNext()) {
				NodeScene sceneNode = (NodeScene) nodeIt.next();				
				Scene scene = sceneNode.getScene();
				unused.remove(scene);
				used.add(scene);
			}
			addAll(unused, groupUnused);
			addAll(used, groupUsed);
		} else {
			groupUsed.getEntries().clear();
			groupUnused.getEntries().clear();
		}
	}

	// TODO: Beim Umstellen auf GEF-Graph entfernen
	// Ist nur da, um zu wissen, ob ein Knoten im Graph schon geloescht wurde,
	// da beim Loeschen des Zest-Knotens der Model-Knoten ja nicht mit geloescht
	// wird...
	private HashSet<NodeScene> alreadyDeletedNodes = new HashSet<NodeScene>();

	/**
	 * Entfernt die Szene der angegebenen SceneFigure aus der Liste der benutzen
	 * Szenen, falls diese nicht mehr im Graphen benutzt wird.
	 * 
	 * @param selectedFigure
	 */
	private void removeUnused(NodeScene deletedNode) {
		// Wir schauen ob die Szene noch im Graphen
		// vorkommt. Wenn sie nun
		// nicht mehr vorkommt, dann entfernen wir
		// sie aus der Gruppe der benutzen Szenen.
		alreadyDeletedNodes.add(deletedNode);
		Scene scene = deletedNode.getScene();

		Project project = Application.getCurrentProject();
		boolean isUsed = false;
		if (project != null) {
			Collection<IGraphNode> sceneNodes = project.getSceneGraph()
					.searchNodes(NodeScene.class, true);
			for (IGraphNode node : sceneNodes) {
				NodeScene sceneNode = (NodeScene) node;
				if (sceneNode.getScene() == scene
						&& !alreadyDeletedNodes.contains(sceneNode)) {
					// Noch nicht geloeschter Szenen-Knoten referenziert Szene
					// ==> Szene wird noch benutzt
					isUsed = true;
					break;
				}
			}
		}
		if (!isUsed) {
			logger.debug("Scene '" + scene + "' is unused in graph. " //$NON-NLS-1$ //$NON-NLS-2$
					+ "Updating scene respository"); //$NON-NLS-1$
			updateTreeStatus(scene, false);
		}

	}

	/**
	 * Fuegt eine neue Szene in diesen Container ein.
	 * 
	 * @param newScene
	 *            Die neue Szene
	 */
	private void add(Scene newScene, SceneTreeGroup group) {
		ArrayList<Scene> tmp = new ArrayList<Scene>();
		tmp.add(newScene);
		addAll(tmp, group);
	}

	/**
	 * Fuegt eine Menge von Szenen in diesen Container ein.
	 * 
	 * @param newScenes
	 *            Die Szenen die hinzufuegt werden sollen.
	 */
	private void addAll(Collection<Scene> newScenes, SceneTreeGroup group) {
		// Keine der Szenen gibt es also bereits und in der Liste sind auch
		// keine doppelten enthalten. Wir koennen die Szenen also hinzufuegen
		for (Scene newScene : newScenes) {
			newScene.addPropertyChangeListener(this);
			logger.info("Added scene : " + newScene); //$NON-NLS-1$
			SceneTreeLeaf leaf = new SceneTreeLeaf(group, newScene);
			group.addEntries(leaf);
			// setze die neue Szene als selektierte
			SceneRepository sceneRepository = (SceneRepository) Application
					.getDefault().getView(SceneRepository.ID);
			if (sceneRepository != null) {
				((SceneRepository) sceneRepository).selectScene(newScene);
			}
		}
		fireChangeEvent();
	}

	/**
	 * Entfernt eine Szene aus diesem Container
	 * 
	 * @param scene
	 *            Die Szene die entfernt werden soll.
	 * @return Gibt true zurueck, wenn die Szene im Container existierte und
	 *         daher entfernt wurde. Gibt false zurueck, wenn die Szene gar
	 *         nicht im Container enthalten war.
	 */
	private boolean remove(Scene scene) {
		List<Scene> tmp = new ArrayList<Scene>(1);
		tmp.add(scene);
		return removeAll(tmp);
	}

	/**
	 * Entfernt die angegebenen Szenen aus diesem Container.
	 * 
	 * @param scenes
	 *            Die Szenen die entfernt werden sollen.
	 * @return Gibt true zurueck, wenn mindestens eine der Szenen in diesem
	 *         Container enthalten war und daher entfernt wurde.
	 */
	private boolean removeAll(List<Scene> scenes) {
		boolean removed = false;
		for (Scene scene : scenes) {
			SceneTreeLeaf leaf = findLeaf(scene);
			scene.removePropertyChangeListener(this);
			logger.debug("Removed scene : " + scene); //$NON-NLS-1$
			removed = removed | groupUnused.removeEntry(leaf);
			removed = removed | groupUsed.removeEntry(leaf);
		}

		if (removed) {
			fireChangeEvent();
		}
		return removed;
	}

	/**
	 * Finds the SceneLeaf associated with a given scene.
	 * 
	 * @param scene
	 *            The given scene.
	 * @return The SceneLeaf object for the scene or null, if no leaf for this
	 *         scene exists in the scene-container.
	 */
	public SceneTreeLeaf findLeaf(Scene scene) {
		Queue<Object> workQueue = new LinkedList<Object>();
		workQueue.addAll(getRoot().getEntries());
		while (workQueue.size() > 0) {
			Object item = workQueue.poll();
			if (item instanceof SceneTreeGroup) {
				SceneTreeGroup group = (SceneTreeGroup) item;
				workQueue.addAll(group.getEntries());
			} else if (item instanceof SceneTreeLeaf) {
				SceneTreeLeaf leaf = (SceneTreeLeaf) item;
				if (leaf.getScene().equals(scene)) {
					return leaf;
				}
			}
		}
		logger.debug("No SceneLeaf found for scene: " + scene.getTitle()); //$NON-NLS-1$
		return null;
	}

	/**
	 * Liefert das Rootelement
	 * 
	 * @return
	 */
	public SceneTreeGroup getRoot() {
		return root;
	}

	/**
	 * Liefert den Namen des Rootelement
	 * 
	 * @return
	 */
	public static String getRootName() {
		return ROOT_NAME;
	}

	@Override
	public void onProjectClosed(Project project) {
		project.removePropertyChangeListener(this);
		logger.debug("Removed as listener of project: " + project); //$NON-NLS-1$
		init(null);
		fireChangeEvent();
	}

	@Override
	public void onProjectOpened(Project project) {
		logger.debug("Added as listener of project: " + project); //$NON-NLS-1$
		init(project);
		fireChangeEvent();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object source = evt.getSource();
		String property = evt.getPropertyName();
		Object newValue = evt.getNewValue();
		if (source instanceof BeanList && newValue instanceof Video) {
			if (property.equals(BeanList.PROP_ITEM_ADDED)) {
				// Ein Video wurde dem Projekt hinzugefuegt
				// ==> Als Listener beim Video eintragen, um mitzukriegen, wenn
				// neue Szenen hinzugefuegt werden.
				Video video = ((Video) newValue);
				video.getScenes().addPropertyChangeListener(this);
				addAll(video.getScenes(), groupUnused);
				logger.debug("Registered as listener at video: " + newValue); //$NON-NLS-1$

			} else if (property.equals(BeanList.PROP_ITEM_REMOVED)) {
				// Ein Video wurde dem Projekt hinzugefuegt
				// ==> Als Listener beim Video wieder austragen
				// Alle Szenen des Videos entfernen
				Video video = (Video) newValue;
				video.getScenes().removePropertyChangeListener(this);
				logger.debug("Unregistered as listener at video: " + newValue); //$NON-NLS-1$
				removeAll(video.getScenes());
			}

		} else if (source instanceof BeanList && newValue instanceof Scene) {

			if (property.equals(BeanList.PROP_ITEM_ADDED)) {
				// Eine Szene wurde dem Video hinzugefuegt
				// ==> Szene in Container aufnehmen
				add((Scene) newValue, groupUnused);

			} else if (property.equals(BeanList.PROP_ITEM_REMOVED)) {
				// Eine Szene wurde vom Video entfernt Video wurde dem Projekt
				// hinzugefuegt
				// ==> Als Listener beim Video wieder austragen
				remove((Scene) newValue);

			}
		} else if (source instanceof Scene) {
			Scene scene = (Scene) source;
			SceneTreeLeaf leaf = findLeaf(scene);
			if (leaf != null) {
				SceneTreeGroup group = leaf.getParent();
				if (remove(scene)) {
					add(scene, group);
				}
			}
		}
	}

	/**
	 * Updated den Status einer Szene, ob sie in dem Graphen ist, oder nicht.
	 * 
	 * @param sceneObject
	 *            Die Szenen deren Status upgedatet werden soll
	 * @param inGraph
	 *            True, falls sich die Szene nun im Graphen befindet, false
	 *            falls sie nicht im Graphen enthalten ist.
	 */
	private void updateTreeStatus(Scene sceneObject, boolean inGraph) {
		SceneTreeNode leafToRemove = new SceneTreeLeaf(null, sceneObject);
		if (inGraph) {
			int removePos = groupUnused.getEntries().indexOf(leafToRemove);
			if (removePos >= 0) {
				SceneTreeNode leafToAdd = groupUnused.getEntries().get(
						removePos);
				groupUnused.removeEntry(leafToRemove);
				groupUsed.addEntries(leafToAdd);
			}
		} else {
			int removePos = groupUsed.getEntries().indexOf(leafToRemove);
			if (removePos >= 0) {
				SceneTreeNode leafToAdd = groupUsed.getEntries().get(removePos);
				groupUsed.removeEntry(leafToRemove);
				groupUnused.addEntries(leafToAdd);
			}
		}
		fireChangeEvent();
	}
}
