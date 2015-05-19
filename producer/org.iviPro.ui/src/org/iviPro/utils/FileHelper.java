package org.iviPro.utils;

import java.io.File;

public class FileHelper {

	/**
	 * L�scht eine beliebige Datei, wenn die �bergebene Datei ein Ordner
	 * ist, wird dieser rekursiv gel�scht.
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
