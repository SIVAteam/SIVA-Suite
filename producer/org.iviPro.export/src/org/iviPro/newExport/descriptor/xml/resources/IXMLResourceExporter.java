package org.iviPro.newExport.descriptor.xml.resources;

import java.util.Set;

import org.iviPro.model.LocalizedElement;
import org.iviPro.model.resources.IResource;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.iviPro.newExport.descriptor.xml.objects.SivaDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * An abstract implementation of exporters which export the necessary XML
 * structures needed for project {@link IResource resources} as defined
 * by the SIVA XML scheme.
 * @author John
 */
public abstract class IXMLResourceExporter implements SivaDefinition {
	
	/**
	 * The resource to export.
	 */
	protected IResource resource;
	protected Document document;
	protected IdManager idManager;
	
	/**
	 * Constructor for abstract resource exporters. 
	 * @param exportObj resource to export
	 */
	public IXMLResourceExporter(IResource exportObj) {
		resource = exportObj;
	}
	
	/**
	 * Exports necessary XML structure for the resource associated with this 
	 * exporter to the given document. Relies on the given
	 * <code>IdManager</code> for generation of resource ids and pathnames.
	 * 
	 * @param doc document to export to
	 * @param idManager <code>IdManager</code> handling ids and pathnames
	 * @param alreadyExported list of already exported objects
	 * @throws ExportException if a problem during the export occurs
	 */
	public void exportResource(Document doc, 
			IdManager idManager, Set<Object> alreadyExported) throws ExportException {
		if (alreadyExported.contains(resource)) {
			return;	
		}
		alreadyExported.add(resource);
				
		document = doc;
		this.idManager = idManager;
		
		Element resElement = doc.createElement(getResourceTag());
		resElement.setAttribute(ATTR_RES_ID, idManager.getID(resource));
		
		setAdditionalRessourceAttributes(resElement);
		addContentElements(resElement);		
		
		getRessources(document).appendChild(resElement);
	}
	
	/**
	 * Adds necessary XML sub-structures for the localized contents of the
	 * resource associated with this exporter to the given resource element.
	 * 
	 * @param resElement element to which structure is added
	 * @throws ExportException if a problem occurs during creation of the 
	 * structure
	 */
	private void addContentElements(Element resElement) throws ExportException {
		for (LocalizedElement content : resource.getLocalizedContents()) {
			Element contentElement = document.createElement(TAG_CONTENT);
			contentElement.setAttribute(ATTR_LANGCODE, content.getSivaLangcode());
			
			setAdditionalContentAttributes(contentElement, content);
			
			resElement.appendChild(contentElement);
		}		
	}	
	
	/**
	 * Returns the resource element of the given document.
	 * 
	 * @param doc document to search in
	 * @return resource element of the document as defined by <code>
	 * @throws ExportException
	 */
	protected Element getRessources(Document doc) throws ExportException {
		NodeList nodeList = doc.getDocumentElement().getElementsByTagName(
				TAG_RESOURCES);
		if (nodeList.getLength() == 1) {
			return (Element) nodeList.item(0);
		} else {
			throw new ExportException(
					"Error: ressources XML-Element could not be found."); //$NON-NLS-1$
		}
	}
	
	/**
	 * Returns the tag used to identify the type of the exported resource in the XML structure.
	 * @return XML tag for the resource.
	 */
	protected abstract String getResourceTag();
	/**
	 * Adds exporter specific attributes to the XML element representing the exported resource.
	 * @param resElement XML element representing the exported resource
	 */
	protected abstract void setAdditionalRessourceAttributes(Element resElement);
	/**
	 * Adds exporter specific attributes to the content element used within the XML element of
	 * the exported resource.
	 * @param contentElement content element to which attributes are added
	 * @param content content of the resource
	 * @throws ExportException
	 */
	protected abstract void setAdditionalContentAttributes(Element contentElement, LocalizedElement content) throws ExportException;
}
