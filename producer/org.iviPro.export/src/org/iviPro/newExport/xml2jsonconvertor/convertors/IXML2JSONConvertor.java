package org.iviPro.newExport.xml2jsonconvertor.convertors;

import org.iviPro.newExport.xml2jsonconvertor.JSONNode;
import org.iviPro.newExport.xml2jsonconvertor.XML2JSONConvertorException;
import org.w3c.dom.Document;

/**
 * Interface for XML to JSON convertors.
 */
public interface IXML2JSONConvertor {
	
	/**
	 * Convert the XML document to the convertor's specific JSON structure.  
	 * @param xmlDocument contains the parsed XML document.
	 * @param rootNode contains the JSON root node.
	 * @throws XML2JSONConvertorException 
	 */
	public void convert(Document xmlDocument, JSONNode rootNode) throws XML2JSONConvertorException ;
}
