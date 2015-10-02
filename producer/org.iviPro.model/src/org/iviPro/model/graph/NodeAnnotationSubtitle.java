/**
 * 
 */
package org.iviPro.model.graph;

import java.util.ArrayList;
import java.util.List;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.resources.IResource;
import org.iviPro.model.resources.Subtitle;

/**
 * @author juhoffma
 */
public class NodeAnnotationSubtitle extends INodeAnnotationLeaf {

	/**
	 * Der Name des "subtitle"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_SUBTITLE = "subtitle"; //$NON-NLS-1$	
	
	/**
	 * @uml.property   name="audio"
	 */
	private Subtitle subtitle;
	
	public NodeAnnotationSubtitle(LocalizedString title, Project project) {
		super(title, project);
	}

	public NodeAnnotationSubtitle(String title, Project project) {
		super(title, project);;
	}

	public void setSubtitle(Subtitle subtitle) {
		this.subtitle = subtitle;
		firePropertyChange(PROP_SETCONTENT, null, subtitle);
	}
	
	public Subtitle getSubtitle() {
		if (subtitle == null) {
			String title = "";
			String description = "";
			if (getTitle() != null) {
				title = getTitle();
			}
			subtitle = new Subtitle(title, project);
			if (getDescription() != null) {
				description = getDescription();
			}
			subtitle.setDescription(description);
		}
		return this.subtitle;
	}

	@Override
	public List<IResource> getResources() {
		ArrayList<IResource> resources = new ArrayList<IResource>(1);
		if (subtitle != null) {
			resources.add(subtitle);
		}
		return resources;
	}
	
	@Override
	public boolean isDependentOn(IAbstractBean object) {
		return object != null && object == subtitle;
	}

	@Override
	public String getBeanTag() {
		return "Subtitle annotation";
	}
}
