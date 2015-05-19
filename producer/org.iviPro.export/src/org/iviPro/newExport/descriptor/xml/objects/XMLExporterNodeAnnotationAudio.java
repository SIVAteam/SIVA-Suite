package org.iviPro.newExport.descriptor.xml.objects;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.graph.NodeAnnotationAudio;
import org.iviPro.model.resources.IResource;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Exporter fuer Knoten vom Typ NodeAnnotationAudio
 * 
 * @author dellwo
 * 
 */
public class XMLExporterNodeAnnotationAudio extends
		IXMLExporterNodeAnnotationLeaf {

	private NodeAnnotationAudio audioAnnotation;

	XMLExporterNodeAnnotationAudio(NodeAnnotationAudio exportObj) {
		super(exportObj);
		audioAnnotation = exportObj;
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeAnnotationAudio.class;
	}

	@Override
	protected String getTagNameAction() {
		return TAG_PLAYAUDIO;
	}

	@Override
	protected String getTagNameResource() {
		return TAG_AUDIOSTREAM;
	}

	@Override
	protected boolean requiresPositionInfo() {
		return true;
	}
	
	@Override
	protected IResource getReferencedResource() {
		if (audioAnnotation.getContentType() == NodeAnnotationAudio.CONTENT_AUDIO) {
			return audioAnnotation.getAudio();
		} else {
			return audioAnnotation.getAudioPart();
		}
	}

	@Override
	protected void setAdditionalActionElements(Element action,
			IdManager idManager, Document doc) {
		if (!isGlobalAnnotation) {
			action.setAttribute(ATTR_PLAYAUDIO_MUTE,
					String.valueOf(audioAnnotation.isMuteVideo()));
		}
	}
}
