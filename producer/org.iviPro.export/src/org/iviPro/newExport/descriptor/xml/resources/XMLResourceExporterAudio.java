package org.iviPro.newExport.descriptor.xml.resources;

import org.iviPro.model.LocalizedElement;
import org.iviPro.model.LocalizedFile;
import org.iviPro.model.resources.Audio;
import org.iviPro.newExport.ExportException;
import org.w3c.dom.Element;

public class XMLResourceExporterAudio extends IXMLResourceExporter {
	
	private Audio audio;
	private long start;
	private long end;

	public XMLResourceExporterAudio(Audio audio) {
		this(audio, 0L, audio.getDuration());
	}
	
	public XMLResourceExporterAudio(Audio audio, long start, long end) {
		super(audio);
		this.audio = audio;
		this.start = start;
		this.end = end;
	}
	
	@Override
	protected void setAdditionalRessourceAttributes(Element resElement)
			throws ExportException {
		if (idManager.getProfile().getGeneral().isExportAudioExtensions()
				&& idManager.getProfile().getAudio().getAudioVariants().size() == 1) {
			resElement.setAttribute(ATTR_RES_AUDIOCODEC, idManager.getProfile()
					.getAudio().getAudioVariants().get(0).getAudioProfiles()
					.get(0).getAudioCodec().getTranscoderParameter());
		}
	}
	
	@Override
	protected void setAdditionalContentAttributes(Element contentElement,
			LocalizedElement content) throws ExportException {
		LocalizedFile file = (LocalizedFile) content;
		// Add reference to file
		String path = idManager.getAudioFileName(idManager.getID(resource),
				file.getValue(), file.getLanguage(), start, end);
		contentElement.setAttribute(ATTR_HREF, path);
	}

	@Override
	protected String getResourceTag() {
		return TAG_AUDIOSTREAM;
	}
}
