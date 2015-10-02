package org.iviPro.newExport.descriptor.xml.resources;

import org.iviPro.model.LocalizedElement;
import org.iviPro.model.LocalizedFile;
import org.iviPro.model.resources.PdfDocument;
import org.iviPro.newExport.ExportException;
import org.w3c.dom.Element;

public class XMLResourceExporterPdf extends IXMLResourceExporter {
	
	private PdfDocument pdfDoc;

	public XMLResourceExporterPdf(PdfDocument pdfDoc) {
		super(pdfDoc);
		this.pdfDoc = pdfDoc;
	}

	@Override
	protected String getResourceTag() {
		return TAG_PDFDOCUMENT;
	}

	@Override
	protected void setAdditionalRessourceAttributes(Element resElement) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setAdditionalContentAttributes(Element contentElement,
			LocalizedElement content) throws ExportException {
		LocalizedFile file = (LocalizedFile) content;		
		// Add reference to file
		String path = idManager.getPdfDocumentFileName(idManager.getID(resource),
				file.getValue(), file.getLanguage());
		contentElement.setAttribute(ATTR_HREF, path);
	}

}
