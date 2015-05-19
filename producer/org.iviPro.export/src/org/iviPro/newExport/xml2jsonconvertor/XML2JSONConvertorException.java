package org.iviPro.newExport.xml2jsonconvertor;

/**
 * An Exception for issues occuring during the conversion.
 */
public class XML2JSONConvertorException extends Exception {

	private static final long serialVersionUID = 4456135422594366855L;

	public XML2JSONConvertorException() {
		super();
	}

	public XML2JSONConvertorException(String message) {
		super(message);
	}

	public XML2JSONConvertorException(String message, Throwable cause) {
		super(message, cause);
	}

	public XML2JSONConvertorException(Throwable cause) {
		super(cause);
	}

}
