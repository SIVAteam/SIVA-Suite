package org.iviPro.newExport.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.iviPro.newExport.Messages;

public class ZipUtils {
	private static final String PATH_DELIMITER = "/"; //$NON-NLS-1$
	private static final String EMPTY = ""; //$NON-NLS-1$

	public static void zip(File input, File output, boolean overwrite,
			boolean useTempDirectory) throws IOException {
		zip(input, output, overwrite, useTempDirectory,
				ZipOutputStream.DEFLATED, Deflater.DEFAULT_COMPRESSION);
	}

	public static void zip(File input, File output, boolean overwrite,
			boolean useTempDirectory, int method, int level) throws IOException {
		if (output.exists() && !overwrite) {
			throw new IOException(String.format(
					Messages.Exception_ZipOutputFileAlreadyExists,
					output.getAbsolutePath()));
		}

		if (useTempDirectory) {
			File tmpOutput = FileUtils.getTempPath(FileUtils.getTempDirecory(),
					output);
			zip(input, tmpOutput, method, level);
			IoUtils.copy(tmpOutput, output);
		} else {
			zip(input, output, method, level);
		}
	}

	private static void zip(File input, File output, int method, int level)
			throws IOException {
		ZipOutputStream out = null;
		try {
			out = new ZipOutputStream(new FileOutputStream(output));
			out.setMethod(method);
			out.setLevel(level);

			zip(input, out);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ignore) {
				// Ignores the exception.
			}
		}
	}

	public static void zip(Collection<File> inputs, File output,
			boolean overwrite, boolean useTempDirectory) throws IOException {
		zip(inputs, output, overwrite, useTempDirectory,
				ZipOutputStream.DEFLATED, Deflater.DEFAULT_COMPRESSION);
	}

	public static void zip(Collection<File> inputs, File output,
			boolean overwrite, boolean useTempDirectory, int method, int level)
			throws IOException {
		if (output.exists() && !overwrite) {
			throw new IOException(String.format(
					Messages.Exception_ZipOutputFileAlreadyExists,
					output.getAbsolutePath()));
		}

		if (useTempDirectory) {
			File tmpOutput = FileUtils.getTempPath(FileUtils.getTempDirecory(),
					output);
			zip(inputs, tmpOutput, method, level);
			FileUtils.copy(tmpOutput, output);
		} else {
			zip(inputs, output, method, level);
		}
	}

	private static void zip(Collection<File> inputs, File output, int method,
			int level) throws IOException {
		ZipOutputStream out = null;
		try {
			out = new ZipOutputStream(new FileOutputStream(output));
			out.setMethod(method);
			out.setLevel(level);
			for (File input : inputs) {
				zip(input, out);
			}
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ignore) {
				// Ignores the exception.
			}
		}
	}

	private static void zip(File input, ZipOutputStream out) throws IOException {
		if (input.isFile()) {
			zip(EMPTY, input.getName(), out);
		} else {
			for (String fileName : input.list()) {
				zip(EMPTY, input + PATH_DELIMITER + fileName, out);
			}
		}
	}

	private static void zip(String path, String input, ZipOutputStream out)
			throws IOException {
		File file = new File(input);
		String filePath = EMPTY.equals(path) ? file.getName() : path
				+ File.separator + file.getName();
		if (file.isDirectory()) {
			if (file.list().length > 0) {
				for (String fileName : file.list()) {
					zip(filePath, input + File.separator + fileName, out);
				}
			} else {
				out.putNextEntry(new ZipEntry(filePath + File.separator));
			}
		} else {
			out.putNextEntry(new ZipEntry(filePath));
			IoUtils.copy(file, out);
		}
	}

	public static void unzip(File zipfile, File directory) throws IOException {
		ZipFile zfile = new ZipFile(zipfile);
		Enumeration<? extends ZipEntry> entries = zfile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			File file = new File(directory, entry.getName());
			if (entry.isDirectory()) {
				file.mkdirs();
			} else {
				file.getParentFile().mkdirs();
				InputStream in = zfile.getInputStream(entry);
				try {
					IoUtils.copy(in, file);
				} finally {
					in.close();
				}
			}
		}
	}

}
