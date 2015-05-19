package org.iviPro.export.xml.objects;

import java.util.ArrayList;
import java.util.List;
import org.iviPro.export.ExportType;
import org.iviPro.export.Exporter;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.Scene;
import org.iviPro.model.Video;
import org.iviPro.model.graph.NodeAnnotationVideo;
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

	XMLExporterNodeAnnotationVideo(IAbstractBean exportObj) {
		super(exportObj);
		videoAnnotation = (NodeAnnotationVideo) exportObj;
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeAnnotationVideo.class;
	}

	@Override
	protected List<IFileBasedObject> getFileBasedObjects() {
		List<IFileBasedObject> fbo = new ArrayList<IFileBasedObject>();
		// das zu exportierende Video
		Video video;
		// prüfe ob eine Szene oder ein Video exportiert wird
		if (videoAnnotation.getContentType() == NodeAnnotationVideo.CONTENT_SCENE) {
			video = videoAnnotation.getScene().getVideo();
		} else {
			video = videoAnnotation.getVideo();
		}		
		fbo.add(video);
		return fbo;
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
	protected void setAdditionalRessourceAttributes(Element element,
			IDManager idManager) {
		if (idManager.getExportType() != ExportType.HTML5) {
			element.setAttribute(ATTR_RES_CONTAINERFORMAT, idManager
					.getExportType().getFFmpegVideoContainerFormat());
			element.setAttribute(ATTR_RES_VIDEOCODEC, idManager.getExportType()
					.getFFmpegVideoCodec());
			element.setAttribute(ATTR_RES_AUDIOCODEC, idManager.getExportType()
					.getFFmpegAudioCodec());
		}
	}

	@Override
	protected String getResourceSubdirectory() {
		return Exporter.EXPORT_SUBDIR_VIDEOS;
	}

	@Override
	protected void setAdditionActionAttributes(Element action,
			IDManager idManager) {
		action.setAttribute(ATTR_PLAYAUDIO_MUTE, "" //$NON-NLS-1$
				+ videoAnnotation.isMuteVideo());
	}

	@Override
	protected ExportParameters getExportParameters() {	
		if (videoAnnotation.getContentType() == NodeAnnotationVideo.CONTENT_SCENE) {
			// setze die Start und Endzeit für den Export
			Scene scene = videoAnnotation.getScene();
			this.parameters.addValue(Exporter.EXPORT_KEY_STARTTIME, scene.getStart());
			this.parameters.addValue(Exporter.EXPORT_KEY_ENDTIME, scene.getEnd());	
			this.parameters.addValue(Exporter.EXPORT_ADDTOEXPORTFILE, scene.getStart() + "" + scene.getEnd());
		} else {
			this.parameters.addValue(Exporter.EXPORT_KEY_STARTTIME, 0L);
			this.parameters.addValue(Exporter.EXPORT_KEY_ENDTIME, videoAnnotation.getVideo().getDuration());			
		}
		return this.parameters;
	}
}
