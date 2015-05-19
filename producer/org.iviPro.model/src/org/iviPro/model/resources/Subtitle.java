package org.iviPro.model.resources;

import java.util.ArrayList;
import java.util.List;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedElement;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;


/**
 * kapselt einen Subtitle, die Klasse wird benutzt um die Subtitles auch im Medien Rep 
 * anzuzeigen.
 * Der eigentliche Subtitle wird als Description gespeichert
 * @author juhoffma
 */
public class Subtitle extends IAbstractBean implements IResource {
	
	public Subtitle(LocalizedString title, Project project) {
		super(title, project);
	}
	
	public Subtitle(String title, Project project) {
		super(title, project);
	}

	@Override
	public List<LocalizedElement> getLocalizedContents() {
		return new ArrayList<LocalizedElement>(getDescriptions());
	}
}
