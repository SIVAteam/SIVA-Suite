package org.iviPro.mediaaccess;

public class MediaAccessException extends Exception {

	public MediaAccessException(String msg) {
		super(msg);
	}
	
	public MediaAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
