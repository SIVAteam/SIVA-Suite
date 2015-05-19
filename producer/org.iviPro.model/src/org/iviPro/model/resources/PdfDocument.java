package org.iviPro.model.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.iviPro.model.IMediaObject;
import org.iviPro.model.LocalizedElement;
import org.iviPro.model.Project;

public class PdfDocument extends IMediaObject implements IResource {
	
	private static Logger logger = Logger.getLogger(RichText.class);

	public PdfDocument(File file, Project project) {
		super(file, project);
	}

	@Override
	public List<LocalizedElement> getLocalizedContents() {
		return new ArrayList<LocalizedElement>(getFiles());
	}
}
