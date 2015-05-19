/**
 * 
 */
package org.iviPro.model.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.resources.IResource;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.RichText;

/**
 * Annotationsknoten fuer eine Richtext-Annotation.
 * 
 * @author dellwo
 */
public class NodeAnnotationRichtext extends INodeAnnotationLeaf {

	/**
	 * Der der Annotation zugeordnete Richtext.
	 * 
	 * @uml.property name="richtext"
	 */
	private RichText richtext;
	
	/**
	 * Erstellt eine neue Richtext-Annotation
	 * 
	 * @param title
	 *            Der Titel der Annotation.
	 * @param project
	 *            Das zugehoerige Projekt.
	 */
	public NodeAnnotationRichtext(LocalizedString title, Project project) {
		super(title, project);
	}

	/**
	 * Erstellt eine neue Richtext-Annotation
	 * 
	 * @param title
	 *            Der Titel der Annotation.
	 * @param project
	 *            Das zugehoerige Projekt.
	 */
	public NodeAnnotationRichtext(String title, Project project) {
		super(title, project);
	}
	

	/**
	 * Gibt den mit der Annotation verknuepften Richtext zurueck.
	 * 
	 * @return Mit der Annotation verknuepfter Richtext.
	 * @uml.property name="richtext"
	 */
	public RichText getRichtext() {
		return richtext;
	}

	/**
	 * Setzt den mit der Annotation verknuepften Richtext.
	 * 
	 * @param richtext
	 *            Mit der Annotaiton verknuepfter Richtext.
	 * @uml.property name="richtext"
	 */
	public void setRichtext(RichText richtext) {
		this.richtext = richtext;
		firePropertyChange(PROP_SETCONTENT, null, richtext);
	}

	/**
	 * Retrieves the set of pictures contained in the media repository which 
	 * are referenced by the richtext and therefore need to be exported. 
	 * @return list of referenced pictures
	 */
	public Set<Picture> getPictures() {
		return richtext.getPictureSet();
	}	
	
	@Override
	public List<IResource> getResources() {
		ArrayList<IResource> resources = new ArrayList<IResource>(1);
		if (richtext != null) {
			resources = new ArrayList<IResource>(richtext.getPictureSet());
			resources.add(richtext);
		}
		return resources;
	}
	
	@Override
	public boolean isDependentOn(IAbstractBean object) {
			return object != null && object == richtext;
	}
}
