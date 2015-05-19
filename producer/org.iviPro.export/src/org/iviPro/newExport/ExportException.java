package org.iviPro.newExport;


/**
 * Thrown when an error occurs during the export process.
 * 
 * @author Codebold
 * 
 */
public class ExportException extends Exception {

	/**
	 * Specifies the version of this class for serialization purposes.
	 */
	private static final long serialVersionUID = 4613438839040756427L;

	/**
	 * Constructs a <code>ExportException</code> with the specified detail
	 * message.
	 * 
	 * @param message
	 *            The detail message.
	 */
	public ExportException(String message) {
		super(message);
	}

	/**
	 * Constructs a <code>ExportException</code> from an existing exception with
	 * the specified detail message.
	 * <p>
	 * The existing exception will be embedded in the new one, but the new
	 * exception will have its own message.
	 * 
	 * @param message
	 *            The detail message.
	 * @param cause
	 *            The exception to be wrapped in the
	 *            <code>ExportException</code>.
	 */
	public ExportException(String message, Throwable cause) {
		super(message, cause);
	}
}
