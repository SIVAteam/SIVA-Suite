package org.iviPro.newExport.descriptor.xml.objects;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.graph.NodeAnnotationPdf;
import org.iviPro.model.resources.IResource;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLExporterNodeAnnotationPdf extends
		IXMLExporterNodeAnnotationLeaf {
	
	private NodeAnnotationPdf pdfAnnotation;

	XMLExporterNodeAnnotationPdf(NodeAnnotationPdf exportObj) {
		super(exportObj);
		pdfAnnotation = exportObj;
	}

	@Override
	protected String getTagNameResource() {
		return TAG_PDFDOCUMENT;
	}

	@Override
	protected String getTagNameAction() {
		return TAG_SHOWPDF;
	}

	@Override
	protected boolean requiresPositionInfo() {
		return true;
	}

	@Override
	protected IResource getReferencedResource() {
		return pdfAnnotation.getPdf();
	}

	@Override
	protected void setAdditionalActionElements(Element action,
			IdManager idManager, Document doc) {
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeAnnotationPdf.class;
	}
}
