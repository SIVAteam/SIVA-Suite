package org.iviPro.newExport.descriptor.xml.resources;

import java.io.IOException;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.iviPro.model.LocalizedElement;
import org.iviPro.model.LocalizedFile;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.RichText;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.Messages;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;

public class XMLResourceExporterRichtext extends IXMLResourceExporter {
	
	private static final String SIVA_IMG_CLASS = "class=\"sivaPlayer_richtextImage\"";
	private RichText richtext;
		
	public XMLResourceExporterRichtext(RichText richtext) {
		super(richtext);
		this.richtext = richtext;
	}

	private void addCDATAContent(Element contentElement,
			LocalizedFile file) throws ExportException {
		String fileContent =""; //$NON-NLS-1$
		try {
			fileContent = FileUtils.readFileToString(file.getValue());
		} catch (IOException e) {
			throw new ExportException(
					String.format(Messages.Exception_CannotReadResource,
							file.getAbsolutePath()));
		}
		
		// check for proper body tags 
		String[] splits = fileContent.split("</?body>"); //$NON-NLS-1$
		if (splits.length != 3) {
			throw new ExportException(
					String.format(Messages.Exception_WrongHtmlFileFormat,
							file.getAbsolutePath()));
		}
		
		fileContent = splits[1];
		fileContent = fileContent.replaceAll("\\s+", " ");
		fileContent = replacePictureReferences(fileContent, file.getLanguage());	
		CDATASection cdata = document.createCDATASection(fileContent);
		contentElement.appendChild(cdata);
		
	}
	
	/**
	 * Replaces references to pictures of the media repository within the given
	 * richtext with the location of the respective picture after its export.
	 * Additionally, the class attribute of the referencing <img> tags is used to
	 * distinguish these "local" references from references to images outside the
	 * media repository (which might exist in externally manipulated richtext 
	 * files). 
	 * @param richtextContent richtext in which references are replaced
	 * @param locale locale of the file in which the given content is stored
	 * @return content with replaced references
	 * @throws ExportException if an export path could not be determined
	 */
	private String replacePictureReferences(String richtextContent, Locale locale) 
			throws ExportException {
		for (Picture pic : richtext.getPictureSet()) {
			// Adding the " used in the src attribute to the search string allows 
			// for replacing and adding the class attribute in one go
			String filePath = pic.getFile().getValue().toURI().toString() + "\"";
			
			StringBuilder replacement = new StringBuilder();
			// Add export location (picture subfolder + filename)
			String exportPath = idManager.getImageFileName(idManager.getID(pic),
					pic.getFile(locale).getValue(), locale, pic);
			exportPath = exportPath.replaceAll("Files\\\\", "/").replaceAll("\\\\", "/");
			replacement.append(exportPath).append("\" ").append(SIVA_IMG_CLASS);
			richtextContent = richtextContent.replaceAll(filePath, replacement.toString());
			
		}
		return	richtextContent;
	}
	
	@Override
	protected void setAdditionalRessourceAttributes(Element resElement) {
	}
	
	@Override
	protected void setAdditionalContentAttributes(Element contentElement,
			LocalizedElement content) throws ExportException {
		LocalizedFile file = (LocalizedFile) content;		
		// Add reference to file
		String path = idManager.getRichPageFileName(idManager.getID(resource),
				file.getValue(), file.getLanguage());
		contentElement.setAttribute(ATTR_HREF, path);
		
		// Add content of richtext as CDATA to content element
		addCDATAContent(contentElement, file);
	}

	@Override
	protected String getResourceTag() {
		return TAG_RICHPAGE;
	}
}
