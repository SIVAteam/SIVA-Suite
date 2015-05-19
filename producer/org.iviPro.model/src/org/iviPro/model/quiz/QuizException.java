package org.iviPro.model.quiz;

public class QuizException extends Exception {

	/**
	 * The version id of this class for serialization purposes.
	 */
	private static final long serialVersionUID = -3645602236009026551L;

	public QuizException(String message, Throwable cause) {
		super(message, cause);
	}

	public QuizException(String message) {
		super(message);
	}

	public QuizException(Throwable cause) {
		super(cause);
	}

}
