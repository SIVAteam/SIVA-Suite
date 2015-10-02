package org.iviPro.newExport.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

/**
 * Mit dieser Klasse ist es möglich, die Ausgabeströme von FFMPEG zu überwachen.
 * 
 * @author Florian Stegmaier
 */
public class StreamHandler extends Thread {
	private static final Logger logger = Logger.getLogger(StreamHandler.class);
	InputStream is;
	String type;
	
	private String readString = "";

	public StreamHandler(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			if (type.equals("stderr")){
				while ((line = br.readLine()) != null) {
					logger.debug(type + "> " + line); //$NON-NLS-1$
					readString = readString + "\n" + line;
				}
			} else {
				while ((line = br.readLine()) != null) {
					logger.debug(type + "> " + line); //$NON-NLS-1$
					readString = readString + line + "\n";
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public String getString() {
		return this.readString;
	}
}
