package org.iviPro.views.scenerepository;

public abstract class SceneTreeNode implements Comparable<SceneTreeNode> {

	protected SceneTreeGroup parent;

	SceneTreeNode(SceneTreeGroup parent) {
		this.parent = parent;
	}

	/**
	 * Liefert den Anzeigename.
	 */
	public abstract String getName();

	/**
	 * Liefert das Vaterobjekt
	 */
	public SceneTreeGroup getParent() {
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SceneTreeNode o) {
		return getName().compareTo(o.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SceneTreeNode) {
			SceneTreeNode other = (SceneTreeNode) obj;
			return getName().equals(other.getName());
		} else {
			return false;
		}
	}
}
