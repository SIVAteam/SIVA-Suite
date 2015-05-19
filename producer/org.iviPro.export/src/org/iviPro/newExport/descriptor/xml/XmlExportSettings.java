package org.iviPro.newExport.descriptor.xml;

/**
 * Specifies the settings of transforming the export profiles from and to XML.
 * 
 * @author Codebold
 * 
 */
public interface XmlExportSettings {

	/** The name of the XML descriptor file. */
	public static final String DESCRIPTOR_XML_FILENAME = "export.xml"; //$NON-NLS-1$
	
	/** The name of the JSON descriptor file. */
	public static final String DESCRIPTOR_JSON_FILENAME = "export.js"; //$NON-NLS-1$

	/** The encoding of the output files. */
	public static final String ENCODING = "UTF-8"; //$NON-NLS-1$

	/** The XML version to use. */
	public static final String XML_VERSION = "1.0"; //$NON-NLS-1$

	/**
	 * Specifies whether the documents depends on external markup declarations.
	 * <p>
	 * The values must be <i>yes</i> or <i>no</i>.
	 */
	public static final boolean STANDALONE = false;

	/**
	 * Specifies whether the transformer should output additional whitespace
	 * characters.
	 * <p>
	 * The values must be <i>yes</i> or <i>no</i>.
	 */
	public static final String INDENT = "yes"; //$NON-NLS-1$

	/**
	 * Specifies whether error checking is enforced or not.
	 */
	public static final boolean STRICT_ERROR_CHECKING = true;
	
	/**
	 * Indentation for JSON file.
	 */
	public static final int JSON_INDENT_FACTOR = 2;
	
	/**
	 * Prefix written to the JSON file to make the data usable in java script.
	 */
	public static final String JSON_DATA_PREFIX = "if(!sivaVideoConfiguration){" 
			+ "\n " + "var sivaVideoConfiguration = [];"
			+ "\n" + "} " 
			+ "\n" + "sivaVideoConfiguration.push(";
	
	/**
	 * Suffix written to JSON file to make data usable in java script.
	 */
	public static final String JSON_DATA_SUFFIX = ");";
}
