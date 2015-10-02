package org.iviPro.newExport.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.xml.ProfileExporter;

public class FileUtils {
	
	private static final Logger logger = Logger
			.getLogger(FileUtils.class);

	public static final String TEMP_DIRECTORY = System
			.getProperty("java.io.tmpdir"); //$NON-NLS-1$

	public static final String USER_HOME = System.getProperty("user.home"); //$NON-NLS-1$

	/**
	 * Deletes the file or directory represented by the given <code>File</code>.
	 * @param file handle representing a file or directory
	 */
	public static void delete(File file) {
		if (!file.isFile()) {			
			File[] files = file.listFiles();
			for (File f : files) {
				delete(f);
			}
		}
		try {
			Files.delete(Paths.get(file.getAbsolutePath()));
		} catch (IOException e) {
			logger.error("Could not delete file " + file + ".");
		}
	}

	public static void clear(File directory) {
		if (!directory.exists() || !directory.isDirectory()) {
			throw new IllegalArgumentException(String.format(
					Messages.Exception_DirectoryNotFound,
					directory.getAbsolutePath()));
		} else {
			File[] files = directory.listFiles();
			for (File f : files) {
				delete(f);
			}
		}
	}

	public static File createSubdirectory(File directory,
			String subDirectoryName) throws IOException {
		File subDirectory = new File(directory.getAbsolutePath()
				+ File.separator + subDirectoryName);
		if (subDirectory.exists() && subDirectory.isDirectory()) {
			return subDirectory;
		} else if (!subDirectory.mkdirs()) {
			throw new IOException(String.format(
					Messages.Exception_CreatingDirectoryFailed,
					subDirectoryName, directory.getAbsolutePath()));
		} else {
			return subDirectory;
		}
	}

	public static void copy(File in, File out) throws IOException {
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

	public static void copyDirectoryContent(File sourceDirectory,
			File targetDirectory) throws IOException {
		if (!sourceDirectory.exists()) {
			throw new IOException("Source directory does not exist.");
		}
		for (File file : sourceDirectory.listFiles()) {
			if (file.isDirectory()) {
				File targetSubDirectory = createSubdirectory(targetDirectory,
						file.getName());
				copyDirectoryContent(file, targetSubDirectory);
			} else {
				copy(file, new File(targetDirectory, file.getName()));
			}
		}
	}

	public static File getTempPath(File tmpDirectory, File file) {
		return new File(tmpDirectory.toURI().resolve(file.getName()));
	}

	public static File getTempPath(File tmpDirectory, String file) {
		return new File(tmpDirectory.toURI().resolve(file));
	}

	public static File getTempDirecory() throws IOException {
		if (TEMP_DIRECTORY == null || TEMP_DIRECTORY.length() == 0) {
			throw new IOException(Messages.Exception_TempDirectoryNotAvailable);
		}
		File tmpDirecory = new File(TEMP_DIRECTORY);
		if (!tmpDirecory.exists()) {
			throw new IOException(Messages.Exception_TempDirectoryDoesNotExist);
		}
		if (!tmpDirecory.canWrite()) {
			throw new IOException(Messages.Exception_TempDirectoryNotWriteable);
		}
		return tmpDirecory;
	}
}
