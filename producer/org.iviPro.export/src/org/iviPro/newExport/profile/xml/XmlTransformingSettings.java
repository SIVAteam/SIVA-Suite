package org.iviPro.newExport.profile.xml;

/**
 * Specifies the settings of transforming the export profiles from and to XML.
 * 
 * @author Codebold
 * 
 */
public interface XmlTransformingSettings {

	/** The encoding of the output file. */
	public static final String ENCODING = "UTF-8"; //$NON-NLS-1$

	/** The XML parameter for the transformer. */
	public static final String XML_OUTPUT = "xml"; //$NON-NLS-1$

	/** The XML version to use. */
	public static final String XML_VERSION = "1.0"; //$NON-NLS-1$

	/**
	 * Specifies whether the documents depends on external markup declarations.
	 * <p>
	 * The values must be <i>yes</i> or <i>no</i>.
	 */

	public static final String STANDALONE = "no"; //$NON-NLS-1$

	/**
	 * Specifies whether the transformer should output additional whitespace
	 * characters.
	 * <p>
	 * The values must be <i>yes</i> or <i>no</i>.
	 */
	public static final String INDENT = "yes"; //$NON-NLS-1$
}
