package org.iviPro.model.graph;

import java.util.ArrayList;
import java.util.List;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.resources.IResource;
import org.iviPro.model.resources.PdfDocument;

public class NodeAnnotationPdf extends INodeAnnotationLeaf {
	
	private PdfDocument pdfDoc;

	public NodeAnnotationPdf(LocalizedString title, Project project) {
		super(title, project);
	}
	
	public NodeAnnotationPdf(String title, Project project) {
		super(title, project);
	}

	@Override
	public List<IResource> getResources() {
		ArrayList<IResource> resources = new ArrayList<IResource>(1);
		if (pdfDoc != null) {
			resources.add(pdfDoc);
		}
		return resources;
	}

	@Override
	public boolean isDependentOn(IAbstractBean object) {
		return object != null && object == pdfDoc;
	}

	/**
	 * Set the <code>PdfDocument</code> of this annotation.
	 * @param pdfDoc <code>PdfDocument</code> for this annotation
	 */
	public void setPdf(PdfDocument pdfDoc) {
		this.pdfDoc = pdfDoc;
		firePropertyChange(PROP_SETCONTENT, null, pdfDoc);
	}
	
	/**
	 * Get the <code>PdfDocument</code> of this annotation.
	 * @return <code>PdfDocument</code> stored in this annotation
	 */
	public PdfDocument getPdf() {
		return pdfDoc;
	}
}
