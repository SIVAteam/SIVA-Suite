package org.iviPro.newExport.descriptor.xml.objects;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.resources.IResource;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.w3c.dom.Document;
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

	XMLExporterNodeAnnotationPicture(NodeAnnotationPicture exportObj) {
		super(exportObj);
		pictureAnnotation = exportObj;
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeAnnotationPicture.class;
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
	protected IResource getReferencedResource() {
		if (pictureAnnotation.getContentType() == NodeAnnotationPicture.CONTENT_PICTURE) {
			return pictureAnnotation.getPicture();
		} else {
			return null;
		}
	}
	
	@Override
	protected void setAdditionalActionElements(Element action,
			IdManager idManager, Document doc) {
		if (pictureAnnotation.getContentType() == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
			action.setAttribute(ATTR_PICGAL_COLUMNS, String
					.valueOf(pictureAnnotation.getPictureGallery()
							.getNumberColumns()));
			
			// Add a separate element for galleries instead of a RefResId
			Element galRessources = doc.createElement(TAG_SHOWIMAGE_GALRES);
			for (IResource res : pictureAnnotation.getResources()) {
				Element galRessource = doc.createElement(TAG_SHOWIMAGE_GALSRES);
				galRessource.setAttribute(ATTR_REF_RES_ID, idManager.getID(res));
				galRessources.appendChild(galRessource);
			}
			action.appendChild(galRessources);
		}
	}
}
