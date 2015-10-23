package org.iviPro.newExport.descriptor.xml;

public interface IdDefinition {

	/**
	 * Prefix fuer alle Trigger-IDs
	 */
	public static final String PREFIX_TRIGGER = "trigger-"; //$NON-NLS-1$
	
	/**
	 * Prefix fuer alle IDs von Image-Ressourcen
	 */
	public static final String PREFIX_RES_IMAGE = "i_"; //$NON-NLS-1$

	/**
	 * Prefix fuer alle IDs von loadVideoAction Eintraegen
	 */
	public static final String PREFIX_LOAD_VIDEO_ACTION = "load-"; //$NON-NLS-1$

	/**
	 * Prefix fuer alle IDs von showSelectionControl Eintraegen
	 */
	public static final String PREFIX_NODE_SELECTION = "select-"; //$NON-NLS-1$

	/**
	 * Prefix fuer alle IDs von showQuizControl Eintraegen
	 */
	public static final String PREFIX_NODE_QUIZ = "quiz-"; //$NON-NLS-1$
	
	/**
	 * Prefix fuer alle IDs von randomSelection Eintraegen
	 */
	public static final String PREFIX_NODE_RANDOMSELECTION = "random-"; //$NON-NLS-1$

	/**
	 * Action-ID fuer alle Actions zum Anzeigen von INodeAnnotationLeaf-Objekten
	 */
	public static final String ACTIONID_SHOW_ANNOTATION = "show-"; //$NON-NLS-1$

	/**
	 * Action-ID fuer alle NodeEnd Objekte
	 */
	public static final String ACTIONID_ENDSIVA = "end-siva"; //$NON-NLS-1$

	/**
	 * Action-ID fuer alle Mark Nodes
	 */
	public static final String ACTIONID_NODEMARK = "show-"; //$NON-NLS-1$

	public static final String EMPTY = ""; //$NON-NLS-1$
	public static final String ID_SEPARATOR = "-"; //$NON-NLS-1$
	public static final String ENTITY_TYPE_SUFFIX = "_"; //$NON-NLS-1$
	public static final String EXTENSION_SEPARATOR = "."; //$NON-NLS-1$
}
