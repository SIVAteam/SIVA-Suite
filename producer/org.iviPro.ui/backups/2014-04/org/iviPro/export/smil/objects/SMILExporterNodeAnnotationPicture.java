package org.iviPro.export.smil.objects;

import org.iviPro.export.Exporter;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.graph.NodeAnnotationPicture;

public class SMILExporterNodeAnnotationPicture extends
		SMILExporterNodeAnnotationLeaf {

	private NodeAnnotationPicture pictureAnnotation;

	public SMILExporterNodeAnnotationPicture(IAbstractBean object) {
		super(object);
		pictureAnnotation = (NodeAnnotationPicture) object;
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeAnnotationPicture.class;
	}

	@Override
	protected String getTagName() {
		return TAG_IMAGE;
	}

	@Override
	protected String getSubDirectory() {
		return Exporter.EXPORT_SUBDIR_PICTURES;
	}

	@Override
	protected boolean requiresPositionInfo() {
		return true;
	}

	@Override
	protected IFileBasedObject getFileBasedObject() {
		return this.pictureAnnotation.getPicture();
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}
}
