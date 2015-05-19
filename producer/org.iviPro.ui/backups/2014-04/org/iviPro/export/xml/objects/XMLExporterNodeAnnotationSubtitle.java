package org.iviPro.export.xml.objects;

import java.util.List;

import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.w3c.dom.Element;

/**
 * Exporter fuer Knoten vom Typ NodeAnnotationText
 * 
 * @author dellwo
 * 
 */
public class XMLExporterNodeAnnotationSubtitle extends
		IXMLExporterNodeAnnotationLeaf {

	XMLExporterNodeAnnotationSubtitle(IAbstractBean exportObj) {
		super(exportObj);
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeAnnotationSubtitle.class;
	}

	@Override
	protected List<IFileBasedObject> getFileBasedObjects() {
		return null;
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
	protected void setAdditionalRessourceAttributes(Element element,
			IDManager idManager) {
	}

	@Override
	protected String getResourceSubdirectory() {
		return null;
	}

	@Override
	protected void setAdditionActionAttributes(Element action,
			IDManager idManager) {
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}

}
