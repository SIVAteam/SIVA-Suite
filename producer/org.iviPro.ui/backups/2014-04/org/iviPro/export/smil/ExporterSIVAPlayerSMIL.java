package org.iviPro.export.smil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.iviPro.export.smil.objects.SMILExporter;
import org.iviPro.export.smil.objects.SMILExporterFactory;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.NodeScene;
import org.w3c.dom.Document;

/**
 * Class to export a smil file according to the current application.
 * 
 * @author Emanuel Berndl
 * 
 */
public class ExporterSIVAPlayerSMIL {

	private static Logger logger = Logger.getLogger(ExporterSIVAPlayerSMIL.class);

	private IDManager idManager;

	/**
	 * Konstruktor mit idManager.
	 * 
	 * @param idManager
	 *            Der zu übergebende idManager.
	 */
	public ExporterSIVAPlayerSMIL(IDManager idManager) {
		this.idManager = idManager;
	}

	public Collection<NodeScene> export(File file) throws ExportException {
		logger.info("Exporting into file: " + file.getAbsolutePath());

		// Get the current project
		Project project = Application.getCurrentProject();

		// Read the modelgraph and its nodes
		Graph modelGraph = project.getSceneGraph();
		Collection<NodeScene> allNodes = getAllNodeScene(modelGraph);

		Document document = createSmilDocument();
		
		HashSet<IAbstractBean> alreadyExported = new HashSet<IAbstractBean>();
		SMILExporter exporter = SMILExporterFactory.createSMILExporter(project);
		exporter.exportObject(document, idManager, project, alreadyExported, null);

		logger.debug("\n\n\n"); //$NON-NLS-1$
		logger.debug("Resulting SIVA Player SMIL:"); //$NON-NLS-1$
		logger.debug("======================================================\n" //$NON-NLS-1$
				+ document);
		logger.debug("======================================================"); //$NON-NLS-1$
		logger.debug("\n\n\n"); //$NON-NLS-1$

		logger.debug("Writing SMIL file: " + file.getAbsolutePath()); //$NON-NLS-1$

		writeSmilFile(file, document);
		
		changeFileToSmil(file);

		return allNodes;
	}

	private Document createSmilDocument() throws ExportException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new ExportException(e);
		}
		Document document = builder.newDocument();

		document.setXmlVersion("1.0");
		document.setStrictErrorChecking(true);
		return document;
	}

	@SuppressWarnings("unchecked")
	private Collection<NodeScene> getAllNodeScene(Graph modelGraph) {
		return (Collection) modelGraph.searchNodes(NodeScene.class, true);
	}

	private void writeSmilFile(File file, Document doc) throws ExportException {
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

	/**
	 * The file that has been built up to the call of this function is a xml
	 * file (so the first line corresponds to an xml header). In order to make
	 * this file a .smil file and consecutively readable for smil players, the
	 * first line of the xml file has to be deleted, which is done by this
	 * function.
	 * 
	 * @param file
	 *            The file to be changed.
	 * @return A proper smil file, according to the file produced until now.
	 * @throws IOException
	 */
	private void changeFileToSmil(File file) throws ExportException {
		// TODO May be implemented in a more efficient way
		try {
			BufferedReader reader = new BufferedReader(
			        new InputStreamReader(new FileInputStream(file), "UTF8"));
			
			StringBuffer buffer = new StringBuffer();

			// Read out all of the file except the first line
			String readLine = null;
			boolean firstLine = true;
			while ((readLine = reader.readLine()) != null) {
				
				if (!firstLine) {
					if(readLine.startsWith("<smilText")) {
						readLine = changeFileLine(readLine);
					}
					buffer.append(readLine + "\n");
				} else {
					firstLine = false;
				}
				
			}

			if (reader != null) {
				reader.close();
			}

			// Write the new file onto the old one
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(buffer.toString());
			writer.flush();

			if (writer != null) {
				writer.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			throw new ExportException("IOException occured while exporting: " + e.getMessage());
		}
	}
	
	/**
	 * Method changes some HTML-sequences into something that SMIL can display and work with.
	 * 
	 * @param line	The line that is to be checked for sequences.
	 */
	private String changeFileLine (String line) {
		String replacement = line;
		
		replacement = replacement.replaceAll("&lt;br/&gt;", "<br/>");
		
		return replacement;
	}
}
