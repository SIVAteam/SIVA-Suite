package org.iviPro.transcoding.exception;

public interface Reason {

	public int getErrorCode();

	public String getIdentifier();

	public String getMessage();

	public String[] getDetails();
}
