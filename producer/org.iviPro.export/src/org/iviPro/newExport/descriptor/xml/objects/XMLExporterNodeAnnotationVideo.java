package org.iviPro.newExport.descriptor.xml.objects;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.graph.NodeAnnotationVideo;
import org.iviPro.model.resources.IResource;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Exporter fuer Knoten vom Typ NodeAnnotationVideo
 * 
 * @author dellwo
 * 
 */
public class XMLExporterNodeAnnotationVideo extends
		IXMLExporterNodeAnnotationLeaf {

	private NodeAnnotationVideo videoAnnotation;

	XMLExporterNodeAnnotationVideo(NodeAnnotationVideo exportObj) {
		super(exportObj);
		videoAnnotation = exportObj;
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeAnnotationVideo.class;
	}

	@Override
	protected String getTagNameAction() {
		return TAG_SHOWVIDEO;
	}

	@Override
	protected String getTagNameResource() {
		return TAG_VIDEOSTREAM;
	}

	@Override
	protected boolean requiresPositionInfo() {
		return true;
	}
	
	@Override
	protected IResource getReferencedResource() {
		if (videoAnnotation.getContentType() == NodeAnnotationVideo.CONTENT_VIDEO) {
			return videoAnnotation.getVideo();
		} else {
			return videoAnnotation.getScene();
		}
	}

	@Override
	protected void setAdditionalActionElements(Element action,
			IdManager idManager, Document doc) {
		if (!isGlobalAnnotation) {
			action.setAttribute(ATTR_PLAYAUDIO_MUTE,
					String.valueOf(videoAnnotation.isMuteVideo()));
		}
	}
}
