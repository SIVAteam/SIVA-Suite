package org.iviPro.transcoding.process;

import java.io.IOException;
import java.io.InputStream;

import org.iviPro.transcoding.util.Constant;

public class ProcessOutputReader implements Runnable {

	private final InputStream in;
	private final StringBuilder output;

	public ProcessOutputReader(InputStream in) {
		this.in = in;
		this.output = new StringBuilder("");
	}

	@Override
	public void run() {
		byte[] buffer = new byte[1024];
		try {
			for (int n = 0; n != -1; n = in.read(buffer)) {
				byte[] read = new byte[n];
				System.arraycopy(buffer, 0, read, 0, n);
				output.append(new String(read));
			}
		} catch (IOException e) {
			output.append(Constant.LINE_FEED + e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					output.append(Constant.LINE_FEED + e.getMessage());
				}
			}
		}
	}

	public String getOutput() {
		return output.toString();
	}

}
