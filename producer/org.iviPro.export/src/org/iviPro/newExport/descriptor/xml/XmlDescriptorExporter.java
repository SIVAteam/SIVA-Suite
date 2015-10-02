package org.iviPro.newExport.descriptor.xml;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.descriptor.xml.objects.ExporterFactory;
import org.iviPro.newExport.descriptor.xml.objects.IXMLExporter;
import org.iviPro.newExport.profile.Profile;
import org.iviPro.newExport.resources.ProjectResources;
import org.iviPro.newExport.xml2jsonconvertor.XML2JSONConvertor;
import org.iviPro.newExport.xml2jsonconvertor.XML2JSONConvertorException;
import org.iviPro.newExport.xml2jsonconvertor.convertors.IXML2JSONConvertor;
import org.iviPro.newExport.xml2jsonconvertor.convertors.SivaPlayerXML2JSONConvertor;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XmlDescriptorExporter implements XmlExportSettings {

	// @formatter:off
	private static final String LINE_DELIMITER = "========================================\n"; //$NON-NLS-1$
	private static final String LOG_DESCRIPTOR_CONTENT = "%s\n" + LINE_DELIMITER + "XML descriptor file (%s): \n" + LINE_DELIMITER + "%s\n" + LINE_DELIMITER; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String LOG_CREATE_DESCRIPTOR = "%s Creating XML descriptor file (%s) for project %s."; //$NON-NLS-1$
	// @formatter:on

	private static Logger logger = Logger
			.getLogger(XmlDescriptorExporter.class);

	private final Project project;
	private final Profile profile;
	private final File xmlFile;
	private final File jsonFile;
	private final ProjectResources projectResources;
	private final String loggerPrefix;

	public XmlDescriptorExporter(Project project, Profile profile,
			File outputFolder, ProjectResources projectResources) {
		this.project = project;
		this.profile = profile;
		this.xmlFile = new File(outputFolder, DESCRIPTOR_XML_FILENAME);
		this.jsonFile = new File(outputFolder, DESCRIPTOR_JSON_FILENAME);
		this.projectResources = projectResources;
		this.loggerPrefix = profile.getProfileTitle();
	}

	public void export() throws ExportException {
		logger.debug(String.format(LOG_CREATE_DESCRIPTOR, loggerPrefix,
				xmlFile.getAbsolutePath(), project.getTitle()));

		IdManager idManager = new IdManager(profile, projectResources);
		Document document = createDocument();
		
		// Set of already exported objects. Since different exporters export
		// different types, the general Object type has to be used here.
		HashSet<Object> alreadyExported = new HashSet<Object>();

		IXMLExporter exporter = ExporterFactory.createExporter(project);
		exporter.exportObject(document, idManager, project, alreadyExported);

		logger.debug(String.format(LOG_DESCRIPTOR_CONTENT, loggerPrefix,
				xmlFile.getAbsolutePath(), document.toString()));

		writeXmlFile(document);
		logger.debug(String.format(LOG_DESCRIPTOR_CONTENT, loggerPrefix,
				xmlFile.getAbsolutePath(), document.toString()));
		writeJsonFile();
	}

	private Document createDocument() throws ExportException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException cause) {
			logger.error(cause.getMessage());
			throw new ExportException(Messages.Exception_GettingDocumentBuilderFailed,
					cause);
		}

		Document document = builder.newDocument();
		document.setXmlVersion(XML_VERSION);
		document.setXmlStandalone(STANDALONE);
		document.setStrictErrorChecking(STRICT_ERROR_CHECKING);
		return document;
	}

	private void writeXmlFile(Document doc) throws ExportException {
		Transformer transformer = null;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException cause) {
			logger.error(cause.getMessage());
			throw new ExportException(Messages.Exception_GettingXmlTransformerFailed,
					cause);
		}
		transformer.setOutputProperty(OutputKeys.INDENT, INDENT);
		transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING);
		try {
			transformer.transform(new DOMSource(doc), new StreamResult(
					xmlFile));
		} catch (TransformerException cause) {
			logger.error(cause.getMessage());
			throw new ExportException(Messages.Exception_WritingDescriptorFileFailed,
					cause);
		}
	}
	
	/**
	 * Creates a JSON file from a given XML file.
	 * @param XMLfile provided XML file
	 * @throws ExportException if file manipulation causes an IOExecption 
	 * to be thrown
	 */
	private void writeJsonFile() throws ExportException {
		try {
			IXML2JSONConvertor sivaConfigurationConvertor = 
					new SivaPlayerXML2JSONConvertor();
			XML2JSONConvertor convertor = new XML2JSONConvertor(xmlFile, 
					jsonFile, sivaConfigurationConvertor, true,
					"if(!sivaVideoConfiguration){var sivaVideoConfiguration=[];}"
							+ ";sivaVideoConfiguration.push(",
					");");
			convertor.convert();
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new ExportException(Messages.Exception_WritingDescriptorFileFailed,
					e);
		} catch (SAXException e) {
			logger.error(e.getMessage());
			throw new ExportException(Messages.Exception_WritingDescriptorFileFailed,
					e);
		} catch (XML2JSONConvertorException e) {
			logger.error(e.getMessage());
			throw new ExportException(Messages.Exception_WritingDescriptorFileFailed,
					e);
		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage());
			throw new ExportException(Messages.Exception_WritingDescriptorFileFailed,
					e);
		}
	}
}
