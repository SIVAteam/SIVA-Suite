package org.iviPro.model.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.iviPro.model.IMediaObject;
import org.iviPro.model.LocalizedElement;
import org.iviPro.model.LocalizedFile;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;

public class PdfDocument extends IMediaObject implements IResource {
	
	/**
	 * Der Name des "description"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_PDFDESCRIPTION = "pdfdescription"; //$NON-NLS-1$
	
	/**
	 * Stores localized summaries describing the pdf document
	 */
	private HashMap<Locale, LocalizedString> summaryMap = new HashMap<Locale, LocalizedString>(1);
	
	public PdfDocument(File file, Project project) {
		super(file, project);
	}
	
	@Override
	public List<LocalizedElement> getLocalizedContents() {
		return new ArrayList<LocalizedElement>(getFiles());
	}
	
	public String getSummary(Locale language) {
		if (summaryMap.containsKey(language)) {
			return summaryMap.get(language).getValue();
		} else if (summaryMap.containsKey(Locale.ROOT)) {
			return summaryMap.get(Locale.ROOT).getValue();
		} else {
			return null;
		}
	}
	
	public String getSummary() {
		Locale langCode = project.getCurrentLanguage();
		return getSummary(langCode);
	}
	
	public Collection<LocalizedString> getSummaries() {
		ArrayList<LocalizedString> summaries = new ArrayList<LocalizedString>(
				summaryMap.size());
		for (LocalizedString description : summaryMap.values()) {
			summaries.add(description);
		}
		return summaries;
	}
	
	public void setSummary(LocalizedString summary) {
		LocalizedString oldValue;
		if (summary.getValue() == null) {
			oldValue = summaryMap.remove(summary.getLanguage());
		} else {
			oldValue = summaryMap.put(summary.getLanguage(), summary);
		}
		firePropertyChange(PROP_PDFDESCRIPTION, oldValue, summary);
	}
	
	public void setSummary(String summary) {
		Locale curLang = project.getCurrentLanguage();
		setSummary(new LocalizedString(summary, curLang));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PdfDocument)) {
			return false;
		}
		PdfDocument pdfObj = (PdfDocument)obj;
			
		// Check equality of files
		Collection<LocalizedFile>pdfObjFiles = pdfObj.getFiles();
		if (getFiles().size() != pdfObjFiles.size()) {
			return false;
		} else {
			for (LocalizedFile file : getFiles()) {
				if (!pdfObjFiles.contains(file)) {
					return false;
				}
			}
		}
		
		// Check equality of summaries 
		Collection<LocalizedString>pdfObjSummaries = pdfObj.getSummaries();
		if (getSummaries().size() != pdfObjSummaries.size()) {
			return false;
		} else {
			for (LocalizedString summary : getSummaries()) {
				if (!pdfObjSummaries.contains(summary)) {
					return false;
				}
			}
		}
		return true;		
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		for (LocalizedFile file : getFiles()) {
			hash += file.hashCode();
		}
		for (LocalizedString string : getSummaries()) {
			hash += string.hashCode();
		}
		return hash;
	}
	
	@Override
	public String getBeanTag() {
		return "Pdf document";
	}
}
