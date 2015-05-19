package org.iviPro.newExport.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;

/**
 * Provides some static methods for getting path names, or certain parts of
 * filenames like extensions.
 * 
 * @author dellwo, Codebold
 * 
 */
public class PathHelper {

	/**
	 * The name of the system property which holds the path to the directory 
	 * where to look for libraries and native programs.
	 * <b>Note:</b><br>
	 * Changes to this value have to be carried over to 
	 * <code>org.iviPro.utils.PathHelper</code> as well.
	 */
	private static final String SYSPROP_LIBDIR = "libdir"; //$NON-NLS-1$

	/**
	 * The name of the system property which holds the path to the directory 
	 * where application specific data is stored.
	 * <b>Note:</b><br>
	 * Changes to this value have to be carried over to 
	 * <code>org.iviPro.utils.PathHelper</code> as well.
	 */
	private static final String SYSPROP_SIVADIR = "user.siva"; //$NON-NLS-1$
	
	/** Specifies the path to the lib directory. */
	private static final String LIB_DIRECTORY = System.getProperty(SYSPROP_LIBDIR); //$NON-NLS-1$;
	
	/**
	 * Specifies the path to the application directory in the user's home
	 * directory.
	 */
	public static final String SIVA_USER_DIRECTORY = System.getProperty(SYSPROP_SIVADIR); //$NON-NLS-1$
		
	private static final String FILE_HTML_PLAYER_DIRECTORY = "HTML5player"; //$NON-NLS-1$

	private static final String FILE_FLASH_PLAYER_DIRECTORY = "player"; //$NON-NLS-1$

	private static final String FILE_FFMPEG_EXECUTABLE = "ffmpeg.exe"; //$NON-NLS-1$

	private static final String FILE_CUSTOM_EXPORT_PROFILES_DIRECTORY = "ExportProfiles"; //$NON-NLS-1$
	
	/** Specifies the path to the user defined profiles. */
	public static final File CUSTOM_EXPORT_PROFILES_DIRECTORY = new File(
			SIVA_USER_DIRECTORY, FILE_CUSTOM_EXPORT_PROFILES_DIRECTORY
					+ File.separator);

	/** Specifies the path to the ffmpeg executable. */
	public static final File FFMPEG_EXECUTABLE = new File(LIB_DIRECTORY
			+ File.separator + FILE_FFMPEG_EXECUTABLE);

	/** Specifies the path to the flash player directory. */
	public static final File FLASH_PLAYER_DIRECTORY = new File(LIB_DIRECTORY
			+ File.separator + FILE_FLASH_PLAYER_DIRECTORY + File.separator);

	/** Specifies the path to the HTML player directory. */
	public static final File HTML_PLAYER_DIRECTORY = new File(LIB_DIRECTORY
			+ File.separator + FILE_HTML_PLAYER_DIRECTORY + File.separator);

	/**
	 * Cuts the extension of a filename of, e.g. 'test.jpg' results in 'test'.
	 * 
	 * @param filename
	 *            The filename whose extensions should be cut off.
	 * @return The filename without extension. If the given filename is null,
	 *         the result will also be null.
	 */
	public static String getFilenameWithoutExtension(String filename) {
		if (filename != null) {
			int dotPos = filename.lastIndexOf('.');
			if (dotPos > 0) {
				return filename.substring(0, dotPos);
			} else {
				return filename;
			}
		}
		return null;
	}

	/**
	 * Gets the extension of the submitted file.
	 * 
	 * @param file
	 *            The file.
	 * @return The file extension or <code>null</code> if the file has no
	 *         extension.
	 */
	public static String getExtension(File file) {
		String filename = file.getName();
		int dotPos = filename.lastIndexOf('.') + 1;
		if (dotPos > 0 && dotPos < filename.length()) {
			String extension = filename.substring(dotPos);
			return extension;
		}
		return null;
	}

	/**
	 * Kopiert eine Datei in eine andere Datei.
	 * 
	 * @param in
	 *            Zu kopierende Datei
	 * @param out
	 *            Ziel-Datei
	 * @param overwrite
	 *            Wenn die Ziel-Datei existiert und overwrite auf true gesetzt
	 *            ist, wird die Zieldatei überschrieben. Ansonsten wird eine
	 *            IOException geworfen. Existiert die Ziel-Datei nicht, hat der
	 *            Parameter keine Bedeutung.
	 * @throws IOException
	 *             Falls beim Kopieren ein Fehler auftrat oder wenn die
	 *             Ziel-Datei existierte und overwrite auf false gesetzt ist.
	 */
	public static void copyFile(File in, File out, boolean overwrite)
			throws IOException {
		if (out.exists() && !overwrite) {
			throw new IOException("Target file does already exist."); //$NON-NLS-1$
		}
		// Ausgabe-Datei erstellen, falls sie noch nicht existiert.
		if (!out.exists()) {
			if (!out.createNewFile()) {
				throw new IOException(
						String.format(
								"File '%s' could not be created!", out.getAbsolutePath())); //$NON-NLS-1$
			}
		}
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			throw e;
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

	/**
	 * Kopiert alle Dateien aus einem Ordner in einen anderen Ordner. Eventuell
	 * vorhandene Dateien werden ueberschrieben, wenn overwrite auf true gesetzt
	 * ist.
	 * 
	 * @param srcDir
	 *            Das Quell-Verzeichnis
	 * @param targetDir
	 *            Das Ziel-Verzeichnis.
	 * @param overwrite
	 *            Gibt an ob eventuell schon vorhandene Dateien ueberschrieben
	 *            werden sollen.
	 * @param blacklist
	 *            Dateien mit diesen Namen werden nicht mitkopiert.
	 * 
	 * @throws IOException
	 *             Wenn beim Kopieren ein Fehler auftrat
	 */
	public static void copyFiles(File srcDir, File targetDir,
			boolean overwrite, Set<String> blacklist) throws IOException {
		// Falls Ziel-Verzeichnis nicht existiert, erstellen wir es
		if (!targetDir.exists()) {
			if (!targetDir.mkdirs()) {
				throw new IOException("Directory '" + targetDir //$NON-NLS-1$
						+ "' could not be created."); //$NON-NLS-1$
			}
		}
		// Teste das Quelle und Ziel les- bzw schreibbar sind.
		if (!(srcDir.exists() && srcDir.isDirectory() && srcDir.canRead())) {
			throw new IOException("Directory '" + srcDir + "' can't be read."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (!(targetDir.exists() && targetDir.isDirectory() && targetDir
				.canWrite())) {
			throw new IOException("Directory '" + targetDir //$NON-NLS-1$
					+ "' can't be written."); //$NON-NLS-1$
		}
		// Kopiere alle Dateien
		File[] childs = srcDir.listFiles();
		for (File src : childs) {
			String srcName = src.getName();
			// Nur wenn Datei nicht in Blacklist ist kopieren
			if (!blacklist.contains(srcName)) {
				File target = new File(targetDir, srcName);
				if (src.isFile()) {
					// Wenn child eine Datei ist, kopiere sie falls Ziel nicht
					// existiert oder ueberschrieben werden darf.
					if (!target.exists() || overwrite) {
						copyFile(src, target, overwrite);
					}
				} else {
					// Wenn child ein Verzeichnis ist, kopiere alls Dateien in
					// das
					// entsprechende Zielverzeichnis. Existiert das
					// Zielverzeichnis
					// noch nicht, wird es erstellt.
					copyFiles(src, target, overwrite, blacklist);
				}
			}
		}

	}

	/* Speichert das App-Data Directory, wo wir unsere Arbeitsdaten ablegen */
	private static File appDataDirectory = null;

	/**
	 * Gibt den Pfad auf das App-Data Verzeichnis zurueck
	 * 
	 * @param applicationId
	 *            Ein eindeutiger Identifier der Anwendung dessen App-Data
	 *            Verzeichnis zurueck gegeben werden soll.
	 * @return
	 */
	public static File getAppDataDirectory(String applicationId) {
		if (appDataDirectory != null) {
			return appDataDirectory;
		}
		File directory = null;
		String userHome = null;
		try {
			userHome = System.getProperty("user.home"); //$NON-NLS-1$
		} catch (SecurityException ignore) {
		}
		if (userHome != null) {
			OSId osId = getOSId();
			if (osId == OSId.WINDOWS) {
				File appDataDir = null;
				try {
					String appDataEV = System.getenv("APPDATA"); //$NON-NLS-1$
					if ((appDataEV != null) && (appDataEV.length() > 0)) {
						appDataDir = new File(appDataEV);
					}
				} catch (SecurityException ignore) {
				}
				if ((appDataDir != null) && appDataDir.isDirectory()) {
					// ${APPDATA}\${applicationId}
					String path = applicationId + "\\"; //$NON-NLS-1$
					directory = new File(appDataDir, path);
				} else {
					// ${userHome}\Application Data\${applicationId}
					String path = "Application Data\\" + applicationId + "\\"; //$NON-NLS-1$ //$NON-NLS-2$
					directory = new File(userHome, path);
				}
			} else if (osId == OSId.OSX) {
				// ${userHome}/Library/Application Support/${applicationId}
				String path = "Library/Application Support/" + applicationId //$NON-NLS-1$
						+ "/"; //$NON-NLS-1$
				directory = new File(userHome, path);
			} else {
				// ${userHome}/.${applicationId}/
				String path = "." + applicationId + "/"; //$NON-NLS-1$ //$NON-NLS-2$
				directory = new File(userHome, path);
			}
		}
		appDataDirectory = directory;
		return directory;
	}

	/*
	 * The following enum and method only exist to distinguish Windows and OSX
	 * for the sake of getDirectory().
	 */
	private enum OSId {
		WINDOWS, OSX, UNIX
	}

	private static OSId getOSId() {
		PrivilegedAction<String> doGetOSName = new PrivilegedAction<String>() {
			public String run() {
				return System.getProperty("os.name"); //$NON-NLS-1$
			}
		};
		OSId id = OSId.UNIX;
		String osName = AccessController.doPrivileged(doGetOSName);
		if (osName != null) {
			if (osName.toLowerCase().startsWith("mac os x")) { //$NON-NLS-1$
				id = OSId.OSX;
			} else if (osName.contains("Windows")) { //$NON-NLS-1$
				id = OSId.WINDOWS;
			}
		}
		return id;
	}

}
