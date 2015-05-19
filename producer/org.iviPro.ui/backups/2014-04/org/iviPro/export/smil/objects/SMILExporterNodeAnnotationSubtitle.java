package org.iviPro.export.smil.objects;

import org.iviPro.export.xml.ExportParameters;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.graph.NodeAnnotationSubtitle;

public class SMILExporterNodeAnnotationSubtitle extends
		SMILExporterNodeAnnotationLeaf {

	public SMILExporterNodeAnnotationSubtitle(IAbstractBean object) {
		super(object);
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeAnnotationSubtitle.class;
	}

	@Override
	protected String getTagName() {
		return TAG_TEXT;
	}

	@Override
	protected String getSubDirectory() {
		return null;
	}

	@Override
	protected boolean requiresPositionInfo() {
		return false;
	}

	@Override
	protected IFileBasedObject getFileBasedObject() {
		return null;
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}

}
