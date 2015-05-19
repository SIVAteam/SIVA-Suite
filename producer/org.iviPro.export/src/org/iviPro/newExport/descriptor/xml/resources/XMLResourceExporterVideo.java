package org.iviPro.newExport.descriptor.xml.resources;

import org.iviPro.model.LocalizedElement;
import org.iviPro.model.LocalizedFile;
import org.iviPro.model.resources.Video;
import org.iviPro.newExport.ExportException;
import org.w3c.dom.Element;

public class XMLResourceExporterVideo extends IXMLResourceExporter {

	private Video video;
	private long start;
	private long end;
	
	public XMLResourceExporterVideo(Video video) {
		this(video, 0L, video.getDuration());
	}
	
	public XMLResourceExporterVideo(Video video, long start, long end) {
		super(video);
		this.video = video;
		this.start = start;
		this.end = end;
	}
	
	@Override
	protected void setAdditionalRessourceAttributes(Element resElement)
			throws ExportException {
		if (idManager.getProfile().getGeneral().isExportVideoExtensions()
				&& idManager.getProfile().getVideo().getVideoVariants().size() == 1
				&& idManager.getProfile().getVideo().getVideoVariants().get(0)
						.getVideoProfiles().size() == 1) {
			resElement.setAttribute(ATTR_RES_CONTAINERFORMAT, idManager
					.getProfile().getVideo().getVideoVariants().get(0)
					.getVideoProfiles().get(0).getVideoContainer()
					.getTranscoderParameter());
			resElement.setAttribute(ATTR_RES_AUDIOCODEC, idManager.getProfile()
					.getVideo().getVideoVariants().get(0).getVideoProfiles()
					.get(0).getAudioCodec().getTranscoderParameter());
			resElement.setAttribute(ATTR_RES_VIDEOCODEC, idManager.getProfile()
					.getVideo().getVideoVariants().get(0).getVideoProfiles()
					.get(0).getVideoCodec().getTranscoderParameter());
		}

	}

	@Override
	protected void setAdditionalContentAttributes(Element contentElement,
			LocalizedElement content) {
		LocalizedFile locFile = (LocalizedFile)content;
		// Add reference to file
		String path = idManager.getVideoFileName(idManager.getID(resource), 
				locFile.getValue(), locFile.getLanguage(), start, end, video);
		contentElement.setAttribute(ATTR_HREF, path);
	}

	@Override
	protected String getResourceTag() {
		return TAG_VIDEOSTREAM;
	}
}
