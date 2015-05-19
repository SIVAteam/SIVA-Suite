package org.iviPro.editors.shotoverview;

import java.util.List;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchWindow;
import org.iviPro.scenedetection.sd_main.Scene;
import org.iviPro.scenedetection.sd_misc.Mpeg7Export;
import org.iviPro.model.Video;

public class SceneOverviewEditorInput implements IEditorInput {

	private IWorkbenchWindow window;

	private List<Scene> sceneList;

	private Video vid;

	private boolean mpeg7;

	private Mpeg7Export exporter;

	private String mediaPath;

	public SceneOverviewEditorInput(IWorkbenchWindow window,
			List<Scene> sceneList, Video video, boolean mpeg7,
			Mpeg7Export exporter, String mediaPath) {
		this.window = window;
		this.vid = video;
		this.sceneList = sceneList;
		this.mpeg7 = mpeg7;
		this.exporter = exporter;
		this.mediaPath = mediaPath;
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
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolTipText() {
		// TODO Auto-generated method stub
		return "";
	}

	public List<Scene> getSceneList() {
		return sceneList;
	}

	public IWorkbenchWindow getWindow() {
		return window;
	}

	public Video getVideo() {
		return vid;
	}

	public boolean getMpeg7() {
		return mpeg7;
	}

	public Mpeg7Export getMpeg7Exporter() {
		return exporter;
	}

	public String getMediaLocator() {
		return mediaPath;
	}
}
