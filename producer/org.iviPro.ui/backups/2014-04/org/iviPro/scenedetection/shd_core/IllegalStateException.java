package org.iviPro.scenedetection.shd_core;

public class IllegalStateException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String detailMessage;

    public IllegalStateException(String string) {
        detailMessage = string;
    }
}