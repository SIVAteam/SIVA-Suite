package org.iviPro.newExport.descriptor.xml.resources;

import org.iviPro.model.LocalizedElement;
import org.iviPro.model.LocalizedFile;
import org.iviPro.model.resources.Picture;
import org.iviPro.newExport.ExportException;
import org.w3c.dom.Element;

public class XMLResourceExporterPicture extends IXMLResourceExporter {

	private Picture picture;
	
	public XMLResourceExporterPicture(Picture picture) {
		super(picture);
		this.picture = picture;
	}
	
	@Override
	protected void setAdditionalRessourceAttributes(Element resElement) {
	}

	@Override
	protected void setAdditionalContentAttributes(Element contentElement,
			LocalizedElement content) throws ExportException {
		LocalizedFile file = (LocalizedFile) content;
		// Add reference to file
		String path = idManager.getImageFileName(idManager.getID(resource), 
				file.getValue(), file.getLanguage(), picture);
		contentElement.setAttribute(ATTR_HREF, path);
	}

	@Override
	protected String getResourceTag() {
		return TAG_IMAGE;
	}

}
