package org.iviPro.newExport.profile.xml;

/**
 * Thrown when an error occurs during the transforming process.
 * 
 * @author Codebold
 * 
 */
public class TransformingException extends Exception {

	/**
	 * Specifies the version of this class for serialization purposes.
	 */
	private static final long serialVersionUID = 3013988919192865507L;

	/**
	 * Constructs a <code>TransformingException</code> with the specified detail
	 * message.
	 * 
	 * @param message
	 *            The detail message.
	 */
	public TransformingException(String message) {
		super(message);
	}

	/**
	 * Constructs a <code>TransformingException</code> from an existing
	 * exception with the specified detail message.
	 * <p>
	 * The existing exception will be embedded in the new one, but the new
	 * exception will have its own message.
	 * 
	 * @param message
	 *            The detail message.
	 * @param cause
	 *            The exception to be wrapped in the
	 *            <code>TransformingException</code>.
	 */
	public TransformingException(String message, Throwable cause) {
		super(message, cause);
	}
}
