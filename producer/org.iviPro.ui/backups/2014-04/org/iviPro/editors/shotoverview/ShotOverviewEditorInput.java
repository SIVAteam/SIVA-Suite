package org.iviPro.editors.shotoverview;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchWindow;
import org.iviPro.application.Application;
import org.iviPro.model.Video;
import org.iviPro.scenedetection.sd_main.Shot;

public class ShotOverviewEditorInput implements IEditorInput {

	private boolean parallel;

	private boolean mpeg7;

	private List<Shot> shotList;

	private Video selectedVideo;

	private IWorkbenchWindow window;

	public ShotOverviewEditorInput(IWorkbenchWindow window, boolean parallel,
			boolean mpeg7, List<Shot> shotList, Video selectedVideo) {
		this.window = window;
		this.parallel = parallel;
		this.mpeg7 = mpeg7;
		// Removes memory leaks
		this.shotList = new LinkedList<Shot>();
		for (int i = 0; i < shotList.size(); i++) {
			this.shotList.add(shotList.get(i).clone());
		}
		this.selectedVideo = selectedVideo;
	}

	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
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
		return selectedVideo.getTitle(Application.getCurrentLanguage());
	}

	@Override
	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolTipText() {
		return "ToolTip";
	}
	
	public boolean isParallel() {
		return parallel;
	}

	public boolean isMpeg7() {
		return mpeg7;
	}

	public List<Shot> getShotList() {
		return shotList;
	}

	public Video getSelectedVideo() {
		return selectedVideo;
	}

	public IWorkbenchWindow getWindow() {
		return window;
	}
	
	public void shotGarbageCollection() {
		shotList = null;
	}

}