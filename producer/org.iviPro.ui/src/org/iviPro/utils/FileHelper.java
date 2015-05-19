package org.iviPro.utils;

import java.io.File;

public class FileHelper {

	/**
	 * Löscht eine beliebige Datei, wenn die übergebene Datei ein Ordner
	 * ist, wird dieser rekursiv gelöscht.
	 * 
	 * @param path Pfad zur Datei/Ordner
	 */
	public static void delete(File path) {
		if (path.isFile()) {
			path.delete();
		} else {
			File [] files = path.listFiles();
			for (File f : files) {
				delete(f);
			}
			path.delete();
		}
	}
}
