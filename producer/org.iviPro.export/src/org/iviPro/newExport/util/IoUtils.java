package org.iviPro.newExport.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IoUtils {

	public static final int DEFAULT_BUFFER_SIZE = 4096;

	public static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		while (true) {
			int readCount = in.read(buffer);
			if (readCount < 0) {
				break;
			}
			out.write(buffer, 0, readCount);
		}
	}

	public static void copy(File file, OutputStream out) throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			copy(in, out);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public static void copy(InputStream in, File file) throws IOException {
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			copy(in, out);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	public static void copy(File source, File target) throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(source);
			out = new FileOutputStream(target);
			copy(in, out);
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}
}
