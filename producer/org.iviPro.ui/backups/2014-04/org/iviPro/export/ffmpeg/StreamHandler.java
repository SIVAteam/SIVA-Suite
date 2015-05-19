package org.iviPro.export.ffmpeg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.iviPro.utils.PathHelper;

/**
 * Mit dieser Klasse ist es möglich, die Ausgabeströme von FFMPEG zu überwachen.
 * 
 * @author Florian Stegmaier
 */
public class StreamHandler extends Thread {
	private static Logger logger = Logger.getLogger(StreamHandler.class);
	InputStream is;
	String type;
	
	private boolean isFinished = false;
	
	private String readString = "";

	public StreamHandler(InputStream is, String type) {
		PropertyConfigurator.configure(PathHelper.getPathToLoggerIni());
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
					if (line != null) {
						readString = readString + "\n" + line;
					}
				}
			} else {
				while ((line = br.readLine()) != null) {
					logger.debug(type + "> " + line); //$NON-NLS-1$
					if (line != null) {
						readString = readString + line + "\n";
					}
				}
			}
			this.isFinished = true;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			this.isFinished = true;
		}
	}
	
	public boolean isFinished() {
		return this.isFinished;
	}
	
	public String getString() {
		return this.readString;
	}
}
