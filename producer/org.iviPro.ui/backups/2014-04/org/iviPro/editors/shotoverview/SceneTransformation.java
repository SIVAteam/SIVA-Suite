package org.iviPro.editors.shotoverview;

import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.Scene;
import org.iviPro.model.Video;

public final class SceneTransformation {

	public static Scene createModelScene(LocalizedString title, Video video, Project project) {
		Scene sc = new Scene(title, video, project);
		return sc;
	}
	
}
