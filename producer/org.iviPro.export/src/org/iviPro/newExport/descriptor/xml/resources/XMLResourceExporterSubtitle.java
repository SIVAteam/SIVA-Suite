package org.iviPro.newExport.descriptor.xml.resources;

import org.iviPro.model.LocalizedElement;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.resources.Subtitle;
import org.iviPro.newExport.ExportException;
import org.w3c.dom.Element;

public class XMLResourceExporterSubtitle extends IXMLResourceExporter {
	
	private Subtitle subtitle;
	
	public XMLResourceExporterSubtitle(Subtitle subtitle) {
		super(subtitle);
		this.subtitle = subtitle;
	}

	@Override
	protected void setAdditionalRessourceAttributes(Element resElement)
			throws ExportException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setAdditionalContentAttributes(Element contentElement,
			LocalizedElement content) {
		// Add subtitle string as content of content element
		LocalizedString text = (LocalizedString) content;
		contentElement.setTextContent(text.getValue());	
	}

	@Override
	protected String getResourceTag() {
		return TAG_SUBTITLE;
	}
}
