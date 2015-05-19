package org.iviPro.newExport.profile.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.Profile;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parses the export profiles from XML.
 * 
 * @author Codebold
 * 
 */
public class ProfileParser implements XmlTransformingSettings {

	/**
	 * Parses the {@link Profile} from the submitted XML file.
	 * 
	 * @param file
	 *            The XML file containing the export profile definition.
	 * @return The parsed export profile.
	 * @throws TransformingException
	 *             If an error occurs during parsing the descriptor file.
	 */
	public static Profile parseProfile(File file) throws TransformingException {
		try {
			return parseProfile(new InputSource(new FileInputStream(file)));
		} catch (FileNotFoundException e) {
			throw new TransformingException(String.format(
					Messages.Exception_ProfileFileNotFound, file.toString()));
		}
	}

	/**
	 * Parses the {@link Profile} from the submitted source.
	 * 
	 * @param source
	 *            The source of the XML input.
	 * @return The parsed export profile.
	 * @throws TransformingException
	 *             If an error occurs during parsing the descriptor file.
	 */
	public static Profile parseProfile(InputSource source)
			throws TransformingException {
		try {
			String encoding = source.getEncoding();
			if (encoding == null) {
				source.setEncoding(ENCODING);
			}
			ProfileHandler handler = new ProfileHandler();
			SAXParserFactory.newInstance().newSAXParser()
					.parse(source, handler);
			return handler.getProfile();
		} catch (SAXException cause) {
			throw new TransformingException(
					Messages.Exception_ParsingProfileFileFailed, cause);
		} catch (IOException cause) {
			throw new TransformingException(
					Messages.Exception_ParsingProfileFileFailed, cause);
		} catch (ParserConfigurationException cause) {
			throw new RuntimeException(
					Messages.Exception_NoSaxTransformerFactory, cause);
		} catch (NumberFormatException cause) {
			// TODO Be more specific...
			throw new TransformingException("Profile contains invalid values!");
		} catch (NullPointerException cause) {
			// TODO Check all parsed attributes against null. Or leave it as it
			// is. There are many attributes...
			throw new TransformingException(
					"Profile's missing some attributes!");
		}
	}
}
