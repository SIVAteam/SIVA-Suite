package org.iviPro.export.xml.objects;

import java.util.List;

import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.graph.NodeAnnotationText;
import org.w3c.dom.Element;

/**
 * Exporter fuer Knoten vom Typ NodeAnnotationText
 * 
 * @author dellwo
 * 
 */
public class XMLExporterNodeAnnotationText extends
		IXMLExporterNodeAnnotationLeaf {

	XMLExporterNodeAnnotationText(IAbstractBean exportObj) {
		super(exportObj);
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeAnnotationText.class;
	}

	@Override
	protected List<IFileBasedObject> getFileBasedObjects() {
		return null;
	}

	@Override
	protected String getTagNameAction() {
		return TAG_SHOWPLAINTEXT;
	}

	@Override
	protected String getTagNameResource() {
		return TAG_PLAINTEXT;
	}

	@Override
	protected boolean requiresPositionInfo() {
		return true;
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
