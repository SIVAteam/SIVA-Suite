package org.iviPro.export.smil.objects;

import org.iviPro.export.Exporter;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.Video;
import org.iviPro.model.graph.NodeAnnotationVideo;

public class SMILExporterNodeAnnotationVideo extends
		SMILExporterNodeAnnotationLeaf {

	private NodeAnnotationVideo videoAnnotation;

	public SMILExporterNodeAnnotationVideo(IAbstractBean object) {
		super(object);
		this.videoAnnotation = (NodeAnnotationVideo) object;
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeAnnotationVideo.class;
	}

	@Override
	protected String getTagName() {
		return TAG_VIDEO;
	}

	@Override
	protected String getSubDirectory() {
		return Exporter.EXPORT_SUBDIR_VIDEOS;
	}

	@Override
	protected boolean requiresPositionInfo() {
		return true;
	}

	@Override
	protected IFileBasedObject getFileBasedObject() {
		Video video;
		if (videoAnnotation.getContentType() == NodeAnnotationVideo.CONTENT_SCENE) {
			video = videoAnnotation.getScene().getVideo();
		} else {
			video = videoAnnotation.getVideo();
		}
		return video;
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}

}
