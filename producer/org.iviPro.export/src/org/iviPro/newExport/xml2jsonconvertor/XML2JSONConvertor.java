package org.iviPro.newExport.xml2jsonconvertor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.iviPro.newExport.xml2jsonconvertor.convertors.IXML2JSONConvertor;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class XML2JSONConvertor {

	private File xmlFile;
	private File jsonFile;
	private JSONNode json;
	private IXML2JSONConvertor convertor;
	private boolean formatJSONOutput;
	private String outputBefore;
	private String outputAfter;
	
	/**
	 * Create a new instance of XML2JSONConvertor by specifying the XML input file
	 * and the JSON output file.
	 * @param xmlFile to convert.
	 * @param jsonFile to be converted.
	 * @param formatJSONOutput true if spaces should be used in JSON output for better
	 * 			reading, false if a compressed JSON output should be generated. 
	 * @param outputBefore contains a JavaScript string that should be written before
	 * 			JSON.
	 * @param outputAfter contains a JavaScript string that should be written after
	 * 			JSON.
	 * @throws FileNotFoundException 
	 */
	public XML2JSONConvertor(File xmlFile, File jsonFile, IXML2JSONConvertor convertor, boolean formatJSONOutput, String outputBefore, String outputAfter) throws FileNotFoundException {
		if(!xmlFile.exists()){
			throw new FileNotFoundException("Could not find specified XML file.");
		}
		this.xmlFile = xmlFile;
		this.jsonFile = jsonFile;
		this.convertor = convertor;
		this.formatJSONOutput = formatJSONOutput;
		this.outputBefore = outputBefore;
		this.outputAfter = outputAfter;
	}

	/**
	 * Perform conversion of the XML file to a JSON file.
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws XML2JSONConvertorException 
	 */
	public void convert() throws ParserConfigurationException, SAXException, IOException, XML2JSONConvertorException {
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.xmlFile);
		document.getDocumentElement().normalize();
		this.json = new JSONNode(new LinkedHashMap<String, JSONNode>());
		this.convertor.convert(document, this.json);
		this.writeJSONFile();
	}
	
	/**
	 * Write generated JSON object to specified JSON file.
	 * @throws IOException
	 */
	private void writeJSONFile() throws IOException{
		FileWriter writer = null;
		try {
			writer = new FileWriter(this.jsonFile);
			writer.write(this.outputBefore);
			if(this.formatJSONOutput){
				writer.write(json.toString(0));
			}
			else{
				writer.write(json.toString());
			}
			writer.write(this.outputAfter);
		} catch (IOException e) {
			throw e;
		}
		finally{
			if(writer != null){
				writer.close();
			}
		}
	}
}
