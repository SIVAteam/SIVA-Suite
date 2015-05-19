package org.iviPro.editors.sceneeditor;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.iviPro.application.Application;
import org.iviPro.editors.shotoverview.ShotOverviewEditor;
import org.iviPro.model.Video;
import org.iviPro.utils.PathHelper;

public class DefineScenesShotInsertEditorInput implements IEditorInput {

	private static Logger logger = Logger
			.getLogger(DefineScenesShotInsertEditorInput.class);

	private Video video;

	private long start;

	private long end;
	
	private long framePosToAdd;
	
	private ShotOverviewEditor ed;
	
	private int shotId;

	public DefineScenesShotInsertEditorInput(Video video, long start, long end, ShotOverviewEditor ed, long framePosToAdd, int shotId) {
		this.video = video;
		this.start = start;
		this.end = end;
		this.ed = ed;
		this.framePosToAdd = framePosToAdd;
		logger.debug("Created new DefineScenesShotInsertEditorInput for "
				+ video);
		PropertyConfigurator.configure(PathHelper.getPathToLoggerIni());
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
		return video.getTitle(Application.getCurrentLanguage());
	}

	@Override
	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolTipText() {
		return video.getFile().getAbsolutePath();
	}

	public Video getVideo() {
		return video;
	}
	
	public ShotOverviewEditor getOverviewEditor() {
		return ed;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public long getFramePosToAdd() {
		return framePosToAdd;
	}
	
	public int getShotId() {
		return shotId;
	}
	
}
