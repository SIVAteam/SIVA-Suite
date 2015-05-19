package org.iviPro.model.graph;

import java.util.ArrayList;
import java.util.List;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.PictureGallery;
import org.iviPro.model.Project;
import org.iviPro.model.resources.IResource;
import org.iviPro.model.resources.Picture;

/**
 * Annotationsknoten fuer Bild-Annotationen
 * Unterstützt sowohl eine Bildergalerie auch als ein einzelnes Bild
 * 
 * @author juhoffma
 */
public class NodeAnnotationPicture extends INodeAnnotationLeaf {

	/**
	 * Das Bild dem die Annotation zu Grunde liegt.
	 * 
	 * @uml.property name="picture"
	 */
	private Picture picture;
	
	/**
	 * Die Bilder der Bildergalerie
	 * @uml.property name="pictures"
	 */
	private PictureGallery pictureGallery;
	
	/**
	 * Gibt an ob ein einzelnes Bild oder eine galerie verwendet wird
	 * @uml.property name="contentType"
	 */
	private int contentType = CONTENT_PICTURE;
	
	public static final int CONTENT_PICTURE = 0;
	public static final int CONTENT_PICTUREGALLERY = 1;	

	/**
	 * Erstellt eine neue Bild-Annotation
	 * 
	 * @param title
	 *            Der Titel der Annotation.
	 * @param project
	 *            Das zugehoerige Projekt.
	 */
	public NodeAnnotationPicture(LocalizedString title, Project project) {
		super(title, project);
	}

	/**
	 * Erstellt eine neue Bild-Annotation
	 * 
	 * @param title
	 *            Der Titel der Annotation.
	 * @param project
	 *            Das zugehoerige Projekt.
	 */
	public NodeAnnotationPicture(String title, Project project) {
		super(title, project);
	}
	
	/**
	 * Setzt das mit der Annotation verknuepfte Bild.
	 * 
	 * @param picture
	 *            Mit der Annotaiton verknuepftes Bild.
	 * @uml.property name="picture"
	 */
	public void setPicture(Picture picture) {
		this.picture = picture;
		this.pictureGallery = null;
		this.contentType = CONTENT_PICTURE;
		firePropertyChange(PROP_SETCONTENT, null, picture);
	}
	
	/**
	 * Setzt die mit der Annotation verknuepfte Bildergalerie
	 * 
	 * @param pictureGallery Mit der Annotaiton verknuepfte Bildergalerie.
	 * @uml.property name="pictureGallery"
	 */
	public void setPictureGallery(PictureGallery pictureGallery) {
		this.pictureGallery = pictureGallery;
		this.picture = null;
		this.contentType = CONTENT_PICTUREGALLERY;
		System.out.println("SET PICTURE GALLERY" + this.contentType);
		firePropertyChange(PROP_SETCONTENT, null, pictureGallery);
	}	
	
	public int getContentType() {
		return this.contentType;
	}
	
	public void setContentType(int contentType) {
		this.contentType = contentType;
	}
	
	/**
	 * Gibt das mit der Annotation verknuepfte Bild zurueck.
	 * 
	 * @return Mit der Annotation verknuepftes Bild.
	 * @uml.property name="picture"
	 */
	public Picture getPicture() {
		return picture;
	}

	/**
	 * Gibt die Bildergalerie zurück
	 * @return Bildergalerie
	 * @uml.property name="pictureGallery"
	 */
	public PictureGallery getPictureGallery() {
		if (this.pictureGallery == null) {
			this.pictureGallery = new PictureGallery("", project);
		}
		return pictureGallery;
	}

	@Override
	public List<IResource> getResources() {
		ArrayList<IResource> resources = new ArrayList<IResource>(1);
		if (contentType == CONTENT_PICTURE 
				&& picture != null) {
			resources.add(picture);
		} else if (contentType == CONTENT_PICTUREGALLERY 
				&& pictureGallery != null) {
			resources = new ArrayList<IResource>(pictureGallery.getPictures());
		}
		return resources;
	}
	
	@Override
	public boolean isDependentOn(IAbstractBean object) {
		// Bild-Annotation ist abhaengig von ihrem Bild
		return object != null && (object == picture || object == pictureGallery);
	}
}
