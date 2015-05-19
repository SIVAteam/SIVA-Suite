package org.iviPro.export.smil.objects;

import org.iviPro.export.Exporter;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.graph.NodeAnnotationRichtext;

public class SMILExporterNodeAnnotationRichtext extends
		SMILExporterNodeAnnotationLeaf {

	private NodeAnnotationRichtext richtextAnnotation;

	public SMILExporterNodeAnnotationRichtext(IAbstractBean object) {
		super(object);
		this.richtextAnnotation = (NodeAnnotationRichtext) object;
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeAnnotationRichtext.class;
	}

	@Override
	protected String getTagName() {
		return TAG_TEXT;
	}

	@Override
	protected String getSubDirectory() {
		return Exporter.EXPORT_SUBDIR_RICHTEXTS;
	}

	@Override
	protected boolean requiresPositionInfo() {
		return true;
	}

	@Override
	protected IFileBasedObject getFileBasedObject() {
		return this.richtextAnnotation.getRichtext();
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}

}
