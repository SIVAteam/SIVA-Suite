package org.iviPro.export.xml;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.iviPro.application.Application;
import org.iviPro.export.ExportException;
import org.iviPro.export.xml.objects.ExporterFactory;
import org.iviPro.export.xml.objects.IXMLExporter;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.NodeScene;
import org.w3c.dom.Document;

public class ExporterSIVAPlayerXML {

	private static Logger logger = Logger
			.getLogger(ExporterSIVAPlayerXML.class);
	// ID-Manager anlegen
	private IDManager idManager;

	public ExporterSIVAPlayerXML(IDManager idManager) {
		this.idManager = idManager;
	}

	/**
	 * Startet den Export und speichert das erzeugte XML-File in der angegebenen
	 * Datei.
	 * 
	 * @param file
	 * @throws ExportException
	 */
	public Collection<NodeScene> export(File file) throws ExportException {
		logger.info("Exporting into file: " + file.getAbsolutePath()); //$NON-NLS-1$

		Project project = Application.getCurrentProject();

		// Alle Szenen-Knoten im Graphen ermitteln
		org.iviPro.model.graph.Graph modelGraph = project.getSceneGraph();
		Collection<NodeScene> allSceneNodes = getAllNodeScene(modelGraph);

		Document document = createDocument();

		HashSet<IAbstractBean> alreadyExported = new HashSet<IAbstractBean>();
		IXMLExporter exporter = ExporterFactory.createExporter(project);
		exporter.exportObject(document, idManager, project, alreadyExported);

		logger.debug("\n\n\n"); //$NON-NLS-1$
		logger.debug("Resulting SIVA Player XML:"); //$NON-NLS-1$
		logger.debug("======================================================\n" //$NON-NLS-1$
				+ document);
		logger.debug("======================================================"); //$NON-NLS-1$
		logger.debug("\n\n\n"); //$NON-NLS-1$

		logger.debug("Writing XML file: " + file.getAbsolutePath()); //$NON-NLS-1$
		writeXmlFile(file, document);

		return allSceneNodes;

	}

	private Document createDocument() throws ExportException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// Schema schema = getSchema();
		// factory.setSchema(schema);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new ExportException(e);
		}
		Document document = builder.newDocument();
		document.setXmlVersion("1.0"); //$NON-NLS-1$
		document.setStrictErrorChecking(true);
		return document;
	}

	private void writeXmlFile(File file, Document doc) throws ExportException {
		try {
			// Prepare the DOM document for writing
			Source source = new DOMSource(doc);

			// Prepare the output file
			Result result = new StreamResult(file);

			// Write the DOM document to the file
			Transformer xformer = TransformerFactory.newInstance()
					.newTransformer();
			xformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

			xformer.transform(source, result);
		} catch (Exception e) {
			throw new ExportException(e);
		}

	}

	@SuppressWarnings("unchecked")
	private Collection<NodeScene> getAllNodeScene(Graph modelGraph) {
		return (Collection) modelGraph.searchNodes(NodeScene.class, true);
	}
}
