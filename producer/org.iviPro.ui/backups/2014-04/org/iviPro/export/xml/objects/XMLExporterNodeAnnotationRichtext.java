package org.iviPro.export.xml.objects;

import java.util.ArrayList;
import java.util.List;

import org.iviPro.export.Exporter;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.graph.NodeAnnotationRichtext;
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

	XMLExporterNodeAnnotationRichtext(IAbstractBean exportObj) {
		super(exportObj);
		richtextAnnotation = (NodeAnnotationRichtext) exportObj;
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeAnnotationRichtext.class;
	}

	@Override
	protected List<IFileBasedObject> getFileBasedObjects() {
		List<IFileBasedObject> fbo = new ArrayList<IFileBasedObject>();
		fbo.add(richtextAnnotation.getRichtext());
		return fbo;
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
	protected void setAdditionalRessourceAttributes(Element element,
			IDManager idManager) {
	}

	@Override
	protected String getResourceSubdirectory() {
		return Exporter.EXPORT_SUBDIR_RICHTEXTS;
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
