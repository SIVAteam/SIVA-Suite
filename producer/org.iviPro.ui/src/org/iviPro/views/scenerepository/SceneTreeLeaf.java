package org.iviPro.views.scenerepository;

import org.apache.log4j.Logger;
import org.iviPro.model.resources.Scene;

/**
 * Diese Klasse ist die letzte Hierarchiestufe in der Ansicht des
 * Szenenrepositories.
 * 
 * @author Florian Stegmaier
 * 
 */
public class SceneTreeLeaf extends SceneTreeNode {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SceneTreeLeaf.class);

	private Scene scene;

	/**
	 * Konstruktor...
	 * 
	 * @param parent
	 *            Vater-Knoten
	 * @param scene
	 *            Szene fuer diesen Szenen-Knoten
	 */
	public SceneTreeLeaf(SceneTreeGroup parent, Scene scene) {
		super(parent);
		this.scene = scene;
	}

	/**
	 * Liefert das gespeicherte Szenenobjekt.
	 * 
	 * @return
	 */
	public Scene getScene() {
		return scene;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return scene.getTitle();
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// org.iviPro.internObjects.Scene#clone(org.iviPro.internObjects.SceneGroup)
	// */
	// @Override
	// public SceneLeaf clone(SceneGroup parent) throws
	// CloneNotSupportedException {
	// SceneLeaf clone = (SceneLeaf) super.clone();
	// clone.scene = scene.clone();
	// return clone;
	// }
}
