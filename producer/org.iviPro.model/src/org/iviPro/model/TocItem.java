/**
 * 
 */
package org.iviPro.model;

import java.util.LinkedList;
import java.util.List;

import org.iviPro.model.annotation.OverlayPathItem;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.ScreenArea;

/**
 * @author dellwo
 * @uml.dependency supplier="org.iviPro.model.graph.NodeScene"
 */
public class TocItem extends IAbstractBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5409404449824788760L;

	public TocItem(String title, Project project) {
		super(title, project);
		children = new BeanList<TocItem>(project);
	}

	/**
	 * 
	 */
	private List<TocItem> children;

	/**
	 * Getter of the property <tt>children</tt>
	 * 
	 * @return Returns the children.
	 * 
	 */
	public synchronized List<TocItem> getChildren() {
		if (children == null) {
			children = new BeanList<TocItem>(project);
		}
		return children;
	}

	/**
	 * @uml.property name="scene"
	 */
	private NodeScene scene;

	/**
	 * Getter of the property <tt>scene</tt>
	 * 
	 * @return Returns the scene.
	 * @uml.property name="scene"
	 */
	public NodeScene getScene() {
		return scene;
	}

	/**
	 * Setter of the property <tt>scene</tt>
	 * 
	 * @param scene
	 *            The scene to set.
	 * @uml.property name="scene"
	 */
	public void setScene(NodeScene scene) {
		String oldValue = "";
		if (this.scene != null) {
			oldValue = this.scene.getTitle();
		}
		this.scene = scene;
		String newValue = "";
		if (scene != null) {
			newValue = scene.getTitle();
		}
		firePropertyChange("bla", oldValue, newValue);
	}

	/**
	 * @uml.property name="overlayPath" readOnly="true"
	 */
	private List<OverlayPathItem> overlayPath;

	public List<OverlayPathItem> getOverlayPath() {
		return new LinkedList<OverlayPathItem>();
	}


	public void setOverlayPath(List<OverlayPathItem> overlayPath) {
		String old = "";
		if (this.overlayPath != null) {
			old = this.overlayPath.toString();
		}
		this.overlayPath = overlayPath;
		// Overlaypath soll keine Einträge beinhalten (vlg. SelectionControl)
		this.overlayPath.clear();
		firePropertyChange("setOverlayPath", old, this.overlayPath.toString());
	}

	/**
	 * @uml.property name="screenArea"
	 */
	private ScreenArea screenArea;
	
	public void setScreenArea(ScreenArea screenArea) {
		String old = "";
		if (this.screenArea != null) {
			 old = this.screenArea.toString();
		}
		this.screenArea = screenArea;
		firePropertyChange("setScreenArea", old, this.screenArea.toString());
	}
	
	public ScreenArea getScreenArea() {
		return screenArea;
	}
}
