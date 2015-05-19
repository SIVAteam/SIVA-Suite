package org.iviPro.newExport.descriptor.xml.objects;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.graph.NodeAnnotationRichtext;
import org.iviPro.model.resources.IResource;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Exporter fuer Knoten vom Typ NodeAnnotationRichtext
 * 
 * @author dellwo
 * 
 */
public class XMLExporterNodeAnnotationRichtext extends
		IXMLExporterNodeAnnotationLeaf {

	private NodeAnnotationRichtext richtextAnnotation;

	XMLExporterNodeAnnotationRichtext(NodeAnnotationRichtext exportObj) {
		super(exportObj);
		richtextAnnotation = exportObj;
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeAnnotationRichtext.class;
	}

	@Override
	protected String getTagNameAction() {
		return TAG_SHOWRICHPAGE;
	}

	@Override
	protected String getTagNameResource() {
		return TAG_RICHPAGE;
	}

	@Override
	protected boolean requiresPositionInfo() {
		return true;
	}
	
	@Override
	protected IResource getReferencedResource() {
		return richtextAnnotation.getRichtext();
	}

	@Override
	protected void setAdditionalActionElements(Element action,
			IdManager idManager, Document doc) {
	}
}
