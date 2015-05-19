package org.iviPro.newExport.descriptor.xml.objects;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.resources.IResource;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Exporter fuer Knoten vom Typ NodeAnnotationSubtitle
 * 
 * @author dellwo
 * 
 */
public class XMLExporterNodeAnnotationSubtitle extends
		IXMLExporterNodeAnnotationLeaf {
	
	private NodeAnnotationSubtitle subtitleAnnotation;
	
	XMLExporterNodeAnnotationSubtitle(NodeAnnotationSubtitle exportObj) {
		super(exportObj);
		subtitleAnnotation = exportObj;
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeAnnotationSubtitle.class;
	}

	@Override
	protected String getTagNameAction() {
		return TAG_SHOWSUBTITLE;
	}

	@Override
	protected String getTagNameResource() {
		return TAG_SUBTITLE;
	}

	@Override
	protected boolean requiresPositionInfo() {
		return false;
	}
	
	@Override
	protected IResource getReferencedResource() {
		return subtitleAnnotation.getSubtitle();
	}

	@Override
	protected void setAdditionalActionElements(Element action,
			IdManager idManager, Document doc) {
	}
}
