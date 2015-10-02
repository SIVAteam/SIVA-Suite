package org.iviPro.newExport.descriptor.xml.resources;

import org.iviPro.model.LocalizedElement;
import org.iviPro.model.resources.VideoThumbnail;
import org.iviPro.newExport.ExportException;
import org.w3c.dom.Element;

public class XMLResourceExporterVideoThumbnail extends IXMLResourceExporter {
	
	private VideoThumbnail thumbnail;

	public XMLResourceExporterVideoThumbnail(VideoThumbnail thumbnail) {
		super(thumbnail);
		this.thumbnail = thumbnail;
	}

	@Override
	protected void setAdditionalRessourceAttributes(Element resElement) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void setAdditionalContentAttributes(Element contentElement,
			LocalizedElement content) throws ExportException {
		// Add reference to file
		String parentId = (thumbnail.getScene()==null
				? idManager.getID(thumbnail.getVideo())	
						: idManager.getID(thumbnail.getScene()));
		String path = idManager.getVideoThumbnailName(parentId, 
				content.getLanguage(), thumbnail.getVideo(), thumbnail.getTime());
		contentElement.setAttribute(ATTR_HREF, path);
	}
	
	@Override
	protected String getResourceTag() {
		return TAG_IMAGE;
	}
}
