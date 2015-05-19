package org.iviPro.export.smil.objects;

import org.iviPro.export.Exporter;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.model.Audio;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.graph.NodeAnnotationAudio;

public class SMILExporterNodeAnnotationAudio extends
		SMILExporterNodeAnnotationLeaf {

	private NodeAnnotationAudio audioAnnotation;

	public SMILExporterNodeAnnotationAudio(IAbstractBean object) {
		super(object);
		this.audioAnnotation = (NodeAnnotationAudio) audioAnnotation;
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeAnnotationAudio.class;
	}

	@Override
	protected String getTagName() {
		return TAG_AUDIO;
	}

	@Override
	protected String getSubDirectory() {
		return Exporter.EXPORT_SUBDIR_AUDIOS;
	}

	@Override
	protected boolean requiresPositionInfo() {
		return false;
	}

	@Override
	protected IFileBasedObject getFileBasedObject() {
		Audio audio;

		if (audioAnnotation.getContentType() == NodeAnnotationAudio.CONTENT_AUDIOPART) {
			audio = audioAnnotation.getAudioPart().getAudio();
		} else {
			audio = audioAnnotation.getAudio();
		}
		return audio;
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}

}
