package org.iviPro.export.xml.objects;

import java.util.ArrayList;
import java.util.List;

import org.iviPro.export.ExportType;
import org.iviPro.export.Exporter;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.Audio;
import org.iviPro.model.AudioPart;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.graph.NodeAnnotationAudio;
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

	XMLExporterNodeAnnotationAudio(IAbstractBean exportObj) {
		super(exportObj);
		audioAnnotation = (NodeAnnotationAudio) exportObj;
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeAnnotationAudio.class;
	}

	@Override
	protected List<IFileBasedObject> getFileBasedObjects() {
		List<IFileBasedObject> fbo = new ArrayList<IFileBasedObject>();
		// das zu exportierende Audio-File
		Audio audio;
		// prüfe ob eine Szene oder ein Video exportiert wird
		if (audioAnnotation.getContentType() == NodeAnnotationAudio.CONTENT_AUDIOPART) {
			audio = audioAnnotation.getAudioPart().getAudio();
		} else {
			audio = audioAnnotation.getAudio();
		}	
		fbo.add(audio);
		return fbo;
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
		return false;
	}

	@Override
	protected void setAdditionalRessourceAttributes(Element element,
			IDManager idManager) {
		if (idManager.getExportType() != ExportType.HTML5) {
			element.setAttribute(ATTR_RES_AUDIOCODEC, idManager.getExportType()
					.getFFmpegAudioCodec());
		}
	}

	@Override
	protected String getResourceSubdirectory() {
		return Exporter.EXPORT_SUBDIR_AUDIOS;
	}

	@Override
	protected void setAdditionActionAttributes(Element action,
			IDManager idManager) {
		action.setAttribute(ATTR_PLAYAUDIO_MUTE, "" //$NON-NLS-1$
				+ audioAnnotation.isMuteVideo());
	}

	@Override
	protected ExportParameters getExportParameters() {
		// setze die Start und Endzeit für den Export
		if (audioAnnotation.getContentType() == NodeAnnotationAudio.CONTENT_AUDIOPART) {
			AudioPart audioPart = audioAnnotation.getAudioPart();
			this.parameters.addValue(Exporter.EXPORT_KEY_STARTTIME, audioPart.getStart());
			this.parameters.addValue(Exporter.EXPORT_KEY_ENDTIME, audioPart.getEnd());	
			this.parameters.addValue(Exporter.EXPORT_ADDTOEXPORTFILE, audioPart.getStart() + "" + audioPart.getEnd());
		} 
		return this.parameters;
	}

}
