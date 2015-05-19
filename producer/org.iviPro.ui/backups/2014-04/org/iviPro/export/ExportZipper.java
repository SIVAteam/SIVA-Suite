package org.iviPro.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Diese Klasse dient zum Zippen der exportierten Dateien.
 * 
 * @author langa
 * 
 */
public class ExportZipper {

	/**
	 * Erstellt einen neuen ExportZipper und zippt den zuvor in ein
	 * tempor&auml;res Verzeichnis geschriebenen Export
	 * 
	 * @param zipDir
	 *            Verzeichnis in dem die Zipdatei liegen soll
	 * @param exportDir
	 *            Verzeichnis in dem der Export liegt (tempor&auml;res
	 *            Verzeichnis)
	 * @param zipFileName
	 *            Dateiname der Zipdatei
	 * @throws IOException
	 */
	public ExportZipper(String zipDir, String exportDir, String zipFileName)
			throws IOException {

		File zipFile = new File(zipDir + zipFileName);
		if (zipFile.exists()) {
			zipFile.delete();
		}
		ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(
				zipFile));
		zipOut.setLevel(5);
		addFileToZip("", exportDir, zipOut); //$NON-NLS-1$
		zipOut.close();
	}

	private void addFileToZip(String path, String srcFile, ZipOutputStream zip)
			throws IOException {

		// tmp/ aus Pfad entfernen
		path = (path.equals(Exporter.EXPORT_ZIP_TMP) || path.equals("")) ? "" //$NON-NLS-1$ //$NON-NLS-2$
				: path + File.separator;

		File file = new File(srcFile);
		if (file.isDirectory()) {
			if (file.list().length > 0) {
				addFolderToZip(path, srcFile, zip);
			} else {
				// Leere Verzeichnis schreiben
				file.mkdirs();
				zip.putNextEntry(new ZipEntry(path + file.getName()
						+ File.separator));
			}
		} else {
			byte[] buf = new byte[1024];
			int len;
			FileInputStream in = new FileInputStream(srcFile);
			zip.putNextEntry(new ZipEntry(path + file.getName()));
			while ((len = in.read(buf)) > 0) {
				zip.write(buf, 0, len);
			}
			in.close();
		}
	}

	private void addFolderToZip(String path, String srcFolder,
			ZipOutputStream zip) throws IOException {
		File folder = new File(srcFolder);
		folder.mkdirs();

		for (String fileName : folder.list()) {
			if (path.equals("")) { //$NON-NLS-1$
				addFileToZip(folder.getName(), srcFolder + File.separator
						+ fileName, zip);
			} else {
				addFileToZip(path + folder.getName(), srcFolder
						+ File.separator + fileName, zip);
			}
		}
	}

}
