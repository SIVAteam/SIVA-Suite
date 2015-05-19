package org.iviPro.preview;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.iviPro.model.graph.NodeScene;

public class PreviewInput implements IEditorInput {

	private NodeScene scene;
	
	public PreviewInput(NodeScene scene) {
		this.scene = scene;
	}
	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return Messages.PreviewInput_PreviewTitle;
	}

	@Override
	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolTipText() {
		// TODO Auto-generated method stub
		return Messages.PreviewInput_PreviewToolTip;
	}

	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public NodeScene getScene() {
		return scene;
	}

}
