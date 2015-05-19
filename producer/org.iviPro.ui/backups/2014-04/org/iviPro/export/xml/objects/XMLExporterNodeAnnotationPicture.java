package org.iviPro.export.xml.objects;

import java.util.ArrayList;
import java.util.List;

import org.iviPro.export.Exporter;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.Picture;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.w3c.dom.Element;

/**
 * Exporter fuer Knoten vom Typ NodeAnnotationPicture
 * 
 * @author dellwo
 * 
 */
public class XMLExporterNodeAnnotationPicture extends
		IXMLExporterNodeAnnotationLeaf {

	private NodeAnnotationPicture pictureAnnotation;

	XMLExporterNodeAnnotationPicture(IAbstractBean exportObj) {
		super(exportObj);
		pictureAnnotation = (NodeAnnotationPicture) exportObj;		
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeAnnotationPicture.class;
	}

	@Override
	protected List<IFileBasedObject> getFileBasedObjects() {
		List<IFileBasedObject> fbo = new ArrayList<IFileBasedObject>();
		if (pictureAnnotation.getContentType() == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
			for (Picture pic : pictureAnnotation.getPictureGallery().getPictures()) {
				fbo.add(pic);
			}
		} else {
			fbo.add(pictureAnnotation.getPicture());
		}
		return fbo;
	}

	@Override
	protected String getTagNameAction() {
		if (pictureAnnotation.getContentType() == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
			return TAG_SHOWIMAGE_GAL;
		}
		return TAG_SHOWIMAGE;
	}

	@Override
	protected String getTagNameResource() {
		return TAG_IMAGE;
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
		return Exporter.EXPORT_SUBDIR_PICTURES;
	}

	@Override
	protected void setAdditionActionAttributes(Element action,
			IDManager idManager) {
		if (pictureAnnotation.getContentType() == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
			action.setAttribute(ATTR_PICGAL_COLUMNS, "" + pictureAnnotation.getPictureGallery().getNumberColumns());
		}
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}
}
