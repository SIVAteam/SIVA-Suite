package org.iviPro.transcoding.exception;

/**
 * Thrown when an error occurs during the transcoding process.
 * 
 * @author Codebold
 *
 */
public class TranscodingException extends Exception {

	private static final long serialVersionUID = -2387771012808660062L;

	private final Reason reason;

	public TranscodingException(Reason reason) {
		super(reason.getMessage());
		this.reason = reason;
	}

	public TranscodingException(Reason reason, Throwable cause) {
		super(reason.getMessage(), cause);
		this.reason = reason;
	}

	public Reason getReason() {
		return reason;
	}

}
