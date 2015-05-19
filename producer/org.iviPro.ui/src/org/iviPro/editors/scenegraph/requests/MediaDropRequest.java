/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.iviPro.editors.scenegraph.requests;

import java.util.List;

import org.eclipse.gef.Request;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.graph.NodeScene;

/**
 * Request fuer das Droppen eines Medien-Objekts auf den Graph.
 * 
 * @author dellwo
 * 
 */
public class MediaDropRequest extends Request {

	/**
	 * Die Liste mit den Medien-Objekten die gedroppt wurden.
	 */
	private List<IAbstractBean> droppedMediaObjects;

	/**
	 * Der Szenen-Knoten auf den die Media-Objekte gedroppt wurden und fuer den
	 * daraus die Annotationen erstellt werden sollen.
	 */
	private NodeScene sceneNode;

	public List<IAbstractBean> getDroppedMediaObjects() {
		return droppedMediaObjects;
	}

	public void setDroppedMediaObjects(List<IAbstractBean> droppedMediaObjects) {
		this.droppedMediaObjects = droppedMediaObjects;
	}

	public NodeScene getSceneNode() {
		return sceneNode;
	}

	public void setSceneNode(NodeScene sceneNode) {
		this.sceneNode = sceneNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[SceneNode=" + sceneNode //$NON-NLS-1$
				+ ", MediaObjects=" + droppedMediaObjects + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
